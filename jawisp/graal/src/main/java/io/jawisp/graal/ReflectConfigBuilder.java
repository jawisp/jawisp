package io.jawisp.graal;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class ReflectConfigBuilder {
    private final Set<Class<?>> discovered = new HashSet<>();
    private final Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());

    public void buildFrom(Object root, Path outputPath) {
        // 1. Object graph traversal (existing logic)
        traverseObjectGraph(root);
        
        // 2. Package scanning from @Application root package
        scanApplicationPackages(root.getClass());
        
        writeJson(outputPath);
    }

    private void traverseObjectGraph(Object root) {
        if (root == null) return;
        walk(root);
        discoverFromType(root.getClass());
    }

    private void scanApplicationPackages(Class<?> appClass) {
        String appPackage = appClass.getPackageName();
        Set<String> projectPackages = findProjectPackages(appPackage);
        
        for (String pkg : projectPackages) {
            scanPackage(pkg);
        }
    }

    private Set<String> findProjectPackages(String appPackage) {
        Set<String> packages = new HashSet<>();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        
        // Scan all packages under app package and siblings
        try {
            Enumeration<URL> resources = cl.getResources("");
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                if ("file".equals(url.getProtocol())) {
                    scanDirectory(new java.io.File(url.getFile()), appPackage, packages);
                } else if ("jar".equals(url.getProtocol())) {
                    scanJar(url, appPackage, packages);
                }
            }
        } catch (Exception e) {
            // Fallback: common project packages
            packages.add(appPackage);
            packages.add(appPackage + ".controller");
            packages.add(appPackage + ".service"); 
            packages.add(appPackage + ".model");
            packages.add(appPackage + ".config");
            packages.add(appPackage + ".repository");
        }
        
        return packages;
    }

    private void scanDirectory(java.io.File rootDir, String appPackage, Set<String> packages) {
        String appPath = appPackage.replace('.', '/');
        if (rootDir.getAbsolutePath().contains(appPath)) {
            packages.add(appPackage);
        }
        // Add common subpackages
        packages.add(appPackage + ".controller");
        packages.add(appPackage + ".service");
        packages.add(appPackage + ".model");
    }

    private void scanJar(URL jarUrl, String appPackage, Set<String> packages) {
        try (JarFile jar = new JarFile(new java.io.File(jarUrl.getFile().split("!")[0].replace("file:", "")))) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.endsWith(".class") && name.startsWith(appPackage.replace('.', '/'))) {
                    packages.add(extractPackage(name));
                }
            }
        } catch (IOException ignored) {}
    }

    private String extractPackage(String className) {
        return className.substring(0, className.lastIndexOf('/')).replace('/', '.');
    }

    private void scanPackage(String packageName) {
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> resources = cl.getResources(packageName.replace('.', '/'));
            
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                if ("file".equals(resource.getProtocol())) {
                    scanFileDirectory(new java.io.File(resource.getFile()), packageName);
                }
            }
        } catch (IOException e) {
            // Ignore classpath issues
        }
    }

    private void scanFileDirectory(java.io.File directory, String packageName) {
        java.io.File[] files = directory.listFiles();
        if (files != null) {
            for (java.io.File file : files) {
                if (file.isDirectory()) {
                    scanFileDirectory(file, packageName + "." + file.getName());
                } else if (file.getName().endsWith(".class")) {
                    String className = packageName + "." + 
                        file.getName().substring(0, file.getName().length() - 6);
                    try {
                        Class<?> clazz = Class.forName(className);
                        if (isProjectClass(clazz)) {
                            discovered.add(clazz);
                        }
                    } catch (ClassNotFoundException ignored) {}
                }
            }
        }
    }

    // Existing traversal methods (unchanged)
    private void discoverFromType(Class<?> type) {
        if (type == null || type == Object.class || discovered.contains(type)) return;
        discovered.add(type);
        
        Class<?> c = type;
        while (c != null && c != Object.class) {
            for (Field f : c.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers())) continue;
                Class<?> fieldType = f.getType();
                if (isProjectClass(fieldType)) {
                    discoverFromType(fieldType);
                }
                Type genericType = f.getGenericType();
                if (genericType instanceof ParameterizedType pt) {
                    for (Type arg : pt.getActualTypeArguments()) {
                        if (arg instanceof Class<?> cls && isProjectClass(cls)) {
                            discoverFromType(cls);
                        }
                    }
                }
            }
            c = c.getSuperclass();
        }
    }

    private void walk(Object obj) {
        if (obj == null) return;
        Class<?> type = obj.getClass();
        if (!visited.add(obj)) return;
        discoverClass(type);
        
        Class<?> c = type;
        while (c != null && c != Object.class) {
            for (Field f : c.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers())) continue;
                f.setAccessible(true);
                try {
                    Object value = f.get(obj);
                    if (value != null && isProjectClass(value.getClass())) {
                        walk(value);
                    }
                } catch (IllegalAccessException ignored) {}
            }
            c = c.getSuperclass();
        }
    }

    private boolean isProjectClass(Class<?> clazz) {
        if (clazz == null || clazz.isPrimitive() || clazz.isArray() || 
            clazz == String.class || clazz.getName().startsWith("java.") ||
            clazz.getName().startsWith("javax.")) {
            return false;
        }
        return true;
    }

    private void discoverClass(Class<?> clazz) {
        if (isProjectClass(clazz)) {
            discovered.add(clazz);
            discoverFromType(clazz);
        }
    }

    private void writeJson(Path outputPath) {
        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(
                Files.newOutputStream(outputPath), StandardCharsets.UTF_8))) {
            
            out.println("[");
            List<Class<?>> sorted = new ArrayList<>(discovered);
            sorted.sort(Comparator.comparing(Class::getName));
            
            for (int i = 0; i < sorted.size(); i++) {
                Class<?> clazz = sorted.get(i);
                out.println("  {");
                out.println("    \"name\": \"" + escapeJson(clazz.getName()) + "\",");
                out.println("    \"allDeclaredConstructors\": true,");
                out.println("    \"allPublicConstructors\": true,");
                out.println("    \"allDeclaredMethods\": true,");
                out.println("    \"allPublicMethods\": true,");
                out.print("    \"allPrivateMethods\": true");
                
                // Add fields section if the class has non-static fields
                Field[] fields = clazz.getDeclaredFields();
                List<Field> nonStaticFields = new ArrayList<>();
                for (Field field : fields) {
                    if (!Modifier.isStatic(field.getModifiers())) {
                        nonStaticFields.add(field);
                    }
                }
                
                if (!nonStaticFields.isEmpty()) {
                    out.println(",\n    \"fields\": [");
                    for (int j = 0; j < nonStaticFields.size(); j++) {
                        Field field = nonStaticFields.get(j);
                        out.println("      {");
                        out.println("        \"name\": \"" + escapeJson(field.getName()) + "\"");
                        out.println(j < nonStaticFields.size() - 1 ? "      }," : "      }");
                    }
                    out.println("    ]");
                } else {
                    out.println("");
                }
                
                out.println(i < sorted.size() - 1 ? "  }," : "  }");
            }
            out.println("]");
            out.flush();
            out.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to write reflect-config.json", e);
        }
    }
    
    private static String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }
}
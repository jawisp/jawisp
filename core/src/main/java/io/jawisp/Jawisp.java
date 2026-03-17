package io.jawisp;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jawisp.config.Config;
import io.jawisp.config.PropertyReader;
import io.jawisp.http.HttpServer;
import io.jawisp.http.netty.NettyServer;

/**
 * The Jawisp class is the main entry point for the JAWISP application.
 * It initializes the HTTP server and manages its lifecycle.
 *
 * @author Taras Chornyi
 * @since 1.0.0
 */
public class Jawisp {

    private static final Logger logger = LoggerFactory.getLogger(Jawisp.class);
    private static PropertyReader property = PropertyReader.getInstance();

    // Dev mode static state
    private static final AtomicBoolean shouldRestart = new AtomicBoolean(false);
    private static volatile long lastChangeTime = 0;
    private static final long DEBOUNCE_MS = 1500;
    private static volatile boolean devModeActive = false;

    private final Config config;
    private final HttpServer server;
    private long start = System.nanoTime();
    private final Thread devWatcherThread;

    static {
        logger.info("JAWISP v1.0.0 starting ...");
    }

    private Jawisp(Config config) {
        this.config = config;

        var isDev = property.get("jawisp.devtools.livereload.enabled").asBoolean().orElse(false);
        if (isDev) {
            logger.info("DEV MODE enabled - Hot reload active");
        }

        AtomicInteger index = new AtomicInteger(1);
        config.getRoutes().stream()
                .forEach(route -> logger.info("Route[{}]: {} {}",
                        index.getAndIncrement(),
                        route.getMethod().name(),
                        route.getPath()));

        this.server = new NettyServer(config);

        // Start dev watcher if dev mode enabled
        if (isDev) {
            devModeActive = true;
            this.devWatcherThread = new Thread(this::watchDevFiles, "Jawisp-HotReload");
            this.devWatcherThread.setDaemon(true);
            this.devWatcherThread.start();
        } else {
            this.devWatcherThread = null;
        }
    }

    public static Jawisp build() {
        return build(config -> {
        });
    }

    public static Jawisp build(Consumer<Config> config) {
        Config cfg = new Config();
        config.accept(cfg);
        return new Jawisp(cfg);
    }

    public Jawisp start() {
        startServer();

        // If dev mode, enter watch loop
        if (devModeActive) {
            devLoop();
        }

        return this;
    }

    private void devLoop() {
        while (true) {
            if (shouldRestart.getAndSet(false)) {
                // Code changed! Restarting JVM...
                System.exit(1);
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void watchDevFiles() {
        try {
            WatchService watcher = FileSystems.getDefault().newWatchService();
            // Path projectRoot = Paths.get("src/main").toRealPath(); // Project root
            Path projectRoot = Paths.get(System.getProperty("user.dir")).toRealPath();

            // Find ALL src/main directories across all modules
            List<Path> srcMainDirs = new ArrayList<>();
            Files.walkFileTree(projectRoot, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (isSrcMainDir(dir)) {
                        srcMainDirs.add(dir.toRealPath());
                    }
                    return FileVisitResult.CONTINUE;
                }
            });

            // Register watch on each src/main and all subdirectories
            for (Path srcMainDir : srcMainDirs) {
                Files.walkFileTree(srcMainDir, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path subdir, BasicFileAttributes attrs)
                            throws IOException {
                        subdir.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
                        return FileVisitResult.CONTINUE;
                    }
                });
            }

            // Rest of your existing event loop (unchanged)
            while (true) {
                WatchKey key = watcher.poll(1, TimeUnit.SECONDS);
                if (key != null) {
                    boolean hasChange = false;
                    for (WatchEvent<?> event : key.pollEvents()) {
                        if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                            Path changed = (Path) event.context();
                            String fileName = changed.toString();

                            if (fileName.endsWith(".java")) {
                                long now = System.currentTimeMillis();
                                if (now - lastChangeTime > DEBOUNCE_MS) {
                                    logger.info("📝 Hot reload detected: {}", fileName);
                                    lastChangeTime = now;
                                    hasChange = true;
                                }
                            }
                        }
                    }
                    key.reset();

                    if (hasChange) {
                        shouldRestart.set(true);
                        if (server != null) {
                            // Stopping existing server...
                            server.stop();
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Hot reload watcher failed: {}", e.getMessage(), e);
        }
    }

    // Helper method to detect src/main directories
    private boolean isSrcMainDir(Path dir) {
        Path parent = dir.getParent();
        Path grandparent = (parent != null) ? parent.getParent() : null;

        return "main".equals(dir.getFileName().toString())
                && parent != null
                && "src".equals(parent.getFileName().toString())
                && grandparent != null; // At module level, not root
    }

    private void startServer() {
        try {
            server.start();
            long end = System.nanoTime();
            long elapsedMs = (end - start) / 1_000_000;
            logger.info("Server started on http://localhost:{}/ in {} ms", config.port(), elapsedMs);

        } catch (Exception e) {
            logger.error("Server start failed: {}", e.getMessage());
        }
    }

    public void stop() {
        try {
            server.stop();
        } catch (Exception e) {
            logger.error("Error during stopping server {}", e.getMessage());
        }
    }

    public Config config() {
        return config;
    }

    public HttpServer server() {
        return server;
    }

}

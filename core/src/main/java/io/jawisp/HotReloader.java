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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jawisp.config.Config;
import io.jawisp.http.HttpServer;

public class HotReloader {
    private static final Logger log = LoggerFactory.getLogger(HotReloader.class);
    
    private final Config config;
    private final HttpServer server;
    private final AtomicBoolean shouldRestart = new AtomicBoolean(false);
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final Thread watcherThread;
    private final Thread restartCheckerThread;
    private volatile long lastChangeTime = 0;
    private static final long DEBOUNCE_MS = 1500;
    private WatchService watcher;

    public HotReloader(Config config, HttpServer server) {
        this.config = config;
        this.server = server;
        this.watcherThread = new Thread(this::watchDevFiles, "Jawisp-HotReload");
        this.watcherThread.setDaemon(true);
        this.restartCheckerThread = new Thread(this::restartLoop, "Jawisp-RestartChecker");
        this.restartCheckerThread.setDaemon(true);
    }

    public void start() {
        if (running.compareAndSet(false, true)) {
            watcherThread.start();
            restartCheckerThread.start();
            log.debug("HotReloader fully activated");
        }
    }

    public void stop() {
        running.set(false);
        if (watcherThread.isAlive()) watcherThread.interrupt();
        if (restartCheckerThread.isAlive()) restartCheckerThread.interrupt();
        try {
            if (watcher != null) watcher.close();
        } catch (IOException e) {
            log.warn("Watcher close ignored: {}", e.getMessage());
        }
    }

    private void restartLoop() {
        while (running.get()) {
            if (shouldRestart.getAndSet(false)) {
                log.debug("Restarting JVM due to code changes...");
                try {
                    server.stop();
                } catch (Exception e) {
                    log.warn("Server stop during restart: {}", e.getMessage());
                }
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
            watcher = FileSystems.getDefault().newWatchService();
            Path projectRoot = Paths.get(System.getProperty("user.dir")).toRealPath();
            
            List<Path> srcMainDirs = findAllSrcMainDirs(projectRoot);
            log.debug("Watching {} src/main directories for changes", srcMainDirs.size());

            for (Path srcMainDir : srcMainDirs) {
                registerRecursiveWatcher(srcMainDir, watcher);
            }

            watchEvents();
        } catch (IOException e) {
            log.error("Hot reload setup failed: {}", e.getMessage(), e);
        }
    }

    private List<Path> findAllSrcMainDirs(Path projectRoot) throws IOException {
        List<Path> srcMainDirs = new ArrayList<>();
        Files.walkFileTree(projectRoot, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) 
                    throws IOException {
                if (isSrcMainDir(dir)) {
                    srcMainDirs.add(dir.toRealPath());
                    log.debug("Found src/main: {}", dir);
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return srcMainDirs;
    }

    private void registerRecursiveWatcher(Path srcMainDir, WatchService watcher) throws IOException {
        Files.walkFileTree(srcMainDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) 
                    throws IOException {
                dir.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void watchEvents() {
        while (running.get()) {
            try {
                WatchKey key = watcher.poll(1, TimeUnit.SECONDS);
                if (key != null) {
                    boolean hasChange = processEvents(key);
                    key.reset();
                    
                    if (hasChange && running.get()) {
                        shouldRestart.set(true);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.warn("Watch event error: {}", e.getMessage());
            }
        }
    }

    private boolean processEvents(WatchKey key) {
        boolean hasChange = false;
        for (WatchEvent<?> event : key.pollEvents()) {
            if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                Path changed = (Path) event.context();
                String fileName = changed.toString();
                
                if (fileName.endsWith(".java")) {
                    long now = System.currentTimeMillis();
                    if (now - lastChangeTime > DEBOUNCE_MS) {
                        log.debug("Hot reload detected: {}", fileName);
                        lastChangeTime = now;
                        hasChange = true;
                    }
                }
            }
        }
        return hasChange;
    }

    private boolean isSrcMainDir(Path dir) {
        Path parent = dir.getParent();
        Path grandparent = parent != null ? parent.getParent() : null;
        return "main".equals(dir.getFileName().toString()) 
            && parent != null 
            && "src".equals(parent.getFileName().toString())
            && grandparent != null;
    }
}

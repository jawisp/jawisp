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

/**
 * The HotReloader class provides a mechanism for monitoring changes in Java source files and restarting the server accordingly.
 * This is particularly useful during development to enable hot-reloading of code without manually stopping and starting the server.
 * 
 * @author Taras Chornyi
 * @since 1.0.21
 */
public class HotReloader {
    private static final Logger log = LoggerFactory.getLogger(HotReloader.class);

    /**
     * Configuration settings for the application.
     */
    private final Config config;

    /**
     * The HTTP server instance being managed for restarts.
     */
    private final HttpServer server;

    /**
     * Flag to indicate if a restart is required due to code changes.
     */
    private final AtomicBoolean shouldRestart = new AtomicBoolean(false);

    /**
     * Flag to track whether the HotReloader is currently running.
     */
    private final AtomicBoolean running = new AtomicBoolean(false);

    /**
     * Thread responsible for watching file system changes.
     */
    private final Thread watcherThread;

    /**
     * Thread that checks if a restart is needed and performs it when necessary.
     */
    private final Thread restartCheckerThread;

    /**
     * Timestamp of the last detected change, used for debouncing.
     */
    private volatile long lastChangeTime = 0;

    /**
     * Debounce period in milliseconds to prevent rapid successive restarts from multiple small changes.
     */
    private static final long DEBOUNCE_MS = 1500;

    /**
     * WatchService used to monitor file system events.
     */
    private WatchService watcher;

    /**
     * Constructs a HotReloader with the given configuration and HTTP server.
     *
     * @param config The application configuration settings.
     * @param server The HTTP server instance to manage restarts for.
     */
    public HotReloader(Config config, HttpServer server) {
        this.config = config;
        this.server = server;
        this.watcherThread = new Thread(this::watchDevFiles, "Jawisp-HotReload");
        this.watcherThread.setDaemon(true);
        this.restartCheckerThread = new Thread(this::restartLoop, "Jawisp-RestartChecker");
        this.restartCheckerThread.setDaemon(true);
    }

    /**
     * Starts the HotReloader by beginning the monitoring of source files and setting up threads for change detection and restarts.
     */
    public void start() {
        if (running.compareAndSet(false, true)) {
            watcherThread.start();
            restartCheckerThread.start();
            log.debug("HotReloader fully activated");
        }
    }

    /**
     * Stops the HotReloader gracefully, interrupting the monitoring threads and closing any file system watchers.
     */
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

    /**
     * Continuously checks if a restart is needed and performs the restart by stopping the server and exiting the JVM.
     */
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

    /**
     * Sets up the WatchService to monitor all src/main directories in the project root for changes.
     */
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

    /**
     * Recursively searches the project root for all 'src/main' directories.
     *
     * @param projectRoot The base directory to search from.
     * @return A list of Paths to all found src/main directories.
     * @throws IOException If an I/O error occurs during file traversal.
     */
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

    /**
     * Registers a WatchService watcher for all directories under the specified path, watching for modification events.
     *
     * @param srcMainDir The directory to start recursive registration from.
     * @param watcher The WatchService to register with.
     * @throws IOException If an I/O error occurs during registration.
     */
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

    /**
     * Polls the WatchService for events and processes them to determine if a restart is required.
     */
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

    /**
     * Processes a WatchKey to check if any Java source files have been modified.
     *
     * @param key The WatchKey that generated the events.
     * @return True if a change was detected and debounced, false otherwise.
     */
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

    /**
     * Checks if the given directory is a src/main directory.
     *
     * @param dir The directory to check.
     * @return True if the directory matches the 'src/main' structure, false otherwise.
     */
    private boolean isSrcMainDir(Path dir) {
        Path parent = dir.getParent();
        Path grandparent = parent != null ? parent.getParent() : null;
        return "main".equals(dir.getFileName().toString())
            && parent != null
            && "src".equals(parent.getFileName().toString())
            && grandparent != null;
    }
}
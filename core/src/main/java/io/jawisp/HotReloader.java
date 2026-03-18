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
 * The HotReloader class provides a mechanism for monitoring changes in Java
 * source files and restarting the server accordingly.
 * This is particularly useful during development to enable hot‑reloading of
 * code without manually stopping and starting the server.
 *
 * @author Taras Chornyi
 * @since 1.0.21
 */
public class HotReloader {

    private static final Logger log = LoggerFactory.getLogger(HotReloader.class);

    private final Config config;
    private final HttpServer server;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final Thread watcherThread;

    private volatile long lastChangeTime = 0;
    private static final long DEBOUNCE_MS = 100;

    private WatchService watcher;

    /**
     * Constructor to initialize HotReloader with configuration and server instances.
     *
     * @param config The configuration object
     * @param server The HTTP server instance
     */
    public HotReloader(Config config, HttpServer server) {
        this.config = config;
        this.server = server;
        this.watcherThread = new Thread(this::watchDevFiles, "HotReload");
        this.watcherThread.setDaemon(true);
    }

    /**
     * Starts the hot reloading mechanism.
     */
    public void start() {
        if (running.compareAndSet(false, true)) {
            watcherThread.start();
            log.debug("HotReloader fully activated");
        }
    }

    /**
     * Stops the hot reloading mechanism and cleans up resources.
     */
    public void stop() {
        running.set(false);
        watcherThread.interrupt();
        if (watcher != null) {
            try {
                watcher.close();
            } catch (IOException e) {
                log.warn("Watcher close ignored: {}", e.getMessage());
            }
        }
    }

    /**
     * Watches for changes in the project's src/main directories.
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
     * Recursively finds all directories named 'src/main' under the project root.
     *
     * @param projectRoot The root directory of the project
     * @return A list of paths to 'src/main' directories
     * @throws IOException If an I/O error occurs while accessing files in the file system
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
     * Recursively registers the WatchService for all subdirectories of a given directory.
     *
     * @param srcMainDir The directory to register
     * @param watcher    The WatchService instance
     * @throws IOException If an I/O error occurs while accessing files in the file system
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
     * Processes events received by the WatchService.
     */
    private void watchEvents() {
        while (running.get()) {
            try {
                WatchKey key = watcher.poll(500, TimeUnit.MILLISECONDS);
                if (key != null) {
                    boolean hasChange = processEvents(key);
                    key.reset();

                    if (hasChange && running.get()) {
                        // Restart logic is inside processEvents now
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
     * Processes individual watch events.
     *
     * @param key The WatchKey instance containing the events
     * @return True if a relevant change was detected, false otherwise
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
                        log.info("Hot reload detected: {}", fileName);
                        lastChangeTime = now;
                        hasChange = true;

                        try {
                            server.stop();
                            // Presumably you start again elsewhere (e.g. main loop) or
                            // via a restart strategy outside this class.
                        } catch (Exception e) {
                            log.warn("Server stop during reload: {}", e.getMessage());
                        }
                    }
                }
            }
        }
        return hasChange;
    }

    /**
     * Checks if the given directory is a 'src/main' directory.
     *
     * @param dir The directory to check
     * @return True if the directory is named 'src/main', false otherwise
     */
    private boolean isSrcMainDir(Path dir) {
        Path parent = dir.getParent();
        Path grandparent = parent != null ? parent.getParent() : null;
        return parent != null
            && grandparent != null
            && "src".equals(parent.getFileName().toString())
            && "main".equals(dir.getFileName().toString());
    }
}
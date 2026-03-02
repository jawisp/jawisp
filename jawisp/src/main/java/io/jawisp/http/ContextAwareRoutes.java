package io.jawisp.http;

public class ContextAwareRoutes extends Routes {
    private final String contextPath;

    public ContextAwareRoutes(String contextPath) {
        // FORCE empty to be truly empty
        this.contextPath = (contextPath == null || contextPath.trim().isEmpty()) ? ""
                : contextPath.trim().replaceAll("^/+", "/").replaceAll("/+$", "");
    }

    private String forceCleanPath(String path) {
        if (path == null || path.trim().isEmpty()) {
            return "/";
        }

        // Remove ALL multiple slashes
        String cleanPath = path.replaceAll("/+", "/");

        // If no context path, return clean path directly
        if (this.contextPath.isEmpty()) {
            return cleanPath;
        }
        if (cleanPath.equals("/")) {
            return this.contextPath;
        }

        // Context path exists - strip leading slash from route path
        String route = cleanPath.startsWith("/") ? cleanPath.substring(1) : cleanPath;
        String prefix = this.contextPath;

        return prefix + "/" + route;
    }

    // ALL methods identical
    @Override
    public void get(String path, Handler handler) {
        super.get(forceCleanPath(path), handler);
    }

    @Override
    public void post(String path, Handler handler) {
        super.post(forceCleanPath(path), handler);
    }

    @Override
    public void put(String path, Handler handler) {
        super.put(forceCleanPath(path), handler);
    }

    @Override
    public void patch(String path, Handler handler) {
        super.patch(forceCleanPath(path), handler);
    }

    @Override
    public void delete(String path, Handler handler) {
        super.delete(forceCleanPath(path), handler);
    }

    @Override
    public void head(String path, Handler handler) {
        super.head(forceCleanPath(path), handler);
    }

    @Override
    public void options(String path, Handler handler) {
        super.options(forceCleanPath(path), handler);
    }

    @Override
    public void trace(String path, Handler handler) {
        super.trace(forceCleanPath(path), handler);
    }

    @Override
    public void connect(String path, Handler handler) {
        super.connect(forceCleanPath(path), handler);
    }

    @Override
    public void before(String path, Handler handler) {
        super.before(forceCleanPath(path), handler);
    }

    @Override
    public void after(String path, Handler handler) {
        super.after(forceCleanPath(path), handler);
    }
}

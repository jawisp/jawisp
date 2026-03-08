package io.jawisp.http.netty;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import io.jawisp.http.Context;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;

/**
 * The ResourceHandler class is responsible for handling HTTP requests and
 * responding with the requested resources.
 * It manages the loading, sanitization, and delivery of static resources.
 *
 * @author Taras Chornyi
 * @since 1.0.8
 */

public class ResourceHandler {
    private static volatile ResourceHandler instance;
    private final Map<String, String> contentTypeMap;

    /**
     * Private constructor for the ResourceHandler class.
     * Initializes the content type map by calling the initializeContentTypeMap
     * method.
     */
    private ResourceHandler() {
        this.contentTypeMap = initializeContentTypeMap();
    }

    /**
     * Retrieves the singleton instance of ResourceHandler.
     *
     * @return the singleton ResourceHandler instance
     */
    public static ResourceHandler getInstance() {
        if (instance == null) {
            synchronized (ResourceHandler.class) {
                if (instance == null) {
                    instance = new ResourceHandler();
                }
            }
        }
        return instance;
    }

    /**
     * Handles the incoming HTTP request and sends the corresponding resource
     * response.
     *
     * @param ctx     the ChannelHandlerContext for the current channel
     * @param request the FullHttpRequest received from the client
     * @throws Exception if an error occurs while handling the request
     */
    public void response(ChannelHandlerContext ctx, FullHttpRequest request, Context context) throws Exception {
        String sanitizedPath = sanitizeUri(request.uri());
        if (sanitizedPath == null) {
            context.status(403);
            return;
        }

        URL resource = getClass().getClassLoader().getResource(sanitizedPath);
        if (resource == null) {
            context.status(404);
            return;
        }

        // Read from embedded resource (no filesystem, because it won't work under GRAALVM)
        try (InputStream is = resource.openStream()) {
            byte[] content = is.readAllBytes();

            DefaultHttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            HttpHeaders headers = response.headers();
            headers.set(HttpHeaderNames.CONTENT_TYPE, getMimeType(sanitizedPath));
            headers.set(HttpHeaderNames.CONTENT_LENGTH, String.valueOf(content.length));
            headers.set(HttpHeaderNames.CACHE_CONTROL, "public, max-age=3600");

            final boolean isKeepAlive = HttpUtil.isKeepAlive(request);
            if (!isKeepAlive) {
                response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
            } else if (request.protocolVersion().equals(HttpVersion.HTTP_1_0)) {
                response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            }

            ctx.write(response);
            ctx.write(new DefaultHttpContent(ctx.alloc().buffer().writeBytes(content)));

            ChannelFuture lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
            if (!isKeepAlive) {
                lastContentFuture.addListener(ChannelFutureListener.CLOSE);
            }
        }
    }

    /**
     * Sanitizes the URI to prevent path traversal attacks and normalize the leading
     * slash.
     *
     * @param uri the URI to sanitize
     * @return the sanitized URI, or null if invalid
     */
    private String sanitizeUri(String uri) {
        if (uri == null || uri.isEmpty()) {
            return null;
        }

        // Decode URI (%20 -> space, etc.) - simplified
        uri = uri.replace("%20", " ").replace("%2F", "/");

        // Prevent path traversal
        if (uri.contains("..") || uri.contains("\\") || uri.startsWith("/.")) {
            return null;
        }

        // Normalize leading slash
        return uri.startsWith("/") ? uri.substring(1) : uri;
    }

    /**
     * Determines the MIME type of a file based on its extension.
     *
     * @param filePath the path of the file
     * @return the MIME type of the file, or "application/octet-stream" if unknown
     */
    private String getMimeType(String filePath) {
        if (filePath == null || filePath.lastIndexOf('.') == -1) {
            return "application/octet-stream";
        }

        String extension = filePath.substring(filePath.lastIndexOf('.')).toLowerCase();
        return contentTypeMap.getOrDefault(extension, "application/octet-stream");
    }

    /**
     * Initializes the content type map with common file extensions and their
     * corresponding MIME types.
     *
     * @return a map of file extensions to MIME types
     */
    private static Map<String, String> initializeContentTypeMap() {
        Map<String, String> map = new HashMap<>();
        map.put(".css", "text/css");
        map.put(".js", "application/javascript");
        map.put(".html", "text/html");
        map.put(".htm", "text/html");
        map.put(".png", "image/png");
        map.put(".jpg", "image/jpeg");
        map.put(".jpeg", "image/jpeg");
        map.put(".gif", "image/gif");
        map.put(".svg", "image/svg+xml");
        map.put(".ico", "image/x-icon");
        map.put(".json", "application/json");
        map.put(".xml", "application/xml");
        map.put(".txt", "text/plain");
        map.put(".pdf", "application/pdf");
        map.put(".zip", "application/zip");
        map.put(".mp4", "video/mp4");
        map.put(".webm", "video/webm");
        map.put(".ogg", "video/ogg");
        map.put(".mp3", "audio/mpeg");
        map.put(".wav", "audio/wav");
        map.put(".woff", "font/woff");
        map.put(".woff2", "font/woff2");
        map.put(".ttf", "font/ttf");
        map.put(".eot", "application/vnd.ms-fontobject");
        return map;
    }
}
package io.jawisp.http.netty;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_0;

import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedFile;

public class ResourceHandler {
    private static volatile ResourceHandler instance;

    private final Map<String, String> contentTypeMap = initializeContentTypeMap();

    private ResourceHandler() {
    }

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

    public void response(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        var path = request.uri();
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        var filePath = getClass().getClassLoader().getResource(path).getPath();

        try (RandomAccessFile raf = new RandomAccessFile(filePath, "r")) {
            long length = raf.length();

            DefaultHttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            HttpHeaders headers = response.headers();
            headers.set(HttpHeaderNames.CONTENT_TYPE, getMimeType(filePath));
            headers.set(HttpHeaderNames.CONTENT_LENGTH, length);

            final boolean isKeepAlive = HttpUtil.isKeepAlive(request);
            if (!isKeepAlive) {
                response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
            } else if (request.protocolVersion().equals(HTTP_1_0)) {
                response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            }
            ctx.write(response);

            if (ctx.pipeline().get(SslHandler.class) == null) {
                ctx.write(new DefaultFileRegion(raf.getChannel(), 0, length));
            } else {
                ctx.write(new ChunkedFile(raf));
            }

             ChannelFuture lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
            if (!isKeepAlive) {
                // Close the connection when the whole content is written out.
                lastContentFuture.addListener(ChannelFutureListener.CLOSE);
            }
        } catch (Exception e) {
            // Send error response
            DefaultFullHttpResponse errorResponse = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR,
                    Unpooled.copiedBuffer(("ERR: " + e.getMessage()).getBytes()));
            errorResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
            ctx.writeAndFlush(errorResponse).addListener(ChannelFutureListener.CLOSE);
        }
    }

    private String getMimeType(String filePath) {
        if (filePath == null || filePath.lastIndexOf('.') == -1) {
            return "application/octet-stream";
        }

        String extension = filePath.substring(filePath.lastIndexOf('.')).toLowerCase();
        return contentTypeMap.getOrDefault(extension, "application/octet-stream");
    }

    /**
     * Initializes the content type mapping map with common file extensions.
     * 
     * @return initialized map of file extensions to content types
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

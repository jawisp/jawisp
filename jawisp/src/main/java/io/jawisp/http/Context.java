package io.jawisp.http;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

import io.jawisp.http.json.JsonMapper;
import io.jawisp.http.json.JsonMapperProvider;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpUtil;

public class Context {
    private final String path;
    private final FullHttpRequest request;
    private StringBuilder result = new StringBuilder();
    private int status = 200;
    private String contentType = "text/plain; charset=UTF-8";
    private boolean keepAlive = true;
    private final Route route;
    private JsonMapper jsonMapper;

    public Context(FullHttpRequest request, Route route) {
        this.path = request.uri();
        this.request = request;
        this.route = route;
        this.keepAlive = HttpUtil.isKeepAlive(request);
    }

    public Context result(String result) {
        this.result.append(result);
        return this;
    }

    public Context status(int status) {
        this.status = status;
        return this;
    }

    public Context json(String json) {
        this.contentType = "application/json; charset=UTF-8";
        this.result.append(json);
        return this;
    }

    public String body() {
        ByteBuf content = request.content();
        return content == null ? "" : content.toString(StandardCharsets.UTF_8);
    }

    public byte[] bodyAsBytes() {
        ByteBuf content = request.content();
        if (content == null || content.readableBytes() == 0) {
            return new byte[0]; // Return an empty byte array if the content is null or empty
        }
        byte[] bytes = new byte[content.readableBytes()];
        content.readBytes(bytes);
        return bytes;
    }

    public <T> T bodyAsClass(Type type) {
        if (isJson()) {
            return jsonMapper().fromJsonString(body(), type);
        } else {
            // TODO: throw new BadRequestResponse("Content-Type is not application/json");
            throw new UnsupportedOperationException("Content-Type is not application/json");
        }
    }

    private boolean isJson() {
        HttpHeaders headers = request.headers();
        String contentType = headers.get(HttpHeaderNames.CONTENT_TYPE);
        return contentType != null &&
                (contentType.contains("application/json") ||
                        contentType.contains("application/*") ||
                        contentType.contains("*/*"));
    }

    public String pathParam(String name) {
        String[] parts = path.split("/");
        String[] patternParts = route.getPath().split("/");
        for (int i = 0; i < patternParts.length; i++) {
            if (patternParts[i].startsWith(":") &&
                    patternParts[i].substring(1).equals(name) &&
                    i < parts.length) {
                return parts[i];
            }
        }
        return null;
    }

    public String getPath() {
        return path;
    }

    public FullHttpRequest getRequest() {
        return request;
    }

    public StringBuilder getResult() {
        return result;
    }

    public int getStatus() {
        return status;
    }

    public String getContentType() {
        return contentType;
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }

    // Setter for config - chainable
    // public Context jsonMapper(JsonMapper jsonMapper) {
    //     this.jsonMapper = jsonMapper;
    //     return this;
    // }

    public JsonMapper jsonMapper() {
        if (jsonMapper == null) {
            jsonMapper = JsonMapperProvider.load();  
        }
        return jsonMapper;
    }
}

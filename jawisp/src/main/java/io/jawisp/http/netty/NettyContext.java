package io.jawisp.http.netty;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import io.jawisp.http.Context;
import io.jawisp.http.Route;
import io.jawisp.http.json.JsonMapper;
import io.jawisp.http.json.JsonMapperProvider;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpUtil;

public class NettyContext implements Context {

    private final String path;
    private final FullHttpRequest request;
    private final Route route;
    private String result;
    private int status;
    private String contentType;
    private boolean keepAlive;
    private JsonMapper jsonMapper;

    public NettyContext(FullHttpRequest request, Route route) {
        this.path = request.uri();
        this.request = request;
        this.route = route;
        this.keepAlive = HttpUtil.isKeepAlive(request);
        this.contentType = "text/plain; charset=UTF-8";
        this.result = "";
        this.status = 200;
    }

    @Override
    public Context result(String result) {
        this.result = result;
        return this;
    }
    
    @Override
    public String result() {
        return result;
    }

    @Override
    public Context status(int status) {
        this.status = status;
        return this;
    }

    @Override
    public int status() {
        return status;
    }

    @Override
    public Context json(String json) {
        this.contentType = "application/json; charset=UTF-8";
        this.result = json;
        return this;
    }

    @Override
    public String body() {
        ByteBuf content = request.content();
        return content == null ? "" : content.toString(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] bodyAsBytes() {
        ByteBuf content = request.content();
        if (content == null || content.readableBytes() == 0) {
            return new byte[0]; // Return an empty byte array if the content is null or empty
        }
        byte[] bytes = new byte[content.readableBytes()];
        content.readBytes(bytes);
        return bytes;
    }

    @Override
    public <T> T bodyAsClass(Type type) {
        if (Utils.isJson(request)) {
            return jsonMapper().fromJsonString(body(), type);
        } else {
            throw new UnsupportedOperationException("Content-Type is not application/json");
        }
    }

    @Override
    public boolean isKeepAlive() {
        return keepAlive;
    }

    @Override
    public String contentType() {
        return contentType;
    }

    @Override
    public Context contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    @Override
    public String path() {
        return path;
    }

    @Override
    public JsonMapper jsonMapper() {
        if (jsonMapper == null) {
            jsonMapper = JsonMapperProvider.load();
        }
        return jsonMapper;
    }

    @Override
    public String pathParam(String name) {
        return pathParamMap().get(name);
    }

    @Override
    public Map<String, String> pathParamMap() {
        return Utils.pathParamMap(path, route.getPath());
    }

}
package io.jawisp.http.netty;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import io.jawisp.http.Context;
import io.jawisp.http.Route;
import io.jawisp.http.json.JsonMapper;
import io.jawisp.http.json.JsonMapperProvider;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.util.AttributeKey;

/**
 * The NettyContext class implements the Context interface for use with the Netty framework.
 * It provides methods to handle HTTP requests and responses, manage attributes, and interact with the Netty ChannelHandlerContext.
 *
 * @author reftch
 * @version 1.0.0
 */
public class NettyContext implements Context {

    private final ChannelHandlerContext ctx;
    private final FullHttpRequest request;
    private final String path;
    private final Route route;
    private String result;
    private int status;
    private String contentType;
    private boolean keepAlive;
    private JsonMapper jsonMapper;

    /**
     * Constructs a new NettyContext instance with the given ChannelHandlerContext, FullHttpRequest, and Route.
     *
     * @param ctx the ChannelHandlerContext for the request
     * @param request the FullHttpRequest object representing the HTTP request
     * @param route the Route object representing the matched route
     */
    public NettyContext(ChannelHandlerContext ctx, FullHttpRequest request, Route route) {
        this.ctx = ctx;
        this.request = request;
        this.route = route;
        this.path = request.uri();
        this.keepAlive = HttpUtil.isKeepAlive(request);
        this.contentType = "text/plain; charset=UTF-8";
        this.result = "";
        this.status = 200;
    }

    /**
     * Gets the text of the HTTP response.
     *
     * @return the text of the HTTP response
     */
    @Override
    public String result() {
        return result;
    }

    /**
     * Sets the result of the HTTP response. 
     *
     * @param text the string result to set
     * @return the current NettyContext instance
     */
    @Override
    public Context text(String text) {
        this.result = text;
        return this;
    }    

    /**
     * Sets the status code of the HTTP response.
     *
     * @param status the status code to set
     * @return the current NettyContext instance
     */
    @Override
    public Context status(int status) {
        this.status = status;
        return this;
    }

    /**
     * Gets the status code of the HTTP response.
     *
     * @return the status code of the HTTP response
     */
    @Override
    public int status() {
        return status;
    }

    /**
     * Sets the JSON body of the HTTP response.
     *
     * @param json the JSON string to set
     * @return the current NettyContext instance
     */
    @Override
    public Context json(String json) {
        this.contentType = "application/json; charset=UTF-8";
        this.result = json;
        return this;
    }

    /**
     * Gets the body of the HTTP request as a string.
     *
     * @return the body as a string
     */
    @Override
    public String body() {
        ByteBuf content = request.content();
        return content == null ? "" : content.toString(StandardCharsets.UTF_8);
    }

    /**
     * Gets the body of the HTTP request as bytes.
     *
     * @return the body as bytes
     */
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

    /**
     * Converts the body of the HTTP request to an object of the specified type.
     *
     * @param type the type of the class to convert to
     * @param <T> the type of the class
     * @return the converted object
     */
    @Override
    public <T> T bodyAsClass(Type type) {
        if (Utils.isJson(request)) {
            return jsonMapper().fromJsonString(body(), type);
        } else {
            throw new UnsupportedOperationException("Content-Type is not application/json");
        }
    }

    /**
     * Checks if the connection should be kept alive.
     *
     * @return true if the connection should be kept alive, false otherwise
     */
    @Override
    public boolean isKeepAlive() {
        return keepAlive;
    }

    /**
     * Gets the content type of the HTTP request or response.
     *
     * @return the content type
     */
    @Override
    public String contentType() {
        return contentType;
    }

    /**
     * Sets the content type of the HTTP request or response.
     *
     * @param contentType the content type to set
     * @return the current NettyContext instance
     */
    @Override
    public Context contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * Gets the path of the HTTP request.
     *
     * @return the path of the HTTP request
     */
    @Override
    public String path() {
        return path;
    }

    /**
     * Gets the JsonMapper instance for handling JSON.
     *
     * @return the JsonMapper instance
     */
    @Override
    public JsonMapper jsonMapper() {
        if (jsonMapper == null) {
            jsonMapper = JsonMapperProvider.load();
        }
        return jsonMapper;
    }

    /**
     * Gets a path parameter by name.
     *
     * @param name the name of the path parameter
     * @return the value of the path parameter
     */
    @Override
    public String pathParam(String name) {
        return pathParamMap().get(name);
    }

    /**
     * Gets all path parameters as a map.
     *
     * @return a map of path parameters
     */
    @Override
    public Map<String, String> pathParamMap() {
        return Utils.pathParamMap(path, route.getPath());
    }

    /**
     * Sets an attribute on the request.
     *
     * @param name the name of the attribute
     * @param value the value of the attribute
     * @return the current Context object for method chaining
     */
    @Override
    public Context attribute(String name, Object value) {
        ctx.channel().attr(AttributeKey.valueOf(name)).set(value);
        return this;
    }

    /**
     * Retrieves an attribute from the request.
     *
     * @param name the name of the attribute
     * @param <T> the type of the attribute value
     * @return the attribute value or null if the attribute does not exist
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T attribute(String name) {
        return (T) ctx.channel().attr(AttributeKey.valueOf(name)).get();
    }

}
package io.jawisp.http.netty;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.jawisp.http.Context;
import io.jawisp.http.Route;
import io.jawisp.http.json.JsonMapper;
import io.jawisp.http.json.JsonMapperProvider;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

/**
 * The NettyContext class implements the Context interface for use with the
 * Netty framework.
 * It provides methods to handle HTTP requests and responses, manage attributes,
 * and interact with the Netty ChannelHandlerContext.
 *
 * @author reftch
 * @version 1.0.3
 */
public class NettyContext implements Context {

    private static final AttributeKey<Object> SESSION_ATTR_PREFIX = AttributeKey.valueOf("session.");
    private static final String COOKIE_HEADER = "Cookie";

    private final ChannelHandlerContext ctx;
    private final FullHttpRequest request;
    private final DefaultHttpResponse response;
    private final String path;
    private final Route route;
    private String result;
    private int status;
    private String contentType;
    private boolean keepAlive;
    private JsonMapper jsonMapper;

    /**
     * Constructs a new NettyContext instance with the given ChannelHandlerContext,
     * FullHttpRequest, and Route.
     *
     * @param ctx     the ChannelHandlerContext for the request
     * @param request the FullHttpRequest object representing the HTTP request
     * @param route   the Route object representing the matched route
     */
    public NettyContext(ChannelHandlerContext ctx, FullHttpRequest request, Route route) {
        this.ctx = ctx;
        this.request = request;
        this.route = route;
        this.path = request.uri();
        this.contentType = "text/plain; charset=UTF-8";
        this.result = "";
        this.status = 200;
        this.keepAlive = request != null ? HttpUtil.isKeepAlive(request) : false;
        this.response = new DefaultHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.valueOf(status));
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
     * @param <T>  the type of the class
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
     * @param name  the name of the attribute
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
     * @param <T>  the type of the attribute value
     * @return the attribute value or null if the attribute does not exist
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T attribute(String name) {
        return (T) ctx.channel().attr(AttributeKey.valueOf(name)).get();
    }

    /**
     * Retrieves the value of the header with the specified name.
     *
     * @param name the name of the header to retrieve
     * @return the value of the header, or null if the header is not present
     */
    @Override
    public String header(String name) {
        return headerMap().get(name);
    }

    /**
     * Retrieves a map containing all the headers in this context.
     *
     * @return a map where the keys are header names and the values are header
     *         values
     */
    @Override
    public Map<String, String> headerMap() {
        if (request == null || request.headers() == null) {
            return Collections.emptyMap();
        }
        return request.headers().entries().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue));
    }

    /**
     * Retrieves the value of the cookie with the specified name.
     *
     * @param name the name of the cookie to retrieve
     * @return the value of the cookie, or null if the cookie is not present
     */
    @Override
    public String cookie(String name) {
        return cookieMap().get(name);
    }

    /**
     * Retrieves a map containing all the cookies in this context.
     *
     * @return a map where the keys are cookie names and the values are cookie
     *         values
     */
    @Override
    public Map<String, String> cookieMap() {
        Map<String, String> cookies = new LinkedHashMap<>();
        String cookieHeader = request.headers().get(COOKIE_HEADER);

        if (cookieHeader != null) {
            Set<Cookie> nettyCookies = ServerCookieDecoder.LAX.decode(cookieHeader);
            for (Cookie c : nettyCookies) {
                cookies.put(c.name(), c.value());
            }
        }
        return cookies;
    }

    /**
     * Sets a cookie in the HTTP response.
     *
     * @param name   the name of the cookie
     * @param value  the value of the cookie
     * @param maxAge the maximum age of the cookie in seconds
     */
    @Override
    public void cookie(String name, String value, int maxAge) {
        HttpHeaders headers = response.headers();
        DefaultCookie cookie = new DefaultCookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);
        headers.add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.LAX.encode(cookie));
    }

    /**
     * Removes a cookie from the HTTP response.
     *
     * @param name the name of the cookie to remove
     * @param path the path of the cookie
     */
    @Override
    public void removeCookie(String name, String path) {
        HttpHeaders headers = response.headers();
        DefaultCookie cookie = new DefaultCookie(name, "");
        cookie.setPath(path);
        cookie.setMaxAge(0); // instruct browser to delete
        headers.add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.LAX.encode(cookie));
    }

    /**
     * Gets the original HTTP request object.
     *
     * @return the HTTP request object
     */
    @Override
    public HttpRequest request() {
        return request;
    }

    /**
     * Gets the HTTP response object.
     *
     * @return the HTTP response object
     */
    @Override
    public HttpResponse response() {
        return response;
    }

    /**
     * Sets a session attribute with the specified name and value.
     *
     * @param <T>   the type of the value to be stored
     * @param name  the name of the session attribute
     * @param value the value to be associated with the session attribute
     */
    @Override
    public <T> void sessionAttribute(String name, T value) {
        ctx.channel().attr(getSessionKey(name)).set(value);
    }

    /**
     * Retrieves the value of a session attribute with the specified name.
     *
     * @param <T>  the expected type of the session attribute value
     * @param name the name of the session attribute
     * @return the value of the session attribute, or null if it does not exist
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T sessionAttribute(String name) {
        Attribute<Object> attr = ctx.channel().attr(getSessionKey(name));
        return (T) attr.get();
    }

    /**
     * Helper method to build the full attribute key by appending the specified name
     * to the session attribute prefix.
     *
     * @param name the name of the attribute
     * @return the full attribute key
     */
    private AttributeKey<Object> getSessionKey(String name) {
        return AttributeKey.valueOf(SESSION_ATTR_PREFIX + name);
    }

}
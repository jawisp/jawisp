package io.jawisp.http;

@FunctionalInterface
public interface Handler {
    void handle(Context context);
}

package io.jawisp.core;

@FunctionalInterface
public interface Handler {
    void handle(Context context);
}

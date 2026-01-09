package io.jawisp.http;

public enum MediaType {
    TEXT_PLAIN("text/plain"),
    APPLICATION_JSON("application/json"),
    APPLICATION_XML("application/xml"),
    TEXT_HTML("text/html");

    private final String produces;

    MediaType(String produces) {
        this.produces = produces;
    }

    public String getName() {
        return produces;
    }
}

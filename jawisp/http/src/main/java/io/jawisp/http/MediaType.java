package io.jawisp.http;

public enum MediaType {
    TEXT_PLAIN("text/plain"),
    APPLICATION_JSON("application/json"),
    APPLICATION_XML("application/xml"),
    TEXT_HTML("text/html"),
    
    // Extended types
    APPLICATION_FORM_URLENCODED("application/x-www-form-urlencoded"),
    MULTIPART_FORM_DATA("multipart/form-data"),
    APPLICATION_OCTET_STREAM("application/octet-stream"),
    IMAGE_JPEG("image/jpeg"),
    IMAGE_PNG("image/png"),
    TEXT_CSS("text/css"),
    APPLICATION_JAVASCRIPT("application/javascript");

    private final String mediaType;

    MediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getMediaType() {
        return mediaType;
    }
    
    public static MediaType fromMediaType(String produces) {
        for (MediaType type : values()) {
            if (type.mediaType.equalsIgnoreCase(produces)) {
                return type;
            }
        }
        return TEXT_PLAIN; // default fallback
    }
}

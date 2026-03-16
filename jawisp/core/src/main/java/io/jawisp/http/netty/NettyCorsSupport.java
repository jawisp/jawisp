package io.jawisp.http.netty;

import io.jawisp.config.cors.CorsSettings;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.cors.CorsConfig;
import io.netty.handler.codec.http.cors.CorsConfigBuilder;
import io.netty.handler.codec.http.cors.CorsHandler;

public final class NettyCorsSupport {

    public static CorsHandler from(CorsSettings s) {
        if (s == null || !s.enabled()) {
            return null;
        }

        CorsConfigBuilder builder;

        if (s.allowAnyOrigin() || s.allowedOrigins().isEmpty()) {
            builder = CorsConfigBuilder.forAnyOrigin(); 
        } else {
            builder = CorsConfigBuilder.forOrigins(s.allowedOrigins().toArray(new String[0]));                                         
        }

        if (!s.allowedMethods().isEmpty()) {
            builder.allowedRequestMethods(
                    s.allowedMethods().toArray(new HttpMethod[0])
            );                                          
        }

        if (!s.allowedHeaders().isEmpty()) {
            builder.allowedRequestHeaders(s.allowedHeaders().toArray(new String[0]));                                          
        }

        if (!s.exposedHeaders().isEmpty()) {
            builder.exposeHeaders(s.exposedHeaders().toArray(new String[0]));                                          
        }

        if (s.allowCredentials()) {
            builder.allowCredentials();                 
        }

        if (s.allowNullOrigin()) {
            builder.allowNullOrigin();                  
        }

        if (s.maxAgeSeconds() > 0) {
            builder.maxAge(s.maxAgeSeconds());          
        }

        if (s.shortCircuit()) {
            builder.shortCircuit();                     
        }

        CorsConfig config = builder.build();         
        // CorsConfig config = CorsConfigBuilder.forAnyOrigin().allowNullOrigin().allowCredentials().build();   
        return new CorsHandler(config);                
    }

    private NettyCorsSupport() {
    }
}

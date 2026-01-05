package io.jawisp.example;

import io.jawisp.core.Jawisp;
import io.jawisp.core.annotation.Application;

@Application
public class App {

    public static void main(String[] args) throws Exception {
        //  Write to GraalVM native-image config location
        // Path nativeImageDir = Path.of("src", "main", "resources",
        //     "META-INF", "native-image", "reflect-config.json");
        // // Path nativeImageDir = Path.of("build", "resources", "main",
        // //     "META-INF", "native-image", "reflect-config.json");
        // new ReflectConfigBuilder()
        //     .buildFrom(new App(), nativeImageDir);

        Jawisp.run();
    }
}

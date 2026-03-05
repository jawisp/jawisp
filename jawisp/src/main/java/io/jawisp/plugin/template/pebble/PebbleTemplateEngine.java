package io.jawisp.plugin.template.pebble;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import java.util.Set;

import io.jawisp.plugin.template.TemplateEngine;
import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.loader.ClasspathLoader;

/**
 * The PebbleTemplateEngine class implements the TemplateEngine interface
 * and provides methods to render templates using the Pebble templating engine.
 *
 * @author reftch
 * @version 1.0.4
 */
public class PebbleTemplateEngine implements TemplateEngine {
    private final PebbleEngine engine;

    /**
     * Constructs a new PebbleTemplateEngine instance with a default configuration.
     */
    public PebbleTemplateEngine() {
        PebbleEngine.Builder builder = new PebbleEngine.Builder();

        ClasspathLoader loader = new ClasspathLoader();
        loader.setPrefix("templates");  // void method
        builder.loader(loader);

        this.engine = builder.build();
    }

    /**
     * Renders a template with the given name and model.
     *
     * @param templateName the name of the template to render
     * @param model the model to use for rendering
     * @return the rendered template as a string
     * @throws RuntimeException if an error occurs during rendering
     */
    @Override
    public String render(String templateName, Map<String, Object> model) {
        try (StringWriter writer = new StringWriter()) {
            engine.getTemplate(templateName).evaluate(writer, model);
            return writer.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to render template: " + templateName, e);
        }
    }

    /**
     * Renders a template with the given name and model to the specified writer.
     *
     * @param templateName the name of the template to render
     * @param model the model to use for rendering
     * @param writer the writer to output the rendered template
     * @throws RuntimeException if an error occurs during rendering
     */
    @Override
    public void render(String templateName, Map<String, Object> model, Writer writer) {
        try {
            engine.getTemplate(templateName).evaluate(writer, model);
        } catch (IOException e) {
            throw new RuntimeException("Failed to render template: " + templateName, e);
        }
    }

    /**
     * Returns the supported file extensions for this template engine.
     *
     * @return a set of supported file extensions
     */
    @Override
    public Set<String> getExtensions() {
        return Set.of("html", "peb", "pebble");
    }
}
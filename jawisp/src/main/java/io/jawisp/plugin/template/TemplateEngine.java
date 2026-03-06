package io.jawisp.plugin.template;

import java.io.Writer;
import java.util.Map;

/**
 * The TemplateEngine interface defines methods for rendering templates.
 * Implementing classes should provide the logic to render templates using
 * different templating engines.
 *
 * @author Taras Chornyi
 * @since 1.0.0
 */
public interface TemplateEngine {

    /**
     * Renders a template with the given name and model.
     *
     * @param templateName the name of the template to render
     * @param model the model to use for rendering
     * @return the rendered template as a string
     * @throws RuntimeException if an error occurs during rendering
     */
    String render(String templateName, Map<String, Object> model);

    /**
     * Renders a template with the given name and model to the specified writer.
     *
     * @param templateName the name of the template to render
     * @param model the model to use for rendering
     * @param writer the writer to output the rendered template
     * @throws RuntimeException if an error occurs during rendering
     */
    void render(String templateName, Map<String, Object> model, Writer writer);

}
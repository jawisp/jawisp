package io.jawisp.plugin.template.thymeleaf;

import java.io.Writer;
import java.util.Map;

import io.jawisp.plugin.template.TemplateEngine;

/**
 * The ThymeleafTemplateEngine class implements the TemplateEngine interface
 * and provides methods for rendering templates using Thymeleaf.
 *
 * @author reftch
 * @version 1.0.5
 */
public class ThymeleafTemplateEngine implements TemplateEngine {
    private final org.thymeleaf.TemplateEngine engine;

    /**
     * Constructs a new ThymeleafTemplateEngine instance.
     */
    public ThymeleafTemplateEngine() {
        org.thymeleaf.templateresolver.ClassLoaderTemplateResolver resolver =
            new org.thymeleaf.templateresolver.ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(org.thymeleaf.templatemode.TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false);

        this.engine = new org.thymeleaf.TemplateEngine();
        this.engine.setTemplateResolver(resolver);
    }

    /**
     * Renders a template with the given name and model.
     *
     * @param templateName the name of the template to render
     * @param model        the model to use for rendering
     * @return the rendered template as a String
     */
    @Override
    public String render(String templateName, Map<String, Object> model) {
        org.thymeleaf.context.Context context = new org.thymeleaf.context.Context();
        context.setVariables(model);
        return engine.process(templateName, context);
    }

    /**
     * Renders a template with the given name and model to the specified Writer.
     *
     * @param templateName the name of the template to render
     * @param model        the model to use for rendering
     * @param writer       the Writer to which the rendered template will be written
     */
    @Override
    public void render(String templateName, Map<String, Object> model, Writer writer) {
        org.thymeleaf.context.Context context = new org.thymeleaf.context.Context();
        context.setVariables(model);
        engine.process(templateName, context, writer);
    }
}
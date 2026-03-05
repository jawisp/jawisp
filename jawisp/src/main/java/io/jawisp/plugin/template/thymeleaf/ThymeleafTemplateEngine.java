package io.jawisp.plugin.template.thymeleaf;

import java.io.Writer;
import java.util.Map;
import java.util.Set;

import io.jawisp.plugin.template.TemplateEngine;

public class ThymeleafTemplateEngine implements TemplateEngine {
    private final org.thymeleaf.TemplateEngine engine;

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

    @Override
    public String render(String templateName, Map<String, Object> model) {
        org.thymeleaf.context.Context context = new org.thymeleaf.context.Context();
        context.setVariables(model);
        return engine.process(templateName, context);
    }

    @Override
    public void render(String templateName, Map<String, Object> model, Writer writer) {
        org.thymeleaf.context.Context context = new org.thymeleaf.context.Context();
        context.setVariables(model);
        engine.process(templateName, context, writer);
    }

    @Override
    public Set<String> getExtensions() {
        return Set.of("html");
    }
}

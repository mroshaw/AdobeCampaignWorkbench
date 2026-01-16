package com.campaignworkbench.campaignrenderer;

public final class TemplateGenerationException extends TemplateException {
    public TemplateGenerationException(String msg, String template, int line, Throwable cause) {
        super(msg, template, line, cause);
    }
}

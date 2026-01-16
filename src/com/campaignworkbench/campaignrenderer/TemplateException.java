package com.campaignworkbench.campaignrenderer;

public abstract class TemplateException extends RuntimeException {
    private final String templateName;
    private final int templateLine;

    protected TemplateException(String message, String templateName, int templateLine, Throwable cause) {
        super(message, cause);
        this.templateName = templateName;
        this.templateLine = templateLine;
    }

    public String getTemplateName() { return templateName; }
    public int getTemplateLine() { return templateLine; }
}

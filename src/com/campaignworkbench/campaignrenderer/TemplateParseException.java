package com.campaignworkbench.campaignrenderer;

public final class TemplateParseException extends TemplateException {
    public TemplateParseException(String msg, String template, int line, Throwable cause) {
        super(msg, template, line, cause);
    }
}

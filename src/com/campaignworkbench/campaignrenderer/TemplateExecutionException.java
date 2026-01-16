package com.campaignworkbench.campaignrenderer;

public final class TemplateExecutionException extends TemplateException {
    public TemplateExecutionException(String msg, String template, int line, Throwable cause) {
        super(msg, template, line, cause);
    }
}

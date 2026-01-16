package com.campaignworkbench.campaignrenderer;

public final class TemplateRenderResult {

    private final String generatedJavaScript;
    private final String renderedOutput;

    public TemplateRenderResult(String generatedJavaScript, String renderedOutput) {
        this.generatedJavaScript = generatedJavaScript;
        this.renderedOutput = renderedOutput;
    }

    public String getGeneratedJavaScript() {
        return generatedJavaScript;
    }

    public String getRenderedOutput() {
        return renderedOutput;
    }
}

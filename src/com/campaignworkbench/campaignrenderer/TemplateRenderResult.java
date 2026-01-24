package com.campaignworkbench.campaignrenderer;

/**
 * Wrapper of the results of the rendering process
 */
public class TemplateRenderResult {
    private final String generatedJavaScript;
    private final String renderedOutput;

    public TemplateRenderResult(String generatedJavaScript, String renderedOutput) {
        this.generatedJavaScript = generatedJavaScript;
        this.renderedOutput = renderedOutput;
    }

    public String generatedJavaScript() {
        return generatedJavaScript;
    }

    public String renderedOutput() {
        return renderedOutput;
    }
}

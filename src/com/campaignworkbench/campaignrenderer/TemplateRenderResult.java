package com.campaignworkbench.campaignrenderer;

/**
 * Wrapper of the results of the rendering process
 */
public record TemplateRenderResult(String generatedJavaScript, String renderedOutput) {
}

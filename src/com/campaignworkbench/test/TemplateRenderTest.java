package com.campaignworkbench.test;

import com.campaignworkbench.campaignrenderer.TemplateRenderResult;
import com.campaignworkbench.campaignrenderer.TemplateRenderer;
import com.campaignworkbench.util.FileUtil;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import java.nio.file.Path;

public class TemplateRenderTest {
    public static void main(String[] args) {
        Context cx = Context.enter();
        try {
            cx.setOptimizationLevel(-1);
            cx.setLanguageVersion(Context.VERSION_1_7);

            Scriptable scope = cx.initStandardObjects();

            // Load template
            Path templatePath = Path.of("Workspaces/Test Workspace/Templates/ETMModuleTestTemplate.template");
            String template = FileUtil.read(templatePath);

            // Load XML context
            Path contextPath = Path.of("Workspaces/Test Workspace/XmlContext/SalesReceipt.xml");
            String xml = FileUtil.read(contextPath);
            cx.evaluateString(
                    scope,
                    "var rtEvent = new XML(`" + xml + "`);",
                    contextPath.toString(),
                    1,
                    null
            );

            // Render template
            TemplateRenderResult renderResult = TemplateRenderer.render(template, cx, scope, templatePath.toString());
            System.out.println(renderResult.getRenderedOutput());

        } finally {
            Context.exit();
        }
    }
}

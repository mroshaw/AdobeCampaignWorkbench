package com.campaignworkbench.test;

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

            // Render template
            String output = TemplateRenderer.render(template, cx, scope, templatePath.toString());
            System.out.println(output);

        } finally {
            Context.exit();
        }
    }
}

package com.campaignworkbench.test;

import com.campaignworkbench.util.FileUtil;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import java.nio.file.Path;

/**
 * Quick unit test of the Template Renderer
 */
public class ModuleRenderTest {
    static void main() {
        Context cx = Context.enter();

        try {
            cx.setOptimizationLevel(-1);
            cx.setLanguageVersion(Context.VERSION_1_7);

            Scriptable scope = cx.initStandardObjects();

            // Load module
            Path modulePath = Path.of("Workspaces/Test Workspace/Modules/ETM_M72_MCPaymentScheduleDetails.module");
            String module = FileUtil.read(modulePath);

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

            // Render module
            // String renderResult = ModuleRenderer.renderModule(module, workspaceFile, cx, scope, modulePath.toString());
            // System.out.println(renderResult);

        } finally {
            Context.exit();
        }
    }
}

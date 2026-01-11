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

            // Expose CampaignFunctions using Packages
            cx.evaluateString(scope,
                    "var formatDate = function(dateObj, formatStr) {" +
                            "    return Packages.com.myapp.CampaignFunctions.formatDate(dateObj, formatStr);" +
                            "};",
                    "campaignFunctions.js", 1, null);

            // Load XML context
            Path contextPath = Path.of("XmlContext/OrderShipped-Frames-Partial-Tracking.xml");
            String xml = FileUtil.read(contextPath);
            cx.evaluateString(
                    scope,
                    "var rtEvent = new XML(`" + xml + "`);",
                    contextPath.toString(),
                    1,
                    null
            );

            // Load template
            Path templatePath = Path.of("Templates/TestTemplate.template");
            String template = FileUtil.read(templatePath);

            // Render template
            String output = TemplateRenderer.render(template, cx, scope, templatePath.toString());
            System.out.println(output);

        } finally {
            Context.exit();
        }
    }
}

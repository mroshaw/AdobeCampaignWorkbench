package com.campaignworkbench.test;

import com.campaignworkbench.campaignrenderer.TemplateException;
import com.campaignworkbench.campaignrenderer.TemplateRenderer;
import com.campaignworkbench.ide.Workspace;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import java.nio.file.Paths;

public class SyntaxErrorLineMappingTest {
    public static void main(String[] args) {
        Context cx = Context.enter();
        try {
            cx.setOptimizationLevel(-1);
            Scriptable scope = cx.initStandardObjects();
            Workspace workspace = new Workspace(Paths.get("Workspaces/Test Workspace"));

            // Template from issue description with a syntax error at 'getCurrentDate('
            String templateSource = "<%@ include view='TestFunctions' %>\n" +
                                   "<%@ include view='MoreTestFunctions' %>\n" +
                                   "<html>\n" +
                                   "<body style=\"background-color:MediumSeaGreen;\">\n" +
                                   "Hello <%= rtEvent.ctx.customer.name %> <%= test() %> <BR>\n" +
                                   "<%\n" +
                                   "	var test = test();\n" +
                                   "	\n" +
                                   "%>\n" +
                                   "Test Result: <%= test %>\n" +
                                   "<br>\n" +
                                   "Current Date: <%= getCurrentDate( %>\n" +
                                   "</body>\n" +
                                   "</html>";

            try {
                com.campaignworkbench.campaignrenderer.TemplateRenderResult result = TemplateRenderer.render(workspace, templateSource, cx, scope, "TestTemplate.template");
                System.out.println("Generated JS:\n" + result.generatedJavaScript());
                System.out.println("Error: Render should have failed with a syntax error.");
            } catch (TemplateException ex) {
                System.out.println("Caught Expected Exception: " + ex.getClass().getSimpleName());
                System.out.println("Message: " + ex.getMessage());
                System.out.println("File: " + ex.getTemplateName());
                System.out.println("Line: " + ex.getTemplateLine());
                System.out.println("Root Cause: " + ex.getRootCause());
                if (ex.getCause() instanceof org.mozilla.javascript.RhinoException) {
                    System.out.println("Rhino Line: " + ((org.mozilla.javascript.RhinoException)ex.getCause()).lineNumber());
                }
                
                // Based on the template above:
                // 1: <%@ include view='TestFunctions' %>
                // 2: <%@ include view='MoreTestFunctions' %>
                // 3: <html>
                // 4: <body ...>
                // 5: Hello ...
                // 6: <%
                // 7:    var test = test();
                // 8:    
                // 9: %>
                // 10: Test Result: ...
                // 11: <br>
                // 12: Current Date: <%= getCurrentDate( %>
                
                if ("TestTemplate.template".equals(ex.getTemplateName()) && ex.getTemplateLine() == 12) {
                    System.out.println("SUCCESS: Correct file and line number (12) reported.");
                } else {
                    System.out.println("FAILURE: Incorrect file or line number. Expected 12, got " + ex.getTemplateLine());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Context.exit();
        }
    }
}

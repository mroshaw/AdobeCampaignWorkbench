package com.campaignworkbench.campaignrenderer;

import com.campaignworkbench.ide.IdeException;
import com.campaignworkbench.workspace.EtmModule;
import com.campaignworkbench.workspace.Workspace;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import java.nio.file.Path;

/**
 * Renders a Module into template source.
 * A module is a "meta-template": its JavaScript produces template code.
 */
public final class ModuleRenderer {

    private ModuleRenderer() {}

    public static String renderModule(
            Workspace workspace,
            EtmModule module,
            Context cx,
            Scriptable scope
    ) {

        // Get the module context
        if(!module.isDataContextSet())
        {
            throw new IdeException("Data context is not set on module: " + module.getFileName(), null);
        }

        Path xmlContextFile = module.getAbsoluteFilePath();
        String xmlContextContent = module.getDataContextContent();

        String moduleSource = module.getWorkspaceFileContent();
        String moduleFileName = module.getBaseFileName();
        String js = "";
        try {
            cx.evaluateString(
                    scope,
                    "var ctx = new XML(`" + xmlContextContent + "`);",
                    xmlContextFile.getFileName().toString(),
                    1,
                    null
            );

            js =
                    "var out = new java.lang.StringBuilder();\n" +
                            transformModuleToJavaScript(moduleSource) +
                            "out.toString();";

            Object result = cx.evaluateString(scope, js, moduleFileName, 1, null);
            return Context.toString(result);
        }
        catch (org.mozilla.javascript.RhinoException rhinoException) {
            throw new RendererExecutionException(
                    "Error executing module: " + rhinoException.getMessage(),
                    module,
                    js,
                    rhinoException.lineNumber(),
                    rhinoException.details(),
                    "Check module source code for errors",
                    rhinoException
            );
        }
    }

    /**
     * Converts module syntax into JavaScript that writes template source.
     * This is deliberately simpler than TemplateRenderer.
     */
    private static String transformModuleToJavaScript(String source) {
        StringBuilder js = new StringBuilder();
        int pos = 0;

        while (pos < source.length()) {
            int start = source.indexOf("<%", pos);
            if (start == -1) {
                appendText(js, source.substring(pos));
                break;
            }

            appendText(js, source.substring(pos, start));

            int end = source.indexOf("%>", start);
            if (end == -1) {
                throw new IllegalArgumentException("Unclosed <% in module");
            }

            String code = source.substring(start + 2, end).trim();

            if (code.startsWith("=")) {
                js.append("out.append(")
                        .append(code.substring(1).trim())
                        .append(");\n");
            } else {
                js.append(code).append("\n");
            }

            pos = end + 2;
        }

        return js.toString();
    }

    private static void appendText(StringBuilder js, String text) {
        if (text.isEmpty()) return;
        text = text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .replace("\n", "\\n");
        js.append("out.append(\"").append(text).append("\");\n");
    }
}
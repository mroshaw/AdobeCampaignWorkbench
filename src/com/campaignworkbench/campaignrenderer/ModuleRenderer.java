package com.campaignworkbench.campaignrenderer;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import java.nio.file.Path;

/**
 * Renders a Module into template source.
 * A module is a "meta-template": its JavaScript produces template code.
 */
public final class ModuleRenderer {

    private ModuleRenderer() {}

    /**
     * Processes and generates output for included modules
     * @param cx the Rhino context in which to process the code
     * @param scope the Rhino scope
     * @return the evaluate code as a string
     */
    public static String renderModule(
            WorkspaceContextFile workspaceContextFile,
            Context cx,
            Scriptable scope
    ) {

        // Get the module context
        Path xmlContextFile = workspaceContextFile.getContextFilePath();
        String xmlContextContent = workspaceContextFile.getContextContent();

        String moduleSource = workspaceContextFile.getWorkspaceFileContent();
        String sourceName = workspaceContextFile.getFileName().toString();

        try {

            cx.evaluateString(
                    scope,
                    "var ctx = new XML(`" + xmlContextContent + "`);",
                    xmlContextFile.getFileName().toString(),
                    1,
                    null
            );

            String js = """
                    var out = new java.lang.StringBuilder();
                    %s
                    out.toString();""".formatted(transformModuleToJavaScript(moduleSource));

            Object result = cx.evaluateString(scope, js, sourceName, 1, null);
            return Context.toString(result);
        }
        catch (org.mozilla.javascript.RhinoException e) {
            throw new RendererExecutionException(
                    "Error executing module: " + e.getMessage(),
                    workspaceContextFile,
                    moduleSource,
                    e.lineNumber(),
                    e.details(),
                    "Check the module source for JavaScript errors.",
                    e
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
        int lineCount = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') lineCount++;
        }

        text = text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .replace("\n", "\\n");
        js.append("out.append(\"").append(text).append("\");\n");

        for (int i = 0; i < lineCount; i++) {
            js.append("//\n");
        }
    }
}

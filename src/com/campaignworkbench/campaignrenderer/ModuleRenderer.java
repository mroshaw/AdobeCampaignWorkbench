package com.campaignworkbench.campaignrenderer;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

/**
 * Renders a Module into template source.
 * A module is a "meta-template": its JavaScript produces template code.
 */
public final class ModuleRenderer {

    private ModuleRenderer() {}

    /**
     * Processes and generates output for included modules
     * @param moduleSource source code of the module
     * @param cx the Rhino context in which to process the code
     * @param scope the Rhino scope
     * @param sourceName name of the source being processed
     * @return the evaluate code as a string
     */
    public static String renderModule(
            String moduleSource,
            Context cx,
            Scriptable scope,
            String sourceName
    ) {
        try {
            String js =
                    "var out = new java.lang.StringBuilder();\n" +
                            transformModuleToJavaScript(moduleSource) +
                            "out.toString();";

            Object result = cx.evaluateString(scope, js, sourceName, 1, null);
            return Context.toString(result);
        }
        catch (org.mozilla.javascript.RhinoException e) {
            throw new TemplateExecutionException(
                    "Error executing module: " + e.getMessage(),
                    sourceName,
                    moduleSource,
                    e.lineNumber(),
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
        text = text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .replace("\n", "\\n");
        js.append("out.append(\"").append(text).append("\");\n");
    }
}

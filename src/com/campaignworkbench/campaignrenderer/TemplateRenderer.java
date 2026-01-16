package com.campaignworkbench.campaignrenderer;

import com.campaignworkbench.util.FileUtil;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import java.nio.file.Path;

public final class TemplateRenderer {

    private TemplateRenderer() {}

    /**
     * Renders a template or block with Campaign-style syntax.
     *
     * @param templateSource Raw template content
     * @param cx             Rhino context
     * @param scope          Shared Rhino scope
     * @param sourceName     File name for error reporting
     * @return Rendered HTML/Text
     */
    public static String render(String templateSource, Context cx, Scriptable scope, String sourceName) {

        try {
            // Expose CampaignFunctions using Packages
            cx.evaluateString(scope,
                    "var formatDate = function(dateObj, formatStr) {" +
                            "    return com.campaignworkbench.campaignrenderer.CampaignFunctions.formatDate(dateObj, formatStr);" +
                            "};",
                    "campaignFunctions.js", 1, null);

            cx.evaluateString(scope,
                    "var parseTimeStamp = function(strTime) {" +
                            "    return com.campaignworkbench.campaignrenderer.CampaignFunctions.parseTimeStamp(strTime);" +
                            "};",
                    "campaignFunctions.js", 1, null);

            cx.evaluateString(scope,
                    "var System = Packages.java.lang.System;",
                    "jsImports.js", 1, null
            );

            final String js;
            try {
                js = transformToJavaScript(templateSource, cx, scope);
            }
            catch (Exception e) {
                throw new TemplateGenerationException(
                        "Failed to generate JavaScript from template",
                        sourceName,
                        -1,
                        e
                );
            }

            System.out.println(js);

            try {
                Object result = cx.evaluateString(scope, js, sourceName, 1, null);
                return Context.toString(result);
            }
            catch (org.mozilla.javascript.EvaluatorException e) {
                String message =
                        "JavaScript syntax error" +
                                (e.lineSource() != null ? ":\n" + e.lineSource() : "");

                throw new TemplateParseException(
                        message,
                        sourceName,
                        e.lineNumber(),
                        e
                );
            }
            catch (org.mozilla.javascript.JavaScriptException e) {
                throw new TemplateExecutionException(
                        "JavaScript execution error: " + e.getMessage(),
                        sourceName,
                        e.lineNumber(),
                        e
                );
            }
        }
        catch (org.mozilla.javascript.RhinoException e) {
            throw new TemplateExecutionException(
                    "Rhino error: " + e.getMessage(),
                    sourceName,
                    e.lineNumber(),
                    e
            );
        }
    }

    /**
     * Converts template text into valid JavaScript that appends to a StringBuilder.
     * Includes are resolved recursively at transformation time.
     */
    private static String transformToJavaScript(String templateSource, Context cx, Scriptable scope) {
        StringBuilder js = new StringBuilder();
        js.append("var out = new java.lang.StringBuilder();\n");

        int pos = 0;
        while (pos < templateSource.length()) {
            int start = templateSource.indexOf("<%", pos);
            if (start == -1) {
                appendText(js, templateSource.substring(pos));
                break;
            }

            // Append static text before tag
            appendText(js, templateSource.substring(pos, start));

            int end = templateSource.indexOf("%>", start);
            if (end == -1) {
                throw new IllegalArgumentException("Unclosed <% tag in template");
            }

            String code = templateSource.substring(start + 2, end);
            String trimmed = code.trim();

            // Handle include directive: <%@ include view='...' %>
            if (trimmed.matches("@\\s*include.*")) {
                String includedFile = extractIncludeFile(trimmed);
                if (includedFile != null) {
                    Path path = Path.of("Workspaces/Test Workspace/PersoBlocks", includedFile + ".block");
                    String blockSource = FileUtil.read(path);
                    js.append(transformToJavaScript(blockSource, cx, scope));
                }
            }
            // Expression block: <%= ... %>
            else if (trimmed.startsWith("=")) {
                String expr = trimmed.substring(1).trim();
                js.append("out.append(").append(expr).append(");\n");
            }
            // Statement block: <% ... %>
            else {
                js.append(trimmed).append("\n");
            }

            pos = end + 2;
        }

        js.append("out.toString();\n");
        return js.toString();
    }

    /** Appends static text safely */
    private static void appendText(StringBuilder js, String text) {
        if (text.isEmpty()) return;
        text = text.replace("\r\n", "\n").replace("\r", "\n");
        text = text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
        js.append("out.append(\"").append(text).append("\");\n");
    }

    /** Extracts the file name from <%@ include view='...' %> */
    private static String extractIncludeFile(String code) {
        int viewIndex = code.indexOf("view=");
        if (viewIndex == -1) return null;

        int startQuote = code.indexOf("'", viewIndex);
        int endQuote = code.indexOf("'", startQuote + 1);
        if (startQuote == -1 || endQuote == -1) return null;

        return code.substring(startQuote + 1, endQuote).trim();
    }
}

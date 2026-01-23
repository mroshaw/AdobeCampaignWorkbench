package com.campaignworkbench.campaignrenderer;

import com.campaignworkbench.ide.Workspace;
import com.campaignworkbench.util.FileUtil;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import java.nio.file.Path;

/**
 * Main renderer class, responsible for parsing templates, modules, blocks and XML context
 * to generate an HTML page
 */
public final class TemplateRenderer {

    private TemplateRenderer() {}

    /**
     * @param templateSource source code of the template to render
     * @param cx Rhino context
     * @param scope Rhino scope
     * @param sourceName name of the source being processed
     * @return HTML source of the renderer page
     */
    public static TemplateRenderResult render(
            Workspace workspace,
            String templateSource,
            Context cx,
            Scriptable scope,
            String sourceName
    ) {

        try {
            injectStandardFunctions(cx, scope);

            // 1️⃣ PREPROCESS (no JavaScript execution)
            String expanded = preprocess(workspace, templateSource, cx, scope);

            // 2️⃣ TRANSFORM TO JS
            String js = transformToJavaScript(expanded);

            // 3️⃣ EXECUTE
            Object result = cx.evaluateString(scope, js, sourceName, 1, null);
            return new TemplateRenderResult(js, Context.toString(result));
        }
        catch (org.mozilla.javascript.EvaluatorException e) {
            throw new TemplateParseException(
                    "JavaScript syntax error",
                    sourceName,
                    templateSource,
                    e.lineNumber(),
                    e
            );
        }
        catch (org.mozilla.javascript.JavaScriptException e) {
            throw new TemplateExecutionException(
                    "JavaScript execution error: " + e.getMessage(),
                    sourceName,
                    templateSource,
                    e.lineNumber(),
                    e
            );
        }
        catch (org.mozilla.javascript.RhinoException e) {
            throw new TemplateExecutionException(
                    "Rhino error: " + e.getMessage(),
                    sourceName,
                    templateSource,
                    e.lineNumber(),
                    e
            );
        }
    }

    // ---------------------------------------------------------------------

    private static String preprocess(
            Workspace workspace,
            String source,
            Context cx,
            Scriptable scope
    ) {
        StringBuilder out = new StringBuilder();
        int pos = 0;

        while (pos < source.length()) {
            int start = source.indexOf("<%@", pos);
            if (start == -1) {
                out.append(source.substring(pos));
                break;
            }

            out.append(source, pos, start);

            int end = source.indexOf("%>", start);
            if (end == -1) {
                throw new IllegalArgumentException("Unclosed <%@ directive");
            }

            String directive = source.substring(start + 3, end).trim();

            if (directive.startsWith("include")) {
                if (directive.contains("module=")) {
                    String name = extractQuoted(directive, "module");
                    Path p = workspace.getModulesPath().resolve(name + ".module");
                    String moduleSource = FileUtil.read(p);
                    String moduleOutput =
                            ModuleRenderer.renderModule(moduleSource, cx, scope, p.toString());
                    out.append(preprocess(workspace, moduleOutput, cx, scope));
                }
                else if (directive.contains("view=")) {
                    String name = extractQuoted(directive, "view");
                    Path p = workspace.getBlocksPath().resolve(name + ".block");
                    String blockSource = FileUtil.read(p);
                    out.append(preprocess(workspace, blockSource, cx, scope));
                }
            }

            pos = end + 2;
        }

        return out.toString();
    }

    // ---------------------------------------------------------------------

    private static String transformToJavaScript(String source) {
        StringBuilder js = new StringBuilder();
        js.append("var out = new java.lang.StringBuilder();\n");

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
                throw new IllegalArgumentException("Unclosed <% tag");
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

        js.append("out.toString();");
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

    private static String extractQuoted(String directive, String key) {
        int keyPos = directive.indexOf(key + "=");
        if (keyPos == -1) {
            throw new IllegalArgumentException(
                    "Missing attribute '" + key + "' in directive: " + directive
            );
        }

        int quotePos = keyPos + key.length() + 1;
        if (quotePos >= directive.length()) {
            throw new IllegalArgumentException(
                    "Malformed attribute '" + key + "' in directive: " + directive
            );
        }

        char quote = directive.charAt(quotePos);
        if (quote != '\'' && quote != '"') {
            throw new IllegalArgumentException(
                    "Attribute '" + key + "' must be quoted in directive: " + directive
            );
        }

        int end = directive.indexOf(quote, quotePos + 1);
        if (end == -1) {
            throw new IllegalArgumentException(
                    "Unterminated quoted value for '" + key + "' in directive: " + directive
            );
        }

        return directive.substring(quotePos + 1, end);
    }


    private static void injectStandardFunctions(Context cx, Scriptable scope) {
        cx.evaluateString(scope,
                "var formatDate = function(d,f){" +
                        " return com.campaignworkbench.campaignrenderer.CampaignFunctions.formatDate(d,f);" +
                        "};",
                "campaignFunctions.js", 1, null);

        cx.evaluateString(scope,
                "var parseTimeStamp = function(s){" +
                        " return com.campaignworkbench.campaignrenderer.CampaignFunctions.parseTimeStamp(s);" +
                        "};",
                "campaignFunctions.js", 1, null);

        cx.evaluateString(scope,
                "var System = Packages.java.lang.System;",
                "jsImports.js", 1, null);
    }
}

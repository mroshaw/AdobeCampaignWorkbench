package com.campaignworkbench.campaignrenderer;

import com.campaignworkbench.ide.IDEException;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Scriptable;

import java.nio.file.Path;

/**
 * Main renderer class, responsible for parsing templates, modules, blocks and XML context
 * to generate an HTML page
 */
public final class TemplateRenderer {

    private TemplateRenderer() {
    }

    /**
     * @return HTML source of the renderer page
     */
    public static TemplateRenderResult render(
            Workspace workspace,
            Template template
    ) {
        String js;

        // Get the template context
        Path dataContextFile = template.getDataContextFilePath();
        String dataContextContent = template.getDataContextContent();

        // Add 'rtEvent' wrapper if give <ctx> root
        if (dataContextContent.startsWith("<ctx>")) {
            dataContextContent = "<rtEvent>" + dataContextContent + "</rtEvent>";
        }

        Path messageContextFile = template.getMessageContextFilePath();
        String messageContextContent = template.getMessageContextContent();

        String templateSource = template.getWorkspaceFileContent();
        String sourceName = template.getFileName().toString();

        Context cx;
        Scriptable scope;
        try {

            cx = Context.enter();
            cx.setOptimizationLevel(-1);
            cx.setLanguageVersion(Context.VERSION_1_8);
            scope = cx.initStandardObjects();

            // Add the Data Context
            cx.evaluateString(
                    scope,
                    "var rtEvent = new XML(`" + dataContextContent + "`);",
                    dataContextFile.getFileName().toString(),
                    1,
                    null
            );

            scope.put("xmlContext", scope, dataContextContent);

            // Add the Message Context
            cx.evaluateString(
                    scope,
                    "var message = new XML(`" + messageContextContent + "`);",
                    messageContextFile.getFileName().toString(),
                    1,
                    null
            );

            scope.put("xmlContext", scope, messageContextContent);

            injectStandardFunctions(cx, scope);
        } catch (RhinoException rhinoException) {
            throw new RendererInitException(
                    "An unexpected error occurred while setting XML context",
                    template,
                    template.getWorkspaceFileContent(),
                    rhinoException.lineNumber(),
                    rhinoException.details(),
                    "Please check the context XML",
                    rhinoException
            );
        } catch (Exception exception) {
            throw new RendererInitException(
                    "An unexpected error occurred while setting XML context",
                    template,
                    template.getWorkspaceFileContent(),
                    -1,
                    exception.getMessage(),
                    "Please check the context XML",
                    exception
            );
        }

        js = "";

        try {
            // PREPROCESS (no JavaScript execution)
            String expanded = preprocess(workspace, template, templateSource, cx, scope);

            // TRANSFORM TO JS
            js = transformToJavaScript(expanded);

            // EXECUTE
            Object result = cx.evaluateString(scope, js, sourceName, 1, null);
            return new TemplateRenderResult(js, Context.toString(result));
        } catch (RendererParseException rendererParseException) {
            throw rendererParseException;
        } catch (org.mozilla.javascript.EvaluatorException evaluatorException) {
            throw new RendererParseException(
                    "JavaScript evaluator error: " + evaluatorException.getMessage(),
                    template,
                    js,
                    evaluatorException.lineNumber(),
                    evaluatorException.details(),
                    "Check JavaScript syntax is the template and associated modules and blocks",
                    evaluatorException
            );
        } catch (org.mozilla.javascript.JavaScriptException javaScriptException) {
            throw new RendererExecutionException(
                    "JavaScript execution error: " + javaScriptException.getMessage(),
                    template,
                    js,
                    javaScriptException.lineNumber(),
                    javaScriptException.details(),
                    "Check JavaScript syntax is the template and associated modules and blocks",
                    javaScriptException

            );
        } catch (org.mozilla.javascript.RhinoException rhinoError) {
            throw new RendererExecutionException(
                    "Rhino error: " + rhinoError.getMessage(),
                    template,
                    js,
                    rhinoError.lineNumber(),
                    rhinoError.details(),
                    "Check JavaScript syntax is the template and associated modules and blocks",
                    rhinoError
            );
        }
    }

    // ---------------------------------------------------------------------

    private static String preprocess(
            Workspace workspace,
            WorkspaceFile workspaceFile,
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
                    EtmModule module = workspace.getEtmModuleByName(name).orElseThrow(() ->
                            new RendererParseException("Module not found in workspace: " + name,
                                    null,
                                    source,
                                    -1,
                                    "Module not found in workspace",
                                    "Check module name and check module is added to the workspace",
                                    null)
                    );
                    String moduleOutput =
                            ModuleRenderer.renderModule(module, cx, scope);
                    out.append(preprocess(workspace, workspaceFile, moduleOutput, cx, scope));
                } else if (directive.contains("view=")) {
                    String name = extractQuoted(directive, "view");
                    PersonalisationBlock block = workspace.getBlockByName(name).orElseThrow(() ->
                            new RendererParseException("Block not found in workspace: " + name,
                                    null,
                                    source,
                                    -1,
                                    "Block not found in workspace",
                                    "Check block name and check block is added to the workspace",
                                    null)
                    );
                    out.append(preprocess(workspace, block, block.getWorkspaceFileContent(), cx, scope));
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
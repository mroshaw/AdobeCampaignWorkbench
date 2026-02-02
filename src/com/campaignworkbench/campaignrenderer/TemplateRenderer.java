package com.campaignworkbench.campaignrenderer;

import com.campaignworkbench.ide.IDEException;
import com.campaignworkbench.util.FileUtil;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Main renderer class, responsible for parsing templates, modules, blocks and XML context
 * to generate an HTML page
 */
public final class TemplateRenderer {

    private record SourceMapping(String sourceName, int startLineInExpanded, int endLineInExpanded, int lineOffsetInOriginal) {
    }

    private static class PreprocessedLine {
        final String sourceName;
        final int originalLine;
        
        PreprocessedLine(String sourceName, int originalLine) {
            this.sourceName = sourceName;
            this.originalLine = originalLine;
        }
    }

    /**
     * Thread local storage for source mappings to ensure thread safety during rendering
     */
    private static final ThreadLocal<List<SourceMapping>> sourceMappings = ThreadLocal.withInitial(ArrayList::new);
    private static final ThreadLocal<List<PreprocessedLine>> lineMappings = ThreadLocal.withInitial(ArrayList::new);
    private static final ThreadLocal<List<PreprocessedLine>> jsLineToPreprocessedLine = ThreadLocal.withInitial(ArrayList::new);

    private TemplateRenderer() {}

    /**
     * Renders a template into HTML
     * @param workspace the current workspace
     * @param workspaceContextFile workspace file being processed
     * @return HTML source of the rendered page
     */
    public static TemplateRenderResult render(
            Workspace workspace,
            Template workspaceContextFile
    ) {
        sourceMappings.get().clear();
        lineMappings.get().clear();
        jsLineToPreprocessedLine.get().clear();
        String expanded = "";

        // Get the template context
        Path dataContextFile = workspaceContextFile.getDataContextFilePath();
        String dataContextContent = workspaceContextFile.getDataContextContent();

        // Add 'rtEvent' wrapper if give <ctx> root
        if(dataContextContent.startsWith("<ctx>")) {
            dataContextContent = "<rtEvent>" + dataContextContent + "</rtEvent>";
        }

        Path messageContextFile = workspaceContextFile.getMessageContextFilePath();
        String messageContextContent = workspaceContextFile.getMessageContextContent();

        String templateSource = workspaceContextFile.getWorkspaceFileContent();
        String sourceName = workspaceContextFile.getFileName().toString();

        try {

            Context cx = Context.enter();
            cx.setOptimizationLevel(-1);

            Scriptable scope = cx.initStandardObjects();

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

            // 1. PREPROCESS (no JavaScript execution)
            expanded = preprocess(workspace, workspaceContextFile, templateSource, cx, scope, sourceName);

            // 2. TRANSFORM TO JS
            String js = transformToJavaScript(expanded, workspaceContextFile, sourceName);

            // 3. EXECUTE
            // We use line 2 because transformToJavaScript adds "var out..." at line 1.
            Object result = cx.evaluateString(scope, js, "RenderedTemplate", 1, null);
            String output = Context.toString(result).trim();

            // Strip source markers from final output
            output = output.replaceAll("/\\* SOURCE:.*? \\*/[\r\n]*", "");
            output = output.replaceAll("/\\* END SOURCE:.*? \\*/[\r\n]*", "");

            return new TemplateRenderResult(js.trim(), output);
        }
        catch (org.mozilla.javascript.RhinoException e) {
            SourceMapping mapping = findSourceMapping(e.lineNumber());
            String message = switch (e) {
                case org.mozilla.javascript.EvaluatorException _ -> "JavaScript syntax error";
                case org.mozilla.javascript.JavaScriptException _ -> "JavaScript execution error: " + e.getMessage();
                default -> "Rhino error: " + e.getMessage();
            };
            
            int mappedLine = mapping != null ? (e.lineNumber() - mapping.startLineInExpanded() + 1 + mapping.lineOffsetInOriginal()) : e.lineNumber();
            String mappedSourceName = mapping != null ? mapping.sourceName() : sourceName;

            // Use the new jsLineToPreprocessedLine for precise mapping
            List<PreprocessedLine> jsMappings = jsLineToPreprocessedLine.get();
            if (e.lineNumber() > 0 && e.lineNumber() <= jsMappings.size()) {
                PreprocessedLine preInfo = jsMappings.get(e.lineNumber() - 1);
                if (preInfo != null) {
                    int preLine = preInfo.originalLine;
                    if (preLine > 0 && preLine <= lineMappings.get().size()) {
                        PreprocessedLine originalInfo = lineMappings.get().get(preLine - 1);
                        if (originalInfo != null) {
                            mappedLine = originalInfo.originalLine;
                            mappedSourceName = originalInfo.sourceName;
                        }
                    }
                }
            }

            if (e instanceof org.mozilla.javascript.EvaluatorException evaluatorException) {
                throw new RendererParseException(
                        message,
                        workspaceContextFile,
                        expanded.isEmpty() ? templateSource : expanded,
                        mappedLine,
                        evaluatorException.details(),
                        "Check the template or included files for syntax errors in JavaScript blocks.",
                        evaluatorException
                );
            } else {
                throw new RendererExecutionException(
                        message,
                        workspaceContextFile,
                        expanded,
                        mappedLine,
                        e.details(),
                        e instanceof org.mozilla.javascript.JavaScriptException ? 
                            "Ensure all variables are defined and function calls are valid." :
                            "Check for script errors or invalid Rhino context operations.",
                        e
                );
            }
        }
        catch(RendererException e) {
            throw e;
        }
        catch(java.lang.IllegalArgumentException e) {
            throw new RendererParseException(
                    "Validation Error: " + e.getMessage(),
                    workspaceContextFile,
                    templateSource,
                    -1,
                    e.getMessage(),
                    "Check directive syntax and ensure required attributes are present.",
                    e
            );
        }
        catch(Exception e) {
            throw new RendererExecutionException(
                    "Unexpected error: " + e.getMessage(),
                    workspaceContextFile,
                    templateSource,
                    -1,
                    e.getClass().getSimpleName(),
                    "Check the logs for more details.",
                    e
            );
        } finally {
            Context.exit();
        }

    }

    // ---------------------------------------------------------------------

    /**
     * Preprocesses the template source to handle includes and directives
     * @param workspace the current workspace
     * @param workspaceFile the workspace file being processed
     * @param source the template source code
     * @param cx Rhino context
     * @param scope Rhino scope
     * @param sourceName name of the source being processed
     * @return preprocessed template source
     */
    private static void preprocessInternal(
            Workspace workspace,
            WorkspaceFile workspaceFile,
            String source,
            Context cx,
            Scriptable scope,
            String sourceName,
            StringBuilder out
    ) {
        int pos = 0;
        int currentOriginalLine = 1;

        while (pos < source.length()) {
            int start = source.indexOf("<%@", pos);
            if (start == -1) {
                String tail = source.substring(pos);
                for (char c : tail.toCharArray()) {
                    out.append(c);
                    if (c == '\n') {
                        lineMappings.get().add(new PreprocessedLine(sourceName, currentOriginalLine++));
                    }
                }
                break;
            }

            // Append text before directive
            String textBefore = source.substring(pos, start);
            for (char c : textBefore.toCharArray()) {
                out.append(c);
                if (c == '\n') {
                    lineMappings.get().add(new PreprocessedLine(sourceName, currentOriginalLine++));
                }
            }

            int end = source.indexOf("%>", start);
            if (end == -1) {
                throw new RendererParseException(
                        "Unclosed <%@ directive",
                        workspaceFile,
                        source,
                        currentOriginalLine,
                        "Unclosed <%@ directive",
                        "Ensure all directives are closed with '%>'.",
                        null
                );
            }

            String directive = source.substring(start + 3, end).trim();
            int directiveLines = countLines(source.substring(start, end + 2));

            if (directive.startsWith("include")) {
                if (directive.contains("module=")) {
                    String name = extractQuoted(directive, "module");

                    EtmModule module = workspace.getEtmModuleByName(name).orElseThrow(() ->
                        new IDEException("Module file not found: " + name, null)
                    );

                    try {

                        if(!module.isDataContextSet()) {
                            throw new RendererParseException(
                                    "Context is not set on module: " + name,
                                    workspaceFile,
                                    source,
                                    currentOriginalLine,
                                    null,
                                    "Check if the module file exists and is valid: " + module.getFileName(),
                                    null);
                        }

                        String moduleOutput = ModuleRenderer.renderModule(module, cx, scope);
                        Path trackingPath = module.getFilePath();
                        
                        out.append("/* SOURCE:").append(trackingPath.getFileName()).append(" */\n");
                        lineMappings.get().add(new PreprocessedLine(sourceName, currentOriginalLine));
                        
                        preprocessInternal(workspace, workspaceFile, moduleOutput, cx, scope, trackingPath.toString(), out);
                        
                        if (out.length() > 0 && out.charAt(out.length() - 1) != '\n') {
                            out.append('\n');
                            lineMappings.get().add(new PreprocessedLine(trackingPath.toString(), countLines(moduleOutput) + 1));
                        }
                        out.append("/* END SOURCE:").append(trackingPath.getFileName()).append(" */\n");
                        lineMappings.get().add(new PreprocessedLine(sourceName, currentOriginalLine + directiveLines));
                    } catch (Exception e) {
                        if (e instanceof RendererException te) throw te;
                        throw new RendererParseException(
                                "Failed to include/render module: " + name,
                                workspaceFile,
                                source,
                                currentOriginalLine,
                                e.getMessage(),
                                "Check if the module file exists and is valid: " + module.getFileName(),
                                e
                        );
                    }
                }
                else if (directive.contains("view=")) {
                    String name = extractQuoted(directive, "view");
                    Path p = workspace.getBlocksPath().resolve(name + ".block");
                    try {
                        String blockSource = FileUtil.read(p);
                        
                        out.append("/* SOURCE:").append(p.getFileName()).append(" */\n");
                        lineMappings.get().add(new PreprocessedLine(sourceName, currentOriginalLine));
                        
                        preprocessInternal(workspace, workspaceFile, blockSource, cx, scope, p.toString(), out);
                        
                        // If the block doesn't end with a newline, adding one might shift things.
                        // But preprocessInternal ensures lines are mapped.
                        if (out.length() > 0 && out.charAt(out.length() - 1) != '\n') {
                            out.append('\n');
                            // Map this extra newline to the last line of the block or EOF
                            lineMappings.get().add(new PreprocessedLine(p.toString(), countLines(blockSource) + 1));
                        }

                        out.append("/* END SOURCE:").append(p.getFileName()).append(" */\n");
                        lineMappings.get().add(new PreprocessedLine(sourceName, currentOriginalLine + directiveLines));
                    } catch (Exception e) {
                        if (e instanceof RendererException te) throw te;
                        throw new RendererParseException(
                                "Failed to include block: " + name,
                                workspaceFile,
                                source,
                                currentOriginalLine,
                                e.getMessage(),
                                "Check if the block file exists: " + p,
                                e
                        );
                    }
                }
                currentOriginalLine += directiveLines;
            } else {
                // Other directive - replace with newlines
                for(int i=0; i < directiveLines; i++) {
                    out.append('\n');
                    lineMappings.get().add(new PreprocessedLine(sourceName, currentOriginalLine++));
                }
            }
            
            pos = end + 2;
        }
    }

    private static String preprocess(
            Workspace workspace,
            WorkspaceFile workspaceFile,
            String source,
            Context cx,
            Scriptable scope,
            String sourceName
    ) {
        StringBuilder out = new StringBuilder();
        preprocessInternal(workspace, workspaceFile, source, cx, scope, sourceName, out);
        
        // Add one final newline mapping for EOF if needed
        if (out.length() > 0 && out.charAt(out.length() - 1) != '\n') {
            out.append('\n');
            // Hard to know the exact original line here, but let's guess
        }

        return out.toString();
    }

    /**
     * Counts the number of newlines in a string
     * @param s the string to check
     * @return the number of newlines found
     */
    private static int countLines(String s) {
        int lines = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '\n') lines++;
        }
        return lines;
    }

    // ---------------------------------------------------------------------

    private static String transformToJavaScript(String source, WorkspaceFile workspaceFile, String sourceName) {
        StringBuilder js = new StringBuilder();
        List<PreprocessedLine> jsMappings = jsLineToPreprocessedLine.get();
        jsMappings.clear();
        
        js.append("var out = new java.lang.StringBuilder();\n");
        jsMappings.add(new PreprocessedLine(sourceName, 1)); // Header line 1

        int pos = 0;
        int currentPreprocessedLine = 1;

        while (pos < source.length()) {
            int start = source.indexOf("<%", pos);
            if (start == -1) {
                String text = source.substring(pos);
                appendTextToJS(js, text, currentPreprocessedLine, jsMappings);
                break;
            }

            String textBefore = source.substring(pos, start);
            appendTextToJS(js, textBefore, currentPreprocessedLine, jsMappings);
            
            // Re-calculate line number exactly from source
            currentPreprocessedLine = lineNumberAt(source, start);

            int end = source.indexOf("%>", start);
            if (end == -1) {
                throw new RendererParseException(
                        "Unclosed <% directive",
                        workspaceFile,
                        source,
                        currentPreprocessedLine,
                        "Unclosed <% directive",
                        "Ensure all scriptlet blocks are closed with '%>'.",
                        null
                );
            }

            String code = source.substring(start + 2, end);
            int scriptletStartLine = currentPreprocessedLine;

            if (code.startsWith("=")) {
                js.append("out.append(").append(code.substring(1).trim()).append(");\n");
                jsMappings.add(new PreprocessedLine("RenderedTemplate", scriptletStartLine));
            } else {
                // We must preserve all lines in 'code' exactly
                int lineInCode = 0;
                StringBuilder lineBuf = new StringBuilder();
                for (int i = 0; i < code.length(); i++) {
                    char c = code.charAt(i);
                    if (c == '\n') {
                        js.append(lineBuf.toString()).append("\n");
                        jsMappings.add(new PreprocessedLine("RenderedTemplate", scriptletStartLine + lineInCode));
                        lineBuf.setLength(0);
                        lineInCode++;
                    } else if (c != '\r') {
                        lineBuf.append(c);
                    }
                }
                js.append(lineBuf.toString()).append("\n");
                jsMappings.add(new PreprocessedLine("RenderedTemplate", scriptletStartLine + lineInCode));
            }

            pos = end + 2;
            currentPreprocessedLine = lineNumberAt(source, pos);
        }

        js.append("out.toString();");
        jsMappings.add(new PreprocessedLine(sourceName, currentPreprocessedLine)); // Footer line
        
        return js.toString().trim();
    }

    private static void appendTextToJS(StringBuilder js, String text, int startLine, List<PreprocessedLine> jsMappings) {
        if (text.isEmpty()) return;
        int lineCount = countLines(text);

        String escaped = text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .replace("\n", "\\n");
        
        js.append("out.append(\"").append(escaped).append("\");\n");
        jsMappings.add(new PreprocessedLine("RenderedTemplate", startLine));

        // Keep line numbers in sync by adding commented newlines for each newline in the text
        for (int i = 0; i < lineCount; i++) {
            js.append("//\n");
            jsMappings.add(new PreprocessedLine("RenderedTemplate", startLine + i + 1));
        }
    }

    private static int lineNumberAt(String source, int charIndex) {
        int line = 1;
        for (int i = 0; i < charIndex && i < source.length(); i++) {
            if (source.charAt(i) == '\n') {
                line++;
            }
        }
        return line;
    }

    private static SourceMapping findSourceMapping(int expandedLine) {
        List<SourceMapping> map = sourceMappings.get();
        SourceMapping best = null;
        for (SourceMapping entry : map) {
            if (entry.startLineInExpanded() <= expandedLine) {
                if (best == null || entry.startLineInExpanded() > best.startLineInExpanded()) {
                    best = entry;
                }
            }
        }
        return best;
    }

    /**
     * Extracts a quoted attribute value from a directive
     * @param directive the full directive string
     * @param key the attribute key to look for
     * @return the quoted value (excluding quotes)
     */
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
                """
                var formatDate = function(d,f){
                 return com.campaignworkbench.campaignrenderer.CampaignFunctions.formatDate(d,f);
                };
                """,
                "campaignFunctions.js", 1, null);

        cx.evaluateString(scope,
                """
                var parseTimeStamp = function(s){
                 return com.campaignworkbench.campaignrenderer.CampaignFunctions.parseTimeStamp(s);
                };
                """,
                "campaignFunctions.js", 1, null);

        cx.evaluateString(scope,
                "var System = Packages.java.lang.System;",
                "jsImports.js", 1, null);
    }
}

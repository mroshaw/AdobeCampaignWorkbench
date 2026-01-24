package com.campaignworkbench.campaignrenderer;

import com.campaignworkbench.ide.Workspace;
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

    private static final ThreadLocal<List<SourceMapping>> sourceMappings = ThreadLocal.withInitial(ArrayList::new);

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
        sourceMappings.get().clear();
        String expanded = "";
        try {
            injectStandardFunctions(cx, scope);

            // 1. PREPROCESS (no JavaScript execution)
            expanded = preprocess(workspace, templateSource, cx, scope, sourceName);

            // 2. TRANSFORM TO JS
            String js = transformToJavaScript(expanded, sourceName);

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

            if (e instanceof org.mozilla.javascript.EvaluatorException evaluatorException) {
                throw new TemplateParseException(
                        message,
                        mappedSourceName,
                        expanded.isEmpty() ? templateSource : expanded,
                        mappedLine,
                        evaluatorException.details(),
                        "Check the template or included files for syntax errors in JavaScript blocks.",
                        evaluatorException
                );
            } else {
                throw new TemplateExecutionException(
                        message,
                        mappedSourceName,
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
        catch(TemplateException e) {
            throw e;
        }
        catch(java.lang.IllegalArgumentException e) {
            throw new TemplateParseException(
                    "Validation Error: " + e.getMessage(),
                    sourceName,
                    templateSource,
                    -1,
                    e.getMessage(),
                    "Check directive syntax and ensure required attributes are present.",
                    e
            );
        }
        catch(Exception e) {
            throw new TemplateExecutionException(
                    "Unexpected error: " + e.getMessage(),
                    sourceName,
                    templateSource,
                    -1,
                    e.getClass().getSimpleName(),
                    "Check the logs for more details.",
                    e
            );
        }

    }

    // ---------------------------------------------------------------------

    private static String preprocess(
            Workspace workspace,
            String source,
            Context cx,
            Scriptable scope,
            String sourceName
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
                int line = lineNumberAt(source, start);
                throw new TemplateParseException(
                        "Unclosed <%@ directive",
                        sourceName,
                        source,
                        line,
                        "Unclosed <%@ directive",
                        "Ensure all directives are closed with '%>'.",
                        null
                );
            }

            String directive = source.substring(start + 3, end).trim();

            if (directive.startsWith("include")) {
                if (directive.contains("module=")) {
                    String name = extractQuoted(directive, "module");
                    Path p = workspace.getModulesPath().resolve(name + ".module");
                try {
                    String moduleSource = FileUtil.read(p);
                    String moduleOutput =
                            ModuleRenderer.renderModule(moduleSource, cx, scope, p.toString());
                    out.append("/* SOURCE:").append(p.getFileName()).append(" */\n");
                    out.append(preprocess(workspace, moduleOutput, cx, scope, p.toString()));
                    out.append("\n/* END SOURCE:").append(p.getFileName()).append(" */");
                } catch (Exception e) {
                    if (e instanceof TemplateException te) throw te;
                    throw new TemplateParseException(
                            "Failed to include/render module: " + name,
                            sourceName,
                            source,
                            lineNumberAt(source, start),
                            e.getMessage(),
                            "Check if the module file exists and is valid: " + p,
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
                        out.append(preprocess(workspace, blockSource, cx, scope, p.toString()));
                        out.append("\n/* END SOURCE:").append(p.getFileName()).append(" */");
                    } catch (Exception e) {
                        if (e instanceof TemplateException te) throw te;
                        throw new TemplateParseException(
                                "Failed to include block: " + name,
                                sourceName,
                                source,
                                lineNumberAt(source, start),
                                e.getMessage(),
                                "Check if the block file exists: " + p,
                                e
                        );
                    }
                }
            } else {
                // Replace other directives with exactly the same number of newlines they occupied
                int directiveLines = countLines(source.substring(start, end + 2));
                for(int i=0; i<directiveLines; i++) out.append("\n");
            }
            
            // If the directive was on a line by itself (with a newline following it),
            // we've already accounted for the directive's line with the above appends/newlines.
            // But if we want to BE SURE that the NEXT line of content starts at the NEXT line number,
            // we should replace the directive block exactly.
            // The directive starts at 'start' and ends at 'end+2'.
            // If we replace it with exactly ONE newline, it works if it was on its own line.

            pos = end + 2;
        }

        return out.toString();
    }

    private static int countLines(String s) {
        int lines = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '\n') lines++;
        }
        return lines;
    }

    // ---------------------------------------------------------------------

    private static String transformToJavaScript(String source, String sourceName) {
        StringBuilder js = new StringBuilder();
        js.append("var out = new java.lang.StringBuilder();\n");

        int currentExpandedLine = 2; // "var out..." is line 1.

        int pos = 0;
        while (pos < source.length()) {
            int start = source.indexOf("<%", pos);
            if (start == -1) {
                String text = source.substring(pos);
                appendText(js, text);
                break;
            }

            String text = source.substring(pos, start);
            appendText(js, text);
            currentExpandedLine += 1 + countLines(text);

            int end = source.indexOf("%>", start);
            if (end == -1) {
                int line = lineNumberAt(source, start);
                throw new TemplateParseException(
                        "Unclosed <% directive",
                        sourceName,
                        source,
                        line,
                        "Unclosed <% directive",
                        "Ensure all scriptlet blocks are closed with '%>'.",
                        null
                );
            }

            String code = source.substring(start + 2, end).trim();
            int codeLinesInExpanded = 0;

            if (code.startsWith("=")) {
                js.append("out.append(").append(code.substring(1).trim()).append(");\n");
                codeLinesInExpanded = 1;
            } else {
                js.append(code).append("\n");
                codeLinesInExpanded = 1 + countLines(code);
            }

            // --- SOURCE MAPPING LOGIC ---
            String fileName = sourceName;
            int sourceLineInPreprocessed = lineNumberAt(source, start);
            
            // Check if we are inside an included file by looking for SOURCE markers
            int lastSourceMarker = source.lastIndexOf("/* SOURCE:", start);
            int lastEndMarker = source.lastIndexOf("/* END SOURCE:", start);
            
            if (lastSourceMarker != -1 && (lastEndMarker == -1 || lastEndMarker < lastSourceMarker)) {
                // We ARE inside an include
                int markerEnd = source.indexOf(" */", lastSourceMarker);
                if (markerEnd != -1 && markerEnd < start) {
                    fileName = source.substring(lastSourceMarker + 10, markerEnd);
                    // The line number in the original file is the line number in the preprocessed source
                    // minus the line number where the SOURCE marker was found.
                    int markerLine = lineNumberAt(source, lastSourceMarker);
                    sourceLineInPreprocessed = sourceLineInPreprocessed - markerLine;
                }
            } else {
                // If not in an include, account for markers that came before us in the main file
                int shift = calculateMarkerLineShift(source, start);
                sourceLineInPreprocessed -= shift;
            }
            
            sourceMappings.get().add(new SourceMapping(fileName, currentExpandedLine, currentExpandedLine + codeLinesInExpanded - 1, sourceLineInPreprocessed - 1));
            currentExpandedLine += codeLinesInExpanded;

            pos = end + 2;
        }

        js.append("out.toString();");
        return js.toString().trim();
    }

    private static void updateLastMappingEnd(int endLine) {
        List<SourceMapping> mappings = sourceMappings.get();
        if (!mappings.isEmpty()) {
            SourceMapping last = mappings.get(mappings.size() - 1);
            mappings.set(mappings.size() - 1, new SourceMapping(last.sourceName(), last.startLineInExpanded(), endLine, last.lineOffsetInOriginal()));
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

    private static int calculateMarkerLineShift(String expandedSource, int charIndex) {
        return calculateMarkerLineShiftInRange(expandedSource, 0, charIndex);
    }

    private static int calculateMarkerLineShiftInRange(String expandedSource, int start, int end) {
        int shift = 0;
        int pos = start;
        while (pos < end) {
            int startMarker = expandedSource.indexOf("/* SOURCE:", pos);
            if (startMarker == -1 || startMarker >= end) break;

            int endOfStartMarker = expandedSource.indexOf(" */", startMarker);
            if (endOfStartMarker == -1) break;
            
            int endMarker = expandedSource.indexOf("/* END SOURCE:", endOfStartMarker);
            if (endMarker == -1) break;
            
            int endOfEndMarker = expandedSource.indexOf(" */", endMarker);
            if (endOfEndMarker == -1) break;
            
            // Count lines from the start of the line containing startMarker
            // up to and including the newline following endOfEndMarker.
            int blockStartLine = lineNumberAt(expandedSource, startMarker);
            
            int nextNewline = expandedSource.indexOf('\n', endOfEndMarker);
            int blockEndLine;
            if (nextNewline == -1) {
                blockEndLine = countLines(expandedSource) + 1;
                pos = expandedSource.length();
            } else {
                blockEndLine = lineNumberAt(expandedSource, nextNewline);
                pos = nextNewline + 1;
            }
            
            int blockLines = blockEndLine - blockStartLine + 1;
            // Subtract blockLines - 1 (because the directive occupied 1 line)
            shift += (blockLines - 1);
        }
        return shift;
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
            if (best != null && expandedLine > best.endLineInExpanded()) {
                // It's after the end of this mapping, might be the next one or in "no man's land"
            }
            return best;
    }

    private static void appendText(StringBuilder js, String text) {
        if (text.isEmpty()) return;
        int lineCount = countLines(text);

        text = text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .replace("\n", "\\n");
        js.append("out.append(\"").append(text).append("\");\n");

        // Keep line numbers in sync by adding commented newlines for each newline in the text
        for (int i = 0; i < lineCount; i++) {
            js.append("//\n");
        }
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

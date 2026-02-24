package com.campaignworkbench.ide.editor.richtextfx;

import org.fxmisc.richtext.CodeArea;

/**
 * Interface describing a class that folds and unfolds regions of code
 */
public interface IFoldParser {
    /**
     * Returns foldable regions as (startParagraph, endParagraph) pairs.
     */
    FoldRegions findFoldRegions(CodeArea codeArea);

    boolean isParagraphFolded(int paragraphIndex);
    void foldParagraph(int startParagraphIndex);
    void unfoldParagraph(int startParagraphIndex);

    void foldAll();

    void unfoldAll();
}

package com.campaignworkbench.ide.editor.richtextfx;

import org.fxmisc.richtext.CodeArea;

import java.util.List;
import java.util.Set;

public interface IFoldParser {
    /**
     * Returns foldable regions as (startParagraph, endParagraph) pairs.
     */
    FoldRegions findFoldRegions(CodeArea codeArea);

    boolean isParagraphFolded(int paragraphIndex);
    void foldParagraph(int startParagraphIndex);
    void unfoldParagraph(int startParagraphIndex);

    void addFoldedParagraph(int paragraphIndex);
    void removeFoldedParagraph(int paragraphIndex);

    void foldAll();

    void unfoldAll();
}

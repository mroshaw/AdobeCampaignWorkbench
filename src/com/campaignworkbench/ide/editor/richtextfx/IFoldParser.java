package com.campaignworkbench.ide.editor.richtextfx;

import org.fxmisc.richtext.CodeArea;

import java.util.List;
import java.util.Set;

public interface IFoldParser {
    /**
     * Returns foldable regions as (startParagraph, endParagraph) pairs.
     */
    FoldRegions findFoldRegions(CodeArea codeArea, Set<Integer> foldedParagraphs);
    // Boolean isParagraphFoldStart(int paragraph);
    // Boolean isParagraphFoldEnd(int paragraph);

}

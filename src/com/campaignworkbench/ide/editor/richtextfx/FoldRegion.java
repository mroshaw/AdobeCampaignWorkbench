package com.campaignworkbench.ide.editor.richtextfx;

public class FoldRegion {

    private final int startParagraphIndex;
    private final int endParagraphIndex;
    private boolean isFolded;

    public FoldRegion(int startParagraphIndex, int endParagraphIndex, boolean isFolded) {
        this.startParagraphIndex = startParagraphIndex;
        this.endParagraphIndex = endParagraphIndex;
        this.isFolded = isFolded;
    }

    public void setFoldedState(boolean state) {
        isFolded = state;
    }

    public boolean getFoldedState() {
        return isFolded;
    }

    public int getStart() {
        return startParagraphIndex;
    }

    public int getEnd() {
        return endParagraphIndex;
    }

    public Boolean isParagraphWithin(int paragraphIndex) {
        return paragraphIndex > startParagraphIndex && paragraphIndex <= endParagraphIndex;
    }
}

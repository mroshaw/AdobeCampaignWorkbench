package com.campaignworkbench.ide.editor;

public class SourcePanel {

    private RSyntaxEditor sourcePreview;

    public SourcePanel() {
        sourcePreview = new RSyntaxEditor();
        sourcePreview.setEditable(true);
        sourcePreview.setSyntax(SyntaxType.SOURCE_PREVIEW);

    }
}

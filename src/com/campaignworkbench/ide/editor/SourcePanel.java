package com.campaignworkbench.ide.editor;

import com.campaignworkbench.ide.IThemeable;
import com.campaignworkbench.ide.IDETheme;
import com.campaignworkbench.ide.ThemeManager;

public class SourcePanel implements IThemeable {

    private RSyntaxEditor sourcePreview;

    public SourcePanel() {
        ThemeManager.register(this);
        sourcePreview = new RSyntaxEditor();
        sourcePreview.setEditable(true);
        sourcePreview.setSyntax(SyntaxType.SOURCE_PREVIEW);

    }

    @Override
    public void applyTheme(IDETheme theme) {

    }
}

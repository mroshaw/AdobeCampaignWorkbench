package com.campaignworkbench.ide;

import com.campaignworkbench.ide.editor.RSyntaxEditor;
import javafx.scene.Scene;

import java.util.ArrayList;
import java.util.List;

public final class ThemeManager {

    private static IDETheme currentTheme = IDETheme.DARK;
    private static final List<RSyntaxEditor> editors = new ArrayList<>();

    private ThemeManager() {}

    public static void register(RSyntaxEditor editor) {
        editors.add(editor);
        editor.applyTheme(currentTheme);
    }

    public static void applyCurrentTheme() {
        setTheme(currentTheme);
    }

    public static void setTheme(IDETheme theme) {
        currentTheme = theme;

        for (RSyntaxEditor editor : editors) {
            editor.applyTheme(theme);
        }
        CampaignWorkbenchIDE.setTheme(theme);
    }
}

package com.campaignworkbench.ide;

import com.campaignworkbench.ide.editor.RSyntaxEditor;
import javafx.scene.Scene;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the application-wide theme and handles registration of editors for theme updates
 */
public final class ThemeManager {

    private static IDETheme currentTheme = IDETheme.DARK;
    private static final List<RSyntaxEditor> editors = new ArrayList<>();

    private ThemeManager() {}

    /**
     * Registers an editor to receive theme updates
     * @param editor the editor to register
     */
    public static void register(RSyntaxEditor editor) {
        editors.add(editor);
        editor.applyTheme(currentTheme);
    }

    /**
     * Re-applies the current theme to all registered components
     */
    public static void applyCurrentTheme() {
        setTheme(currentTheme);
    }

    /**
     * Sets a new application-wide theme
     * @param theme the theme to apply
     */
    public static void setTheme(IDETheme theme) {
        currentTheme = theme;

        for (RSyntaxEditor editor : editors) {
            editor.applyTheme(theme);
        }
        CampaignWorkbenchIDE.setTheme(theme);
    }
}

package com.campaignworkbench.ide.editor;

import com.campaignworkbench.ide.EditorTab;
import com.campaignworkbench.ide.IThemeable;
import com.campaignworkbench.ide.IDETheme;
import com.campaignworkbench.ide.ThemeManager;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Window;

import java.nio.file.Path;

public class EditorTabPanel implements IThemeable, IJavaFxNode {

    private TabPane tabPane;

    public EditorTabPanel(ChangeListener<Tab> tabChangedHListener) {
        tabPane = new TabPane();

        tabPane.getSelectionModel().selectedItemProperty().addListener(tabChangedHListener);
        ThemeManager.register(this);
    }

    public Window getWindow() {
        return tabPane.getScene().getWindow();
    }

    public void addEditorTab(Path path, String content)
    {
        EditorTab tab = new EditorTab(path, content);
        tab.setClosable(true);
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
    }

    public Path getSelectedFile() {
        return getSelected().getFile();
    }

    public String getSelectedText() {
        return getSelected().getEditorText();
    }

    public String getSelectedFileName() {
        return getSelectedFile().getFileName().toString();
    }

    private EditorTab getSelected() {
        return (EditorTab)tabPane.getSelectionModel().getSelectedItem();
    }

    public boolean isSelected() {
        return getSelected() != null;
    }

    @Override
    public void applyTheme(IDETheme theme) {
        switch (theme) {
            case DARK:
                tabPane.setStyle(
                        "-fx-background-color: #2b2b2b;" +          // TabPane background
                                "-fx-tab-min-height: 30;" +                 // optional, makes tabs slightly taller
                                "-fx-tab-text-color: white;"                // Tab text color
                );
                // Each tab content (RSyntaxEditor) should handle its own theme
                break;

            case LIGHT:
            default:
                tabPane.setStyle(
                        "-fx-background-color: #ececec;" +
                                "-fx-tab-text-color: black;"
                );
                break;
        }
    }

    @Override
    public Node getNode() {
        return tabPane;
    }
}

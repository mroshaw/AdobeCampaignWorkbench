package com.campaignworkbench.ide;

import com.campaignworkbench.campaignrenderer.WorkspaceFile;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Window;

import java.nio.file.Path;

/**
 * Implements a tabbed panel of Editor tabs
 */
public class EditorTabPanel implements IJavaFxNode {

    private final TabPane tabPane;

    /**
     * Constructor
     *
     * @param tabChangedListener action to call when the tab is changed
     */
    public EditorTabPanel(ChangeListener<Tab> tabChangedListener) {
        tabPane = new TabPane();
        tabPane.setMinHeight(0);

        tabPane.getSelectionModel().selectedItemProperty().addListener(tabChangedListener);
        tabPane.getSelectionModel().selectedItemProperty().addListener((_, _, newTab) -> refreshTabEditor(newTab));

        // Set style class
        tabPane.getStyleClass().add("editor-tab-panel");
    }

    private void refreshTabEditor(Tab tab) {
        if (tab instanceof EditorTab editorTab) {
            editorTab.refreshEditor();
        }
    }

    /**
     * Finds and selects a tab by its file path, or opens it if not found.
     * Then jumps to the specified line.
     *
     * @param path The path to the file
     * @param line The line number to jump to (1-indexed)
     */
    public void openFileAndGoToLine(Path path, int line) {
        EditorTab targetTab = null;
        for (Tab tab : tabPane.getTabs()) {
            if (tab instanceof EditorTab editorTab) {
                if (editorTab.getFile().equals(path)) {
                    targetTab = editorTab;
                    break;
                }
            }
        }

        if (targetTab == null) {
            return;
        }

        tabPane.getSelectionModel().select(targetTab);
        targetTab.getEditor().gotoLine(line);
    }

    /**
     * @param path The path to the file
     * @return true if the file is already open in a tab
     */
    public boolean isOpened(Path path) {
        for (Tab tab : tabPane.getTabs()) {
            if (tab instanceof EditorTab editorTab) {
                if (editorTab.getFile().equals(path)) {
                    return true;
                }
            }
        }
        return false;
    }

    private EditorTab getExistingTab(Path path) {
        for (Tab tab : tabPane.getTabs()) {
            if (tab instanceof EditorTab editorTab) {
                if (editorTab.getFile().equals(path)) {
                    return editorTab;
                }
            }
        }
        return null;
    }

    /**
     * Gets the window underlying the tab panel
     *
     * @return the underlying tab window
     */
    public Window getWindow() {
        return tabPane.getScene().getWindow();
    }

    /**
     * Adds a new Editor Tab to the Tab Panel
     *
     * @param workspaceFile workspace file to be edited in the tab
     */
    public void addEditorTab(WorkspaceFile workspaceFile) {
        EditorTab existingTab = getExistingTab(workspaceFile.getFilePath());
        if(existingTab != null) {
            // Set focus on the tab
            tabPane.getSelectionModel().select(existingTab);
            return;
        }

        EditorTab tab = new EditorTab(workspaceFile);
        tab.setClosable(true);
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
    }

    public WorkspaceFile getSelectedWorkspaceFile() {
        return getSelected().getWorkspaceFile();
    }

    /**
     * @return the file from the currently selected tab
     */
    public Path getSelectedFile() {
        return getSelected().getFile();
    }

    /**
     * @return the code as text from the currently selected tab
     */
    public String getSelectedText() {
        return getSelected().getEditorText();
    }

    /**
     * @return the code in the currently selected tab
     */
    public String getSelectedFileName() {
        return getSelectedFile().getFileName().toString();
    }

    public boolean isSelectedTemplateAndReady() {
        EditorTab selectedTab = getSelected();
        return selectedTab.isTemplateTab() && selectedTab.isContextSet();
    }

    private EditorTab getSelected() {
        return (EditorTab) tabPane.getSelectionModel().getSelectedItem();
    }

    /**
     * @return true if this tab is the currently selected tab
     */
    public boolean isSelected() {
        return getSelected() != null;
    }

    public void saveSelectedTab() {
        getSelected().saveFile();
    }

    public void saveAllTabs() {
        for (Tab tab : tabPane.getTabs()) {
            if (tab instanceof EditorTab editorTab) {
                editorTab.saveFile();
            }
        }
    }

    public void closeAllTabs() {
        saveAllTabs();
        tabPane.getTabs().clear();
    }

    @Override
    public Node getNode() {
        return tabPane;
    }
}

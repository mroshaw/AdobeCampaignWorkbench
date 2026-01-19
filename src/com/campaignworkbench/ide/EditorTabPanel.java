package com.campaignworkbench.ide;

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
     * @param tabChangedListener action to call when the tab is changed
     */
    public EditorTabPanel(ChangeListener<Tab> tabChangedListener) {
        tabPane = new TabPane();

        tabPane.getSelectionModel().selectedItemProperty().addListener(tabChangedListener);
    }

    /**
     * Gets the window underlying the tab panel
     * @return the underlying tab window
     */
    public Window getWindow() {
        return tabPane.getScene().getWindow();
    }

    /**
     * Adds a new Editor Tab to the Tab Panel
     * @param path full path of the file to open in the new editor tab
     * @param content any default code to populate in the new editor tab
     */
    public void addEditorTab(Path path, String content)
    {
        EditorTab tab = new EditorTab(path, content);
        tab.setClosable(true);
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
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

    private EditorTab getSelected() {
        return (EditorTab)tabPane.getSelectionModel().getSelectedItem();
    }

    /**
     * @return true if this tab is the currently selected tab
     */
    public boolean isSelected() {
        return getSelected() != null;
    }

    @Override
    public Node getNode() {
        return tabPane;
    }
}

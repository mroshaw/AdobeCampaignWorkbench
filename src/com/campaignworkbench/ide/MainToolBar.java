package com.campaignworkbench.ide;

import com.campaignworkbench.util.UiUtil;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import org.controlsfx.glyphfont.FontAwesome;

/**
 * Implements a button toolbar for use within the IDE User Interface
 */
public class MainToolBar implements IJavaFxNode {

    private final ToolBar toolBar;
    private final Button runButton;

    /**
     * Constructor
     * @param openWorkspaceHandler - action to take when the open workspace button is clicked
     * @param runHandler - action to take when the run button is clicked
     */
    public MainToolBar(
            EventHandler<ActionEvent> openWorkspaceHandler,
            EventHandler<ActionEvent> newWorkspaceHandler,
            EventHandler<ActionEvent> closeWorkspaceHandler,
            EventHandler<ActionEvent> closeEditorTabsHandler,
            EventHandler<ActionEvent> runHandler
    ) {
        Button openWorkspaceButton = UiUtil.createButton("", "Open Workspace", FontAwesome.Glyph.FOLDER_OPEN,  "workspace-icon",2, true, openWorkspaceHandler);
        Button newWorkspaceButton = UiUtil.createButton("", "New Workspace", FontAwesome.Glyph.FOLDER,  "positive-icon",2, true, newWorkspaceHandler);
        Button closeWorkspaceButton = UiUtil.createButton("", "Close Workspace", FontAwesome.Glyph.CLOSE,  "negative-icon",2, true, closeWorkspaceHandler);
        Button closeEditorTabsButton = UiUtil.createButton("", "Close all editor tabs", FontAwesome.Glyph.FILES_ALT,  "negative-icon",2, true, closeEditorTabsHandler);
        runButton = UiUtil.createButton("", "Run template", FontAwesome.Glyph.PLAY,  "positive-icon",2, false, runHandler);

        toolBar = new ToolBar(
                openWorkspaceButton,
                newWorkspaceButton,
                closeWorkspaceButton,
                new Separator(Orientation.VERTICAL),
                closeEditorTabsButton,
                new Separator(Orientation.VERTICAL),
                runButton
        );

        toolBar.getStyleClass().add(".large-toolbar");
    }

    /**
     * @param state true or false state of the run button
     */
    public void setRunButtonState(boolean state) {
        runButton.setDisable(!state);
    }

    @Override
    public Node getNode() {
        return toolBar;
    }
}

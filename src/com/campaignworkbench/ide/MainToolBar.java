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
     * @param openWorkspaceHandler - action to take when open workspace button is clicked
     * @param runHandler - action to take when run button is clicked
     */
    public MainToolBar(
            EventHandler<ActionEvent> openWorkspaceHandler,
            EventHandler<ActionEvent> runHandler
    ) {
        // Open Template button
        Button openWorkspaceButton = UiUtil.createButton("", "Open Workspace", FontAwesome.Glyph.FOLDER_OPEN,  "workspace-icon",2, true, openWorkspaceHandler); // FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.FOLDER_OPEN, "24px");
        runButton = UiUtil.createButton("", "Run template", FontAwesome.Glyph.PLAY,  "positive-icon",2, false, runHandler); // FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.FOLDER_OPEN, "24px");

        toolBar = new ToolBar(
                openWorkspaceButton,
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

package com.campaignworkbench.ide;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.utils.FontAwesomeIconFactory;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/**
 * Implements a button toolbar for use within the IDE User Interface
 */
public class MainToolBar implements IJavaFxNode {

    private final HBox toolBar;
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
        Text openWorkspaceIcon = FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.FOLDER_OPEN, "24px");
        openWorkspaceIcon.setFill(Color.YELLOW);
        Button openWorkspaceButton = new Button(); //("Open Workspace");
        openWorkspaceButton.setTooltip(new Tooltip("Open Workspace"));
        openWorkspaceButton.setGraphic(openWorkspaceIcon);
        openWorkspaceButton.setOnAction(openWorkspaceHandler);

        // Run Template button
        Text runTemplateIcon = FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.PLAY, "24px");
        runTemplateIcon.setFill(Color.GREEN);
        runButton = new Button(); // ("Run Template");
        runButton.setTooltip(new Tooltip("Run Template"));
        runButton.setGraphic(runTemplateIcon);
        runButton.setDisable(true);
        runButton.setOnAction(runHandler);

        toolBar = new HBox(
                10,
                openWorkspaceButton,
                runButton
        );
        toolBar.setAlignment(Pos.CENTER_LEFT);
        toolBar.getStyleClass().add("tool-bar");
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

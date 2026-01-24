package com.campaignworkbench.ide;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

/**
 * User interface control to provided a logging console
 */
public class LogPanel implements IJavaFxNode {

    private final TextArea logArea;
    VBox logPanel;

    /**
     * Constructor
     */
    public LogPanel(String label) {
        Label logLabel = new Label(label);
        logLabel.setPadding(new Insets(0,0, 0,5));
        logLabel.setStyle("-fx-font-weight: bold;");
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setCursor(Cursor.TEXT); // full-width log pane
        logArea.setFont(Font.font("Source Code Pro", 14));

        logPanel = new VBox(5, logLabel, logArea);
        logPanel.setPadding(new Insets(0,0, 0,5));
        VBox.setVgrow(logArea, Priority.ALWAYS);
    }

    public Node getNode() {
        return logPanel;
    }

    /**
     * Adds a line of content to the log
     * @param msg text containing content of the log line to add
     */
    public void appendLog(String msg) {
        Platform.runLater(() -> logArea.appendText(msg + "\n"));
    }

}

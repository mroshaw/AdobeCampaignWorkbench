package com.campaignworkbench.ide.editor;

import com.campaignworkbench.ide.IDETheme;
import com.campaignworkbench.ide.ThemeManager;
import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class LogPanel implements IJavaFxNode  {

    private TextArea logArea;
    private Label logLabel;
    VBox logBox;

    public LogPanel() {
        // --- Full-width log pane ---
        logLabel = new Label("Logs");
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setCursor(Cursor.TEXT); // full-width log pane
        logArea.setFont(Font.font("Source Code Pro", 14));

        logBox = new VBox(5, logLabel, logArea);
        VBox.setVgrow(logArea, Priority.ALWAYS);
    }

    public Node getNode() {
        return logBox;
    }

    public void appendLog(String msg) {
        Platform.runLater(() -> logArea.appendText(msg + "\n"));
    }

}

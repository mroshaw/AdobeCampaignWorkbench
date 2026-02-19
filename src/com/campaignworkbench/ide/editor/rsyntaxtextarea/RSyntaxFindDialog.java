package com.campaignworkbench.ide.editor.rsyntaxtextarea;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;
import org.fife.ui.rtextarea.SearchResult;

import javax.swing.*;

public class RSyntaxFindDialog {

    private final RSyntaxTextArea textArea;
    private Stage dialog;
    private Stage owner;

    public RSyntaxFindDialog(RSyntaxTextArea textArea, Stage owner) {
        this.textArea = textArea;
    }

    public void show(String fileName) {
        if (dialog != null && dialog.isShowing()) {
            dialog.toFront();
            return;
        }

        dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.NONE);
        dialog.setTitle("Find - " + fileName);
        dialog.setWidth(400);

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        TextField searchField = new TextField();
        searchField.setPromptText("Search text...");

        CheckBox caseCheck = new CheckBox("Case sensitive");
        CheckBox wholeWordCheck = new CheckBox("Whole word");

        HBox buttons = new HBox(10);
        Button findNext = new Button("Find Next");
        Button findPrev = new Button("Find Previous");
        buttons.getChildren().addAll(findNext, findPrev);

        root.getChildren().addAll(searchField, caseCheck, wholeWordCheck, buttons);

        Scene scene = new Scene(root);
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setScene(scene);
        dialog.setResizable(false);
        dialog.show();

        // Handlers
        findNext.setOnAction(e -> doSearch(searchField.getText(), caseCheck.isSelected(), wholeWordCheck.isSelected(), false));
        findPrev.setOnAction(e -> doSearch(searchField.getText(), caseCheck.isSelected(), wholeWordCheck.isSelected(), true));
    }

    private void doSearch(String text, boolean matchCase, boolean wholeWord, boolean backward) {
        if (text == null || text.isEmpty()) return;

        SearchContext context = new SearchContext();
        context.setSearchFor(text);
        context.setSearchWrap(true);
        context.setMatchCase(matchCase);
        context.setWholeWord(wholeWord);
        context.setRegularExpression(false);
        context.setSearchForward(!backward);
// Ensure Swing updates happen on the EDT
        SwingUtilities.invokeLater(() -> {
            SearchResult result = SearchEngine.find(textArea, context);
            textArea.repaint(); // force repaint to keep tokens consistent

            if (!result.wasFound()) {
                // JavaFX alert must stay on FX thread
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Find");
                    alert.setHeaderText(null);
                    alert.setContentText("Text not found!");
                    alert.showAndWait();
                });
            }
        });
    }
}

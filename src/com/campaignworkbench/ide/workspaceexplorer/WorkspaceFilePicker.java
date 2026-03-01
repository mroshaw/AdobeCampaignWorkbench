package com.campaignworkbench.ide.workspaceexplorer;

import com.campaignworkbench.workspace.WorkspaceFile;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Window;

import java.util.Optional;

public class WorkspaceFilePicker {

    public static <T extends WorkspaceFile> Optional<T> show(
            Window owner,
            String title,
            String headerText,
            String contentText,
            ObservableList<T> workspaceFiles
    ) {
        Dialog<T> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.setTitle(title);
        dialog.setHeaderText(headerText);

        ButtonType pickButtonType =
                new ButtonType("Pick", ButtonBar.ButtonData.OK_DONE);

        dialog.getDialogPane().getButtonTypes()
                .addAll(pickButtonType, ButtonType.CANCEL);

        ListView<T> listView = new ListView<>(workspaceFiles);

        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null
                        ? null
                        : item.getFileName());
            }
        });

        if (!workspaceFiles.isEmpty()) {
            listView.getSelectionModel().selectFirst();
        }

        Node pickButton = dialog.getDialogPane().lookupButton(pickButtonType);
        pickButton.setDisable(
                listView.getSelectionModel().getSelectedItem() == null
        );

        listView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) ->
                        pickButton.setDisable(newVal == null)
        );

        // Configure double click to select
        listView.setOnMouseClicked(evt -> {
            if (evt.getClickCount() == 2) { // double click
                T selected = listView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    // Set the dialog result to the selected item and close
                    dialog.setResult(selected);
                    dialog.close();
                }
            }
        });

        dialog.getDialogPane().setContent(new BorderPane(listView));

        dialog.setResultConverter(button -> {
            if (button == pickButtonType) {
                return listView.getSelectionModel().getSelectedItem();
            }
            return null;
        });

        return dialog.showAndWait();
    }
}
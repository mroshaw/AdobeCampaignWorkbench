package com.campaignworkbench.ide;

import javafx.scene.Node;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseButton;

import java.io.File;
import java.util.function.Consumer;

public class WorkspaceExplorer implements IJavaFxNode {

    private TreeView<File> treeView;
    private Consumer<File> fileOpenHandler;

    /**
     * @param fileOpenHandler that handles double clicks of files in the Explorer
     */
    public WorkspaceExplorer(Consumer<File> fileOpenHandler) {
        treeView = new TreeView<>();
        setupDoubleClick();

        treeView.setCellFactory(tv -> new TreeCell<>() {
            @Override
            protected void updateItem(File item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });
        this.fileOpenHandler = fileOpenHandler;
    }

    /**
     * @return the TreeView associated with the Explorer
     */
    public TreeView<File> getTreeView() {
        return treeView;
    }

    /**
     * @param workspace workspace to display
     */
    public void displayWorkspace(Workspace workspace) {
        TreeItem<File> root = new TreeItem<>(workspace.getRoot().toFile());
        for (String sub : Workspace.REQUIRED) {
            TreeItem<File> directory = new TreeItem<>(workspace.getRoot().resolve(sub).toFile());
            workspace.getFolderFiles(sub).forEach(f -> directory.getChildren().add(new TreeItem<>(f)));
            root.getChildren().add(directory);
        }
        root.setExpanded(true);
        treeView.setRoot(root);
    }

    private void setupDoubleClick() {
        treeView.setOnMouseClicked(evt -> {
            if (evt.getButton() == MouseButton.PRIMARY && evt.getClickCount() == 2) {
                TreeItem<File> sel = treeView.getSelectionModel().getSelectedItem();
                if (sel != null && sel.getValue().isFile() && fileOpenHandler != null) {
                    fileOpenHandler.accept(sel.getValue());
                }
            }
        });
    }

    @Override
    public Node getNode() {
        return treeView;
    }
}

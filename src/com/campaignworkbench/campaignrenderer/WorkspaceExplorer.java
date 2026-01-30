package com.campaignworkbench.campaignrenderer;

import com.campaignworkbench.ide.IJavaFxNode;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.function.Consumer;

import javafx.geometry.Insets;

/**
 * User interface control to explore and navigate the files in a workspace
 */
public class WorkspaceExplorer implements IJavaFxNode {

    private Workspace workspace;
    private TreeView<Object> treeView;
    private Consumer<WorkspaceFile> fileOpenHandler;
    /**
     * The panel containing the workspace explorer
     */
    private VBox workspaceExplorerPanel;

    /**
     * @param label           Label to use for the control in the UI
     * @param fileOpenHandler that handles double clicks of files in the Explorer
     */
    public WorkspaceExplorer(String label, Workspace workspace, Consumer<WorkspaceFile> fileOpenHandler) {
        this.workspace = workspace;
        treeView = new TreeView<>();
        setupDoubleClick();

        treeView.setCellFactory(tv -> new TreeCell<>() {

            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else if (item instanceof WorkspaceFile workspaceFileItem) {
                    setText(workspaceFileItem.getBaseFileName());
                } else {
                    setText(item.toString());
                }
            }
        });
        this.fileOpenHandler = fileOpenHandler;

        Label explorerLabel = new Label(label);
        explorerLabel.setPadding(new Insets(0, 0, 0, 5));
        // explorerLabel.setStyle("-fx-font-weight: bold;");

        workspaceExplorerPanel = new VBox(5, explorerLabel, treeView);
        workspaceExplorerPanel.setMinHeight(0);
        VBox.setVgrow(treeView, Priority.ALWAYS);
        workspaceExplorerPanel.setPadding(new Insets(0, 0, 0, 5));

        if (workspace != null) {
            refreshWorkspace();
        }
    }

    /**
     * @return the TreeView associated with the Explorer
     */
    public TreeView<Object> getTreeView() {
        return treeView;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
        refreshWorkspace();
    }

    /**
     * Refresh the workspace tree
     */
    public void refreshWorkspace() {

        TreeItem<Object> root = new TreeItem<>(workspace.getRootFolderPath().toString());
        TreeItem<Object> templateRoot = new TreeItem<>("Templates");
        TreeItem<Object> moduleRoot = new TreeItem<>("Modules");
        TreeItem<Object> blockRoot = new TreeItem<>("Blocks");
        TreeItem<Object> contextRoot = new TreeItem<>("Contexts");

        // Add templates
        for (Template template : workspace.getTemplates()) {
            TreeItem<Object> treeItem = new TreeItem<>(template);
            templateRoot.getChildren().add(treeItem);
        }
        templateRoot.setExpanded(true);

        // Add modules
        for (EtmModule module : workspace.getModules()) {
            TreeItem<Object> treeItem = new TreeItem<>(module);
            moduleRoot.getChildren().add(treeItem);
        }
        moduleRoot.setExpanded(true);

        // Add blocks
        for (PersonalisationBlock block : workspace.getBlocks()) {
            TreeItem<Object> treeItem = new TreeItem<>(block);
            blockRoot.getChildren().add(treeItem);
        }
        blockRoot.setExpanded(true);

        // Add contexts
        for (ContextXml context : workspace.getContexts()) {
            TreeItem<Object> treeItem = new TreeItem<>(context);
            contextRoot.getChildren().add(treeItem);
        }
        contextRoot.setExpanded(true);

        root.getChildren().add(templateRoot);
        root.getChildren().add(moduleRoot);
        root.getChildren().add(blockRoot);
        root.getChildren().add(contextRoot);

        root.setExpanded(true);
        treeView.setRoot(root);
    }

    private void setupDoubleClick() {
        treeView.setOnMouseClicked(evt -> {
            if (evt.getButton() == MouseButton.PRIMARY && evt.getClickCount() == 2) {

                TreeItem<Object> selectedItem = treeView.getSelectionModel().getSelectedItem();
                Object selectedObject = selectedItem.getValue();

                if (selectedObject instanceof WorkspaceFile workspaceFile) {
                    if (fileOpenHandler != null) {
                        fileOpenHandler.accept(workspaceFile);
                    }
                }
            }
        });
    }

    @Override
    public Node getNode() {
        return workspaceExplorerPanel;
    }
}

package com.campaignworkbench.campaignrenderer;

import com.campaignworkbench.ide.IJavaFxNode;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.utils.FontAwesomeIconFactory;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Consumer;

import javafx.geometry.Insets;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

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

        TreeItem<Object> root = WorkspaceExplorerItem.createTreeItem(FontAwesomeIcon.FOLDER_ALT, workspace.getRootFolderPath().toString(), "16px", 5, Color.YELLOW);
        TreeItem<Object> templateRoot = WorkspaceExplorerItem.createTreeItem(FontAwesomeIcon.ENVELOPE, "Templates", "16px", 5, Color.FORESTGREEN);
        TreeItem<Object> moduleRoot = WorkspaceExplorerItem.createTreeItem(FontAwesomeIcon.TASKS, "Modules", "16px", 5, Color.CORNFLOWERBLUE);
        TreeItem<Object> blockRoot = WorkspaceExplorerItem.createTreeItem(FontAwesomeIcon.LIST, "Blocks", "16px", 5, Color.PALEVIOLETRED);
        TreeItem<Object> contextRoot = WorkspaceExplorerItem.createTreeItem(FontAwesomeIcon.FILE_CODE_ALT, "Contexts", "16px", 5, Color.LIGHTCORAL);

        // Add templates
        for (Template template : workspace.getTemplates()) {
            TreeItem<Object> treeItem = WorkspaceExplorerItem.createTreeItem(template);
            Path dataContextPath = template.getDataContextFileName();
            Path messageContextPath = template.getMessageContextFileName();
            TreeItem<Object> dataContextItem = dataContextPath == null ? WorkspaceExplorerItem.createTreeItem("No Data Context") : WorkspaceExplorerItem.createTreeItem("Data: "  +dataContextPath.toString());
            TreeItem<Object> messageContextItem = messageContextPath == null ? WorkspaceExplorerItem.createTreeItem("No Message Context") : WorkspaceExplorerItem.createTreeItem("Message: "  +messageContextPath.toString());
            treeItem.getChildren().add(dataContextItem);
            treeItem.getChildren().add(messageContextItem);
            treeItem.setExpanded(true);
            templateRoot.getChildren().add(treeItem);
        }
        templateRoot.setExpanded(true);

        WorkspaceExplorerItem.enableMixedContent(treeView, 2, 6);

        // Add modules
        for (EtmModule module : workspace.getModules()) {
            TreeItem<Object> treeItem = new TreeItem<>(module);
            Path dataContextPath = module.getDataContextFileName();
            TreeItem<Object> dataContextItem = dataContextPath == null ? WorkspaceExplorerItem.createTreeItem("No Data Context") : WorkspaceExplorerItem.createTreeItem("Data: "  + dataContextPath.toString());
            treeItem.getChildren().add(dataContextItem);
            moduleRoot.getChildren().add(treeItem);
            // treeItem.setExpanded(true);
        }
        // moduleRoot.setExpanded(true);

        // Add blocks
        for (PersonalisationBlock block : workspace.getBlocks()) {
            TreeItem<Object> treeItem = new TreeItem<>(block);
            blockRoot.getChildren().add(treeItem);
        }
        // blockRoot.setExpanded(true);

        // Add contexts
        for (ContextXml context : workspace.getContexts()) {
            TreeItem<Object> treeItem = new TreeItem<>(context);
            contextRoot.getChildren().add(treeItem);
        }
        // contextRoot.setExpanded(true);

        root.getChildren().add(templateRoot);
        root.getChildren().add(moduleRoot);
        root.getChildren().add(blockRoot);
        root.getChildren().add(contextRoot);

        root.setExpanded(true);
        treeView.setRoot(root);
    }

    private void setupDoubleClick() {
        treeView.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, evt -> {
            if (evt.getButton() == MouseButton.PRIMARY && evt.getClickCount() == 2) {

                TreeItem<Object> selectedItem = treeView.getSelectionModel().getSelectedItem();
                if (selectedItem == null) return;

                Object selectedObject = selectedItem.getValue();

                if (selectedObject instanceof WorkspaceFile workspaceFile) {
                    if (fileOpenHandler != null) {
                        fileOpenHandler.accept(workspaceFile);

                        // Important: consume the event *before* TreeCell handles it
                        evt.consume();
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

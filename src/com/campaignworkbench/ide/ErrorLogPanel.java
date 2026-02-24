package com.campaignworkbench.ide;

import com.campaignworkbench.campaignrenderer.RendererException;
import com.campaignworkbench.campaignrenderer.WorkspaceFile;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Implements a TreeView containing a list of errors with details as TreeItems.
 */
public class ErrorLogPanel implements IJavaFxNode {

    private final TreeView<String> errorTreeView;
    /**
     * The panel containing the error log
     */
    VBox logPanel;
    private final Map<TreeItem<String>, RendererException> errorData = new HashMap<>();
    private BiConsumer<WorkspaceFile, Integer> onErrorDoubleClicked;

    /**
     * Constructor
     * @param label The label for the error log panel
     */
    public ErrorLogPanel(String label) {
        Label logLabel = new Label(label);
        logLabel.setPadding(new Insets(0,0, 0,5));
        // logLabel.setStyle("-fx-font-weight: bold;");

        errorTreeView = new TreeView<>();
        errorTreeView.setShowRoot(false);

        errorTreeView.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                TreeItem<String> selectedItem = errorTreeView.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    // If a child node is clicked, we want to find its parent error node
                    TreeItem<String> errorNode = selectedItem;
                    while (errorNode != null && !errorData.containsKey(errorNode)) {
                        errorNode = errorNode.getParent();
                    }

                    if (errorNode != null && errorData.containsKey(errorNode)) {
                        RendererException ex = errorData.get(errorNode);
                        if (onErrorDoubleClicked != null) {
                            onErrorDoubleClicked.accept(ex.getWorkspaceFile(), ex.getTemplateLine());
                        }
                    }
                }
            }
        });

        logPanel = new VBox(5, logLabel, errorTreeView);
        logPanel.setPadding(new Insets(0,0, 0,5));
        logPanel.setMinHeight(0);
        VBox.setVgrow(errorTreeView, Priority.ALWAYS);
    }

    /**
     * Sets the callback for when an error is double-clicked
     * @param callback the callback function accepting template name and line number
     */
    public void setOnErrorDoubleClicked(BiConsumer<WorkspaceFile, Integer> callback) {
        this.onErrorDoubleClicked = callback;
    }

    /**
     * Clears all errors from the tree view
     */
    public void clearErrors() {
        errorTreeView.setRoot(new TreeItem<>("Root"));
        errorData.clear();
    }

    /**
     * Adds an error to the tree view based on the provided exception
     * @param exception the template exception to add
     */
    public void addError(Exception exception) {
        if (errorTreeView.getRoot() == null) {
            clearErrors();
        }

        if(exception instanceof RendererException templateException) {

            TreeItem<String> errorNode = new TreeItem<>(templateException.getMessage() + " at line " + (templateException.getTemplateLine() == -1 ? "N/A" : templateException.getTemplateLine()));
            errorNode.setExpanded(false);
            errorData.put(errorNode, templateException);

            errorNode.getChildren().add(new TreeItem<>("Type: " + templateException.getClass().getSimpleName()));
            errorNode.getChildren().add(new TreeItem<>("Template: " + (templateException.getWorkspaceFile() == null ? "" : templateException.getWorkspaceFile().getFileName())));
            errorNode.getChildren().add(new TreeItem<>("Pre Process JS Line: " + (templateException.getTemplateLine() == -1 ? "N/A" : templateException.getTemplateLine())));

            TreeItem<String> rootCauseNode = new TreeItem<>("Root Cause: " + templateException.getRootCause());
            errorNode.getChildren().add(rootCauseNode);

            TreeItem<String> solutionNode = new TreeItem<>("Recommended Solution: " + templateException.getSolution());
            errorNode.getChildren().add(solutionNode);

            errorTreeView.getRoot().getChildren().add(errorNode);
        }
    }

    @Override
    public Node getNode() {
        return logPanel;
    }
}

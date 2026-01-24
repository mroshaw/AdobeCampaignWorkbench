package com.campaignworkbench.ide;

import com.campaignworkbench.campaignrenderer.TemplateException;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class ErrorLogPanel implements IJavaFxNode {

    private TreeView<String> errorTreeView;
    /**
     * The panel containing the error log
     */
    VBox logPanel;
    private Map<TreeItem<String>, TemplateException> errorData = new HashMap<>();
    private BiConsumer<String, Integer> onErrorDoubleClicked;

    /**
     * Constructor
     * @param label The label for the error log panel
     */
    public ErrorLogPanel(String label) {
        Label logLabel = new Label(label);
        logLabel.setPadding(new Insets(0,0, 0,5));
        logLabel.setStyle("-fx-font-weight: bold;");

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
                        TemplateException ex = errorData.get(errorNode);
                        if (onErrorDoubleClicked != null) {
                            onErrorDoubleClicked.accept(ex.getTemplateName(), ex.getTemplateLine());
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
    public void setOnErrorDoubleClicked(BiConsumer<String, Integer> callback) {
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
     * @param ex the template exception to add
     */
    public void addError(TemplateException ex) {
        if (errorTreeView.getRoot() == null) {
            clearErrors();
        }

        TreeItem<String> errorNode = new TreeItem<>(ex.getMessage() + " at line " +  (ex.getTemplateLine() == -1 ? "N/A" : ex.getTemplateLine()));
        errorNode.setExpanded(false);
        errorData.put(errorNode, ex);

        errorNode.getChildren().add(new TreeItem<>("Type: " + ex.getClass().getSimpleName()));
        errorNode.getChildren().add(new TreeItem<>("File: " + ex.getTemplateName()));
        errorNode.getChildren().add(new TreeItem<>("Line: " + (ex.getTemplateLine() == -1 ? "N/A" : ex.getTemplateLine())));
        
        TreeItem<String> rootCauseNode = new TreeItem<>("Root Cause: " + ex.getRootCause());
        errorNode.getChildren().add(rootCauseNode);

        TreeItem<String> solutionNode = new TreeItem<>("Recommended Solution: " + ex.getSolution());
        errorNode.getChildren().add(solutionNode);

        errorTreeView.getRoot().getChildren().add(errorNode);
    }

    @Override
    public Node getNode() {
        return logPanel;
    }
}

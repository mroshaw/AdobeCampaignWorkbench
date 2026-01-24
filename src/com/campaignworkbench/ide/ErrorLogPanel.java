package com.campaignworkbench.ide;

import com.campaignworkbench.campaignrenderer.TemplateException;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ErrorLogPanel implements IJavaFxNode {

    private TreeView<String> errorTreeView;
    VBox logPanel;

    public ErrorLogPanel(String label) {
        Label logLabel = new Label(label);
        logLabel.setPadding(new Insets(0,0, 0,5));
        logLabel.setStyle("-fx-font-weight: bold;");

        errorTreeView = new TreeView<>();
        errorTreeView.setShowRoot(false);
        logPanel = new VBox(5, logLabel, errorTreeView);
        logPanel.setPadding(new Insets(0,0, 0,5));
        VBox.setVgrow(errorTreeView, Priority.ALWAYS);
    }

    public void clearErrors() {
        errorTreeView.setRoot(new TreeItem<>("Root"));
    }

    public void addError(TemplateException ex) {
        if (errorTreeView.getRoot() == null) {
            clearErrors();
        }

        TreeItem<String> errorNode = new TreeItem<>(ex.getMessage());
        errorNode.setExpanded(false);

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

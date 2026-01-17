package com.campaignworkbench.ide.editor;

import com.campaignworkbench.ide.IThemeable;
import com.campaignworkbench.ide.IDETheme;
import com.campaignworkbench.ide.ThemeManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

public class MainMenuBar implements IThemeable, IJavaFxNode  {

    MenuBar menuBar;

    public MainMenuBar(
            EventHandler<ActionEvent> openTemplateFileHandler,
            EventHandler<ActionEvent> openBlockFileHandler,
            EventHandler<ActionEvent> openXMLFileHandler,
            EventHandler<ActionEvent> saveCurrentFileHandler,

            EventHandler<ActionEvent> applyLightThemeHandler,
            EventHandler<ActionEvent> applyDarkThemeHandler
    ) {
        menuBar = new MenuBar();

        // --- File Menu ---
        Menu fileMenu = new Menu("File");

        // Open submenu
        Menu openSub = new Menu("Open");
        MenuItem openWorkspace = new MenuItem("Workspace");
        MenuItem openTemplate = new MenuItem("Template");
        MenuItem openBlock = new MenuItem("Block");
        MenuItem openXmlContext = new MenuItem("XML Context");

        openWorkspace.setOnAction(e -> dummyHandler("Open Workspace"));
        openTemplate.setOnAction(openTemplateFileHandler);
        openBlock.setOnAction(openBlockFileHandler);
        openXmlContext.setOnAction(openXMLFileHandler);

        openSub.getItems().addAll(openWorkspace, openTemplate, openBlock, openXmlContext);

        // New submenu
        Menu newSub = new Menu("New");
        MenuItem newWorkspace = new MenuItem("Workspace");
        MenuItem newTemplate = new MenuItem("Template");
        MenuItem newBlock = new MenuItem("Block");
        MenuItem newXmlContext = new MenuItem("XML Context");

        newWorkspace.setOnAction(e -> dummyHandler("New Workspace"));
        newTemplate.setOnAction(e -> dummyHandler("New Template"));
        newBlock.setOnAction(e -> dummyHandler("New Block"));
        newXmlContext.setOnAction(e -> dummyHandler("New XML Context"));

        newSub.getItems().addAll(newWorkspace, newTemplate, newBlock, newXmlContext);

        // Save items
        MenuItem saveCurrent = new MenuItem("Save Current");
        saveCurrent.setAccelerator(
                new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN)
        );
        MenuItem saveCurrentAs = new MenuItem("Save Current As");
        MenuItem exitItem = new MenuItem("Exit");

        saveCurrent.setOnAction(saveCurrentFileHandler);
        saveCurrentAs.setOnAction(e -> dummyHandler("Save Current As"));
        exitItem.setOnAction(e -> Platform.exit());

        fileMenu.getItems().addAll(openSub, newSub, saveCurrent, saveCurrentAs, new SeparatorMenuItem(), exitItem);

        // --- Help Menu ---
        Menu helpMenu = new Menu("Help");
        MenuItem aboutItem = new MenuItem("About");
        aboutItem.setOnAction(e -> dummyHandler("About"));
        helpMenu.getItems().add(aboutItem);

        Menu viewMenu = new Menu("View");
        MenuItem darkThemeItem = new MenuItem("Dark Theme");
        darkThemeItem.setOnAction(applyDarkThemeHandler);
        viewMenu.getItems().add(darkThemeItem);

        MenuItem lightThemeItem = new MenuItem("Light Theme");
        lightThemeItem.setOnAction(applyLightThemeHandler);
        viewMenu.getItems().add(lightThemeItem);


        // --- Add menus to menu bar ---
        menuBar.getMenus().addAll(fileMenu, viewMenu, helpMenu);

        ThemeManager.register(this);
    }

    private void dummyHandler(String action) {

    }

    @Override
    public void applyTheme(IDETheme theme) {
        switch (theme) {
            case DARK:
                menuBar.setStyle(
                        "-fx-background-color: #2b2b2b; " +
                                "-fx-text-fill: #dddddd;"
                );
                // Optional: recursively style menu items
                menuBar.getMenus().forEach(this::applyDarkToMenu);
                break;

            case LIGHT:
            default:
                menuBar.setStyle(
                        "-fx-background-color: white; " +
                                "-fx-text-fill: black;"
                );
                menuBar.getMenus().forEach(this::applyLightToMenu);
                break;
        }
    }

    @Override
    public Node getNode() {
        return menuBar;
    }

    private void applyDarkToMenu(Menu menu) {
        menu.setStyle("-fx-text-fill: #dddddd;");
        menu.getItems().forEach(item -> item.setStyle("-fx-text-fill: #dddddd;"));
    }

    private void applyLightToMenu(Menu menu) {
        menu.setStyle("-fx-text-fill: black;");
        menu.getItems().forEach(item -> item.setStyle("-fx-text-fill: black;"));
    }
}

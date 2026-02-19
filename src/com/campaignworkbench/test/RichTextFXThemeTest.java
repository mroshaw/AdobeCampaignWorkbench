package com.campaignworkbench.test;

import atlantafx.base.theme.CupertinoDark;
import com.campaignworkbench.ide.editor.SyntaxType;
import com.campaignworkbench.ide.editor.richtextfx.RichTextFXEditor;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class RichTextFXThemeTest extends Application {
    public void start(Stage stage) {

        String stylesheet = new CupertinoDark().getUserAgentStylesheet();
        Application.setUserAgentStylesheet(null);
        Application.setUserAgentStylesheet(stylesheet);

        RichTextFXEditor editor = new RichTextFXEditor(SyntaxType.CAMPAIGN);
        Scene scene = new Scene((Parent) editor.getNode(), 1000, 800);

        stage.setTitle("Theme Test");
        stage.setScene(scene);
        stage.show();
    }

    static void main(String[] args) {
        launch(args);
    }
}

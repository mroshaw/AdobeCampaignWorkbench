package com.campaignworkbench.test;

import com.campaignworkbench.ide.editor.SyntaxType;
import com.campaignworkbench.ide.editor.richtextfx.RichTextFXEditor;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class RichTextFXEditorTest extends Application {

    @Override
    public void start(Stage stage) {
        RichTextFXEditor editor = new RichTextFXEditor(SyntaxType.XML);
        editor.setText("""
                <root>\n\t<parent>\n\t\t<child>value</child>\n\t</parent>\n\t<another>\n\t\t<nested>\n\t\t\t<deep>text</deep>\n\t\t</nested>\n\t</another>\n</root>""");
        Scene scene = new Scene((Parent) editor.getNode(), 400, 200);

        stage.setTitle("RichTextFX Editor Test");
        stage.setScene(scene);
        stage.show();
    }

    static void main(String[] args) {
        launch(args);
    }
}

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class JavaFXSmokeTest extends Application {

    @Override
    public void start(Stage stage) {
        Label label = new Label("JavaFX is working.");
        Scene scene = new Scene(label, 400, 200);

        stage.setTitle("JavaFX Smoke Test");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

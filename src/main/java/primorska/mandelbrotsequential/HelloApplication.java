package primorska.mandelbrotsequential;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.Pane;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import java.io.IOException;

public class HelloApplication extends Application {
    private static final int DEFAULT_WIDTH = 800;
    private static final int DEFAULT_HEIGHT = 600;

    public static void main(String[] args) {
        launch(args); // Launches the JavaFX application
    }

    @Override
    public void start(Stage primaryStage) {
        // Create a root layout
        Pane root = new Pane();

        // Create a scene with the default width and height
        Scene scene = new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT);

        // Set up the stage (window)
        primaryStage.setTitle("Mandelbrot Set Viewer");
        primaryStage.setScene(scene);

        // Allow the window to be resized
        primaryStage.setResizable(true);

        // Show the window
        primaryStage.show();
    }
}

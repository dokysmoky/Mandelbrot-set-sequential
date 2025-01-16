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
        // Create a canvas for drawing
        Canvas canvas = new Canvas(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        root.getChildren().add(canvas);

        // Get the graphics context of the canvas
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Draw the Mandelbrot set
        drawMandelbrot(gc, DEFAULT_WIDTH, DEFAULT_HEIGHT);

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
    private void drawMandelbrot(GraphicsContext gc, int width, int height) {
        // Iterate through each pixel on the canvas
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // Map pixel coordinates to the complex plane
                double real = map(x, 0, width, -2.5, 1);
                double imaginary = map(y, 0, height, -1, 1);

                // Compute the number of iterations for the Mandelbrot formula
                int iterations = mandelbrotIterations(real, imaginary, 1000);

                // Compute the color based on the number of iterations
                int color = iterationsToColor(iterations, 1000);

                // Draw the pixel
                gc.getPixelWriter().setArgb(x, y, color);
            }
        }
    }

    private double map(double value, double minSource, double maxSource, double minTarget, double maxTarget) {
        // Map a value from one range to another
        return minTarget + (value - minSource) * (maxTarget - minTarget) / (maxSource - minSource);
    }

    private int mandelbrotIterations(double real, double imaginary, int maxIterations) {
        double zReal = 0;
        double zImaginary = 0;
        int iterations = 0;

        while (iterations < maxIterations && (zReal * zReal + zImaginary * zImaginary) <= 4) {
            double tempReal = zReal * zReal - zImaginary * zImaginary + real;
            zImaginary = 2 * zReal * zImaginary + imaginary;
            zReal = tempReal;
            iterations++;
        }

        return iterations;
    }

    private int iterationsToColor(int iterations, int maxIterations) {
        if (iterations == maxIterations) {
            return 0xFF000000; // Black for points inside the set
        }
        int colorValue = (int) (255.0 * iterations / maxIterations);
        return 0xFF000000 | (colorValue << 16) | (colorValue << 8) | colorValue; // Grayscale
    }
}

package primorska.mandelbrotsequential;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class HelloApplication extends Application {

    private double minX = -2.5, maxX = 1.5;
    private double minY = -1.5, maxY = 1.5;
    private double zoomFactor = 1.0;
    private Canvas canvas;
    private GraphicsContext gc;
    private TextField widthField;
    private TextField heightField;
    private int imageWidth = 800;
    private int imageHeight = 600;

    @Override
    public void start(Stage primaryStage) {
        canvas = new Canvas(imageWidth, imageHeight);
        gc = canvas.getGraphicsContext2D();

        // Create UI elements for size input and saving
        widthField = new TextField(String.valueOf(imageWidth));
        heightField = new TextField(String.valueOf(imageHeight));
        Button resizeButton = new Button("Resize");
        Button saveButton = new Button("Save");

        resizeButton.setOnAction(e -> handleResize());
        saveButton.setOnAction(e -> handleSave(primaryStage));

        HBox controls = new HBox(10, widthField, heightField, resizeButton, saveButton);

        // Create AnchorPane as the root container
        AnchorPane root = new AnchorPane();
        root.getChildren().addAll(canvas, controls);

        // Anchor the controls at the bottom of the window
        AnchorPane.setBottomAnchor(controls, 10.0);
        AnchorPane.setLeftAnchor(controls, 10.0);
        AnchorPane.setRightAnchor(controls, 10.0);

        // Create the scene and add key event listener for movement
        Scene scene = new Scene(root);
        scene.setOnKeyPressed(event -> {
            if (canvas.isFocused()) {
                switch (event.getCode()) {
                    case ADD:
                    case PLUS:
                        zoomFactor *= 1.5;
                        break;
                    case SUBTRACT:
                    case MINUS:
                        zoomFactor /= 1.5;
                        break;
                    case UP:
                        minY -= 0.1 * (maxY - minY) / zoomFactor;
                        maxY -= 0.1 * (maxY - minY) / zoomFactor;
                        break;
                    case DOWN:
                        minY += 0.1 * (maxY - minY) / zoomFactor;
                        maxY += 0.1 * (maxY - minY) / zoomFactor;
                        break;
                    case LEFT:
                        minX -= 0.1 * (maxX - minX) / zoomFactor;
                        maxX -= 0.1 * (maxX - minX) / zoomFactor;
                        break;
                    case RIGHT:
                        minX += 0.1 * (maxX - minX) / zoomFactor;
                        maxX += 0.1 * (maxX - minX) / zoomFactor;
                        break;
                    default:
                        break;
                }
                drawMandelbrot();
            }
        });

        // Ensure focus is on canvas after scene is shown
        primaryStage.setOnShown(event -> Platform.runLater(() -> canvas.requestFocus()));

        primaryStage.setTitle("Mandelbrot Set Explorer");
        primaryStage.setScene(scene);
        primaryStage.show();

        drawMandelbrot();
    }

    private void handleResize() {
        try {
            imageWidth = Integer.parseInt(widthField.getText());
            imageHeight = Integer.parseInt(heightField.getText());
            canvas.setWidth(imageWidth);
            canvas.setHeight(imageHeight);
            drawMandelbrot();
        } catch (NumberFormatException e) {
            System.out.println("Invalid width or height.");
        }
    }

    private void handleSave(Stage primaryStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PNG Files", "*.png"),
                new FileChooser.ExtensionFilter("JPEG Files", "*.jpg"),
                new FileChooser.ExtensionFilter("JPG Files", "*.jpg")
        );
        File file = fileChooser.showSaveDialog(primaryStage);
        if (file != null) {
            saveImage(file);
        }
    }

    private void saveImage(File file) {
        WritableImage writableImage = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
        canvas.snapshot(null, writableImage);
        try {
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(writableImage, null);
            ImageIO.write(bufferedImage, "png", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void drawMandelbrot() {
        long startTime = System.nanoTime(); // Start timing

        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        double width = canvas.getWidth();
        double height = canvas.getHeight();
        double rangeX = (maxX - minX) / zoomFactor;
        double rangeY = (maxY - minY) / zoomFactor;

        for (int px = 0; px < width; px++) {
            for (int py = 0; py < height; py++) {
                double x0 = minX + px * rangeX / width;
                double y0 = minY + py * rangeY / height;
                double x = 0.0, y = 0.0;
                int iteration = 0;
                int maxIter = 1000;

                while (x * x + y * y <= 4 && iteration < maxIter) {
                    double xtemp = x * x - y * y + x0;
                    y = 2 * x * y + y0;
                    x = xtemp;
                    iteration++;
                }

                Color color;
                if (iteration < maxIter) {
                    double t = (double) iteration / maxIter;
                    color = Color.hsb(280 - t * 280, 0.8, 1.0 - t * 0.8);
                } else {
                    color = Color.BLACK;
                }

                gc.getPixelWriter().setColor(px, py, color);
            }
        }

        long endTime = System.nanoTime(); // End timing
        double elapsedTimeInMs = (endTime - startTime) / 1_000_000.0; // Convert to milliseconds

        // Log the elapsed time
        System.out.printf("Mandelbrot set drawn in %.2f ms%n", elapsedTimeInMs);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

package primorska.mandelbrotsequential;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class HelloApplication extends Application {

    private double minX = -2.5, maxX = 1.5;
    private double minY = -1.5, maxY = 1.5;
    private double zoomFactor = 1.0;
    private Canvas canvas;
    private GraphicsContext gc;
    private TextField widthField;
    private TextField heightField;
    private ComboBox<String> modeBox;
    private int imageWidth = 800;
    private int imageHeight = 600;
    private boolean needsRedraw = true;
    private long lastDrawTime = 0;
    private final long frameInterval = 16_666_667;

    private int javafxColorToRGB(Color color) {
        int r = (int) (color.getRed() * 255);
        int g = (int) (color.getGreen() * 255);
        int b = (int) (color.getBlue() * 255);
        int a = (int) (color.getOpacity() * 255);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    @Override
    public void start(Stage primaryStage) {
        canvas = new Canvas(imageWidth, imageHeight);
        gc = canvas.getGraphicsContext2D();

        widthField = new TextField(String.valueOf(imageWidth));
        heightField = new TextField(String.valueOf(imageHeight));
        Button resizeButton = new Button("Resize");
        Button saveButton = new Button("Save");

        modeBox = new ComboBox<>();
        modeBox.getItems().addAll("Sequential", "Parallel");
        modeBox.setValue("Sequential");

        resizeButton.setOnAction(e -> handleResize());
        saveButton.setOnAction(e -> handleSave(primaryStage));
        modeBox.setOnAction(e -> {
            needsRedraw = true;
            canvas.requestFocus();
        });

        HBox controls = new HBox(10, widthField, heightField, resizeButton, saveButton, modeBox);

        AnchorPane root = new AnchorPane();
        root.getChildren().addAll(canvas, controls);
        AnchorPane.setBottomAnchor(controls, 10.0);
        AnchorPane.setLeftAnchor(controls, 10.0);

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
                }
                needsRedraw = true;
            }
        });

        primaryStage.setOnShown(e -> Platform.runLater(() -> canvas.requestFocus()));
        primaryStage.setTitle("Mandelbrot Explorer");
        primaryStage.setScene(scene);
        primaryStage.show();

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (needsRedraw && now - lastDrawTime > frameInterval) {
                    drawMandelbrot(modeBox.getValue().equals("Parallel"));
                    lastDrawTime = now;
                    needsRedraw = false;
                }
            }
        };
        timer.start();
    }

    private void drawMandelbrot(boolean isParallel) {
        long startTime = System.nanoTime();
        int width = (int) canvas.getWidth();
        int height = (int) canvas.getHeight();
        double rangeX = (maxX - minX) / zoomFactor;
        double rangeY = (maxY - minY) / zoomFactor;
        int maxIter = 1000;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Runnable compute = () -> {
            if (isParallel) {
                int cores = Runtime.getRuntime().availableProcessors();
                Thread[] threads = new Thread[cores];
                for (int t = 0; t < cores; t++) {
                    final int threadId = t;
                    threads[t] = new Thread(() -> {
                        for (int y = threadId; y < height; y += cores) {
                            for (int x = 0; x < width; x++) {
                                double x0 = minX + x * rangeX / width;
                                double y0 = minY + y * rangeY / height;
                                double zx = 0.0, zy = 0.0;
                                int iter = 0;
                                while (zx * zx + zy * zy <= 4 && iter < maxIter) {
                                    double tmp = zx * zx - zy * zy + x0;
                                    zy = 2 * zx * zy + y0;
                                    zx = tmp;
                                    iter++;
                                }
                                int rgb = (iter < maxIter)
                                        ? javafxColorToRGB(Color.hsb(280 - ((double) iter / maxIter) * 280, 0.8, 1.0 - ((double) iter / maxIter) * 0.8))
                                        : 0xFF000000;
                                image.setRGB(x, y, rgb);
                            }
                        }
                    });
                    threads[t].start();
                }
                for (Thread thread : threads) {
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        double x0 = minX + x * rangeX / width;
                        double y0 = minY + y * rangeY / height;
                        double zx = 0.0, zy = 0.0;
                        int iter = 0;
                        while (zx * zx + zy * zy <= 4 && iter < maxIter) {
                            double tmp = zx * zx - zy * zy + x0;
                            zy = 2 * zx * zy + y0;
                            zx = tmp;
                            iter++;
                        }
                        int rgb = (iter < maxIter)
                                ? javafxColorToRGB(Color.hsb(280 - ((double) iter / maxIter) * 280, 0.8, 1.0 - ((double) iter / maxIter) * 0.8))
                                : 0xFF000000;
                        image.setRGB(x, y, rgb);
                    }
                }
            }

            Platform.runLater(() -> {
                WritableImage fxImage = SwingFXUtils.toFXImage(image, null);
                gc.clearRect(0, 0, width, height);
                gc.drawImage(fxImage, 0, 0);
                System.out.printf("Rendered in %.2f ms [%s]%n", (System.nanoTime() - startTime) / 1e6,
                        isParallel ? "Parallel" : "Sequential");
            });
        };

        new Thread(compute).start();
    }

    private void handleResize() {
        try {
            imageWidth = Integer.parseInt(widthField.getText());
            imageHeight = Integer.parseInt(heightField.getText());
            canvas.setWidth(imageWidth);
            canvas.setHeight(imageHeight);
            needsRedraw = true;
            canvas.requestFocus();
        } catch (NumberFormatException e) {
            System.out.println("Invalid input for width/height.");
        }
    }

    private void handleSave(Stage stage) {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Image", "*.png"));
        File file = chooser.showSaveDialog(stage);
        if (file != null) {
            WritableImage image = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
            canvas.snapshot(null, image);
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void generateAndSaveImage(boolean parallel) {
        int width = 800, height = 600;
        double minX = -2.5, maxX = 1.5;
        double minY = -1.5, maxY = 1.5;
        double zoomFactor = 1.0;
        int maxIter = 1000;

        WritableImage image = new WritableImage(width, height);
        PixelWriter writer = image.getPixelWriter();

        Runnable task = () -> {
            if (parallel) {
                int cores = Runtime.getRuntime().availableProcessors();
                Thread[] threads = new Thread[cores];
                for (int t = 0; t < cores; t++) {
                    final int id = t;
                    threads[t] = new Thread(() -> {
                        for (int y = id; y < height; y += cores) {
                            for (int x = 0; x < width; x++) {
                                double x0 = minX + x * (maxX - minX) / width / zoomFactor;
                                double y0 = minY + y * (maxY - minY) / height / zoomFactor;
                                double zx = 0.0, zy = 0.0;
                                int iter = 0;
                                while (zx * zx + zy * zy <= 4 && iter < maxIter) {
                                    double tmp = zx * zx - zy * zy + x0;
                                    zy = 2 * zx * zy + y0;
                                    zx = tmp;
                                    iter++;
                                }
                                Color color = (iter < maxIter)
                                        ? Color.hsb(280 - ((double) iter / maxIter) * 280, 0.8, 1.0 - ((double) iter / maxIter) * 0.8)
                                        : Color.BLACK;
                                synchronized (writer) {
                                    writer.setColor(x, y, color);
                                }
                            }
                        }
                    });
                    threads[t].start();
                }
                for (Thread thread : threads) {
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        double x0 = minX + x * (maxX - minX) / width / zoomFactor;
                        double y0 = minY + y * (maxY - minY) / height / zoomFactor;
                        double zx = 0.0, zy = 0.0;
                        int iter = 0;
                        while (zx * zx + zy * zy <= 4 && iter < maxIter) {
                            double tmp = zx * zx - zy * zy + x0;
                            zy = 2 * zx * zy + y0;
                            zx = tmp;
                            iter++;
                        }
                        Color color = (iter < maxIter)
                                ? Color.hsb(280 - ((double) iter / maxIter) * 280, 0.8, 1.0 - ((double) iter / maxIter) * 0.8)
                                : Color.BLACK;
                        writer.setColor(x, y, color);
                    }
                }
            }

            File file = new File(parallel ? "mandelbrot_parallel.png" : "mandelbrot.png");
            try {
                BufferedImage bimg = SwingFXUtils.fromFXImage(image, null);
                ImageIO.write(bimg, "png", file);
                System.out.println("Saved to " + file.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        new Thread(task).start();
    }

    public static void main(String[] args) {
        boolean guiMode = true;
        boolean parallelMode = false;

        for (String arg : args) {
            if (arg.equalsIgnoreCase("--no-gui")) {
                guiMode = false;
            } else if (arg.equalsIgnoreCase("--parallel")) {
                parallelMode = true;
            }
        }

        if (guiMode) {
            launch(args);
        } else {
            System.out.println("Generating image in " + (parallelMode ? "parallel" : "sequential") + " mode...");
            generateAndSaveImage(parallelMode);
        }
    }
}

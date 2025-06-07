
package primorska.mandelbrotsequential.distributed;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class WorkerServer {

    private static int mandelbrotColor(double x0, double y0, int maxIter) {
        double zx = 0.0, zy = 0.0;
        int iter = 0;
        while (zx * zx + zy * zy <= 4 && iter < maxIter) {
            double tmp = zx * zx - zy * zy + x0;
            zy = 2 * zx * zy + y0;
            zx = tmp;
            iter++;
        }

        if (iter < maxIter) {
            int hue = (int) (280 - ((double) iter / maxIter) * 280);
            return hsbToRGB(hue, 0.8, 1.0 - ((double) iter / maxIter) * 0.8);
        } else {
            return 0xFF000000; // opaque black
        }
    }

    private static int hsbToRGB(double hue, double saturation, double brightness) {
        int rgb = java.awt.Color.HSBtoRGB((float)(hue / 360.0), (float)saturation, (float)brightness);
        return 0xFF000000 | (rgb & 0x00FFFFFF); // ensure full alpha
    }

    public static void main(String[] args) throws IOException {
       // int port = 5000;
        int port = Integer.parseInt(args[0]);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Worker ready on port " + port);

            while (true) {
                try (Socket client = serverSocket.accept();
                     ObjectInputStream in = new ObjectInputStream(client.getInputStream());
                     ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream())) {

                    Task task = (Task) in.readObject();
                    System.out.println("Received task: lines " + task.getStartY() + " to " + task.getEndY());

                    int width = task.getWidth();
                    int height = task.getHeight();
                    int[] pixels = new int[(task.getEndY() - task.getStartY()) * width];

                    double rangeX = (task.getMaxX() - task.getMinX()) / task.getZoomFactor();
                    double rangeY = (task.getMaxY() - task.getMinY()) / task.getZoomFactor();

                    for (int y = task.getStartY(); y < task.getEndY(); y++) {
                        for (int x = 0; x < width; x++) {
                            double x0 = task.getMinX() + x * rangeX / width;
                            double y0 = task.getMinY() + y * rangeY / height;
                            int rgb = mandelbrotColor(x0, y0, task.getMaxIter());
                            pixels[(y - task.getStartY()) * width + x] = rgb;
                        }
                    }

                    Result result = new Result(task.getStartY(), pixels);
                    out.writeObject(result);
                    out.flush();

                    System.out.println("Sent result for lines " + task.getStartY() + " to " + task.getEndY());
                } catch (Exception e) {
                    System.err.println("Error processing task: " + e.getMessage());
                }
            }
        }
    }
}

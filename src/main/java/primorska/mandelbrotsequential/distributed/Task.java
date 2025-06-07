package primorska.mandelbrotsequential.distributed;

import java.io.Serializable;

public class Task implements Serializable {
    private final int startY;
    private final int endY;
    private final int width;
    private final int height;
    private final double minX, maxX, minY, maxY;
    private final double zoomFactor;
    private final int maxIter;

    public Task(int startY, int endY, int width, int height,
                double minX, double maxX, double minY, double maxY,
                double zoomFactor, int maxIter) {
        this.startY = startY;
        this.endY = endY;
        this.width = width;
        this.height = height;
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.zoomFactor = zoomFactor;
        this.maxIter = maxIter;
    }

    public int getStartY() { return startY; }
    public int getEndY() { return endY; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public double getMinX() { return minX; }
    public double getMaxX() { return maxX; }
    public double getMinY() { return minY; }
    public double getMaxY() { return maxY; }
    public double getZoomFactor() { return zoomFactor; }
    public int getMaxIter() { return maxIter; }
}

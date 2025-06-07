
package primorska.mandelbrotsequential.distributed;

import java.io.Serializable;

public class Result implements Serializable {
    public final int startY;
    public final int[] rgbValues;

    public Result(int startY, int[] rgbValues) {
        this.startY = startY;
        this.rgbValues = rgbValues;
    }
}

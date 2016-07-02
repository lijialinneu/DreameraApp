package neu.dreamerajni.utils;

/**
 * Created by 10405 on 2016/7/2.
 */

public class OpenCVHelper {
    static {
        System.loadLibrary("OpenCV");
    }

    public static native int[] Canny(int[] buf, int w, int h);
}

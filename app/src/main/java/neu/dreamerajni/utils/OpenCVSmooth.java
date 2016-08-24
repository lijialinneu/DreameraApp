package neu.dreamerajni.utils;

/**
 * Created by 10405 on 2016/7/5.
 */

public class OpenCVSmooth {
    static {
        System.loadLibrary("OpenCV");
    }

    /**
     * 边缘检测
     * @param buf
     * @param w
     * @param h
     * @return
     */
    public static native int[] smooth(int[] buf, int w, int h);
}

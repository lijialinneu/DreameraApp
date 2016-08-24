package neu.dreamerajni.filter;

import android.graphics.Bitmap;

/**
 * Created by 10405 on 2016/7/26.
 * extends BaseFilter
 */

class JewelryBlue extends BaseFilter {
    JewelryBlue(Bitmap src) {
        super(src);
    }

    @Override
    public Bitmap filterBitmap() {
        setCm(new float[] { //宝石蓝
                1, 0, 0, 0, 0,
                0, 1, 0, 0, 0,
                0, 0, 1.6f, 0, 0,
                0, 0, 0, 1, 0 });
        return super.filterBitmap();
    }
}

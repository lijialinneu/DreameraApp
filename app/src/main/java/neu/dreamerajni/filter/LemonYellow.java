package neu.dreamerajni.filter;

import android.graphics.Bitmap;

/**
 * Created by 10405 on 2016/7/26.
 * extends BaseFilter
 */

class LemonYellow extends BaseFilter {
    LemonYellow(Bitmap src) {
        super(src);
    }

    @Override
    public Bitmap filterBitmap() {
        setCm(new float[] { //荧光绿
                1, 0, 0, 0, 50,
                0, 1, 0, 0, 50,
                0, 0, 1, 6, 0,
                0, 0, 0, 1, 0 });
        return super.filterBitmap();
    }
}

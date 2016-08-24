package neu.dreamerajni.filter;

import android.graphics.Bitmap;

/**
 * Created by 10405 on 2016/7/26.
 * FluorescentGreen extends BaseFilter
 */

class FluorescentGreen extends BaseFilter {
    FluorescentGreen(Bitmap src) {
        super(src);
    }

    @Override
    public Bitmap filterBitmap() {
        setCm(new float[] { //荧光绿
                1, 0, 0, 0, 0,
                0, 1.4f, 0, 0, 0,
                0, 0, 1, 0, 0,
                0, 0, 0, 1, 0 });
        return super.filterBitmap();
    }
}

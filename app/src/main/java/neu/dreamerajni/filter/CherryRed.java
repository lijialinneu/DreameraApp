package neu.dreamerajni.filter;

import android.graphics.Bitmap;

/**
 * Created by 10405 on 2016/7/26.
 * CherryRed extends BaseFilter
 */

class CherryRed extends BaseFilter{
    CherryRed(Bitmap src) {
        super(src);
    }

    @Override
    public Bitmap filterBitmap() {
        setCm(new float[] { //樱桃红
                2, 0, 0, 0, 0,
                0, 1, 0, 0, 0,
                0, 0, 1, 0, 0,
                0, 0, 0, 1, 0 });
        return super.filterBitmap();
    }
}

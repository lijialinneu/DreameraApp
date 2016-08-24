package neu.dreamerajni.filter;

import android.graphics.Bitmap;

import java.util.ArrayList;

/**
 * Created by 10405 on 2016/7/26.
 * This class use static factory model to construct each
 * Filter class.
 */

public class FilterFactory {
    private Bitmap src;
    private ArrayList<String> typeArray = new ArrayList<String>(){{
        add("高饱和");add("灰度");add("怀旧");add("宝丽来");
        add("樱桃红");add("柠檬青");add("荧光绿");add("宝石蓝");
    }};


    public FilterFactory(Bitmap src) {
        this.src = src;
    }

    public String getFilterType(int i) {
        return typeArray.get(i);
    }

    public BaseFilter createFilter(int id) {
        BaseFilter bf;
        switch (id) {
            case 0:
                bf = new HSAT(src);
                break;
            case 1:
                bf = new Gray(src);
                break;
            case 2:
                bf = new Nostalgic(src);
                break;
            case 3:
                bf = new Polaroid(src);
                break;
            case 4:
                bf = new CherryRed(src);
                break;
            case 5:
                bf = new LemonYellow(src);
                break;
            case 6:
                bf = new FluorescentGreen(src);
                break;
            case 7:
                bf = new JewelryBlue(src);
                break;
            default:
                bf =  new BaseFilter(src);
                break;
        }
        return bf;
    }

}

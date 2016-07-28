package neu.dreamerajni.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

/**
 * Created by lijialin on 16-3-9.
 */


public class ZoomListener implements ScaleGestureDetector.OnScaleGestureListener,
        View.OnTouchListener {

    private final int INITMODE = 0; //初始状态
    private final int DRAGMODE = 1; //拖拽状态
    private final int ZOOMMODE = 2; //放缩状态
    private int mode = INITMODE;

    private float initX, initY;//第一次触摸的x、y值
    private float lastX, lastY;//第一次触摸的x、y值
    private boolean flag = true;

    private ScaleGestureDetector mScaleGestureDetector = null;

    private Matrix mScaleMatrix = new Matrix();
    private Matrix mScaleMatrix2 = new Matrix();
    private Matrix saveMatrix = new Matrix(); // 保存的matrix

    /**
     * 构造函数
     * @author 10405
     * @param bitmap
     */
    public ZoomListener(Context context, Bitmap bitmap) {
        mScaleGestureDetector = new ScaleGestureDetector(context, this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch(event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: //手指按下
                mode = DRAGMODE;
                saveMatrix.set(mScaleMatrix);
                initX = event.getX();
                initY = event.getY();
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                mode = ZOOMMODE;
                break;

            case MotionEvent.ACTION_MOVE:
                if(mode == DRAGMODE){
                    mScaleMatrix.set(saveMatrix);
                    mScaleMatrix.postTranslate(event.getX() - initX, event.getY() - initY);// 平移
                    zoom(mScaleMatrix);
                } else if(mode == ZOOMMODE) {
                    mScaleGestureDetector.onTouchEvent(event);
                }
                break;

            case MotionEvent.ACTION_UP: //手指抬起
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_OUTSIDE: //出界
                mode = INITMODE;
                break;

        }
        return true; //这个一定要写true，否则对移动等event无反应
    }


    /**
     * 放缩函数，需要被override
     * @author 10405
     * @param matrix
     */
    public void zoom (Matrix matrix) {}


    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        float scaleFactor = detector.getScaleFactor();
        mScaleMatrix.postScale(scaleFactor, scaleFactor,
                detector.getFocusX(), detector.getFocusY());
        zoom(mScaleMatrix);
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
    }

}
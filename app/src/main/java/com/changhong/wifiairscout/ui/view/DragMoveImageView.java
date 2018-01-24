package com.changhong.wifiairscout.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

/**
 * Created by fuheng on 2017/12/15.
 */

public class DragMoveImageView extends AppCompatImageView {
    public DragMoveImageView(Context context) {
        super(context);
        init();
    }

    public DragMoveImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DragMoveImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

//        getDrawable().setBounds(0,0,getWidth(),getHeight());
//        getDrawable().draw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        return true;
//        Log.e(getClass().getSimpleName(), "onTouchEvent = " + event.toString());

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                return true;
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_POINTER_UP:
                if (event.getPointerCount() == 2) {
                    Log.e(getClass().getSimpleName(), "onTouchEvent = " + event.toString());
                    lastY = getCurrentY(event);
                    lastX = getCurrentX(event);
                    lastDistance = get2PointDistance(event);
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() > 1) {
                    on2PointDraged(event);
                }
                return true;
        }
        return super.onTouchEvent(event);
    }

    private double lastDistance;
    private float lastX, lastY;

    private float getCurrentX(MotionEvent event) {
        float x1 = event.getX(0);
        float x2 = event.getX(1);
        return (x1 + x2) / 2;
    }

    private float getCurrentY(MotionEvent event) {
        float y1 = event.getY(0);
        float y2 = event.getY(1);
        return (y1 + y2) / 2;
    }

    private double get2PointDistance(MotionEvent event) {
        float x1 = event.getX(0);
        float y1 = event.getY(0);
        float x2 = event.getX(1);
        float y2 = event.getY(1);

        float x = (x1 + x2) / 2;
        float y = (y1 + y2) / 2;

        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    private void on2PointDraged(MotionEvent event) {
        float x1 = event.getX();
        float y1 = event.getY();
        float x2 = event.getX(1);
        float y2 = event.getY(1);

        float x = (x1 + x2) / 2;
        float y = (y1 + y2) / 2;


        double distance = get2PointDistance(event);
        float scale = (float) (distance / lastDistance * getScaleX());

        if (scale < 1)
            scale = 1;
        else if (scale > 4)
            scale = 4;

        if (getScrollX() != scale) {
            setScaleX(scale);
            setScaleY(scale);
        }

//        float aX = x - lastX;
//        if (aX > 0) {
//            if (getScrollX() - aX < 0)
//                aX = getScrollX();
//        } else if (aX < 0) {
//            if (getScrollX() - aX > getWidth() * scale - getWidth())
//                aX = getWidth() * scale - getWidth();
//        }


//        scrollTo((int) aX, 0);
//        scrollBy(Math.round(lastX - x), Math.round(lastY - x));
        onScroll(x, y);

        lastX = x;
        lastY = y;
        lastDistance = distance;
    }

    private void onScroll(float x, float y) {
        float moveX = x - lastX;
        float moveY = y - lastY;
        float width = getWidth() * getScaleX();
        float height = getWidth() * getScrollY();

        int toX = (int) Math.min(Math.max(0, getScrollX() - moveX), width - getWidth());
        int toY = (int) Math.min(Math.max(0, getScrollY() - moveY), height - getHeight());

        if (toX != getScrollX() || toY != getScrollY()) {
            scrollTo(toX, toY);
            postInvalidate();
        }
    }
}

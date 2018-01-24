package com.changhong.wifiairscout.contral;

import android.graphics.Point;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fuheng on 2017/12/15.
 */

public class TouchContral {

    private float lastX = -1, lastY = -1;
    private double lastDistance;

    private OnTouchContralListener listener;

    public boolean onTouch(MotionEvent event) {
        int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                lastX = event.getX();
                lastY = event.getY();
                if (listener != null)
                    listener.onActionDown(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                float delta = lastY - event.getY();
                lastX = event.getX();
                lastY = event.getY();
                if (listener != null) {
                    List<PointF> pointFS = new ArrayList<>();

                    if (event.getPointerCount() == 1)
                        pointFS.add(new PointF(event.getX(), event.getY()));
                    else {
                        for (int i = 0; i < event.getPointerCount(); i++)
                            pointFS.add(new PointF(event.getX(i), event.getY(i)));

                        double newDistance = get2PointDistance(event);
                        listener.onScale(lastDistance, newDistance);
                        lastDistance = newDistance;
                    }
                    listener.onMove(pointFS);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (listener != null)
                    listener.onActionUp(event.getX(), event.getY());
                return false;
            case MotionEvent.ACTION_POINTER_UP:
                if (event.getPointerCount() == 1) {
                    lastX = (int) event.getX();
                    lastY = (int) event.getY();
                }
            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getPointerCount() == 2) {
                    lastDistance = get2PointDistance(event);
                }
                break;
        }

        return true;
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

    public void setListener(OnTouchContralListener listener) {
        this.listener = listener;
    }

    public interface OnTouchContralListener {
        void onScale(double lastDistance, double newDistance);

        void onActionDown(float x, float y);

        void onActionUp(float x, float y);

        void onMove(List<PointF> points);
    }
}

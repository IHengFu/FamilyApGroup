package com.changhong.wifiairscout.ui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

/**
 * Created by fuheng on 2017/12/20.
 */

public class DragViewPager<T> extends ViewPager implements View.OnLongClickListener{
    /**
     * 是否正在拖拽
     */
    private boolean mIsDragging;

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWindowParams;
    private View mDragView;
    private Bitmap mDragBitmap;

    private OnDragDropListener mDropListener;
    private T mInfo;

    public DragViewPager(Context context) {
        super(context);
        init();
    }

    public DragViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        switch (ev.getAction() & MotionEvent.ACTION_MASK) {
//            case MotionEvent.ACTION_DOWN:
//
//                return true;
//            case MotionEvent.ACTION_UP:
//                if (mIsDragging)
//                    return true;
//                break;
//        }
        if (mIsDragging)
            return true;

        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!mIsDragging) {
            return super.onTouchEvent(ev);
        } else {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_UP:
                    stopDragging();
                    mIsDragging = false;
                    break;
                case MotionEvent.ACTION_MOVE:
                    dragView((int) ev.getX(), (int) ev.getY());
                    break;
            }
        }
        return mIsDragging;
    }

    private void dragView(int x, int y) {
        if (mDragView.getVisibility() == View.GONE) {
            mDragView.setVisibility(View.VISIBLE);
        }

        int[] location = new int[2];
        this.getLocationOnScreen(location);//获取在整个屏幕内的绝对坐标
        mWindowParams.x = x + location[0] - mDragView.getWidth() / 2;
        mWindowParams.y = y + location[1] - mDragView.getHeight() / 2;
        mWindowManager.updateViewLayout(mDragView, mWindowParams);

    }

    private void stopDragging() {

        if (mDragView != null) {
            if (mDropListener != null) {
                mDropListener.onDrop(mInfo,
                        Math.round(mWindowParams.x + mDragView.getWidth() / 2), Math.round(mWindowParams.y + mDragView.getHeight() / 2));
            }
            mDragView.setVisibility(GONE);
            WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            wm.removeView(mDragView);
            mDragView = null;
        }

        if (mDragBitmap != null) {
            mDragBitmap.recycle();
            mDragBitmap = null;
        }

    }

    private void init() {
        mWindowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
    }

    private void startDragging() {
        if (mDragView != null) {
            Context context = getContext();


            ViewGroup item = (ViewGroup) mDragView;

            Drawable bgdrawable = item.getBackground();

            item.setBackground(null);
            item.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(item.getDrawingCache());

            item.setBackground(bgdrawable);

            mWindowParams = new WindowManager.LayoutParams();
            mWindowParams.gravity = Gravity.TOP | Gravity.LEFT;

//            item.getLocationInWindow(location); //获取在当前窗口内的绝对坐标
            int[] location = new int[2];
            item.getLocationOnScreen(location);//获取在整个屏幕内的绝对坐标
            System.out.println("view--->x坐标:" + location[0] + "view--->y坐标:" + location[1]);
            System.out.println("item--->x坐标:" + item.getLeft() + "view--->y坐标:" + item.getTop());
            mWindowParams.x = location[0] - item.getWidth() / 2;
            mWindowParams.y = location[1] - item.getHeight() / 2;

            mWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            mWindowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;

            mWindowParams.format = PixelFormat.TRANSLUCENT;
            mWindowParams.windowAnimations = 0;


            AppCompatImageView v = new AppCompatImageView(context);
            v.setPadding(0, 0, 0, 0);
            v.setImageBitmap(bitmap);
            mDragBitmap = bitmap;
            mWindowManager.addView(v, mWindowParams);
            mDragView = v;
            mDragView.setVisibility(View.GONE);
        }
    }

    public void onLongPress(View view, T t) {
        System.err.println("onLongPress");

        mIsDragging = true;
        mDragView = view;

        mInfo = t;
        startDragging();
        if (mDropListener != null)
            mDropListener.onStartDrag();
    }

    public void setDropListener(OnDragDropListener listener) {
        mDropListener = listener;
    }

    @Override
    public boolean onLongClick(View view) {
        return false;
    }

    public interface OnDragDropListener<T> {
        void onDrop(T t, int x, int y);

        void onStartDrag();
    }
}

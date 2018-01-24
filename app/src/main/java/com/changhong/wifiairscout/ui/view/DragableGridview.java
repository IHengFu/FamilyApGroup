package com.changhong.wifiairscout.ui.view;

/**
 * Created by fuheng on 2017/12/20.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.changhong.wifiairscout.R;

/**
 * @author guojie
 */
public class DragableGridview extends GridView implements GestureDetector.OnGestureListener {

    //private static final String			TAG			= "MyGridView";

    private int lastX, lastY, newX, newY;
    private ImageView mDragView;
    private TextView mDeleteTextView;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWindowParams;
    private WindowManager.LayoutParams mDeleteLayoutParams;
    private int mDragPointX;                    // at what x offset inside the item did the user
    private int mXOffset;                        // the difference between screen coordinates and
    private int mYOffset;                        // the difference between screen coordinates and
    private Bitmap mDragBitmap;
    private Drawable mTrashcan;
    int dragedItemIndex;
    int dropedItemIndex;
    private OnSwappingListener onSwappingListener;
    private OnItemClickListener onItemClickListener;
    private OnItemDeleteListener onItemDeleteListener;

    boolean mIsDragging = false;
    boolean mIsScrolling = false;
    int scroll = 0, maxScroll = 0;

    private GestureDetector mGesture;
    // anim vars
    public static int animT = 150;


    /**
     * 通过控制是否启用来确定是否传递child的child的点击事件
     */
    private boolean enable = true;

    private Handler handler = new Handler();

    private SmoothScrollRunnable smoothScrollRunnable;

    @SuppressWarnings("unused")
    private int mSelectedItemBgColor;
    private final static int DELETE_PANEL_HEIGHT = 74;
    private boolean isCanDeleteItem;
    private Rect mDeleteRect;

    public DragableGridview(Context context, AttributeSet attrs) {
        super(context, attrs);
        mGesture = new GestureDetector(getContext(), this);
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return enable;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return mGesture.onTouchEvent(ev);

            case MotionEvent.ACTION_MOVE:
                if (mIsDragging) {
                    newX = (int) ev.getX();
                    newY = (int) ev.getY();

                    if (mDragView != null) {
                        dragView(newX, newY);
                        dropedItemIndex = pointToPosition(newX, newY + scroll);
                    }

                    return true;
                }
                return mGesture.onTouchEvent(ev);

            case MotionEvent.ACTION_UP:

                if (mIsDragging) {
                    stopDragging();
                    mIsDragging = false;
                    return true;
                } else {
                    mGesture.onTouchEvent(ev);
                }

                /** reset scroll value */
                if (mIsScrolling) {

                    if (scroll < 0) {
                        if (null != smoothScrollRunnable) {
                            smoothScrollRunnable.stop();
                        }

                        smoothScrollRunnable = new SmoothScrollRunnable(getHandler(), scroll, 0);
                        handler.post(smoothScrollRunnable);
                        scroll = 0;
                    }

                    if (scroll > maxScroll) {
                        if (null != smoothScrollRunnable) {
                            smoothScrollRunnable.stop();
                        }
                        smoothScrollRunnable = new SmoothScrollRunnable(getHandler(), scroll, maxScroll);
                        handler.post(smoothScrollRunnable);
                        scroll = maxScroll;
                    }

                    mIsScrolling = false;
                }

//			this.invalidate();
        }
        return mIsDragging;
    }

    private void startDragging() {
        dragedItemIndex = pointToPosition(lastX, lastY + scroll);

        if (dragedItemIndex != -1) {
            Context context = getContext();
            mDeleteTextView = new TextView(context);
            mDeleteTextView.setPadding(4, 4, 4, 4);
            mDeleteTextView.setBackgroundColor(0x8800aa00);
            mDeleteTextView.setTextColor(Color.BLACK);
            mDeleteTextView.setTextSize(20);
            mDeleteTextView.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CLIP_VERTICAL);

            mDeleteLayoutParams = new WindowManager.LayoutParams();
            mDeleteTextView.setText(R.string.action_delete);
            mDeleteLayoutParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            mDeleteLayoutParams.x = 0;
            mDeleteLayoutParams.y = //TODO BAR HEIGHT //cmucApplication.getStatusBarHeight();
                    mDeleteLayoutParams.height = DELETE_PANEL_HEIGHT;
            mDeleteLayoutParams.width = WindowManager.LayoutParams.FILL_PARENT;

            if (mDeleteRect == null)
                mDeleteRect = new Rect(mDeleteLayoutParams.x, mDeleteLayoutParams.x, mDeleteLayoutParams.x + getWidth(), mDeleteLayoutParams.y + mDeleteLayoutParams.height);
            mDeleteLayoutParams.format = PixelFormat.TRANSLUCENT;
            mDeleteLayoutParams.windowAnimations = R.style.mydialog;
            mWindowManager.addView(mDeleteTextView, mDeleteLayoutParams);
            isCanDeleteItem = false;

            ViewGroup item = (ViewGroup) getChildAt(dragedItemIndex);
            mDragPointX = lastX - item.getLeft();
//			mDragPointY = lastY - item.getTop();

            item.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(item.getDrawingCache());

            mWindowParams = new WindowManager.LayoutParams();
            mWindowParams.gravity = Gravity.TOP | Gravity.LEFT;
            mWindowParams.x = mXOffset - lastX + item.getLeft();
            mWindowParams.y = mYOffset - lastY + item.getTop();

            mWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            mWindowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;

            mWindowParams.format = PixelFormat.TRANSLUCENT;
            mWindowParams.windowAnimations = 0;


            ImageView v = new ImageView(context);
            v.setPadding(0, 0, 0, 0);
            v.setImageBitmap(bitmap);
            mDragBitmap = bitmap;
            mWindowManager.addView(v, mWindowParams);
            mDragView = v;
            mDragView.setVisibility(View.GONE);
        }
    }

    private void dragView(int x, int y) {
        if (mDragView.getVisibility() == View.GONE) {
            mDragView.setVisibility(View.VISIBLE);
        }
        mWindowParams.x = x - mDragPointX;
        mWindowParams.y = y;
        mWindowManager.updateViewLayout(mDragView, mWindowParams);
        Rect dragRect = new Rect(mWindowParams.x, mWindowParams.y, mWindowParams.x + mDragView.getWidth(), mWindowParams.y + mDragView.getHeight());
        if (isCollsionWithRect(dragRect, mDeleteRect)) {
            isCanDeleteItem = true;
            mDeleteTextView.setBackgroundColor(0xaaff0000);
        } else {
            isCanDeleteItem = false;
            mDeleteTextView.setBackgroundColor(0x8800aa00);
        }

    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int expandSpec = MeasureSpec.makeMeasureSpec(
                Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }

    public boolean isCollsionWithRect(Rect rect1, Rect rect2) {
        // 当矩形1位于矩形2的左侧
        if (rect1.left >= rect2.left && rect1.left >= rect2.right) {
            return false; // 当矩形1位于矩形2的右侧
        } else if (rect1.right <= rect2.left && rect1.right <= rect2.right) {
            return false; // 当矩形1位于矩形2的下方
        } else if (rect1.top >= rect2.top && rect1.top >= rect2.bottom) {
            return false;
        } else if (rect1.bottom <= rect2.top && rect1.bottom <= rect2.top) {
            return false;
        }
        // 所有不会发生碰撞都不满足时，肯定就是碰撞了
        return true;
    }

    private void stopDragging() {

        if (mDragView != null) {
            mDragView.setVisibility(GONE);
            mDeleteTextView.setVisibility(View.GONE);
            WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            wm.removeView(mDragView);
            wm.removeView(mDeleteTextView);
            mDragView.setImageDrawable(null);
            mDragView = null;
            if (isCanDeleteItem && dragedItemIndex != -1) {
                if (onItemDeleteListener != null) {
                    onItemDeleteListener.delete(dragedItemIndex);
                    isCanDeleteItem = false;
                }
            }
        }

        if (mDragBitmap != null) {
            mDragBitmap.recycle();
            mDragBitmap = null;
        }

        if (mTrashcan != null) {
            mTrashcan.setLevel(0);
        }

        if (dragedItemIndex != -1 && dropedItemIndex != -1 && dragedItemIndex != dropedItemIndex) {
            if (onSwappingListener != null)
                onSwappingListener.waspping(dragedItemIndex, dropedItemIndex);

            dragedItemIndex = -1;
            dropedItemIndex = -1;
        }
    }

    public void setOnSwappingListener(OnSwappingListener l) {
        this.onSwappingListener = l;
    }

    public void setOnItemClick(OnItemClickListener l) {
        this.onItemClickListener = l;
    }

    @Override
    public boolean onDown(MotionEvent ev) {
        maxScroll = this.getHeight() - ((View) this.getParent()).getHeight() + 200;

        if (maxScroll <= 0) {
            maxScroll = 0;
        }
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent ev) {

        this.invalidate();

        mIsDragging = true;
        lastX = (int) ev.getX();
        lastY = (int) ev.getY();

        mXOffset = (int) ev.getRawX();
        mYOffset = (int) ev.getRawY();

        View v = getChildAt(pointToPosition((int) ev.getX(), (int) ev.getY() + scroll));
        showSelectItem(v, false);

        startDragging();
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

        mIsScrolling = true;

        if (scroll < 0 || scroll > maxScroll) {
            scrollBy(0, (int) distanceY / 3);
            scroll += distanceY / 3;
        } else {
            scroll += distanceY;
            scrollBy(0, (int) distanceY);
        }
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

        this.invalidate();
        View v = getChildAt(pointToPosition((int) e.getX(), (int) e.getY() + scroll));
        showSelectItem(v, true);
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        if (!mIsDragging) {
            int index = pointToPosition((int) e.getX(), (int) e.getY() + scroll);
            if (index != -1)
                onItemClickListener.click(index);
        }
        return false;
    }

    final class SmoothScrollRunnable implements Runnable {

        static final int ANIMATION_DURATION_MS = 200;
        static final int ANIMATION_FPS = 1000 / 60;

        private final Interpolator interpolator;
        private final int scrollToY;
        private final int scrollFromY;
        private final Handler handler;

        private boolean continueRunning = true;
        private long startTime = -1;
        private int currentY = -1;

        public SmoothScrollRunnable(Handler handler, int fromY, int toY) {
            this.handler = handler;
            this.scrollFromY = fromY;
            this.scrollToY = toY;
            this.interpolator = new AccelerateDecelerateInterpolator();
        }

        @Override
        public void run() {

            /**
             * Only set startTime if this is the first time we're starting, else
             * actually calculate the Y delta
             */
            if (startTime == -1) {
                startTime = System.currentTimeMillis();
            } else {

                /**
                 * We do do all calculations in long to reduce software float
                 * calculations. We use 1000 as it gives us good accuracy and
                 * small rounding errors
                 */
                long normalizedTime = (1000 * (System.currentTimeMillis() - startTime)) / ANIMATION_DURATION_MS;
                normalizedTime = Math.max(Math.min(normalizedTime, 1000), 0);

                final int deltaY = Math.round((scrollFromY - scrollToY)
                        * interpolator.getInterpolation(normalizedTime / 1000f));
                this.currentY = scrollFromY - deltaY;
                // setHeaderScroll(currentY);
                scrollTo(0, currentY);
            }

            // If we're not at the target Y, keep going...
            if (continueRunning && scrollToY != currentY) {
                handler.postDelayed(this, ANIMATION_FPS);
            }
        }

        public void stop() {
            this.continueRunning = false;
            this.handler.removeCallbacks(this);
        }
    }

    ;

    private void showSelectItem(View item, boolean isShow) {
        if (item == null)
            return;
        if (isShow)
            item.setBackgroundColor(0xaa0000ff);
        else
            item.setBackgroundColor(0);
    }

    /**
     * 调换位置传
     *
     * @author guojie
     */
    public interface OnSwappingListener {
        public abstract void waspping(int oldIndex, int newIndex);
    }

    /**
     * 传出itemOnClick事件
     *
     * @author guojie
     */
    public interface OnItemClickListener {
        public abstract void click(int index);
    }

    public interface OnItemDeleteListener {
        public abstract void delete(int index);
    }

    /**
     * @param mSelectedItemBgColor the mSelectedItemBgColor to set
     */
    public void setmSelectedItemBgColor(int mSelectedItemBgColor) {
        this.mSelectedItemBgColor = mSelectedItemBgColor;
    }

    public void setOnItemDeleteListener(OnItemDeleteListener onItemDeleteListener) {
        this.onItemDeleteListener = onItemDeleteListener;
    }


}


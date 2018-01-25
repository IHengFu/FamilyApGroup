package com.changhong.wifiairscout.ui.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import com.changhong.wifiairscout.App;
import com.changhong.wifiairscout.R;
import com.changhong.wifiairscout.db.data.DeviceLocation;
import com.changhong.wifiairscout.model.WifiDevice;
import com.changhong.wifiairscout.preferences.Preferences;
import com.changhong.wifiairscout.ui.view.map.PointMap;
import com.changhong.wifiairscout.ui.view.map.TypeMap;
import com.changhong.wifiairscout.ui.view.map.WaveForceMap;
import com.changhong.wifiairscout.utils.CommUtils;
import com.changhong.wifiairscout.utils.UnitUtils;

/**
 * Created by fuheng on 2017/12/8.
 */
public class DragViewGroup extends ViewGroup implements View.OnTouchListener,
        View.OnClickListener, View.OnLongClickListener {
    private final int RADIUS_ROUND_RECT;
    // layout vars
    private final int SIZE_CHILD;
    private final int SIZE_CHILD_TEXT;

    // dragging vars
    /**
     * 拖拽对象的序号
     */
    private int dragged = -1;
    /**
     * 上次预留的坐标
     */
    private int lastX = -1, lastY = -1;
    private double lastDistance;
    protected boolean enabled = true, touching = false;

    // anim vars
    public static final int DURATION_ANIM = 150;

    // listeners
    protected OnDragListener onDragListener;
    protected OnClickListener secondaryOnClickListener;
    private AdapterView.OnItemClickListener onItemClickListener;

    private Rect mTextBoundsRect = new Rect();

    /**
     * 缩放相关
     */
    private float mScale = 1f;
    /**
     * 拖拽相关
     */
    private int mScrollX, mScrollY;

    private Paint mPathDropEnable = null;
    private boolean mIsDraggedRectShowing;


    /**
     * 设备信息
     */
    private List<DeviceLocation> mListDeviceInfo;

    /**
     * 删除图标
     */
    private AppCompatImageView mDeleteTextView;
    private WindowManager mWindowManager;
    private Rect mDeleteRect;

    private boolean isPointState;

    /**
     * 绘制种类
     */
    private TypeMap mTypeMap;


    // CONSTRUCTOR AND HELPERS
    public DragViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);

        setListeners();

        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        setChildrenDrawingOrderEnabled(true);

        SIZE_CHILD = UnitUtils.dip2px(getContext(), 30);
        SIZE_CHILD_TEXT = UnitUtils.dip2px(getContext(), 12);
        RADIUS_ROUND_RECT = UnitUtils.dip2px(getContext(), 3);

        initPaint();

        //设置当前图样式
        setPointState(Preferences.getIntance().getMapShowStyle() == 0);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {

        try {

            if (mTypeMap != null)
                mTypeMap.drawContent(canvas);

            super.dispatchDraw(canvas);

            if (mTypeMap != null)
                mTypeMap.drawSketchMap(canvas);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    protected void setListeners() {
        setOnTouchListener(this);
        super.setOnClickListener(this);
        setOnLongClickListener(this);
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        secondaryOnClickListener = l;
    }

    public void addDevice(WifiDevice device, int absX, int absY) {
        if (device == null) {
            throw new RuntimeException("WifiDevice对象不可为空");
        }

        if (isContainDevice(device)) {
            Toast.makeText(getContext(), "设备" + device.getName() + "不能重复添加", Toast.LENGTH_SHORT).show();
            return;
        }

        //判读是否进入此组件内
        int[] outLocation = new int[2];
        getLocationOnScreen(outLocation);
        if (absX < outLocation[0] || absX > outLocation[0] + getWidth()
                || absY < outLocation[1] || absY > outLocation[1] + getHeight()) {
            return;
        }

        DeviceLocation location = addDeviceLocation(device,
                absX - outLocation[0] + mScrollX,
                absY - outLocation[1] + mScrollY);
        View child = createDisplayView(location, mScale, mScrollX, mScrollY);
        child.setTag(device);

        super.addView(child);
        if (mTypeMap != null) {
            mTypeMap.onAdd(this, child, location);
        }

        refresh();

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
//        Log.e(getClass().getSimpleName(), String.format("onLayout(%d,%d,%d,%d)", l, t, r, b));

        if (mTypeMap != null)
            mTypeMap.onLayout(changed, l, t, r, b, mScale, mScrollX, mScrollY);

        //布局设备 child 只位移不缩放
        for (int i = 0; i < getChildCount(); i++) {
            DeviceLocation d = mListDeviceInfo.get(i);
            float x = d.getX() * mScale - mScrollX;
            float y = d.getY() * mScale - mScrollY;
            if (i != dragged) {
                setChildLocation(i, x, y);
            }
        }
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        if (dragged == -1)
            return i;
        else if (i == childCount - 1)
            return dragged;
        else if (i >= dragged)
            return i + 1;
        return i;
    }

    private int getIndexFromCoor(int x, int y) {
        for (int i = getChildCount() - 1; i >= 0; i--) {
            View v = getChildAt(i);
            if (x < v.getLeft() || x > v.getRight() || y < v.getTop() || y > v.getBottom())
                continue;
            return i;
        }
        return -1;
    }

    // EVENT HANDLERS
    public void onClick(View view) {
        if (enabled) {
            if (secondaryOnClickListener != null)
                secondaryOnClickListener.onClick(view);
            if (onItemClickListener != null && getLastIndex() != -1)
                onItemClickListener.onItemClick(null,
                        getChildAt(getLastIndex()), getLastIndex(),
                        getLastIndex());
        }
    }

    public boolean onLongClick(View view) {
        if (!enabled)
            return false;
        int index = getLastIndex();
        if (index != -1) {
            dragged = index;
            setDraggedRectShow(true);
            animateDragged();
            showDeleteView();
            return true;
        }
        return false;
    }

    private void setChildLocation(int index, float x, float y) {
        View v = getChildAt(index);
        v.layout(Math.round(x - v.getWidth() / 2),
                Math.round(y - v.getHeight() / 2),
                Math.round(x + v.getWidth() / 2),
                Math.round(y + v.getHeight() / 2));
    }

    public boolean onTouch(View view, MotionEvent event) {
        int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                enabled = true;
                lastX = (int) event.getX();
                lastY = (int) event.getY();
                touching = true;
                break;
            case MotionEvent.ACTION_MOVE:
                if (dragged != -1) {
                    dragView(event.getX(), event.getY());
                } else {
                    if (event.getPointerCount() == 1) {
                        onScroll(event.getX(), event.getY());
                    } else {
                        onScale(event);
                    }
                }
                lastX = (int) event.getX();
                lastY = (int) event.getY();
                break;
            case MotionEvent.ACTION_UP:
                if (dragged != -1) {
                    View v = getChildAt(dragged);
                    v.clearAnimation();

                    float x = event.getX(), y = event.getY();
                    float v_width = getChildWidth(v);
                    float v_height = getChildHeight(v);

                    x = Math.max(x, -mScrollX + v_width / 2);
                    x = Math.min(x, getWidth() - mScrollX - v_width / 2);
                    y = Math.max(y, -mScrollY + v_height / 2);
                    y = Math.min(y, getHeight() - mScrollY - v_height / 2);

                    setChildLocation(dragged, x, y);
                    //将移动了的view 的值存入对象
                    mListDeviceInfo.get(dragged).setXY((x + mScrollX) / mScale, (y + mScrollY) / mScale);
                    hideDeleteView();
                    //重新定场强的样式
                    refresh();
                    dragged = -1;
                    setDraggedRectShow(false);
                }
                touching = false;
                break;
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
        if (dragged != -1)
            return true;
        else
            return false;
    }

    private void dragView(float x, float y) {

        //判断是否进入删除区域
        onDragedInDeleteView((int) x, (int) y);

        View v = getChildAt(dragged);

        float v_width = getChildWidth(v);
        float v_height = getChildHeight(v);

        x = Math.max(x, -mScrollX + v_width / 2);
        x = Math.min(x, getWidth() - mScrollX - v_width / 2);
        y = Math.max(y, -mScrollY + v_height / 2);
        y = Math.min(y, getHeight() - mScrollY - v_height / 2);

        setChildLocation(dragged, x, y);
    }

    // EVENT HELPERS
    protected void animateDragged() {
        View v = getChildAt(dragged);
        float x = lastX;
        float y = lastY;
        float v_width = v.getWidth();
        float v_height = v.getHeight();
        v.layout(Math.round(x - v_width / 2), Math.round(y - v_height / 2), Math.round(x + v_width / 2), Math.round(y + v_height / 2));
        AnimationSet animSet = new AnimationSet(true);
//        ScaleAnimation scale = new ScaleAnimation(1, 1.5f, 1, 1.5f,
//                0.5f, 0.5f);
//        scale.setDuration(DURATION_ANIM);
        AlphaAnimation alpha = new AlphaAnimation(1, .5f);
        alpha.setDuration(DURATION_ANIM);

//        animSet.addAnimation(scale);
        animSet.addAnimation(alpha);
        animSet.setFillEnabled(true);
        animSet.setFillAfter(true);

        v.clearAnimation();
        v.startAnimation(animSet);
    }

    private void onScroll(float x, float y) {
        float moveX = x - lastX;
        float moveY = y - lastY;

        mScrollX = (int) (mScrollX - moveX);
        mScrollY = (int) (mScrollY - moveY);

        contralScrollAndScale();
        requestLayout();
    }

    public int getLastIndex() {
        return getIndexFromCoor(lastX, lastY);
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener l) {
        this.onItemClickListener = l;
    }

    private void contralScrollAndScale() {
        int width_bg = (int) (getWidth() * mScale);
        int height_bg = (int) (getHeight() * mScale);

        if (width_bg >= getWidth()) {
            if (mScrollX < 0)
                mScrollX = 0;
            else if (mScrollX + getWidth() > width_bg)
                mScrollX = width_bg - getWidth();
        } else {
            if (mScrollX > 0)
                mScrollX = 0;
            else if (getWidth() + mScrollX - width_bg < 0)
                mScrollX = width_bg - getWidth();
        }

        if (height_bg >= getHeight()) {
            if (mScrollY < 0)
                mScrollY = 0;
            else if (mScrollY + getHeight() > height_bg)
                mScrollY = height_bg - getHeight();
        } else {
            if (mScrollY > 0)
                mScrollY = 0;
            else if (getHeight() + mScrollY - height_bg < 0)
                mScrollY = height_bg - getHeight();
        }

        if (mScale < .9F)
            mScale = .9F;
        else if (mScale > 2)
            mScale = 2;
    }

    private double get2PointDistance(MotionEvent event) {
        float x1 = event.getX(0);
        float y1 = event.getY(0);
        float x2 = event.getX(1);
        float y2 = event.getY(1);

        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    private void onScale(MotionEvent event) {
        double distance = get2PointDistance(event);
        float scale = (float) (distance / lastDistance * mScale);

        mScrollX += (event.getX() + mScrollX) * (scale / mScale - 1);
        mScrollY += (event.getY() - mScrollY) * (scale / mScale - 1);

        if (mScale != scale) {
            mScale = scale;
            lastDistance = distance;
        }

        contralScrollAndScale();

        requestLayout();
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {

        super.onScrollChanged(l, t, oldl, oldt);
    }

    private boolean isContainDevice(WifiDevice device) {
        if (mListDeviceInfo == null || device == null)
            return false;

        for (DeviceLocation deviceLocation : mListDeviceInfo) {
            if (device.equals(deviceLocation.getWifiDevice()))
                return true;
        }

        return false;
    }


    private DeviceLocation addDeviceLocation(WifiDevice device, int x, int y) {
        if (device == null || device.getMac() == null) {
            return null;
        }
        if (mListDeviceInfo == null)
            mListDeviceInfo = new ArrayList<>();

        DeviceLocation location = new DeviceLocation(mListDeviceInfo.size(),
                x, y, device.getMac(), device.getType(), 0);
        location.setWifiDevice(device);
        mListDeviceInfo.add(location);
        return location;
    }

    /**
     * 刷新界面，这里中转一下，再刷新view
     */
    public void refresh() {
        if (getChildCount() > 0)
            for (int i = 0; i < getChildCount(); ++i) {
                TextView view = (TextView) getChildAt(i);
                DeviceLocation d = mListDeviceInfo.get(i);
                Drawable drawable = view.getCompoundDrawables()[1];
                view.setCompoundDrawables(null, drawable, null, null);
                view.setTag(d);
                if (d.getWifiDevice() == null) {
                    view.setEnabled(false);
                    DrawableCompat.setTint(drawable, Color.GRAY);
                } else {
                    DrawableCompat.setTint(drawable, Color.YELLOW);
                }
                view.setText(d.getDisplayName());
            }
        if (mTypeMap != null) mTypeMap.refresh(this, mListDeviceInfo, mScale, mScrollX, mScrollY);
    }

    private void initPaint() {
        mPathDropEnable = new Paint();
        mPathDropEnable.setStyle(Paint.Style.STROKE);
        mPathDropEnable.setColor(getResources().getColor(R.color.colorPrimary));
        mPathDropEnable.setStrokeWidth(UnitUtils.dip2px(getContext(), 2));
        mPathDropEnable.setAntiAlias(true);
        //DashPathEffect是Android提供的虚线样式API，具体的使用可以参考下面的介绍
        mPathDropEnable.setPathEffect(new DashPathEffect(new float[]{2, 3}, .5f));
    }

    private void showDeleteView() {
        if (dragged != -1) {
            if (mDeleteTextView == null) {
                Context context = getContext();
                mDeleteTextView = new AppCompatImageView(context);
                mDeleteTextView.setPadding(4, 4, 4, 4);
                mDeleteTextView.setBackgroundResource(R.drawable.round_rect_delete);
                mDeleteTextView.setImageResource(R.drawable.animatied_vector_delete);
            }
            Animatable drawable = (Animatable) mDeleteTextView.getDrawable();
            drawable.start();
            mDeleteTextView.setEnabled(true);

            WindowManager.LayoutParams mDeleteLayoutParams = new WindowManager.LayoutParams();
            mDeleteLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
            int[] location = new int[2];
            getLocationInWindow(location);

            mDeleteLayoutParams.height = UnitUtils.dip2px(getContext(), 48);
            mDeleteLayoutParams.width = mDeleteLayoutParams.height;
            mDeleteLayoutParams.x = location[0] + getWidth() / 2 - mDeleteLayoutParams.width / 2;
            mDeleteLayoutParams.y = location[1] - CommUtils.getStatusBarHeight((Activity) getContext()) - mDeleteLayoutParams.height;

            mDeleteTextView.setVisibility(View.VISIBLE);

            if (mDeleteRect == null)
                mDeleteRect = new Rect();
            mDeleteRect.set((getWidth() - mDeleteLayoutParams.width) / 2, -mDeleteLayoutParams.height,
                    (getWidth() + mDeleteLayoutParams.width) / 2, 0);
            mDeleteLayoutParams.format = PixelFormat.TRANSLUCENT;
//            mDeleteLayoutParams.windowAnimations = R.style.mydialog;
            mWindowManager.addView(mDeleteTextView, mDeleteLayoutParams);
        }
    }

    private boolean isInDeleteState(int x, int y) {

        int left = x - SIZE_CHILD / 2;
        if (left > mDeleteRect.right)
            return false;

        int right = left + SIZE_CHILD;
        if (right < mDeleteRect.left)
            return false;

        int top = y - SIZE_CHILD / 2;
        if (top > mDeleteRect.bottom)
            return false;

        int bottom = top + SIZE_CHILD;
        if (bottom < mDeleteRect.top)
            return false;


//        return mDeleteRect.contains(x, y);
        return true;
    }

    /**
     * 判断是否删除
     */
    private void onDragedInDeleteView(int x, int y) {
        if (mDeleteTextView != null) {
            boolean isEnable = !isInDeleteState(x, y);
            if (mDeleteTextView.isEnabled() == isEnable) {
                return;
            }
            mDeleteTextView.setEnabled(isEnable);

//            Animatable drawable = (Animatable) mDeleteTextView.getDrawable();
//            if (!isEnable) {
//
//                if (!drawable.isRunning()) {
//                    drawable.start();
//                }
//            } else {
//                drawable.stop();
//            }

        }
    }

    private void hideDeleteView() {
        if (dragged != -1 && mDeleteTextView != null) {
            if (isInDeleteState(lastX, lastY)) {

                View view = getChildAt(dragged);
                DeviceLocation obj = mListDeviceInfo.remove(dragged);
                removeViewAt(dragged);
                if (mTypeMap != null) {
                    mTypeMap.onRemove(view, obj);
                }
            }
            mDeleteTextView.setEnabled(true);
            mDeleteTextView.setVisibility(View.GONE);
            mWindowManager.removeView(mDeleteTextView);
        }
    }

    public void setDraggedRectShow(boolean isShowing) {
        mIsDraggedRectShowing = isShowing;
    }

    private void reset() {
        //清空过去的数据，还原
        mScale = 1;
        mScrollX = 0;
        mScrollY = 0;

        removeAllViews();

        if (mTypeMap != null)
            mTypeMap.clean(this);

        if (mListDeviceInfo != null)
            mListDeviceInfo.clear();

        requestLayout();
    }

    public void importData(List<DeviceLocation> array) {
        reset();
        mListDeviceInfo = array;
        //判读是否进入此组件内
        for (DeviceLocation location : array) {
            Log.e(getClass().getSimpleName(), "==~" + location.toString());
            View child = createDisplayView(location, mScale, mScrollX, mScrollY);
            child.setTag(location);

            super.addView(child);
            if (mTypeMap != null) {
                mTypeMap.onAdd(this, child, location);
            }
        }
        refresh();
    }

    public List<DeviceLocation> exportData() {
        return mListDeviceInfo;
    }

    public void setPointState(boolean pointState) {

        if (mTypeMap != null) {
            mTypeMap.clean(this);
        }


        if (pointState) {
            mTypeMap = new PointMap(this.getContext());
        } else {
            mTypeMap = new WaveForceMap(this.getContext(), SIZE_CHILD_TEXT, RADIUS_ROUND_RECT);
        }

        refresh();

        isPointState = pointState;
        postInvalidate();
    }

    public View createDisplayView(DeviceLocation d, float scale, int scrollX, int scrollY) {
        TextView child = new TextView(getContext());

        child.setGravity(Gravity.CENTER);
        child.setMaxLines(1);
        child.setEllipsize(TextUtils.TruncateAt.END);
        child.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9);
        if (d.getWifiDevice() == null) {
            child.setEnabled(false);
        }
        child.setText(d.getDisplayName());
        setDisplayViewByState(child, d, scale, scrollX, scrollY);
        return child;
    }

    private void setDisplayViewByState(TextView view, DeviceLocation d, float scale, int scrollX, int scrollY) {
        String title = view.getText().toString();
        view.getPaint().getTextBounds(title, 0, title.length(), mTextBoundsRect);
        float x = d.getX() * scale - scrollX;
        float y = d.getY() * scale - scrollY;


        view.setBackgroundColor(Color.TRANSPARENT);
        Drawable drawable = getResources().getDrawable(App.RESID_WIFI_DEVICE[d.getType()]).mutate();
        drawable.setBounds(0, 0,
                (SIZE_CHILD * drawable.getMinimumWidth() / drawable.getMinimumHeight()), SIZE_CHILD);
        view.setGravity(Gravity.CENTER);
        drawable = DrawableCompat.wrap(drawable);
        view.setCompoundDrawables(null, drawable, null, null);
        view.setBackgroundResource(R.drawable.animatied_vector_oval);
        view.setCompoundDrawablePadding(0);

        if (d.getWifiDevice() == null) {
            DrawableCompat.setTint(drawable, Color.DKGRAY);
        }
        view.setPadding(SIZE_CHILD, SIZE_CHILD, SIZE_CHILD, SIZE_CHILD);
        float width = Math.max(drawable.getBounds().width(), mTextBoundsRect.width());
        float height = drawable.getBounds().height() + mTextBoundsRect.height() + view.getCompoundDrawablePadding() + SIZE_CHILD_TEXT;
        width = height = Math.max(width, height) + 2 * SIZE_CHILD;
        view.layout((int) (x - width / 2), (int) (y - height / 2),
                (int) (x + width / 2), (int) (y + height / 2));
    }

    private int getChildWidth(View child) {
        return child.getWidth() - SIZE_CHILD * 2;
    }

    private int getChildHeight(View child) {
        return child.getHeight() - SIZE_CHILD * 2;
    }
}
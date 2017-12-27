package wifiairscout.changhong.com.wifiairscout.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import wifiairscout.changhong.com.wifiairscout.App;
import wifiairscout.changhong.com.wifiairscout.R;
import wifiairscout.changhong.com.wifiairscout.contral.TouchContral;
import wifiairscout.changhong.com.wifiairscout.model.DeviceLocation;
import wifiairscout.changhong.com.wifiairscout.model.WifiDevice;
import wifiairscout.changhong.com.wifiairscout.utils.UnitUtils;

/**
 * Created by fuheng on 2017/12/8.
 */

public class DragViewGroup extends ViewGroup implements View.OnTouchListener,
        View.OnClickListener, View.OnLongClickListener {
    // layout vars
    private float childSize = 0;
    private int padding, scroll = 0;
    protected float lastDelta = 0;
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
    public static int animT = 150;


    // listeners
    protected OnDragListener onDragListener;
    protected OnClickListener secondaryOnClickListener;
    private AdapterView.OnItemClickListener onItemClickListener;

    private static final int STATE_NORMAL = 0;
    private static final int STATE_DRAG = 1;
    private static final int STATE_MOVE_CONTNT = 2;
    private static final int STATE_MOVE_ZOOM = 3;
    private int mState;

    private Drawable mBgDrawable;
    /**
     * 设备信息
     */
    private List<DeviceLocation> mListDeviceInfo;
    private Path mPath80, mPath50, mPath30, mPath0;

    private Paint mPathPaint = null;
    private TouchContral touchContral;

    /**
     * 强弱示意图
     */
    private Drawable mSketchMapDrawable;
    /**
     * 强弱示意文字
     */
    private String[] mStrIntensity;
    /**
     * 删除图标
     */
    private AppCompatImageView mDeleteTextView;
    private WindowManager mWindowManager;
    private Rect mDeleteRect;
    private Rect mRectStrSkechMap;

    // CONSTRUCTOR AND HELPERS
    public DragViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        setListeners();

        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        setChildrenDrawingOrderEnabled(true);

        childSize = UnitUtils.dip2px(getContext(), 40);

        initPaint();
        initSketchMap();
    }


    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (mBgDrawable != null)
            mBgDrawable.draw(canvas);

        if (mPath0 != null) {
            mPathPaint.setColor(0x33ff0000);
            canvas.drawPath(mPath0, mPathPaint);
        }
        if (mPath30 != null) {
            mPathPaint.setColor(0x44ffff00);
            canvas.drawPath(mPath30, mPathPaint);
        }
        if (mPath50 != null) {

            mPathPaint.setColor(0x5588ff00);
            canvas.drawPath(mPath50, mPathPaint);
        }
        if (mPath80 != null) {
            mPathPaint.setColor(0x6600ff00);
            canvas.drawPath(mPath80, mPathPaint);
        }

        super.dispatchDraw(canvas);

        drawSketchMap(canvas);
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

    public void addDevice(WifiDevice device) {
        if (device == null) {
            throw new RuntimeException("WifiDevice对象不可为空");
        }

        if (isContainDevice(device)) {
            Toast.makeText(getContext(), device.getName() + " 设备不能重复添加", Toast.LENGTH_SHORT).show();
            return;
        }

        TextView child = createDisplayView(addDeviceLocation(device));
        initForceWave();
        super.addView(child);
    }

    public void addDevice(WifiDevice device, int absX, int absY) {
        if (device == null) {
            throw new RuntimeException("WifiDevice对象不可为空");
        }
        //判读是否进入此组件内
        int[] outLocation = new int[2];
        getLocationOnScreen(outLocation);
        if (absX < outLocation[0] || absX > outLocation[0] + getWidth() || absY < outLocation[1] || absY > outLocation[1] + getHeight())
            return;

        if (isContainDevice(device)) {
            Toast.makeText(getContext(), device.getName() + " 设备不能重复添加", Toast.LENGTH_SHORT).show();
            return;
        }

        TextView child = createDisplayView(addDeviceLocation(device, absX - outLocation[0], absY - outLocation[1]));
        initForceWave();
        super.addView(child);
    }

    // LAYOUT
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
//        Log.e(getClass().getSimpleName(), String.format("onLayout(%d,%d,%d,%d)", l, t, r, b));
        //布局背景
        if (changed) {
            if (mBgDrawable != null) {
                mBgDrawable.setBounds(0, 0, r - l, b - t);
            }
        }

        //布局设备
        for (int i = 0; i < getChildCount(); i++)

            if (i != dragged) {
                View v = getChildAt(i);
                v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
            }

        mSketchMapDrawable.setBounds(mRectStrSkechMap.width() + 1 + UnitUtils.dip2px(getContext(), 10),
                getHeight() - UnitUtils.dip2px(getContext(), 20),
                mRectStrSkechMap.width() + 1 + UnitUtils.dip2px(getContext(), 110),
                getHeight() - UnitUtils.dip2px(getContext(), 10));
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
        x += getScrollX();
        y += getScrollY();
        for (int i = getChildCount() - 1; i >= 0; i--) {
            View v = getChildAt(i);
            if (x < v.getLeft() || x > v.getRight() || y < v.getTop() || y > v.getBottom())
                continue;
            return i;
        }
        return -1;
    }

    public int getIndexOf(View child) {
        for (int i = 0; i < getChildCount(); i++)
            if (getChildAt(i) == child)
                return i;
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
            animateDragged();
            showDeleteView();
            return true;
        }
        return false;
    }

    private void setChildBounds(int dragged, float l, float t, float r, float b) {
        View v = getChildAt(dragged);
        v.layout((int) l, (int) t, (int) r, (int) b);
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
                int delta = lastY - (int) event.getY();

                if (dragged != -1) {
                    dragView(event.getX(), event.getY());
                } else {
                    if (event.getPointerCount() == 1) {

                        scroll += delta;
//                    clampScroll();
                        onScroll(event.getX(), event.getY());
                        if (Math.abs(delta) > 2) {
                            enabled = false;
                        }
//                        layout(getLeft(), getTop(), getRight(), getBottom());
                    } else {
                        on2PointDraged(event);
                    }
                }
                lastX = (int) event.getX();
                lastY = (int) event.getY();
                lastDelta = delta;
                break;
            case MotionEvent.ACTION_UP:
                if (dragged != -1) {
                    View v = getChildAt(dragged);
                    float v_width = v.getWidth();
                    float v_height = v.getHeight();
                    float x = event.getX(), y = event.getY();

                    x = Math.max(x, v_width / 2);
                    x = Math.min(x, getWidth() - v_width / 2);
                    y = Math.max(y, v_height / 2);
                    y = Math.min(y, getHeight() - v_height / 2);
                    x += getScrollX();
                    y += getScrollY();
                    v.clearAnimation();
                    setChildBounds(dragged, x - v_width / 2, y - v_height / 2, x + v_width / 2, y + v_height / 2);
                    //将移动了的view 的值存入对象
                    mListDeviceInfo.get(dragged).setXY(x, y);
                    hideDeleteView();
                    //重新定场强的样式
                    initForceWave();
                    dragged = -1;
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
        return false;
    }

    private void dragView(float x, float y) {
        onDragedInDeleteView((int) x, (int) y);

        View v = getChildAt(dragged);
        float v_width = v.getWidth();
        float v_height = v.getHeight();

        x = Math.max(x, v_width / 2);
        x = Math.min(x, getWidth() - v_width / 2);
        y = Math.max(y, v_height / 2);
        y = Math.min(y, getHeight() - v_height / 2);

        x -= v_width / 4;
        y -= v_height / 4;

        x += getScrollX();
        y += getScrollY();

        v.layout(Math.round(x - v_width / 2),
                Math.round(y - v_width / 2),
                Math.round(x + v_width / 2),
                Math.round(y + v_width / 2));

    }

    // EVENT HELPERS
    protected void animateDragged() {
        View v = getChildAt(dragged);
        float x = getScrollX() + lastX;
        float y = getScrollY() + lastY;
        float v_width = v.getWidth();
        float v_height = v.getHeight();
        v.layout(Math.round(x - v_width / 2), Math.round(y - v_height / 2), Math.round(x + v_width / 2), Math.round(y + v_height / 2));
        AnimationSet animSet = new AnimationSet(true);
        ScaleAnimation scale = new ScaleAnimation(1, 1.5f, 1, 1.5f,
                0.5f, 0.5f);
        scale.setDuration(animT);
        AlphaAnimation alpha = new AlphaAnimation(1, .5f);
        alpha.setDuration(animT);

        animSet.addAnimation(scale);
        animSet.addAnimation(alpha);
        animSet.setFillEnabled(true);
        animSet.setFillAfter(true);

        v.clearAnimation();
        v.startAnimation(animSet);
    }

    public void scrollToTop() {
        scroll = 0;
    }

    public void scrollToBottom() {
        scroll = Integer.MAX_VALUE;
        clampScroll();
    }

    private void onScroll(float x, float y) {
        if (mBgDrawable == null)
            return;

        float moveX = x - lastX;
        float moveY = y - lastY;
        int width = mBgDrawable.getBounds().width();
        int height = mBgDrawable.getBounds().height();

        int toX = (int) Math.min(Math.max(0, getScrollX() - moveX), width - getWidth());
        int toY = (int) Math.min(Math.max(0, getScrollY() - moveY), height - getHeight());

        scrollTo(toX, toY);
        postInvalidate();
    }

    protected void clampScroll() {
        int stretch = 3, overreach = getHeight() / 2;
        int max = getMaxScroll();
        max = Math.max(max, 0);

        if (scroll < -overreach) {
            scroll = -overreach;
            lastDelta = 0;
        } else if (scroll > max + overreach) {
            scroll = max + overreach;
            lastDelta = 0;
        } else if (scroll < 0) {
            if (scroll >= -stretch)
                scroll = 0;
            else if (!touching)
                scroll -= scroll / stretch;
        } else if (scroll > max) {
            if (scroll <= max + stretch)
                scroll = max;
            else if (!touching)
                scroll += (max - scroll) / stretch;
        }
    }

    protected int getMaxScroll() {
//        int rowCount = (int) Math.ceil((double) getChildCount() / colCount);
//        int max = rowCount
//                * childSize + (rowCount + 1) * padding - getHeight();
        int max = getHeight();
        return max;
    }

    public int getLastIndex() {
        return getIndexFromCoor(lastX, lastY);
    }


    public void setOnItemClickListener(AdapterView.OnItemClickListener l) {
        this.onItemClickListener = l;
    }

    public void setBackground(Drawable drawable) {

        if (drawable == null) {
            setScaleY(1);
            setScaleX(1);
            scrollTo(0, 0);
            layout(getLeft(), getTop(), getRight(), getBottom());
            return;
        }
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        if (width == 0)
            width = getWidth();
        if (height == 0)
            height = getHeight();

        if (width < getWidth() || height < getHeight()) {//当较小的时候，等比缩放
            float scale = getWidth() * 1f / width;
            scale = Math.max(scale, getHeight() * 1f / width);

            drawable.setBounds(0, 0, Math.round(width * scale), Math.round(height * scale));

        }
        mBgDrawable = drawable;
        postInvalidate();
    }

    @Override
    public void setBackgroundResource(int resid) {
        setBackground(getContext().getResources().getDrawable(resid));
//        super.setBackgroundResource(resid);
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
        double distance = get2PointDistance(event);
        float scale = (float) (distance / lastDistance * getScaleX());

        if (scale < 0.5f)
            scale = 0.5f;
        else if (scale > 4)
            scale = 4;

        if (getScrollX() != scale) {
            setScaleX(scale);
            setScaleY(scale);
        }

        lastDistance = distance;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {

        super.onScrollChanged(l, t, oldl, oldt);
    }

    public List<DeviceLocation> getmListDeviceInfo() {
        return mListDeviceInfo;
    }

    public void setListDeviceInfo(List<DeviceLocation> list) {
        this.mListDeviceInfo = list;
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

    /**
     * 将设备信息添加到位置列表中
     */
    private DeviceLocation addDeviceLocation(WifiDevice device) {
        return addDeviceLocation(device, getWidth() / 2 + getScrollX(), getHeight() / 2 + getScrollY());
    }

    private DeviceLocation addDeviceLocation(WifiDevice device, int x, int y) {
        if (device == null) {
            return null;
        }
        if (mListDeviceInfo == null)
            mListDeviceInfo = new ArrayList<>();

        DeviceLocation location = new DeviceLocation(mListDeviceInfo.size(),
                x, y, device.getMac(), device.getType());
        location.setWifiDevice(device);
        mListDeviceInfo.add(location);
        return location;
    }

    private TextView createDisplayView(DeviceLocation d) {
        TextView child = new TextView(getContext());
        child.setGravity(Gravity.CENTER);
        Drawable drawable = getResources().getDrawable(App.RESID_WIFI_DEVICE[d.getType()]).mutate();
        drawable.setBounds(0, 0,
                (int) (childSize * drawable.getIntrinsicWidth() / drawable.getIntrinsicHeight()), (int) childSize);
        drawable = DrawableCompat.wrap(drawable);
        child.setCompoundDrawables(null, drawable, null, null);
        if (d.getWifiDevice() == null) {
            child.setText(d.getMac());
            child.setEnabled(false);
            drawable.setTint(Color.DKGRAY);
        } else {
            child.setText(d.getWifiDevice().getName());
        }
        child.layout((int) (d.getX() - childSize / 2), (int) (d.getY() - childSize / 2),
                (int) (d.getX() + childSize), (int) (d.getY() + childSize));
        return child;
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
                view.setTag(d.getWifiDevice());
                if (d.getWifiDevice() == null) {
                    view.setText(d.getMac());
                    view.setEnabled(false);
                    drawable.setTint(Color.DKGRAY);
                } else {
                    view.setText(d.getWifiDevice().getName());
                }
            }
        initForceWave();
    }


    private void initForceWave() {

        if (mListDeviceInfo == null || mListDeviceInfo.isEmpty())
            return;
        float cX = -1, cY = -1;//中心点坐标

        for (DeviceLocation device : mListDeviceInfo) {
            if (device.getType() == App.TYPE_DEVICE_WIFI) {
                cX = device.getX();
                cY = device.getY();
                break;
            }
        }

        if (cX == -1 || cY == -1 || mListDeviceInfo.size() - 1 == 0) {
            cleanPath();
            postInvalidate();
            return;
        }

        ArrayList<DeviceLocation> effectPoints = new ArrayList<>();
        for (DeviceLocation d : mListDeviceInfo) {
            if (d.getType() == App.TYPE_DEVICE_WIFI || d.getWifiDevice() == null || d.getWifiDevice().getRssi() == 100)
                continue;
            effectPoints.add(d);
        }

        switch (effectPoints.size()) {
            case 0:
                cleanPath();
                break;
            case 1:
                mPath0 = creaetPathOnePoints(effectPoints, 0, cX, cY);
                mPath30 = creaetPathOnePoints(effectPoints, 30, cX, cY);
                mPath50 = creaetPathOnePoints(effectPoints, 50, cX, cY);
                mPath80 = creaetPathOnePoints(effectPoints, 80, cX, cY);
                break;
            case 2:
                mPath0 = creaetPathTwoPoints(effectPoints, 0, cX, cY);
                mPath30 = creaetPathTwoPoints(effectPoints, 30, cX, cY);
                mPath50 = creaetPathTwoPoints(effectPoints, 50, cX, cY);
                mPath80 = creaetPathTwoPoints(effectPoints, 80, cX, cY);
                break;
            default:
                mPath0 = creaetPathMorePoints(effectPoints, 0, cX, cY);
                mPath30 = creaetPathMorePoints(effectPoints, 30, cX, cY);
                mPath50 = creaetPathMorePoints(effectPoints, 50, cX, cY);
                mPath80 = creaetPathMorePoints(effectPoints, 80, cX, cY);
                break;
        }


        postInvalidate();
    }

    private void cleanPath() {
        mPath0 = null;
        mPath30 = null;
        mPath50 = null;
        mPath80 = null;
    }

    private Path creaetPathOnePoints(List<DeviceLocation> list, float rate, float cX, float cY) {
        Path path = new Path();

        DeviceLocation d = list.get(0);
        double distance = getDistanceIn2Points(cX, cY, d.getX(), d.getY());

        double R = distance;
        R /= (100 - d.getWifiDevice().getRssi());
        R *= (100 - rate);

        path.addCircle(cX, cY, (float) R, Path.Direction.CW);

        return path;
    }

    private Path creaetPathTwoPoints(List<DeviceLocation> list, float rate, float cX, float cY) {
        Path path = new Path();

        ArrayList<PointF> points = new ArrayList<>(list.size());
        ArrayList<Double> rs = new ArrayList<>(list.size());
        for (DeviceLocation d : list) {
            double distance = getDistanceIn2Points(cX, cY, d.getX(), d.getY());
            double R = distance / (100 - d.getWifiDevice().getRssi()) * (100 - rate);

            rs.add(R);

            double x = 100 - rate;
            x /= 100 - d.getWifiDevice().getRssi();
            x *= d.getX() - cX;
            x = x + cX;

            double y = 100 - rate;
            y /= 100 - d.getWifiDevice().getRssi();
            y *= d.getY() - cY;
            y = y + cY;

            points.add(new PointF((float) x, (float) y));
        }


        //先增加最小半径
        double minR = Integer.MAX_VALUE;

        for (Double r : rs) {
            if (minR > r)
                minR = r;
        }
        path.addCircle(cX, cY, (float) minR, Path.Direction.CW);

        //其它点增加到中心点为直径的圆
        for (int i = 0; i < points.size(); i++) {
            double R = rs.get(i);
            if (R <= minR)
                continue;

            PointF p = points.get(i);

            float oX = (cX + p.x) / 2;
            float oY = (cY + p.y) / 2;
            double nR = getDistanceIn2Points(oX, oY, p.x, p.y);
            path.addCircle(oX, oY, (float) nR, Path.Direction.CW);
        }
        path.close();
        return path;
    }

    private Path creaetPathMorePoints(List<DeviceLocation> list, float rate, float cX, float cY) {
        Path path = new Path();

        ArrayList<PointF> points = new ArrayList<>(list.size());

        double minR = Integer.MAX_VALUE;
        for (DeviceLocation d : list) {
            double distance = getDistanceIn2Points(cX, cY, d.getX(), d.getY());
            double R = distance / (100 - d.getWifiDevice().getRssi()) * (100 - rate);

            minR = Math.min(R, minR);
            double x = 100 - rate;
            x /= 100 - d.getWifiDevice().getRssi();
            x *= d.getX() - cX;
            x = x + cX;

            double y = 100 - rate;
            y /= 100 - d.getWifiDevice().getRssi();
            y *= d.getY() - cY;
            y = y + cY;

            points.add(new PointF((float) x, (float) y));
        }

        path.addCircle(cX, cY, (float) minR, Path.Direction.CW);

        for (int i = 0; i < points.size() - 2; ++i) {
            for (int j = i + 1; j < points.size() - 1; j++) {
                for (int k = j + 1; k < points.size(); k++) {
                    if (isInLine(points.get(i), points.get(j), points.get(k)))
                        continue;
                    path.addPath(getPathBy3Points(points.get(i), points.get(j), points.get(k)));
                }
            }
        }


        path.close();
        return path;
    }

    /**
     * 获取三个点组成的圆
     */
    private Path getPathBy3Points(PointF... ps) {
        Path path = new Path();
        if (ps == null || ps.length != 3)
            return path;

        float a = ps[0].x - ps[1].x;
        float b = ps[0].y - ps[1].y;
        float c = ps[0].x - ps[2].x;
        float d = ps[0].y - ps[2].y;

        double e = Math.pow(ps[0].x, 2) - Math.pow(ps[1].x, 2);
        e += Math.pow(ps[0].y, 2) - Math.pow(ps[1].y, 2);
        e /= 2;

        double f = Math.pow(ps[0].x, 2) - Math.pow(ps[2].x, 2);
        f += Math.pow(ps[0].y, 2) - Math.pow(ps[2].y, 2);
        f /= 2;

        double x0 = b * f - d * e;
        x0 /= b * c - a * d;

        double y0 = c * e - a * f;
        y0 /= b * c - a * d;

        double R = getDistanceIn2Points((float) x0, (float) y0, ps[0].x, ps[0].y);

        path.addCircle((float) x0, (float) y0, (float) R, Path.Direction.CW);

        return path;
    }

    /**
     * 判断三个点是否在同一条直线上
     */
    private boolean isInLine(PointF... ps) {
        if (ps.length < 3)
            return false;

        boolean isX0 = ps[0].x == 0;
        if (isX0) {
            for (int i = 1; i < ps.length - 1; i++) {
                if (ps[i].x != 0)
                    return false;
            }
            return true;
        }

        float angle = isX0 ? 0 : ps[0].y / ps[0].x;

        for (int i = 1; i < ps.length - 1; i++) {
            if (ps[i].y / ps[i].x != angle)
                return false;
        }
        return true;
    }

    /**
     * 获取两点之间的距离
     */
    private double getDistanceIn2Points(float x1, float y1, float x2, float y2) {
        double distance = Math.pow(x1 - x2, 2);
        distance += Math.pow(y1 - y2, 2);
        distance = Math.sqrt(distance);
        return distance;
    }

    /**
     * 设置拖拽监控
     */
    public void setOnDragListener(OnDragListener onDragListener) {
        this.onDragListener = onDragListener;
    }

    /**
     * 拖动item的接口
     */
    public interface OnDragListener {
        void onDraged(DeviceLocation deviceLocation, float x, float y);
    }

    private void initPaint() {
        mPathPaint = new Paint();
        mPathPaint.setStyle(Paint.Style.FILL);
        mPathPaint.setColor(0x6655bb44);
        mPathPaint.setTextSize(UnitUtils.dip2px(getContext(), 12));
        mPathPaint.setTextScaleX(0.8f);
        mPathPaint.setTextLocale(Locale.CHINESE);
        mPathPaint.setTypeface(Typeface.DEFAULT_BOLD);
        //去锯齿
        mPathPaint.setAntiAlias(true);
        mPathPaint.setStrokeWidth(5); //设置线条的宽度
    }

    private void showDeleteView() {
        if (dragged != -1) {
            Context context = getContext();
            mDeleteTextView = new AppCompatImageView(context);
            mDeleteTextView.setPadding(4, 4, 4, 4);
            mDeleteTextView.setImageResource(android.R.drawable.ic_menu_delete);
//            mDeleteTextView.setBackgroundColor(0x6600aa00);
            mDeleteTextView.setBackgroundResource(R.drawable.round_rect_delete);

            WindowManager.LayoutParams mDeleteLayoutParams = new WindowManager.LayoutParams();
            mDeleteLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
            int[] location = new int[2];
            getLocationOnScreen(location);
            mDeleteLayoutParams.height = UnitUtils.dip2px(getContext(), 48);
            mDeleteLayoutParams.width = mDeleteLayoutParams.height;
            mDeleteLayoutParams.x = location[0] + getWidth() / 2 - mDeleteLayoutParams.width / 2;
            mDeleteLayoutParams.y = location[1] - mDeleteLayoutParams.height;

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
        System.err.println(String.format("(%d,%d),", x, y) + mDeleteRect.toString());
        return mDeleteRect.contains(x, y);
    }

    private void onDragedInDeleteView(int x, int y) {
        if (mDeleteTextView != null) {
//            mDeleteTextView.setBackgroundColor(isInDeleteState(x, y) ? 0x66ff0000 : 0x6600aa00);
            mDeleteTextView.setEnabled(!isInDeleteState(x, y));
        }
    }

    private void hideDeleteView() {
        if (dragged != -1 && mDeleteTextView != null) {
            if (isInDeleteState(lastX, lastY)) {
                removeViewAt(dragged);
                mListDeviceInfo.remove(dragged);
            }

            mDeleteTextView.setVisibility(View.GONE);
            mWindowManager.removeView(mDeleteTextView);
        }
    }

    /**
     * 绘制示意图
     */
    private void drawSketchMap(Canvas canvas) {
        mSketchMapDrawable.draw(canvas);
        Rect rect = mSketchMapDrawable.getBounds();
        mPathPaint.setColor(Color.DKGRAY);
        canvas.drawText(mStrIntensity[0], rect.left - mRectStrSkechMap.width() - 1, rect.bottom, mPathPaint);
        canvas.drawText(mStrIntensity[1], rect.right + 1, rect.bottom, mPathPaint);

    }

    private void initSketchMap() {
        mSketchMapDrawable = getResources().getDrawable(R.drawable.gradient_sketch_color);
        mStrIntensity = getResources().getStringArray(R.array.intensity);
        mRectStrSkechMap = new Rect();
        mPathPaint.getTextBounds(mStrIntensity[0], 0, mStrIntensity[0].length(), mRectStrSkechMap);
    }
}
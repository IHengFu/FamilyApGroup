package wifiairscout.changhong.com.wifiairscout.ui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
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
import java.util.Locale;

import wifiairscout.changhong.com.wifiairscout.App;
import wifiairscout.changhong.com.wifiairscout.R;
import wifiairscout.changhong.com.wifiairscout.model.DeviceLocation;
import wifiairscout.changhong.com.wifiairscout.model.HouseData;
import wifiairscout.changhong.com.wifiairscout.model.WifiDevice;
import wifiairscout.changhong.com.wifiairscout.utils.CommUtils;
import wifiairscout.changhong.com.wifiairscout.utils.FileUtils;
import wifiairscout.changhong.com.wifiairscout.utils.UnitUtils;

/**
 * Created by fuheng on 2017/12/25.
 */
public class DragViewGroup2 extends ViewGroup implements View.OnTouchListener,
        View.OnClickListener, View.OnLongClickListener {
    private final int RADIUS_ROUND_RECT;
    // layout vars
    private final int SIZE_CHILD;
    private final int SIZE_CHILD_TEXT;

    private static final int[] COLOR_PATH_RANGE = {0x33ff0000, 0x44ffff00, 0x5588ff00, 0x6600ff00};

    private static final float[] RATE_WAVE = {0, .3f, .5f, .8f};
    private static final double[] ARR_RATE_LINE = new double[RATE_WAVE.length];

    static {
        for (int i = 0; i < ARR_RATE_LINE.length; i++) {
            ARR_RATE_LINE[i] = CommUtils.dbm2Distance((App.MIN_RSSI - App.MAX_RSSI) * (1 - RATE_WAVE[i]) + App.MAX_RSSI);
        }
    }

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

    private static final int STATE_NORMAL = 0;
    private static final int STATE_DRAG = 1;
    private static final int STATE_MOVE_CONTNT = 2;
    private static final int STATE_MOVE_ZOOM = 3;
    private int mState;

    private Rect mTextBoundsRect = new Rect();

    /**
     * 缩放相关
     */
    private float mScale = 1f;
    /**
     * 拖拽相关
     */
    private int mScrollX, mScrollY;

    private Drawable mBgDrawable;

    private HouseData mHouseData;

    private ArrayList<RectF> mArrayRectDropEnable;
    /**
     * 可绘制空间的path
     */
    private Path mPathDrawWaveEnable;

    private Paint mPathDropEnable = null;
    private boolean mIsDraggedRectShowing;


    /**
     * 设备信息
     */
    private List<DeviceLocation> mListDeviceInfo;

    private Paint mPathPaint = null;

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
    public DragViewGroup2(Context context, AttributeSet attrs) {
        super(context, attrs);

        setListeners();

        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        setChildrenDrawingOrderEnabled(true);

        SIZE_CHILD = UnitUtils.dip2px(getContext(), 30);
        SIZE_CHILD_TEXT = UnitUtils.dip2px(getContext(), 12);
        RADIUS_ROUND_RECT = UnitUtils.dip2px(getContext(), 3);

        initPaint();
        initSketchMap();
    }


    @Override
    protected void dispatchDraw(Canvas canvas) {
        try {
            if (mBgDrawable != null)
                mBgDrawable.draw(canvas);

//        for (int i = 0; i < mArrayPathWave.length; ++i)
//            if (mArrayPathWave[i] != null) {
//                mPathPaint.setColor(COLOR_PATH_RANGE[i]);
//                canvas.drawPath(mArrayPathWave[i], mPathPaint);
//            }

            if (mDrawableWave != null)
                mDrawableWave.draw(canvas);

            super.dispatchDraw(canvas);

            if (mIsDraggedRectShowing && mArrayRectDropEnable != null && !mArrayRectDropEnable.isEmpty()) {
                for (RectF rect : mArrayRectDropEnable) {
//                canvas.drawRect(rect, mPathDropEnable);
                    canvas.drawRoundRect(rect, RADIUS_ROUND_RECT, RADIUS_ROUND_RECT, mPathDropEnable);
                }
            }

            drawSketchMap(canvas);
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

        View child = createDisplayView(addDeviceLocation(device,
                absX - outLocation[0] + mScrollX,
                absY - outLocation[1] + mScrollY));
        child.setTag(device);
        try {
            initForceWave();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.addView(child);
    }

    // LAYOUT
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.e(getClass().getSimpleName(), String.format("onLayout(%d,%d,%d,%d)", l, t, r, b));
        //布局背景
        if (mBgDrawable != null) {
            mBgDrawable.setBounds(-mScrollX, -mScrollY,
                    (int) (mBgDrawable.getIntrinsicWidth() * mScale - mScrollX),
                    (int) (mBgDrawable.getIntrinsicHeight() * mScale - mScrollY));
        }

        if (mDrawableWave != null) {
            mDrawableWave.setBounds(-mScrollX, -mScrollY,
                    (int) (mBgDrawable.getIntrinsicWidth() * mScale - mScrollX),
                    (int) (mBgDrawable.getIntrinsicHeight() * mScale - mScrollY));
        }

        //布局设备 child 只位移不缩放
        for (int i = 0; i < getChildCount(); i++) {
            DeviceLocation d = mListDeviceInfo.get(i);
            float x = d.getX() * mScale - mScrollX;
            float y = d.getY() * mScale - mScrollY;
            if (i != dragged) {
                setChildLocation(i, x, y);
            }
        }

        if (mArrayRectDropEnable != null) {
            for (int i = 0; i < mArrayRectDropEnable.size(); i++) {
                Rect rect = mHouseData.getAreas().get(i);//用原始数据作为参照
                RectF newRect = mArrayRectDropEnable.get(i);
                newRect.set(rect.left * mScale - mScrollX, rect.top * mScale - mScrollY,
                        rect.right * mScale - mScrollX, rect.bottom * mScale - mScrollY);
            }
        }

        //游标卡尺位置不动
        if (changed) {
            mSketchMapDrawable.setBounds(mRectStrSkechMap.width() + 1 + UnitUtils.dip2px(getContext(), 10),
                    getHeight() - UnitUtils.dip2px(getContext(), 20),
                    mRectStrSkechMap.width() + 1 + UnitUtils.dip2px(getContext(), 110),
                    getHeight() - UnitUtils.dip2px(getContext(), 10));
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
                    float v_width = v.getWidth();
                    float v_height = v.getHeight();

                    x = Math.max(x, mBgDrawable.getBounds().left + v_width / 2);
                    x = Math.min(x, mBgDrawable.getBounds().right - v_width / 2);
                    y = Math.max(y, mBgDrawable.getBounds().top + v_height / 2);
                    y = Math.min(y, mBgDrawable.getBounds().bottom) - v_height / 2;

                    setChildLocation(dragged, x, y);
                    //将移动了的view 的值存入对象
                    mListDeviceInfo.get(dragged).setXY((x + mScrollX) / mScale, (y + mScrollY) / mScale);
                    hideDeleteView();
                    //重新定场强的样式
                    try {
                        initForceWave();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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
        return false;
    }

    private void dragView(float x, float y) {

        //判断是否进入删除区域
        onDragedInDeleteView((int) x, (int) y);

        View v = getChildAt(dragged);

        float v_width = v.getWidth();
        float v_height = v.getHeight();

        x = Math.max(x, mBgDrawable.getBounds().left + v_width / 2);
        x = Math.min(x, mBgDrawable.getBounds().right - v_width / 2);
        y = Math.max(y, mBgDrawable.getBounds().top + v_height / 2);
        y = Math.min(y, mBgDrawable.getBounds().bottom) - v_height / 2;

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
        if (mBgDrawable == null)
            return;

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
        int width_bg = mBgDrawable.getBounds().width();
        int height_bg = mBgDrawable.getBounds().height();

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

        if (mScale < .5F)
            mScale = .5F;
        else if (mScale > 2)
            mScale = 2;
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

    public List<DeviceLocation> getListDeviceInfo() {
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
        return addDeviceLocation(device, (int) (getWidth() / 2 + mScrollX), (int) (getHeight() / 2 + mScrollY));
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

    private View createDisplayView(DeviceLocation d) {
        TextView child = new TextView(getContext());
        Drawable drawable = getResources().getDrawable(App.RESID_WIFI_DEVICE[d.getType()]).mutate();
        drawable.setBounds(0, 0,
                (SIZE_CHILD * drawable.getMinimumWidth() / drawable.getMinimumHeight()), SIZE_CHILD);
        child.setGravity(Gravity.CENTER);
        drawable = DrawableCompat.wrap(drawable);
        child.setCompoundDrawables(null, drawable, null, null);
        child.setGravity(Gravity.CENTER);
        child.setMaxLines(1);
        child.setEllipsize(TextUtils.TruncateAt.END);

        String title = "";
        if (d.getWifiDevice() == null) {
            title = d.getMac();
            child.setEnabled(false);
            drawable.setTint(Color.DKGRAY);
        } else {
            title = d.getWifiDevice().getName();
        }
        child.setText(title);
        child.setBackgroundResource(R.drawable.round_rect_child);

        mPathPaint.getTextBounds(title, 0, title.length(), mTextBoundsRect);
        float x = d.getX() * mScale - mScrollX;
        float y = d.getY() * mScale - mScrollY;
        float width = Math.max(drawable.getBounds().width(), mTextBoundsRect.width());
        float height = drawable.getBounds().height() + mTextBoundsRect.height();
        child.setCompoundDrawablePadding(0);
        child.layout((int) (x - width / 2), (int) (y - height / 2),
                (int) (x + width / 2), (int) (y + height / 2));
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
        cleanPath();

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
        ArrayList<Path> arrPath = null;
        switch (effectPoints.size()) {
            case 0:
                break;
            case 1:
                arrPath = createPathOnePoints(effectPoints, cX, cY);
                break;
            case 2:
                arrPath = createPathTwoPoints(effectPoints, cX, cY);
                break;
            default:
                arrPath = createPathMorePoints(effectPoints, cX, cY);
                break;
        }


        if (arrPath != null) {

            for (int i = 0; i < arrPath.size() - 1; i++) {//扣
                arrPath.get(i).op(arrPath.get(i + 1), Path.Op.DIFFERENCE);
                arrPath.get(i).close();
            }

            Bitmap bitmap = Bitmap.createBitmap(mBgDrawable.getIntrinsicWidth(), mBgDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_4444);
            Canvas canvas = new Canvas(bitmap);
            {//只展示房间内的信号强度
                Path path = new Path();
                for (Rect r : mHouseData.getAreas())
                    path.addRect(r.left, r.top, r.right, r.bottom, Path.Direction.CW);
                canvas.clipPath(path);
            }
            for (int i = 0; i < ARR_RATE_LINE.length; i++) {
                Path path = arrPath.get(i);
                mPathPaint.setColor(COLOR_PATH_RANGE[i]);
                canvas.drawPath(path, mPathPaint);
            }

            mDrawableWave = new BitmapDrawable(getResources(), bitmap);
            mDrawableWave.setBounds(-mScrollX, -mScrollY,
                    (int) (mBgDrawable.getIntrinsicWidth() * mScale - mScrollX),
                    (int) (mBgDrawable.getIntrinsicHeight() * mScale - mScrollY));

        }

        postInvalidate();
    }


    private void cleanPath() {
        if (mDrawableWave != null) {
            mDrawableWave.getBitmap().recycle();
            mDrawableWave = null;
            System.gc();
        }
    }

    private BitmapDrawable mDrawableWave;

    private ArrayList<Path> createPathOnePoints(List<DeviceLocation> list, float cX, float cY) {

        DeviceLocation d = list.get(0);

        double R = getDistanceIn2Points(cX, cY, d.getX(), d.getY());
        double distance = CommUtils.dbm2Distance(d.getWifiDevice().getRssi());

        ArrayList<Path> arrayList = new ArrayList<>(ARR_RATE_LINE.length);

        for (int i = 0; i < ARR_RATE_LINE.length; i++) {
            Path path = new Path();

            double nR = R / distance * ARR_RATE_LINE[i];
            path.addCircle(cX, cY, (float) nR, Path.Direction.CW);
            path.close();

            arrayList.add(path);

        }

        return arrayList;
    }

    private ArrayList<Path> createPathTwoPoints(List<DeviceLocation> list, float cX, float cY) {
        ArrayList<Path> arrayList = new ArrayList<>();

        ArrayList<PointF> points = new ArrayList<>(list.size());
        ArrayList<Double> rs = new ArrayList<>(list.size());

        //先增加最小半径
        double minR;
        for (int i = 0; i < ARR_RATE_LINE.length; i++) {
            rs.clear();
            points.clear();

            Path path = new Path();

            for (DeviceLocation d : list) {
                double R = getDistanceIn2Points(cX, cY, d.getX(), d.getY());
                double distance = CommUtils.dbm2Distance(d.getWifiDevice().getRssi());
                double nR = R / distance * ARR_RATE_LINE[i];

                rs.add(nR);

                double rate = ARR_RATE_LINE[i] / R;

                double x = d.getX() - cX;
                x *= rate;
                x += cX;
                double y = d.getY() - cY;
                y *= rate;
                y += cY;

                points.add(new PointF((float) x, (float) y));
            }

            minR = Integer.MAX_VALUE;
            for (double r : rs) {
                if (minR > r)
                    minR = r;
            }

            path.addCircle(cX, cY, (float) minR, Path.Direction.CW);

            //其它点增加到中心点为直径的圆
            for (int j = 0; j < points.size(); j++) {
                double R = rs.get(j);
                if (R <= minR)
                    continue;

                PointF p = points.get(j);

                float oX = (cX + p.x) / 2;
                float oY = (cY + p.y) / 2;
                double nR = getDistanceIn2Points(oX, oY, p.x, p.y);
                path.addCircle(oX, oY, (float) nR, Path.Direction.CW);
            }
            path.close();

            arrayList.add(path);
        }

        return arrayList;
    }

    private ArrayList<Path> createPathMorePoints(List<DeviceLocation> list, float cX, float cY) {
        ArrayList<Path> arrayList = new ArrayList<>();

        ArrayList<PointF> points = new ArrayList<>(list.size());
        ArrayList<Double> rs = new ArrayList<>(list.size());

        //先增加最小半径
        double minR;

        for (int i = 0; i < ARR_RATE_LINE.length; i++) {
            rs.clear();
            points.clear();
            minR = Integer.MAX_VALUE;
            Path path = new Path();

            for (DeviceLocation d : list) {
                double R = getDistanceIn2Points(cX, cY, d.getX(), d.getY());
                double distance = CommUtils.dbm2Distance(d.getWifiDevice().getRssi());
                double nR = R / distance * ARR_RATE_LINE[i];

                rs.add(nR);

                double rate = ARR_RATE_LINE[i] / R;

                double x = d.getX() - cX;
                x *= rate;
                x += cX;
                double y = d.getY() - cY;
                y *= rate;
                y += cY;

                points.add(new PointF((float) x, (float) y));
            }


            for (Double r : rs) {
                if (minR > r)
                    minR = r;
            }

            path.addCircle(cX, cY, (float) minR, Path.Direction.CW);

            //其它点增加到中心点为直径的圆
            for (int l = 0; l < points.size() - 2; ++l) {
                for (int j = i + 1; j < points.size() - 1; j++) {
                    for (int k = j + 1; k < points.size(); k++) {
                        if (isInLine(points.get(l), points.get(j), points.get(k)))
                            continue;
                        path.addPath(getPathBy3Points(points.get(l), points.get(j), points.get(k)));
                    }
                }
            }
            path.close();
            arrayList.add(path);
        }

        return arrayList;
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
        mPathPaint.setTextSize(SIZE_CHILD_TEXT);
        mPathPaint.setTextScaleX(0.8f);
        mPathPaint.setTextLocale(Locale.CHINESE);
        mPathPaint.setTypeface(Typeface.DEFAULT_BOLD);
        //去锯齿
        mPathPaint.setAntiAlias(true);
        mPathPaint.setStrokeWidth(5); //设置线条的宽度

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

    /**
     * 判断是否删除
     */
    private void onDragedInDeleteView(int x, int y) {
        if (mDeleteTextView != null) {
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
        mPathPaint.setColor(Color.WHITE);
        canvas.drawText(mStrIntensity[0], rect.left - mRectStrSkechMap.width() - 1, rect.bottom, mPathPaint);
        canvas.drawText(mStrIntensity[1], rect.right + 1, rect.bottom, mPathPaint);


    }

    private void initSketchMap() {
        mSketchMapDrawable = getResources().getDrawable(R.drawable.gradient_sketch_color);
        mStrIntensity = getResources().getStringArray(R.array.intensity);
        mRectStrSkechMap = new Rect();
        mPathPaint.getTextBounds(mStrIntensity[0], 0, mStrIntensity[0].length(), mRectStrSkechMap);
    }

    public void setDraggedRectShow(boolean isShowing) {
        mIsDraggedRectShowing = isShowing;
    }

    /**
     * 判断两个矩形是否相交
     ***/
    private boolean isRectCoverRect(Rect r1, Rect r2) {
        if (r1.right <= r2.left || r1.left >= r2.right || r1.top >= r2.bottom || r1.bottom <= r2.top)
            return false;

        return true;
    }

    public void setHouseData(HouseData houseData) {
        if (houseData == mHouseData || (mHouseData != null && mHouseData.equals(houseData))) {
            return;
        }
        mHouseData = houseData;

        reset();
    }

    /**
     * -90~270
     */
    private double getRelativeAngle(float cx, float cy, float x, float y) {
        double reslut = y - cy;
        reslut /= x - cx;

        reslut = Math.atan(reslut);
        reslut = Math.toDegrees(reslut);

        if (reslut < 0) {
            reslut += 360;
        }

        return reslut;
    }

    private void reset() {
        //清空过去的数据，还原
        mScale = 1;
        mScrollX = 0;
        mScrollY = 0;

        removeAllViews();

        if (mArrayRectDropEnable != null)
            mArrayRectDropEnable.clear();

        cleanPath();

        if (mListDeviceInfo != null)
            mListDeviceInfo.clear();


        //建新数据
        if (mHouseData == null) {
            requestLayout();
            return;
        }

        Bitmap bitmap = FileUtils.getBitmapFromAssets(getContext(), mHouseData.getBackground());

        BitmapDrawable drawable = new BitmapDrawable(getResources(), bitmap);

        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        drawable.setBounds(0, 0, width, height);

        if (mHouseData.getAreas() == null)
            return;
        if (mArrayRectDropEnable == null)
            mArrayRectDropEnable = new ArrayList<>();

        if(mPathDrawWaveEnable!=null)
            mPathDrawWaveEnable.reset();

        for (Rect rect : mHouseData.getAreas()) {
            RectF newRect = new RectF(rect);
            mArrayRectDropEnable.add(newRect);
        }
        mBgDrawable = drawable;

        requestLayout();
    }

}
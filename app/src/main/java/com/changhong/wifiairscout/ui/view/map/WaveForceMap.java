package com.changhong.wifiairscout.ui.view.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.changhong.wifiairscout.App;
import com.changhong.wifiairscout.R;
import com.changhong.wifiairscout.db.data.DeviceLocation;
import com.changhong.wifiairscout.utils.CommUtils;
import com.changhong.wifiairscout.utils.UnitUtils;

/**
 * Created by fuheng on 2018/1/18.
 */

public class WaveForceMap implements TypeMap {

    private final Context mContext;
    private final int SIZE_CHILD_TEXT;
    private final float RADIUS_ROUND_RECT;

    /**
     * 强弱示意图
     */
    private Drawable mSketchMapDrawable;
    /**
     * 强弱示意文字
     */
    private String[] mStrIntensity;

    private BitmapDrawable mDrawableWave;
    private Paint mPathPaint;

    private Rect mRectStrSkechMap;


    public WaveForceMap(Context context, int size_child_text, float radius) {
        mContext = context;
        SIZE_CHILD_TEXT = size_child_text;
        RADIUS_ROUND_RECT = radius;

        initPaint();
        initSketchMap(context);
    }

    @Override
    public void refresh(ViewGroup viewGroup, List<DeviceLocation> list, float scale, int scrollX, int scrollY) {
        if (viewGroup.getChildCount() > 0)
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                TextView view = (TextView) viewGroup.getChildAt(i);
                if (view.getBackground() != null)
                    view.setBackground(null);
            }
        initForceWave(viewGroup, list, scale, scrollX, scrollY);
    }

    @Override
    public void clean(ViewGroup viewGroup) {
        cleanPath();
    }

    @Override
    public void drawContent(Canvas canvas) {

        if (mDrawableWave != null)
            mDrawableWave.draw(canvas);

    }

    @Override
    public void drawSketchMap(Canvas canvas) {
        mSketchMapDrawable.draw(canvas);
        Rect rect = mSketchMapDrawable.getBounds();
        mPathPaint.setColor(Color.WHITE);
        canvas.drawText(mStrIntensity[0], rect.left - mRectStrSkechMap.width() - 1, rect.bottom, mPathPaint);
        canvas.drawText(mStrIntensity[1], rect.right + 1, rect.bottom, mPathPaint);
    }

    @Override
    public void onLayout(boolean changed, int l, int t, int r, int b, float scale, int scrollX, int scrollY) {
        //布局背景
        if (mDrawableWave != null) {
            mDrawableWave.setBounds(-scrollX, -scrollY,
                    (int) ((r - l) * scale - scrollX),
                    (int) ((b - t) * scale - scrollY));
        }

        if (changed)
            mSketchMapDrawable.setBounds(mRectStrSkechMap.width() + 1 + UnitUtils.dip2px(mContext, 10),
                    b - t - UnitUtils.dip2px(mContext, 20),
                    mRectStrSkechMap.width() + 1 + UnitUtils.dip2px(mContext, 110),
                    b - t - UnitUtils.dip2px(mContext, 10));
    }

    @Override
    public void onAdd(ViewGroup group, View view, DeviceLocation d) {

    }

    @Override
    public void onRemove(View view, DeviceLocation d) {

    }

    private void initForceWave(ViewGroup viewGroup, List<DeviceLocation> list, float scale, int scrollX, int scrollY) {
        cleanPath();

        if (list == null || list.isEmpty())
            return;

        float cX = -1, cY = -1;//中心点坐标

        for (DeviceLocation device : list) {
            if (device.getType() == App.TYPE_DEVICE_WIFI) {
                cX = device.getX();
                cY = device.getY();
                break;
            }
        }

        if (cX == -1 || cY == -1 || list.size() - 1 == 0) {
            viewGroup.postInvalidate();
            return;
        }

        ArrayList<DeviceLocation> effectPoints = new ArrayList<>();
        for (DeviceLocation d : list) {
            if (d.getType() == App.TYPE_DEVICE_WIFI || d.getWifiDevice() == null)
                continue;
            effectPoints.add(d);
        }

        if (effectPoints.isEmpty()) {
            return;
        }

        ArrayList<Path> arrPath = null;
        switch (effectPoints.size()) {
            case 0:
                break;
            case 1:
                arrPath = createPathOnePoints(effectPoints, cX, cY);
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

            Bitmap bitmap = Bitmap.createBitmap(viewGroup.getWidth(), viewGroup.getHeight(), Bitmap.Config.ARGB_4444);
            Canvas canvas = new Canvas(bitmap);
            {//只展示房间内的信号强度
                Path path = new Path();
                path.addRoundRect(0, 0, viewGroup.getWidth(), viewGroup.getHeight(), RADIUS_ROUND_RECT, RADIUS_ROUND_RECT, Path.Direction.CCW);
                canvas.clipPath(path);
            }

            for (int i = 0; i < ARR_DISTANCE_RATE.length; i++) {
                Path path = arrPath.get(i);
                mPathPaint.setColor(COLOR_PATH_RANGE[i]);
                canvas.drawPath(path, mPathPaint);
            }

            mDrawableWave = new BitmapDrawable(mContext.getResources(), bitmap);
            mDrawableWave.setBounds((int) -scrollX, (int) -scrollY,
                    (int) (viewGroup.getWidth() * scale - scrollX),
                    (int) (viewGroup.getHeight() * scale - scrollY));

        }

        viewGroup.postInvalidate();
    }

    private ArrayList<Path> createPathOnePoints(List<DeviceLocation> list, float cX, float cY) {

        DeviceLocation d = list.get(0);

        double R = getDistanceIn2Points(cX, cY, d.getX(), d.getY());
        double distance = CommUtils.dbm2Distance(d.getWifiDevice().getRssi());

        ArrayList<Path> arrayList = new ArrayList<>(ARR_DISTANCE_RATE.length);

        for (int i = 0; i < ARR_DISTANCE_RATE.length; i++) {
            Path path = new Path();

            double nR = R * ARR_DISTANCE_RATE[i] / distance;
            path.addCircle(cX, cY, (float) nR, Path.Direction.CW);
            path.close();

            arrayList.add(path);

        }

        return arrayList;
    }

    private ArrayList<Path> createPathMorePoints(List<DeviceLocation> list, float cX, float cY) {
        ArrayList<Path> arrayList = new ArrayList<>();

        for (DeviceLocation d : list) {
            d.setAngle((float) getRelativeAngle(cX, cY, d.getX(), d.getY()));
        }

        DeviceLocation[] arrLocation = new DeviceLocation[list.size()];
        arrLocation = list.toArray(arrLocation);
        Arrays.sort(arrLocation);
        PointF[] tempSameValuePoints = new PointF[arrLocation.length];
        for (float v : ARR_DISTANCE_RATE) {

            //获取等高线的点
            for (int j = 0; j < arrLocation.length; ++j) {//收集最短半径和所有的点坐标
                DeviceLocation d = arrLocation[j];
                double distance = CommUtils.dbm2Distance(d.getWifiDevice().getRssi());
                float rate = (float) (v / distance);

                float x = d.getX() - cX;
                x *= rate;
                x += cX;
                float y = d.getY() - cY;
                y *= rate;
                y += cY;

                if (tempSameValuePoints[j] == null)
                    tempSameValuePoints[j] = new PointF(x, y);
                else
                    tempSameValuePoints[j].set(x, y);
            }

            //获取绘制贝塞尔曲线的点并增加辅助点
            ArrayList<PointF> arrPoints = new ArrayList();
            int addnum = 0;
            for (int i = 0, j; i < tempSameValuePoints.length; ++i) {
                j = i + 1;
                if (j == tempSameValuePoints.length)
                    j = 0;

                float angle1 = arrLocation[i].getAngle();
                float angle2 = arrLocation[j].getAngle();

                PointF f1 = tempSameValuePoints[i];
                PointF f2 = tempSameValuePoints[j];

                arrPoints.add(f1);

                if (angle2 > angle1) {
                    if (angle2 - angle1 < 45)
                        continue;
                } else if (angle2 + 360 - angle1 < 45) {
                    continue;
                }

                //add points
                double R1 = getDistanceIn2Points(cX, cY, f1.x, f1.y);
                double R2 = getDistanceIn2Points(cX, cY, f1.x, f2.y);

                if (angle2 > angle1)
                    addnum = Math.round((angle2 - angle1) / 45);
                else
                    addnum = Math.round((angle2 + 360 - angle1) / 45);

                if (i + 1 == tempSameValuePoints.length)
                    pushPointsToArray(cX, cY, R1, R2, angle1, angle2 + 360, addnum, arrPoints);
                else
                    pushPointsToArray(cX, cY, R1, R2, angle1, angle2, addnum, arrPoints);
            }


            Path path = createPathByBezier(arrPoints, cX, cY);
            arrayList.add(path);
        }

        return arrayList;
    }

    /**
     * -90~270
     */
    private static double getRelativeAngle(float cx, float cy, float dx, float dy) {

        float x = dx - cx;
        float y = dy - cy;

        double result = y / x;
        result = Math.atan(result);
        result = Math.toDegrees(result);

        if (x < 0) {
            result += 180;
        } else if (y < 0)
            result += 360;

        return result;
    }

    /**
     * @param arrPoints 顺时针旋转的坐标
     * @param cX        圆心坐标X
     * @param cY        圆心坐标Y
     * @return 生成连接每个点的贝塞尔曲线的path
     */
    private Path createPathByBezier(ArrayList<PointF> arrPoints, float cX, float cY) {
        //用贝塞尔曲线连接所有点并闭合
        Path path = new Path();
        path.moveTo(arrPoints.get(0).x, arrPoints.get(0).y);

        path.moveTo(arrPoints.get(1).x, arrPoints.get(1).y);
        int length = arrPoints.size();
        for (int i = 2; i < length + 2; i++) {

            PointF p1 = arrPoints.get((i - 2) % length);
            PointF p2 = arrPoints.get((i - 1) % length);
            PointF p3 = arrPoints.get(i % length);
            PointF p4 = arrPoints.get((i + 1) % length);

            float pX = p2.x + (p3.x - p1.x) / 5;
            float pY = p2.y + (p3.y - p1.y) / 5;
            float nX = p3.x - (p4.x - p2.x) / 5;
            float nY = p3.y - (p4.y - p2.y) / 5;
            path.cubicTo(pX, pY, nX, nY, p3.x, p3.y);
        }
        path.close();
        return path;
    }

    /**
     * 将增加的点推到arrPoints中
     *
     * @param arrPoints 不能为null，用来接收结果对象
     */
    private void pushPointsToArray(float cX, float cY, double radius1, double radius2, float angle1, float angle2, int addnum, ArrayList<PointF> arrPoints) {
        float perAngle = (angle2 - angle1) / (addnum + 1);
        double perRadius = (radius2 - radius1) / (addnum + 1);

        for (int j = 1; j < addnum + 1; j++) {//计算要添加的辅助点，渐进前后两个点之间的半径角度
            double Rt = perRadius * j + radius1;
            float angle = angle1 + j * perAngle;
            float x = (float) Math.cos(angle / 180 * Math.PI);
            x *= Rt;
            x += cX;
            float y = (float) Math.sin(angle / 180 * Math.PI);
            y *= Rt;
            y += cY;
            arrPoints.add(new PointF(x, y));
        }
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

    private void cleanPath() {
        if (mDrawableWave != null) {
            mDrawableWave.getBitmap().recycle();
            mDrawableWave = null;
            System.gc();
        }
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
    }

    private void initSketchMap(Context context) {
        mSketchMapDrawable = context.getDrawable(R.drawable.gradient_sketch_color);
        mStrIntensity = context.getResources().getStringArray(R.array.intensity);
        mRectStrSkechMap = new Rect();
        mPathPaint.getTextBounds(mStrIntensity[0], 0, mStrIntensity[0].length(), mRectStrSkechMap);
    }


}

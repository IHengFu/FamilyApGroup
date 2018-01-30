package com.changhong.wifiairscout.ui.view.map;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.changhong.wifiairscout.App;
import com.changhong.wifiairscout.R;
import com.changhong.wifiairscout.db.data.DeviceLocation;
import com.changhong.wifiairscout.preferences.Preferences;

import java.util.List;

/**
 * Created by fuheng on 2018/1/22.
 */

public class PointMap implements TypeMap {

    private final Context mContext;

    public PointMap(Context context) {
        mContext = context;
    }

    @Override
    public void refresh(ViewGroup viewGroup, List<DeviceLocation> list, float scale, int scrollX, int scrollY) {
        if (viewGroup.getChildCount() > 0)
            for (int i = 0; i < viewGroup.getChildCount(); ++i) {
                View view = viewGroup.getChildAt(i);
                DeviceLocation d = list.get(i);
                setDrawableAndStart(view, d);

//                ImageView imageview = view.findViewById(R.id.img_background);
//                if (imageview.getVisibility() != View.GONE)
//                    imageview.setVisibility(View.GONE);
                view.measure(
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

                float x = d.getX() * scale - scrollX;
                float y = d.getY() * scale - scrollY;
                float width = Math.max(view.getMeasuredWidth(), view.getMeasuredHeight());
                view.layout((int) (x - width / 2), (int) (y - width / 2),
                        (int) (x + width / 2), (int) (y + width / 2));
                view.postInvalidate();
            }
    }


    @Override
    public void clean(ViewGroup viewGroup) {
        if (viewGroup.getChildCount() > 0)
            for (int i = 0; i < viewGroup.getChildCount(); ++i) {
                View view = viewGroup.getChildAt(i);
                ImageView imageview = view.findViewById(R.id.img_background);
                Animatable drawable = (Animatable) imageview.getDrawable();
                drawable.stop();
                imageview.setVisibility(View.GONE);
            }
    }

    @Override
    public void drawContent(Canvas canvas) {
    }

    @Override
    public void drawSketchMap(Canvas canvas) {

    }

    @Override
    public void onLayout(boolean changed, int l, int t, int r, int b, float scale, int scrollX, int scrollY) {

    }

    @Override
    public void onAdd(ViewGroup group, View view, DeviceLocation d) {
        setDrawableAndStart(view, d);
    }


    @Override
    public void onRemove(View view, DeviceLocation d) {
        ImageView imageview = view.findViewById(R.id.img_background);
        Animatable drawable = (Animatable) imageview.getDrawable();
        drawable.stop();
        imageview.setVisibility(View.GONE);
//        if (view.getBackground() != null && view.getBackground() instanceof Animatable)
//            ((Animatable) view.getBackground()).stop();
    }

    private int getColorByRssi(int rssi) {
        float rate = rssi - App.MIN_RSSI;
        rate /= App.MAX_RSSI - App.MIN_RSSI;
//        return Color.HSVToColor(new float[]{rate * 120f, 1, 1});

        if (rssi > -Preferences.getIntance().getRssiLimitValue()) {
            return COLOR_STRONG;
        } else return COLOR_WEAK;

    }

    private void setDrawableAndStart(View view, DeviceLocation d) {
        ImageView imageview = view.findViewById(R.id.img_background);
        if (d.getWifiDevice() != null && d.getWifiDevice().getType() != App.TYPE_DEVICE_WIFI) {
            if (imageview.getVisibility() == View.GONE)
                imageview.setVisibility(View.VISIBLE);
            Animatable drawable = (Animatable) imageview.getDrawable();
            DrawableCompat.setTint(imageview.getDrawable(), getColorByRssi(d.getWifiDevice().getRssi()));
            if (!drawable.isRunning())
                drawable.start();

//            if (view.getBackground() != null && view.getBackground() instanceof Animatable) {
//                Animatable drawable = (Animatable) view.getBackground();
//                if (!drawable.isRunning())
//                    drawable.start();
//            } else {
//                Drawable drawable1 = mContext.getDrawable(R.drawable.animatied_vector_oval).mutate();
//                DrawableCompat.setTint(drawable1, getColorByRssi(d.getWifiDevice().getRssi()));
//                view.setBackground(drawable1);
//                Animatable drawable = (Animatable) drawable1;
//                drawable.start();
//            }
        } else if (view.getBackground() != null) {

            Animatable drawable = (Animatable) imageview.getDrawable();
            if (drawable.isRunning())
                drawable.stop();

            if (imageview.getVisibility() != View.GONE)
                imageview.setVisibility(View.GONE);

//            Animatable drawable = (Animatable) view.getBackground();
//            drawable.stop();
//            view.setBackground(null);
        }
    }
}

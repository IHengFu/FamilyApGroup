package com.changhong.wifiairscout.ui.view.map;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import com.changhong.wifiairscout.App;
import com.changhong.wifiairscout.R;
import com.changhong.wifiairscout.model.DeviceLocation;

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
                TextView view = (TextView) viewGroup.getChildAt(i);
                DeviceLocation d = list.get(i);

                if (d.getWifiDevice() == null) {
                    if (view.getBackground() != null) {
                        Animatable drawable = (Animatable) view.getBackground();
                        drawable.stop();
                        view.setBackground(null);
                    }
                } else {
                    if (view.getBackground() == null) {
                        view.setBackgroundResource(R.drawable.animatied_vector_oval);
                        AnimatedVectorDrawable drawable = (AnimatedVectorDrawable) view.getBackground();
                        DrawableCompat.setTint(drawable, getColorByRssi(d.getWifiDevice().getRssi()));
                        drawable.start();
                    }
                }
            }
    }


    @Override
    public void clean(ViewGroup viewGroup) {
        if (viewGroup.getChildCount() > 0)
            for (int i = 0; i < viewGroup.getChildCount(); ++i) {
                TextView view = (TextView) viewGroup.getChildAt(i);
                if (view.getBackground() != null) {
                    if (view.getBackground() != null && view.getBackground() instanceof AnimatedVectorDrawable) {
                        AnimatedVectorDrawable drawable1 = ((AnimatedVectorDrawable) view.getBackground());
                        drawable1.stop();
                    }
                    view.setBackground(null);
                }
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
        if (d.getWifiDevice() != null) {
            view.setBackgroundResource(R.drawable.animatied_vector_oval);
            Animatable drawable = (Animatable) view.getBackground();
            drawable.start();
        }
    }

    @Override
    public void onRemove(View view, DeviceLocation d) {
        if (d.getWifiDevice() != null)
            ((Animatable) view.getBackground()).stop();
    }

    private int getColorByRssi(int rssi) {
        float rate = rssi - App.MIN_RSSI;
        rate /= App.MAX_RSSI - App.MIN_RSSI;
        for (int i = 0; i < RATE_WAVE.length; i++) {
            if (rate < RATE_WAVE[i]) {
                return COLOR_PATH_RANGE[i] & 0xffffffff;
            }
        }

        return Color.GREEN;
    }
}

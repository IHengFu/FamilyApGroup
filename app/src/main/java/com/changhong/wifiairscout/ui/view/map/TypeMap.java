package com.changhong.wifiairscout.ui.view.map;

import android.graphics.Canvas;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import com.changhong.wifiairscout.db.data.DeviceLocation;

/**
 * Created by fuheng on 2018/1/18.
 */

public interface TypeMap {
    public static final int[] COLOR_PATH_RANGE = {0x11ff0000, 0x22ff0000, 0x33ffff00, 0x4488ff00, 0x5500ff00};

    public static final float[] RATE_WAVE = {0,
            .25f,
            .5f,
            .7f,
            .85f};

    public static final float[] ARR_DISTANCE_RATE = {32.229473369482086f,
            8.913532445726261f,
            2.465167821707151f,
            0.8816249605953462f,
            0.40771973258763594f};

    public static final int COLOR_STRONG = 0xff00dd00;
    public static final int COLOR_MEDIUM = 0xffFFAE00;
    public static final int COLOR_WEAK = 0xffdd5555;

//    static {
//        ARR_DISTANCE_RATE = new float[RATE_WAVE.length];
//        for (int i = 0; i < ARR_DISTANCE_RATE.length; i++) {
//            ARR_DISTANCE_RATE[i] = (float) CommUtils.dbm2Distance((App.MIN_RSSI - App.MAX_RSSI) * (1 - RATE_WAVE[i]) + App.MAX_RSSI);
//        }
//    }


    void refresh(ViewGroup viewGroup, List<DeviceLocation> list, float scale, int scrollX, int scrollY);

    void clean(ViewGroup viewGroup);

    void drawContent(Canvas canvas);

    /**
     * 绘制示意图
     */
    void drawSketchMap(Canvas canvas);

    void onLayout(boolean changed, int l, int t, int r, int b, float scale, int scrollX, int scrollY);

    void onAdd(ViewGroup group, View view, DeviceLocation d);

    void onRemove(View view, DeviceLocation d);
}

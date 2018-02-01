package com.changhong.wifiairscout.interfaces;

import android.view.View;

import com.changhong.wifiairscout.model.WifiDevice;

/**
 * Created by fuheng on 2018/1/27.
 */

public interface OnItemDragListener {
    void onMove(View view, WifiDevice device);

    void onDroped(View view, WifiDevice device);

    void onDelete(WifiDevice device);

    void onAdd(WifiDevice device);

}

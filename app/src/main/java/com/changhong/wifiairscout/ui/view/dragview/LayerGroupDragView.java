package com.changhong.wifiairscout.ui.view.dragview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import com.changhong.wifiairscout.model.WifiDevice;
import com.changhong.wifiairscout.ui.view.map.TypeMap;

/**
 * Created by fuheng on 2017/12/8.
 */
public class LayerGroupDragView extends FrameLayout {
    /**
     * 效果层
     */
    private View mLayerEffectMap;
    /**
     * 比例尺层
     */
    private View mLayerSketchMap;
    /**
     * 操作层
     */
    private View mLayerContral;

    private TypeMap mTypeMap;


    public LayerGroupDragView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initEffectLayer(context);
        initContralLayer(context);
        initLayerSketchMap(context);
    }

    private void initEffectLayer(Context context) {
        View view = new View(context);
        addView(view, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        view.setFocusable(false);
        view.setEnabled(false);
        view.setClickable(false);

        mLayerEffectMap = view;
    }

    private void initContralLayer(Context context) {
        View view = new View(context);
        addView(view, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        view.setFocusable(false);
        view.setEnabled(false);
        view.setClickable(false);

        mLayerContral = view;
    }

    private void initLayerSketchMap(Context context) {
        View view = new View(context);
        FrameLayout.LayoutParams param = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        param.gravity = Gravity.LEFT | Gravity.BOTTOM;
        addView(view, param);
        view.setFocusable(false);
        view.setEnabled(false);
        view.setClickable(false);
        mLayerSketchMap = view;
    }

    public void addDevice(WifiDevice device, int absX, int absY) {
        //TODO

    }

}
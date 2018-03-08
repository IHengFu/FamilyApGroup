package com.changhong.wifiairscout.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.changhong.wifiairscout.App;
import com.changhong.wifiairscout.R;
import com.changhong.wifiairscout.db.data.DeviceLocation;
import com.changhong.wifiairscout.db.data.ProgrammeGroup;
import com.changhong.wifiairscout.preferences.Preferences;
import com.changhong.wifiairscout.ui.view.map.TypeMap;
import com.changhong.wifiairscout.utils.UnitUtils;

import java.util.List;

/**
 * Created by fuheng on 2018/2/4.
 */

public class ProgrammeAdapter extends BaseExpandableListAdapter {

    private final LayoutInflater mInflater;

    private final Context mContext;
    private final List<ProgrammeGroup> mArrProgramme;
    private final List<List<DeviceLocation>> mArrLocation;

    private final String FORMATE_GROUP;

    private final String[] mArrStrIntensity;
    private final int limitValue;

    public ProgrammeAdapter(Context context, List<ProgrammeGroup> arrProgramme, List<List<DeviceLocation>> arrLocation) {
        mInflater = LayoutInflater.from(context);

        FORMATE_GROUP = "%s(" + context.getString(R.string.average) + ": %d)";

        mContext = context;

        mArrProgramme = arrProgramme;

        mArrLocation = arrLocation;

        mArrStrIntensity = context.getResources().getStringArray(R.array.intensity);

        limitValue = -Preferences.getIntance(context).getRssiLimitValue();
    }


    @Override
    public int getGroupCount() {
        if (mArrProgramme == null)
            return 0;
        return mArrProgramme.size();
    }

    @Override
    public int getChildrenCount(int i) {
        if (mArrLocation == null)
            return 0;
        return mArrLocation.get(i).size();
    }

    @Override
    public Object getGroup(int i) {
        if (mArrProgramme == null)
            return null;
        return mArrProgramme.get(i);
    }

    @Override
    public Object getChild(int groupId, int childId) {
        if (mArrLocation == null)
            return 0;
        return mArrLocation.get(groupId).get(childId);
    }

    @Override
    public long getGroupId(int i) {
        if (mArrProgramme == null)
            return 0;
        return mArrProgramme.get(i).getGroup();
    }

    @Override
    public long getChildId(int groupId, int childId) {
        if (mArrLocation == null)
            return 0;
        return mArrLocation.get(groupId).get(childId).getId();
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {

        if (view == null) {
            view = mInflater.inflate(android.R.layout.simple_list_item_1, viewGroup, false);
            view.setPadding(UnitUtils.dip2px(mContext, 30), view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom());
            view.setBackgroundColor(0x44aaaaaa);
        }

        ProgrammeGroup pg = (ProgrammeGroup) getGroup(i);

        TextView tv = (TextView) view;


        SpannableString spanText = new SpannableString(String.format(FORMATE_GROUP, pg.getName(), pg.getRssi()));
        int indexofmao = spanText.toString().lastIndexOf(':');
        spanText.setSpan(new RelativeSizeSpan(.7f), pg.getName().length(), spanText.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        spanText.setSpan(new ForegroundColorSpan(getIntensityColor(pg.getRssi())), indexofmao + 2, spanText.length() - 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        tv.setText(spanText);

        return view;
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {

        if (view == null)
            view = mInflater.inflate(android.R.layout.simple_list_item_2, viewGroup, false);
        try {
            DeviceLocation dl = (DeviceLocation) getChild(i, i1);

            TextView tv1 = view.findViewById(android.R.id.text1);
            SpannableString spanText = new SpannableString("<img/>");
            Drawable drawable = mContext.getDrawable(App.RESID_WIFI_DEVICE[dl.getType()]);
            spanText.setSpan(new ImageSpan(drawable), 0, 6, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            tv1.setText(spanText);
            DrawableCompat.setTint(drawable, Color.BLACK);
            float size = tv1.getPaint().getTextSize();
            drawable.setBounds(0, 0, (int) size, (int) size);
            tv1.append(dl.getDisplayName());
            if (dl.getType() != App.TYPE_DEVICE_WIFI) {
                spanText = new SpannableString("(" + getIntensityString(dl.getRssi()) + "  " + dl.getRssi() + ")");
                spanText.setSpan(new RelativeSizeSpan(.8f), 0, spanText.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                ForegroundColorSpan colorSpan = new ForegroundColorSpan(getIntensityColor(dl.getRssi()));
                spanText.setSpan(colorSpan, 1, 2, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                tv1.append(spanText);
            }

            TextView tv2 = view.findViewById(android.R.id.text2);
            tv2.setPadding((int) size, tv2.getPaddingTop(), tv2.getPaddingRight(), tv2.getPaddingBottom());
            tv2.setText(dl.getMac());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return false;
    }

    private String getIntensityString(int rssi) {
        if (rssi <= limitValue)
            return mArrStrIntensity[2];
        else if (rssi > (App.MAX_RSSI + limitValue) / 2)
            return mArrStrIntensity[0];
        else
            return mArrStrIntensity[1];
    }

    private int getIntensityColor(int rssi) {
        if (rssi <= limitValue)
            return TypeMap.COLOR_WEAK;
        else if (rssi > (App.MAX_RSSI + limitValue) / 2)
            return TypeMap.COLOR_STRONG;
        else
            return TypeMap.COLOR_MEDIUM;
    }
}

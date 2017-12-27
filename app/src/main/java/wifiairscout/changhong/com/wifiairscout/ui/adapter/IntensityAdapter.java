package wifiairscout.changhong.com.wifiairscout.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import wifiairscout.changhong.com.wifiairscout.App;
import wifiairscout.changhong.com.wifiairscout.model.DeviceLocation;

/**
 * Created by fuheng on 2017/12/22.
 */

public class IntensityAdapter extends BaseAdapter {

    private final Context mContext;
    private final List<DeviceLocation> mArrDeviceLocation;

    public static final float SIZE_CHILD = 24;

    public IntensityAdapter(Context context, List<DeviceLocation> list) {
        this.mContext = context;
        this.mArrDeviceLocation = list;
    }

    @Override
    public int getCount() {
        if (mArrDeviceLocation == null)
            return 0;
        return mArrDeviceLocation.size();
    }

    @Override
    public Object getItem(int i) {


        if (mArrDeviceLocation == null)
            return null;
        return mArrDeviceLocation.get(i);
    }

    @Override
    public long getItemId(int i) {
        if (mArrDeviceLocation == null)
            return 0;
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        DeviceLocation d = mArrDeviceLocation.get(i);

        TextView child = new TextView(mContext);
        child.setGravity(Gravity.CENTER);
        Drawable drawable = mContext.getResources().getDrawable(App.RESID_WIFI_DEVICE[d.getType()]).mutate();
        drawable.setBounds(0, 0,
                (int) (SIZE_CHILD * drawable.getIntrinsicWidth() / drawable.getIntrinsicHeight()), (int) SIZE_CHILD);
        drawable = DrawableCompat.wrap(drawable);
        child.setCompoundDrawables(null, drawable, null, null);
        if (d.getWifiDevice() == null) {
            child.setText(d.getMac());
            child.setEnabled(false);
            drawable.setTint(Color.DKGRAY);
        } else {
            child.setText(d.getWifiDevice().getName());
        }
        child.layout((int) (d.getX() - SIZE_CHILD / 2), (int) (d.getY() - SIZE_CHILD / 2),
                (int) (d.getX() + SIZE_CHILD), (int) (d.getY() + SIZE_CHILD));
        return child;
    }
}

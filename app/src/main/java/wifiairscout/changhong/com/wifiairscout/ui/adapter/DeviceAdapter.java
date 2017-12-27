package wifiairscout.changhong.com.wifiairscout.ui.adapter;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import wifiairscout.changhong.com.wifiairscout.App;
import wifiairscout.changhong.com.wifiairscout.R;
import wifiairscout.changhong.com.wifiairscout.model.WifiDevice;

/**
 * Created by fuheng on 2017/12/8.
 */

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.WifiDeviceHolder> {
    private List<WifiDevice> devices;

    private OnItemClickListener itemClickListener;

    public DeviceAdapter(List<WifiDevice> devices) {
        this.devices = devices;
    }

    @Override
    public WifiDeviceHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        WifiDeviceHolder holder = new WifiDeviceHolder(LayoutInflater.from(
                parent.getContext()).inflate(R.layout.item_device, parent,
                false));
        return holder;
    }

    @Override
    public void onBindViewHolder(final WifiDeviceHolder holder, final int position) {
        final WifiDevice device = devices.get(position);
        holder.icon.setImageResource(App.RESID_WIFI_DEVICE[device.getType()]);
        holder.tv.setText(device.toString());
        holder.tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (itemClickListener != null)
                    itemClickListener.onItemClick(device, position);
            }
        });
        ((View) holder.tv.getParent()).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (itemClickListener != null)
                    return itemClickListener.onItemLongClick(device, position);
                return false;
            }
        });

    }

    @Override
    public int getItemCount() {
        if (devices == null)
            return 0;
        return devices.size();
    }

    class WifiDeviceHolder extends RecyclerView.ViewHolder {

        private final TextView tv;
        private final ImageView icon;

        public WifiDeviceHolder(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.text);
            icon = (ImageView) itemView.findViewById(R.id.icon);
        }
    }

    //点击事件接口
    public interface OnItemClickListener {
        void onItemClick(WifiDevice device, int position);

        boolean onItemLongClick(WifiDevice device, int position);
    }

    //设置点击事件的方法
    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }
}
package com.changhong.wifiairscout.model;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;

import com.changhong.wifiairscout.App;
import com.changhong.wifiairscout.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fuheng on 2018/3/12.
 */

public class FalseData {
    private Context mContext;

    public FalseData(Context context) {
        mContext = context;
    }

    public final List<WifiDevice> getDevices() {
        List<WifiDevice> result = new ArrayList<WifiDevice>(6);
        for (int i = 0; i < 4; i++) {
            String mac = "";
            for (int j = 0; j < 5; j++) {
                mac += String.format("%02x", (byte) (Math.random() * 0xff)).toUpperCase();
                mac += ":";
            }
            mac += String.format("%02x", (byte) (Math.random() * 0xff)).toUpperCase();

            WifiDevice d = new WifiDevice(App.TYPE_DEVICE_CLIENT, WifiDevice.Companion.toStringIp((int) (Math.random() * Integer.MAX_VALUE)),
                    mac, mContext.getString(R.string.client) + i);
            d.setRssi((byte) Math.min((Math.random() * 80 - 120), App.MAX_RSSI));
            result.add(d);
        }
        {

            WifiInfo wifiinfo = App.sInstance.getWifiInfo();
            int frequency = wifiinfo.getFrequency();

            if (frequency < 5000) {
                App.sInstance.setCurWlanIdx((byte) 1);
                App.sInstance.setCurChannel((byte) ((frequency - 2407) / 5));
                if ((frequency - 2407) % 5 > 3)
                    App.sInstance.setCurChannel((byte) (App.sInstance.getCurChannel() + 1));
            } else {
                int[] arrFrequency = mContext.getResources().getIntArray(R.array.channel_5g_cn_frequency);
                int min = Integer.MAX_VALUE;
                for (int i = 0; i < arrFrequency.length; i++) {

                    if (Math.abs(frequency - arrFrequency[i]) < min) {
                        min = Math.abs(frequency - arrFrequency[i]);
                        App.sInstance.setCurChannel((byte) i);
                    }
                }
                App.sInstance.setCurWlanIdx((byte) 0);
            }

            result.add(0, new WifiDevice(App.TYPE_DEVICE_PHONE, WifiDevice.Companion.toStringIp(wifiinfo.getIpAddress()), wifiinfo.getMacAddress(),
                    mContext.getString(R.string.my_phone)));
            result.get(0).setRssi((byte) wifiinfo.getRssi());

            WifiDevice wc = new WifiDevice(App.TYPE_DEVICE_CONNECT, "127.00.00.1", "ff:ff:ff:ff:ff:ff", "中继器");
            result.add(0, wc);

            DhcpInfo dhcp = App.sInstance.getDhcpInfo();
            result.add(0, new WifiDevice(App.TYPE_DEVICE_WIFI, WifiDevice.Companion.toStringIp(dhcp.ipAddress),
                    wifiinfo.getBSSID(), mContext.getString(R.string.wifi)));
            result.get(0).setRssi((byte) 100);

            ArrayList<DualBandInfo> arrDualBInfo = new ArrayList<DualBandInfo>();
            arrDualBInfo.add(new DualBandInfo((byte) 0, (byte) 44, (byte) 0, (byte) 0, "test1", (byte) 0, (byte) 0, null));
            arrDualBInfo.add(new DualBandInfo((byte) 1, (byte) 12, (byte) 0, (byte) 0, "test1", (byte) 0, (byte) 0, null));
            result.get(0).setDual_band((byte) 1);
            result.get(0).setArrayDualBandInfo(arrDualBInfo);
        }
        return result;
    }
}

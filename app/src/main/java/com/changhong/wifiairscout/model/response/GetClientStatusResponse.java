package com.changhong.wifiairscout.model.response;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import com.changhong.wifiairscout.model.WifiDevice;

/**
 * 注册消息响应信息
 * Created by fuheng on 2017/12/14.
 */

public class GetClientStatusResponse extends BaseResponse {


    private ArrayList<WifiDevice> devices;

    public GetClientStatusResponse(short status) {
        super(status);
    }

    public GetClientStatusResponse(byte[] data) {
        super(data);
    }

    public GetClientStatusResponse() {
        super();
    }

    public void init(byte[] data) {
        super.init(data);
        int index = 2;
        byte amount = data[index++];

        if (amount == 0)
            return;
        devices = new ArrayList<>();
        for (int i = 0; i < amount; ++i) {
            devices.add(getWifiDevice(data, index));
            index += 6 + 2;
        }
    }

    public GetClientStatusResponse(byte[] data, ArrayList<WifiDevice> devices) {
        super(data);
        this.devices = devices;
    }

    private WifiDevice getWifiDevice(byte[] data, int offset) {
        StringBuilder sb = new StringBuilder();
        int index = 0;
        for (int i = 0; i < 6; ++i) {
            sb.append(String.format("%02x", data[offset + index++])).append(':');
        }
        sb.deleteCharAt(sb.length() - 1);

        String mac = sb.toString();

        byte rssi = data[offset + 6];
        boolean wlan_idx = data[offset + 7] == 0;//无线radio索引, 0: 5G; 1: 2.4G; 2: 2.4G和5G

        return new WifiDevice(rssi, mac, wlan_idx);
    }

    public ArrayList<WifiDevice> getDevices() {
        return devices;
    }

    @Override
    public String toString() {
        return "GetClientResponse{" +
                "devices=" + devices +
                "} " + super.toString();
    }

}

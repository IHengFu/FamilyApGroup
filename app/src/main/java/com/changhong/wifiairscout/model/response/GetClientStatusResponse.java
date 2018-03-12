package com.changhong.wifiairscout.model.response;

import com.changhong.wifiairscout.App;
import com.changhong.wifiairscout.model.WifiDevice;

import java.util.ArrayList;

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
            index += 6 + 2 + 37;
        }
    }

    public GetClientStatusResponse(byte[] data, ArrayList<WifiDevice> devices) {
        super.init(data);
        this.devices = devices;
    }

    private WifiDevice getWifiDevice(byte[] data, int offset) {
        StringBuilder sb = new StringBuilder();
        int index = 0;

        String mac = parseToMacAddress(data, offset);

        index += 6;

        String name = getStringInData(data, offset + index, 32);

        index += 32;

        String IP = parseToIPv4(data, offset + index);
        index += 4;

        byte dual_band = data[offset + index++];

        byte rssi = data[offset + index++];

        byte wlan_idx = data[offset + index++];//无线radio索引, 0: 5G; 1: 2.4G; 2: 2.4G和5G

        WifiDevice result = new WifiDevice(App.TYPE_DEVICE_CLIENT, IP, mac, name);
        result.setRssi(rssi);
        result.setWlan_idx(wlan_idx);
        result.setDual_band(dual_band);

        return result;
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

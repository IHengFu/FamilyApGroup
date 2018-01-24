package com.changhong.wifiairscout.model.response;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import com.changhong.wifiairscout.App;
import com.changhong.wifiairscout.model.WifiDevice;

/**
 * 注册消息响应信息
 * Created by fuheng on 2017/12/14.
 */

public class GetClientResponse extends BaseResponse {


    private ArrayList<WifiDevice> devices;

    public GetClientResponse(byte[] data) {
        super(data);
        int index = 2;
        byte amount = data[index++];

        if (amount == 0)
            return;
        devices = new ArrayList<>();
        for (int i = 0; i < amount; ++i) {
            devices.add(getWifiDevice(data, index));
            index += 6 + 32 + 4;
        }
    }

    private WifiDevice getWifiDevice(byte[] data, int offset) {
        StringBuilder sb = new StringBuilder();
        int index = 0;
        for (int i = 0; i < 6; ++i) {
            sb.append(String.format("%02x", data[offset + index++])).append(':');
        }
        sb.deleteCharAt(sb.length() - 1);

        String mac = sb.toString();

        String name = null;
        try {
            int first0 = 32;
            for (int i = 0; i < 32; ++i)
                if (data[offset + index + i] == 0) {
                    first0 = i;
                    break;
                }
            name = new String(data, offset + index, first0, App.CHARSET).trim();
        } catch (UnsupportedEncodingException e) {
            name = new String(data, offset, 32);
            e.printStackTrace();
        }
        index += 32;

        int ip = data[offset + index];
        ip = data[offset + index + 1] << 8 | ip;
        ip = data[offset + index + 2] << 16 | ip;
        ip = data[offset + index + 3] << 24 | ip;

        return new WifiDevice(App.TYPE_DEVICE_CLIENT, WifiDevice.Companion.toStringIp(ip), mac, name, (byte) 0);
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

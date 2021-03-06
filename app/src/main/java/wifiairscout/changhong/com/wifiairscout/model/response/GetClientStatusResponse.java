package wifiairscout.changhong.com.wifiairscout.model.response;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import wifiairscout.changhong.com.wifiairscout.model.WifiDevice;

/**
 * 注册消息响应信息
 * Created by fuheng on 2017/12/14.
 */

public class GetClientStatusResponse extends BaseResponse {


    private ArrayList<WifiDevice> devices;

    public GetClientStatusResponse(byte[] data) {
        super(data);
        int index = 2;
        byte amount = data[index++];

        if (amount == 0)
            return;
        devices = new ArrayList<>();
        for (int i = 0; i < amount; ++i) {
            devices.add(getWifiDevice(data, index));
            index += 6 + 1;
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

        return new WifiDevice(rssi, mac);
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

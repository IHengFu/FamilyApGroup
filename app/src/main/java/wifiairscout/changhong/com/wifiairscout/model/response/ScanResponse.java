package wifiairscout.changhong.com.wifiairscout.model.response;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import wifiairscout.changhong.com.wifiairscout.App;
import wifiairscout.changhong.com.wifiairscout.model.WifiDevice;

/**
 * 扫描结果
 * Created by fuheng on 2018/1/12
 */

public class ScanResponse extends BaseResponse {

    private List<WifiDevice> mListAp;

    public ScanResponse(byte[] data) {
        super(data);
        byte amount = data[2];

        if (amount == 0)
            return;
        int offset = 3;
        mListAp = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            mListAp.add(getWifiDevice(data, offset));
            offset += 42;
        }


    }

    private WifiDevice getWifiDevice(byte[] data, int offset) {
        int index = 0;

        String name;
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

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; ++i) {
            sb.append(String.format("%02x", data[offset + index++])).append(':');
        }
        sb.deleteCharAt(sb.length() - 1);

        String mac = sb.toString();

        WifiDevice ap = new WifiDevice(App.TYPE_DEVICE_WIFI, WifiDevice.Companion.toStringIp(0), mac, name, data[offset + index++]);
        ap.setRssi(data[offset + index++]);

        ap.setEncryptType(data[offset + index++]);
        ap.setCipher(data[offset + index++]);

        return ap;
    }

}

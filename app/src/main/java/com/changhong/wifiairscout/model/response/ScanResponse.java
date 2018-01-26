package com.changhong.wifiairscout.model.response;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.changhong.wifiairscout.App;
import com.changhong.wifiairscout.model.ScanItem;
import com.changhong.wifiairscout.model.WifiDevice;

/**
 * 扫描结果
 * Created by fuheng on 2018/1/12
 */

public class ScanResponse extends BaseResponse {


    private List<ScanItem> mListAp;

    public ScanResponse(short status) {
        super(status);
    }

    public ScanResponse(byte[] data) {
        super(data);
    }

    public ScanResponse() {
        super();
    }

    public void init(byte[] data) {
        super.init(data);
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

    private ScanItem getWifiDevice(byte[] data, int offset) {
        int index = 0;

        String ssid = getStringInData(data, offset + index, 32);
        index += 32;

        String bssid = parseToMacAddress(data, offset + index);
        index += 6;

        byte channel = data[offset + index++];

        byte rssi = data[offset + index++];
        byte encrypt = data[offset + index++];
        byte cipher = data[offset + index++];

        ScanItem ap = new ScanItem();

        ap.setSsid(ssid);
        ap.setBssid(bssid);
        ap.setChannel(channel);
        ap.setRssi(rssi);
        ap.setEncrypt(encrypt);
        ap.setCipher(cipher);

        return ap;
    }

    public List<ScanItem> getListAp() {
        return mListAp;
    }

    @Override
    public String toString() {
        return "ScanResponse{" +
                "mListAp=" + mListAp +
                "} " + super.toString();
    }
}

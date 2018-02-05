package com.changhong.wifiairscout.model.response;

import java.io.UnsupportedEncodingException;

import com.changhong.wifiairscout.App;
import com.changhong.wifiairscout.model.WifiDevice;

/**
 * Created by fuheng on 2017/12/18.
 */

public class GetMasterResponse extends BaseResponse {
    private WifiDevice master;

    public GetMasterResponse(short status) {
        super(status);
    }

    public GetMasterResponse(byte[] data) {
        super(data);
    }

    public GetMasterResponse() {
        super();
    }

    public void init(byte[] data) {
        super.init(data);

        master = getWifiDevice(data, 2);
    }

    private WifiDevice getWifiDevice(byte[] data, final int offset) {

        int index = 0;

        String name = getStringInData(data, 0, 32);
        index += 32;


        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 4; ++i) {
            sb.append(data[offset + index++] & 0xff).append(':');
        }
        sb.deleteCharAt(sb.length() - 1);

        String ip = sb.toString();

        WifiDevice wifidevice = new WifiDevice(App.TYPE_DEVICE_WIFI, ip, App.sInstance.getMasterMac(), name, data[index]);
        wifidevice.setDual_band(data[index++]);
        wifidevice.setWlan_idx(data[index++]);
        wifidevice.setChannel(data[index++]);
        wifidevice.setBound(data[index++]);
        wifidevice.setSideband(data[index++]);

        wifidevice.setSsid(getStringInData(data, index, 32));
        index += 32;

        wifidevice.setEncrypt(data[index++]);
        wifidevice.setCipher(data[index++]);

        byte[] Key = new byte[64];
        System.arraycopy(data, index, Key, 0, Key.length);

        wifidevice.setKey(Key);


        return wifidevice;
    }

    public WifiDevice getMaster() {
        return master;
    }

    @Override
    public String toString() {
        return "GetMasterResponse{" +
                "master=" + master +
                "} " + super.toString();
    }
}

package com.changhong.wifiairscout.model.response;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.ArrayList;

import com.changhong.wifiairscout.App;
import com.changhong.wifiairscout.model.DualBandInfo;
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

        int index = offset;

        String name = getStringInData(data, index, 32);
        index += 32;

        String ip = parseToIPv4(data, index);
        index += 4;

        byte dual_band = data[index++];

        WifiDevice wifidevice = new WifiDevice(App.TYPE_DEVICE_WIFI, ip, App.sInstance.getMasterMac(), name);
        wifidevice.setDual_band(dual_band);

        ArrayList<DualBandInfo> arrDualBand = new ArrayList<>();

        for (int i = 0; i < (dual_band == 1 ? 2 : 1); ++i) {
            DualBandInfo item = new DualBandInfo();
            item.setWlan_idx(data[index++]);
            item.setChannel(data[index++]);
            item.setBound(data[index++]);
            item.setSideband(data[index++]);

            item.setSsid(getStringInData(data, index, 33));
            index += 33;

            item.setEncrypt(data[index++]);
            item.setCipher(data[index++]);

            byte[] Key = new byte[64];
            System.arraycopy(data, index, Key, 0, Key.length);
            item.setKey(Key);
            index += 64;

            arrDualBand.add(item);
        }

        wifidevice.setArrayDualBandInfo(arrDualBand);


//        wifidevice.setWlan_idx(data[index++]);
//        wifidevice.setBound(data[index++]);
//        wifidevice.setSideband(data[index++]);
//
//        wifidevice.setSsid(getStringInData(data, index, 32));
//        index += 32;
//
//        wifidevice.setEncrypt(data[index++]);
//        wifidevice.setCipher(data[index++]);
//
//        byte[] Key = new byte[64];
//        System.arraycopy(data, index, Key, 0, Key.length);
//
//        wifidevice.setKey(Key);


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

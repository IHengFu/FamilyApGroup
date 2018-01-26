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

        String name = null;
        {
            int first0 = 32;
            for (int i = 0; i < 32; ++i)
                if (data[offset + index + i] == 0) {
                    first0 = i;
                    break;
                }
            try {
                name = new String(data, offset + index, first0, App.CHARSET).trim();
            } catch (UnsupportedEncodingException e) {
                name = new String(data, offset, first0);
                e.printStackTrace();
            }
            index += 32;
        }


        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 4; ++i) {
            sb.append(String.format("%02x", data[offset + index++])).append(':');
        }
        sb.deleteCharAt(sb.length() - 1);

        String ip = sb.toString();

        WifiDevice wifidevice = new WifiDevice(App.TYPE_DEVICE_WIFI, ip, App.sInstance.getMasterMac(), name, data[index]);
        wifidevice.setDual_band(data[index++]);
        wifidevice.setWlan_idx(data[index++]);
        wifidevice.setChannel(data[index++]);
        wifidevice.setBound(data[index++]);
        wifidevice.setSideband(data[index++]);

        try {
            int first0 = 32;
            for (int i = 0; i < 32; ++i)
                if (data[index + i] == 0) {
                    first0 = i;
                    break;
                }
            wifidevice.setSsid(new String(data, index, first0, App.CHARSET).trim());
        } catch (UnsupportedEncodingException e) {
            wifidevice.setSsid(new String(data, index, 32).trim());
            e.printStackTrace();
        } finally {
            index += 32;
        }
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

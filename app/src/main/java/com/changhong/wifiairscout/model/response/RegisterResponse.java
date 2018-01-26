package com.changhong.wifiairscout.model.response;

import com.changhong.wifiairscout.App;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * 注册消息响应信息
 * Created by fuheng on 2017/12/14.
 */

public class RegisterResponse extends BaseResponse {

    private short keepalive_interval;
    private short max_msg_body_len;


    private byte channel;

    private byte dual_band;//	1	1:双频; 0:单频
    private byte wlan_idx;//	1	无线radio索引; 0: 5G; 1: 2.4G;
    private byte bound;//1	0: 20MHz; 1: 40MHz; 2: 80MHz
    private byte sideband;//	1	0: 高; 1: 低
    private String Ssid;//	32	无线名称
    private byte encrypt;//	1	0:disabled; 1:wep; 2:wpa; 4:wpa2; 6:wp2_mixed; 7:wapi
    private byte cipher;//	1	1:tkip; 2:aes; 3:mixed
    private byte[] Key;//	64	密钥

    public RegisterResponse(short status) {
        super(status);
    }

    public RegisterResponse(byte[] data) {
        super(data);
    }

    public RegisterResponse() {
        super();
    }

    public void init(byte[] data) {
        super.init(data);
        keepalive_interval = (short) (data[2] << 8 & data[3]);
        max_msg_body_len = (short) (data[4] << 8 & data[5]);
        dual_band = data[6];
        wlan_idx = data[7];
        channel = data[8];
        bound = data[9];
        sideband = data[10];

        int index = 11;
        try {
            int first0 = 32;
            for (int i = 0; i < 32; ++i)
                if (data[index + i] == 0) {
                    first0 = i;
                    break;
                }
            Ssid = new String(data, index, first0, App.CHARSET).trim();
        } catch (UnsupportedEncodingException e) {
            Ssid = new String(data, index, 32).trim();
            e.printStackTrace();
        } finally {
            index += 32;
        }

        encrypt = data[index++];
        cipher = data[index++];

        Key = new byte[64];
        System.arraycopy(data, index, Key, 0, Key.length);
    }

    public short getKeepalive_interval() {
        return keepalive_interval;
    }

    public short getMax_msg_body_len() {
        return max_msg_body_len;
    }

    public byte getChannel() {
        return channel;
    }

    @Override
    public String toString() {
        return "RegisterResponse{" +
                "keepalive_interval=" + keepalive_interval +
                ", max_msg_body_len=" + max_msg_body_len +
                ", channel=" + channel +
                ", dual_band=" + dual_band +
                ", wlan_idx=" + wlan_idx +
                ", bound=" + bound +
                ", sideband=" + sideband +
                ", Ssid='" + Ssid + '\'' +
                ", encrypt=" + encrypt +
                ", cipher=" + cipher +
                ", Key=" + Arrays.toString(Key) +
                "} " + super.toString();
    }
}

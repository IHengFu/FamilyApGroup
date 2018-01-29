package com.changhong.wifiairscout.model.response;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 注册消息响应信息
 * Created by fuheng on 2017/12/14.
 */

public class RegisterResponse extends BaseResponse {

    private short keepalive_interval;
    private short max_msg_body_len;


    private byte dual_band;//	1	1:双频; 0:单频

    private List<WlanIndexObject> wlanCondition;

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

        int index = 7;
        wlanCondition = new ArrayList<>();
        for (int i = 0; i <= dual_band; ++i) {
            wlanCondition.add(getWlanIndexObject(data, 7 + i * 103));
        }


    }

    private WlanIndexObject getWlanIndexObject(byte[] data, int index) {
        WlanIndexObject obj = new WlanIndexObject();


        obj.wlan_idx = data[index++];
        obj.channel = data[index++];
        obj.bound = data[index++];
        obj.sideband = data[index++];
        obj.Ssid = getStringInData(data, index, 33);
        index += 33;

/*00, 00,
01, 2c,
 0f, a0,
 01,


 00,
  2c,
  02,
   01,
   43, 48, 4d, 61, 73, 74, 65, 72,
    41, 50, 35, 47, 00, 00, 00, 00,
    02, 00, 00, 00, 00, 00, 00, 00,
     00, 00, 00, 00, 06, 00, 00, 00,

     03,
      06,

      03, 31, 32, 33, 34, 35, 36, 37,
       38, 00, 12, 40, 00, c0, 17, 40,
       00, c4, 2d, 43, 00, 00, 00, 00,
        00, 00, 00, 00, 00, 00, 00, 00,
        00, 43, 48, 4d, 61, 73, 74, 65,
        72, 41, 50, 35, 47, 00, 00, 00,
        00, 02, 00, 00, 00, 00, 00, 00,
        00, 00, 00, 00, 00, 06, 00, 00,

         00,
          01,
           05,
            01,
             00, 43, 48, 4d, 61, 73, 74, 65, 72, 41, 50, 32, 47, 00, 00, 00, 00, 02, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 06, 00, 00, 00, 03, 06, 03, 31, 32, 33, 34, 35, 36, 37, 38, 00, 12, 40, 00, c0, 17, 40, 00, c4, 2d, 43, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 43, 48, 4d, 61, 73, 74, 65, 72, 41, 50, 32, 47, 00, 00, 00, 00, 02, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 06, 00, 00, 00*/
        obj.encrypt = data[index++];
        obj.cipher = data[index++];

        obj.Key = new byte[64];
        System.arraycopy(data, index, obj.Key, 0, obj.Key.length);

        return obj;
    }

    public short getKeepalive_interval() {
        return keepalive_interval;
    }

    public short getMax_msg_body_len() {
        return max_msg_body_len;
    }

    public void setKeepalive_interval(short keepalive_interval) {
        this.keepalive_interval = keepalive_interval;
    }

    public void setMax_msg_body_len(short max_msg_body_len) {
        this.max_msg_body_len = max_msg_body_len;
    }


    public byte getDual_band() {
        return dual_band;
    }

    public void setDual_band(byte dual_band) {
        this.dual_band = dual_band;
    }

    public List<WlanIndexObject> getWlanCondition() {
        return wlanCondition;
    }

    public void setWlanCondition(List<WlanIndexObject> wlanCondition) {
        this.wlanCondition = wlanCondition;
    }

    public byte getCurrentWlanIdx(byte wlan_idx) {

        for (WlanIndexObject wlanIndexObject : wlanCondition) {
            if (wlanIndexObject.wlan_idx == wlan_idx) {
                return wlanIndexObject.channel;
            }
        }

        return 0;
    }

    @Override
    public String toString() {
        return "RegisterResponse{" +
                "keepalive_interval=" + keepalive_interval +
                ", max_msg_body_len=" + max_msg_body_len +
                ", dual_band=" + dual_band +
                ", wlanCondition=" + wlanCondition +
                "} " + super.toString();
    }

    public class WlanIndexObject {
        private byte wlan_idx;//	1	无线radio索引; 0: 5G; 1: 2.4G;
        private byte bound;//1	0: 20MHz; 1: 40MHz; 2: 80MHz
        private byte sideband;//	1	0: 高; 1: 低
        private String Ssid;//	32	无线名称
        private byte encrypt;//	1	0:disabled; 1:wep; 2:wpa; 4:wpa2; 6:wp2_mixed; 7:wapi
        private byte cipher;//	1	1:tkip; 2:aes; 3:mixed
        private byte[] Key;//	64	密钥
        private byte channel;

        public byte getWlan_idx() {
            return wlan_idx;
        }

        public void setWlan_idx(byte wlan_idx) {
            this.wlan_idx = wlan_idx;
        }

        public byte getBound() {
            return bound;
        }

        public void setBound(byte bound) {
            this.bound = bound;
        }

        public byte getSideband() {
            return sideband;
        }

        public void setSideband(byte sideband) {
            this.sideband = sideband;
        }

        public String getSsid() {
            return Ssid;
        }

        public void setSsid(String ssid) {
            Ssid = ssid;
        }

        public byte getEncrypt() {
            return encrypt;
        }

        public void setEncrypt(byte encrypt) {
            this.encrypt = encrypt;
        }

        public byte getCipher() {
            return cipher;
        }

        public void setCipher(byte cipher) {
            this.cipher = cipher;
        }

        public byte[] getKey() {
            return Key;
        }

        public void setKey(byte[] key) {
            Key = key;
        }

        public void setChannel(byte channel) {
            this.channel = channel;
        }

        public byte getChannel() {
            return channel;
        }

        @Override
        public String toString() {
            return "WlanIndexObject{" +
                    "wlan_idx=" + wlan_idx +
                    ", bound=" + bound +
                    ", sideband=" + sideband +
                    ", Ssid='" + Ssid + '\'' +
                    ", encrypt=" + encrypt +
                    ", cipher=" + cipher +
                    ", Key=" + Arrays.toString(Key) +
                    ", channel=" + channel +
                    '}';
        }
    }
}

package wifiairscout.changhong.com.wifiairscout.model;

import android.net.wifi.WifiInfo;

import java.io.UnsupportedEncodingException;

import wifiairscout.changhong.com.wifiairscout.App;

/**
 * Created by fuheng on 2017/12/13.
 */

public class MessageDataFactory {
    private static final String TAG = "MessageDataFactory";

    /**
     * 设备注册
     */
    public static final MessageData getRegisterMessage() {
        WifiInfo info = App.sInstance.getWifiInfo();
        String mac = info.getMacAddress();
        int IP = info.getIpAddress();
        MessageData messageData = new MessageData(false, mac, false, false, System.currentTimeMillis());
        messageData.setMsgId((short) 1);

        byte[] msgbody = new byte[37];
        msgbody[0] = (byte) 0xf4;

        byte[] name = null;
        try {
            name = android.os.Build.MODEL.getBytes(App.CHARSET);
        } catch (UnsupportedEncodingException e) {
            name = android.os.Build.MODEL.getBytes();
        }
        for (int i = 0; i < 31; ++i)
            if (i < name.length)
                msgbody[i + 1] = name[i];

        byte[] ipdata = WifiDevice.Companion.to4ByteIp(IP);
        System.arraycopy(ipdata, 0, msgbody, 33, ipdata.length);

        messageData.setMsgBody(msgbody);
        return messageData;
    }

    /**
     * 心跳保活
     */
    public static final MessageData getAliveMessage() {
        WifiInfo info = App.sInstance.getWifiInfo();
        String mac = info.getMacAddress();
        int IP = info.getIpAddress();
        MessageData messageData = new MessageData(false, mac, false, true, System.currentTimeMillis());
        messageData.setMsgId((short) 0);
        return messageData;
    }

    /**
     * @param clientMac 指定Client信息，若全为ff，则返回所有clientMac的设备信息
     */
    public static final MessageData getClientInfo(byte[] clientMac) {
        WifiInfo info = App.sInstance.getWifiInfo();
        String mac = info.getMacAddress();
        int IP = info.getIpAddress();
        MessageData messageData = new MessageData(true, mac, false, true, System.currentTimeMillis());
        messageData.setMsgId((short) 2);
        messageData.setMsgBody(clientMac);
        return messageData;
    }

    /**
     * Client信息
     */
    public static final MessageData getAllClientInfo() {
        byte[] data = new byte[6];
        for (int i = 0; i < data.length; i++) {
            data[i] = -1;
        }
        return getClientInfo(data);
    }

    /**
     * @param clientMac 指定Client信息，若全为ff，则返回所有clientMac的设备状态
     */
    public static final MessageData getClientStatus(byte[] clientMac) {
        WifiInfo info = App.sInstance.getWifiInfo();
        String mac = info.getMacAddress();
        int IP = info.getIpAddress();
        MessageData messageData = new MessageData(true, mac, false, true, System.currentTimeMillis());
        messageData.setMsgId((short) 3);
        messageData.setMsgBody(clientMac);
        return messageData;
    }

    /**
     * Client状态
     */
    public static final MessageData getAllClientStatus() {
        byte[] data = new byte[6];
        for (int i = 0; i < data.length; i++) {
            data[i] = -1;
        }
        return getClientStatus(data);
    }

    public static final MessageData getMasterInfo(String mac) {
        MessageData messageData = new MessageData(true, mac, false, true, System.currentTimeMillis());
        messageData.setMsgId((short) 4);
        messageData.setMsgBody(WifiDevice.Companion.to6ByteMac(mac));
        return messageData;
    }

}

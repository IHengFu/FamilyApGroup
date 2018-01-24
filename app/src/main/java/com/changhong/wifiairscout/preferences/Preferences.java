package com.changhong.wifiairscout.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.changhong.wifiairscout.App;

/**
 * Created by fuheng on 2017/12/12.
 */

public class Preferences {
    private static final String SETTING = "setting";
    private final SharedPreferences sharedPreferences;

    private static Preferences sIntance;

    public static final String KEY_SERVICE_IP = "server ip";
    public static final String KEY_SERVICE_PORT = "server port";
    public static final String KEY_SEND_PORT = "send port";
    public static final String KEY_RECEIVE_PORT = "receive port";

    public static final String KEY_KEEP_ALIVE_INTERVAL = "keepalive_interval";
    public static final String KEY_MAX_MSG_BODY = "max_msg_body_len";
    public static final String KEY_HOUSE_STYLE = "house style";

    public static final String KEY_MAP_SHOW_STYLE = "map show style";
    public static final String KEY_RSSI_LIMIT_VALUE = "rssi limit value";

    private Preferences(Context context) {
        sharedPreferences = context.getSharedPreferences(SETTING, Context.MODE_PRIVATE);
    }

    public static Preferences getIntance() {
        return sIntance;
    }

    public int getServerIp() {
        return getInt(KEY_SERVICE_IP, App.sInstance.getDhcpInfo().gateway);
    }

    public void setServerIp(int value) {
        saveInt(KEY_SERVICE_IP, value);
    }


    public int getServerPort() {
        return getInt(KEY_SERVICE_PORT, 8887);
    }

    public void setServerPort(int value) {
        saveInt(KEY_SERVICE_PORT, value);
    }

    public int getSendPort() {
        return getInt(KEY_SEND_PORT, 8887);
    }

    public void setSendPort(int value) {
        saveInt(KEY_SEND_PORT, value);
    }

    public int getReceivePort() {
        return getInt(KEY_RECEIVE_PORT, 8889);
    }

    public void setReceivePort(int value) {
        saveInt(KEY_RECEIVE_PORT, value);
    }

    public int getKeepAliveInterval() {
        return getInt(KEY_KEEP_ALIVE_INTERVAL, 60000);
    }

    public void setKeepAliveInterval(short value) {
        saveInt(KEY_KEEP_ALIVE_INTERVAL, value);
    }

    public int getMaxMsgBody() {
        return getInt(KEY_MAX_MSG_BODY, 1024);
    }

    public void setMaxMsgBody(short value) {
        saveInt(KEY_MAX_MSG_BODY, value);
    }

    public int getMapShowStyle() {
        return getInt(KEY_MAP_SHOW_STYLE, 0);
    }

    public void setMapShowStyle(int value) {
        saveInt(KEY_MAP_SHOW_STYLE, value);
    }

    public int getRssiLimitValue() {
        return getInt(KEY_RSSI_LIMIT_VALUE, -100);
    }

    public void setRssiLimitValue(int value) {
        saveInt(KEY_RSSI_LIMIT_VALUE, value);
    }

    public static Preferences getIntance(Context context) {
        if (sIntance == null)
            sIntance = new Preferences(context);
        return sIntance;
    }

    public void cleanPreference() {
        sharedPreferences.edit().clear().commit();
    }

    public void removeKey(String... keys) {
        if (keys.length == 0)
            return;
        boolean isEdit = false;
        for (String k : keys)
            if (sharedPreferences.contains(k)) {
                sharedPreferences.edit().remove(k);
                isEdit = true;
            }
        if (isEdit)
            sharedPreferences.edit().commit();
    }

    public void saveString(String key, String value) {
        if (TextUtils.isEmpty(key))
            return;

        if (sharedPreferences.contains(key) && TextUtils.isEmpty(value))
            sharedPreferences.edit().remove(key).commit();

        sharedPreferences.edit().putString(key, value).commit();
    }

    public String getString(String key) {
        return getString(key, null);
    }

    public String getString(String key, String defaultValue) {
        if (sharedPreferences.contains(key))
            return sharedPreferences.getString(key, defaultValue);
        return defaultValue;
    }

    public void saveInt(String key, int value) {
        if (TextUtils.isEmpty(key))
            return;

        sharedPreferences.edit().putInt(key, value).commit();
    }

    public int getInt(String key) {
        return getInt(key, 0);
    }

    public int getInt(String key, int defaultValue) {
        if (sharedPreferences.contains(key))
            return sharedPreferences.getInt(key, defaultValue);
        return defaultValue;
    }

    public int getHouseStyle() {
        return getInt(KEY_HOUSE_STYLE);
    }

    public void setHouseStyle(int value) {
        saveInt(KEY_HOUSE_STYLE, value);
    }
}

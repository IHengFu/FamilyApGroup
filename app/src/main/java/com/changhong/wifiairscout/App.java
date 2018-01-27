package com.changhong.wifiairscout;

import android.app.Application;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatDelegate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.changhong.wifiairscout.preferences.Preferences;

/**
 * Created by fuheng on 2017/12/12.
 */

public class App extends Application {
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    public static App sInstance;

    public static boolean sTest = true;

    private ExecutorService mThreadPool;

    /**
     * 路由器mac地址
     */
    private String masterMac = null;
    /**
     * 5G or 2.4G
     */
    private byte mCurWlanIdx;
    /**
     * 当前信道
     */
    private byte mCurChannel;

    public byte getCurWlanIdx() {
        return mCurWlanIdx;
    }

    public void setCurWlanIdx(byte mCurWlanIdx) {
        this.mCurWlanIdx = mCurWlanIdx;
    }

    public byte getCurChannel() {
        return mCurChannel;
    }

    public void setCurChannel(byte mCurChannel) {
        this.mCurChannel = mCurChannel;
    }

    public static final byte MAX_RSSI = -20;
    public static final byte MIN_RSSI = -111;

    /**
     * 方案最大个数
     */
    public static final byte MAX_NUM_PROGRAMME = 10;


    public static final int[] RES_ID_HOUME_PICTURE = {R.mipmap.house_base, R.mipmap.house1,
            R.mipmap.house2_1, R.mipmap.house2_2,
            R.mipmap.house3_1, R.mipmap.house3_2,
            R.mipmap.house4, R.mipmap.house4_1};

    public static final byte TYPE_DEVICE_WIFI = 0;
    public static final byte TYPE_DEVICE_PHONE = 1;
    public static final byte TYPE_DEVICE_CLIENT = 2;
    public static final int[] RESID_WIFI_DEVICE = {R.mipmap.ic_wifi_nor, R.mipmap.ic_phone_nor, R.mipmap.ic_client_nor};

    //默认编码
    public static final String CHARSET = "utf-8";

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        Preferences.getIntance(getApplicationContext());

    }

    public WifiInfo getWifiInfo() {
        //获取wifi状态
        WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifi.getConnectionInfo();
        return wifiInfo;
    }

    public DhcpInfo getDhcpInfo() {
        //获取wifi状态
        WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcpinfo = wifi.getDhcpInfo();
        return dhcpinfo;
    }

    public ExecutorService getThreadPool() {
        if (mThreadPool == null)
            mThreadPool = Executors.newFixedThreadPool(3);
        return mThreadPool;
    }

    public void shutDownTask() {
        if (mThreadPool == null) {
            return;
        }
        mThreadPool.shutdown();
    }

    public String getMasterMac() {
        return masterMac;
    }

    public void setMasterMac(String masterMac) {
        this.masterMac = masterMac;
    }
}

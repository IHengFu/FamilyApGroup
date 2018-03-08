package com.changhong.wifiairscout.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * Created by fuheng on 2018/2/8.
 */

public class WifiControl {

    private final Context mContext;
    private final WifiManager mWifiManager;
    private final DhcpInfo mDhcpInfo;
    private WifiConfiguration mWifiConfiguration;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                List<ScanResult> list = mWifiManager.getScanResults();
                //判断是否掉线
                for (ScanResult scanResult : list) {
                    if (scanResult.BSSID.equals(mWifiInfo.getBSSID()))
                        return;
                }
                EventBus.getDefault().post(new WifiError(mWifiInfo.getBSSID()));
            }
        }
    };

    private WifiInfo mWifiInfo;

    public WifiControl(Context context) {
        mContext = context;

        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }

        mWifiInfo = mWifiManager.getConnectionInfo();
        mDhcpInfo = mWifiManager.getDhcpInfo();
        for (WifiConfiguration wifiConfiguration : mWifiManager.getConfiguredNetworks()) {
            if (wifiConfiguration.BSSID.equals(mWifiInfo.getBSSID())) {
                mWifiConfiguration = wifiConfiguration;
                break;
            }
        }
    }

    public void waitForConnect() {

        new Runnable() {

            @Override
            public void run() {
                do {
                    mWifiManager.enableNetwork(mWifiConfiguration.networkId, true);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } while (mWifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLED);

            }
        };
    }

    private void checkWifiOnline() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mContext.registerReceiver(mReceiver, filter);
        mWifiManager.startScan();
    }

    public static class WifiError {
        public String getMessage() {
            return message;
        }

        public WifiError(String message) {
            this.message = message;
        }

        String message;
    }
}

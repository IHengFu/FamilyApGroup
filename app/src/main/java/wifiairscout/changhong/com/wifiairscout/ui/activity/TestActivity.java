package wifiairscout.changhong.com.wifiairscout.ui.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import wifiairscout.changhong.com.wifiairscout.App;
import wifiairscout.changhong.com.wifiairscout.R;
import wifiairscout.changhong.com.wifiairscout.utils.CommUtils;
import wifiairscout.changhong.com.wifiairscout.utils.WifiUtils;

/**
 * Created by fuheng on 2017/12/20.
 */

public class TestActivity extends AppCompatActivity implements Runnable {
    private TextView mTextView01;
    private TextView mTextTest;

    private WifiUtils mWifiUtils;

    private BroadcastReceiver mScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
                return;
            StringBuilder sb = new StringBuilder();

            List<ScanResult> arrResult = mWifiUtils.getWifiAccessPointList();
            if (arrResult.isEmpty()) {
                sb.append("<Empty>");
            }
            System.err.println("scanResult size = " + arrResult.size());
            for (ScanResult it : arrResult) {
                String str = String.format("NAME:%s\nMAC:%s\nCHANNEL:%d\nLEVEL:%d\nDISTANCE:%f"
                        , it.SSID, it.BSSID.toUpperCase(), getChannelByFrequency(it.frequency), it.level, CommUtils.dbm2Distance(it.level));
                sb.append(str).append('\n');
            }
//            for (int i = 0; i < arrResult.size(); i++) {
//                sb.append(arrResult.get(i).toString());
//                sb.append("\n");
//            }
            mTextTest.setText(sb.toString());
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_test);

        mTextTest = findViewById(R.id.text_test);

        mTextView01 = findViewById(R.id.textview01);

        handler.postDelayed(this, 1000);

        mWifiUtils = new WifiUtils(this);

        if (Build.VERSION.SDK_INT >= 23 && !isGpsOPen(this)) {
            Settings.Secure.putInt(getContentResolver(), Settings.Secure.LOCATION_MODE, 1);
        }


        mWifiUtils.openWifi();
    }

    private boolean isGpsOPen(TestActivity testActivity) {
        LocationManager lm;//【位置管理】
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean ok = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (ok) {//开了定位服务
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                // 没有权限，申请权限。
//                        Toast.makeText(getActivity(), "没有权限", Toast.LENGTH_SHORT).show();
                Settings.Secure.putInt(getContentResolver(), Settings.Secure.LOCATION_MODE, 1);
                return false;
            }
        } else {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, 1315);
        }
        return true;
    }

    /**
     * 根据频率获得信道
     *
     * @param frequency
     * @return
     */
    public static int getChannelByFrequency(int frequency) {
        int channel = -1;
        switch (frequency) {
            case 2412:
                channel = 1;
                break;
            case 2417:
                channel = 2;
                break;
            case 2422:
                channel = 3;
                break;
            case 2427:
                channel = 4;
                break;
            case 2432:
                channel = 5;
                break;
            case 2437:
                channel = 6;
                break;
            case 2442:
                channel = 7;
                break;
            case 2447:
                channel = 8;
                break;
            case 2452:
                channel = 9;
                break;
            case 2457:
                channel = 10;
                break;
            case 2462:
                channel = 11;
                break;
            case 2467:
                channel = 12;
                break;
            case 2472:
                channel = 13;
                break;
            case 2484:
                channel = 14;
                break;
            case 5745:
                channel = 149;
                break;
            case 5765:
                channel = 153;
                break;
            case 5785:
                channel = 157;
                break;
            case 5805:
                channel = 161;
                break;
            case 5825:
                channel = 165;
                break;
        }
        return channel;
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            WifiInfo wifiiifo = mWifiUtils.getCurrentWifiInfo();
            int channel = getChannelByFrequency(wifiiifo.getFrequency());
            mTextView01.setText("rssi = " + wifiiifo.getRssi() + ",channel = " + channel);
            mWifiUtils.startScan();
            handler.postDelayed(TestActivity.this, 1000);
            super.handleMessage(msg);
        }
    };

    @Override
    public void run() {
        handler.sendEmptyMessage(0);
    }

    @Override
    protected void onResume() {
        registWifiScanListener();
        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(mScanReceiver);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(this);
    }

    private void registWifiScanListener() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(mScanReceiver, filter);
    }
}

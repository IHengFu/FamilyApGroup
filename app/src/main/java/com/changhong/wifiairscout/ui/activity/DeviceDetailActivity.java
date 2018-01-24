package com.changhong.wifiairscout.ui.activity;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import com.changhong.wifiairscout.App;
import com.changhong.wifiairscout.R;
import com.changhong.wifiairscout.model.WifiDevice;
import com.changhong.wifiairscout.model.response.GetClientStatusResponse;
import com.changhong.wifiairscout.ui.view.ArcView;
import com.changhong.wifiairscout.ui.view.SignalView;
import com.changhong.wifiairscout.utils.CommUtils;

/**
 * Created by fuheng on 2017/12/12.
 */

public class DeviceDetailActivity extends AppCompatActivity {
    private TextView mTvDeviceName;
    private TextView mTvMac;
    private TextView mTvCurChannal;
    private WifiDevice device;
    private ArcView signalView;
    private SignalView mAnimSignalView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        device = getIntent().getParcelableExtra(Intent.EXTRA_DATA_REMOVED);

        setContentView(R.layout.layout_device_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.mipmap.ic_back);
//        getSupportActionBar().setIcon(App.RESID_WIFI_DEVICE[device.getType()]);

        CommUtils.transparencyBar(this);

        EventBus.getDefault().register(this);


        mTvDeviceName = (TextView) findViewById(R.id.tv_device_name);
        mTvMac = (TextView) findViewById(R.id.tv_mac);
        mTvCurChannal = (TextView) findViewById(R.id.tv_current_channel);


        signalView = findViewById(R.id.signal_view);

        mAnimSignalView = findViewById(R.id.view_anim_signal);

        reset();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {

        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true) //在ui线程执行
    public void onDataSynEvent(GetClientStatusResponse event) {

        if (event.getDevices() != null) {
            for (WifiDevice wifiDevice : event.getDevices()) {
                if (wifiDevice.equals(device)) {
                    device.eat(wifiDevice);
                    reset();
                    break;
                }
            }
        }


    }

    private void reset() {
        getSupportActionBar().setTitle(device.getName());

        mTvDeviceName.setText(device.getIp());
        mTvMac.setText(device.getMac());
        mTvCurChannal.setText("" + device.getChannel());

        int rate = (int) ((device.getRssi() - App.MIN_RSSI) * 100f / (App.MAX_RSSI - App.MIN_RSSI));
        if (device.getType() == App.TYPE_DEVICE_WIFI)
            rate = 100;
        signalView.setDisplayString("当前信号", String.valueOf(device.getRssi()), "-111dBm", ">-49dBm", "dBm");
        signalView.setProgress(rate);


        int color = (int) ((100 - rate) * 0xff / 100f);
        color = Color.HSVToColor(new float[]{rate * 120f / 100, 1, 1});
        mAnimSignalView.setColor(color);
    }

}

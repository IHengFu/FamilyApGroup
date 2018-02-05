package com.changhong.wifiairscout.ui.activity;


import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.changhong.wifiairscout.App;
import com.changhong.wifiairscout.R;
import com.changhong.wifiairscout.model.MessageData;
import com.changhong.wifiairscout.model.MessageDataFactory;
import com.changhong.wifiairscout.model.WifiDevice;
import com.changhong.wifiairscout.model.response.GetClientStatusResponse;
import com.changhong.wifiairscout.model.response.GetTranslateSpeedResponse;
import com.changhong.wifiairscout.task.GenericTask;
import com.changhong.wifiairscout.task.TaskListener;
import com.changhong.wifiairscout.task.TaskResult;
import com.changhong.wifiairscout.task.UDPTask;
import com.changhong.wifiairscout.ui.view.ArcView;
import com.changhong.wifiairscout.ui.view.SignalView;
import com.changhong.wifiairscout.utils.CommUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * Created by fuheng on 2017/12/12.
 */

public class DeviceDetailActivity extends BaseActivtiy implements View.OnClickListener, TaskListener<MessageData> {
    protected EditText mTvDeviceName;
    protected TextView mTvMac;
    private TextView mTvCurChannal;
    private TextView mTvIP;
    private ArcView signalView;
    private SignalView mAnimSignalView;
    private TextView mBtnSpeed;

    private GenericTask mTask;
    protected WifiDevice device;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        device = getIntent().getParcelableExtra(Intent.EXTRA_DATA_REMOVED);

        setContentView(R.layout.layout_device_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.mipmap.ic_back);

        CommUtils.transparencyBar(this);

        EventBus.getDefault().register(this);
        getSupportActionBar().setTitle(R.string.title_device_detail);

        mTvDeviceName = findViewById(R.id.tv_device_name);
        setEditTextDeable();

        mTvIP = findViewById(R.id.tv_ip);
        mTvMac = findViewById(R.id.tv_mac);
        mTvCurChannal = findViewById(R.id.tv_current_channel);


        signalView = findViewById(R.id.signal_view);

        mAnimSignalView = findViewById(R.id.view_anim_signal);

        mBtnSpeed = findViewById(R.id.btn_speed);
        mBtnSpeed.setOnClickListener(this);
    }

    protected void setEditTextDeable() {
        mTvDeviceName.setEnabled(false);
        mTvDeviceName.setBackground(null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        reset();
    }

    @Override
    protected void onDestroy() {
        if (mTask != null)
            mTask.cancle();
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

    protected void reset() {

        if (device != null) {
            mTvIP.setText(device.getIp());
            mTvMac.setText(device.getMac());
            mTvCurChannal.setText("" + device.getChannel());
            mTvDeviceName.setText(device.getName());

            mAnimSignalView.setVisibility(View.VISIBLE);
            if (device.getType() == App.TYPE_DEVICE_WIFI) {
                signalView.setDisplayString(getString(R.string.tab_cur_signal), "∞", App.MIN_RSSI + "dBm", ">" + App.MAX_RSSI + "dBm", "dBm");
                signalView.setProgress(100);
                ((View) mBtnSpeed.getParent()).setVisibility(View.GONE);

            } else {
                signalView.setDisplayString(getString(R.string.tab_cur_signal), String.valueOf(device.getRssi()), App.MIN_RSSI + "dBm", ">" + App.MAX_RSSI + "dBm", "dBm");
                float rate = getRate();
                signalView.setProgress((int) rate * 100);
                mAnimSignalView.setColor(getSignalColor(rate));
                ((View) mBtnSpeed.getParent()).setVisibility(View.VISIBLE);
            }

            if (device != null && device.getWlan_idx() == 0)
                getSupportActionBar().setIcon(R.drawable.vector_5g);
            else
                getSupportActionBar().setIcon(0);
        } else {
            mTvIP.setText(null);
            mTvMac.setText(null);
            mTvCurChannal.setText(null);
            mTvDeviceName.setText(null);
            mAnimSignalView.setVisibility(View.INVISIBLE);
            signalView.setDisplayString(getString(R.string.tab_cur_signal), "-∞", App.MIN_RSSI + "dBm", ">" + App.MAX_RSSI + "dBm", "dBm");
            mAnimSignalView.setColor(Color.DKGRAY);
            ((View) mBtnSpeed.getParent()).setVisibility(View.GONE);

            getSupportActionBar().setIcon(0);
        }

    }

    private int getSignalColor(float rate) {
        int color = Color.HSVToColor(new float[]{rate * 120, 1, 1});
        return color;
    }

    private float getRate() {
        float rate = (device.getRssi() - App.MIN_RSSI) * 1f / (App.MAX_RSSI - App.MIN_RSSI);
        if (device.getType() == App.TYPE_DEVICE_WIFI)
            rate = 1;
        return rate;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_speed:
                doTestSpeed();
                break;
        }
    }

    private void doTestSpeed() {
        mTask = new UDPTask().execute(MessageDataFactory.getTranslateSpeed(device.getMac()), this);
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void onPreExecute(GenericTask task) {
        showProgressDialog(getString(R.string.notice_wait_for_test_speed), true, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                mTask.cancle();
            }
        });
    }

    @Override
    public void onPostExecute(GenericTask task, TaskResult result) {
        hideProgressDialog();
        mTask = null;
        if (result != TaskResult.OK) {
            showToast(task.getException().getMessage());
        }
    }

    @Override
    public void onProgressUpdate(GenericTask task, MessageData param) {
        mBtnSpeed.setText(new GetTranslateSpeedResponse(param.getMsgBody()).getmListRate().get(0).getRateString());
    }

    @Override
    public void onCancelled(GenericTask task) {
        hideProgressDialog();
        showToast(getString(R.string.notice_test_task_canceled));
        mTask = null;
    }
}

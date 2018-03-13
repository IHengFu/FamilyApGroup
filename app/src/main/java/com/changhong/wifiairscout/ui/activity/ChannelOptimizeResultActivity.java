package com.changhong.wifiairscout.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.changhong.wifiairscout.App;
import com.changhong.wifiairscout.R;
import com.changhong.wifiairscout.db.dao.DeviceLocationDao;
import com.changhong.wifiairscout.db.dao.ProgrammeDao;
import com.changhong.wifiairscout.db.data.DeviceLocation;
import com.changhong.wifiairscout.db.data.ProgrammeGroup;
import com.changhong.wifiairscout.model.WifiDevice;
import com.changhong.wifiairscout.model.response.GetClientStatusResponse;
import com.changhong.wifiairscout.ui.adapter.ProgrammeAdapter;
import com.changhong.wifiairscout.utils.CommUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fuheng on 2018/2/4.
 */

public class ChannelOptimizeResultActivity extends BaseActivtiy implements View.OnClickListener {

    private Toolbar mToolBar;

    private ExpandableListView mExpandableListView;
    private BaseExpandableListAdapter mAdapter;
    private List<List<DeviceLocation>> mArrayDeviceLocation;
    private List<ProgrammeGroup> mArrayProgramme;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_channel_optimize_result);
        mToolBar = findViewById(R.id.toolbar);
        mToolBar.setTitle(R.string.optimizeResult);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findViewById(R.id.btn_accept).setOnClickListener(this);

        initData();

        mAdapter = new ProgrammeAdapter(this, mArrayProgramme, mArrayDeviceLocation);

        mExpandableListView = findViewById(R.id.list_channelOptimizeCompare);
        mExpandableListView.setAdapter(mAdapter);
        {
            TextView textview = new TextView(this);
            textview.setText(R.string.no_data);
            mExpandableListView.setEmptyView(textview);
        }
        CommUtils.transparencyBar(this);
//        if (App.sTest)
//            EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
//        if (App.sTest)
//            EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View view) {
        finish();
    }

    private void initData() {
        ProgrammeDao pDao = new ProgrammeDao(this);

        DeviceLocationDao dDao = new DeviceLocationDao(this);

        //添加记录
        mArrayProgramme = new ArrayList<>();
        mArrayProgramme.addAll(pDao.queryByUserName(App.sInstance.getGuestName()));

        mArrayDeviceLocation = new ArrayList<>();

        for (ProgrammeGroup programmeGroup : mArrayProgramme) {
            mArrayDeviceLocation.add(dDao.queryByProgrammeId(programmeGroup.getGroup()));
        }

        //添加当前
//        if (App.sTest) {
//            long groupid = System.currentTimeMillis();
//            ProgrammeGroup e = new ProgrammeGroup();
//            e.setName(getString(R.string.cur_programme));
//            e.setGroup(groupid);
//            e.setUserName(App.sInstance.getGuestName());
//            mArrayProgramme.add(e);
//
//            mArrayDeviceLocation.add(new ArrayList<DeviceLocation>());
//        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true) //在ui线程执行
    public void onDataSynEvent(GetClientStatusResponse event) {
        if (!App.sTest)//TODO 目前正式版不展示
            return;

        Log.e(getClass().getSimpleName(), "event---->" + event.toString());
        int totleRSSI = 0;
        int count = 0;

        ArrayList<WifiDevice> devices = MainActivity.Companion.getSIntance().getMArrayDevices();

        List<DeviceLocation> arrLocation = mArrayDeviceLocation.get(mArrayDeviceLocation.size() - 1);
        arrLocation.clear();

        ProgrammeGroup programme = mArrayProgramme.get(mArrayProgramme.size() - 1);

        for (WifiDevice device : devices) {
            if (device.getType() == App.TYPE_DEVICE_CONNECT)
                continue;
            if (device.getType() == App.TYPE_DEVICE_CLIENT || device.getType() == App.TYPE_DEVICE_PHONE) {
                totleRSSI += device.getRssi();
                count++;
            }

            DeviceLocation item = new DeviceLocation();
            item.setType(device.getType());
            item.setMac(device.getMac());
            item.setNickName(device.getName());
            item.setGroup(programme.getGroup());
            item.setRssi(device.getRssi());
            arrLocation.add(item);
        }

        if (count > 0)
            programme.setRssi((byte) (totleRSSI / count));

        mAdapter.notifyDataSetChanged();

    }

}

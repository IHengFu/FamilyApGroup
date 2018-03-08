package com.changhong.wifiairscout.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.changhong.wifiairscout.App;
import com.changhong.wifiairscout.R;
import com.changhong.wifiairscout.model.MessageData;
import com.changhong.wifiairscout.model.MessageDataFactory;
import com.changhong.wifiairscout.model.response.RegisterResponse;
import com.changhong.wifiairscout.model.response.ScanNewResponse;
import com.changhong.wifiairscout.preferences.Preferences;
import com.changhong.wifiairscout.task.GenericTask;
import com.changhong.wifiairscout.task.TaskListener;
import com.changhong.wifiairscout.task.TaskResult;
import com.changhong.wifiairscout.task.UDPTask;
import com.changhong.wifiairscout.utils.CommUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/1/15.
 */

public class ChannelConditionActivity extends BaseActivtiy implements View.OnClickListener {
    private Toolbar mToolBar;
    private ListView mListView;
    private ArrayAdapter mAdapter;
    private ArrayList<CharSequence> mArrayData = new ArrayList();
    private TextView mTvAdvice;
    private TextView mTvAdviceTitle;
    private int mBestChannel;

    private List<GenericTask> mArrayTask = new ArrayList<>();
    private TextView btnCancel;
    private TextView btnContinue;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_channel_condition);

        mToolBar = findViewById(R.id.toolbar);
        if (App.sInstance.getCurWlanIdx() == 0)
            mToolBar.setTitle(R.string.channel5_0);
        else
            mToolBar.setTitle(R.string.channel2_4);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CommUtils.transparencyBar(this);

        {
            mListView = findViewById(R.id.list_channelUsedCondetion);
            mAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, mArrayData);
            mListView.setAdapter(mAdapter);
        }

        mTvAdvice = findViewById(R.id.textview_advice);
        mTvAdviceTitle = findViewById(R.id.tv_advice_title);

        btnContinue = findViewById(R.id.btn_accept);
        btnContinue.setOnClickListener(this);
        btnCancel = findViewById(R.id.btn_refuse);
        btnCancel.setOnClickListener(this);

        doScan();

    }

    @Override
    protected void onDestroy() {
        if (!mArrayTask.isEmpty()) {
            for (GenericTask genericTask : mArrayTask) {
                genericTask.cancle();
            }
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_accept:
                if (!isInBestChannel())
                    doOptimization(mBestChannel);
                else {
                    startActivity(new Intent(ChannelConditionActivity.this, ChannelOptimizeResultActivity.class));
                    finish();
                }
                break;
            case R.id.btn_refuse:
                finish();
                break;
        }
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

    /**
     * 优化
     */
    private void doOptimization(int channel) {
        if (App.sTest) {
            showProgressDialog(getString(R.string.changeChannel), true, null);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    hideProgressDialog();
                    startActivity(new Intent(ChannelConditionActivity.this, ChannelOptimizeResultActivity.class));
                    finish();
                }
            }, 2000);
        } else {
            new UDPTask().execute(MessageDataFactory.setChannel(channel, App.sInstance.getCurWlanIdx(), App.sInstance.getMasterMac()), mSetChannelListener);
        }
    }

    private void doScan() {
        if (App.sTest) {
            int[] NUMBER_OF_CHANNEL;
            if (App.sInstance.getCurWlanIdx() == 0) {
                NUMBER_OF_CHANNEL = getResources().getIntArray(R.array.channel_5g_cn);
            } else {
                NUMBER_OF_CHANNEL = new int[13];
                for (int i = 0; i < NUMBER_OF_CHANNEL.length; i++) {
                    NUMBER_OF_CHANNEL[i] = i + 1;
                }
            }

            List<ScanNewResponse.ScanRusult> list = new ArrayList<>();
            int length = (int) (Math.random() * 50);

            for (int i = 0; i < length; ++i) {
                byte channel = (byte) NUMBER_OF_CHANNEL[(int) (Math.random() * NUMBER_OF_CHANNEL.length)];
                ScanNewResponse.ScanRusult device = new ScanNewResponse.ScanRusult(channel, App.MIN_RSSI);
                list.add(device);
            }
            resetData(list);
        } else
            new UDPTask().execute(MessageDataFactory.doScan(App.sInstance.getMasterMac(), App.sInstance.getCurWlanIdx()), mScanListener);
    }


    private void resetData(List<ScanNewResponse.ScanRusult> list) {
        mArrayData.clear();
        int[] NUMBER_OF_CHANNEL;
        if (App.sInstance.getCurWlanIdx() == 0) {
            NUMBER_OF_CHANNEL = getResources().getIntArray(R.array.channel_5g_cn);
        } else {
            NUMBER_OF_CHANNEL = new int[13];
            for (int i = 0; i < NUMBER_OF_CHANNEL.length; i++) {
                NUMBER_OF_CHANNEL[i] = i + 1;
            }
        }
        //统计各信道AP数量
        int[] num_channel = new int[NUMBER_OF_CHANNEL.length];
        for (ScanNewResponse.ScanRusult sr : list) {
            for (int i = 0; i < NUMBER_OF_CHANNEL.length; i++) {
                if (NUMBER_OF_CHANNEL[i] == sr.getChannel()) {
                    num_channel[i]++;
                    break;
                }
            }
        }

        for (int i = 0; i < num_channel.length; ++i) {
            String string = String.format(getString(R.string.formatChannalUsageCondition), NUMBER_OF_CHANNEL[i], num_channel[i]);
            if (NUMBER_OF_CHANNEL[i] == App.sInstance.getWlanIndexObject().get(App.sInstance.getCurWlanIdx()).getChannel()) {
                SpannableString ss = new SpannableString(string + "<img/>");
                Drawable drawable = getDrawable(R.drawable.ic_wifi_black_24dp);
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                ss.setSpan(new ImageSpan(drawable), string.length(), ss.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                mArrayData.add(ss);
            } else
                mArrayData.add(string);
        }

        float minWeight = Float.MAX_VALUE;
        float temp;
        for (int i = 0; i < num_channel.length; ++i) {
            temp = num_channel[i];
            if (i > 0)
                temp += 0.3f * num_channel[i - 1];
            if (i > 1)
                temp += 0.15f * num_channel[i - 2];
            if (i > 2)
                temp += 0.075f * num_channel[i - 3];
            if (i > 3)
                temp += 0.0375 * num_channel[i - 4];

            if (i < num_channel.length - 1)
                temp += 0.3f * num_channel[i + 1];
            if (i < num_channel.length - 2)
                temp += 0.15f * num_channel[i + 2];
            if (i < num_channel.length - 3)
                temp += 0.075f * num_channel[i + 3];
            if (i < num_channel.length - 4)
                temp += 0.0375f * num_channel[i + 4];

            if (minWeight > temp) {
                minWeight = temp;
                mBestChannel = NUMBER_OF_CHANNEL[i];
            }
        }


        mTvAdvice.setText(String.format(getString(R.string.adviceChannel), mBestChannel));
        mAdapter.notifyDataSetChanged();
        if (isInBestChannel()) {
            mTvAdviceTitle.setText(R.string.noticeCurChannelBest);
            mTvAdvice.setVisibility(View.GONE);

            btnCancel.setText(android.R.string.cancel);
            btnContinue.setText(R.string.action_continue);

        } else {
            mTvAdvice.setVisibility(View.VISIBLE);
            mTvAdviceTitle.setText(R.string.optimizationAdvice);
            btnCancel.setText(R.string.action_refuse);
            btnContinue.setText(R.string.action_accept);
        }
    }

    private void showAlertDialog(CharSequence alertMessage, DialogInterface.OnClickListener listener) {
        AlertDialog dialog = new AlertDialog.Builder(this).setMessage(alertMessage)
                .setPositiveButton(R.string.action_exit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        finish();
                    }
                }).setNegativeButton(R.string.action_retry, listener).create();
        dialog.setCancelable(true);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                dialogInterface.dismiss();
                finish();
            }
        });
        dialog.show();
    }

    /**
     * 扫描结果反馈
     */
    private TaskListener mScanListener = new TaskListener<MessageData>() {
        @Override
        public String getName() {
            return null;
        }

        @Override
        public void onPreExecute(GenericTask task) {
            showProgressDialog(getString(R.string.onScanChanneling), true, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    finish();
                }
            });
            mArrayTask.add(task);
        }

        @Override
        public void onPostExecute(GenericTask task, TaskResult result) {
            mArrayTask.remove(task);
            hideProgressDialog();
            if (result != TaskResult.OK) {
                showAlertDialog(task.getException().getMessage(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        doScan();
                    }
                });
            }
        }

        @Override
        public void onProgressUpdate(GenericTask task, MessageData param) {
            if (param == null)
                return;
            ScanNewResponse response = new ScanNewResponse(param.getMsgBody());
            resetData(response.getListAp());
        }

        @Override
        public void onCancelled(GenericTask task) {
            hideProgressDialog();
            mArrayTask.remove(task);
        }
    };
    private TaskListener mSetChannelListener = new TaskListener<MessageData>() {
        @Override
        public String getName() {
            return null;
        }

        public void onPreExecute(GenericTask task) {
            showProgressDialog(getString(R.string.changeChannel), true, null);
            mArrayTask.add(task);
        }

        public void onPostExecute(GenericTask task, TaskResult result) {
            mArrayTask.remove(task);
            hideProgressDialog();
            if (result != TaskResult.OK) {
                showToast(task.getException().getMessage());
            } else
                doRegister();
        }

        public void onProgressUpdate(GenericTask task, MessageData param) {
            showToast(getString(R.string.optimizationComplete));
        }

        public void onCancelled(GenericTask task) {
            hideProgressDialog();
            mArrayTask.remove(task);
        }
    };

    private void doRegister() {
        new UDPTask().execute(MessageDataFactory.getRegisterMessage(), mRegisterListener);
    }

    private TaskListener mRegisterListener = new TaskListener<MessageData>() {
        @Override
        public String getName() {
            return null;
        }

        @Override
        public void onPreExecute(GenericTask task) {
            mArrayTask.add(task);
            showProgressDialog(getString(R.string.noticeWaitForReconnect), false, null);
        }

        @Override
        public void onPostExecute(GenericTask task, TaskResult result) {
            mArrayTask.remove(task);
            if (result != TaskResult.OK)
                doRegister();
            else {
                hideProgressDialog();
                startActivity(new Intent(ChannelConditionActivity.this, ChannelOptimizeResultActivity.class));
                finish();
            }
        }

        @Override
        public void onProgressUpdate(GenericTask task, MessageData param) {
            RegisterResponse rr = new RegisterResponse(param.getMsgBody());

            App.sInstance.setWlanIndexObject(rr.getWlanCondition());
            Preferences.getIntance().setMaxMsgBody(rr.getMax_msg_body_len());
            Preferences.getIntance().setKeepAliveInterval(rr.getKeepalive_interval());
            App.sInstance.setMasterMac(param.getMacString());
            if (App.sInstance.getWifiInfo().getFrequency() >= 5000)
                App.sInstance.setCurWlanIdx((byte) 0);
            else
                App.sInstance.setCurWlanIdx((byte) 1);
            App.sInstance.setCurChannel(rr.getCurrentWlanIdx(App.sInstance.getCurWlanIdx()));
        }

        @Override
        public void onCancelled(GenericTask task) {
            mArrayTask.remove(task);
        }
    };

    private boolean isInBestChannel() {
        if (App.sInstance.getWlanIndexObject() != null && mBestChannel == App.sInstance.getWlanIndexObject().get(App.sInstance.getCurChannel()).getChannel())
            return true;
        return false;
    }
}

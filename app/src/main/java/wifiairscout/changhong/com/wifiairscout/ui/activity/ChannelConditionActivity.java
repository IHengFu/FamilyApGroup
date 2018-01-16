package wifiairscout.changhong.com.wifiairscout.ui.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import wifiairscout.changhong.com.wifiairscout.R;
import wifiairscout.changhong.com.wifiairscout.model.MessageData;
import wifiairscout.changhong.com.wifiairscout.model.MessageDataFactory;
import wifiairscout.changhong.com.wifiairscout.model.WifiDevice;
import wifiairscout.changhong.com.wifiairscout.model.response.BaseResponse;
import wifiairscout.changhong.com.wifiairscout.model.response.GetChannelResponse;
import wifiairscout.changhong.com.wifiairscout.model.response.ScanResponse;
import wifiairscout.changhong.com.wifiairscout.service.StartService;
import wifiairscout.changhong.com.wifiairscout.task.GenericTask;
import wifiairscout.changhong.com.wifiairscout.task.TaskListener;
import wifiairscout.changhong.com.wifiairscout.task.TaskResult;
import wifiairscout.changhong.com.wifiairscout.task.UDPTask;
import wifiairscout.changhong.com.wifiairscout.utils.CommUtils;

/**
 * Created by Administrator on 2018/1/15.
 */

public class ChannelConditionActivity extends BaseActivtiy implements View.OnClickListener {
    private Toolbar mToolBar;
    private UDPTask mUdpTask;
    private ListView mListView;
    private ArrayAdapter mAdapter;
    private ArrayList<String> mArrayData = new ArrayList();
    private TextView mTvAdvice;
    private TextView mTvAdviceTitle;
    private View mPanelAsk;
    private int mBestChannel;
    private byte mCurChannel = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_channel_condition);

        mToolBar = findViewById(R.id.toolbar);
        mToolBar.setTitle(R.string.house_choice);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.channel2_4);

        CommUtils.transparencyBar(this);

        {
            mListView = findViewById(R.id.list_channelUsedCondetion);
            AppCompatTextView textView = new AppCompatTextView(this);
            textView.setText(R.string.no_data);
            mListView.setEmptyView(textView);
            mAdapter = new ArrayAdapter(this, R.layout.item_list_simple_text, mArrayData);
            mListView.setAdapter(mAdapter);
        }

        mTvAdvice = findViewById(R.id.textview_advice);
        mTvAdviceTitle = findViewById(R.id.tv_advice_title);
        mPanelAsk = findViewById(R.id.layout_ask);

        findViewById(R.id.btn_accept).setOnClickListener(this);
        findViewById(R.id.btn_refuse).setOnClickListener(this);

        startLoadChannel();

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        if (mUdpTask != null)
            mUdpTask.cancle();
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_accept:

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
        mUdpTask = new UDPTask().execute(MessageDataFactory.setChannel(channel), mSetChannelListener);
    }

    private void startLoadChannel() {
        mUdpTask = new UDPTask().execute(MessageDataFactory.doScan(false), mScanListener);
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
        }

        @Override
        public void onPostExecute(GenericTask task, TaskResult result) {
            hideProgressDialog();
            if (result != TaskResult.OK) {
                showAlertDialog(task.getException().getMessage(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startLoadChannel();
                    }
                });
            }
        }

        @Override
        public void onProgressUpdate(GenericTask task, MessageData param) {
            if (param == null)
                return;
            ScanResponse response = new ScanResponse(param.getMsgBody());
            mArrayData.clear();
            int[] num_channel = new int[13];
            for (WifiDevice wifiDevice : response.getListAp()) {
                num_channel[wifiDevice.getChannel() - 1]++;
            }

            float minWeight = Float.MAX_VALUE;
            float temp = 0;
            for (int i = 0; i < num_channel.length; ++i) {
                mArrayData.add(String.format(getString(R.string.formatChannalUsageCondition), i + 1, num_channel[i]));
                temp = num_channel[i];
                if (i > 0)
                    temp += 0.3f * num_channel[i - 1];
                if (i < num_channel.length - 1)
                    temp += 0.3f * num_channel[i + 1];
                if (minWeight > temp) {
                    minWeight = temp;
                    mBestChannel = i;
                }
            }


            mTvAdvice.setText(String.format(getString(R.string.adviceChannel), mBestChannel));
            mAdapter.notifyDataSetChanged();
            if (mCurChannel != -1 && mBestChannel == mCurChannel) {
                mTvAdviceTitle.setText(R.string.noticeCurChannelBest);
                mTvAdvice.setVisibility(View.GONE);
                mPanelAsk.setVisibility(View.GONE);
            } else {
                mTvAdvice.setVisibility(View.VISIBLE);
                mPanelAsk.setVisibility(View.VISIBLE);
                mTvAdviceTitle.setText(R.string.optimizationAdvice);
            }
        }

        @Override
        public void onCancelled(GenericTask task) {
            hideProgressDialog();
        }
    };
    private TaskListener mSetChannelListener = new TaskListener<BaseResponse>() {
        @Override
        public String getName() {
            return null;
        }

        public void onPreExecute(GenericTask task) {
            showProgressDialog(getString(R.string.changeChannel), true, null);
        }

        public void onPostExecute(GenericTask task, TaskResult result) {
            hideProgressDialog();
            if (result != TaskResult.OK) {
                showToast(task.getException().getMessage());
            }
        }

        public void onProgressUpdate(GenericTask task, BaseResponse param) {
            showToast(getString(R.string.optimizationComplete));
            StartService.Companion.startServcie(ChannelConditionActivity.this, StartService.ACTION_LOAD_CUR_CHANNEL);
            finish();
        }

        public void onCancelled(GenericTask task) {
            hideProgressDialog();
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true) //在ui线程执行
    public void onDataSynEvent(GetChannelResponse event) {
        mCurChannel = event.getChannel();
    }
}

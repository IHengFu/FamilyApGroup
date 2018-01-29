package com.changhong.wifiairscout.ui.activity;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.changhong.wifiairscout.App;
import com.changhong.wifiairscout.R;
import com.changhong.wifiairscout.model.MessageData;
import com.changhong.wifiairscout.model.MessageDataFactory;
import com.changhong.wifiairscout.model.WifiDevice;
import com.changhong.wifiairscout.model.response.BaseResponse;
import com.changhong.wifiairscout.model.response.GetClientResponse;
import com.changhong.wifiairscout.model.response.GetClientStatusResponse;
import com.changhong.wifiairscout.model.response.GetMasterResponse;
import com.changhong.wifiairscout.model.response.GetTranslateSpeedResponse;
import com.changhong.wifiairscout.model.response.RegisterResponse;
import com.changhong.wifiairscout.model.response.ScanNewResponse;
import com.changhong.wifiairscout.model.response.ScanResponse;
import com.changhong.wifiairscout.task.GenericTask;
import com.changhong.wifiairscout.task.TaskListener;
import com.changhong.wifiairscout.task.TaskResult;
import com.changhong.wifiairscout.task.UDPTask;

import java.util.ArrayList;

/**
 * Created by fuheng on 2017/12/20.
 */

public class TestActivity extends BaseActivtiy implements View.OnClickListener {
    private TextView mTextView;
    private ArrayList<GenericTask> arrTask = new ArrayList<>();
    private final int[] RES_ID_BTN = {R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4, R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9, R.id.btn10};
    private ArrayList<WifiDevice> mDevice;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_test);

        ImageView imageView = findViewById(R.id.imageView_test);
        Drawable drawable = imageView.getDrawable();
        mTextView = findViewById(R.id.text_test);
//        drawable = DrawableCompat.wrap(drawable).mutate();
//        imageView.setImageDrawable(drawable);
//        drawable.setTint(Color.BLUE);
        DrawableCompat.setTint(drawable, Color.YELLOW);
        if (drawable instanceof AnimatedVectorDrawableCompat) {
            AnimatedVectorDrawableCompat d = ((AnimatedVectorDrawableCompat) drawable);
            d.start();
        }
        for (int i : RES_ID_BTN) {
            findViewById(i).setOnClickListener(this);
        }

        showToast(App.sInstance.getWifiInfo().getFrequency() + "");

        findViewById(R.id.fab).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn1:
                new UDPTask().execute(MessageDataFactory.getRegisterMessage(), new AbstractListener() {
                    @Override
                    public void onProgressUpdate(GenericTask task, MessageData param) {
                        App.sInstance.setMasterMac(param.getMacString());
                        RegisterResponse a = new RegisterResponse(param.getMsgBody());
                        mTextView.append(a.toString() + "\n");
                    }
                });
                break;
            case R.id.btn2:
                new UDPTask().execute(MessageDataFactory.getAliveMessage(), new AbstractListener());
                break;
            case R.id.btn3:
                new UDPTask().execute(MessageDataFactory.getAllClientInfo(), new AbstractListener() {
                    @Override
                    public void onProgressUpdate(GenericTask task, MessageData param) {
                        GetClientResponse a = new GetClientResponse(param.getMsgBody());
                        mTextView.append(a.toString() + "\n");
                        mDevice = a.getDevices();
                    }
                });
                break;
            case R.id.btn4:
                new UDPTask().execute(MessageDataFactory.getAllClientStatus(), new AbstractListener() {
                    @Override
                    public void onProgressUpdate(GenericTask task, MessageData param) {
                        mTextView.append(new GetClientStatusResponse(param.getMsgBody()).toString() + "\n");
                    }
                });
                break;

            case R.id.btn5:
                if (App.sInstance.getMasterMac() == null) {
                    showToast("请先注册");
                    return;
                }
                new UDPTask().execute(MessageDataFactory.getMasterInfo(App.sInstance.getMasterMac()), new AbstractListener() {
                    @Override
                    public void onProgressUpdate(GenericTask task, MessageData param) {
                        mTextView.append(new GetMasterResponse(param.getMsgBody()).toString() + "\n");
                    }
                });
                break;

            case R.id.btn6:
                if (App.sInstance.getMasterMac() == null) {
                    showToast("请先注册");
                    return;
                }
                new UDPTask().execute(MessageDataFactory.doScan(App.sInstance.getMasterMac(), App.sInstance.getCurWlanIdx()), new AbstractListener() {
                    @Override
                    public void onProgressUpdate(GenericTask task, MessageData param) {
                        mTextView.append(new ScanNewResponse(param.getMsgBody()).toString() + "\n");
                    }
                });
                break;
            case R.id.btn7:
                new UDPTask().execute(MessageDataFactory.doScan(App.sInstance.getMasterMac()), new AbstractListener() {
                    @Override
                    public void onProgressUpdate(GenericTask task, MessageData param) {
                        mTextView.append(new ScanResponse(param.getMsgBody()).toString() + "\n");
                    }
                });
                break;
            case R.id.btn8:
                if (mDevice == null || mDevice.size() < 0) {
                    showToast("请先获取client");
                    return;
                }
                new UDPTask().execute(MessageDataFactory.getTranslateSpeed(mDevice.get((int) (Math.random() * mDevice.size())).getMac()), new AbstractListener() {
                    @Override
                    public void onProgressUpdate(GenericTask task, MessageData param) {
                        mTextView.append(new GetTranslateSpeedResponse(param.getMsgBody()).toString() + "\n");
                    }
                });
                break;
            case R.id.btn9:
                new UDPTask().execute(MessageDataFactory.setWorkChannel(Math.random() * 2 > 1), new AbstractListener());
                break;
            case R.id.btn10:
                new UDPTask().execute(MessageDataFactory.setChannel(13, App.sInstance.getCurWlanIdx(), App.sInstance.getMasterMac()), new AbstractListener());
                break;
            case R.id.fab:
                mTextView.setText(null);
                break;
        }
    }

    private class AbstractListener implements TaskListener<MessageData> {
        public AbstractListener() {
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public void onPreExecute(GenericTask task) {
            arrTask.add(task);

            String name = task.toString();
            name = name.substring(name.lastIndexOf('.') + 1);
            mTextView.append("<= " + name + "started. =>\n");
        }

        @Override
        public void onPostExecute(GenericTask task, TaskResult result) {
            arrTask.remove(task);
            if (task.getException() != null) {
                mTextView.append("error:" + task.getException().getClass().getSimpleName() + "->" + task.getException().getMessage() + "\n");
            }

            String name = task.toString();
            name = name.substring(name.lastIndexOf('.') + 1);
            mTextView.append("<= " + name + "Stoped. =>\n");
        }

        @Override
        public void onProgressUpdate(GenericTask task, MessageData param) {
            BaseResponse a = new BaseResponse(param.getMsgBody());
            mTextView.append(a.toString() + "\n");
//            App.sInstance.setMasterMac(param.getMacString());
//            mDevice = ((GetClientResponse) a).getDevices();
        }

        @Override
        public void onCancelled(GenericTask task) {
            arrTask.remove(task);
            String name = task.toString();
            name = name.substring(name.lastIndexOf('.') + 1);
            mTextView.append("<= " + name + "Cancelled. =>\n");
        }
    }
}

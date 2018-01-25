package com.changhong.wifiairscout.ui.activity;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;

import com.changhong.wifiairscout.R;

/**
 * Created by fuheng on 2017/12/12.
 */

public class DeviceLocationDetailActivity extends DeviceDetailActivity implements TextWatcher {
    private String mNickName;
    private String mMac;
    private Intent mIntent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNickName = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        mMac = getIntent().getStringExtra(Intent.EXTRA_ASSIST_INPUT_DEVICE_ID);

        mTvDeviceName.addTextChangedListener(this);
    }

    @Override
    protected void setEditTextDeable() {

    }

    @Override
    protected void reset() {
        super.reset();

        mTvDeviceName.setHint(device == null ? null : device.getName());
        if (!TextUtils.isEmpty(mNickName))
            mTvDeviceName.setText(mNickName);
        else
            mTvDeviceName.setText(null);

        mTvMac.setText(mMac);

        if (device == null)
            getSupportActionBar().setTitle(getString(R.string.title_device_detail) + "(" + getString(R.string.offline) + ")");
        else
            getSupportActionBar().setTitle(R.string.title_device_detail);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {

        if (editable == null || editable.length() == 0) {
            if (TextUtils.isEmpty(mNickName))
                setResult(RESULT_CANCELED);
            else
                setResult(RESULT_OK, getResponseInstent(null));
        } else {
            if (editable.toString().equals(mNickName))
                setResult(RESULT_CANCELED);
            else
                setResult(RESULT_OK, getResponseInstent(editable.toString()));
        }
    }

    private Intent getResponseInstent(String string) {
        if (mIntent == null) {
            mIntent = new Intent();
            mIntent.putExtra(Intent.EXTRA_ASSIST_INPUT_DEVICE_ID, mMac);
        }
        mIntent.putExtra(Intent.EXTRA_TEXT, string);
        return mIntent;
    }
}

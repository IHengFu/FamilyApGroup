package com.changhong.wifiairscout.ui.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.changhong.wifiairscout.R;
import com.changhong.wifiairscout.db.DBHelper;
import com.changhong.wifiairscout.preferences.Preferences;
import com.changhong.wifiairscout.utils.CommUtils;

/**
 * Created by Administrator on 2018/1/9.
 */

public class OperationActivity extends AppCompatActivity implements View.OnClickListener {

    private AppCompatEditText mEtRssi;
    private AppCompatSpinner mSpinner;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_operation);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.mipmap.ic_back);
        toolbar.setTitle(R.string.action_settings);

        mSpinner = findViewById(R.id.spinner);
        mSpinner.setSelection(Preferences.getIntance().getMapShowStyle());
        mEtRssi = findViewById(R.id.et_rssi);
        mEtRssi.setText(String.valueOf(Preferences.getIntance().getRssiLimitValue()));

        findViewById(R.id.btn_cancle).setOnClickListener(this);
        findViewById(R.id.btn_save).setOnClickListener(this);
        findViewById(R.id.btn_clean).setOnClickListener(this);

        CommUtils.transparencyBar(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_cancle:
                finish();
                break;
            case R.id.btn_save:
                doSaveAction();
                finish();
                break;
            case R.id.btn_clean:
                askForCleanProgramme();
                break;
        }
    }

    private void askForCleanProgramme() {
        new AlertDialog.Builder(this).setMessage(R.string.ask_delete_recode).setPositiveButton(R.string.action_give_up, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).setNegativeButton(R.string.action_continue, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                DBHelper.getHelper(OperationActivity.this).clear();
                dialogInterface.dismiss();
            }
        }).create().show();
    }

    private void doSaveAction() {
        boolean isChanged = false;

        int style = mSpinner.getSelectedItemPosition();
        if (style != Preferences.getIntance().getMapShowStyle()) {
            Preferences.getIntance().setMapShowStyle(style);
            isChanged = true;
        }

        if (mEtRssi.getText().length() > 0) {
            int value = Integer.parseInt(mEtRssi.getText().toString());
            if (value != Preferences.getIntance().getRssiLimitValue()) {
                Preferences.getIntance().setRssiLimitValue(value);
                isChanged = true;
            }
        }

        if (isChanged)
            setResult(RESULT_OK);
    }
}

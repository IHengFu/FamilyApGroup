package wifiairscout.changhong.com.wifiairscout.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import wifiairscout.changhong.com.wifiairscout.R;
import wifiairscout.changhong.com.wifiairscout.preferences.Preferences;
import wifiairscout.changhong.com.wifiairscout.utils.CommUtils;

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
        }
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

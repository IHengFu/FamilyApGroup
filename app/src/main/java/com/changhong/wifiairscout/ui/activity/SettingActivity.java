package com.changhong.wifiairscout.ui.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.changhong.wifiairscout.App;
import com.changhong.wifiairscout.R;
import com.changhong.wifiairscout.model.WifiDevice;
import com.changhong.wifiairscout.preferences.Preferences;
import com.changhong.wifiairscout.utils.CommUtils;

/**
 * Created by fuheng on 2017/12/12.
 */

public class SettingActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText editReceivePort;
    private EditText editSendPort;
    private EditText editSevicePort;
    private EditText editSeviceIp;
    private TextView tvNetGate;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_setting);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editReceivePort = findViewById(R.id.et_receive_port);
        editReceivePort.addTextChangedListener(PortWatcher);
        editSendPort = findViewById(R.id.et_send_port);
        editSendPort.addTextChangedListener(PortWatcher);
        editSevicePort = findViewById(R.id.et_service_port);
        editSevicePort.addTextChangedListener(PortWatcher);
        editSeviceIp = findViewById(R.id.et_service_ip);

        TextView tvMyIp = findViewById(R.id.tv_ip);
        tvMyIp.setText(WifiDevice.Companion.toStringIp(App.sInstance.getWifiInfo().getIpAddress()));

        tvNetGate = findViewById(R.id.tv_default_net_gate);
        tvNetGate.setText(WifiDevice.Companion.toStringIp(App.sInstance.getDhcpInfo().gateway));

        findViewById(R.id.btn_set).setOnClickListener(this);

        editReceivePort.setText(String.valueOf(Preferences.getIntance().getReceivePort()));
        editSendPort.setText(String.valueOf(Preferences.getIntance().getSendPort()));
        editSevicePort.setText(String.valueOf(Preferences.getIntance().getServerPort()));
        editSeviceIp.setText(WifiDevice.Companion.toStringIp(Preferences.getIntance().getServerIp()));

        CommUtils.transparencyBar(this);
    }

    private TextWatcher PortWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (editable == null || editable.length() == 0)
                return;
            int value = 0;
            try {
                Integer.parseInt(editable.toString());
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return;
            }
            if (value > 65535) {
                editable.clear();
                editable.append(String.valueOf(65535));
            }
        }
    };

    @Override
    public void onBackPressed() {
        if (!checkChanged()) {
            super.onBackPressed();
            return;
        }

        new AlertDialog.Builder(this).setMessage(R.string.ask_save_or_not).setPositiveButton(R.string.action_give_up, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                finish();
            }
        }).setNegativeButton(R.string.action_continue, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).create().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_option, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                if (checkChanged()) {
                    saveEdit();
                    setResult(RESULT_OK);
                }
                finish();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean checkChanged() {
        int sendPort = CommUtils.toInt(editSendPort.getText().toString(), Preferences.getIntance().getSendPort());
        int receivePort = CommUtils.toInt(editReceivePort.getText().toString(), Preferences.getIntance().getReceivePort());
        int ip = WifiDevice.Companion.toIpValue(editSeviceIp.getText().toString());
        int servicePort = CommUtils.toInt(editSevicePort.getText().toString(), Preferences.getIntance().getServerPort());
        return isChanged(sendPort, receivePort, ip, servicePort);
    }

    private boolean isChanged(int sendPort, int receiveport, int serviceip, int serviceport) {
        if (sendPort != Preferences.getIntance().getSendPort())
            return true;
        if (receiveport != Preferences.getIntance().getReceivePort())
            return true;
        if (serviceip != Preferences.getIntance().getServerIp())
            return true;
        if (serviceport != Preferences.getIntance().getServerPort())
            return true;
        return false;
    }


    /**
     * 保存设置
     */
    private void saveEdit() {
        String receivePort = editReceivePort.getText().toString();
        if (receivePort != null) {
            Preferences.getIntance().setReceivePort(Integer.parseInt(receivePort));
        } else {
            Preferences.getIntance().removeKey(Preferences.KEY_RECEIVE_PORT);
        }

        String sendPort = editSendPort.getText().toString();
        if (sendPort != null) {
            Preferences.getIntance().setSendPort(Integer.parseInt(sendPort));
        } else {
            Preferences.getIntance().removeKey(Preferences.KEY_SEND_PORT);
        }

        String serviceip = editSeviceIp.getText().toString();
        if (serviceip != null) {
            Preferences.getIntance().setServerIp(WifiDevice.Companion.toIpValue(serviceip));
        } else {
            Preferences.getIntance().removeKey(Preferences.KEY_SERVICE_IP);
        }

        String serviceport = editSevicePort.getText().toString();
        if (serviceport != null)
            Preferences.getIntance().setServerPort(Integer.parseInt(serviceport));
        else
            Preferences.getIntance().removeKey(Preferences.KEY_SERVICE_PORT);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_set:
                editSeviceIp.setText(tvNetGate.getText());
                break;
        }
    }
}

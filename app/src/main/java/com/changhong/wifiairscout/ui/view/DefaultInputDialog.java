package com.changhong.wifiairscout.ui.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.changhong.wifiairscout.R;
import com.changhong.wifiairscout.utils.KeyboardUtils;

/**
 * Created by fuheng on 2018/1/22.
 */

public class DefaultInputDialog extends Dialog implements View.OnClickListener {

    private final EditText mEditText;
    private final TextView mTextTab;
    private OnInputDialogCommitListener mListener;

    public DefaultInputDialog(@NonNull Context context) {
        super(context, 0);
        setContentView(R.layout.dialog_input);

        findViewById(R.id.btn_accept).setOnClickListener(this);
        findViewById(R.id.btn_cancle).setOnClickListener(this);
        mEditText = findViewById(R.id.et_input);
        mTextTab = findViewById(R.id.tv_tab);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_accept:
                if (mListener != null) {
                    mListener.onCommit(this, mEditText.getText().toString(), true);
                }
                break;
            case R.id.btn_cancle:
                dismiss();
                break;
        }

    }

    public void setInputType(int type) {
        mEditText.setInputType(type);
    }

    public void show() {
        super.show();
        mEditText.requestFocus();
    }

    @Override
    public void setCancelable(boolean flag) {
        findViewById(R.id.btn_cancle).setVisibility(flag ? View.VISIBLE : View.GONE);
        super.setCancelable(flag);
    }

    public void setOnCommitListener(@Nullable OnInputDialogCommitListener listener) {
        mListener = listener;
    }

    public void setHint(CharSequence c) {
        mEditText.setHint(c);

    }

    public void setHint(int resid) {
        mEditText.setHint(resid);

    }

    public void setTab(CharSequence c) {
        mTextTab.setText(c);
        mTextTab.setVisibility(TextUtils.isEmpty(c) ? View.GONE : View.VISIBLE);
    }

    public void setTab(int resid) {
        mTextTab.setText(resid);
        mTextTab.setVisibility(resid == 0 ? View.GONE : View.VISIBLE);
    }

    public interface OnInputDialogCommitListener {
        void onCommit(DialogInterface var1, String var2, boolean var3);
    }
}

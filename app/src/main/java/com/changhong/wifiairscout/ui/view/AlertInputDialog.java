package com.changhong.wifiairscout.ui.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.changhong.wifiairscout.R;

/**
 * Created by fuheng on 2018/1/22.
 */

public class AlertInputDialog {

    private final EditText mEditText;
    private final AlertDialog dialog;
    private OnInputDialogCommitListener mListener;

    public AlertInputDialog(@NonNull Context context) {
        mEditText = new EditText(context);
        mEditText.setMaxLines(1);
        mEditText.setMaxEms(10);
        dialog = new AlertDialog.Builder(context).setView(mEditText).setPositiveButton(R.string.action_commit, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (mListener != null)
                    mListener.onCommit(dialogInterface, mEditText.getText().toString(), true);
            }
        }).setNegativeButton(R.string.action_cancel, null).create();
    }

    public void show() {
        mEditText.requestFocus();
        dialog.show();
    }

    public void setTitle(CharSequence chs) {
        dialog.setTitle(chs);
    }

    public void setTitle(int resid) {
        dialog.setTitle(resid);
    }

    public void setInputType(int type){
        mEditText.setInputType(type);
    }

    public void setOnCommitListener(@Nullable OnInputDialogCommitListener listener) {
        mListener = listener;
    }

    public static interface OnInputDialogCommitListener {
        void onCommit(DialogInterface var1, String var2, boolean var3);
    }
}

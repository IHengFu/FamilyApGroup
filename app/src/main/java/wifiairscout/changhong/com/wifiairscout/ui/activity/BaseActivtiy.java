package wifiairscout.changhong.com.wifiairscout.ui.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

/**
 * Created by Administrator on 2018/1/15.
 */

public class BaseActivtiy extends AppCompatActivity {
    private ProgressDialog mProgressDialog;
    private Toast mToast;

    protected void showProgressDialog(CharSequence cs, boolean cancelable, DialogInterface.OnCancelListener listener) {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog.show(this, null, cs, true, cancelable, listener);
        } else {
            mProgressDialog.dismiss();
            mProgressDialog.setMessage(cs);
            mProgressDialog.setCancelable(cancelable);
            mProgressDialog.setOnCancelListener(listener);
            mProgressDialog.show();
        }
    }

    protected void hideProgressDialog(){
        mProgressDialog.dismiss();
    }

    protected void showToast(CharSequence cs) {
        if (mToast == null)
            mToast = Toast.makeText(this, cs, Toast.LENGTH_SHORT);
        else
            mToast.setText(cs);
        mToast.show();
    }
}

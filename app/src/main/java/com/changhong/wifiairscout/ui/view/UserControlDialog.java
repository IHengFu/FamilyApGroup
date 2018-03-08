package com.changhong.wifiairscout.ui.view;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.changhong.wifiairscout.R;

import java.util.List;

/**
 * Created by fuheng on 2018/1/22.
 */

public class UserControlDialog extends Dialog implements View.OnClickListener {

    private final EditText mEditText;
    private final TextView mTextTab;
    private final ViewSwitcher mViewSwticher01;
    private final RadioGroup mRadioGroup;
    private final TextView mTextTitle;
    private DefaultInputDialog.OnInputDialogCommitListener mListener;

    private List<String> mChoices;

    public UserControlDialog(@NonNull Context context) {
        super(context, 0);
        setContentView(R.layout.dialog_user_choose);

        findViewById(R.id.btn_accept).setOnClickListener(this);
        findViewById(R.id.btn_cancle).setOnClickListener(this);

        mRadioGroup = findViewById(R.id.radiogroup01);
        mViewSwticher01 = findViewById(R.id.viewswticher01);
        mTextTitle = findViewById(R.id.tv_title);
        mEditText = findViewById(R.id.et_input);
        mTextTab = findViewById(R.id.tv_tab);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_accept:

                if (mViewSwticher01.getDisplayedChild() == 0) {//自定义
                    String string = mEditText.getText().toString();
                    if (mChoices != null && !mChoices.isEmpty() && mChoices.contains(string)) {
                        // TODO NOTICIE
                        Toast.makeText(getContext(), "", Toast.LENGTH_SHORT).show();
                        break;
                    } else if (mListener != null) {
                        mListener.onCommit(this, string, true);
                    }
                } else {//选择已有
                    int id = getGroupChechIndex();
                    if (id == -1)
                        break;
                    else if (id == mChoices.size()) {
                        mViewSwticher01.showNext();
                        break;
                    } else if (mListener != null) {
                        mListener.onCommit(this, mChoices.get(id), true);
                    }

                }

                break;
            case R.id.btn_cancle:
                dismiss();
                break;
        }

    }

    public void setChoices(List<String> choice) {
        mChoices = choice;
        if (mChoices == null || mChoices.isEmpty())
            mViewSwticher01.setDisplayedChild(0);
        else {
            mRadioGroup.removeAllViews();
            for (String s : choice) {
                RadioButton radiobutton = new RadioButton(mRadioGroup.getContext());
                radiobutton.setText(s);
                mRadioGroup.addView(radiobutton);
            }

            {//多增加个其它
                RadioButton radiobutton = new RadioButton(mRadioGroup.getContext());
                radiobutton.setText(R.string.new_user);
                mRadioGroup.addView(radiobutton);
            }
            mViewSwticher01.setDisplayedChild(1);
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

    public void setHint(CharSequence c) {
        mEditText.setHint(c);

    }

    public void setHint(int resid) {
        mEditText.setHint(resid);

    }

    public void setTitle(CharSequence c) {
        mTextTitle.setText(c);
        mTextTitle.setVisibility(TextUtils.isEmpty(c) ? View.GONE : View.VISIBLE);
    }

    public void setTitle(int resid) {
        mTextTitle.setText(resid);
        mTextTitle.setVisibility(resid == 0 ? View.GONE : View.VISIBLE);
    }

    public void setTab(CharSequence c) {
        mTextTab.setText(c);
        mTextTab.setVisibility(TextUtils.isEmpty(c) ? View.GONE : View.VISIBLE);
    }

    public void setTab(int resid) {
        mTextTab.setText(resid);
        mTextTab.setVisibility(resid == 0 ? View.GONE : View.VISIBLE);
    }

    private int getGroupChechIndex() {
        int id = mRadioGroup.getCheckedRadioButtonId();
        if (id == -1)
            return id;
        return mRadioGroup.indexOfChild(mRadioGroup.findViewById(id));
    }

    public void setOnCommitListener(DefaultInputDialog.OnInputDialogCommitListener listener) {
        this.mListener = listener;
    }
}

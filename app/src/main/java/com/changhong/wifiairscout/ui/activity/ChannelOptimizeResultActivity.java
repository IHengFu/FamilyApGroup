package com.changhong.wifiairscout.ui.activity;

import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import com.changhong.wifiairscout.R;

/**
 * Created by fuheng on 2018/2/4.
 */

public class ChannelOptimizeResultActivity extends BaseActivtiy implements View.OnClickListener {
    private ExpandableListView mExpandableListView;
    private ExpandableListAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_channel_optimize_result);

        mExpandableListView = findViewById(R.id.list_channelOptimizeCompare);
        findViewById(R.id.btn_accept).setOnClickListener(this);

        mExpandableListView.setAdapter(mAdapter);
    }

    @Override
    public void onClick(View view) {
        finish();
    }
    

}

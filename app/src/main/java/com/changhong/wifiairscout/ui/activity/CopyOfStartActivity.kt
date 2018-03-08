package com.changhong.wifiairscout.ui.activity

import android.os.Bundle
import com.changhong.wifiairscout.App


/**
 * Created by fuheng on 2018/03/08
 */

class CopyOfStartActivity : StartActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        App.sTest = true
        super.onCreate(savedInstanceState)
    }

}

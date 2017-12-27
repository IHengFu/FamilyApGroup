package wifiairscout.changhong.com.wifiairscout.utils.timecontor

import android.os.CountDownTimer

/**
 * Created by fuheng on 2017/12/11.
 */

class MyCountDownTimer(millisInFuture: Long, countDownInterval: Long, val mListener: MyCountDownTimerListener?) : CountDownTimer(millisInFuture, countDownInterval) {

    override fun onTick(l: Long) {
        mListener?.onTick(l, this)
    }

    override fun onFinish() {
        mListener?.onFinish(this)
    }
}

interface MyCountDownTimerListener {
    fun onTick(l: Long, timer: CountDownTimer)

    fun onFinish(timer: CountDownTimer)
}

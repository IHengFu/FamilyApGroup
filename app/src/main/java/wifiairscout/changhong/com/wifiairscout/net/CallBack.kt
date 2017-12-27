package wifiairscout.changhong.com.wifiairscout.net

/**
 * Created by fuheng on 2017/12/12.
 */

 interface CallBack {
    fun onSendCallback(data: ByteArray)

    fun onError(e :String?)
}

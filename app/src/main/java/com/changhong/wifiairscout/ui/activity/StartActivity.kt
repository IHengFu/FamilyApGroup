package com.changhong.wifiairscout.ui.activity

import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.changhong.wifiairscout.App
import com.changhong.wifiairscout.R
import com.changhong.wifiairscout.model.DualBandInfo
import com.changhong.wifiairscout.model.MessageData
import com.changhong.wifiairscout.model.MessageDataFactory
import com.changhong.wifiairscout.model.WifiDevice
import com.changhong.wifiairscout.model.response.GetMasterResponse
import com.changhong.wifiairscout.model.response.RegisterResponse
import com.changhong.wifiairscout.preferences.Preferences
import com.changhong.wifiairscout.task.GenericTask
import com.changhong.wifiairscout.task.TaskListener
import com.changhong.wifiairscout.task.TaskResult
import com.changhong.wifiairscout.task.UDPTask
import com.changhong.wifiairscout.utils.CommUtils


/**
 * Created by fuheng on 2017/12/14.
 */

open class StartActivity : BaseActivtiy() {

    private val tv_status: TextView by lazy { findViewById<TextView>(R.id.tv_status) }

    var mUdpTask: UDPTask? = null

    var alertDialog: AlertDialog? = null

    private var arrayDevice: ArrayList<WifiDevice>? = null

    private var mIsWaitForNextPage = false

    private var startTime: Long = System.currentTimeMillis()

    private var mMaster: WifiDevice? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.layout_start)
        (findViewById<ImageView>(R.id.icon).drawable as Animatable).start()
        init()

        startTime = System.currentTimeMillis()
    }

    fun init() {
        if (!CommUtils.isWifiConnected(this)) {
            showAlertDialogConnectWifi()
        } else
            if (App.sTest) {
                mMaster = createTestWifiDevice()
                Handler().postDelayed(Runnable { goNext() }, 2000)
            } else
                startConnectMaster()
    }

    override fun onDestroy() {
        mUdpTask?.cancle()
        super.onDestroy()
    }

    companion object {
        const val REQUEST_OPRATION = 2 //设置请求的code
        const val REQUEST_SETTING_WIFI = 3 //wifi设置请求的code
    }

    private fun startConnectMaster() {
        val msg = MessageDataFactory.getRegisterMessage()
        if (mUdpTask?.isCancelled == false)
            mUdpTask?.cancle()
        mUdpTask = UDPTask().execute(msg, mRegisterListener)

    }


    fun showAlertDialog() {
        var dialog = AlertDialog.Builder(this@StartActivity).setMessage(R.string.cannot_connect_master).setPositiveButton(R.string.action_exit) { dialogInterface, _ ->
            dialogInterface.dismiss()
            finish()
        }.setNeutralButton(R.string.action_settings) { dialogInterface, _ ->
            dialogInterface.dismiss()
            startActivityForResult(Intent(this@StartActivity, SettingActivity::class.java), REQUEST_OPRATION)
        }.setNegativeButton(R.string.action_retry, { dialogInterface, _ ->
            dialogInterface.dismiss()
            startConnectMaster()
        }).create()
        dialog?.setCancelable(false)
        dialog?.show()
    }

    fun showAlertDialogConnectWifi() {
        var dialog = AlertDialog.Builder(this@StartActivity).setMessage(R.string.notice_connect_wifi)
                .setPositiveButton(R.string.action_exit) { dialogInterface, _ ->
                    dialogInterface.dismiss()
                    finish()
                }.setNegativeButton(R.string.action_settings, { dialogInterface, _ ->
            dialogInterface.dismiss()
            startActivityForResult(Intent(android.provider.Settings.ACTION_WIFI_SETTINGS), REQUEST_SETTING_WIFI)
        }).create()
        dialog?.setCancelable(true)
        dialog?.setOnCancelListener() { p0 ->
            p0.dismiss()
            finish()
        }
        dialog?.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == MainActivity.REQUEST_OPRATION) {
            if (resultCode != AppCompatActivity.RESULT_OK) {
                showAlertDialog()
            } else
                startConnectMaster()
        } else if (requestCode == REQUEST_SETTING_WIFI) {
            init()
        }
    }

    fun goNext() {
        val intent = Intent(this@StartActivity, MainActivity::class.java)
        intent.putExtra(Intent.EXTRA_DATA_REMOVED, mMaster)
        startActivity(intent)
        finish()
    }


    inner abstract class UDPTaskListner(val n: String) : TaskListener<MessageData> {

        override fun onPreExecute(task: GenericTask?) {
            tv_status.text = n
        }

        override fun onCancelled(task: GenericTask?) {
            if (task?.exception != null)
                Toast.makeText(this@StartActivity, task.exception?.message ?: "", Toast.LENGTH_SHORT).show()
        }

        override fun getName(): String {
            return String()
        }

    }

    private val mRegisterListener by lazy {
        object : UDPTaskListner(getString(R.string.notice_init)) {
            override fun onPostExecute(task: GenericTask?, result: TaskResult?) {
                if (result == TaskResult.OK) {
//                    if (System.currentTimeMillis() - startTime > 2000)
//                        goNext()
//                    else
//                        Handler().postDelayed(Runnable { goNext() }, 2000 - System.currentTimeMillis() + startTime)
                    startLoadMaster()
                } else if (task?.exception != null) {
                    Toast.makeText(this@StartActivity, task.exception?.message ?: task.exception?.javaClass?.simpleName, Toast.LENGTH_SHORT).show()
                    showAlertDialog()
                }
            }

            override fun onProgressUpdate(task: GenericTask?, param: MessageData?) {
                val rr = RegisterResponse(param?.msgBody)

                App.sInstance.wlanIndexObject = rr.wlanCondition

                Preferences.getIntance().setMaxMsgBody(rr.max_msg_body_len)
                Preferences.getIntance().setKeepAliveInterval(rr.keepalive_interval)
                App.sInstance.masterMac = param?.macString
                if (App.sInstance.wifiInfo.frequency >= 5000)
                    App.sInstance.curWlanIdx = 0
                else
                    App.sInstance.curWlanIdx = 1
                App.sInstance.curChannel = rr.getCurrentWlanIdx(App.sInstance.curWlanIdx)
            }
        }
    }

    fun showRetryDialog(listener: DialogInterface.OnClickListener) {
        if (alertDialog == null)
            alertDialog = AlertDialog.Builder(this@StartActivity).setMessage(R.string.ask_retry).setPositiveButton(R.string.action_exit) { dialogInterface, _ ->
                dialogInterface.dismiss()
                finish()
            }.create()
        alertDialog?.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.action_retry), listener)
        alertDialog?.show()
    }

    private fun startLoadMaster() {
        val msg = MessageDataFactory.getMasterInfo(App.sInstance.masterMac)

        UDPTask().execute(msg, mLoadMasterListener)
    }


    private val mLoadMasterListener = object : UDPTaskListner("获取Master信息……") {

        override fun onPostExecute(task: GenericTask?, result: TaskResult?) {
            if (result != TaskResult.OK) {
                showRetryDialog(object : DialogInterface.OnClickListener {
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        startLoadMaster()
                        p0?.dismiss()
                    }
                })
                return;
            }

            if (System.currentTimeMillis() - startTime > 2000)
                goNext()
            else
                Handler().postDelayed(Runnable { goNext() }, 2000 - System.currentTimeMillis() + startTime)
        }

        override fun onProgressUpdate(task: GenericTask?, param: MessageData?) {
            val response = GetMasterResponse(param?.msgBody)
            val master = response.master
            master.name = getString(R.string.wifi)
            master.rssi = App.MAX_RSSI
            master.type = App.TYPE_DEVICE_WIFI
            mMaster = master
            App.sInstance.curWlanIdx = master.wlan_idx
        }
    }

    private fun createTestWifiDevice(): WifiDevice {//test
        val dhcp = App.sInstance.dhcpInfo
        val wifiinfo = App.sInstance.wifiInfo
        val master = WifiDevice(App.TYPE_DEVICE_WIFI, WifiDevice.toStringIp(dhcp.ipAddress),
                wifiinfo.bssid, getString(R.string.wifi))
        master.rssi = 100.toByte()

        val arrDualBInfo = ArrayList<DualBandInfo>()
        arrDualBInfo.add(DualBandInfo(0, 44, 0, 0, "test1", 0, 0, null))
        arrDualBInfo.add(DualBandInfo(1, 12, 0, 0, "test1", 0, 0, null))
        master.dual_band = 1
        master.arrayDualBandInfo = arrDualBInfo
        return master
    }
}

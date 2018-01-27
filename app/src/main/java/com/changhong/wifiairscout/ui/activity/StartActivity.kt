package com.changhong.wifiairscout.ui.activity

import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.changhong.wifiairscout.App

import com.changhong.wifiairscout.R
import com.changhong.wifiairscout.model.MessageData
import com.changhong.wifiairscout.model.MessageDataFactory
import com.changhong.wifiairscout.model.WifiDevice
import com.changhong.wifiairscout.model.response.GetClientResponse
import com.changhong.wifiairscout.model.response.GetClientStatusResponse
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

class StartActivity : AppCompatActivity() {

    private val tv_status: TextView by lazy { findViewById<TextView>(R.id.tv_status) }

    var mUdpTask: UDPTask? = null

    var alertDialog: AlertDialog? = null
    private var currentChannel = 0.toByte()
    private var arrayDevice: ArrayList<WifiDevice>? = null

    private var mIsWaitForNextPage = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_start)
        (findViewById<ImageView>(R.id.icon).drawable as Animatable).start()
        init()
    }

    fun init() {
        if (!CommUtils.isWifiConnected(this)) {
            showAlertDialogConnectWifi()
        } else
            if (App.sTest) {
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

    private fun startLoadDevice() {
        val msg = MessageDataFactory.getAllClientInfo()

        if (mUdpTask?.isCancelled == false)
            mUdpTask?.cancle()
        mUdpTask = UDPTask().execute(msg, mLoadDeviceListener)

    }

    private fun startLoadDeviceStatus() {
        val msg = MessageDataFactory.getAllClientStatus()

        if (mUdpTask?.isCancelled == false)
            mUdpTask?.cancle()
        mUdpTask = UDPTask().execute(msg, mLoadDeviceStatusListener)
    }

    private fun startLoadMaster() {
        val msg = MessageDataFactory.getMasterInfo(App.sInstance.masterMac)

        if (mUdpTask?.isCancelled == false)
            mUdpTask?.cancle()
        mUdpTask = UDPTask().execute(msg, mLoadMasterListener)
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
        //将信道统一
//        arrayDevice?.forEach { it ->
//            it.channel = currentChannel
//        }

        val intent = Intent(this@StartActivity, MainActivity::class.java)
//        intent.putParcelableArrayListExtra(Intent.EXTRA_DATA_REMOVED, arrayDevice)
        intent.putExtra(Intent.EXTRA_CC, currentChannel)
        startActivity(intent)

//        startService(Intent(this@StartActivity, StartService::class.java))
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
//                alertDialog = null//将此对象滞空，下一歩需要新的dialog
                } else if (task?.exception != null) {
                    Toast.makeText(this@StartActivity, task.exception?.message ?: task.exception?.javaClass?.simpleName, Toast.LENGTH_SHORT).show()
                    showAlertDialog()
                }
            }

            override fun onProgressUpdate(task: GenericTask?, param: MessageData?) {
                val rr = RegisterResponse(param?.msgBody)
                Preferences.getIntance().setMaxMsgBody(rr.max_msg_body_len)
                Preferences.getIntance().setKeepAliveInterval(rr.keepalive_interval)
                currentChannel = rr.channel
                App.sInstance.masterMac = param?.macString
                App.sInstance.curChannel=rr.channel
                App.sInstance.curWlanIdx=rr.wlan_idx
//            startLoadDevice()
                goNext()
            }
        }
    }

    private val mLoadDeviceListener by lazy {
        object : UDPTaskListner(getString(R.string.notice_get_device)) {

            override fun onProgressUpdate(task: GenericTask?, param: MessageData?) {

                val gcr = GetClientResponse(param?.msgBody)
                arrayDevice = gcr.devices

            }

            override fun onPostExecute(task: GenericTask?, result: TaskResult?) {
                if (result == TaskResult.OK) {
                    startLoadMaster()
                    return
                }

                if (task?.exception != null)
                    Toast.makeText(this@StartActivity, task.exception?.message ?: task.exception?.javaClass?.simpleName, Toast.LENGTH_SHORT).show()
                showRetryDialog(DialogInterface.OnClickListener { p0, _ ->
                    p0?.dismiss()
                    startLoadDeviceStatus()
                })

                alertDialog?.show()
            }
        }
    }

    private val mLoadDeviceStatusListener by lazy {
        object : UDPTaskListner(getString(R.string.notice_get_device_status)) {

            override fun onPostExecute(task: GenericTask?, result: TaskResult?) {
                Log.e(this@StartActivity.javaClass.simpleName, "task end " + result)
                if (result == TaskResult.OK)
                    return

                if (task?.exception != null)
                    Toast.makeText(this@StartActivity, task.exception?.message ?: task.exception?.javaClass?.simpleName, Toast.LENGTH_SHORT).show()
                showRetryDialog(DialogInterface.OnClickListener { p0, _ ->
                    p0?.dismiss()
                    startLoadDeviceStatus()
                })

                alertDialog?.show()
            }

            override fun onProgressUpdate(task: GenericTask?, param: MessageData?) {

                val gcsr = GetClientStatusResponse(param?.msgBody)

                var arrayStatus = gcsr.devices
                //嵌入信号强度
                if (arrayStatus != null)
                    for (wifiDevice in arrayStatus) {
                        arrayDevice?.forEach { it ->
                            if (wifiDevice.mac.equals(it.mac)) {
                                it.rssi = wifiDevice.rssi
                            }
                        }
                    }

                val localMac = App.sInstance.wifiInfo.macAddress;
                for (device in arrayStatus)
                    if (localMac.equals(device.mac)) {
                        device.name = getString(R.string.my_phone)
                        device.type = App.TYPE_DEVICE_PHONE
                    } else {
                        device.type = App.TYPE_DEVICE_CLIENT
                        device.rssi = (device.rssi - 100).toByte()
                    }

                goNext()
            }

        }
    }

    private val mLoadMasterListener by lazy {
        object : UDPTaskListner(getString(R.string.notice_get_master)) {

            override fun onPostExecute(task: GenericTask?, result: TaskResult?) {
                if (result == TaskResult.OK) {
                    startLoadDeviceStatus()
                    return
                }

                if (task?.exception != null)
                    Toast.makeText(this@StartActivity, task.exception?.message ?: task.exception?.javaClass?.simpleName, Toast.LENGTH_SHORT).show()
                showRetryDialog(DialogInterface.OnClickListener { p0, _ ->
                    p0?.dismiss()
                    startLoadDeviceStatus()
                })

                alertDialog?.show()
            }

            override fun onProgressUpdate(task: GenericTask?, param: MessageData?) {

                Log.e(javaClass.simpleName, CommUtils.toHexString(param?.msgBody))
                val master = GetMasterResponse(param?.msgBody).master

                arrayDevice?.add(master!!)
            }

            override fun onCancelled(task: GenericTask?) {
                if (task?.exception != null)
                    Toast.makeText(this@StartActivity, task.exception?.message, Toast.LENGTH_SHORT).show()
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
}

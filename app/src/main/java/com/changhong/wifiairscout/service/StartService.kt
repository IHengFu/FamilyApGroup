package com.changhong.wifiairscout.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.util.Log

import org.greenrobot.eventbus.EventBus
import com.changhong.wifiairscout.App
import com.changhong.wifiairscout.R
import com.changhong.wifiairscout.model.MessageData
import com.changhong.wifiairscout.model.MessageDataFactory
import com.changhong.wifiairscout.model.WifiDevice
import com.changhong.wifiairscout.model.response.*
import com.changhong.wifiairscout.task.GenericTask
import com.changhong.wifiairscout.task.TaskListener
import com.changhong.wifiairscout.task.TaskResult
import com.changhong.wifiairscout.task.UDPTask
import com.changhong.wifiairscout.utils.CommUtils

/**
 * Created by fuheng on 2017/12/15.
 */

class StartService : Service() {

    companion object {
        const val ACTION_START_ALL = 0
        const val ACTION_LOAD_DEVICE = 1
        const val ACTION_LOAD_DEVICE_STATUS = 2
        const val ACTION_LOAD_MASTER = 3
        const val ACTION_CHANGE_CUR_CHANNEL = 5

        fun startService(context: Context, action: Int) {
            val intent = Intent(context, StartService::class.java)
            intent.putExtra(Intent.EXTRA_INDEX, action)
            context.startService(intent)
        }

        fun startService(context: Context, action: Int, extraInt: Int) {
            val intent = Intent(context, StartService::class.java)
            intent.putExtra(Intent.EXTRA_INDEX, action)
            intent.putExtra(Intent.EXTRA_CHOSEN_COMPONENT, extraInt)
            context.startService(intent)
        }

    }

    val handler = Handler()

    private var arrTask = ArrayList<GenericTask>()

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        when (intent.getIntExtra(Intent.EXTRA_INDEX, 0)) {
            ACTION_START_ALL -> {
                startLoadMaster()
                startLoadDevice()
                startLoadDeviceStatus()
            }

            ACTION_LOAD_MASTER -> startLoadMaster()
            ACTION_LOAD_DEVICE_STATUS -> startLoadDeviceStatus()
            ACTION_LOAD_DEVICE -> startLoadDevice()

            ACTION_CHANGE_CUR_CHANNEL -> changeCurrentChannel(intent.getIntExtra(Intent.EXTRA_CHOSEN_COMPONENT, -1))

        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        arrTask.forEach { it.cancle() }
        super.onDestroy()
    }

    private fun startConnectMaster() {
        val msg = MessageDataFactory.getRegisterMessage()

        UDPTask().execute(msg, mRegisterListener)
    }

    private fun startLoadDevice() {
        val msg = MessageDataFactory.getAllClientInfo()

        UDPTask().execute(msg, mLoadDeviceListener)

    }

    private fun startLoadDeviceStatus() {
        val msg = MessageDataFactory.getAllClientStatus()

        UDPTask().execute(msg, mLoadDeviceStatusListener)
    }

    private fun startLoadMaster() {
        if (App.sInstance.masterMac == null)
            return

        val msg = MessageDataFactory.getMasterInfo(App.sInstance.masterMac)

        UDPTask().execute(msg, mLoadMasterListener)
    }

    private fun changeCurrentChannel(channel: Int) {
        if (channel == -1)
            return
        val msg = MessageDataFactory.setChannel(channel, App.sInstance.curWlanIdx, App.sInstance.masterMac)

        UDPTask().execute(msg, object : UDPTaskListner("修改到信道$channel……", 1, 500) {
            override fun onProgressUpdate(task: GenericTask?, param: MessageData?) {
//                loadCurrentChannel()
                startLoadMaster()
            }

        })
    }

    inner abstract class UDPTaskListner(val n: String, var retryTimes: Int, val retryInterval: Long) : TaskListener<MessageData> {

        override fun onPreExecute(task: GenericTask?) {
            if (!arrTask.contains(task))
                arrTask.add(task!!)
        }

        override fun onPostExecute(task: GenericTask?, result: TaskResult?) {
            if (arrTask.contains(task))
                arrTask.remove(task!!)
            closeSelf()
        }

        override fun onCancelled(task: GenericTask?) {
            if (arrTask.contains(task))
                arrTask.remove(task!!)
        }

        override fun getName(): String {
            return String()
        }


        fun retry(runnable: Runnable) {
            if (--retryTimes == 0)
                return
            if (retryInterval > 0)
                handler.postDelayed(runnable, retryInterval)
            else
                handler.post(runnable)
        }

    }

    private val mRegisterListener = object : UDPTaskListner("初始化……", 3, 500) {
        override fun onPostExecute(task: GenericTask?, result: TaskResult?) {
            if (result == TaskResult.OK) {
            } else if (task?.exception != null)
                retry(Runnable { startConnectMaster() })
            super.onPostExecute(task, result)
        }

        override fun onProgressUpdate(task: GenericTask?, param: MessageData?) {
            val rr = RegisterResponse(param?.msgBody)
            EventBus.getDefault().postSticky(rr)
        }
    }

    private val mLoadDeviceListener = object : UDPTaskListner("获取设备……", 3, 500) {

        override fun onProgressUpdate(task: GenericTask?, param: MessageData?) {

            val gcr = GetClientResponse(param?.msgBody)

            val localMac = App.sInstance.wifiInfo.macAddress
            var index = -1
            for (i in 0 until gcr.devices.size)
                if (localMac.equals(gcr.devices.get(i).mac))
                    index = i

            if (index != -1)
                gcr.devices.removeAt(index)

            for (device in gcr.devices)
            //TODO
//                if (localMac.equals(device.mac)) {
//                    device.name = getString(R.string.my_phone)
//                    device.type = App.TYPE_DEVICE_PHONE
//                } else
                device.type = App.TYPE_DEVICE_CLIENT


            val wc = WifiDevice(App.TYPE_DEVICE_CONNECT, "127.00.00.1", "ff:ff:ff:ff:ff:ff", "中继器", App.sInstance.curChannel);
            gcr.devices.add(0, wc)

            EventBus.getDefault().postSticky(gcr)

        }

        override fun onPostExecute(task: GenericTask?, result: TaskResult?) {
            if (result != TaskResult.OK)
                if (task?.exception != null)
                    retry(Runnable { startLoadDevice() })
            super.onPostExecute(task, result)
        }
    }

    private val mLoadDeviceStatusListener = object : UDPTaskListner("获取设备状态……", 3, 500) {

        override fun onPostExecute(task: GenericTask?, result: TaskResult?) {
            if (result != TaskResult.OK)
                retry(Runnable { startLoadDeviceStatus() })
            super.onPostExecute(task, result)
        }

        override fun onProgressUpdate(task: GenericTask?, param: MessageData?) {
            val gcsr = GetClientStatusResponse(param?.msgBody)
            for (device in gcsr.devices) {
                System.err.println("mac = ${device.mac} rssi = ${device.rssi}")
                device.rssi = Math.min(device.rssi - 100, -20).toByte()
                System.err.println("==>mac = ${device.mac} rssi = ${device.rssi}")
            }
            EventBus.getDefault().postSticky(gcsr)
        }

    }


    private val mLoadMasterListener = object : UDPTaskListner("获取Master信息……", 3, 500) {

        override fun onPostExecute(task: GenericTask?, result: TaskResult?) {
            if (result != TaskResult.OK)
                retry(Runnable { startLoadMaster() })
            super.onPostExecute(task, result)
        }

        override fun onProgressUpdate(task: GenericTask?, param: MessageData?) {

            val master = GetMasterResponse(param?.msgBody).master
            master.name = getString(R.string.wifi)
            master.rssi = App.MAX_RSSI
            master.type = App.TYPE_DEVICE_WIFI
            App.sInstance.curChannel = master.channel
            App.sInstance.curWlanIdx = master.wlan_idx
            EventBus.getDefault().postSticky(master)

        }
    }

//    private val mGetChannelListener = object : UDPTaskListner("获取当前信道……", 3, 500) {
//
//        override fun onPostExecute(task: GenericTask?, result: TaskResult?) {
//            if (result != TaskResult.OK)
//                retry(Runnable { startLoadMaster() })
//            super.onPostExecute(task, result)
//        }
//
//        override fun onProgressUpdate(task: GenericTask?, param: MessageData?) {
//
//            val getChannelResponse = GetChannelResponse(param?.msgBody)
//            EventBus.getDefault().postSticky(getChannelResponse)
//
//        }
//    }

    fun closeSelf() {
        if (arrTask.isEmpty()) {
            super.stopSelf()
        }
    }
}

package wifiairscout.changhong.com.wifiairscout.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.util.Log

import org.greenrobot.eventbus.EventBus
import wifiairscout.changhong.com.wifiairscout.App
import wifiairscout.changhong.com.wifiairscout.R
import wifiairscout.changhong.com.wifiairscout.model.MessageData
import wifiairscout.changhong.com.wifiairscout.model.MessageDataFactory
import wifiairscout.changhong.com.wifiairscout.model.response.*
import wifiairscout.changhong.com.wifiairscout.task.GenericTask
import wifiairscout.changhong.com.wifiairscout.task.TaskListener
import wifiairscout.changhong.com.wifiairscout.task.TaskResult
import wifiairscout.changhong.com.wifiairscout.task.UDPTask
import wifiairscout.changhong.com.wifiairscout.utils.CommUtils

/**
 * Created by fuheng on 2017/12/15.
 */

class StartService : Service() {

    companion object {
        const val ACTION_START_ALL = 0
        const val ACTION_LOAD_DEVICE = 1
        const val ACTION_LOAD_DEVICE_STATUS = 2
        const val ACTION_LOAD_MASTER = 3
        const val ACTION_LOAD_CUR_CHANNEL = 4

        fun startServcie(context: Context, action: Int) {
            val intent = Intent(context, Service::class.java)
            intent.putExtra(Intent.EXTRA_INDEX, action)
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
                loadCurrentChannel()
            }

            ACTION_LOAD_MASTER -> startLoadMaster()
            ACTION_LOAD_DEVICE_STATUS -> startLoadDeviceStatus()
            ACTION_LOAD_DEVICE -> startLoadDevice()

            ACTION_LOAD_CUR_CHANNEL -> loadCurrentChannel()

        }
        return super.onStartCommand(intent, flags, startId)
    }


    override fun onDestroy() {
        arrTask.forEach { it.cancle() }
        super.onDestroy()
    }

    private fun loadCurrentChannel() {
        val msg = MessageDataFactory.getChannel()

        UDPTask().execute(msg, mLoadDeviceListener)
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
        val msg = MessageDataFactory.getMasterInfo(App.sInstance.masterMac)

        UDPTask().execute(msg, mLoadMasterListener)
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
            master.rssi = App.MIN_RSSI
            master.type = App.TYPE_DEVICE_WIFI

            EventBus.getDefault().postSticky(master)

        }
    }

    private val mGetChannelListener = object : UDPTaskListner("获取当前信道……", 3, 500) {

        override fun onPostExecute(task: GenericTask?, result: TaskResult?) {
            if (result != TaskResult.OK)
                retry(Runnable { startLoadMaster() })
            super.onPostExecute(task, result)
        }

        override fun onProgressUpdate(task: GenericTask?, param: MessageData?) {

            val getChannelResponse = GetChannelResponse(param?.msgBody)
            EventBus.getDefault().postSticky(getChannelResponse)

        }
    }

    fun closeSelf() {
        if (arrTask.isEmpty()) {
            super.stopSelf()
        }
    }
}

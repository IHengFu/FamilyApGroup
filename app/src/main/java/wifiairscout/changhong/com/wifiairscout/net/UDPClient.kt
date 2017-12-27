package wifiairscout.changhong.com.wifiairscout.net

import android.util.Log
import wifiairscout.changhong.com.wifiairscout.model.MessageData
import wifiairscout.changhong.com.wifiairscout.preferences.Preferences

import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketException
import java.net.UnknownHostException

/**
 * Created by fuheng on 2017/12/11.
 */
class UDPClient(val ip: String, val port: Int, val msg: MessageData, val callBack: CallBack?) : Runnable {

    var dSocket: DatagramSocket? = null
    var address: InetAddress? = null

    var countTimer: Thread? = null

    @Synchronized
    fun send() {
        val sb = StringBuilder()

        try {
            address = InetAddress.getByName(ip)

            dSocket = DatagramSocket(Preferences.getIntance().sendPort) // 注意此处要先在配置文件里设置权限,否则会抛权限不足的异常
            dSocket?.soTimeout = TIME_OUT
            sb.append("正在连接服务器...").append("/n")
        } catch (e: SocketException) {
            e.printStackTrace()
            sb.append("服务器连接失败.").append("/n")
            callBack?.onError("服务器连接失败.")
            return
        } catch (e: UnknownHostException) {
            e.printStackTrace();
            sb.append("未知地址错误.").append("/n")
            callBack?.onError("未知地址错误.")
            return
        }

        startTimeCounter()

        try {
            var data = msg.messageData
            val dPacket = DatagramPacket(data, data.size,
                    address, port)
            dSocket?.send(dPacket)
        } catch (e: IOException) {
            e.printStackTrace()
            sb.append("消息发送失败.").append("/n")
            callBack?.onError(e.message)
            countTimer?.interrupt()
            dSocket?.close()
            return
        }
        sb.append("消息发送成功!").append("/n")

        try {
            val data = ByteArray(1024)
            val rPacket = DatagramPacket(data, data.size, address, port)
            dSocket!!.receive(rPacket)
            callBack?.onSendCallback(rPacket.data)
            sb.append("接收消息成功").append("/n")
        } catch (e: IOException) {
            e.printStackTrace()
            sb.append("消息发送失败.").append("/n")
            callBack?.onError(e.message)
        }

        dSocket?.close()
        countTimer?.interrupt()
        Log.i(javaClass.simpleName, "log = " + sb.toString())
    }


    companion object {
        private val TIME_OUT = 30000
        private val WHAT_ERROR = -1
        private val WHAT_SEND_COMPLETE = 0
        private val WHAT_RECEIVE = 1

    }

    override fun run() {
        send();
    }

    fun startTimeCounter() {
        countTimer = object : Thread() {
            override fun run() {
                try {
                    Thread.sleep(TIME_OUT.toLong())
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                dSocket?.close()
                callBack?.onError("任务超时")
            }
        }
        countTimer?.start()
    }

}

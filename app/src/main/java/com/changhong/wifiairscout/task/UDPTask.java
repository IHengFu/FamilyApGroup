package com.changhong.wifiairscout.task;

import android.os.CountDownTimer;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

import com.changhong.wifiairscout.model.MessageData;
import com.changhong.wifiairscout.model.WifiDevice;
import com.changhong.wifiairscout.model.response.BaseResponse;
import com.changhong.wifiairscout.preferences.Preferences;
import com.changhong.wifiairscout.utils.CommUtils;
import com.changhong.wifiairscout.utils.timecontor.MyCountDownTimer;
import com.changhong.wifiairscout.utils.timecontor.MyCountDownTimerListener;

/**
 * Created by fuheng on 2017/12/14.
 */

public class UDPTask extends GenericTask implements MyCountDownTimerListener {

    private DatagramSocket udpServer;

    private MyCountDownTimer myCountDownTimer;

    public UDPTask execute(MessageData msg, TaskListener listener) {
        setListener(listener);

        String ip = WifiDevice.Companion.toStringIp(Preferences.getIntance().getServerIp());
        int port = Preferences.getIntance().getServerPort();

        TaskParams params = new TaskParams();
        params.put("ip", ip);
        params.put("port", port);
        params.put("msg", msg);
        execute(params);

//        myCountDownTimer = new MyCountDownTimer(30000, 30000, this);
//        myCountDownTimer.start();
        return this;
    }

    @Override
    protected TaskResult _doInBackground(TaskParams... params) {

        String ip = params[0].getString("ip");
        int port = (Integer) params[0].get("port");
        MessageData msg = (MessageData) params[0].get("msg");

        return connect(ip, port, msg);
    }

    private synchronized TaskResult connect(String ip, int port, MessageData msg) {
        try {
            long startTime = System.currentTimeMillis();
            Log.e("请求", msg.toByteString());
            Log.e("请求", msg.toString());
            udpServer = new DatagramSocket(Preferences.getIntance().getSendPort());
            InetAddress address = InetAddress.getByName(ip);
            send(msg.getMessageData(), address, port);

            Thread.sleep(500);

            while (true) {

                if (System.currentTimeMillis() - startTime > 30000) {
                    Log.e("耗时", Thread.currentThread().getName() + "  :  " + (System.currentTimeMillis() - startTime));
                    throw new TimeoutException("通信超时");
                }

                byte[] data = receive(address, port);
                Log.e("响应", CommUtils.toHexString(data));
                if (data != null) {
                    MessageData response = new MessageData(data);
                    Log.e("响应", response.toByteString());
                    Log.e("响应", response.toString());
                    if (response.isResponse() && msg.getMsgId() == response.getMsgId() && msg.getSequeceNo() == response.getSequeceNo())//是 消息响应 并且 消息ID相同
                    {
                        BaseResponse br = new BaseResponse(response.getMsgBody());
                        if (br.getStatus() == 0)
                            publishProgress(response);
                        else
                            throw new Exception(br.getStatusMessage());
                        break;
                    } else {
                        throw new Exception("错误数据");
                    }
                }
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
            setException(e);
            return TaskResult.FAILED;
        } catch (IOException e) {
            e.printStackTrace();
            setException(e);
            return TaskResult.IO_ERROR;
        } catch (InterruptedException e) {
            e.printStackTrace();
            setException(e);
            return TaskResult.FAILED;
        }catch (TimeoutException e){
            e.printStackTrace();
            setException(e);
            return TaskResult.IO_ERROR;
        } catch (Exception e) {
            e.printStackTrace();
            setException(new Exception("错误数据"));
            return TaskResult.FAILED;
        } finally {
            udpServer.close();
        }

        return TaskResult.OK;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    private synchronized void send(byte[] sendBuf, InetAddress addr, int port) throws IOException {
        DatagramPacket sendPacket = new DatagramPacket(sendBuf, sendBuf.length, addr, port);
        udpServer.setSoTimeout(10000);
        udpServer.send(sendPacket);
    }

    private synchronized byte[] receive(InetAddress addr, int port) throws IOException {
        byte[] buff = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(buff, buff.length, addr, port);
        udpServer.setSoTimeout(10000);
        udpServer.receive(receivePacket);
        byte[] result = new byte[receivePacket.getLength()];
        System.arraycopy(buff, 0, result, 0, receivePacket.getLength());
        return result;
    }

    @Override
    public void onTick(long l, @NotNull CountDownTimer timer) {

    }

    @Override
    public void onFinish(@NotNull CountDownTimer timer) {
        setException(new TimeoutException("通信超时"));
        cancle();
    }
}

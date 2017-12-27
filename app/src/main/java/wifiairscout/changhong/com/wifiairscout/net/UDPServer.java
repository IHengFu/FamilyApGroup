package wifiairscout.changhong.com.wifiairscout.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import wifiairscout.changhong.com.wifiairscout.model.MessageData;
import wifiairscout.changhong.com.wifiairscout.preferences.Preferences;

public class UDPServer implements Runnable {

    private byte[] msg = new byte[1024];
    private boolean life = true;

    public UDPServer() {
    }

    public boolean isLife() {
        return life;
    }

    public void setLife(boolean life) {
        this.life = life;
    }

    @Override
    public void run() {
        DatagramSocket dSocket = null;
        DatagramPacket dPacket = new DatagramPacket(msg, msg.length);
        try {
            dSocket = new DatagramSocket(Preferences.getIntance().getReceivePort());
            while (life) {
                try {
                    dSocket.receive(dPacket);
                    System.out.println(new java.util.Date().toString() + "  : msg sever received ," + new String(dPacket.getData()));

                    byte[] data = new MessageData().getMessageData();
                    dSocket.send(new DatagramPacket(data, data.length, dPacket.getAddress(), dPacket.getPort()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}

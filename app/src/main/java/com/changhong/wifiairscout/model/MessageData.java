package com.changhong.wifiairscout.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.changhong.wifiairscout.utils.CommUtils;

/**
 * Created by fuheng on 2017/12/8.
 */

public class MessageData {
    /**
     * 当前协议版本号，目前版本号固定为1，版本号非1的消息直接丢弃
     */
    private byte ver;
    /**
     * from）表示发送消息的设备类型，该字段的各BIT为含义表示如下；
     * C：Client
     * A：APP
     * 说明：C位置1代表Client，A位置1代表APP，C位和A位都置1表示Master
     */
    private byte frm;
    /**
     * to：表示接收消息的设备类型，该字段的各BIT为含义同frm字段；
     */
    private byte to;
    /**
     * op：操作字段，为0表示请求，为1表示响应；
     */
    private byte op;
    /**
     * 发送消息设备的唯一标识号，此处使用设备MAC来表示；
     */
    private byte[] mac;
    /**
     * 分片位，0表示消息未分片，1表示消息是分片
     */
    private byte F;
    /**
     * 当F为置1时，本标志位才有效，为1表示消息是最后一个分片，反之则不是；
     */
    private byte L;
    /**
     * 为1表示当前消息为保活报文，反之则不是
     */
    private byte K;
    /**
     * （消息类型）为0表示获取（GET）消息，为1表示设置（SET）消息，为2表示通知（INFORM）消息；
     */
    private byte T;
    /**
     * 目前保留，以后扩展时使用
     */
    private byte RSV;
    /**
     * sequence no./fragment id：发送消息的序列号（从0开始，逐次递增，
     * 回复消息的序列号设置为接收到请求的序列号），
     * 如果为分片消息（F位置1）则为分片序号，用于分片重组；
     */
    private short sequeceNo;
    /**
     * fragment offset：仅为分片消息（F位置1）时使用，表示分片的偏移（本字段乘以8为真实偏移）；
     */
    private short freOffset;
    /**
     * 设备发送消息时的时间戳
     */
    private int timestamp;
    /**
     * 消息ID
     */
    private short msgId;
    /**
     * 针对不同的消息ID，消息体不同，具体参见接口消息章节
     */
    private byte[] msgBody;

    public byte getVer() {
        return ver;
    }

    public void setVer(byte ver) {
        this.ver = ver;
    }

    public byte getFrm() {
        return frm;
    }

    public void setFrm(byte frm) {
        this.frm = frm;
    }

    public byte getTo() {
        return to;
    }

    public void setTo(byte to) {
        this.to = to;
    }

    public byte getOp() {
        return op;
    }

    public void setOp(byte op) {
        this.op = op;
    }

    public String getMacString() {
        StringBuilder sb = new StringBuilder();
        for (byte b : mac) {
            sb.append(String.format("%x", b & 0xff)).append(':');
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    public byte[] getMac() {
        return mac;
    }

    public void setMac(String mac) {
        if (this.mac == null)
            this.mac = new byte[6];
        if (mac.indexOf(':') > 0) {
            String[] listP = mac.split(":");
            for (int i = 0; i < this.mac.length; i++) {
                this.mac[i] = (byte) (Integer.parseInt(listP[i], 16) & 0xff);
            }
        } else {
            for (int i = 0; i < mac.length(); i += 2) {
                this.mac[i] = (byte) (Integer.parseInt(mac.substring(i, i + 2), 16));
            }
        }
    }

    public byte getF() {
        return F;
    }

    public void setF(byte f) {
        F = f;
    }

    public byte getL() {
        return L;
    }

    public void setL(byte l) {
        L = l;
    }

    public byte getK() {
        return K;
    }

    public void setK(byte k) {
        K = k;
    }

    public byte getT() {
        return T;
    }

    public void setT(byte t) {
        T = t;
    }

    public byte getRSV() {
        return RSV;
    }

    public void setRSV(byte RSV) {
        this.RSV = RSV;
    }

    public short getSequeceNo() {
        return sequeceNo;
    }

    public void setSequeceNo(short sequeceNo) {
        this.sequeceNo = sequeceNo;
    }

    public short getFreOffset() {
        return freOffset;
    }

    public void setFreOffset(short freOffset) {
        this.freOffset = freOffset;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = (int) timestamp;
    }

    public short getMsgId() {
        return msgId;
    }

    public void setMsgId(short msgId) {
        this.msgId = msgId;
    }

    public byte[] getMsgBody() {
        return msgBody;
    }

    public void setMsgBody(byte[] msgBody) {
        this.msgBody = msgBody;
    }

    public void write(OutputStream os) throws IOException {
        {//ver,frm,to,op
            byte d = ver;
            d <<= 2;
            d |= frm & 3;
            d <<= 2;
            d |= to & 3;
            d <<= 2;
            d |= op & 3;
            os.write(d);
        }
        //MAC
        for (byte b : mac) {
            os.write(b);
        }
        {//F,L,K,T,RSV
            byte d = F;
            d <<= 1;
            d |= L & 1;
            d <<= 1;
            d |= K & 1;
            d <<= 2;
            d |= T & 3;
            d <<= 3;
            d |= RSV & 3;
            os.write(d);
        }
        //sequece no. / fragment id
        os.write(sequeceNo >>> 8);
        os.write(sequeceNo & 0xff);
        //fragment offset
        os.write(0);
        os.write(0);
        //timestamp
        os.write((timestamp >>> 24) & 0xff);
        os.write((timestamp >> 16) & 0xff);
        os.write((timestamp >> 8) & 0xff);
        os.write(timestamp & 0xff);
        //message id
        os.write(msgId >>> 8);
        os.write(msgId & 0xff);
        //message length{
        {
            int length = msgBody == null ? 0 : msgBody.length;
            os.write(length >>> 8);
            os.write(length & 0xff);
        }
        //messagebody
        if (msgBody != null)
            for (byte b : msgBody)
                os.write(b);
    }

    public void read(byte[] data) {
        int index = 0;
        {//ver,frm,to,op
            ver = (byte) (data[index] >>> 6 & 3);
            frm = (byte) (data[index] >>> 4 & 3);
            to = (byte) (data[index] >>> 2 & 3);
            op = (byte) (data[index++] & 3);
        }
        //MAC
        {
            mac = new byte[6];
            System.arraycopy(data, index, mac, 0, 6);
            index += 6;
        }
        {//F,L,K,T,RSV
            F = (byte) (data[index] >>> 7 & 1);
            L = (byte) (data[index] >>> 6 & 1);
            K = (byte) (data[index] >>> 5 & 1);
            T = (byte) (data[index] >>> 3 & 3);
            RSV = (byte) (data[index++] & 5);
        }
        //sequece no. / fragment id
        sequeceNo = (short) (data[index++] << 8 | (data[index++] & 0xff));
        //fragment offset
        freOffset = (short) (data[index++] << 8 | (data[index++] & 0xff));
        //timestamp
        timestamp = (data[index++] << 24) | (data[index++] << 16) | (data[index++] << 8) | (data[index++] & 0xff);
        //message id
        msgId = (short) (data[index++] << 8 | (data[index++] & 0xff));
        //message length{
        int length = (short) (data[index++] << 8 | (data[index++] & 0xff));
        //messagebody
        if (length != 0) {
            msgBody = new byte[length];
            System.arraycopy(data, index, msgBody, 0, msgBody.length);
        }
    }

    public byte[] getMessageData() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            write(baos);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }

    public static void getDefaultMessageData(String mac, boolean isResponse, boolean isAlive) {
        MessageData msg = new MessageData();
        msg.setFrm((byte) 1);
        msg.setTo((byte) 3);
        msg.setOp((byte) (isResponse ? 1 : 0));
        msg.setMac(mac);
        msg.setK((byte) (isAlive ? 1 : 0));


    }

    public MessageData() {
        this.ver = 1;
    }

    /**
     * @param isGet   T（消息类型）为0表示获取（GET）消息，为1表示设置（SET[1]）消息，为2表示通知（INFORM）消息；
     * @param isAlive K 是否是保活（1）
     */
    public MessageData(boolean isGet, String mac, boolean isResponse, boolean isAlive, long time) {
        this();
        setFrm((byte) 1);
        setTo((byte) 3);
        setOp((byte) (isResponse ? 1 : 0));
        setMac(mac);
        setK((byte) (isAlive ? 1 : 0));
        setT((byte) (isGet ? 0 : 1));
        setTimestamp(time);
    }

    public MessageData(byte[] data) {
        read(data);
    }

    @Override
    public String toString() {
        return "MessageData{" +
                "ver=" + ver +
                ", frm=" + frm +
                ", to=" + to +
                ", op=" + op +
                ", mac=" + getMacString() +
                ", F=" + F +
                ", L=" + L +
                ", K=" + K +
                ", T=" + T +
                ", RSV=" + RSV +
                ", sequeceNo=" + sequeceNo +
                ", freOffset=" + freOffset +
                ", timestamp=" + timestamp +
                ", msgId=" + msgId +
                ", msgBody=" + CommUtils.toHexString(msgBody) +
                '}';
    }

    public String toByteString() {
        return "MessageData{" + CommUtils.toHexString(
                getMessageData()) + "}";
    }

    public boolean isResponse() {
        return op == 1;
    }
}

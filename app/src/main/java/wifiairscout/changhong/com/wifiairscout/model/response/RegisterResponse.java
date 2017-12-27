package wifiairscout.changhong.com.wifiairscout.model.response;

/**
 * 注册消息响应信息
 * Created by fuheng on 2017/12/14.
 */

public class RegisterResponse extends BaseResponse {

    private short keepalive_interval;
    private short max_msg_body_len;


    private byte channel;

    public RegisterResponse(byte[] data) {
        super(data);
        keepalive_interval = (short) (data[2] << 8 & data[3]);
        max_msg_body_len = (short) (data[4] << 8 & data[5]);
        channel = data[6];
    }

    public short getKeepalive_interval() {
        return keepalive_interval;
    }

    public short getMax_msg_body_len() {
        return max_msg_body_len;
    }

    public byte getChannel() {
        return channel;
    }
}

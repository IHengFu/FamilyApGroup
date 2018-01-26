package com.changhong.wifiairscout.model.response;

/**
 * 手动获取信道
 * Created by fuheng on 2018/1/12
 */

public class GetChannelResponse extends BaseResponse {

    private byte channel;

    @Override
    public void init(byte[] data) {
        super.init(data);
        channel = data[2];
    }

    public byte getChannel() {
        return channel;
    }

    public GetChannelResponse(short status) {
        super(status);
    }

    public GetChannelResponse(byte[] data) {
        super(data);
    }

    public GetChannelResponse() {
        super();
    }

    @Override
    public String toString() {
        return "GetChannelResponse{" +
                "channel=" + channel +
                "} " + super.toString();
    }
}

package com.changhong.wifiairscout.model.response;

/**
 * 手动获取信道
 * Created by fuheng on 2018/1/12
 */

public class GetChannelResponse extends BaseResponse {

    private byte channel;

    public GetChannelResponse(byte[] data) {
        super(data);
        channel = data[2];
    }

    public byte getChannel() {
        return channel;
    }
}

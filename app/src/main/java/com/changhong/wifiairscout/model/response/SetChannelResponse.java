package com.changhong.wifiairscout.model.response;

/**
 * 手动设置信道
 * Created by fuheng on 2018/1/12
 */

public class SetChannelResponse extends BaseResponse {
    public SetChannelResponse(short status) {
        super(status);
    }

    public SetChannelResponse(byte[] data) {
        super(data);
    }

    public SetChannelResponse() {
        super();
    }
}

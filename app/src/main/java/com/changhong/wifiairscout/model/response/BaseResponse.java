package com.changhong.wifiairscout.model.response;

import com.changhong.wifiairscout.App;

import java.io.UnsupportedEncodingException;

/**
 * Created by fuheng on 2017/12/14.
 */

public class BaseResponse {
    private short status;

    public BaseResponse(short status) {
        this.status = status;
    }

    public BaseResponse(byte[] data) {
        init(data);
    }

    public BaseResponse() {

    }

    public void init(byte[] data) {
        status = (short) (data[0] << 8 | data[1]);
    }

    public short getStatus() {
        return status;
    }

    private static final String[] MSG = {"成功", "不是合法的设备", "该设备已被注册"};

    public String getStatusMessage() {
        if (status < 0 || status >= MSG.length)
            return "未知错误";
        return MSG[status];
    }

    @Override
    public String toString() {
        return "BaseResponse{" +
                "status=" + status +
                '}';
    }

    protected String getStringInData(byte[] data, int offset, int length) {
        String result = null;
        try {
            int first0 = 32;
            for (int i = 0; i < 32; ++i)
                if (data[offset + i] == 0) {
                    first0 = i;
                    break;
                }
            result = new String(data, offset, first0, App.CHARSET).trim();
        } catch (UnsupportedEncodingException e) {
            result = new String(data, offset, length);
            e.printStackTrace();
        }

        return result;
    }

    protected String parseToMacAddress(byte[] data, int offset) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; ++i) {
            sb.append(String.format("%02x", data[offset + i]));
            if (i != 5) sb.append(':');
        }

        return sb.toString();
    }
}

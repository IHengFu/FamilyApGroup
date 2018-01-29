package com.changhong.wifiairscout.model.response;

import java.util.ArrayList;
import java.util.List;

/**
 * 获取传输速度响应
 * Created by fuheng on 2018/1/12
 */

public class GetTranslateSpeedResponse extends BaseResponse {

    private List<DeviceRate> mListRate;

    public GetTranslateSpeedResponse(short status) {
        super(status);
    }

    public GetTranslateSpeedResponse(byte[] data) {
        super(data);
    }

    public GetTranslateSpeedResponse() {
        super();
    }

    public void init(byte[] data) {
        super.init(data);
        byte amount = data[2];
        if (amount == 0)
            return;
        mListRate = new ArrayList<>(amount);
        for (int i = 0, index = 3; i < amount; i++, index += 10) {
            mListRate.add(getDeviceRate(data, index));
        }
    }

    private DeviceRate getDeviceRate(byte[] data, int offset) {
        StringBuilder sb = new StringBuilder();
        int index = 0;
        for (int i = 0; i < 6; ++i) {
            sb.append(String.format("%02x", data[offset + index++])).append(':');
        }
        sb.deleteCharAt(sb.length() - 1);

        DeviceRate dr = new DeviceRate();
        dr.mac = sb.toString();

        for (int i = 0; i < 4; i++) {
            long temp = data[offset + index + i];
            dr.rate |= temp << (8 * i);
        }
        return dr;
    }

    public class DeviceRate {
        String mac;
        long rate;

        public String getMac() {
            return mac;
        }

        public void setMac(String mac) {
            this.mac = mac;
        }

        public long getRate() {
            return rate;
        }

        public void setRate(long rate) {
            this.rate = rate;
        }

        public String getRateString() {
            long result = rate / 1024;
            if (result == 0)
                return String.format("%db/s", rate);

            result /= 1024;
            if (result == 0)
                return String.format("%.1fkb/s", rate / 1024f);

            result /= 1024;
            if (result == 0)
                return String.format("%.1fmb/s", rate / 1024f / 1024);

            return String.format("%.1fgb/s", rate / 1024f / 1024 / 1024);
        }

        @Override
        public String toString() {
            return "DeviceRate{" +
                    "mac='" + mac + '\'' +
                    ", rate=" + getRateString() +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "GetTranslateSpeedResponse{" +
                "mListRate=" + mListRate +
                "} " + super.toString();
    }
}

package wifiairscout.changhong.com.wifiairscout.model.response;

import java.util.ArrayList;
import java.util.List;

/**
 * 获取传输速度响应
 * Created by fuheng on 2018/1/12
 */

public class GetTranslateSpeedResponse extends BaseResponse {

    private List<DeviceRate> mListRate;

    public GetTranslateSpeedResponse(byte[] data) {
        super(data);
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

        dr.rate = data[offset + index];
        dr.rate = data[offset + index + 1] << 8 | dr.rate;
        dr.rate = data[offset + index + 2] << 16 | dr.rate;
        dr.rate = data[offset + index + 3] << 24 | dr.rate;
        return dr;
    }

    public class DeviceRate {
        String mac;
        int rate;

        public String getMac() {
            return mac;
        }

        public void setMac(String mac) {
            this.mac = mac;
        }

        public int getRate() {
            return rate;
        }

        public void setRate(int rate) {
            this.rate = rate;
        }
    }
}

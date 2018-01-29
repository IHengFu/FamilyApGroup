package com.changhong.wifiairscout.model.response;

import java.util.ArrayList;
import java.util.List;

/**
 * 扫描结果
 * Created by fuheng on 2018/1/12
 */

public class ScanNewResponse extends BaseResponse {


    private List<ScanRusult> mListAp;

    public ScanNewResponse(short status) {
        super(status);
    }

    public ScanNewResponse(byte[] data) {
        super(data);
    }

    public ScanNewResponse() {
        super();
    }

    public void init(byte[] data) {
        super.init(data);
        byte amount = data[2];

        if (amount == 0)
            return;
        int offset = 3;
        mListAp = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            mListAp.add(new ScanRusult(data[offset++], data[offset++]));
        }

    }

    public List<ScanRusult> getListAp() {
        return mListAp;
    }

    @Override
    public String toString() {
        return "ScanResponse{" +
                "mListAp=" + mListAp +
                "} " + super.toString();
    }

    public static class ScanRusult {
        byte rssi;
        byte channel;

        public ScanRusult(byte channel, byte rssi) {
            this.rssi = rssi;
            this.channel = channel;
        }

        public byte getRssi() {
            return rssi;
        }

        public void setRssi(byte rssi) {
            this.rssi = rssi;
        }

        public byte getChannel() {
            return channel;
        }

        public void setChannel(byte channel) {
            this.channel = channel;
        }

        @Override
        public String toString() {
            return "ScanRusult{" +
                    "rssi=" + rssi +
                    ", channel=" + channel +
                    '}';
        }
    }
}

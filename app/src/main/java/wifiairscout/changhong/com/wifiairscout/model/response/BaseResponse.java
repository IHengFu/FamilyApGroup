package wifiairscout.changhong.com.wifiairscout.model.response;

/**
 * Created by fuheng on 2017/12/14.
 */

public class BaseResponse {
    private short status;

    public BaseResponse(byte[] data) {
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
}

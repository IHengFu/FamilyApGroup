package wifiairscout.changhong.com.wifiairscout.model

import android.os.Parcel
import android.os.Parcelable
import android.text.Html

/**
 * Created by fuheng on 2017/12/8.
 */
class WifiDevice(var type: Byte, var ip: String?, val mac: String?, var name: String?, var channel: Byte?) : Parcelable {
    var rssi: Byte = 0

    constructor(r: Byte, s: String) : this(0.toByte(), null, s, null, null) {
        rssi = r
    }

    companion object {
        fun toStringIp(ip: Int): String {
            var result = Integer.toString(ip.and(0xff))
            result += '.'
            result += Integer.toString(ip.ushr(8).and(0xff))
            result += '.'
            result += Integer.toString(ip.ushr(16).and(0xff))
            result += '.'
            result += Integer.toString(ip.ushr(24).and(0xff))
            return result
        }

        fun to4ByteIp(ip: Int): ByteArray {
            var result = ByteArray(4)
            result[0] = ip.and(0xff).toByte()
            result[1] = ip.ushr(8).and(0xff).toByte()
            result[2] = ip.ushr(16).and(0xff).toByte()
            result[3] = ip.ushr(24).and(0xff).toByte()
            return result
        }

        fun toIpValue(ip: ByteArray): Int {
            var value: Int = ip[3].toInt();
            value = value.shl(8).and(ip[2].toInt())
            value = value.shl(8).and(ip[1].toInt())
            value = value.shl(8).and(ip[0].toInt())
            return value
        }

        fun to6ByteMac(mac: String): ByteArray {
            val result = ByteArray(6)
            if (mac.indexOf(':') > 0) {
                val listP = mac.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                for (i in listP.indices) {
                    result[i] = (Integer.parseInt(listP[i], 16) and 0xff).toByte()
                }
            } else {
                var i = 0
                while (i < mac.length) {
                    result[i] = Integer.parseInt(mac.substring(i, i + 2), 16).toByte()
                    i += 2
                }
            }
            return result
        }

        fun toIpValue(ip: String): Int {
            var arr = ip.split(".");

            var result = 0;

            for (s in arr.indices) {
                result = result.shl(8).or(Integer.parseInt(arr[arr.size - 1 - s]).and(0xff))
            }
            return result
        }

        @JvmField
        val CREATOR: Parcelable.Creator<WifiDevice> = object : Parcelable.Creator<WifiDevice> {
            override fun createFromParcel(source: Parcel): WifiDevice = WifiDevice(source)
            override fun newArray(size: Int): Array<WifiDevice?> = arrayOfNulls(size)
        }
    }

    fun eat(d: WifiDevice) {
        if (d.channel != null)
            this.channel = d.channel

        if (d.ip != null)
            this.ip = d.ip

        if (d.name != null)
            this.name = d.name

        this.rssi = d.rssi
    }

    constructor(parcel: Parcel) : this(
            parcel.readByte(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readByte()) {
        this.rssi = parcel.readByte()
    }


    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(type)
        parcel.writeString(ip)
        parcel.writeString(mac)
        parcel.writeString(name)
        parcel.writeByte(channel ?: 0.toByte())
        parcel.writeByte(rssi)
    }

    override fun equals(other: Any?): Boolean {
        if (other != null) {
            if (other is WifiDevice) {
                if (other.mac.equals(mac))//仅需判断mac地址
                    return true
            }
        }
        return super.equals(other)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "WifiDevice(type=$type, ip=$ip, mac=$mac, name=$name, channel=$channel, rssi=$rssi)"
    }

}
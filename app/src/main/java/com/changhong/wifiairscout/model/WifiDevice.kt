package com.changhong.wifiairscout.model

import android.os.Parcel
import android.os.Parcelable
import com.changhong.wifiairscout.App
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by fuheng on 2017/12/8.
 */
class WifiDevice(var type: Byte, var ip: String?, val mac: String, var name: String?) : Parcelable {
    var rssi: Byte = Byte.MIN_VALUE

    var dual_band: Byte = 0//	1	1:双频; 0:单频
    var wlan_idx: Byte = 1//	1	无线radio索引

    var Bssid: String? = null

    var arrayDualBandInfo: List<DualBandInfo>? = null

    constructor(parcel: Parcel) : this(
            parcel.readByte(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString()) {
        rssi = parcel.readByte()
        dual_band = parcel.readByte()
        wlan_idx = parcel.readByte()
        Bssid = parcel.readString()


        arrayDualBandInfo = parcel.createTypedArrayList(DualBandInfo)
    }


    constructor(r: Byte, s: String) : this(0.toByte(), null, s, null) {
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

        if (d.ip != null)
            this.ip = d.ip

        if (d.name != null)
            this.name = d.name

        if (d.rssi != Byte.MIN_VALUE)
            this.rssi = d.rssi
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


    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(type)
        parcel.writeString(ip)
        parcel.writeString(mac)
        parcel.writeString(name)
        parcel.writeByte(rssi)
        parcel.writeByte(dual_band)
        parcel.writeByte(wlan_idx)

        parcel.writeString(Bssid)

        parcel.writeTypedList(arrayDualBandInfo)
    }

    override fun toString(): String {
        return "WifiDevice(type=$type, ip=$ip, mac='$mac', name=$name, rssi=$rssi, dual_band=$dual_band, wlan_idx=$wlan_idx, Bssid=$Bssid, arrayDualBandInfo=$arrayDualBandInfo)"
    }
}
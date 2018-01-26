package com.changhong.wifiairscout.model

import android.os.Parcel
import android.os.Parcelable
import android.text.Html
import java.util.*

/**
 * Created by fuheng on 2017/12/8.
 */
class WifiDevice(var type: Byte, var ip: String?, val mac: String, var name: String?, var channel: Byte?) : Parcelable {
    var rssi: Byte = 0
    var encryptType: Byte = -1
    var cipher: Byte = -1//1:tkip; 2:aes; 3:mixed

    var dual_band: Byte = 0//	1	1:双频; 0:单频
    var wlan_idx: Byte = 0//	1	无线radio索引
    var Bound: Byte = 0//	1	0: 20MHz; 1: 40MHz; 2: 80MHz
    var sideband: Byte = 0//	1	0: 高; 1: 低
    var Ssid: String? = null//	32	无线名称
    var encrypt: Byte = 0//	1	0:disabled; 1:wep; 2:wpa; 4:wpa2; 6:wp2_mixed; 7:wapi
    var Key: ByteArray? = null//	64	密钥

    var Bssid: String? = null

    constructor(parcel: Parcel) : this(
            parcel.readByte(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readValue(Byte::class.java.classLoader) as? Byte) {
        rssi = parcel.readByte()
        encryptType = parcel.readByte()
        cipher = parcel.readByte()
        dual_band = parcel.readByte()
        wlan_idx = parcel.readByte()
        Bound = parcel.readByte()
        sideband = parcel.readByte()
        Ssid = parcel.readString()
        encrypt = parcel.readByte()
        Key = parcel.createByteArray()
        Bssid = parcel.readString()
    }


    constructor(r: Byte, s: String, is5g: Boolean) : this(0.toByte(), null, s, null, null) {
        rssi = r
    }

    companion object {
        val ENCRYPT_TYPE = arrayOf("disabled", "wep", "wpa", "wpa2", "wp2_mixed", "wapi")
        val CIPHER_TYPE = arrayOf("tkip", "aes", "mixed")

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

    fun getEncryptType() = ENCRYPT_TYPE[encryptType.toInt()]
    fun getCipherName() = CIPHER_TYPE[cipher.toInt()]


    fun eat(d: WifiDevice) {
        if (d.channel != null)
            this.channel = d.channel

        if (d.ip != null)
            this.ip = d.ip

        if (d.name != null)
            this.name = d.name

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


    override fun toString(): String {
        return "WifiDevice(type=$type, ip=$ip, mac='$mac', name=$name, channel=$channel, rssi=$rssi, encryptType=$encryptType, cipher=$cipher, dual_band=$dual_band, wlan_idx=$wlan_idx, Bound=$Bound, sideband=$sideband, Ssid=$Ssid, encrypt=$encrypt, Key=${Arrays.toString(Key)})"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(type)
        parcel.writeString(ip)
        parcel.writeString(mac)
        parcel.writeString(name)
        parcel.writeValue(channel)
        parcel.writeByte(rssi)
        parcel.writeByte(encryptType)
        parcel.writeByte(cipher)
        parcel.writeByte(dual_band)
        parcel.writeByte(wlan_idx)
        parcel.writeByte(Bound)
        parcel.writeByte(sideband)
        parcel.writeString(Ssid)
        parcel.writeByte(encrypt)
        parcel.writeByteArray(Key)
        parcel.writeString(Bssid)
    }


}
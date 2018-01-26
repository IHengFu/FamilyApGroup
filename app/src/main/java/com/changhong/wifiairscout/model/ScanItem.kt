package com.changhong.wifiairscout.model

import android.os.Parcel
import android.os.Parcelable
import java.util.*

/**
 * Created by fuheng on 2018/1/26.
 */
class ScanItem() : Parcelable {
    var wlan_idx: Byte = 0//	1	无线radio索引
    var Bound: Byte = 0//	1	0: 20MHz; 1: 40MHz; 2: 80MHz
    var sideband: Byte = 0//	1	0: 高; 1: 低
    var Ssid: String? = null//	32	无线名称
    var encrypt: Byte = 0//	1	0:disabled; 1:wep; 2:wpa; 4:wpa2; 6:wp2_mixed; 7:wapi
    var cipher: Byte = -1//1:tkip; 2:aes; 3:mixed
    var Key: ByteArray? = null//	64	密钥
    var channel: Byte = 0
    var rssi: Byte = 0

    var Bssid: String? = null

    constructor(parcel: Parcel) : this() {
        wlan_idx = parcel.readByte()
        Bound = parcel.readByte()
        sideband = parcel.readByte()
        Ssid = parcel.readString()
        encrypt = parcel.readByte()
        cipher = parcel.readByte()
        Key = parcel.createByteArray()
        channel = parcel.readByte()
        rssi = parcel.readByte()
        Bssid = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(wlan_idx)
        parcel.writeByte(Bound)
        parcel.writeByte(sideband)
        parcel.writeString(Ssid)
        parcel.writeByte(encrypt)
        parcel.writeByte(cipher)
        parcel.writeByteArray(Key)
        parcel.writeByte(channel)
        parcel.writeByte(rssi)
        parcel.writeString(Bssid)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "ScanItem(wlan_idx=$wlan_idx, Bound=$Bound, sideband=$sideband, Ssid=$Ssid, encrypt=$encrypt, cipher=$cipher, Key=${Arrays.toString(Key)}, channel=$channel, rssi=$rssi, Bssid=$Bssid)"
    }

    companion object CREATOR : Parcelable.Creator<ScanItem> {
        override fun createFromParcel(parcel: Parcel): ScanItem {
            return ScanItem(parcel)
        }

        override fun newArray(size: Int): Array<ScanItem?> {
            return arrayOfNulls(size)
        }
    }


}
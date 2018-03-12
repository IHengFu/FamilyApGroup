package com.changhong.wifiairscout.model

import android.os.Parcel
import android.os.Parcelable
import java.util.*

/**
 * Created by fuheng on 2018/3/9.
 */
class DualBandInfo(var wlan_idx: Byte, var channel: Byte, var Bound: Byte, var sideband: Byte, var Ssid: String?, var encrypt: Byte
                   , var Cipher: Byte, var Key: ByteArray?) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readByte(),
            parcel.readByte(),
            parcel.readByte(),
            parcel.readByte(),
            parcel.readString(),
            parcel.readByte(),
            parcel.readByte(),
            parcel.createByteArray()) {
    }

    constructor() : this(
            1,
            Byte.MIN_VALUE,
            0,
            1,
            null,
            0,
            0,
            null) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(wlan_idx)
        parcel.writeByte(channel)
        parcel.writeByte(Bound)
        parcel.writeByte(sideband)
        parcel.writeString(Ssid)
        parcel.writeByte(encrypt)
        parcel.writeByte(Cipher)
        parcel.writeByteArray(Key)
    }

    fun getEncryptType() = ENCRYPT_TYPE[encrypt.toInt()]
    fun getCipherName() = CIPHER_TYPE[Cipher.toInt()]

    override
    fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "DualBandInfo(wlan_idx=$wlan_idx, channel=$channel, Bound=$Bound, sideband=$sideband, Ssid='$Ssid', encrypt=$encrypt, Cipher=$Cipher, Key=${Arrays.toString(Key)})"
    }

    companion object CREATOR : Parcelable.Creator<DualBandInfo> {

        val ENCRYPT_TYPE = arrayOf("disabled", "wep", "wpa", "wpa2", "wp2_mixed", "wapi")
        val CIPHER_TYPE = arrayOf("tkip", "aes", "mixed")

        override fun createFromParcel(parcel: Parcel): DualBandInfo {
            return DualBandInfo(parcel)
        }

        override fun newArray(size: Int): Array<DualBandInfo?> {
            return arrayOfNulls(size)
        }
    }


}
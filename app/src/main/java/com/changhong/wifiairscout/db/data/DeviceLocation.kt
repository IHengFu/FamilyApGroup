package com.changhong.wifiairscout.db.data

import android.os.Parcel
import android.os.Parcelable
import com.changhong.wifiairscout.App
import com.changhong.wifiairscout.model.WifiDevice
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

/**
 * Created by fuheng on 2017/12/15.
 */
@DatabaseTable(tableName = "device_location")
data class DeviceLocation(
        @DatabaseField(generatedId = true) val id: Int,
        @DatabaseField(columnName = "x") var x: Float,
        @DatabaseField(columnName = "y") var y: Float,
        @DatabaseField(columnName = "mac") var mac: String,
        @DatabaseField(columnName = "type") var type: Byte,
        @DatabaseField(columnName = "group") var group: Long = 0) : Comparable<DeviceLocation>, Parcelable {

    @DatabaseField(columnName = "name")
    var nickName: String? = null

    var channel: Byte = 0
    var intensity: Byte = 0
    var wifiDevice: WifiDevice? = null

    var angle: Float = 0f

    @DatabaseField(columnName = "rssi")
    var rssi: Byte = Byte.MIN_VALUE

    constructor() : this(0, 0f, 0f, "", 0, 0)

    override fun compareTo(other: DeviceLocation): Int {
        return (angle - other.angle).toInt()
    }

    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readFloat(),
            parcel.readFloat(),
            parcel.readString(),
            parcel.readByte(),
            parcel.readLong()) {
        nickName = parcel.readString()
        channel = parcel.readByte()
        intensity = parcel.readByte()
        wifiDevice = parcel.readParcelable(WifiDevice::class.java.classLoader)
        angle = parcel.readFloat()
    }

    fun getDisplayName(): String? {
        if (wifiDevice != null) {
            return nickName ?: wifiDevice?.name
        }
        return nickName ?: mac
    }

    fun setXY(x: Float, y: Float) {
        this.x = x
        this.y = y
    }

    override fun toString(): String {
        return "DeviceLocation(id=$id, x=$x, y=$y, mac='$mac', type=$type, group=$group, channel=$channel, intensity=$intensity, wifiDevice=$wifiDevice, nickName=$nickName, angle=$angle)"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeFloat(x)
        parcel.writeFloat(y)
        parcel.writeString(mac)
        parcel.writeByte(type)
        parcel.writeLong(group)
        parcel.writeString(nickName)
        parcel.writeByte(channel)
        parcel.writeByte(intensity)
        parcel.writeParcelable(wifiDevice, flags)
        parcel.writeFloat(angle)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DeviceLocation> {
        override fun createFromParcel(parcel: Parcel): DeviceLocation {
            return DeviceLocation(parcel)
        }

        override fun newArray(size: Int): Array<DeviceLocation?> {
            return arrayOfNulls(size)
        }
    }


}
package wifiairscout.changhong.com.wifiairscout.model

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
        @DatabaseField(columnName = "group") var group: Int = 0) : Comparable<DeviceLocation> {

    constructor() : this(0, 0f, 0f, "", 0, 0)

    override fun compareTo(other: DeviceLocation): Int {
        return (angle - other.angle).toInt()
    }

    var channel: Byte = 0
    var intensity: Byte = 0
    var wifiDevice: WifiDevice? = null

    var angle: Float = 0f


    fun setXY(x: Float, y: Float) {
        this.x = x
        this.y = y
    }

    override fun toString(): String {
        return "DeviceLocation(id=$id, x=$x, y=$y, mac='$mac', type=$type, channel=$channel, intensity=$intensity, WifiDevice=$wifiDevice, angle=$angle)"
    }

}
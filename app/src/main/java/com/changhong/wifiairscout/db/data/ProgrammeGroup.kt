package com.changhong.wifiairscout.db.data

import android.os.Parcel
import android.os.Parcelable
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import com.changhong.wifiairscout.App

/**
 * Created by fuheng on 2018/1/22.
 */

@DatabaseTable(tableName = "programme_table")
data class ProgrammeGroup(
        @DatabaseField(generatedId = true) var id: Int,
        @DatabaseField(columnName = "name") var name: String?,
        @DatabaseField(columnName = "group") var group: Long,
        @DatabaseField(columnName = "averageRSSI") var rssi: Byte,
        @DatabaseField(columnName = "userName") var userName: String?) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString(),
            parcel.readLong(),
            parcel.readByte(),
            parcel.readString()) {
    }

    constructor() : this(0, null, System.currentTimeMillis(), App.MIN_RSSI, null)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeLong(group)
        parcel.writeByte(rssi)
        parcel.writeString(userName)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ProgrammeGroup> {
        override fun createFromParcel(parcel: Parcel): ProgrammeGroup {
            return ProgrammeGroup(parcel)
        }

        override fun newArray(size: Int): Array<ProgrammeGroup?> {
            return arrayOfNulls(size)
        }
    }
}

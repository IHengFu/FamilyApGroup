package com.changhong.wifiairscout.db.data

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
        @DatabaseField(columnName = "userName") var userName: String?) {
    constructor() : this(0, null, System.currentTimeMillis(), App.MIN_RSSI, null)
}

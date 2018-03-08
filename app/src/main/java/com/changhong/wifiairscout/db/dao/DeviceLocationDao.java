package com.changhong.wifiairscout.db.dao;

import android.content.Context;

import com.changhong.wifiairscout.db.DBHelper;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.List;

import com.changhong.wifiairscout.db.data.DeviceLocation;

/**
 * Created by fuheng on 2018/1/23.
 */

public class DeviceLocationDao extends BaseDao<DeviceLocation, Long> {

    public DeviceLocationDao(Context context) {
        super(context);
    }

    public List<DeviceLocation> queryByProgrammeId(long groupId) {
        return queryByParam("group", groupId);
    }

}

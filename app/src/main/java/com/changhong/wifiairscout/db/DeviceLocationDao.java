package com.changhong.wifiairscout.db;

import android.content.Context;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.List;

import com.changhong.wifiairscout.model.DeviceLocation;

/**
 * Created by fuheng on 2018/1/23.
 */

public class DeviceLocationDao {
    private Dao<DeviceLocation, Long> mDao;

    public DeviceLocationDao(Context context) {
        try {
            mDao = DBHelper.getHelper(context).getDao(DeviceLocation.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void add(DeviceLocation t) {
        try {
            mDao.create(t);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(DeviceLocation t) {
        try {
            mDao.delete(t);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<DeviceLocation> loadAll() {
        try {
            return mDao.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<DeviceLocation> queryByName(long groupId) {

        try {
            List<DeviceLocation> a = mDao.queryBuilder().where().eq("group", groupId).query();
            return a;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void deleteById(Long group) {
        try {
            List<DeviceLocation> a = mDao.deleteBuilder().where().eq("group", group).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}

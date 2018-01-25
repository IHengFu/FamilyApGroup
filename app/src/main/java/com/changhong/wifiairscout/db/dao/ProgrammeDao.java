package com.changhong.wifiairscout.db.dao;

import android.content.Context;

import com.changhong.wifiairscout.db.DBHelper;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.List;

import com.changhong.wifiairscout.db.data.ProgrammeGroup;

/**
 * Created by fuheng on 2018/1/23.
 */

public class ProgrammeDao extends BaseDao<ProgrammeGroup, String> {

    public ProgrammeDao(Context context) {
        super(context);
    }


    public List<ProgrammeGroup> queryByName(String name) {
        return queryByParam("name", name);
    }

    public List<ProgrammeGroup> getAll() {

        return loadAll();
    }

}

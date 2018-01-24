package com.changhong.wifiairscout.db;

import android.content.Context;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.Where;

import java.sql.SQLException;
import java.util.List;

import com.changhong.wifiairscout.model.ProgrammeGroup;

/**
 * Created by fuheng on 2018/1/23.
 */

public class ProgrammeDao {
    private Dao<ProgrammeGroup, Integer> mDao;

    public ProgrammeDao(Context context) {
        try {
            mDao = DBHelper.getHelper(context).getDao(ProgrammeGroup.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void add(ProgrammeGroup t) {
        try {
            mDao.create(t);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(ProgrammeGroup t) {
        try {
            mDao.delete(t);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<ProgrammeGroup> queryByName(String name) {
        try {
            List<ProgrammeGroup> a = mDao.queryBuilder().where().eq("name", name).query();
            return a;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<ProgrammeGroup> getAll() {
        try {
            return mDao.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}

package com.changhong.wifiairscout.db.dao;

import android.content.Context;

import com.changhong.wifiairscout.db.DBHelper;
import com.j256.ormlite.dao.Dao;

import java.lang.reflect.ParameterizedType;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by fuheng on 2018/1/23.
 */

public class BaseDao<T1, T2> {
    private Dao<T1, Integer> mDao;

    public BaseDao(Context context) {
        try {
            Class<T1> entityClass = (Class<T1>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
            mDao = DBHelper.getHelper(context).getDao(entityClass);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void add(T1 t) {
        try {
            mDao.create(t);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(T1 t) {
        try {
            mDao.delete(t);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void clear() {
        try {
            mDao.deleteBuilder().delete();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public List<T1> loadAll() {
        try {
            return mDao.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<T1> queryByParam(String columnName, T2 obj) {

        try {
            List<T1> a = mDao.queryBuilder().where().eq(columnName, obj).query();
            return a;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<T1> deleteByParam(String columnName, T2 obj) {
        try {
            List<T1> a = mDao.deleteBuilder().where().eq(columnName, obj).query();
            return a;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateByParam(T1 t1) {
        try {
            mDao.update(t1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public long getRowNums() {
        try {
            return mDao.countOf();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

}

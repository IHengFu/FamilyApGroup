package com.changhong.wifiairscout.db.dao;

import android.content.Context;
import android.text.TextUtils;

import com.changhong.wifiairscout.db.data.ProgrammeGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by fuheng on 2018/1/23.
 */

public class ProgrammeDao extends BaseDao<ProgrammeGroup, String> {

    public ProgrammeDao(Context context) {
        super(context);
    }

    public List<ProgrammeGroup> queryByUserName(String userName) {
        List<ProgrammeGroup> a = queryByParam("userName", userName);
        return a;
    }

    public List<ProgrammeGroup> query(String userName, String name) {
        if (TextUtils.isEmpty(userName))
            return null;
        HashMap<String, String> hash = new HashMap<>();
        hash.put("name", name);
        hash.put("userName", userName);
        return queryByParam(hash);
    }

    public List<String> getUserNames() {

        List<ProgrammeGroup> all = loadAll();
        if (all == null || all.isEmpty())
            return null;

        List<String> result = new ArrayList<>();
        for (ProgrammeGroup o : all) {
            if (!result.contains(o.getUserName()))
                result.add(o.getUserName());
        }

        return result;
    }

}

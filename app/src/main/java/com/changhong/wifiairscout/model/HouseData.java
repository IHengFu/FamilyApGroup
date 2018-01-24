package com.changhong.wifiairscout.model;


import android.graphics.Rect;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fuheng on 2017/12/23.
 */

public class HouseData {
    private String background;
    private short width;
    private short height;
    private List<Rect> areas;
    private String name;

    public HouseData() {
    }

    public HouseData(String jsonStr) {
        if (jsonStr == null)
            return;
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            name = jsonObject.getString("name");
            background = jsonObject.getString("background");
            width = (short) jsonObject.getInt("width");
            height = (short) jsonObject.getInt("height");
            JSONArray jarr = jsonObject.getJSONArray("areas");
            if (jarr != null && jarr.length() > 0) {
                areas = new ArrayList<>(jarr.length());
                for (int i = 0; i < jarr.length(); i++) {
                    JSONObject jobj = jarr.getJSONObject(i);
                    areas.add(new Rect(jobj.getInt("left"), jobj.getInt("top"),
                            jobj.getInt("right")
                            , jobj.getInt("bottom")));

                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public short getWidth() {
        return width;
    }

    public void setWidth(short width) {
        this.width = width;
    }

    public short getHeight() {
        return height;
    }

    public void setHeight(short height) {
        this.height = height;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Rect> getAreas() {
        return areas;
    }

    public void setAreas(List<Rect> areas) {
        this.areas = areas;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof HouseData))
            return false;

        HouseData hd = (HouseData) obj;
        if (name != hd.name || width != hd.width || height != hd.height || background != hd.background)
            return false;

        return true;
    }
}

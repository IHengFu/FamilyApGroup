package wifiairscout.changhong.com.wifiairscout.db;

import android.content.Context;

import java.sql.SQLException;
import java.util.ArrayList;

import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import wifiairscout.changhong.com.wifiairscout.model.DeviceLocation;


/**
 * Created by fuheng on 2017/12/15.
 */

public class DBHelper extends OrmLiteSqliteOpenHelper {

    private static DBHelper instance;

    private static final String DB_NAME = "Ericsson_telematics.db";
    private static final int DB_VERSION = 1;
    private ArrayList<Class> arrayList;

    private DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
        registerTables();

        createTables(connectionSource);
    }

    private void registerTables() {
        registerTable(DeviceLocation.class);
    }

    public void createTables(ConnectionSource connectionSource) {
        for (Class o : arrayList)
            try {
                TableUtils.createTable(connectionSource, o);
            } catch (SQLException e) {
                e.printStackTrace();
            }
    }


    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        for (Class o : arrayList) {
            try {
                TableUtils.dropTable(connectionSource, o, true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        onCreate(database, connectionSource);
    }

    public static synchronized DBHelper getHelper(Context context) {
        context = context.getApplicationContext();
        if (instance == null) {
            synchronized (DBHelper.class) {
                if (instance == null)
                    instance = new DBHelper(context);
            }
        }

        return instance;
    }


    /**
     * 注册数据表
     *
     * @param clazz 表的列结构bean
     * @param <T>
     */
    public <T> void registerTable(Class<T> clazz) {
        if (arrayList == null) {
            arrayList = new ArrayList<>();
        }

        if (!arrayList.contains(clazz))
            arrayList.add(clazz);
    }

    public <T> void getDefaultDao(Class<T> clazz){

    }

    @Override
    public void close() {
        super.close();
    }
/**
     * 降级处理
     */
//    public void downgrade(ConnectionSource cs, int oldVersion, int newVersion) {
//        try {
//            for (DatabaseHandler handler : tableHandlers) {
//                handler.onDowngrade(cs, oldVersion, newVersion);
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } catch (java.sql.SQLException e) {
//            e.printStackTrace();
//        }
//    }

}

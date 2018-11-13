package com.qingqing.base.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;

/**
 * 日志存储数据库（5.3从default独立出来）
 *
 * Created by tanwei on 2016/10/25.
 */

public class LogDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "log.db";
    private static final int DATABASE_VERSION = 1;

    public static final String DB_TABLE_USER_BEHAVIOR_LOG = "t_user_behavior_log";
    public static final String DB_TABLE_KEY_USER_BEHAVIOR_LOG_LOG = "user_behavior_log";
    public static final String DB_TABLE_KEY_USER_BEHAVIOR_LOG_CREATE_TIME = "create_time";
    public static final String DB_TABLE_KEY_USER_BEHAVIOR_LOG_TYPE = "type";
    
    public LogDBHelper(Context context) {
        super(context, (new File(context.getFilesDir(), DATABASE_NAME)).getAbsolutePath(),
                null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        createTableUserBehaviorLog(db);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        
    }

    private void createTableUserBehaviorLog(SQLiteDatabase db) {
        db.execSQL(String.format(
                "CREATE TABLE IF NOT EXISTS %s "
                        + "(_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + DB_TABLE_KEY_USER_BEHAVIOR_LOG_LOG + " TEXT,"
                        + DB_TABLE_KEY_USER_BEHAVIOR_LOG_CREATE_TIME + " INTEGER,"
                        + DB_TABLE_KEY_USER_BEHAVIOR_LOG_TYPE + " INTEGER)",
                DB_TABLE_USER_BEHAVIOR_LOG));
    }
}

package com.qingqing.base.im.db;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.qingqing.base.im.ChatManager;
import com.qingqing.base.log.Logger;

/**
 * 修改记录：
 *
 ** ---------------版本变更记录--------------------
 *
 * 1 ==> 创建db
 *
 * 2 ==> 增加字段 lever, profilerate , isnew
 *
 * 3 ==> 增加字段 ishasnew
 *
 * 4 ==> 增加字段 taNick taUsrId
 *
 * 5 ==> 增加字段 group_role
 *
 * 6 ==> 增加字段 teacher_role
 */
public class DbOpenHelper extends SQLiteOpenHelper {
    
    private static final int DATABASE_VERSION = 6;
    private static DbOpenHelper instance;
    
    private static final String USERNAME_TABLE_CREATE = "CREATE TABLE "
            + ContactDao.TABLE_NAME + " ("
            + ContactDao.COLUMN_NAME_USERNAME + " TEXT PRIMARY KEY, "
            + ContactDao.COLUMN_NAME_NICK + " TEXT, "
            + ContactDao.COLUMN_NAME_ALIAS + " TEXT, "
            + ContactDao.COLUMN_NAME_AVATAR + " TEXT, "
            + ContactDao.COLUMN_NAME_PHONE + " TEXT, "
            + ContactDao.COLUMN_NAME_SEX + " INTEGER, "
            + ContactDao.COLUMN_NAME_USERID + " LONG, "
            + ContactDao.COLUMN_NAME_FRIENDS + " INTEGER, "
            + ContactDao.COLUMN_NAME_EXTRA + " TEXT, "
            + ContactDao.COLUMN_NAME_LEVER + " TEXT, "
            + ContactDao.COLUMN_NAME_PROFILE_RATE + " TEXT, "
            + ContactDao.COLUMN_NAME_IS_NEW + " TEXT, "
            + ContactDao.COLUMN_NAME_IS_HAS_NEW + " TEXT, "
            + ContactDao.COLUMN_NAME_TA_NICK + " TEXT, "
            + ContactDao.COLUMN_NAME_TA_USER_ID + " TEXT, "
            + ContactDao.COLUMN_NAME_GROUP_ROLE + " TEXT, "
            + ContactDao.COLUMN_NAME_TEACHER_ROLE + " TEXT, "
            + ContactDao.COLUMN_NAME_TYPE + " TEXT);";
    
    private DbOpenHelper(Context context) {
        super(context, getUserDatabaseName(), null, DATABASE_VERSION);
    }
    
    public static DbOpenHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DbOpenHelper(context.getApplicationContext());
        }
        return instance;
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(USERNAME_TABLE_CREATE);
    }
    
    private static String getUserDatabaseName() {
        return ChatManager.getInstance().getCurrentUserName() + "_im.db";
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            try {
                db.execSQL("ALTER TABLE " + ContactDao.TABLE_NAME + " ADD lever TEXT");
                db.execSQL(
                        "ALTER TABLE " + ContactDao.TABLE_NAME + " ADD profilerate TEXT");
                db.execSQL("ALTER TABLE " + ContactDao.TABLE_NAME + " ADD isnew TEXT");
                db.execSQL("ALTER TABLE " + ContactDao.TABLE_NAME + " ADD ishasnew TEXT");
            } catch (SQLException e) {
                Logger.w("db",
                        "update from " + oldVersion + " to " + newVersion + " failed", e);
            }
        }
        if (oldVersion < 3) {
            try {
                db.execSQL("ALTER TABLE " + ContactDao.TABLE_NAME + " ADD ishasnew TEXT");
            } catch (SQLException e) {
                Logger.w("db",
                        "update from " + oldVersion + " to " + newVersion + " failed", e);
            }
        }
        // 5.6.5版本添加taNick和taUserId字段
        if (oldVersion < 4) {
            try {
                db.execSQL(String.format(
                        "ALTER TABLE " + ContactDao.TABLE_NAME + " ADD %s TEXT",
                        ContactDao.COLUMN_NAME_TA_NICK));
                db.execSQL(String.format(
                        "ALTER TABLE " + ContactDao.TABLE_NAME + " ADD %s TEXT",
                        ContactDao.COLUMN_NAME_TA_USER_ID));
            } catch (SQLException e) {
                Logger.w("db",
                        "update from " + oldVersion + " to " + newVersion + " failed", e);
            }
        }
        // 5.8.0版本添加groupRole字段
        if (oldVersion < 5) {
            try {
                db.execSQL(String.format(
                        "ALTER TABLE " + ContactDao.TABLE_NAME + " ADD %s TEXT",
                        ContactDao.COLUMN_NAME_GROUP_ROLE));
            } catch (SQLException e) {
                Logger.w("db",
                        "update from " + oldVersion + " to " + newVersion + " failed", e);
            }
        }
        // 5.9.7版本添加teacherRole字段
        if (oldVersion < 6) {
            try {
                db.execSQL(String.format(
                        "ALTER TABLE " + ContactDao.TABLE_NAME + " ADD %s TEXT",
                        ContactDao.COLUMN_NAME_TEACHER_ROLE));
            } catch (SQLException e) {
                Logger.w("db",
                        "update from " + oldVersion + " to " + newVersion + " failed", e);
            }
        }
    }
    
    public void closeDB() {
        if (instance != null) {
            try {
                SQLiteDatabase db = instance.getWritableDatabase();
                db.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            instance = null;
        }
    }
    
}

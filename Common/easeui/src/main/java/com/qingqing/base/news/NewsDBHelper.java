package com.qingqing.base.news;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.qingqing.base.log.Logger;

import java.io.File;

/**
 * 1  建表
 * 2  conversation添加first
 * 3  conversation添加stickTop
 * 4  news添加msg_to，保存群聊groupId
 *
 * Created by huangming on 2016/11/29.
 */

public class NewsDBHelper extends SQLiteOpenHelper {

    private static final String TAG = "NewsDBHelper";
    private static final String DATABASE_NAME = "news.db";
    private static final int DATABASE_VERSION = 4;

    public NewsDBHelper(Context context) {
        super(context, new File(context.getFilesDir(), getDefaultDatabaseName())
                .getAbsolutePath(), null, DATABASE_VERSION);
    }

    private static String getDefaultDatabaseName() {
        return DATABASE_NAME;
        // return DATABASE_PREFIX_NAME + BaseData.getSafeUserId() +
        // DATABASE_SUFFIX_NAME;
    }

    private void createConversationTable(SQLiteDatabase db) {
        db.execSQL(String.format(
                "CREATE TABLE IF NOT EXISTS %s (%s TEXT, %s TEXT, %s TEXT, %s TEXT, %s INTEGER, %s INTEGER, %s INTEGER)",
                DBConstants.TABLE_CONVERSATION, DBConstants.ConversationColumns.KEY_ID,
                DBConstants.ConversationColumns.KEY_USER_ID,
                DBConstants.ConversationColumns.KEY_TYPE,
                DBConstants.ConversationColumns.KEY_NAME,
                DBConstants.ConversationColumns.KEY_FIRST,
                DBConstants.ConversationColumns.KEY_UNREAD_COUNT,
                DBConstants.ConversationColumns.KEY_STICK_TOP));
    }

    private void createNewsTable(SQLiteDatabase db) {
        db.execSQL(String.format(
                "CREATE TABLE IF NOT EXISTS %s (%s TEXT, %s TEXT, %s INTEGER, %s TEXT, %s TEXT, %s TEXT, %s INTEGER, %s LONG, %s TEXT, %s TEXT, %s TEXT)",
                DBConstants.TABLE_NEWS,
                DBConstants.NewsColumns.KEY_ID,
                DBConstants.NewsColumns.KEY_USER_ID,
                DBConstants.NewsColumns.KEY_TYPE,
                DBConstants.NewsColumns.KEY_BID,
                DBConstants.NewsColumns.KEY_BODY,
                DBConstants.NewsColumns.KEY_CONVERSATION_TYPE,
                DBConstants.NewsColumns.KEY_UNREAD,
                DBConstants.NewsColumns.KEY_CREATED_TIME,
                DBConstants.NewsColumns.KEY_FROM,
                DBConstants.NewsColumns.KEY_CONVERSATION_ID,
                DBConstants.NewsColumns.KEY_TO));
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createConversationTable(db);

        createNewsTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 5.6.5
        if (oldVersion <= 1) {
            try {
                Logger.i(TAG, "onUpgrade 5.6.5 from " + oldVersion + " to " + newVersion);
                db.execSQL(String.format(
                        "ALTER TABLE " + DBConstants.TABLE_CONVERSATION
                                + " ADD %s INTEGER",
                        DBConstants.ConversationColumns.KEY_FIRST));
            } catch (SQLException e) {
                Logger.e(TAG, "update 5.6.5 from " + oldVersion + " to " + newVersion
                        + " failed", e);
            }
        }

        // 5.8.0 增加是否置顶
        if (oldVersion <= 2) {
            try {
                Logger.i(TAG, "onUpgrade 5.8.0 from " + oldVersion + " to " + newVersion);
                db.execSQL(String.format(
                        "ALTER TABLE " + DBConstants.TABLE_CONVERSATION
                                + " ADD %s INTEGER",
                        DBConstants.ConversationColumns.KEY_STICK_TOP));
            } catch (SQLException e) {
                Logger.e(TAG, "update 5.8.0 from " + oldVersion + " to " + newVersion
                        + " failed", e);
            }
        }

        // news增加msg_to
        if(oldVersion <= 3){
            try {
                db.execSQL(String.format(
                        "ALTER TABLE " + DBConstants.TABLE_NEWS + " ADD %s TEXT",
                        DBConstants.NewsColumns.KEY_TO));
            } catch (SQLException e) {
                Logger.e(TAG, "update 5.8.0 from " + oldVersion + " to " + newVersion
                        + " failed", e);
            }
        }
    }
}

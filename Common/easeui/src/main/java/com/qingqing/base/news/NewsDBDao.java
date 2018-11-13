package com.qingqing.base.news;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.qingqing.base.data.BaseData;
import com.qingqing.base.log.Logger;
import com.qingqing.base.mqtt.MqttManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangming on 2016/11/29.
 */

public class NewsDBDao {

    private static final String TAG = "NewsDBDao";

    private SQLiteOpenHelper mDBHelper;

    public NewsDBDao(Context context) {
        this.mDBHelper = new NewsDBHelper(context);
    }

    private String getUserId() {
        return BaseData.getSafeUserId();
    }

    private SQLiteDatabase getDatabase() {
        return mDBHelper.getWritableDatabase();
    }

    private String buildConversationUserIdWhereSql() {
        return " (" + DBConstants.ConversationColumns.KEY_USER_ID + "='" + BaseData.getSafeUserId() + "') ";
    }

    private String buildNewsUserIdWhereSql() {
        return " (" + DBConstants.NewsColumns.KEY_USER_ID + "='" + BaseData.getSafeUserId() + "') ";
    }

    synchronized List<NewsConversation> getAllConversations() throws Exception {
        List<NewsConversation> allConversations = new ArrayList<>();

        Cursor cursor = null;
        try {
            cursor = getDatabase().query(DBConstants.TABLE_CONVERSATION,
                    DBConstants.ConversationColumns.COLUMNS,
                    buildConversationUserIdWhereSql(),
                    null, null, null, null);
            while (cursor.moveToNext()) {
                String id = cursor.getString(
                        cursor.getColumnIndex(DBConstants.ConversationColumns.KEY_ID));
                String type = cursor.getString(
                        cursor.getColumnIndex(DBConstants.ConversationColumns.KEY_TYPE));
                int unreadCount = cursor.getInt(cursor.getColumnIndex(
                        DBConstants.ConversationColumns.KEY_UNREAD_COUNT));
                int first = cursor.getInt(
                        cursor.getColumnIndex(DBConstants.ConversationColumns.KEY_FIRST));
                String name = cursor.getString(
                        cursor.getColumnIndex(DBConstants.ConversationColumns.KEY_NAME));
                boolean stickTop = (cursor.getInt(cursor.getColumnIndex(
                        DBConstants.ConversationColumns.KEY_STICK_TOP))) == 1;
                if (!TextUtils.isEmpty(type)) {
                    NewsConversation conversation = new NewsConversation(id, type, name);
                    conversation.setFirst(first != 1);
                    // conversation.setUnreadNewsCount(unreadCount);
                    conversation.setStickTop(stickTop);
                    allConversations.add(conversation);
                }
            }
        } catch (Exception e) {
            Logger.e(TAG, "getAllConversations : " + getUserId(), e);
            throw e;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return allConversations;

    }

    synchronized void saveConversations(NewsConversation conversation) throws Exception {
        ContentValues values = new ContentValues();
        values.put(DBConstants.ConversationColumns.KEY_ID, conversation.getId());
        values.put(DBConstants.ConversationColumns.KEY_TYPE, conversation.getType());
        values.put(DBConstants.ConversationColumns.KEY_USER_ID, getUserId());
        values.put(DBConstants.ConversationColumns.KEY_NAME, conversation.getName());
        values.put(DBConstants.ConversationColumns.KEY_UNREAD_COUNT,
                conversation.getUnreadNewsCount());
        values.put(DBConstants.ConversationColumns.KEY_FIRST,
                conversation.isFirst() ? 0 : 1);
        values.put(DBConstants.ConversationColumns.KEY_STICK_TOP,
                conversation.isStickTop() ? 1 : 0);
        try {
            getDatabase().replace(DBConstants.TABLE_CONVERSATION, null, values);
        } catch (Exception e) {
            Logger.e(TAG, "saveConversations : " + conversation.toString(), e);
            throw e;
        }
    }
    
    synchronized void setConversationStickTop(String conversationId, boolean stickTop)
            throws Exception {
        ContentValues values = new ContentValues();
        values.put(DBConstants.ConversationColumns.KEY_STICK_TOP, stickTop ? 1 : 0);
        try {
            getDatabase().update(DBConstants.TABLE_CONVERSATION, values,
                    DBConstants.ConversationColumns.KEY_ID + "='" + conversationId
                            + "' and " + buildConversationUserIdWhereSql(),
                    null);
        } catch (Exception e) {
            Logger.e(TAG, "setConversationStickTop : " + conversationId + ", stickTop="
                    + stickTop, e);
            throw e;
        }
    }
    
    synchronized boolean getConversationStickTop(String conversationId) throws Exception {
        Cursor cursor = null;
        boolean isStickTop = false;
        try {
            cursor = getDatabase().query(DBConstants.TABLE_CONVERSATION,
                    DBConstants.ConversationColumns.COLUMNS,
                    DBConstants.ConversationColumns.KEY_ID + "='" + conversationId
                            + "' and " + buildConversationUserIdWhereSql(),
                    null, null, null, null, null);
            while (cursor.moveToNext()) {
                int stickTop = cursor.getInt(cursor
                        .getColumnIndex(DBConstants.ConversationColumns.KEY_STICK_TOP));
                isStickTop = (stickTop == 1);
            }
        } catch (Exception e) {
            Logger.e(TAG, "getConversationStickTop : " + conversationId, e);
            throw e;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return isStickTop;
    }
    
    synchronized void saveUnreadNewsCount(String conversationId, int count)
            throws Exception {
        ContentValues values = new ContentValues();
        values.put(DBConstants.ConversationColumns.KEY_UNREAD_COUNT, count);
        try {
            getDatabase().update(
                    DBConstants.TABLE_CONVERSATION,
                    values,
                    DBConstants.ConversationColumns.KEY_ID + "='" + conversationId + "' and " + buildConversationUserIdWhereSql(),
                    null);
        } catch (Exception e) {
            Logger.e(TAG, "saveUnreadNewsCount : " + conversationId + ", count=" + count,
                    e);
            throw e;
        }
    }

    public synchronized List<News> getNewses(String conversationId, int offset, int count) throws Exception {
        List<News> newses = new ArrayList<>();

        Cursor cursor = null;
        try {
            cursor = getDatabase().query(DBConstants.TABLE_NEWS,
                    DBConstants.NewsColumns.COLUMNS,
                    DBConstants.NewsColumns.KEY_CONVERSATION_ID + "='" + conversationId + "' and " + buildNewsUserIdWhereSql(),
                    null,
                    null,
                    null,
                    DBConstants.NewsColumns.KEY_CREATED_TIME + " desc",
                    offset + "," + count);

            while (cursor.moveToNext()) {
                String newsId = cursor
                        .getString(cursor.getColumnIndex(DBConstants.NewsColumns.KEY_ID));
                int newsType = cursor
                        .getInt(cursor.getColumnIndex(DBConstants.NewsColumns.KEY_TYPE));
                String body = cursor.getString(
                        cursor.getColumnIndex(DBConstants.NewsColumns.KEY_BODY));
                int unread = cursor.getInt(
                        cursor.getColumnIndex(DBConstants.NewsColumns.KEY_UNREAD));
                long createdTime = cursor.getLong(
                        cursor.getColumnIndex(DBConstants.NewsColumns.KEY_CREATED_TIME));
                String bid = cursor.getString(
                        cursor.getColumnIndex(DBConstants.NewsColumns.KEY_BID));
                String from = cursor.getString(
                        cursor.getColumnIndex(DBConstants.NewsColumns.KEY_FROM));
                String to = cursor.getString(
                        cursor.getColumnIndex(DBConstants.NewsColumns.KEY_TO));
                String conversationType = cursor.getString(
                        cursor.getColumnIndex(DBConstants.NewsColumns.KEY_CONVERSATION_TYPE));
                News news = new News(newsId, newsType, body, bid, conversationType);
                news.setCreatedTime(createdTime);
                news.setUnread(unread == 1);
                news.setConversationId(conversationId);
                news.setFrom(from);
                news.setTo(to);
                newses.add(news);
                
                if (news.getCreatedTime() <= 0) {
                    Logger.w(MqttManager.TAG,
                            "load time from db invalid : " + news.getCreatedTime());
                }
            }
            Logger.i(TAG, "getNewses : " + conversationId + ", " + newses.size());
        } catch (Exception e) {
            Logger.e(TAG, "getAllConversations : " + getUserId(), e);
            throw e;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return newses;
    }

    synchronized void saveNews(News news) throws Exception {
        ContentValues values = new ContentValues();
        values.put(DBConstants.NewsColumns.KEY_ID, news.getId());
        values.put(DBConstants.NewsColumns.KEY_USER_ID, getUserId());
        values.put(DBConstants.NewsColumns.KEY_TYPE, news.getType());
        values.put(DBConstants.NewsColumns.KEY_BODY, news.getBody());
        values.put(DBConstants.NewsColumns.KEY_UNREAD, news.isUnread() ? 1 : 0);
        values.put(DBConstants.NewsColumns.KEY_CREATED_TIME, news.getCreatedTime());
        values.put(DBConstants.NewsColumns.KEY_BID, news.getBid());
        values.put(DBConstants.NewsColumns.KEY_FROM, news.getFrom());
        values.put(DBConstants.NewsColumns.KEY_CONVERSATION_ID, news.getConversationId());
        values.put(DBConstants.NewsColumns.KEY_CONVERSATION_TYPE, news.getConversationType());
        try {
            getDatabase().replace(DBConstants.TABLE_NEWS, null, values);
        } catch (Exception e) {
            Logger.e(TAG, "saveNews : " + news.toString(), e);
            throw e;
        }
    }

    public synchronized void updateNews(News news) throws Exception {
        
        String where = DBConstants.NewsColumns.KEY_ID + "='" + news.getId() + "' and "
                + buildNewsUserIdWhereSql();
        Cursor cursor = getDatabase().query(DBConstants.TABLE_NEWS, null, where, null,
                null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            ContentValues values = new ContentValues();
            values.put(DBConstants.NewsColumns.KEY_BODY, news.getBody());
            values.put(DBConstants.NewsColumns.KEY_CREATED_TIME, news.getCreatedTime());
            
            getDatabase().update(DBConstants.TABLE_NEWS, values, where, null);
        }
        else {
            saveNews(news);
        }
        
    }

    synchronized void saveUnreadNewsAsRead(String newsId) {
        ContentValues values = new ContentValues();
        values.put(DBConstants.NewsColumns.KEY_UNREAD, 0);
        try {
            getDatabase().update(
                    DBConstants.TABLE_NEWS,
                    values,
                    DBConstants.NewsColumns.KEY_ID + "=" + newsId,
                    null);
        } catch (Exception e) {
            Logger.e(TAG,
                    "saveUnreadNewsAsRead : " + newsId, e);
            throw e;
        }
    }

    synchronized void deleteConversation(String conversationId) throws Exception {
        try {
            getDatabase().delete(
                    DBConstants.TABLE_CONVERSATION,
                    DBConstants.ConversationColumns.KEY_ID + "='" + conversationId + "' and " + buildConversationUserIdWhereSql(),
                    null);
        } catch (Exception e) {
            Logger.e(TAG,
                    "deleteConversation : " + conversationId, e);
            throw e;
        }
    }

    synchronized void deleteAllNewsesOfConversation(String conversationId) throws Exception {
        try {
            getDatabase().delete(
                    DBConstants.TABLE_NEWS,
                    DBConstants.NewsColumns.KEY_CONVERSATION_ID + "='" + conversationId + "' and " + buildNewsUserIdWhereSql(),
                    null);
        } catch (Exception e) {
            Logger.e(TAG,
                    "deleteAllNewsesOfConversation : " + conversationId, e);
            throw e;
        }
    }

    synchronized News deleteNewsBy(String bid) throws Exception {
        News news = null;
        Cursor cursor = null;
        try {
            cursor = getDatabase().query(DBConstants.TABLE_NEWS,
                    DBConstants.NewsColumns.COLUMNS,
                    DBConstants.NewsColumns.KEY_BID + "='" + bid + "'",
                    null,
                    null,
                    null,
                    null,
                    null);

            if (cursor.moveToFirst()) {
                String newsId = cursor
                        .getString(cursor.getColumnIndex(DBConstants.NewsColumns.KEY_ID));
                int newsType = cursor
                        .getInt(cursor.getColumnIndex(DBConstants.NewsColumns.KEY_TYPE));
                String body = cursor.getString(
                        cursor.getColumnIndex(DBConstants.NewsColumns.KEY_BODY));
                int unread = cursor.getInt(
                        cursor.getColumnIndex(DBConstants.NewsColumns.KEY_UNREAD));
                long createdTime = cursor.getLong(
                        cursor.getColumnIndex(DBConstants.NewsColumns.KEY_CREATED_TIME));
                String from = cursor.getString(
                        cursor.getColumnIndex(DBConstants.NewsColumns.KEY_FROM));
                String to = cursor.getString(
                        cursor.getColumnIndex(DBConstants.NewsColumns.KEY_TO));
                String conversationType = cursor.getString(
                        cursor.getColumnIndex(DBConstants.NewsColumns.KEY_CONVERSATION_TYPE));
                String conversationId = cursor.getString(
                        cursor.getColumnIndex(DBConstants.NewsColumns.KEY_CONVERSATION_ID));
                news = new News(newsId, newsType, body, bid, conversationType);
                news.setCreatedTime(createdTime);
                news.setUnread(unread == 1);
                news.setConversationId(conversationId);
                news.setFrom(from);
                news.setTo(to);
            }
        } catch (Exception e) {
            Logger.e(TAG, "deleteNewsBy getNewsBy : " + bid, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        try {
            getDatabase().delete(
                    DBConstants.TABLE_NEWS,
                    DBConstants.NewsColumns.KEY_BID + "='" + bid + "'",
                    null);
        } catch (Exception e) {
            Logger.e(TAG,
                    "deleteNewsBy : " + bid, e);
            throw e;
        }
        return news;
    }

    synchronized void saveUnreadNewsOfConversationAsRead(String conversationId) throws Exception {
        ContentValues values = new ContentValues();
        values.put(DBConstants.NewsColumns.KEY_UNREAD, 0);
        try {
            getDatabase().update(
                    DBConstants.TABLE_NEWS,
                    values,
                    DBConstants.NewsColumns.KEY_UNREAD + "=1" + " and " + DBConstants.NewsColumns.KEY_CONVERSATION_ID + "='" + conversationId + "' and " + buildNewsUserIdWhereSql(),
                    null);
        } catch (Exception e) {
            Logger.e(TAG,
                    "saveUnreadNewsOfConversationAsRead : " + conversationId, e);
            throw e;
        }
    }

    synchronized void saveConversationAsNotFirst(String conversationId) throws Exception {
        ContentValues values = new ContentValues();
        values.put(DBConstants.ConversationColumns.KEY_FIRST, 1);
        try {
            getDatabase().update(
                    DBConstants.TABLE_CONVERSATION,
                    values,
                    DBConstants.ConversationColumns.KEY_ID + "='" + conversationId + "' and " + buildConversationUserIdWhereSql(),
                    null);
        } catch (Exception e) {
            Logger.e(TAG, "saveConversationAsNotFirst : " + conversationId,
                    e);
            throw e;
        }
    }

    synchronized int getUnreadNewsCountOfConversation(String conversationId) throws Exception {
        Cursor cursor = null;
        try {
            cursor = getDatabase().query(DBConstants.TABLE_NEWS,
                    DBConstants.NewsColumns.COLUMNS,
                    DBConstants.NewsColumns.KEY_UNREAD + "=1" + " and " + DBConstants.NewsColumns.KEY_CONVERSATION_ID + "='" + conversationId + "' and " + buildNewsUserIdWhereSql(),
                    null,
                    null,
                    null,
                    null,
                    null);
            int count = cursor.getCount();
            Logger.i(TAG, "getUnreadNewsCountOfConversation : " + conversationId + ", " + count);
            return count;
        } catch (Exception e) {
            Logger.e(TAG, "getUnreadNewsCountOfConversation : " + getUserId(), e);
            throw e;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

}

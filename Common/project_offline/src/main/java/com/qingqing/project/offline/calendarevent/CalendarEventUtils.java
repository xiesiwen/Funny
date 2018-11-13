package com.qingqing.project.offline.calendarevent;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.qingqing.base.data.SPManager;
import com.qingqing.base.log.Logger;
import com.qingqing.base.utils.DateUtils;
import com.qingqing.project.offline.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * 日历事件功能类
 *
 * Created by tanwei on 2016/4/26.
 */
public class CalendarEventUtils {

    private static final String TAG = "calendar";

    private static final String SP_CALENDAR_ID = "calendar_id";

    // 创建日历的字段值
    private static final String CUSTOM_ACCOUNT_NAME = "qingqing student";
    private static final String CUSTOM_DISPLAY_NAME = "轻轻家教";
    private static final String CUSTOM_OWNER_ACCOUNT = "qingqing";

    // 为了保持兼容，将相关字段重新列出
    private static final String _ID = "_id";

    // Calendars
    private static final String CALENDAR_ACCOUNT_NAME = "account_name";
    private static final String CALENDAR_NAME = "name";
    private static final String CALENDAR_DISPLAY_NAME = "calendar_displayName";
    private static final String CALENDAR_SYNC_EVENTS = "sync_events";
    private static final String CALENDAR_ACCOUNT_TYPE = "account_type";
    private static final String CALENDAR_COLOR = "calendar_color";
    private static final String CALENDAR_VISIBLE = "visible";
    private static final String CALENDAR_TIME_ZONE = "calendar_timezone";
    private static final String CALENDAR_ACCESS_LEVEL = "calendar_access_level";
    private static final String CALENDAR_OWNER_ACCOUNT = "ownerAccount";
    private static final String CAN_ORGANIZER_RESPOND = "canOrganizerRespond";
    private static final String CALLER_IS_SYNCADAPTER = "caller_is_syncadapter";

    public static final String ACCOUNT_TYPE_LOCAL = "LOCAL";

    public static final int CAL_ACCESS_OWNER = 700;
    // Events
    private static final String EVENT_CALENDAR_ID = "calendar_id";
    private static final String EVENT_TITLE = "title";
    private static final String EVENT_DELETED = "deleted";
    private static final String EVENT_DESCRIPTION = "description";
    private static final String EVENT_LOCATION = "eventLocation";
    private static final String EVENT_DTSTART = "dtstart";
    private static final String EVENT_DTEND = "dtend";
    private static final String EVENT_TIMEZONE = "eventTimezone";
    private static final String EVENT_END_TIMEZONE = "eventEndTimezone";

    private static final String SYNC_DATA5 = "sync_data5";// 额外使用一个字段来保存关联id

    // Reminders
    private static final String REMINDER_EVENT_ID = "event_id";
    private static final String REMINDER_MINUTES = "minutes";
    private static final String REMINDER_METHOD = "method";

    public static final int METHOD_ALERT = 1;

    private static final String calenderURL = "content://com.android.calendar/calendars";
    private static final String calenderEventURL = "content://com.android.calendar/events";
    private static final String calenderReminderURL = "content://com.android.calendar/reminders";
    private static final Uri calenderURI = Uri.parse(calenderURL);
    private static final Uri eventURI = Uri.parse(calenderEventURL);
    private static final Uri reminderURI = Uri.parse(calenderReminderURL);

    public static String id;

    /** 获取日历id */
    public static boolean generateCalendarId(Context context) {
        if (id == null || id.length() == 0) {
            queryCalendars(context);
            
            if (id == null || id.length() == 0) {
                
                Logger.v(TAG, "query calendar failed");
                createCalendar(context);
                
                if (id == null || id.length() == 0) {
                    
                    Logger.v(TAG, "create calendar failed");
                    return false;
                }
                else {
                    SPManager.put(SP_CALENDAR_ID, id);
                }
            }
            else {
                SPManager.put(SP_CALENDAR_ID, id);
            }
        }
        
        return true;
    }

    /** 遍历日历账户 */
    public static void queryCalendars(Context context) {
        Cursor userCursor = context.getContentResolver().query(calenderURI, null, null,
                null, null);
        
        if (userCursor == null) {
            Logger.w(TAG, "queryCalendars cursor result null");
            return;
        }
        
        Logger.v(TAG, "Count: " + userCursor.getCount());

//        String firstId = null;
        for (userCursor.moveToFirst(); !userCursor.isAfterLast(); userCursor.moveToNext()) {
            
            String name = userCursor.getString(userCursor.getColumnIndex(CALENDAR_NAME));
            String account_name = userCursor.getString(userCursor
                    .getColumnIndex(CALENDAR_ACCOUNT_NAME));
            Logger.v(TAG, "account_name: " + account_name + ", name : " + name);

            String id = userCursor.getString(userCursor.getColumnIndex(_ID));

//            if(firstId == null){
//                firstId = id;
//            }
//
//            if (account_name.contains("System")) {
//                Logger.v(TAG, "system calendar id " + id);
//                CalendarEventUtils.id = id;
//            }
            if (account_name.contains(CUSTOM_ACCOUNT_NAME)) {
                Logger.v(TAG, "qingqing calendar id " + id);
                CalendarEventUtils.id = id;
            }
        }

//        if(id == null && firstId != null){
//            id = firstId;
//        }
        
        userCursor.close();
    }

    /** 创建本地日历 */
    public static void createCalendar(Context context) {
        
        TimeZone timeZone = TimeZone.getDefault();
        ContentValues value = new ContentValues();
        value.put(CALENDAR_NAME, CUSTOM_ACCOUNT_NAME);
        
        value.put(CALENDAR_ACCOUNT_NAME, CUSTOM_ACCOUNT_NAME);
        value.put(CALENDAR_ACCOUNT_TYPE, ACCOUNT_TYPE_LOCAL);
        value.put(CALENDAR_DISPLAY_NAME, CUSTOM_DISPLAY_NAME);
        value.put(CALENDAR_VISIBLE, 1);
        value.put(CALENDAR_COLOR, context.getResources().getColor(R.color.primary_green));
        value.put(CALENDAR_ACCESS_LEVEL, CAL_ACCESS_OWNER);
        value.put(CALENDAR_SYNC_EVENTS, 1);
        value.put(CALENDAR_TIME_ZONE, timeZone.getID());
        value.put(CALENDAR_OWNER_ACCOUNT, CUSTOM_OWNER_ACCOUNT);
        value.put(CAN_ORGANIZER_RESPOND, 0);
        
        Uri calendarUri = Uri.parse(calenderURL);
        calendarUri = calendarUri.buildUpon()
                .appendQueryParameter(CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CALENDAR_ACCOUNT_NAME, CUSTOM_ACCOUNT_NAME)
                .appendQueryParameter(CALENDAR_ACCOUNT_TYPE, ACCOUNT_TYPE_LOCAL).build();
        
        Uri uri = context.getContentResolver().insert(calendarUri, value);
        
        if (uri == null) {
            Logger.w(TAG, "create calendar failed");
            return;
        }
        Long calId = Long.parseLong(uri.getLastPathSegment());
        if (calId != 0) {
//            Toast.makeText(context, "新建日程成功", Toast.LENGTH_SHORT).show();
            id = String.valueOf(calId);
            Logger.w(TAG, "create calendar suc id " + id);
        }
        else {
            Logger.v(TAG, "create calendar failed");
//            Toast.makeText(context, "新建日程失败", Toast.LENGTH_SHORT).show();
        }
    }

    /** 判断event是否存在（根据title） */
    public static boolean isEventExists(Context context, String title) {
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(eventURI, null,
                EVENT_CALENDAR_ID + "=? and " + EVENT_TITLE + "=?",
                new String[] { id, title }, null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        
        return false;
    }

    /** 查询所有日历事件（test） */
    public static void queryEvents(Context context){
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(eventURI, null, EVENT_CALENDAR_ID + "=?" , new String[] { id }, null);
        if(cursor!= null) {
            while (cursor.moveToNext()) {
                String title2 = cursor
                        .getString(cursor.getColumnIndex(EVENT_TITLE));
                long id2 = cursor.getLong(cursor.getColumnIndex(_ID));
                String des2 = cursor
                        .getString(cursor.getColumnIndex(EVENT_DESCRIPTION));
                String start2 = DateUtils.ymdhmSdf.format(new Date(
                        cursor.getLong(cursor.getColumnIndex(EVENT_DTSTART))));

                Logger.v(TAG, "eventId: " + id2 + ", title: " + title2 + ", des: "
                        + des2 + ", start: " + start2);
                Logger.i(TAG, "availability: "
                        + cursor.getInt(cursor.getColumnIndex("availability"))
                        + ", deleted: "
                        + cursor.getInt(cursor.getColumnIndex(EVENT_DELETED))
                        + ", visible: " + cursor.getInt(
                        cursor.getColumnIndex("visible"))
                        + ", eventStatus: " + cursor.getInt(
                        cursor.getColumnIndex("eventStatus")));
//            delEventWithEventId(context, id2);
            }

            cursor.close();
        }
    }

    /** 判断event是否存在（根据event id） */
    public static boolean isEventExistsWithEventId(Context context, long eventId) {
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(eventURI, null, EVENT_CALENDAR_ID + "=? and "
                + _ID + "=?", new String[] { id, String.valueOf(eventId) }, null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }

        return false;
    }

    /** 判断event是否存在（根据bind id） */
    public static boolean isEventExistsWithBindId(Context context, String bindId) {
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(eventURI, null, EVENT_CALENDAR_ID + "=? and "
                + SYNC_DATA5 + "=?", new String[] { id, bindId }, null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }

        return false;
    }
    
    /** 添加event */
    public static long addEvent(Context context, CalendarEventInfo info) {

        ContentResolver cr = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(EVENT_CALENDAR_ID, id);
        values.put(EVENT_DTSTART, info.getStartTimeMillis());
        values.put(EVENT_DTEND, info.getEndTimeMillis());
        values.put(EVENT_TITLE, info.getTitle());
        values.put(EVENT_DESCRIPTION, info.getDescription());
        values.put(EVENT_LOCATION, info.getLocation());
        String timeZone = TimeZone.getDefault().getID();
        values.put(EVENT_TIMEZONE, timeZone);
        values.put(EVENT_END_TIMEZONE, timeZone);
//        String bindId = info.getBindId();
//        if(!TextUtils.isEmpty(bindId)) {
//            values.put(SYNC_DATA5, bindId);
//        }
//        Uri eventUri = eventURI.buildUpon()
//                .appendQueryParameter(CALLER_IS_SYNCADAPTER, "true")
//                .appendQueryParameter(CALENDAR_ACCOUNT_NAME, CUSTOM_ACCOUNT_NAME)
//                .appendQueryParameter(CALENDAR_ACCOUNT_TYPE, ACCOUNT_TYPE_LOCAL).build();
        Uri uri = cr.insert(eventURI, values);
        
        if (uri == null) {
            Logger.w(TAG, "add event failed");
            return 0;
        }
        
        Long eventId = Long.parseLong(uri.getLastPathSegment());
        // 提醒
        ContentValues reminder = new ContentValues();
        reminder.put(REMINDER_EVENT_ID, eventId);
        reminder.put(REMINDER_MINUTES, info.getReminderMinutes());
        reminder.put(REMINDER_METHOD, info.getReminderMethod());
        context.getContentResolver().insert(reminderURI, reminder);
        Logger.v(TAG, "add event suc " + eventId);

        return eventId;
    }

    /** 添加event(某些手机删不掉event，但是无法恢复为可见状态，尝试重新创建) */
    public static long addEventIfExists(Context context, CalendarEventInfo info) {
        
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(eventURI, null,
                EVENT_CALENDAR_ID + "=? and " + EVENT_TITLE + "=?",
                new String[] { id, info.getTitle() }, null);
        
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            long eventId = cursor.getLong(cursor.getColumnIndex(_ID));
            int deleted = cursor.getInt(cursor.getColumnIndex(EVENT_DELETED));
            
            if (eventId > 0 && deleted == 1) {
                Logger.v(TAG, "add event(exists) deleted ");
                cursor.close();
                return addEvent(context, info);
            }
        }
        
        if (cursor != null) {
            cursor.close();
        }
        
        return 0;
    }
    
    /** 更新event的删除状态为0（test） */
    public static void updateEvent(Context context, long eventId) {

        ContentResolver cr = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(EVENT_DELETED, 0);
        values.put("eventStatus", 0);
        int rows;
        rows = cr.update(eventURI, values,
                EVENT_CALENDAR_ID + "=?" + " and " + _ID + "=?",
                new String[] { id, String.valueOf(eventId) });
        if (rows > 0) {
            Logger.v(TAG, "update event suc");
        }
        else {
            // Toast.makeText(context, "更新失败", Toast.LENGTH_SHORT).show();
            Logger.v(TAG, "update event failed");
        }
    }

    /** 删除用户的所有事件 */
    public static int delAllEvent(Context context) {
        
        int rows = context.getContentResolver().delete(eventURI,
                EVENT_CALENDAR_ID + "=?", new String[] { id });
        if (rows > 0) {
            Logger.v(TAG, "del event suc of calendar id " + id);
        }
        else {
            Logger.v(TAG, "del event failed of calendar id " + id);
        }

        return rows;
    }

    /** 删除指定id对应的事件 */
    public static int delEventWithEventId(Context context, long eventId) {

        Uri eventUri = ContentUris.withAppendedId(eventURI, eventId);
        int rows = context.getContentResolver().delete(eventUri,null, null);
        if (rows > 0) {
            Logger.v(TAG, "del event suc of event id " + eventId);
        }
        else {
            Logger.v(TAG, "del event failed of event id " + eventId);
        }

        return rows;
    }

}

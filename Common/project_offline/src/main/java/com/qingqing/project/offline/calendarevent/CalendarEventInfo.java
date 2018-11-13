package com.qingqing.project.offline.calendarevent;

/**
 * 日历事件字段封装
 *
 * Created by tanwei on 2016/4/30.
 */
public class CalendarEventInfo {
    
    private String title;// 标题
    
    private long startTimeMillis, endTimeMillis;// 起始时间
            
    private String description;// 描述
    
    private String location;// 地点
    
    private int reminderMinutes;// 提前多少分钟提醒
    
    private int reminderMethod;// 提醒方式，默认弹框

    private String bindId;// 绑定事件id
    
    public CalendarEventInfo() {
        reminderMethod = CalendarEventUtils.METHOD_ALERT;
    }
    
    public String getTitle() {
        return title;
    }
    
    public CalendarEventInfo setTitle(String title) {
        this.title = title;
        return this;
    }
    
    public long getStartTimeMillis() {
        return startTimeMillis;
    }
    
    public CalendarEventInfo setStartTimeMillis(long startTimeMillis) {
        this.startTimeMillis = startTimeMillis;
        return this;
    }
    
    public long getEndTimeMillis() {
        return endTimeMillis;
    }
    
    public CalendarEventInfo setEndTimeMillis(long endTimeMillis) {
        this.endTimeMillis = endTimeMillis;
        return this;
    }
    
    public String getDescription() {
        return description;
    }
    
    public CalendarEventInfo setDescription(String description) {
        this.description = description;
        return this;
    }
    
    public String getLocation() {
        return location;
    }
    
    public CalendarEventInfo setLocation(String location) {
        this.location = location;
        return this;
    }
    
    public int getReminderMinutes() {
        return reminderMinutes;
    }
    
    public CalendarEventInfo setReminderMinutes(int reminderMinutes) {
        this.reminderMinutes = reminderMinutes;
        return this;
    }
    
    public int getReminderMethod() {
        return reminderMethod;
    }
    
    public CalendarEventInfo setReminderMethod(int reminderMethod) {
        this.reminderMethod = reminderMethod;
        return this;
    }

    public String getBindId() {
        return bindId;
    }

    public void setBindId(String bindId) {
        this.bindId = bindId;
    }
}

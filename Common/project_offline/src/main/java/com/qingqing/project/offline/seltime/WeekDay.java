package com.qingqing.project.offline.seltime;

/**
 * 
 * 定义星期（从周一开始到周日结束，0~6） 日历是从周日开始到周六结束（1~7）
 * 
 * @author tanwei
 * 
 */
public enum WeekDay {
    
    MONDAY(0), TUESDAY(1), WEDNESDAY(2), THURSDAY(3), FRIDAY(4), SATURDAY(5), SUNDAY(6);
    
    private int index;
    
    WeekDay(int index) {
        this.index = index;
    }
    
    public static WeekDay valueOf(int index) {
        switch (index) {
            case 0:
                return MONDAY;
            case 1:
                return TUESDAY;
            case 2:
                return WEDNESDAY;
            case 3:
                return THURSDAY;
            case 4:
                return FRIDAY;
            case 5:
                return SATURDAY;
            case 6:
                return SUNDAY;

            default:
                return MONDAY;
        }
    }

    private static String[] weekStr = new String[] { "星期一", "星期二", "星期三", "星期四",
            "星期五", "星期六", "星期日" };

    private static String[] weekStrSimple = new String[] { "周一", "周二", "周三", "周四", "周五",
            "周六", "周日" };

    /** 获取星期全称，形如星期一 */
    public static String getWeekString(WeekDay week) {
        return weekStr[week.value()];
    }

    /** 获取星期简称，形如周一 */
    public static String getWeekStringSimple(int week) {
        return weekStrSimple[week];
    }

    public int value() {
        return index;
    }
    
    /** 获取星期的简称，形如一、二 */
    @Override
    public String toString() {
        String week = null;
        
        switch (index) {
            case 0:
                week = "一";
                break;
            case 1:
                week = "二";
                break;
            case 2:
                week = "三";
                break;
            case 3:
                week = "四";
                break;
            case 4:
                week = "五";
                break;
            case 5:
                week = "六";
                break;
            case 6:
                week = "日";
                break;
        }
        
        return week;
    }
    
}

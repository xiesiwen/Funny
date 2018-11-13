package com.qingqing.project.offline.studentresource;

import com.qingqing.base.utils.TimeUtil;

/**
 * Created by wangxiaxin on 2017/6/26.
 */

public class StudentResourceUtil {

    /**
     * 获取生源宝中用到的 时间显示
     */
    public static final String getStudentResourceDateString(long time) {
        long tt = TimeUtil.getCountInMinutes(time); // 分钟数
        long hour = tt / 60; // 小时数
        if (hour >= 24) {
            return hour / 24 + " 天前";
        }
        else if (hour > 0) {
            return hour + " 小时前";
        }
        else if (tt > 1){
            return tt + " 分钟前";
        }else{
            return "刚刚";
        }
    }
}

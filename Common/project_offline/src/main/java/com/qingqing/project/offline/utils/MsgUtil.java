package com.qingqing.project.offline.utils;

import com.qingqing.base.utils.TimeUtil;

/**
 * Created by wangxiaxin on 2017/6/26.
 */

public class MsgUtil {

    /**
     * 获取IM以及消息中用到的 时间显示
     */
    public static final String getMsgReceivedDateString(long time) {
        long tt = TimeUtil.getCountInMinutes(time); // 分钟数
        long hour = tt / 60; // 小时数
        if (hour >= 24) {
            return hour / 24 + " 天前";
        }
        else if (hour > 0) {
            return hour + " 小时前";
        }
        else if (tt > 3) {
            return tt + " 分钟前";
        }
        else {
            return "刚刚";
        }
    }
}

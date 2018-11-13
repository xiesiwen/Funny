package com.qingqing.project.offline.seltime;

import java.util.Date;

/**
 * 选中日期回调接口
 *
 * Created by tanwei on 2015/11/27.
 */
public interface IOnDateChangedListener {
    void onDateChanged(WeekDay weekDay, Date date);
}

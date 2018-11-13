package com.qingqing.project.offline.order.v3;

import com.qingqing.project.offline.seltime.TimeSlice;

import java.util.Comparator;

/**
 * 时间排序，按先后顺序排序
 *
 * Created by tanwei on 2017/8/31.
 */

public class TimeComparator implements Comparator<TimeSlice> {
    @Override
    public int compare(TimeSlice lhs, TimeSlice rhs) {
        long timeLeft = lhs.getStartDate().getTime();
        long timeRight = rhs.getStartDate().getTime();
        if (timeLeft > timeRight) {
            return 1;
        }
        else if (timeLeft < timeRight) {
            return -1;
        }
        return 0;
    }
}

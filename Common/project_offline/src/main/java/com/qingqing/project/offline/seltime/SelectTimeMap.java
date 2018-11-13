package com.qingqing.project.offline.seltime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

/**
 * 存放选中的时间段，以日期生成的“yyyy-MM-dd”格式作为key，值为ArrayList
 * 
 * @author tanwei
 * 
 */
public class SelectTimeMap {
    
    private HashMap<String, ArrayList<TimeSlice>> mSelectedTimeSlice;
    
    private ArrayList<TimeSlice> tempList;// 作为结果的容器，避免getTimes()多次调用重复创建
    
    public SelectTimeMap() {
        mSelectedTimeSlice = new HashMap<>();
        comparator = new TimeSliceComparator();
    }
    
    /** 清空所有选中时间块 */
    public void removeTime() {
        mSelectedTimeSlice.clear();
    }
    
    /** 清空指定日期的所有时段 */
    public void removeTime(String date) {
        if (mSelectedTimeSlice.containsKey(date))
            mSelectedTimeSlice.remove(date);
    }
    
    /** 添加选中时间段 */
    public void addTime(TimeSlice time) {
        addTime(SelectTimeUtils.getDateFormatYMD(time.getStartDate()), time);
    }
    
    /** 向指定日期添加选中块 */
    public void addTime(String date, TimeSlice time) {
        if (!mSelectedTimeSlice.containsKey(date)) {
            mSelectedTimeSlice.put(date, new ArrayList<TimeSlice>());
        }
        mSelectedTimeSlice.get(date).add(time);
    }
    
    /** 获取指定日期的所有选中时间块 */
    public ArrayList<TimeSlice> getTimes(String date) {
        if (mSelectedTimeSlice.containsKey(date)) {
            return mSelectedTimeSlice.get(date);
        }
        else {
            mSelectedTimeSlice.put(date, new ArrayList<TimeSlice>());
        }
        return mSelectedTimeSlice.get(date);
    }
    
    /** 获取所有选中时间块 */
    public ArrayList<TimeSlice> getTimes() {
        if (tempList == null) {
            tempList = new ArrayList<>();
        }
        else if (tempList.size() > 0) {
            tempList.clear();
        }
        
        Iterator<ArrayList<TimeSlice>> iterator = mSelectedTimeSlice.values().iterator();
        while (iterator.hasNext()) {
            tempList.addAll(iterator.next());
        }
        
        sortTimeSlice();
        return tempList;
        
    }

    // 按时间先手顺序排序
    private void sortTimeSlice() {
        if (tempList.size() > 1) {
            Collections.sort(tempList, comparator);
        }
    }
    
    private TimeSliceComparator comparator;
    
    private class TimeSliceComparator implements Comparator<TimeSlice> {
        
        @Override
        public int compare(TimeSlice time1, TimeSlice time2) {
            Date date1 = time1.getStartDate(), date2 = time2.getStartDate();
            if (date1.after(date2)) {
                return 1;
            }
            else if (date1.before(date2)) {
                return -1;
            }
            return 0;
        }
    }
}

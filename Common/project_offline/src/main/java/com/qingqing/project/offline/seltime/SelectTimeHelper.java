package com.qingqing.project.offline.seltime;

import android.util.SparseIntArray;

import com.qingqing.base.data.BaseData;
import com.qingqing.base.log.Logger;

import java.util.Calendar;
import java.util.Date;

/**
 * 课时的选择逻辑和数据计算
 * 
 * @author tanwei
 * 
 */
public class SelectTimeHelper {

    private static final String TAG = "SelectHelper";

    public static final int LIMIT_SINGLE = 1; // 老师开课：只能单选
    public static final int LIMIT_FREE = 2;// 免费试听课：只能选2个时间块
    public static final int LIMIT_DEFAULT_COUNT = 4;// 默认一个课次时间：4个时间块
    public static final int LIMIT_SPECIFIED_COUNT = 8;// 调课：只能选和原有课一样的时间长度
    public static final int LIMIT_RANDOM_COUNT = 16;// 续课：可以选一段任意长度大于等于4的连续时间块
    public static final int LIMIT_NONE = 32;// 下单：任意数量的大于等于4的连续时间块

    public static final int BLOCK_AFTERNOON_START = 8;
    public static final int BLOCK_EVENING_START = 20;


    // 默认配置
    public static int sSliceCount = 36;
    public static int sSliceStartBlock = -4;
    public static int sSliceEndBlock = 31;
    public static int sSliceEndHour = 24;

    private int mSceneType;

    private int mLimitedType;
    
    private int mLimitedTime;
    
    private boolean isTeacherNeedBreak;// 老师是否需要课前后休息时间
    
    private SparseIntArray statusSparseArray;
    
    private Calendar mCalendar;
    
    private SelectTimeMap mSelectTimeMap;
    
    private String mSelectedDateKey;

    private boolean isOpenLimit2;// 是否可选最少2个时间块
    
    public SelectTimeHelper() {
        // 预先读取配置
        updateTimeConfig();
        
        statusSparseArray = new SparseIntArray(sSliceCount);
        SelectTimeUtils.initTimeStatus(statusSparseArray);
        
        mCalendar = Calendar.getInstance();
        
        mSelectTimeMap = new SelectTimeMap();
    }
    
    /** 读取配置确定是否切换新的时间段 */
    public static void updateTimeConfig() {
        boolean expanded = BaseData.isTimeExpand();
        if (expanded) {
            sSliceCount = 36;
            sSliceStartBlock = -4;
            sSliceEndBlock = sSliceStartBlock + sSliceCount - 1;
            sSliceEndHour = 24;
        }else{
            sSliceCount = 28;
            sSliceStartBlock = 0;
            sSliceEndBlock = sSliceStartBlock + sSliceCount - 1;
            sSliceEndHour = 22;
        }
        
        Logger.v(TAG, "updateTimeConfig expand  " + expanded + ",count=" + sSliceCount);
    }
    
    /** 设置对选择时间段数量的限制 */
    public void setTypeAndLimitedTime(int sceneType, int limit) {
         mSceneType = sceneType;

        switch (sceneType) {
        
            case SelectTimeUtils.SCENE_TYPE_MATCH_AUDITION:
                mLimitedType = LIMIT_FREE;
                break;
            
            case SelectTimeUtils.SCENE_TYPE_AUDITION:
                mLimitedType = LIMIT_FREE;
                break;
            
            case SelectTimeUtils.SCENE_TYPE_MATCH:
                mLimitedType = LIMIT_RANDOM_COUNT;
                break;
            
            case SelectTimeUtils.SCENE_TYPE_ORDER:
                mLimitedType = LIMIT_NONE;
                break;
            case SelectTimeUtils.SCENE_TYPE_RENEW_ORDER:
                mLimitedType = LIMIT_RANDOM_COUNT;
                break;
            case SelectTimeUtils.SCENE_TYPE_CHANGE_ORDER:
                mLimitedType = LIMIT_SPECIFIED_COUNT;
                mLimitedTime = limit;
                break;
            
            case SelectTimeUtils.SCENE_TYPE_SET_TEACHER_TIME:
                mLimitedType = LIMIT_SINGLE;
                break;
            
            default:
                mLimitedType = LIMIT_RANDOM_COUNT;
                break;
        }
        
         Logger.v(TAG, "type = " + mSceneType + ",limitType = " +
         mLimitedType+",limitTime = " + mLimitedTime);
    }
    
    /** 设置当前选择的日期 */
    public void setDate(Date date) {
        mCalendar.setTime(date);
        mSelectedDateKey = SelectTimeUtils.getDateFormatYMD(date);
    }
    
    /** 设置是否需要课间休息 */
    public void setIsNeedBreak(boolean isNeed) {
        isTeacherNeedBreak = isNeed;
    }

    /** 设置是否可选最少2个时间块 */
    public void setOpenLimit2(boolean isOpen) {
        isOpenLimit2 = isOpen;
    }

    public SparseIntArray getStatusSparseArray() {
        return statusSparseArray;
    }

    /** 更新已选中的状态，并保存 */
    public void updateStatusSelected(SparseIntArray array) {
        for (TimeSlice time : mSelectTimeMap.getTimes(mSelectedDateKey)) {

            int end = time.getEnd();
            // 注意：已选中状态下标是计算过的block，而非点击view对应的index
            for (int i = time.getStart(); i <= end; i++) {
                if (array.get(i) == TimeSliceView.STATUS_VALID
                        || array.get(i) == TimeSliceView.STATUS_OLD_LESSON) {
                    array.put(i, TimeSliceView.STATUS_SELECTED);
                }
            }
        }

        SelectTimeUtils.copySparseArray(array, statusSparseArray);
    }

    /** 恢复老师原有课的时间 */
    public void resetOldTime(TimeSlice time) {
        int end = time.getEnd();
        for (int i = time.getStart(); i <= end; i++) {
            if (statusSparseArray.get(i) == TimeSliceView.STATUS_VALID) {
                statusSparseArray.put(i, TimeSliceView.STATUS_OLD_LESSON);
            }
        }
    }

    public int getKeyAt(int index) {
        return statusSparseArray.keyAt(index);
    }

    public int getStatusAt(int index) {
        return statusSparseArray.valueAt(index);
    }
    
    public SelectTimeMap getSelectTimeMap() {
        return mSelectTimeMap;
    }
    
    public String getDateKey() {
        return mSelectedDateKey;
    }
    
    /** 根据选中下标开始计算 */
    public void select(int index) {
        
        switch (mLimitedType) {
        
            case LIMIT_SINGLE:
                
                if (statusSparseArray.valueAt(index) == TimeSliceView.STATUS_SELECTED) {
                    statusSparseArray.put(statusSparseArray.keyAt(index),
                            TimeSliceView.STATUS_VALID);
                }
                else {
                    statusSparseArray.put(statusSparseArray.keyAt(index),
                            TimeSliceView.STATUS_SELECTED);
                }
                break;
            
            case LIMIT_FREE:
                selectIfFree(index);
                break;
            
            case LIMIT_SPECIFIED_COUNT:
                selectIfSpecifiedTime(index);
                break;
            case LIMIT_RANDOM_COUNT:
                if (isOpenLimit2) {
                    selectWithNewRules(index);
                }
                else {
                    selectIfRandomTime(index);
                }
                break;
            
            case LIMIT_NONE:
                selectIfNormal(index);
                break;
            
            default:
                break;
        }
        
        calTimeSlice();
    }

    // 4.9.5新规则，默认选择4块，如果不满足允许最少选择2块，点击中间需要截断两段的判断会优先判断左边是否满足2块，满足则直接去掉右边的
    private void selectWithNewRules(int index) {
        if (statusSparseArray.valueAt(index) == TimeSliceView.STATUS_VALID) {
            int selCountNext = getSelectedCount(index + 1, true);
            int selCountPre = getSelectedCount(index - 1, false);
            if (selCountNext >= LIMIT_FREE || selCountPre >= LIMIT_FREE) {
                
                statusSparseArray.put(statusSparseArray.keyAt(index),
                        TimeSliceView.STATUS_SELECTED);
            }
            else {
                
                int nextIdle = getIdleCountNoBreak(index, true);
                int preIdle = getIdleCountNoBreak(index - 1, false);
                Logger.v(TAG, "pre : " + preIdle + ",next : " + nextIdle);
                if (nextIdle >= LIMIT_DEFAULT_COUNT) {
                    clearSelectedStatus();
                    
                    setStatusSelected(index, LIMIT_DEFAULT_COUNT, true);
                }
                else if (preIdle >= LIMIT_DEFAULT_COUNT) {
                    clearSelectedStatus();
                    
                    setStatusSelected(index, LIMIT_DEFAULT_COUNT, false);
                }
                else {
                    
                    int total = preIdle + nextIdle;
                    
                    if (total >= LIMIT_DEFAULT_COUNT) {
                        clearSelectedStatus();
                        setStatusSelected(index, nextIdle, true);
                        setStatusSelected(index - 1, LIMIT_DEFAULT_COUNT - nextIdle,
                                false);
                        
                    }
                    else if (total >= LIMIT_FREE) {
                        clearSelectedStatus();
                        setStatusSelected(index);
                    }
                }
            }
        }
        else {
            statusSparseArray.put(statusSparseArray.keyAt(index),
                    TimeSliceView.STATUS_VALID);
            
            int preSel = getSelectedCount(index - 1, false);
            if (preSel < LIMIT_FREE) {
                setStatusIdle(index - 1, false);
                
                // 如果后续所有选中块不满足长度，则去掉，满足则可以保留
                int nextSel = getSelectedCount(index + 1, true);
                if (nextSel < LIMIT_FREE) {
                    setStatusIdle(index + 1, true);
                }
            }
            else {
                // 去掉后续所有选中块
                setStatusIdle(index + 1, true);
            }
        }
    }

    private void selectIfFree(int index) {
        
        if (statusSparseArray.valueAt(index) == TimeSliceView.STATUS_VALID
                || statusSparseArray.valueAt(index) == TimeSliceView.STATUS_OLD_LESSON) {
            
            if (isNextIdleEnough(index, LIMIT_FREE)) {

                clearSelectedStatus();
                
                setStatusSelected(index, LIMIT_FREE, true);
            }
            else if (isPreIdleEnough(index, LIMIT_FREE)) {
                clearSelectedStatus();
                setStatusSelected(index, LIMIT_FREE, false);
            }
        }
        else {
            
            if (getSelectedCount(index, true) == LIMIT_FREE) {
                mSelectTimeMap.removeTime();
                setStatusIdle(index, true);
            }
            else if (getSelectedCount(index, false) == LIMIT_FREE) {
                mSelectTimeMap.removeTime();
                setStatusIdle(index, false);
            }
            else {
                Logger.w(TAG, "less selected item when free");
            }
        }
    }
    
    private void selectIfSpecifiedTime(int index) {
        if (statusSparseArray.valueAt(index) == TimeSliceView.STATUS_VALID
                || statusSparseArray.valueAt(index) == TimeSliceView.STATUS_OLD_LESSON
                || statusSparseArray.valueAt(index) == TimeSliceView.STATUS_STUDENT_LESSON
                || statusSparseArray.valueAt(index) == TimeSliceView.STATUS_TEACHER_LESSON) {
            
            if (isNextIdleEnough(index, mLimitedTime)) {

                clearSelectedStatus();
                
                setStatusSelected(index, mLimitedTime, true);
            }
            else if (isPreIdleEnough(index, mLimitedTime)) {

                clearSelectedStatus();
                setStatusSelected(index, mLimitedTime, false);
            }
            else {
                // 处理夹在中间的情况
                dealIdleMiddle(index, mLimitedTime);
            }
        }
        else {
            if (getSelectedCount(index) >= mLimitedTime) {
                mSelectTimeMap.removeTime();
                setStatusIdle(index);
            }
            else {
                Logger.w(TAG, "less selected item when match");
            }
        }
    }
    
    private void selectIfRandomTime(int index) {
        if (statusSparseArray.valueAt(index) == TimeSliceView.STATUS_VALID) {
            int selCountNext = getSelectedCount(index + 1, true);
            int selCountPre = getSelectedCount(index - 1, false);
            if (selCountNext >= LIMIT_DEFAULT_COUNT || selCountPre >= LIMIT_DEFAULT_COUNT) {
                
                statusSparseArray.put(statusSparseArray.keyAt(index),
                        TimeSliceView.STATUS_SELECTED);
            }
            else {
                
                if (isNextIdleEnough(index, LIMIT_DEFAULT_COUNT)) {
                    clearSelectedStatus();
                    
                    setStatusSelected(index, LIMIT_DEFAULT_COUNT, true);
                }
                else if (isPreIdleEnough(index, LIMIT_DEFAULT_COUNT)) {
                    clearSelectedStatus();
                    
                    setStatusSelected(index, LIMIT_DEFAULT_COUNT, false);
                }
                else {
                    dealIdleMiddle(index, LIMIT_DEFAULT_COUNT);
                }
            }
        }
        else {
            statusSparseArray.put(statusSparseArray.keyAt(index),
                    TimeSliceView.STATUS_VALID);
            
            int preSel = getSelectedCount(index - 1, false);
            if (preSel < LIMIT_DEFAULT_COUNT) {
                setStatusIdle(index - 1, false);
                
                // 如果后续所有选中块不满足长度，则去掉，满足则可以保留
                int nextSel = getSelectedCount(index + 1, true);
                if (nextSel < LIMIT_DEFAULT_COUNT) {
                    setStatusIdle(index + 1, true);
                }
            }
            else {
                // 去掉后续所有选中块
                setStatusIdle(index + 1, true);
            }
        }
    }
    
    private void selectIfNormal(int index) {
        if (statusSparseArray.valueAt(index) == TimeSliceView.STATUS_VALID) {
            int selCountNext = getSelectedCount(index + 1, true);
            int selCountPre = getSelectedCount(index - 1, false);
            if (selCountNext >= LIMIT_DEFAULT_COUNT || selCountPre >= LIMIT_DEFAULT_COUNT) {
                
                statusSparseArray.put(statusSparseArray.keyAt(index),
                        TimeSliceView.STATUS_SELECTED);
            }
            else {
                
                if (isNextIdleEnough(index, LIMIT_DEFAULT_COUNT)) {
                    setStatusSelected(index, LIMIT_DEFAULT_COUNT, true);
                }
                else if (isPreIdleEnough(index, LIMIT_DEFAULT_COUNT)) {
                    setStatusSelected(index, LIMIT_DEFAULT_COUNT, false);
                }
                else {
                    // 处理夹在中间的情况
                    dealIdleMiddle(index, LIMIT_DEFAULT_COUNT);
                }
            }
        }
        else {
            statusSparseArray.put(statusSparseArray.keyAt(index),
                    TimeSliceView.STATUS_VALID);
            int nextSel = getSelectedCount(index + 1, true);
            if (nextSel < LIMIT_DEFAULT_COUNT) {
                setStatusIdle(index + 1, true);
            }
            int preSel = getSelectedCount(index - 1, false);
            if (preSel < LIMIT_DEFAULT_COUNT) {
                setStatusIdle(index - 1, false);
            }
        }
    }
    
    private boolean isNextIdleEnough(int index, int count) {
        
        int nextIdle;
        if (isTeacherNeedBreak) {
            count += 1;
            int preIdle;
            if (index > 0) {
                preIdle = getIdleCountForBreak(index - 1, false);
            }
            else {
                preIdle = 1;
            }
            nextIdle = getIdleCountForBreak(index, true);
             Logger.v(TAG, "isNextIdleEnough  nextIdle = " + nextIdle +
             ",preIdle ="
             + preIdle + ",limit = " + count);
            
            if (preIdle > 0 && nextIdle >= count) {
                
                return true;
            }
        }
        else {
            nextIdle = getIdleCountNoBreak(index, true);
            if (nextIdle >= count) {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean isPreIdleEnough(int index, int count) {
        
        int preIdle;
        if (isTeacherNeedBreak) {
            count += 1;
            
            int nextIdle;
            if (index < sSliceCount - 1) {
                
                nextIdle = getIdleCountForBreak(index + 1, true);
            }
            else {
                nextIdle = 1;
            }
            preIdle = getIdleCountForBreak(index, false);
             Logger.v(TAG, "isPreIdleEnough  nextIdle = " + nextIdle +
             ",preIdle =" + preIdle
             + ",limit = " + count);
            if (preIdle >= count && nextIdle > 0) {
                
                return true;
            }
        }
        else {
            preIdle = getIdleCountNoBreak(index, false);
            if (preIdle >= count) {
                return true;
            }
        }
        
        return false;
    }
    
    private void dealIdleMiddle(int position, int count) {
        
        if (position == 0 || position == sSliceCount - 1) {
            return;
        }
        int preIdle;
        int nextIdle;
        
        if (isTeacherNeedBreak) {
            int limit = count + 2;
            nextIdle = getIdleCountForBreak(position, true);
            preIdle = getIdleCountForBreak(position - 1, false);
            Logger.v(TAG, "middle pre : " + preIdle + ",next : " + nextIdle + ",break : "
                    + isTeacherNeedBreak);
            if ((nextIdle + preIdle) >= limit) {

                clearSelectedStatus();
                
                int next = nextIdle - 1;
                setStatusSelected(position, next, true);
                setStatusSelected(position - 1, count - next, false);
            }
        }
        else {
            nextIdle = getIdleCountNoBreak(position, true);
            preIdle = getIdleCountNoBreak(position - 1, false);
            Logger.v(TAG, "middle pre : " + preIdle + ",next : " + nextIdle + ",break : "
                    + isTeacherNeedBreak);
            if ((nextIdle + preIdle) >= count) {
                clearSelectedStatus();

                setStatusSelected(position, nextIdle, true);
                setStatusSelected(position - 1, count - nextIdle, false);
                
            }
        }
    }
    
    private void calTimeSlice() {
        mSelectTimeMap.removeTime(mSelectedDateKey);
        int index = 0;
        while (index < sSliceCount) {
            if (statusSparseArray.valueAt(index) == TimeSliceView.STATUS_SELECTED) {
                int start = index;
                int end = index;
                
                for (int i = index + 1; i < sSliceCount; i++) {
                    if (statusSparseArray.valueAt(i) == TimeSliceView.STATUS_SELECTED) {
                        end = i;
                    }
                    else {
                        break;
                    }
                }
                
                index = end;
                
                mSelectTimeMap.addTime(SelectTimeUtils.getTimeSliceMulti(
                        statusSparseArray.keyAt(start), statusSparseArray.keyAt(end),
                        mCalendar.getTime()));
            }
            
            index++;
        }
    }
    
    // 根据坐标计算相邻的选中数量
    private int getSelectedCount(int position) {
        int count = 0;
        
        for (int i = position; i < sSliceCount; i++) {
            if (statusSparseArray.valueAt(i) == TimeSliceView.STATUS_SELECTED) {
                count++;
            }
            else {
                break;
            }
        }
        
        for (int i = position - 1; i >= 0; i--) {
            if (statusSparseArray.valueAt(i) == TimeSliceView.STATUS_SELECTED) {
                count++;
            }
            else {
                break;
            }
        }
        
        return count;
    }
    
    // 根据坐标计算后面或者前面的选中数量
    private int getSelectedCount(int start, boolean isNext) {
        int count = 0;
        
        if (isNext) {
            for (int i = start; i < sSliceCount; i++) {
                if (statusSparseArray.valueAt(i) == TimeSliceView.STATUS_SELECTED) {
                    count++;
                }
                else {
                    break;
                }
            }
        }
        else {
            for (int i = start; i >= 0; i--) {
                if (statusSparseArray.valueAt(i) == TimeSliceView.STATUS_SELECTED) {
                    count++;
                }
                else {
                    break;
                }
            }
        }
        
        return count;
    }
    
    // 根据坐标计算后面或者前面的可选数量(不考虑休息间隔)
    private int getIdleCountNoBreak(int start, boolean isNext) {
        int count = 0;
        
        if (isNext) {
            for (int i = start; i < sSliceCount; i++) {
                if (statusSparseArray.valueAt(i) == TimeSliceView.STATUS_VALID
                        || statusSparseArray.valueAt(i) == TimeSliceView.STATUS_SELECTED
                        || statusSparseArray.valueAt(i) == TimeSliceView.STATUS_OLD_LESSON
                        || statusSparseArray.valueAt(i) == TimeSliceView.STATUS_TEACHER_LESSON
                        || statusSparseArray.valueAt(i) == TimeSliceView.STATUS_STUDENT_LESSON) {
                    count++;
                }
                else {
                    break;
                }
            }
        }
        else {
            for (int i = start; i >= 0; i--) {
                if (statusSparseArray.valueAt(i) == TimeSliceView.STATUS_VALID
                        || statusSparseArray.valueAt(i) == TimeSliceView.STATUS_SELECTED
                        || statusSparseArray.valueAt(i) == TimeSliceView.STATUS_OLD_LESSON
                        || statusSparseArray.valueAt(i) == TimeSliceView.STATUS_TEACHER_LESSON
                        || statusSparseArray.valueAt(i) == TimeSliceView.STATUS_STUDENT_LESSON) {
                    count++;
                }
                else {
                    break;
                }
            }
        }
        
        return count;
    }
    
    // 根据坐标计算后面或者前面的可选数量(考虑休息间隔)
    private int getIdleCountForBreak(int start, boolean isNext) {
        int count = 0;
        
        if (isNext) {
            for (int i = start; i < sSliceCount; i++) {
                if (statusSparseArray.valueAt(i) == TimeSliceView.STATUS_VALID
                        || statusSparseArray.valueAt(i) == TimeSliceView.STATUS_SELECTED
                        || statusSparseArray.valueAt(i) == TimeSliceView.STATUS_OLD_LESSON
                        || statusSparseArray.valueAt(i) == TimeSliceView.STATUS_STUDENT_LESSON
                        || statusSparseArray.valueAt(i) == TimeSliceView.STATUS_TEACHER_LESSON) {
                    count++;
                    if (i == sSliceCount - 1) {
                        count++;
                        break;
                    }
                }
                else if (statusSparseArray.valueAt(i) == TimeSliceView.STATUS_INVALID) {
                    count++;
                    break;
                }
                else {
                    break;
                }
            }
        }
        else {
            for (int i = start; i >= 0; i--) {
                if (statusSparseArray.valueAt(i) == TimeSliceView.STATUS_VALID
                        || statusSparseArray.valueAt(i) == TimeSliceView.STATUS_SELECTED
                        || statusSparseArray.valueAt(i) == TimeSliceView.STATUS_OLD_LESSON
                        || statusSparseArray.valueAt(i) == TimeSliceView.STATUS_STUDENT_LESSON
                        || statusSparseArray.valueAt(i) == TimeSliceView.STATUS_TEACHER_LESSON) {
                    count++;
                    if (i == 0) {
                        count++;
                        break;
                    }
                }
                else if (statusSparseArray.valueAt(i) == TimeSliceView.STATUS_INVALID ) {
                    count++;
                    break;
                }
                else {
                    break;
                }
            }
        }
        
        return count;
    }

    // 将指定坐标的前和后可选全部设置为已选
    private void setStatusSelected(int position) {
        for (int i = position; i < sSliceCount; i++) {
            if (statusSparseArray.valueAt(i) == TimeSliceView.STATUS_VALID) {
                statusSparseArray.put(statusSparseArray.keyAt(i),
                        TimeSliceView.STATUS_SELECTED);
            }
            else {
                break;
            }
        }
        
        for (int i = position - 1; i >= 0; i--) {
            if (statusSparseArray.valueAt(i) == TimeSliceView.STATUS_VALID) {
                statusSparseArray.put(statusSparseArray.keyAt(i),
                        TimeSliceView.STATUS_SELECTED);
            }
            else {
                break;
            }
        }
    }

    // 将指定坐标的前或者后（isNext）指定数量的可选全部设置为已选
    private void setStatusSelected(int start, int count, boolean isNext) {
        if (isNext) {
            int end = start + count;
            for (int i = start; i < end; i++) {
                statusSparseArray.put(statusSparseArray.keyAt(i),
                        TimeSliceView.STATUS_SELECTED);
            }
        }
        else {
            int end = start - count;
            for (int i = start; i > end; i--) {
                statusSparseArray.put(statusSparseArray.keyAt(i),
                        TimeSliceView.STATUS_SELECTED);
            }
        }
    }

    // 将指定坐标的前和后的已选全部设置为可选
    private void setStatusIdle(int position) {
        for (int i = position; i < sSliceCount; i++) {
            if (statusSparseArray.valueAt(i) == TimeSliceView.STATUS_SELECTED) {
                statusSparseArray.put(statusSparseArray.keyAt(i),
                        TimeSliceView.STATUS_VALID);
            }
            else {
                break;
            }
        }
        
        for (int i = position - 1; i >= 0; i--) {
            if (statusSparseArray.valueAt(i) == TimeSliceView.STATUS_SELECTED) {
                statusSparseArray.put(statusSparseArray.keyAt(i),
                        TimeSliceView.STATUS_VALID);
            }
            else {
                break;
            }
        }
    }

    // 将指定坐标的前或者后（isNext）指定数量的已选全部设置为可选
    private void setStatusIdle(int start, boolean isNext) {
        if (isNext) {
            for (int i = start; i < sSliceCount; i++) {
                if (statusSparseArray.valueAt(i) == TimeSliceView.STATUS_SELECTED) {
                    statusSparseArray.put(statusSparseArray.keyAt(i),
                            TimeSliceView.STATUS_VALID);
                }
                else {
                    break;
                    
                }
            }
        }
        else {
            for (int i = start; i >= 0; i--) {
                if (statusSparseArray.valueAt(i) == TimeSliceView.STATUS_SELECTED) {
                    statusSparseArray.put(statusSparseArray.keyAt(i),
                            TimeSliceView.STATUS_VALID);
                }
                else {
                    break;
                    
                }
            }
        }
    }
    
    private void clearSelectedStatus() {
        if (mSelectTimeMap.getTimes().isEmpty()) {
            return;
        }
        mSelectTimeMap.removeTime();
        for (int i = 0; i < sSliceCount; i++) {
            if (statusSparseArray.valueAt(i) == TimeSliceView.STATUS_SELECTED) {
                statusSparseArray.put(statusSparseArray.keyAt(i),
                        TimeSliceView.STATUS_VALID);
            }
        }
    }
    
    public boolean canBeClick(final int index) {
        int status = statusSparseArray.valueAt(index);
        Logger.v(TAG, "canBeClick index=" + index + ",status=" + status + ",break="
                + isTeacherNeedBreak);
        
        if (status == TimeSliceView.STATUS_VALID
                || status == TimeSliceView.STATUS_SELECTED
                || status == TimeSliceView.STATUS_OLD_LESSON) {
            if (isTeacherNeedBreak) {
                int pre = index - 1;
                if (pre >= 0) {
                    status = statusSparseArray.valueAt(pre);
                    if (status == TimeSliceView.STATUS_TEACHER_LESSON) {
                        return false;
                    }
                }
                int next = index + 1;
                if (next < sSliceCount) {
                    status = statusSparseArray.valueAt(next);
                    if (status == TimeSliceView.STATUS_TEACHER_LESSON) {
                        return false;
                    }
                }
                
                return true;
            }
            else {
                return true;
            }
        }
        
        return false;
    }

    /** 全选 */
    public void selectAll() {
        if (mSceneType == SelectTimeUtils.SCENE_TYPE_SET_TEACHER_TIME) {
            SelectTimeUtils.fillTimeWithStatus(statusSparseArray, TimeSliceView.STATUS_SELECTED);
            calTimeSlice();
        }
    }

    /** 取消全选 */
    public void clearAll(){
        if (mSceneType == SelectTimeUtils.SCENE_TYPE_SET_TEACHER_TIME) {
            SelectTimeUtils.fillTimeWithStatus(statusSparseArray, TimeSliceView.STATUS_VALID);
            calTimeSlice();
        }
    }
}

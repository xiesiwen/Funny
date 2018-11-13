package com.qingqing.project.offline.order.v3;

import android.support.annotation.NonNull;
import android.util.SparseIntArray;

import com.qingqing.api.proto.v1.Time;
import com.qingqing.api.proto.v1.order.Order;
import com.qingqing.base.log.Logger;
import com.qingqing.base.time.NetworkTime;
import com.qingqing.project.offline.seltime.SelectTimeHelper;
import com.qingqing.project.offline.seltime.SelectTimeUtils;
import com.qingqing.project.offline.seltime.TimeSlice;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 管理时间块状态
 *
 * Created by tanwei on 2017/8/24.
 */

class TimeBlockStatusManager {

    // 互斥状态
    static final int STATUS_INIT = 0;// 可选
    static final int STATUS_OTHER_SELECTED = 1;// 已选
    static final int STATUS_CURRENT_SELECTED = 2;// 当前选中
    // 叠加状态，之间也互斥
    static final int STATUS_MASK_TEACHER_BUSY = 0x10000;// 老师忙（用于家长端表示老师有课）
    static final int STATUS_MASK_TEACHER_OCCUPIED = 0x20000;// 老师有课（用于老师端调课表示老师有课）
    static final int STATUS_MASK_STUDENT_OCCUPIED = 0x40000;// 学生有课（用于老师端调课表示学生有课）

    static final int STATUS_FILTER = 0x00ffff;// 按位与运算后去掉叠加状态
    static final int STATUS_FILTER_MASK = 0xff0000;// 按位与运算后去掉互斥状态

    static final int INVALID_BLOCK = -100;

    private List<TimeSlice> mSelectedTimeList;// 其他选中的时间
    
    private SparseIntArray mStatusMap;// 标记每个block状态
    
    private long mSelectedDateTime;
    
    private boolean intervalHalfHour, lock;// 精确到30分钟;是否锁定了精确到30分
    
    private int mBlockLength;// 时间块长度
    
    private int[] mDisplayBlockArray;// 展示的block数组

    private int selectedBlock = INVALID_BLOCK;

    private IIntervalChangedListener mListener;

    TimeBlockStatusManager(int blockLength) {
        mStatusMap = new SparseIntArray();
        mBlockLength = blockLength;

        mSelectedTimeList = new ArrayList<>();
    }

    void setIntervalChangedListener(IIntervalChangedListener listener) {
        mListener = listener;
    }
    
    /**
     * 刷新不同的已选时间
     *
     * @param list
     *            time list
     */
    void refreshSelectedTime(List<TimeSlice> list) {
        refreshSelectedTime(list, false);
    }

    /**
     * 刷新不同的已选时间
     *
     * @param list
     *            time list
     */
    void refreshSelectedTime(List<TimeSlice> list, boolean forceRefresh) {
        if (list != null) {
            if (mSelectedTimeList.size() > 0) {
                mSelectedTimeList.clear();
            }
            mSelectedTimeList.addAll(list);
            
            if (forceRefresh) {
                Logger.v("refreshSelectedTime force");
                // clear selected status
                int size = mStatusMap.size();
                for (int i = 0; i < size; i++) {
                    int status = mStatusMap.valueAt(i);
                    
                    if ((status & STATUS_OTHER_SELECTED) > 0) {
                        mStatusMap.put(mStatusMap.keyAt(i),
                                STATUS_INIT + (status & STATUS_FILTER_MASK));
                    }
                }

                // reset selected status
                if (mSelectedTimeList.size() > 0) {
                    for (TimeSlice time : mSelectedTimeList) {
                        if (SelectTimeUtils.isDateEquals(time.getStartDate(),
                                mSelectedDateTime)) {
                            int end = time.getEnd();
                            for (int i = time.getStart(); i <= end; i++) {
                                int status = mStatusMap.get(i);
                                mStatusMap.put(i, STATUS_OTHER_SELECTED
                                        + (status & STATUS_FILTER_MASK));
                            }
                        }
                    }
                }

                // check interval lock
                checkLock();
            }
        }
    }
    
    /**
     * 切换到不同日期
     *
     * @param time
     *            date
     */
    void refreshDate(long time) {

        selectedBlock = INVALID_BLOCK;
        intervalHalfHour = false;

        mSelectedDateTime = time;
        setupBlockStatus(true);
    }

    /**
     * 切换精确30分
     *
     */
    void refreshIntervalByHalfHour(){
        this.intervalHalfHour = !intervalHalfHour;
        if (mListener != null) {
            mListener.intervalChanged(intervalHalfHour, lock);
        }

        if (mSelectedDateTime > 0) {
            setupBlockStatus(false);
        }
    }

    // 初始化时间状态：包括计算起始时间、设置当前选中、设置其他已选
    private void setupBlockStatus(boolean clear) {
        
        int startBlock = SelectTimeUtils.getNextAvailableBlock(
                new Date(mSelectedDateTime), NetworkTime.currentTimeMillis());
        mDisplayBlockArray = getBlockArray(startBlock, mBlockLength, intervalHalfHour);
        
        if (clear) {
            mStatusMap.clear();
            for (int i = startBlock; i <= SelectTimeHelper.sSliceEndBlock; i++) {
                mStatusMap.append(i, STATUS_INIT);
            }
        }
        
        if (mSelectedTimeList.size() > 0) {
            for (TimeSlice time : mSelectedTimeList) {
                if (SelectTimeUtils.isDateEquals(time.getStartDate(),
                        mSelectedDateTime)) {
                    int end = time.getEnd();
                    for (int i = time.getStart(); i <= end; i++) {
                        int status = mStatusMap.get(i);
                        mStatusMap.put(i,
                                STATUS_OTHER_SELECTED + (status & STATUS_FILTER_MASK));
                    }
                }
            }
        }
        
        // check interval lock
        checkLock();
    }

    /** 老师忙碌（家长端） */
    void refreshTeacherBusy(int[] blocks) {
        if (blocks != null) {
            for (int block : blocks) {
                int status = mStatusMap.get(block);
                if ((status & STATUS_MASK_TEACHER_BUSY) == 0) {
                    mStatusMap.put(block,
                            (status & STATUS_FILTER) + STATUS_MASK_TEACHER_BUSY);
                }
            }
        }
    }

    /** 调课时间占用（老师端） */
    void refreshOccupiedBlock(Order.TimeConflictInSeparatedBlocks[] array) {

        int start;
        int end;
        int temp;
        for (Order.TimeConflictInSeparatedBlocks blocks : array) {
            for (Order.TimeUsingInSeparatedBlocks time : blocks.timeUsing) {

                int size = time.status.length;
                int status;
                start = time.timeParam.startBlock;
                end = time.timeParam.endBlock;
                for (int i = 0; i < size; i++) {
                    status = time.status[i];
                    switch (status) {
                        // 老师有课，包括已支付、未支付、调课占用
                        case Time.TimeUsingStatus.teacher_has_course_time_using_status:
                        case Time.TimeUsingStatus.teacher_unpay_order_tu_status:
                        case Time.TimeUsingStatus.teacher_change_course_pending_tu_status:
                            // Logger.v(TAG, "teacher time from:" + start + "
                            // to:" + end
                            // + " @ " + time.getTimeParam().getDate());
                            for (int j = start; j <= end; j++) {
                                temp = mStatusMap.get(j);
                                if ((temp & STATUS_MASK_TEACHER_OCCUPIED) == 0) {
                                    mStatusMap.put(j, (temp & STATUS_FILTER)
                                            + STATUS_MASK_TEACHER_OCCUPIED);
                                }
                            }
                            break;
                        // 学生有课，包括已支付、未支付、调课占用
                        case Time.TimeUsingStatus.student_has_course_time_using_status:
                        case Time.TimeUsingStatus.student_unpay_order_tu_status:
                        case Time.TimeUsingStatus.student_change_course_pending_tu_status:
                            // Logger.v(TAG, "student time from:" + start + "
                            // to:" + end
                            // + " @ " + time.getTimeParam().getDate());
                            for (int j = start; j <= end; j++) {
                                temp = mStatusMap.get(j);
                                if ((temp & STATUS_FILTER_MASK) == 0) {
                                    mStatusMap.put(j, (temp & STATUS_FILTER)
                                            + STATUS_MASK_STUDENT_OCCUPIED);
                                }
                            }
                            break;

                    }
                }

            }
        }
    }

    public void setSelected(int block) {

        clearSelected();

        selectedBlock = block;
        mStatusMap.put(block,
                STATUS_CURRENT_SELECTED + (mStatusMap.get(block) & STATUS_FILTER_MASK));

        // check interval lock
        checkLock();
    }

    private void clearSelected() {
        if (selectedBlock > INVALID_BLOCK) {
            int status = mStatusMap.get(selectedBlock);
            mStatusMap.put(selectedBlock, STATUS_INIT + (status & STATUS_FILTER_MASK));
            selectedBlock = INVALID_BLOCK;
        }
    }

    // 根据当前选择的起始时间和其他课次的已选时间（包括起始和结束时间）判断是否锁定30分
    private void checkLock() {
        boolean lock = false;
        if (selectedBlock > INVALID_BLOCK && selectedBlock % 2 != 0) {
            lock = true;
        }

        if (!lock && mSelectedTimeList.size() > 0) {
            List<TimeSlice> temp = new ArrayList<>();
            for (TimeSlice time : mSelectedTimeList) {
                if (SelectTimeUtils.isDateEquals(time.getStartDate(),
                        mSelectedDateTime)) {
                    temp.add(time);
                }
            }

            if (temp.size() > 0) {
                lock = checkTimeStartOrEndAtHalfHour(temp);
            }
        }

        if (lock != this.lock) {
            this.lock = lock;
        }

        if (mListener != null) {
            mListener.intervalChanged(intervalHalfHour, this.lock);
        }
    }

    /**
     * 获取当前展示的block状态集合
     * 
     * @return block-status map
     */
    SparseIntArray getStatusMap() {
        SparseIntArray array = new SparseIntArray(mDisplayBlockArray.length);
        for (int i : mDisplayBlockArray) {
            array.append(i, mStatusMap.get(i));
        }
        
        return array;
    }

    public boolean isIntervalHalfHour() {
        return intervalHalfHour;
    }

    boolean isLocked() {
        return lock;
    }

    /**
     * 检查起始时间是否满足长度
     *
     * @param start
     *            start block
     * @return true 满足长度
     */
    boolean checkBlockEnough(int start) {
        boolean enough = true;
        for (int i = start; i < start + mBlockLength; i++) {
            int status = mStatusMap.get(i, STATUS_INIT);
            if ((status & STATUS_OTHER_SELECTED) > 0) {
                enough = false;
                break;
            }
        }
        
        return enough;
    }

    private static int[] getBlockArray(int start, int length, boolean intervalHalfHour) {
        int array[];
        int index = 0;
        int end = SelectTimeHelper.sSliceEndBlock - length + 1;
        int size;
        if (intervalHalfHour) {
            if ((size = end - start + 1) > 0) {
                array = new int[size];
                for (int i = start; i <= end; i++) {
                    array[index++] = i;
                }
            }
            else {
                array = new int[0];
            }
        }
        else {
            if (start % 2 != 0) {
                start += 1;
            }
            if (end % 2 != 0) {
                end -= 1;
            }
            if ((size = (end - start) / 2 + 1) > 0) {
                array = new int[size];
                
                for (int i = start; i <= end; i = i + 2) {
                    array[index++] = i;
                }
            }
            else {
                array = new int[0];
            }
        }
        
        return array;
    }

    /**
     * 判断时间起始和结束是否不在整点(改为不判断结束时间点)
     *
     * <p>注意起始block是时间块的起点，而结束block应该取时间块的终点</p>
     * <p>比如0作为起始block，应该视为8：00，而作为结束block应该视为8:30</p>
     *
     * @return 不在整点返回true，在整点返回false
     */
    private static boolean checkTimeStartOrEndAtHalfHour(@NonNull List<TimeSlice> list){
        for (TimeSlice time : list) {
            if ((time.getStart() & 1) != 0/* || (time.getEnd() & 1) == 0*/) {
                return true;
            }
        }

        return false;
    }

    interface IIntervalChangedListener {
        void intervalChanged(boolean halfHour, boolean locked);
    }
}

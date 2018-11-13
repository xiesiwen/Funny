package com.qingqing.project.offline.order.v3;

import android.os.Parcel;
import android.os.Parcelable;

import com.qingqing.project.offline.seltime.TimeSlice;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * 选时间参数
 *
 * Created by tanwei on 2017/8/8.
 */

public class SelectTimeParamsV3 implements Parcelable {
    
    private int count;
    
    private int blockLength;
    
    private String teacherId;

    private ArrayList<String> studentIds;
    
    private ArrayList<TimeSlice> timeList;

    private TimeSlice selectedTime;// 已选的时间（修改时间时会用到，包括修改周期）
    
    private boolean optional;// 是否选择任意时间

    private long selectDateTime;// 已选的日期（选择周期时间时会用到），由于没有起始和结束时间，优先级比selectedTime低

    private int specifiedWeek;// 对应Calendar的Week,无效值0

    private int datePosition;// 修改的时间index

    private String orderCourseId;// 调课使用

    private boolean isTimeLimited;// 6.1新增，是否限制时间
    
    private long limitTimeStart, limitTimeEnd;// 6.1新增，限制时间的起始、结束时间
    
    public SelectTimeParamsV3(int count, int length) {
        this(count, length, new ArrayList<TimeSlice>());
    }
    
    public SelectTimeParamsV3(int count, int length, ArrayList<TimeSlice> timeList) {
        this.count = count;
        this.blockLength = length;
        
        if (timeList == null) {
            this.timeList = new ArrayList<>();
        }
        else {
            this.timeList = timeList;
        }
    }
    
    protected SelectTimeParamsV3(Parcel in) {
        count = in.readInt();
        blockLength = in.readInt();
        teacherId = in.readString();

        studentIds = new ArrayList<>();
        in.readList(studentIds, String.class.getClassLoader());
        timeList = new ArrayList<>();
        in.readList(timeList, TimeSlice.class.getClassLoader());
        
        optional = in.readInt() == 1;
        selectDateTime = in.readLong();
        specifiedWeek = in.readInt();
        selectedTime = in.readParcelable(TimeSlice.class.getClassLoader());
        datePosition = in.readInt();

        orderCourseId = in.readString();

        isTimeLimited = in.readInt() == 1;
        limitTimeStart= in.readLong();
        limitTimeEnd= in.readLong();
    }
    
    public int getCount() {
        return count;
    }
    
    public void setCount(int count) {
        this.count = count;
    }
    
    public int getBlockLength() {
        return blockLength;
    }
    
    public void setBlockLength(int length) {
        this.blockLength = length;
    }
    
    public ArrayList<TimeSlice> getTimeList() {
        return timeList;
    }
    
    public void setTimeList(ArrayList<TimeSlice> timeList) {
        if (!this.timeList.isEmpty()) {
            this.timeList.clear();
        }
        this.timeList.addAll(timeList);
    }
    
    public boolean isOptional() {
        return optional;
    }
    
    public void setOptional(boolean optional) {
        this.optional = optional;
    }
    
    public String getTeacherId() {
        return teacherId;
    }
    
    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    public ArrayList<String> getStudentIds() {
        return studentIds;
    }

    public void setStudentIds(ArrayList<String> studentIds) {
        this.studentIds = studentIds;
    }

    public void setStudentId(String studentId) {
        if (studentIds == null) {
            studentIds = new ArrayList<>();
        }
        studentIds.add(studentId);
    }

    public long getSelectDateTime() {
        return selectDateTime;
    }

    public void setSelectDateTime(long selectDateTime) {
        this.selectDateTime = selectDateTime;
    }

    public int getSpecifiedWeek() {
        return specifiedWeek;
    }

    public void setSpecifiedWeek(int specifiedWeek) {
        this.specifiedWeek = specifiedWeek;
    }

    public TimeSlice getSelectedTime() {
        return selectedTime;
    }

    public void setSelectedTime(TimeSlice selectedTime) {
        this.selectedTime = selectedTime;
    }

    public int getDatePosition() {
        return datePosition;
    }

    public void setDatePosition(int datePosition) {
        this.datePosition = datePosition;
    }

    public String getOrderCourseId() {
        return orderCourseId;
    }

    public void setOrderCourseId(String orderCourseId) {
        this.orderCourseId = orderCourseId;
    }

    public boolean isTimeLimited() {
        return isTimeLimited;
    }

    public void setTimeLimited(boolean timeLimited) {
        isTimeLimited = timeLimited;
    }

    public long getLimitTimeStart() {
        return limitTimeStart;
    }

    public void setLimitTimeStart(long limitTimeStart) {
        this.limitTimeStart = limitTimeStart;
    }

    public long getLimitTimeEnd() {
        return limitTimeEnd;
    }

    public void setLimitTimeEnd(long limitTimeEnd) {
        this.limitTimeEnd = limitTimeEnd;
    }

    public static final Creator<SelectTimeParamsV3> CREATOR = new Creator<SelectTimeParamsV3>() {
        @Override
        public SelectTimeParamsV3 createFromParcel(Parcel in) {
            return new SelectTimeParamsV3(in);
        }
        
        @Override
        public SelectTimeParamsV3[] newArray(int size) {
            return new SelectTimeParamsV3[size];
        }
    };
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(count);
        dest.writeInt(blockLength);
        dest.writeString(teacherId);

        dest.writeList(studentIds);
        dest.writeList(timeList);

        dest.writeInt(optional ? 1 : 0);
        dest.writeLong(selectDateTime);
        dest.writeInt(specifiedWeek);
        dest.writeParcelable(selectedTime, flags);
        dest.writeInt(datePosition);
        dest.writeString(orderCourseId);
        
        dest.writeInt(isTimeLimited ? 1 : 0);
        dest.writeLong(limitTimeStart);
        dest.writeLong(limitTimeEnd);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("SelTimeParam:");
        sb.append("count=").append(count);
        sb.append(",blockLength=").append(blockLength);
        sb.append(",teacherId=").append(teacherId);
        sb.append(",selTimeSize=").append(timeList.size());
        sb.append(",optional=").append(optional);
        sb.append(",week=").append(specifiedWeek);
        return sb.toString();
    }

    public void clear(){
        timeList.clear();
        specifiedWeek = 0;
        selectDateTime = 0;
        selectedTime = null;
        datePosition = 0;
    }

    // 检查指定时间是否在限制跨度内
    public boolean checkTimeLimited(TimeSlice time) {
        
        if (isTimeLimited) {
            // 清除当前时间（否则下面的代码覆盖不到时、分、秒）
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(time.getStartDate());
            calendar.set(Calendar.HOUR, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            
            long timeInMillis = calendar.getTimeInMillis();
            
            return timeInMillis < limitTimeStart || timeInMillis > limitTimeEnd;
        }
        
        return false;
    }

    // 检查全部已选的时间是否在限制跨度内
    public int checkTimeLimited() {
        int count = 0;
        if (isTimeLimited) {
            for (TimeSlice timeSlice : timeList) {
                if (checkTimeLimited(timeSlice)) {
                    count++;
                }
            }
        }
        
        return count;
    }
}

package com.qingqing.project.offline.seltime;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

import com.qingqing.api.proto.v1.OrderCommonEnum;

/**
 * 选择时间需要的参数封装类
 *
 * Created by tanwei on 2015/11/29.
 */
public class SelectTimeParams implements Parcelable {
    
    public static final Creator<SelectTimeParams> CREATOR = new Creator<SelectTimeParams>() {
        @Override
        public SelectTimeParams createFromParcel(Parcel in) {
            return new SelectTimeParams(in);
        }
        
        @Override
        public SelectTimeParams[] newArray(int size) {
            return new SelectTimeParams[size];
        }
    };
    
    private ArrayList<String> studentIdList;// qq student id list 朋友团2.0  5.2.5添加
    
    private String qingqingTeacherId;// qq teacher id

    private Date date;
    
    private int mSceneType;// 场景类型
    
    // private boolean isFree;
    
    private List<TimeSlice> mSelectedTimeList;// 已选中的时间
    
    private List<TimeSlice> mOldTimeList;// 原有课程的时间
    
    private String orderCourseId;// 调课时需要原有课程id
    
    private int weekIndex;// 老师设置开课时间时可以设置星期

    private int courseType;// 课程类型  5.2.5添加

    public SelectTimeParams() {
        studentIdList = new ArrayList<>();
        mSelectedTimeList = new ArrayList<>();
        mOldTimeList = new ArrayList<>();
    }
    
    protected SelectTimeParams(Parcel parcel) {
        this();

        parcel.readList(studentIdList, getClass().getClassLoader());
        qingqingTeacherId = parcel.readString();
        long time = parcel.readLong();
        date = time == 0 ? null : new Date(time);
        mSceneType = parcel.readInt();
        // isFree = parcel.readInt() == 1;
        parcel.readList(mSelectedTimeList, TimeSlice.class.getClassLoader());
        parcel.readList(mOldTimeList, TimeSlice.class.getClassLoader());
        orderCourseId = parcel.readString();
        weekIndex = parcel.readInt();
        courseType = parcel.readInt();
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeList(studentIdList);
        parcel.writeString(qingqingTeacherId);
        parcel.writeLong(date == null ? 0 : date.getTime());
        parcel.writeInt(mSceneType);
        // parcel.writeInt(isFree ? 1 : 0);
        parcel.writeList(mSelectedTimeList);
        parcel.writeList(mOldTimeList);
        parcel.writeString(orderCourseId);
        parcel.writeInt(weekIndex);
        parcel.writeInt(courseType);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("studentIds:").append(studentIdList.size());
        sb.append(",teacherId:").append(qingqingTeacherId);
        sb.append(",type:").append(mSceneType);
        // sb.append(",free:").append(isFree);
        sb.append(",selSize:").append(mSelectedTimeList.size());
        sb.append(",oldSize:").append(mOldTimeList.size());
        sb.append(",week:").append(weekIndex);
        sb.append(",courseType:").append(courseType);
        return sb.toString();
    }

    public void setQingqingStudentId(String studentId) {
        studentIdList.add(studentId);
    }

    public ArrayList<String> getStudentIdList() {
        return studentIdList;
    }

    public void setStudentIdList(ArrayList<String> studentIdList) {
        if(studentIdList != null) {
            this.studentIdList.clear();
            this.studentIdList.addAll(studentIdList);
        }
    }

    public String getQingqingTeacherId() {
        return qingqingTeacherId;
    }
    
    public void setQingqingTeacherId(String teacherId) {
        this.qingqingTeacherId = teacherId;
    }
    
    public int getSceneType() {
        return mSceneType;
    }
    
    public void setSceneType(int sceneType) {
        this.mSceneType = sceneType;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public List<TimeSlice> getSelectedTimeList() {
        return mSelectedTimeList;
    }
    
    public void setSelectedTimeList(List<TimeSlice> selectedTimeList) {
        mSelectedTimeList.clear();
        if (selectedTimeList != null) {
            mSelectedTimeList.addAll(selectedTimeList);
        }
    }

    public void addSelectedTime(TimeSlice time) {
        mSelectedTimeList.clear();
        if(time != null) {
            mSelectedTimeList.add(time);
        }
    }

    public List<TimeSlice> getOldTimeList() {
        return mOldTimeList;
    }
    
    public void setOldTimeList(List<TimeSlice> oldTimeList) {
        mOldTimeList.clear();
        if (oldTimeList != null) {
            mOldTimeList.addAll(oldTimeList);
        }
    }

    public void addOldTime(TimeSlice time) {
        mOldTimeList.clear();
        if(time != null) {
            mOldTimeList.add(time);
        }
    }
    
    public String getOrderCourseId() {
        return orderCourseId;
    }
    
    public void setOrderCourseId(String orderCourseId) {
        this.orderCourseId = orderCourseId;
    }
    
    public int getWeekIndex() {
        return weekIndex;
    }
    
    public void setWeekIndex(int weekIndex) {
        this.weekIndex = weekIndex;
    }

    public int getCourseType() {
        return courseType;
    }

    public void setCourseType(int courseType) {
        this.courseType = courseType;
    }

    public boolean isGroupOrderV2(){
        switch (courseType) {
            case OrderCommonEnum.TeacherCoursePriceType.group_two_teacher_course_price_type:
            case OrderCommonEnum.TeacherCoursePriceType.group_three_teacher_course_price_type:
            case OrderCommonEnum.TeacherCoursePriceType.group_four_teacher_course_price_type:
            case OrderCommonEnum.TeacherCoursePriceType.group_five_teacher_course_price_type:
                return true;
        }

        return false;
    }
}

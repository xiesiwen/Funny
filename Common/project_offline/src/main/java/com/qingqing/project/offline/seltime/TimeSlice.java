package com.qingqing.project.offline.seltime;

import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 
 * 时间片段
 * 
 * @author tanwei
 * 
 */
public class TimeSlice implements Parcelable {
    
    private int start;
    
    private int end;
    
    private Date startDate;
    
    private Date endDate;
    
    public TimeSlice(int start, Date startDate) {
        this(start, start, startDate, SelectTimeUtils.addSlice(startDate));
    }
    
    public TimeSlice(int start, int end, Date startDate, Date endDate) {
        this.start = start;
        this.end = end;
        this.startDate = startDate;
        this.endDate = endDate;
    }
    
    /** 时间段的长度，单位：小时，如0.5小时，2.0小时 */
    public float getTimeInHour() {
        return (end - start + 1) * 0.5f;
    }
    
    public int getStart() {
        return start;
    }
    
    public void setStart(int start) {
        this.start = start;
    }
    
    public int getEnd() {
        return end;
    }
    
    public void setEnd(int end) {
        this.end = end;
    }
    
    public Date getStartDate() {
        return startDate;
    }
    
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
    
    public Date getEndDate() {
        return endDate;
    }
    
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("TimeSlice:");
        sb.append("start：");
        sb.append(start);
        sb.append("，end：");
        sb.append(end);
        sb.append("，startDate：");
        sb.append(SelectTimeUtils.formatDateForLog(startDate));
        sb.append("，endDate：");
        sb.append(SelectTimeUtils.formatDateForLog(endDate));
        sb.append("，hour：");
        sb.append(getTimeInHour());
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof TimeSlice) {
            if (SelectTimeUtils.isDateEquals(startDate, ((TimeSlice) o).startDate)
                    && (start == ((TimeSlice) o).getStart())
                    && (end == ((TimeSlice) o).getEnd())) {
                return true;
            }
        }

        return false;
    }

    public static final Creator<TimeSlice> CREATOR = new Creator<TimeSlice>() {
        
        @Override
        public TimeSlice[] newArray(int arg0) {
            return new TimeSlice[arg0];
        }
        
        @Override
        public TimeSlice createFromParcel(Parcel arg0) {
            TimeSlice time = new TimeSlice(arg0.readInt(), arg0.readInt(), new Date(
                    arg0.readLong()), new Date(arg0.readLong()));
            return time;
        }
    };
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel arg0, int arg1) {
        
        arg0.writeInt(start);
        arg0.writeInt(end);
        arg0.writeLong(startDate.getTime());
        arg0.writeLong(endDate.getTime());
    }
    
}

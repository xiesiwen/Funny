package com.qingqing.project.offline.order;

import android.content.Context;
import android.databinding.Bindable;

import com.qingqing.api.proto.v1.course.OrderCourse;
import com.qingqing.project.offline.seltime.SelectTimeUtils;
import com.qingqing.project.offline.seltime.TimeSlice;
import com.qingqing.qingqingbase.ui.BaseUIModel;
import com.qingqing.project.offline.BR;


/**
 * Created by wangxiaxin on 2016/8/29.
 */
public class OrderCourseTimeUIModel extends BaseUIModel {
    
    private OrderCourse.OrderCourseInfoForOrderInfoDetail detail;
    
    public OrderCourseTimeUIModel(Context ctx) {
        super(ctx);
    }
    
    public void setCourseTime(OrderCourse.OrderCourseInfoForOrderInfoDetail detail) {
        this.detail = detail;
        notifyPropertyChanged(BR.timeShowString);
    }
    
    @Bindable
    public String getTimeShowString() {
        
        if (detail == null)
            return "";
        
        TimeSlice slice = SelectTimeUtils.parseToTimeSlice(detail.timeParam);
        return SelectTimeUtils.getContentByTimeSlice(slice);
    }
}

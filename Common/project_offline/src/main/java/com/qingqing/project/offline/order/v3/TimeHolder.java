package com.qingqing.project.offline.order.v3;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.qingqing.project.offline.R;

/**
 * 展示时间的ViewHolder
 *
 * Created by tanwei on 2017/8/8.
 */

public class TimeHolder extends RecyclerView.ViewHolder {
    
    protected TextView tvTitle, tvDate;
    
    protected ImageView imgArrow;
    
    protected TimeHolder(View itemView) {
        super(itemView);
        
        tvTitle = (TextView) itemView
                .findViewById(R.id.item_course_time_title);
        tvDate = (TextView) itemView.findViewById(R.id.item_course_time_date);
        imgArrow = (ImageView) itemView
                .findViewById(R.id.item_course_time_arrow);
    }
}

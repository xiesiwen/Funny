package com.qingqing.project.offline.order.v3;

import java.text.SimpleDateFormat;
import java.util.Locale;

import com.qingqing.project.offline.R;
import com.qingqing.project.offline.seltime.SelectTimeUtils;
import com.qingqing.project.offline.seltime.TimeSlice;
import com.qingqing.project.offline.seltime.WeekDay;

import android.app.Activity;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 *
 * 展示周期时间的Adapter
 *
 * Created by tanwei on 2017/8/8.
 */

public class CircleTimeAdapter extends RecyclerView.Adapter<TimeHolder> {
    
    private SparseArray<TimeSlice> mTimeList;
    
    private int valueColorNormal, valueColorUncompleted;
    
    private LayoutInflater mInflater;
    
    private Resources resources;
    
    private SimpleDateFormat hm, md, ymd;

    private ItemClickListener itemClickListener;
    
    public CircleTimeAdapter(Activity activity, SparseArray<TimeSlice> timeList) {
        mInflater = activity.getLayoutInflater();
        this.mTimeList = timeList;
        resources = activity.getResources();
        
        valueColorNormal = resources.getColor(R.color.black_light);
        valueColorUncompleted = resources.getColor(R.color.accent_orange);

        ymd = new SimpleDateFormat(SelectTimeUtils.FORMAT_YEAR_MONTH_DAY_2, Locale.CHINA);
        md = new SimpleDateFormat(SelectTimeUtils.FORMAT_MONTH_DAY, Locale.CHINA);
        hm = new SimpleDateFormat(SelectTimeUtils.FORMAT_HOUR_MINUTE, Locale.CHINA);
    }
    
    @Override
    public TimeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final TimeHolder timeHolder = new TimeHolder(
                mInflater.inflate(R.layout.item_course_time, parent, false));

        if (itemClickListener != null) {
            timeHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemClickListener.onItemClick(timeHolder,
                            timeHolder.getAdapterPosition());
                }
            });
        }

        return timeHolder;
    }
    
    @Override
    public void onBindViewHolder(TimeHolder holder, int position) {
        
        holder.tvTitle.setText(resources.getString(R.string.text_sel_time_week_formatter,
                WeekDay.getWeekStringSimple(mTimeList.keyAt(position))));
        
        if (mTimeList.valueAt(position) != null) {
            holder.tvDate.setText(formatDate(mTimeList.valueAt(position)));
            holder.tvDate.setTextColor(valueColorNormal);
        }
        else {
            holder.tvDate.setText(R.string.text_sel_time_circle_select);
            holder.tvDate.setTextColor(valueColorUncompleted);
        }
    }
    
    @Override
    public int getItemCount() {
        return mTimeList != null ? mTimeList.size() : 0;
    }
    
    private String formatDate(TimeSlice time) {
        
        String start = hm.format(time.getStartDate());
        String end = hm.format(time.getEndDate());
        String date;
        if(SelectTimeUtils.isTimeInCurrentYear(time.getStartDate())) {
            date = md.format(time.getStartDate());
        }else{
            date = ymd.format(time.getStartDate());
        }
        
        return resources.getString(R.string.text_sel_time_circle_formatter, start, end,
                date);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }
    
    public interface ItemClickListener {
        void onItemClick(RecyclerView.ViewHolder vh, int position);
    }
}

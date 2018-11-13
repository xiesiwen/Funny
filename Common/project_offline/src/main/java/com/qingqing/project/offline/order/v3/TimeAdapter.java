package com.qingqing.project.offline.order.v3;

import android.app.Activity;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qingqing.project.offline.R;
import com.qingqing.project.offline.seltime.SelectTimeUtils;
import com.qingqing.project.offline.seltime.TimeSlice;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * 展示时间的Adapter
 *
 * Created by tanwei on 2017/8/8.
 */

public class TimeAdapter extends RecyclerView.Adapter<TimeHolder> {

    private List<TimeSlice> mTimeList;

    private LayoutInflater mInflater;

    private Resources resources;

    private CircleTimeAdapter.ItemClickListener itemClickListener;
    
    private SelectTimeParamsV3 mParams;

    public TimeAdapter(Activity activity, ArrayList<TimeSlice> timeList){
        this.mTimeList = timeList;
        mInflater = activity.getLayoutInflater();
        resources = activity.getResources();
    }
    
    public void setParams(SelectTimeParamsV3 params) {
        mParams = params;
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
        holder.tvTitle.setText(
                resources.getString(R.string.text_optional_time_ordinal, position + 1));
        holder.tvDate
                .setText(SelectTimeUtils.getContentByTimeSlice(mTimeList.get(position)));
        
        if (mParams != null && mParams.isTimeLimited()) {
            int color = mParams.checkTimeLimited(mTimeList.get(position))
                    ? R.color.accent_red : R.color.black_light;
            holder.tvDate.setTextColor(resources.getColor(color));
        }
    }

    @Override
    public int getItemCount() {
        return mTimeList != null ? mTimeList.size() : 0;
    }

    public void setItemClickListener(CircleTimeAdapter.ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }
}

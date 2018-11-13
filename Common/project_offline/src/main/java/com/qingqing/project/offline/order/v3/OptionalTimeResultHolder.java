package com.qingqing.project.offline.order.v3;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.qingqing.base.dialog.CompDialog;
import com.qingqing.base.view.OnItemClickListener;
import com.qingqing.base.view.recycler.VerticalDividerDecoration;
import com.qingqing.project.offline.R;
import com.qingqing.project.offline.seltime.SelectTimeUtils;
import com.qingqing.project.offline.seltime.TimeSlice;
import com.qingqing.project.offline.view.order.MaxHeightRecyclerView;

import java.util.List;

/**
 * 自定义选择时间展示选择结果的view holder
 *
 * Created by tanwei on 2017/8/22.
 */

public class OptionalTimeResultHolder {

    private Activity activity;

    private View layout;

    private MaxHeightRecyclerView recyclerView;
    
    private List<TimeSlice> resultList;
    
    private Adapter mAdapter;
    
    private ITimeChangedListener mListener;

    private CompDialog clearConfirmDialog;
    
    protected OptionalTimeResultHolder(Activity activity, View layout, List<TimeSlice> list) {
        this.activity = activity;
        this.layout = layout;
        resultList = list;
        
        initView();
    }
    
    public void refresh() {
        if (!resultList.isEmpty()) {
            layout.setVisibility(View.VISIBLE);
            mAdapter.notifyDataSetChanged();
            recyclerView.invalidate();
        }
        else {
            layout.setVisibility(View.GONE);
        }
    }

    public boolean isShowing() {
        return layout.getVisibility() == View.VISIBLE;
    }

    public void setTimeChangedListener(ITimeChangedListener listener) {
        mListener = listener;
    }
    
    private void initView() {
        
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                if (id == R.id.layout_optional_time_empty_layout) {
                    layout.setVisibility(View.GONE);
                }
                else if (id == R.id.layout_optional_time_clear) {
                    showClearConfirmDialog();
                }
            }
        };
        View emptyLayout = layout.findViewById(R.id.layout_optional_time_empty_layout);
        emptyLayout.setOnClickListener(listener);

        layout.findViewById(R.id.layout_optional_time_clear).setOnClickListener(listener);
        
        recyclerView = (MaxHeightRecyclerView) layout
                .findViewById(R.id.layout_optional_time_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                this.layout.getContext());
        layoutManager.setAutoMeasureEnabled(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(
                new VerticalDividerDecoration(this.layout.getContext()));
        
        mAdapter = new Adapter();
        recyclerView.setAdapter(mAdapter);
        // measure item height
        RecyclerView.ViewHolder viewHolder = mAdapter.onCreateViewHolder(recyclerView, 0);
        viewHolder.itemView.measure(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        int height = viewHolder.itemView.getMeasuredHeight();
        // show 5.5 item of max
        recyclerView.setMaxHeight((int) (height * 5.5f));
        
        mAdapter.setItemDelListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                
                // 不是最后一项则需要刷新item次序
                if (position < resultList.size() - 1) {
                    resultList.remove(position);
                    mAdapter.notifyDataSetChanged();
                }
                else {
                    resultList.remove(position);
                    mAdapter.notifyItemRemoved(position);
                }
                
                if (resultList.isEmpty()) {
                    layout.setVisibility(View.GONE);
                }
                else {
                    recyclerView.invalidate();
                }
                
                if (mListener != null) {
                    mListener.timeChanged();
                }
            }
        });
        
    }

    private void showClearConfirmDialog() {
        
        if (clearConfirmDialog == null) {
            
            clearConfirmDialog = OrderDialogUtil.showDialog(activity,
                    activity.getString(R.string.text_dialog_clear_confirm_title),
                    activity.getString(R.string.text_clear),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            resultList.clear();
                            layout.setVisibility(View.GONE);
                            
                            if (mListener != null) {
                                mListener.timeChanged();
                            }
                        }
                    }, activity.getString(R.string.cancel));
        }
        else {
            clearConfirmDialog.show();
        }
    }

    private class Adapter extends RecyclerView.Adapter {
        
        private OnItemClickListener listener;
        
        private void setItemDelListener(OnItemClickListener listener) {
            this.listener = listener;
        }
        
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                int viewType) {
            View itemView = LayoutInflater.from(layout.getContext())
                    .inflate(R.layout.item_optional_time, parent, false);
            ViewHolder viewHolder = new ViewHolder(itemView);
            if (listener != null) {
                viewHolder.setDelListener(listener);
            }
            return viewHolder;
        }
        
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            TimeSlice time = resultList.get(position);
            ((ViewHolder) holder).setData(time, position);
        }
        
        @Override
        public int getItemCount() {
            return resultList != null ? resultList.size() : 0;
        }
    }
    
    private class ViewHolder extends RecyclerView.ViewHolder {
        
        private TextView tvTitle, tvDate;
        
        private ImageView imgDel;
        
        private int position;
        
        public ViewHolder(View itemView) {
            super(itemView);
            
            tvTitle = (TextView) itemView.findViewById(R.id.item_optional_time_title);
            tvDate = (TextView) itemView.findViewById(R.id.item_optional_time_date);
            imgDel = (ImageView) itemView.findViewById(R.id.item_optional_time_del);
        }
        
        private void setDelListener(final OnItemClickListener listener) {
            if (listener != null) {
                imgDel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onItemClick(v, position);
                    }
                });
            }
        }
        
        private void setData(TimeSlice time, int position) {
            this.position = position;
            tvTitle.setText(layout.getContext()
                    .getString(R.string.text_optional_time_ordinal, position + 1));
            tvDate.setText(SelectTimeUtils.getContentByTimeSlice(time));
        }
    }
    
    protected interface ITimeChangedListener {
        void timeChanged();
    }
}

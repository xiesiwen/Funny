package com.qingqing.project.offline.order.v3;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.qingqing.project.offline.R;
import com.qingqing.project.offline.seltime.SelectTimeUtils;

/**
 * 时间块展示Adapter
 *
 * Created by tanwei on 2017/8/23.
 */

public class TimeBlockAdapter extends RecyclerView.Adapter {

    private Context mContext;
    
    private SparseIntArray mStatusMap;// 标记每个block状态
    
    private ITimeBlockClickListener mClickListener;
    
    private int colorInvalid, colorValid, colorSelected;

    private int lastSelectedBlock = TimeBlockStatusManager.INVALID_BLOCK;
    
    public TimeBlockAdapter(Context context) {
        mContext = context;
        mStatusMap = new SparseIntArray();
        
        colorInvalid = context.getResources().getColor(R.color.gray);
        colorValid = context.getResources().getColor(R.color.gray_dark_deep);
        colorSelected = context.getResources().getColor(R.color.accent_orange_deep);
    }
    
    public void setBlockClickListener(ITimeBlockClickListener listener) {
        this.mClickListener = listener;
    }
    
    /**
     * 设置当前选择的block
     *
     * @param block
     *            block
     */
    public void setSelectedTimeBlock(int block) {
        
        if (lastSelectedBlock != block) {
            int mask = mStatusMap.get(block) & TimeBlockStatusManager.STATUS_FILTER_MASK;
            mStatusMap.put(block, TimeBlockStatusManager.STATUS_CURRENT_SELECTED + mask);
            notifyItemChanged(mStatusMap.indexOfKey(block));
            
            // 取消之前的已选状态
            if (lastSelectedBlock > TimeBlockStatusManager.INVALID_BLOCK) {
                mask = mStatusMap.get(lastSelectedBlock)
                        & TimeBlockStatusManager.STATUS_FILTER_MASK;
                mStatusMap.put(lastSelectedBlock, TimeBlockStatusManager.STATUS_INIT + mask);
                notifyItemChanged(mStatusMap.indexOfKey(lastSelectedBlock));
            }
            lastSelectedBlock = block;
        }
    }
    
    /**
     * 更新block状态
     *
     * @param data
     *            [block, status]
     * @param clearSelected
     */
    public void updateBlockStatus(SparseIntArray data, boolean clearSelected) {
        
        if (clearSelected) {
            lastSelectedBlock = TimeBlockStatusManager.INVALID_BLOCK;
        }
        
        mStatusMap = data;
        
        notifyDataSetChanged();
    }

    public int getPositionOfBlock(int block) {
        return mStatusMap.indexOfKey(block);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        
        View itemView = LayoutInflater.from(mContext)
                .inflate(R.layout.item_block_time, parent, false);
        BlockHolder holder = new BlockHolder(itemView);
        if (mClickListener != null) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int block = (int) v.getTag();
                    mClickListener.onTimeBlockClick(block,
                            mStatusMap.get(block, TimeBlockStatusManager.STATUS_INIT));
                }
            });
        }
        return holder;
    }
    
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int block = mStatusMap.keyAt(position);
        holder.itemView.setTag(block);
        ((BlockHolder) holder).setTimeBlock(block,
                mStatusMap.get(block, TimeBlockStatusManager.STATUS_INIT));
    }
    
    @Override
    public int getItemCount() {
        return mStatusMap.size();
    }
    
    private class BlockHolder extends RecyclerView.ViewHolder {
        
        private TextView tvTime;
        
        private ImageView imgBusy;

        private int maskTextSize;
        
        private BlockHolder(View itemView) {
            super(itemView);
            
            tvTime = (TextView) itemView.findViewById(R.id.item_block_time_tv_time);
            imgBusy = (ImageView) itemView.findViewById(R.id.item_block_time_iv_busy);

            maskTextSize = mContext.getResources()
                    .getDimensionPixelSize(R.dimen.font_size_10);
        }
        
        private void setTimeBlock(int timeBlock, int status) {
            tvTime.setText(SelectTimeUtils.getHMbyBlock(timeBlock));
            
            boolean techerOccupied = (status
                    & TimeBlockStatusManager.STATUS_MASK_TEACHER_OCCUPIED) > 0;
            boolean studentOccupied = (status
                    & TimeBlockStatusManager.STATUS_MASK_STUDENT_OCCUPIED) > 0;
            
            switch (status & TimeBlockStatusManager.STATUS_FILTER) {
                case TimeBlockStatusManager.STATUS_INIT:
                    tvTime.setBackgroundResource(R.drawable.bg_sel_time_circle_normal);
                    tvTime.setTextColor(colorValid);
                    if (techerOccupied) {
                        appendMaskText(tvTime, mContext
                                .getString(R.string.text_block_extend_teacher_occupied));
                    }
                    else if (studentOccupied) {
                        appendMaskText(tvTime, mContext
                                .getString(R.string.text_block_extend_student_occupied));
                    }
                    break;
                
                case TimeBlockStatusManager.STATUS_OTHER_SELECTED:
                    if (techerOccupied) {
                        appendMaskText(tvTime, mContext
                                .getString(R.string.text_block_extend_teacher_occupied));
                    }
                    else if (studentOccupied) {
                        appendMaskText(tvTime, mContext
                                .getString(R.string.text_block_extend_student_occupied));
                    }
                    else {
                        tvTime.append(
                                mContext.getString(R.string.text_block_extend_selected));
                    }
                    
                    tvTime.setBackgroundResource(R.drawable.bg_sel_time_circle_disabled);
                    tvTime.setTextColor(colorInvalid);
                    break;
                
                case TimeBlockStatusManager.STATUS_CURRENT_SELECTED:
//                    if (techerOccupied) {
//                        appendMaskText(tvTime, mContext
//                                .getString(R.string.text_block_extend_teacher_occupied));
//                    }
//                    else if (studentOccupied) {
//                        appendMaskText(tvTime, mContext
//                                .getString(R.string.text_block_extend_student_occupied));
//                    }
//                    else {
                        tvTime.append(
                                mContext.getString(R.string.text_block_extend_selecting));
//                    }
                    
                    tvTime.setBackgroundResource(R.drawable.bg_sel_time_circle_selected);
                    tvTime.setTextColor(colorSelected);
                    break;
            }
            
            boolean busy = (status & TimeBlockStatusManager.STATUS_MASK_TEACHER_BUSY) > 0;
            if (busy) {
                imgBusy.setVisibility(View.VISIBLE);
            }
            else {
                imgBusy.setVisibility(View.GONE);
            }
        }

        private void appendMaskText(TextView text, String mask) {
            SpannableString spannableString = new SpannableString(text.getText() + mask);
            spannableString.setSpan(new AbsoluteSizeSpan(maskTextSize),
                    spannableString.length() - mask.length(), spannableString.length(),
                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            
            text.setText(spannableString);
        }
    }

    public static interface ITimeBlockClickListener {
        void onTimeBlockClick(int block, int status);
    }
}

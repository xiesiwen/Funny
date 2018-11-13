package com.qingqing.project.offline.view.order;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.qingqing.base.utils.DisplayUtil;

/**
 * 取最大高度的RecyclerView
 *
 * Created by tanwei on 2017/9/6.
 */

public class MaxHeightRecyclerView extends RecyclerView {

    private int maxHeight;

    public MaxHeightRecyclerView(Context context) {
        super(context);
    }

    public MaxHeightRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MaxHeightRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setMaxHeight(int height){
        maxHeight = height;
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        if (maxHeight > 0 && getMeasuredHeight() > maxHeight) {
            setMeasuredDimension(getMeasuredWidth(), maxHeight);
        }
    }
}

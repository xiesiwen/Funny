package com.qingqing.project.offline.view.calendar;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ListAdapter;

/**
 * Created by huangming on 2015/12/3.
 */
public class DayGridView extends GridView implements View.OnClickListener {
    
    private ListAdapter mAdapter;
    
    private int mNumColumns;
    
    public DayGridView(Context context) {
        this(context, null);
    }
    
    public DayGridView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public DayGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    private DataSetObserver mDataSetObserver = new DataSetObserver() {
        @Override
        public void onInvalidated() {
            super.onInvalidated();
        }
        
        @Override
        public void onChanged() {
            super.onChanged();
            notifyDataChange();
        }
    };
    
    @Override
    public void setNumColumns(int numColumns) {
        mNumColumns = numColumns;
    }
    
    @Override
    public void setAdapter(ListAdapter adapter) {
        if (mAdapter != null) {
            mAdapter.unregisterDataSetObserver(mDataSetObserver);
        }
        removeAllViewsInLayout();
        mAdapter = adapter;
        mAdapter.registerDataSetObserver(mDataSetObserver);
        
        notifyDataChange();
    }
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return false;
    }
    
    @Override
    public ListAdapter getAdapter() {
        return mAdapter;
    }
    
    private void notifyDataChange() {
        if (mAdapter != null) {
            int itemCount = mAdapter.getCount();
            for (int i = 0; i < itemCount; i++) {
                View child = mAdapter.getView(i, getChildAt(i), this);
                if (indexOfChild(child) < 0) {
                    child.setOnClickListener(this);
                    addViewInLayout(child, i, child.getLayoutParams());
                }
            }
        }
    }
    
    @Override
    public void removeAllViews() {
        super.removeAllViews();
        int count = getChildCount();
        while (count > 0) {
            removeDetachedView(getChildAt(0), false);
            count--;
        }
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        
        int numColumns = mNumColumns;
        int count = getChildCount();
        int newHeight = paddingTop + paddingBottom;
        if (numColumns > 0 && count > 0) {
            int columnWidth = (widthSpecSize - paddingLeft - paddingRight) / numColumns;
            int columnMaxHeight = 0;
            for (int i = 0; i < count; i++) {
                View child = getChildAt(i);
                ViewGroup.LayoutParams lp = child.getLayoutParams();
                int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(columnWidth,
                        MeasureSpec.EXACTLY);
                int childHeightMeasureSpec = getChildMeasureSpec(
                        MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), 0,
                        lp.height);
                child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
                columnMaxHeight = Math.max(columnMaxHeight, child.getMeasuredHeight());
                if ((i % numColumns) == numColumns - 1 || i == count - 1) {
                    newHeight += columnMaxHeight;
                    columnMaxHeight = 0;
                }
            }
        }
        newHeight = Math.min(newHeight, heightSpecSize);
        setMeasuredDimension(widthSpecSize, newHeight);
    }
    
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        
        int left = paddingLeft;
        int top = paddingTop;
        
        int numColumns = mNumColumns;
        int count = getChildCount();
        if (numColumns > 0 && count > 0) {
            int columnMaxHeight = 0;
            for (int i = 0; i < count; i++) {
                View child = getChildAt(i);
                int childWidth = child.getMeasuredWidth();
                int childHeight = child.getMeasuredHeight();
                child.layout(left, top, left + childWidth, top + childHeight);
                columnMaxHeight = Math.max(columnMaxHeight, childHeight);
                if ((i % numColumns) == numColumns - 1) {
                    left = paddingLeft;
                    top += columnMaxHeight;
                }
                else {
                    left += childWidth;
                }
            }
        }
    }
    
    @Override
    public void onClick(View view) {
        int index = indexOfChild(view);
        if (mAdapter != null && getOnItemClickListener() != null && index >= 0
                && index < mAdapter.getCount()) {
            getOnItemClickListener().onItemClick(this, view, index,
                    mAdapter.getItemId(index));
        }
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAdapter != null) {
            mAdapter.unregisterDataSetObserver(mDataSetObserver);
        }
        mAdapter = null;
    }
}

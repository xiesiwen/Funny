package com.qingqing.project.offline.view.order;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qingqing.base.config.LogicConfig;
import com.qingqing.project.offline.R;
import com.qingqing.project.offline.seltime.SelectTimeUtils;
import com.qingqing.project.offline.seltime.TimeSlice;

import java.util.List;

/**
 * 封装下单页的时间展示
 *
 * Created by tanwei on 2015/10/15.
 */
public class OrderConfirmTimeDisplayer extends LinearLayout {

    // 最大展示课程时间数量，多了折叠
    private static final int MAX_TIME_SLICE = 4;
    
    private TextView mTitleTextView, mValueTextView;

    private int titleColorNormal, titleColorUncompleted;

    private int valueColorNormal, valueColorUncompleted;
    
    private LinearLayout mContainer;
    
    private ImageView mActionImg;
    
    private float mTotalCourseTimeInHour;
    
    public OrderConfirmTimeDisplayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_order_confirm_time_display, this);
        setOrientation(HORIZONTAL);
        int dimen12 = getResources().getDimensionPixelOffset(R.dimen.dimen_12);
        setPadding(dimen12, dimen12 / 2, dimen12, dimen12 / 2);

        titleColorNormal = getResources().getColor(R.color.gray_dark_deep);
        titleColorUncompleted = getResources().getColor(R.color.accent_orange);

        valueColorNormal = getResources().getColor(R.color.black_light);
        valueColorUncompleted = titleColorUncompleted;
    }
    
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        
        mTitleTextView = (TextView) findViewById(R.id.view_order_confirm_time_title);
        mValueTextView = (TextView) findViewById(R.id.view_order_confirm_time_default);
        mContainer = (LinearLayout) findViewById(R.id.view_order_confirm_time_container);
        mActionImg = (ImageView) findViewById(R.id.view_order_confirm_time_action);
    }
    
    /** 设置标题 */
    public void setTitle(String title) {
        if (!TextUtils.isEmpty(title)) {
            mTitleTextView.setText(title);
        }
    }
    
    /** 设置内容 */
    public void setValue(String value) {
        mValueTextView.setText(value);
        int count = mContainer.getChildCount();
        if (count > 1) {
            mContainer.removeViews(1, count - 1);
        }
    }
    
    /** 设置是否可操作 */
    public void showAction(boolean show) {
        if (show) {
            mActionImg.setVisibility(View.VISIBLE);
            setClickable(true);
        }
        else {
            mActionImg.setVisibility(View.GONE);
            setClickable(false);
        }
    }

    /** 设置是否填充完成，未完成会显示橙色 */
    public void setUncompleted(boolean uncompleted){
        if(uncompleted){
//            mTitleTextView.setTextColor(titleColorUncompleted);
            mValueTextView.setTextColor(valueColorUncompleted);
        }else{
//            mTitleTextView.setTextColor(titleColorNormal);
            mValueTextView.setTextColor(valueColorNormal);
        }
    }

    /** 显示时间列表,返回总课时（hour） */
    public void displayTime(final List<TimeSlice> list, float totalHour) {
        
        int count = mContainer.getChildCount();
        if (count > 1) {
            mContainer.removeViews(1, count - 1);
        }
        if (list == null || list.size() == 0) {
            mValueTextView.setText(getResources().getString(R.string.text_select));
            setUncompleted(true);
        }
        else {
            setUncompleted(false);
            int size = list.size();
            mTotalCourseTimeInHour = totalHour;
            
            TimeSlice time;
            for (int i = 0; i < size; i++) {
                // 最多显示max条，然后添加提示文字
                if (i == MAX_TIME_SLICE) {
                    addNoticeText(false, list);
                    continue;
                }
                else if (i > MAX_TIME_SLICE) {
                    break;
                }
                
                time = list.get(i);
                String timeStr = SelectTimeUtils.getContentByTimeSlice(time);
                
                if (i == 0) {
                    mValueTextView.setText(timeStr);
                }
                else {
                    TextView tv = new TextView(getContext());
                    tv.setGravity(Gravity.RIGHT);
                    tv.setLayoutParams(new LinearLayout.LayoutParams(
                            LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                    tv.setPadding(0, 10, 0, 10);
                    tv.setText(timeStr);
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                    tv.setTextColor(valueColorNormal);
                    mContainer.addView(tv);
                }
            }
        }
    }
    
    // 尾部提示性文字
    private void addNoticeText(boolean isAllDisplayed, final List<TimeSlice> list) {
        TextView tv = new TextView(getContext());
        tv.setGravity(Gravity.RIGHT);
        tv.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        tv.setTextColor(getResources().getColor(R.color.gray_dark));
        
        if (isAllDisplayed) {
            tv.setText(String.format(
                    getResources().getString(R.string.order_confirm_all_time_displayed),
                    list.size(), LogicConfig.getFormatDotString(mTotalCourseTimeInHour)));
        }
        else {
            tv.setText(String.format(
                    getResources().getString(R.string.order_confirm_display_all_time),
                    list.size(), LogicConfig.getFormatDotString(mTotalCourseTimeInHour)));
            tv.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    mContainer.removeViewAt(MAX_TIME_SLICE);
                    displayAllTimeSlice(list);
                }
            });
        }
        
        mContainer.addView(tv);
    }
    
    // 展开全部时间
    private void displayAllTimeSlice(List<TimeSlice> list) {
        
        int size = list.size();
        // 从未显示的时间开始
        for (int i = MAX_TIME_SLICE; i < size; i++) {
            TimeSlice time = list.get(i);
            String timeStr = SelectTimeUtils.getContentByTimeSlice(time);
            TextView tv = new TextView(getContext());
            tv.setGravity(Gravity.RIGHT);
            tv.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT));
            tv.setText(timeStr);
            tv.setPadding(0, 10, 0, 10);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            tv.setTextColor(valueColorNormal);
            mContainer.addView(tv);
        }

        addNoticeText(true, list);
    }

    /** 显示全部时间 */
    public void displayAllTime(List<TimeSlice> list) {

        mContainer.removeAllViews();
        int size = list.size();
        for (int i = 0; i < size; i++) {
            TimeSlice time = list.get(i);
            String timeStr = SelectTimeUtils.getContentByTimeSlice(time);
            TextView tv = new TextView(getContext());
            tv.setGravity(Gravity.RIGHT);
            tv.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT));
            tv.setText(timeStr);
            tv.setPadding(0, 10, 0, 10);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            tv.setTextColor(valueColorNormal);
            mContainer.addView(tv);
        }
    }
}

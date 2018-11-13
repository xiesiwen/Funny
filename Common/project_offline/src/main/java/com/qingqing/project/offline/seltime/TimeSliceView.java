package com.qingqing.project.offline.seltime;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.AbsListView.LayoutParams;

import com.qingqing.api.proto.v1.app.AppCommon;
import com.qingqing.base.data.BaseData;
import com.qingqing.project.offline.R;

/**
 * 显示固定格式的时间段信息和状态信息
 * 
 * @author tanwei
 * 
 */
public class TimeSliceView extends AppCompatTextView {
    
    public static final int STATUS_INVALID = 0;
    
    public static final int STATUS_VALID = 1;
    
    public static final int STATUS_SELECTED = 2;// 区分各端
    
    public static final int STATUS_TEACHER_LESSON = 3;
    
    public static final int STATUS_STUDENT_LESSON = 4;
    
    public static final int STATUS_OLD_LESSON = 5;// 区分各端

    private static int mWidth;
    private int mStatus = -1;
    private int mSliceBlock = -99;
    private String mTimeStr;
    
    public TimeSliceView(Context context) {
        super(context);
        init();
    }
    
    public TimeSliceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        if (mWidth <= 0) {
            int margin = (int) getResources().getDimension(R.dimen.dimen_6);
            DisplayMetrics metrics = new DisplayMetrics();
            ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE))
                    .getDefaultDisplay().getMetrics(metrics);
            int displayWidth = metrics.widthPixels;
            mWidth = (displayWidth - margin * 7) / 6;
        }
        
        LayoutParams params = new LayoutParams(mWidth, mWidth);
        setLayoutParams(params);
        setGravity(Gravity.CENTER);
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
    }

    /** 设置时间片段下标和状态 */
    public void setBlockAndStatus(int block, int status) {
        setBlock(block);
        setStatus(status);
    }
    
    /** 设置时间片段下标 */
    public void setBlock(int index) {

        if (index == mSliceBlock) {
            return;
        }

        mSliceBlock = index;
        mTimeStr = SelectTimeUtils.getSliceContentByBlock(index);
        setText(mTimeStr);
    }

    /** 设置时间片段的状态 */
    public void setStatus(int status) {

        if (mStatus == status) {
            return;
        }

        mStatus = status;
        switch (BaseData.getClientType()) {
            case AppCommon.AppType.qingqing_student:
                setTimeStatusInStudent(status);
                break;
            case AppCommon.AppType.qingqing_teacher:
                setTimeStatusInTeacher(status);
                break;
            case AppCommon.AppType.qingqing_ta:
                setTimeStatusInTA(status);
                break;
        }

    }

    private void setTimeStatusInTeacher(int status) {
        switch (status) {
            case STATUS_INVALID:
                setBackgroundResource(R.drawable.seltime_status_invalid);
                setTextColor(getContext().getResources().getColor(R.color.white));
                setText(mTimeStr);
                break;
            case STATUS_VALID:
                setBackgroundResource(R.drawable.seltime_status_valid);
                setTextColor(getContext().getResources().getColor(R.color.black));
                setText(mTimeStr);
                break;
            case STATUS_TEACHER_LESSON:
                setBackgroundResource(R.drawable.seltime_status_teacher_lesson);
                setTextColor(getContext().getResources().getColor(R.color.primary_blue));
                setText(R.string.text_time_slice_teacher_lesson);
                break;
            case STATUS_SELECTED:
                setBackgroundResource(R.color.primary_blue);
                setTextColor(getContext().getResources().getColor(R.color.white));
                setText(mTimeStr);
                break;
            case STATUS_STUDENT_LESSON:
                setBackgroundResource(R.drawable.seltime_status_student_lesson);
                setTextColor(getContext().getResources().getColor(R.color.primary_green));
                setText(R.string.text_time_slice_student_lesson);
                break;

            case STATUS_OLD_LESSON:
                setBackgroundResource(R.drawable.seltime_status_teacher_old_course);
                // TODO 颜色需要定义，淡紫色
                setTextColor(0xffB698E0);
                setText(R.string.text_time_slice_teacher_old_lesson);
                break;

            default:
                break;
        }
    }

    private void setTimeStatusInStudent(int status) {
        switch (status) {
            case STATUS_VALID:
                setBackgroundResource(R.drawable.seltime_status_valid);
                setTextColor(getContext().getResources().getColor(R.color.black));
                setText(mTimeStr);
                break;
            case STATUS_INVALID:
            case STATUS_TEACHER_LESSON:
            case STATUS_STUDENT_LESSON:
                setBackgroundResource(R.drawable.seltime_status_invalid);
                setTextColor(getContext().getResources().getColor(R.color.white));
                setText(mTimeStr);
                break;
            case STATUS_SELECTED:
                setBackgroundResource(R.drawable.seltime_status_student_selected);
                setTextColor(getContext().getResources().getColor(R.color.white));
                setText(mTimeStr);
                break;
            case STATUS_OLD_LESSON:
                setBackgroundResource(R.drawable.seltime_status_student_original);
                setTextColor(getContext().getResources().getColor(R.color.primary_green));
                setText(R.string.text_time_slice_student_old_lesson);
                break;

            default:
                break;
        }
    }

    private void setTimeStatusInTA(int status) {
        switch (status) {
            case STATUS_INVALID:
                setBackgroundResource(R.drawable.seltime_status_invalid);
                setTextColor(getContext().getResources().getColor(R.color.white));
                setText(mTimeStr);
                break;
            case STATUS_VALID:
                setBackgroundResource(R.drawable.seltime_status_valid);
                setTextColor(getContext().getResources().getColor(R.color.black));
                setText(mTimeStr);
                break;
            case STATUS_TEACHER_LESSON:
                setBackgroundResource(R.drawable.seltime_status_teacher_lesson);
                setTextColor(getContext().getResources().getColor(R.color.primary_blue));
                setText(R.string.text_time_slice_teacher_lesson_ta);
                break;
            case STATUS_SELECTED:
                setBackgroundResource(R.drawable.seltime_status_ta_selected);
                setTextColor(getContext().getResources().getColor(R.color.white));
                setText(mTimeStr);
                break;
            case STATUS_STUDENT_LESSON:
                setBackgroundResource(R.drawable.seltime_status_student_lesson);
                setTextColor(getContext().getResources().getColor(R.color.primary_green));
                setText(R.string.text_time_slice_student_lesson);
                break;

            case STATUS_OLD_LESSON:
                setBackgroundResource(R.drawable.seltime_status_teacher_old_course);
                // TODO 颜色需要定义，淡紫色
                setTextColor(0xffB698E0);
                setText(R.string.text_time_slice_student_old_lesson_ta);
                break;

            default:
                break;
        }
    }
}

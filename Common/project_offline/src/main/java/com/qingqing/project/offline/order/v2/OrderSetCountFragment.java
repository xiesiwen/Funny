package com.qingqing.project.offline.order.v2;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.qingqing.api.proto.v1.OrderCommonEnum;
import com.qingqing.api.proto.v1.OrderDetail;
import com.qingqing.api.proto.v1.app.AppCommon;
import com.qingqing.base.bean.UserBehaviorLogExtraData;
import com.qingqing.base.config.LogicConfig;
import com.qingqing.base.constant.BaseParamKeys;
import com.qingqing.base.core.UserBehaviorLogManager;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.data.StatisticalDataConstants;
import com.qingqing.base.dialog.CompatDialog;
import com.qingqing.project.offline.utils.ABTestUtil;
import com.qingqing.qingqingbase.ui.BaseFragment;
import com.qingqing.base.log.Logger;
import com.qingqing.base.view.picker.NumberPicker;
import com.qingqing.project.offline.view.picker.TimeLengthPicker;
import com.qingqing.project.offline.R;

/**
 * 下单（续课）设置课次以及单次课时长界面
 *
 * Created by tanwei on 2016/9/18.
 */
public class OrderSetCountFragment extends BaseFragment {

    private static final String TAG = "setCount";

    private static final float COURSE_LENGTH_MIN_TEACHER = 1;

    public static final float COURSE_LENGTH_DEFAULT = 2;

    public static final float COURSE_LENGTH_MIN = COURSE_LENGTH_DEFAULT;

    public static final float COURSE_LENGTH_STEP = 0.5f;

    private static final float COURSE_LENGTH_MAX = 6;

    protected OrderParams mParams;

    protected OrderTeacherInfoHolder teacherViewHolder;

    protected TextView mTvCount, mTvLength, mTvTotalHour, mTvTotalAmountLabel, mTvTotalAmount;

    private Button mSubmitBtn;

    protected int mCount, mMinCount, mMaxCount; // 课次

    protected float mCourseLength;// 时长

    private boolean isOrder;// 是否是下单操作

    private CompatDialog mExitDialog;

    protected boolean isExitDialogShow;

    private int styleOfPickerView, styleOfAlertDialog;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mParams = getArguments()
                .getParcelable(BaseParamKeys.PARAM_PARCELABLE_ORDER_CONFIRM);
        Logger.v(TAG, "order param: " + mParams);

        switch (mParams.getCreateType()) {
            case OrderCommonEnum.OrderCreateType.renew_order_by_teacher_oct:
            case OrderCommonEnum.OrderCreateType.renew_order_by_student_oct:
            case OrderCommonEnum.OrderCreateType.renew_order_by_ta_oct:
                // 在线课续课时，如果在后续界面修改了授课方式，那么在进入这个界面，忽略在线课的时长
                float timeInHour = mParams.getTimeList().get(0).getTimeInHour();
                if (mParams.getCourseLength() < timeInHour) {
                    mCourseLength = timeInHour;
                }
                else {
                    mCourseLength = mParams.getCourseLength();
                }
                break;
            
            default:
                isOrder = true;
                float length = mParams.getCourseLength();
                mCourseLength = length > 0 ? length : COURSE_LENGTH_DEFAULT;
                break;
        }

        initParams();
    }

    // 初始化部分参数
    protected void initParams() {
        
        OrderDetail.OrderCourseCountConfig config = mParams.getCourseCountConfig();
        
        if (config != null) {
            mMinCount = config.minCount;
            if (mParams.getCourseCount() > 0) {
                mCount = mParams.getCourseCount();
            }
            else {
                mCount = config.defaultCount;
            }
            mMaxCount = config.maxCount;
            
            if (mMinCount <= 0) {
                Logger.w(TAG, "min count invalid " + mMinCount);
                mMinCount = 1;
            }
            
            if (mCount <= 0) {
                Logger.w(TAG, "default count invalid " + mCount);
                mCount = mMinCount;
            }
            
            if (mMaxCount < mMinCount) {
                Logger.w(TAG, "max(" + mMaxCount + ") < min(" + mMinCount + ")");
                mMaxCount = mMinCount + 10;
            }
            
            if (mMaxCount < mCount) {
                Logger.w(TAG, "max(" + mMaxCount + ") < default(" + mCount + ")");
                mMaxCount = mCount;
            }
        }
        else {
            Logger.w(TAG, "count config null group " + mParams.isGroupOrderV2());
            mMinCount = 1;
            mCount = 1;
            mMaxCount = 60;
        }

        Logger.v(TAG, "count " + mCount + ", min " + mMinCount + ", max " + mMaxCount);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order_set_count, container, false);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            setTitle(isOrder ? R.string.title_order_new : R.string.title_order_renew);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        setTitle(isOrder ? R.string.title_order_new : R.string.title_order_renew);

        mTvCount = (TextView) view.findViewById(R.id.fragment_order_set_count_tv_count);
        mTvLength = (TextView) view.findViewById(R.id.fragment_order_set_count_tv_length);
        mTvTotalHour = (TextView) view.findViewById(R.id.fragment_order_set_count_tv_total_time);
        mTvTotalAmountLabel = (TextView) view.findViewById(R.id.fragment_order_set_count_tv_total_amount);
        mTvTotalAmount = (TextView) view.findViewById(R.id.fragment_order_set_count_tv_total_amount_value);

        ClickListener listener = new ClickListener();

        TextView courseCountDesView = (TextView)(view.findViewById(R.id.fragment_order_set_count_tv_count_summary));
        TextView courseLengthDesView = (TextView)(view.findViewById(R.id.fragment_order_set_count_tv_length_summary));
        if (mParams.isFree()) {
            // 在线试听 隐藏课次、课时描述
            courseCountDesView.setVisibility(View.GONE);
            courseLengthDesView.setVisibility(View.GONE);
        }
        else {
            courseCountDesView.setVisibility(View.VISIBLE);
            courseLengthDesView.setVisibility(View.VISIBLE);
            //非在线才可以选择课次和时长
            view.findViewById(R.id.fragment_order_set_count_layout_count).setOnClickListener(listener);
            view.findViewById(R.id.fragment_order_set_count_layout_length).setOnClickListener(listener);
            mTvCount.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_13, 0);
            mTvLength.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_13, 0);
        }

        mSubmitBtn = (Button) view.findViewById(R.id.fragment_order_set_count_btn);
        mSubmitBtn.setOnClickListener(listener);

        mSubmitBtn.setText(R.string.text_sel_course_time);

        teacherViewHolder = new OrderTeacherInfoHolder();
        teacherViewHolder.initView(view.findViewById(R.id.layout_teacher_info));

        teacherViewHolder.updateTeacherInfo(getActivity(), mParams);

        updateOnParamChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        UserBehaviorLogManager.INSTANCE().savePageLog(
                StatisticalDataConstants.LOG_PAGE_RESERVATION_COURSE,
                new UserBehaviorLogExtraData.Builder()
                        .addExtraData(StatisticalDataConstants.LOG_EXTRA_STATUS,
                                mParams.isGroupOrderV2() ? 2 : 1)
                        .addExtraData(StatisticalDataConstants.LOG_EXTRA_ABTEST,2)
                        .build());
    }

    // 更新课次、时长，总时长、总价格
    protected void updateOnParamChanged() {
        mTvCount.setText(getString(R.string.text_format_times, mCount));
        mTvLength.setText(getString(R.string.text_format_hours,
                LogicConfig.getFormatDotString(mCourseLength)));
        mTvTotalHour.setText(getString(R.string.text_order_total_time,
                LogicConfig.getFormatDotString(mCourseLength * mCount)));
        
        if (mParams.getGradeId() < 0 || mParams.getSiteType() < 0) {
            ((View) mTvTotalAmount.getParent()).setVisibility(View.GONE);
        }
        else {
            if(mParams.isFree()){
                mTvTotalAmount.setText(getString(R.string.text_format_amount,
                        String.valueOf(0)));
            }else {
                double amount = mCourseLength * mCount * mParams.getUnitCoursePrice();
                mTvTotalAmount.setText(getString(R.string.text_format_amount,
                        LogicConfig.getFormatDotString(amount)));
                //首次课折扣优惠计算
                updateFirstCourseDiscount();
            }
        }
        
        mSubmitBtn.setEnabled(mCourseLength > COURSE_LENGTH_STEP && mCount > 0);
    }

    protected void updateFirstCourseDiscount() {

    }

    // 选择课次
    private void setCourseCount() {
        final NumberPicker numberPicker = new NumberPicker(getActivity());
        numberPicker.setDate(mCount, mMinCount, mMaxCount);

        if (styleOfPickerView == 0) {
            TypedValue outValue = new TypedValue();
            getActivity().getTheme().resolveAttribute(R.attr.numberPickerTheme, outValue,
                    true);
            styleOfPickerView = outValue.resourceId;
        }
        
        new CompatDialog.Builder(getActivity(), styleOfPickerView)
                .setCustomView(numberPicker)
                // 必须设置：设置dialog在底部
                .setWindowGravity(Gravity.BOTTOM)
                // 必须设置：设置dialog全屏显示
                .setWindowFullScreen(true).setTitle(R.string.text_order_course_sel_count)
                .setPositiveButton(getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                if (getActivity()!=null){ // # Fabric # 550  Fragment h{14a953c} not attached to Activity
                                    mCount = numberPicker.getNumber();
                                        Logger.v(TAG, "sel count : " + mCount);
                                         updateOnParamChanged();
                                }
                            }
                        })
                .setNegativeButton(getString(R.string.cancel), null).show();
    }

    // 选择时长
    private void setCourseTimeLength() {
        final TimeLengthPicker timeLengthPicker = new TimeLengthPicker(getActivity());
        float min = COURSE_LENGTH_MIN;

        if (BaseData.getClientType() != AppCommon.AppType.qingqing_student
                || mParams.getSiteType() == OrderCommonEnum.OrderSiteType.live_ost) {
            min = COURSE_LENGTH_MIN_TEACHER;
        }

        timeLengthPicker.setParam(min, COURSE_LENGTH_MAX, mCourseLength);
        
        if (styleOfPickerView == 0) {
            TypedValue outValue = new TypedValue();
            getActivity().getTheme().resolveAttribute(R.attr.numberPickerTheme, outValue,
                    true);
            styleOfPickerView = outValue.resourceId;
        }
        
        new CompatDialog.Builder(getActivity(), styleOfPickerView)
                .setCustomView(timeLengthPicker)
                // 必须设置：设置dialog在底部
                .setWindowGravity(Gravity.BOTTOM)
                // 必须设置：设置dialog全屏显示
                .setWindowFullScreen(true).setTitle(R.string.text_order_course_sel_length)
                .setPositiveButton(getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                if (getActivity() != null) { //  # Fabric # 550  Fragment h{14a953c} not attached to Activity
                                    mCourseLength = timeLengthPicker.getTimeLength();
                                    Logger.v(TAG, "sel length : " + mCourseLength);
                                    updateOnParamChanged();
                                }
                            }
                        })
                .setNegativeButton(getString(R.string.cancel), null).show();
    }

    @Override
    public boolean onBackPressed() {
        if (isOrder || isExitDialogShow) {
            if(mParams != null) {
                mParams.clear();
            }
            isExitDialogShow = false;
            if (mFragListener instanceof OrderFragListener) {
                ((OrderFragListener) mFragListener).back();
            }
            return true;
        }
        else {
            // 老师续课时这是第一个界面，需要确认退出
            if (BaseData.getClientType() == AppCommon.AppType.qingqing_teacher) {
                showExitDialog();
                return true;
            }
            else {
                // 保存当前选择的课次和时长
                if(mParams != null) {
                    mParams.setCourseCount(mCount).setCourseLength(mCourseLength);
                }
                return false;
            }
        }
    }

    // 显示返回确认dialog
    protected void showExitDialog() {

        if (mExitDialog == null) {
            TextView tv = new TextView(getActivity());
            tv.setText(R.string.tip_order_quit_renew_confirm);
            tv.setTextColor(getResources().getColor(R.color.gray_dark_deep));
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            tv.setGravity(Gravity.CENTER);
            int dimen = getResources().getDimensionPixelOffset(R.dimen.dimen_12);
            tv.setPadding(0, dimen, 0, dimen);

            if (styleOfAlertDialog == 0) {
                TypedValue outValue = new TypedValue();
                getActivity().getTheme().resolveAttribute(R.attr.compatAlertTheme,
                        outValue, true);
                styleOfAlertDialog = outValue.resourceId;
            }
            mExitDialog = new CompatDialog.Builder(getActivity(), styleOfAlertDialog)
                    .setCustomView(tv).setPositiveButton(R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    getActivity().onBackPressed();

                                    mExitDialog.dismiss();
                                }
                            })
                    .setNegativeButton(R.string.text_order_continue_renew, null).create();

            mExitDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    isExitDialogShow = false;
                }
            });
        }

        mExitDialog.show();
        isExitDialogShow = true;
    }

    class ClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            final int id = v.getId();
            if (id == R.id.fragment_order_set_count_layout_count) {
                setCourseCount();
                UserBehaviorLogManager.INSTANCE().saveClickLog(
                        StatisticalDataConstants.LOG_PAGE_RESERVATION_COURSE,
                        StatisticalDataConstants.CLICK_RESERVATION_COURSE_COURSE_NUM);
            }
            else if (id == R.id.fragment_order_set_count_layout_length) {
                setCourseTimeLength();
                UserBehaviorLogManager.INSTANCE().saveClickLog(
                        StatisticalDataConstants.LOG_PAGE_RESERVATION_COURSE,
                        StatisticalDataConstants.CLICK_RESERVATION_COURSE_CLASS_TIME);
            }
            else if (id == R.id.fragment_order_set_count_btn) {
                
                mParams.setCourseCount(mCount).setCourseLength(mCourseLength);
                
                if (mFragListener != null && mFragListener instanceof OrderFragListener) {
                    ((OrderFragListener) mFragListener).done(mParams);
                }
            }
        }
    }

}

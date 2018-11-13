package com.qingqing.project.offline.order.v2;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.qingqing.api.proto.v1.OrderCommonEnum;
import com.qingqing.api.proto.v1.OrderDetail;
import com.qingqing.api.proto.v1.app.AppCommon;
import com.qingqing.base.time.NetworkTime;
import com.qingqing.base.config.LogicConfig;
import com.qingqing.base.constant.BaseParamKeys;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.data.DefaultDataCache;
import com.qingqing.base.dialog.CompatDialog;
import com.qingqing.base.view.editor.LimitEditText;
import com.qingqing.qingqingbase.ui.BaseFragment;
import com.qingqing.base.log.Logger;
import com.qingqing.base.view.LimitedTextWatcher;
import com.qingqing.base.view.item.SimpleTitleValueActionData;
import com.qingqing.base.view.item.SimpleTitleValueActionView;
import com.qingqing.project.offline.R;
import com.qingqing.project.offline.constant.CourseConstant;
import com.qingqing.project.offline.seltime.SelectTimeHelper;
import com.qingqing.project.offline.seltime.SelectTimeUtils;
import com.qingqing.project.offline.seltime.TimeSlice;
import com.qingqing.project.offline.view.order.OrderConfirmTimeDisplayer;
import com.qingqing.project.offline.view.picker.TimeLengthPicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * 续课确认界面 5.6.5
 *
 * Created by tanwei on 2017/3/13.
 */
public abstract class AbsOrderRenewFragment extends BaseFragment {
    
    private static final int MSG_CHECK_INPUT_COUNT = 0x10000;
    
    protected static final int REQUEST_CODE_SEL_COUPONS = 1;// 选择奖学券
    
    protected static final String TAG = "renewFrag";
    
    protected OrderParams mParams, mBackupParams;
    
    protected int mCount, mMinCount, mMaxCount; // 课次
    
    private ClickListener mListener;
    
    protected ImageView mImgIncrease, mImgDecrease;
    
    protected LimitEditText mEditCount;
    
    protected TextView mTvTotalAmount, mTvDeduction;
    
    protected Button mBtnCommit;
    
    private SimpleTitleValueActionView infoCourseGrade, infoSiteType, infoSite;
    
    private OrderConfirmTimeDisplayer infoTime;
    
    private CompatDialog mExitDialog;
    
    private boolean isExitDialogShow;
    
    private int styleOfPickerView, styleOfAlertDialog;
    
    private boolean refreshUIWhenBack;// 点击我要修改信息后进入后面的流程，此时置为true，表示返回时需要刷新数据
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mParams = getArguments()
                .getParcelable(BaseParamKeys.PARAM_PARCELABLE_ORDER_CONFIRM);
        Logger.v(TAG, "order param: " + mParams);
        
        // 复制当前参数
        mBackupParams = new OrderParams();
        OrderLogic.storeOrderParamsForRenew(mParams, mBackupParams);
        
        initParams();
        
        mListener = new ClickListener();
    }
    
    // 初始化部分参数
    protected void initParams() {
        
        OrderDetail.OrderCourseCountConfig config = mParams.getCourseCountConfig();
        
        if (config != null) {
            mMinCount = config.minCount;
            mCount = config.defaultCount;
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
        
        mParams.setCourseCount(mCount);
        mParams.setCourseLength(mParams.getTimeList().get(0).getTimeInHour());
        
        // 直接使用服务器的时间，前端不做容错处理
        // initTime();
    }
    
    // 计算初始时间
    private void initTime() {
        TimeSlice timeSlice = mParams.getTimeList().get(0);
        
        // 修正时间为当天(如果给的时间比今天晚，取返回的时间)之后的日期（不包含当天）
        Date date = new Date(NetworkTime.currentTimeMillis());
        if (SelectTimeUtils.diffDaysOf2Dates(date, timeSlice.getStartDate()) == -1) {
            date = timeSlice.getStartDate();
        }
        Date startDate = SelectTimeUtils.getNextDateByIndexExclusive(date,
                SelectTimeUtils.getWeekDay(timeSlice.getStartDate()).value());
        
        int startBlock = timeSlice.getStart(), endBlock = timeSlice.getEnd();
        // 检查时长是否和之前的续课时间一致
        if (timeSlice.getTimeInHour() != mParams.getCourseLength()) {
            // 修正起始、结束block
            int diff = (int) (mParams.getCourseLength() / TimeLengthPicker.STEP) - 1;
            int start = timeSlice.getStart();
            int end = start + diff;
            
            // 判断重新计算的结束时间是否超过了当天
            SelectTimeHelper.updateTimeConfig();
            if (end > SelectTimeHelper.sSliceEndBlock) {
                end = SelectTimeHelper.sSliceEndBlock;
                start = end - diff;
            }
            
            startBlock = start;
            endBlock = end;
        }
        
        mParams.getTimeList().clear();
        mParams.getTimeList()
                .add(SelectTimeUtils.getTimeSliceMulti(startBlock, endBlock, startDate));
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_oreder_renew, container, false);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle(R.string.title_order_renew_confirm);
        // header
        OrderTeacherInfoHolder holder = new OrderTeacherInfoHolder();
        holder.initView(view.findViewById(R.id.layout_teacher_info));
        holder.updateTeacherInfo(getActivity(), mParams);
        
        // input
        mImgIncrease = (ImageView) view
                .findViewById(R.id.fragment_order_renew_img_count_increase);
        mImgDecrease = (ImageView) view
                .findViewById(R.id.fragment_order_renew_img_count_decrease);
        mEditCount = (LimitEditText) view.findViewById(R.id.fragment_order_renew_edit_count);
        mTvTotalAmount = (TextView) view
                .findViewById(R.id.fragment_order_renew_tv_total_amount);
        mTvDeduction = (TextView) view
                .findViewById(R.id.fragment_order_renew_tv_coupons_deduction);
        mBtnCommit = (Button) view.findViewById(R.id.fragment_order_renew_btn);
        
        mImgIncrease.setOnClickListener(mListener);
        mImgDecrease.setOnClickListener(mListener);
        ((View) mTvDeduction.getParent()).setOnClickListener(mListener);
        mBtnCommit.setOnClickListener(mListener);
        view.findViewById(R.id.fragment_order_renew_layout_change_info)
                .setOnClickListener(mListener);
        
        mEditCount.addTextChangedListener(new LimitedTextWatcher() {
            @Override
            public void afterTextChecked(Editable s) {
                String textMin = String.valueOf(mMinCount);
                if (s.length() > 0) {
                    int count = Integer.parseInt(s.toString());
                    int courseCount = mParams.getCourseCount();
                    
                    if (count == courseCount) {
                        return;
                    }
                    
                    if (count > mMaxCount) {
                        Logger.v(TAG, "greater than max, ignore");
                        String text = String.valueOf(mMaxCount);
                        mEditCount.setText(text);
                        if (courseCount < mMaxCount) {
                            onRenewCountChanged(mMaxCount);
                        }
                        return;
                    }
                    
                    if (count < mMinCount) {
                        Logger.v(TAG, "less than min, ignore");
                        mEditCount.setText(textMin);
                        if (courseCount > mMinCount) {
                            onRenewCountChanged(mMinCount);
                        }
                        return;
                    }
                    
                    onRenewCountChanged(count);
                }
                else {
                    mEditCount.setText(textMin);
                }
                mEditCount.setSelection(mEditCount.getText().length());
            }
        });
        
        // info
        updateInfo(view.findViewById(R.id.fragment_order_renew_layout_info));
    }
    
    private void updateInfo(View container) {
        // 点击修改信息
        container.setOnClickListener(mListener);
        
        int dimen8 = getResources().getDimensionPixelOffset(R.dimen.dimen_8);
        int dimen4 = dimen8 / 2;
        
        SimpleTitleValueActionView infoTeacher = (SimpleTitleValueActionView) container
                .findViewById(R.id.fragment_order_renew_info_teacher);
        infoTeacher.setPadding(dimen8, dimen4, dimen8, dimen4);
        infoCourseGrade = (SimpleTitleValueActionView) container
                .findViewById(R.id.fragment_order_renew_info_course_grade);
        infoCourseGrade.setPadding(dimen8, dimen4, dimen8, dimen4);
        infoSiteType = (SimpleTitleValueActionView) container
                .findViewById(R.id.fragment_order_renew_info_site_type);
        infoSiteType.setPadding(dimen8, dimen4, dimen8, dimen4);
        infoSite = (SimpleTitleValueActionView) container
                .findViewById(R.id.fragment_order_renew_info_site);
        infoSite.setPadding(dimen8, dimen4, dimen8, dimen4);
        infoTime = (OrderConfirmTimeDisplayer) container
                .findViewById(R.id.fragment_order_renew_info_time);
        infoTime.setPadding(dimen8, dimen4, dimen8, dimen4);
        
        infoTeacher.fillWithData(
                new SimpleTitleValueActionData(getString(R.string.text_order_teacher),
                        mParams.getTeacherInfo().nick, false));
        infoCourseGrade.fillWithData(new SimpleTitleValueActionData(
                getString(R.string.text_order_course_grade), "", false));
        infoSiteType.fillWithData(new SimpleTitleValueActionData(
                getString(R.string.text_order_site_type), "", false));
        infoSite.fillWithData(new SimpleTitleValueActionData(
                getString(R.string.text_order_site), "", false));
        
        refreshUI(mParams);
        
        infoTime.setTitle(getString(R.string.title_order_set_time));
        infoTime.displayAllTime(mParams.getTimeList());
        infoTime.showAction(false);
        
        onRenewCountChanged(mCount);
    }
    
    // 修改课次后先刷新数值，在一定时间后刷新剩余数据（避免频繁修改导致多余计算）
    private void onRenewCountChanged(int count) {
        mParams.setCourseCount(count);
        mEditCount.setText(String.valueOf(count));
        removeMessages(MSG_CHECK_INPUT_COUNT);
        sendEmptyMessageDelayed(MSG_CHECK_INPUT_COUNT,
                BaseData.getClientType() == AppCommon.AppType.qingqing_student ? 1000
                        : 100);
        
        if (count > mMinCount) {
            mImgDecrease.setImageResource(R.drawable.icon_xk_less);
        }
        else {
            mImgDecrease.setImageResource(R.drawable.icon_xk_less_gary);
        }
        
        if (count < mMaxCount) {
            mImgIncrease.setImageResource(R.drawable.icon_xk_more);
        }
        else {
            mImgIncrease.setImageResource(R.drawable.icon_xk_more_gary);
        }
    }
    
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden && refreshUIWhenBack) {
            setTitle(R.string.title_order_renew_confirm);
            refreshUIWhenBack = false;
            
            // 刷新ui
            refreshUI(mParams);
            
            // 判断时间是否被清空
            if (BaseData.getClientType() == AppCommon.AppType.qingqing_student) {
                
                if (mBackupParams.getSiteType() == OrderCommonEnum.OrderSiteType.live_ost
                        && mParams.getSiteType() != OrderCommonEnum.OrderSiteType.live_ost
                        && mBackupParams.getTimeList().get(0)
                                .getTimeInHour() < OrderSetCountFragment.COURSE_LENGTH_MIN) {
                    
                    OrderLogic.storeOrderParamsForRenew(mBackupParams, mParams);
                    initParams();
                    onRenewCountChanged(mParams.getCourseCount());
                    return;
                }
                
            }
            
            // 续课信息不完整且重新填写时也刷新数据
            // 课次或者时长变更但是时间未同步（在选课次界面修改了数据然后返回）
            if ((mBackupParams.getGradeId() < 0 && mParams.getGradeId() > 0)
                    || (mBackupParams.getSiteType() < 0 && mParams.getSiteType() >= 0)
                    || mParams.getTimeList().size() != mParams.getCourseCount()
                    || mParams.getTimeList().get(0).getTimeInHour() != mParams
                            .getCourseLength()) {
                onRenewCountChanged(mParams.getCourseCount());
            }
            else if (mParams.getTimeList().size() > 0) {
                // 刷新时间修改后的数据
                mEditCount.setText(String.valueOf(mParams.getCourseCount()));
                updateOnTimeChanged();
            }
            
        }
    }
    
    // 更改其他信息后（进入过后续界面返回）刷新可能会改动的数据
    private void refreshUI(OrderParams params) {
        
        String courseGrade;
        // 先检查科目是否存在
        if (params.getGradeId() < 0) {
            courseGrade = getString(R.string.text_renew_info_error_grade);
            infoCourseGrade.setValueCompleted(false);
        }
        else {
            DefaultDataCache dataCache = DefaultDataCache.INSTANCE();
            courseGrade = dataCache.getCourseNameById(params.getCourseId()) + " "
                    + dataCache.getGradeNameById(params.getGradeId());
            infoCourseGrade.setValueCompleted(true);
        }
        infoCourseGrade.setValue(courseGrade);
        
        String siteType;
        if (params.getSiteType() >= 0) {
            siteType = CourseConstant.getSiteType(params.getSiteType());
            infoSiteType.setValueCompleted(true);
        }
        else {
            siteType = getString(R.string.text_renew_info_error_site_type);
            infoSiteType.setValueCompleted(false);
        }
        infoSiteType.setValue(siteType);
        
        String address;
        if (params.getSiteType() == OrderCommonEnum.OrderSiteType.live_ost) {
            address = getString(R.string.text_order_address_online);
            infoSite.setValueCompleted(true);
        }
        else if (!TextUtils.isEmpty(params.getAddress())) {
            address = params.getAddress();
            infoSite.setValueCompleted(true);
        }
        else {
            address = getString(R.string.text_select_site);
            infoSite.setValueCompleted(false);
        }
        infoSite.setValue(address);
    }
    
    @Override
    public boolean onHandlerUIMsg(Message msg) {
        if (msg.what == MSG_CHECK_INPUT_COUNT && couldOperateUI()) {
            // 课次更改后重新计算时间和价格并刷新ui
            updateTime();
            updateOnTimeChanged();
            return true;
        }
        return super.onHandlerUIMsg(msg);
    }
    
    private void updateTime() {
        Calendar cal = Calendar.getInstance();
        ArrayList<TimeSlice> timeList = mParams.getTimeList();
        TimeSlice firstTime = timeList.get(0);
        cal.setTime(firstTime.getStartDate());
        timeList.clear();
        timeList.add(firstTime);
        int startBlock = firstTime.getStart(), endBlock = firstTime.getEnd();
        int courseCount = mParams.getCourseCount();
        for (int i = 1; i < courseCount; i++) {
            cal.add(Calendar.WEEK_OF_YEAR, 1);
            TimeSlice time = SelectTimeUtils.getTimeSliceMulti(startBlock, endBlock,
                    cal.getTime());
            timeList.add(time);
        }
    }
    
    // 时间更新后刷新ui
    private void updateOnTimeChanged() {

        infoTime.displayAllTime(mParams.getTimeList());
        
        // 计算总价格
        updatePayAmount();
        
        reqRecommendCoupons();
    }

    protected void updatePayAmount() {

        double totalAmount = mParams.getCourseLength() * mParams.getCourseCount()
                * mParams.getUnitCoursePrice();
        double realAmount = calRealAmount();

        String totalAmountStr = LogicConfig.getFormatDotString(totalAmount);
        mTvTotalAmount.setText(getString(R.string.text_format_amount, totalAmountStr));

        String realAmountStr = LogicConfig.getFormatDotString(realAmount);
        if (realAmount > 0) {
            mBtnCommit.setText(getString(R.string.text_renew_confirm_amount, realAmountStr));
        }
        else {
            mBtnCommit.setText(R.string.text_renew_confirm_pay);
        }
    }

    /**
     * 计算付款金额
     * @return
     */
    protected double calRealAmount() {
        return mParams.getCourseLength() * mParams.getCourseCount()
                    * mParams.getUnitCoursePrice();
    }

    /** 获取推荐奖学券 */
    protected void reqRecommendCoupons() {
        
    }
    
    /** 选择奖学券 */
    protected void selectCoupons() {
        
    }
    
    /** 提交续课 */
    protected abstract void commitRenewOrder();
    
    @Override
    public boolean onBackPressed() {
        if (isExitDialogShow) {
            mParams.clear();
            isExitDialogShow = false;
            if (mFragListener instanceof OrderFragListener) {
                ((OrderFragListener) mFragListener).back();
            }
        }
        else {
            showExitDialog();
        }
        return true;
    }
    
    // 显示返回确认dialog
    private void showExitDialog() {
        
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
    
    protected class ClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.fragment_order_renew_img_count_decrease) {
                if (mParams.getCourseCount() <= mMinCount) {
                    Logger.v(TAG, "min count, decrease ignore");
                    return;
                }
                onRenewCountChanged(mParams.getCourseCount() - 1);
            }
            else if (id == R.id.fragment_order_renew_img_count_increase) {
                if (mParams.getCourseCount() >= mMaxCount) {
                    Logger.v(TAG, "max count, increase ignore");
                    return;
                }
                onRenewCountChanged(mParams.getCourseCount() + 1);
            }
            else if (id == R.id.fragment_order_renew_layout_coupons) {
                selectCoupons();
            }
            else if (id == R.id.fragment_order_renew_layout_change_info
                    || id == R.id.fragment_order_renew_layout_info) {
                if (mFragListener instanceof RenewFragListener) {
                    ((RenewFragListener) mFragListener).toChangeInfo(mParams);
                    refreshUIWhenBack = true;
                }
            }
            else if (id == R.id.fragment_order_renew_btn) {
                commitRenewOrder();
            }
        }
    }
    
    public interface RenewFragListener extends OrderFragListener {
        void toChangeInfo(OrderParams params);
    }
}

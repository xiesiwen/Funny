package com.qingqing.project.offline.order.v2;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.qingqing.api.proto.v1.GradeCourseProto;
import com.qingqing.api.proto.v1.OrderCommonEnum;
import com.qingqing.api.proto.v1.UserProto;
import com.qingqing.api.proto.v1.app.AppCommon;
import com.qingqing.base.config.LogicConfig;
import com.qingqing.base.constant.BaseParamKeys;
import com.qingqing.project.offline.constant.CourseConstant;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.data.DefaultDataCache;
import com.qingqing.qingqingbase.ui.BaseFragment;
import com.qingqing.base.log.Logger;
import com.qingqing.base.utils.ImageUrlUtil;
import com.qingqing.base.view.AsyncImageViewV2;
import com.qingqing.base.view.ToastWrapper;
import com.qingqing.base.view.item.SimpleTitleValueActionData;
import com.qingqing.base.view.item.SimpleTitleValueActionView;
import com.qingqing.project.offline.R;
import com.qingqing.project.offline.order.SelectGradeCourseActivity;
import com.qingqing.project.offline.order.SelectSiteTypeActivity;
import com.qingqing.project.offline.view.order.OrderConfirmTimeDisplayer;

import java.util.ArrayList;

/**
 * 下单（续课）的订单确认界面, 涉及具体交互以及差异化的逻辑由子类实现
 *
 * Created by tanwei on 2016/9/18.
 */
public abstract class AbsOrderCommitFragment extends BaseFragment {
    
    protected static final String TAG = "orderCommit";
    
    // request code
    protected static final int REQUEST_CODE_SEL_COURSE_GRADE = 1;// 选择科目年级
    protected static final int REQUEST_CODE_SEL_SITE_TYPE = 2;// 选择上门方式
    protected static final int REQUEST_CODE_SEL_STUDENT_SITE = 3;// 老师上门
    protected static final int REQUEST_CODE_SEL_TEACHER_SITE = 4;// 学生上门
    protected static final int REQUEST_CODE_SEL_THIRD_SITE = 5;// 第三方场地
    protected static final int REQUEST_CODE_SEL_COUPONS = 6;// 选择奖学券
    
    protected OrderParams mParams;

    protected AsyncImageViewV2 avatar;
    protected TextView nick;
    private TextView mTvFreeCourseSign;

    protected ClickListener mClickListener;
    protected SimpleTitleValueActionView mItemCourseType;// 课程类型
    protected SimpleTitleValueActionView mItemCourseGrade;// 科目年级
    protected SimpleTitleValueActionView mItemSiteType;// 上门方式
    protected SimpleTitleValueActionView mItemSite;// 地址
    protected OrderConfirmTimeDisplayer mItemTimeDisplayer;// 时间
    protected SimpleTitleValueActionView mItemCoupons;// 奖学券
    
    protected TextView mTvUnitPrice;// 课程单价
    protected TextView mTvOriginPrice;
    protected TextView mTvCourseCount;// 课次课时
    protected TextView mTvTotalTime;// 总课时
    protected TextView mToPayAmount;// 应付款
    protected Button mBottomConfirmBtn;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mParams = getArguments()
                .getParcelable(BaseParamKeys.PARAM_PARCELABLE_ORDER_CONFIRM);
        Logger.v(TAG, "order param: " + mParams);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order_commit, container, false);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            setTitle(R.string.title_order_confirm);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        setTitle(R.string.title_order_confirm);
        
        // 老师信息
        View item = view.findViewById(R.id.item_group_member_info);
        item.setBackgroundColor(getResources().getColor(R.color.white));

        avatar = (AsyncImageViewV2) item.findViewById(R.id.item_group_member_avatar);
        nick = (TextView) item.findViewById(R.id.item_group_member_nick);
        mTvFreeCourseSign = (TextView) item.findViewById(R.id.tv_online_free);

        UserProto.SimpleUserInfoV2 userInfo;
        // 做下判断，如果取不到学生信息（理论上不会出现），还是显示老师信息（毕竟显示了好多版本了）
        if (BaseData.getClientType() == AppCommon.AppType.qingqing_teacher
                && mParams.getStudentInfoList().size() > 0) {
            userInfo = mParams.getStudentInfoList().get(0);
        }
        else {
            userInfo = mParams.getTeacherInfo();
        }

        avatar.setImageUrl(ImageUrlUtil.getHeadImg(userInfo),
                LogicConfig.getDefaultHeadIcon(userInfo));
        nick.setText(userInfo.nick);

        mTvFreeCourseSign.setVisibility(mParams.isFree() ? View.VISIBLE : View.GONE);

        // 下单信息

        mClickListener = new ClickListener();
        
        mToPayAmount = (TextView) view
                .findViewById(R.id.fragment_order_commit_tv_total_amount);
        
        mBottomConfirmBtn = (Button) view
                .findViewById(R.id.fragment_order_commit_btn_commit);
        mBottomConfirmBtn.setOnClickListener(mClickListener);
        
        mItemCourseGrade = (SimpleTitleValueActionView) view
                .findViewById(R.id.fragment_order_commit_info_course_grade);
        mItemCourseGrade.fillWithData(new SimpleTitleValueActionData(
                getString(R.string.text_order_course_grade),
                getString(R.string.text_select_course_grade), true));
        mItemCourseGrade.setOnClickListener(mClickListener);
        
        mItemCourseType = (SimpleTitleValueActionView) view
                .findViewById(R.id.fragment_order_commit_info_course_type);
        mItemCourseType.fillWithData(
                new SimpleTitleValueActionData(getString(R.string.text_order_course_type),
                        getString(R.string.text_select), true));
        
        mItemSiteType = (SimpleTitleValueActionView) view
                .findViewById(R.id.fragment_order_commit_info_site_type);
        mItemSiteType.fillWithData(
                new SimpleTitleValueActionData(getString(R.string.text_order_site_type),
                        getString(R.string.text_select_site_type), true));
        mItemSiteType.setOnClickListener(mClickListener);
        
        mItemSite = (SimpleTitleValueActionView) view
                .findViewById(R.id.fragment_order_commit_info_site);
        mItemSite.fillWithData(new SimpleTitleValueActionData(
                getString(R.string.text_order_site), getString(R.string.text_select_site), true));
        mItemSite.setOnClickListener(mClickListener);

        mItemTimeDisplayer = (OrderConfirmTimeDisplayer) view
                .findViewById(R.id.fragment_order_commit_info_time);
        
        mItemCoupons = (SimpleTitleValueActionView) view
                .findViewById(R.id.fragment_order_commit_info_coupons);
        if (mItemCoupons != null) {
            mItemCoupons.fillWithData(
                    new SimpleTitleValueActionData(getString(R.string.text_order_coupons),
                            getString(R.string.text_price_deduction_amount, "0"), true));
            mItemCoupons.setOnClickListener(mClickListener);
        }
        
        mTvUnitPrice = (TextView) view
                .findViewById(R.id.layout_order_commit_bottom_unit_price);
        mTvOriginPrice = (TextView) view
                .findViewById(R.id.layout_order_commit_bottom_unit_origin_price);
        mTvCourseCount = (TextView) view
                .findViewById(R.id.layout_order_commit_bottom_course_count);
        mTvTotalTime = (TextView) view
                .findViewById(R.id.layout_order_commit_bottom_total_hours);

        updateViewWithParams();
        setPayAmount();
    }

    // 根据参数更新view
    private void updateViewWithParams() {

        // 如果是团课则显示课程类型
        if (CourseConstant.isGrouponCourse(mParams.getCourseType())) {
            mItemCourseType.setVisibility(View.VISIBLE);
            mItemCourseType.setValue(
                    CourseConstant.getCourseType(mParams.getCourseType()));
            mItemCourseType.setValueCompleted(true);
        }
        else {
            mItemCourseType.setVisibility(View.GONE);
        }

        // 科目年级
        if (mParams.getGradeId() < 0) {
            mItemCourseGrade.setValue(getString(R.string.text_select_course_grade));
            mItemCourseGrade.setValueCompleted(false);
        }
        else {
            String courseGrade = DefaultDataCache.INSTANCE()
                    .getCourseNameById(mParams.getCourseId()) + " "
                    + DefaultDataCache.INSTANCE().getGradeNameById(mParams.getGradeId());
            mItemCourseGrade.setValue(courseGrade);
            mItemCourseGrade.setValueCompleted(true);
        }

        // 上门方式
        if (mParams.getSiteType() < 0) {
            mItemSiteType.setValue(getString(R.string.text_select_site_type));
            mItemSiteType.setValueCompleted(false);
            
            mItemSite.setValue(getString(R.string.text_select_site));
            mItemSite.setValueCompleted(false);
        }
        else {
            mItemSiteType.setValue(CourseConstant.getSiteType(mParams.getSiteType()));
            mItemSiteType.setValueCompleted(true);
            
            // 上课地址
            if (mParams.getSiteType() == OrderCommonEnum.OrderSiteType.live_ost) {
                mItemSite.setValue(getString(R.string.text_order_address_online));
                mItemSite.setValueCompleted(true);
            }
            else {
                if (!TextUtils.isEmpty(mParams.getAddress())) {
                    mItemSite.setValue(mParams.getAddress());
                    mItemSite.setValueCompleted(true);
                }
                else {
                    mItemSite.setValue(getString(R.string.text_select_site));
                    mItemSite.setValueCompleted(false);
                }
            }
        }

        // 显示时间
        mItemTimeDisplayer.displayTime(mParams.getTimeList(),
                mParams.getTotalTimeInHour());

        // 课程类型、时间不可重新修改
        mItemCourseType.showAction(false);
        mItemTimeDisplayer.showAction(false);

        switch (mParams.getCreateType()) {
            case OrderCommonEnum.OrderCreateType.renew_order_by_student_oct:
            case OrderCommonEnum.OrderCreateType.renew_order_by_teacher_oct:
            case OrderCommonEnum.OrderCreateType.renew_order_by_ta_oct:

                break;

            // 经过老师主页下单的锁定科目年级和上门方式
            default:
                mItemCourseGrade.showAction(false);
                mItemSiteType.showAction(false);
                if (mParams.getSiteType() == OrderCommonEnum.OrderSiteType.teacher_home_ost) {
                    mItemSite.showAction(false);
                }
                break;
        }
    }

    /** 设置显示课程单价、课次、课时以及应付款等数据 */
    protected double setPayAmount() {
        double amount = 0;
        
        float totalTimeInHour = mParams.getTotalTimeInHour();
        double unitCoursePrice = mParams.getUnitCoursePrice();
//        double thirdSitePrice = mParams.getThirdSitePrice();

        if (!mParams.isFree()) {
            amount = totalTimeInHour * unitCoursePrice;
        }
        // 课程单价
        if (!mParams.isFree()) {
            mTvUnitPrice.setText(getString(R.string.text_format_price,
                    LogicConfig.getFormatDotString(unitCoursePrice)));
        }
        else {
            mTvUnitPrice.setText(getString(R.string.text_format_price, "0"));
            mTvOriginPrice.setText(getString(R.string.text_format_amount,
                    LogicConfig.getFormatDotString(unitCoursePrice, 0)));
            mTvOriginPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        }

        // 课次
        mTvCourseCount.setText(getString(R.string.text_order_course_count_and_length,
                mParams.getCourseCount(),
                LogicConfig.getFormatDotString(mParams.getCourseLength())));
        // 总课时
        mTvTotalTime.setText(getString(R.string.text_format_hours,
                LogicConfig.getFormatDotString(totalTimeInHour)));
        
        // 应付款
        switch (BaseData.getClientType()) {
            case AppCommon.AppType.qingqing_student:
                amount -= mParams.getCouponsAmount();
                if (amount < 0) {
                    amount = 0;
                }
                break;
            
            case AppCommon.AppType.qingqing_teacher:
                // 团课价格需要考虑人数
                amount *= OrderLogic.getCountOfGroupType(mParams.getCourseType());
                break;
        }

        showToPayAmount(amount);

        return amount;
    }

    /** 应付金额的特殊字体大小和颜色 */
    protected void showToPayAmount(double amount) {
        SpannableString amountString;
        if(mParams.isFree()){
            amountString = new SpannableString(getString(
                    R.string.text_price_pay_amount, String.valueOf(0)));
        }else{
            amountString = new SpannableString(getString(
                    R.string.text_price_pay_amount, LogicConfig.getFormatDotString(amount)));
        }
//        amountString.setSpan(new RelativeSizeSpan(1.4f), 4, amountString.length(),
//                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        amountString.setSpan(
                new ForegroundColorSpan(getResources().getColor(R.color.gray_dark_deep)),
                0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mToPayAmount.setText(amountString);
    }
    
    /**
     * 选择科目 年级
     */
    protected void selectCourseGrade() {
        
        ArrayList<Integer> gradeList = mParams.getGradeListOfSelectedCourseType();
        if (gradeList != null) {
            
            Bundle bundle = new Bundle();
            ArrayList<Integer> courseList = new ArrayList<>(1);
            courseList.add(mParams.getCourseId());
            bundle.putIntegerArrayList(BaseParamKeys.PARAM_INT_ARRAY_COURSE_ID,
                    courseList);
            bundle.putIntegerArrayList(BaseParamKeys.PARAM_INT_ARRAY_GRADE_ID, gradeList);
            bundle.putInt(BaseParamKeys.PARAM_INT_GRADE_ID, mParams.getGradeId());
            bundle.putInt(BaseParamKeys.PARAM_INT_COURSE_PRICE_TYPE_VALUE,
                    mParams.getGradeId());
            Intent intent = new Intent(getActivity(), SelectGradeCourseActivity.class);
            intent.putExtras(bundle);
            startActivityForResult(intent, REQUEST_CODE_SEL_COURSE_GRADE);
        }
        else {
            Logger.w(TAG, "get grade list of sel course failed");
        }
    }
    
    // 选择科目年级后更新
    protected void updateOnCourseGradeSelected() {
        
        GradeCourseProto.GradeCoursePriceInfoV2 info = mParams
                .getSelectedCourseGradePriceInfo();
        
        GradeCourseProto.GradeCourseWithName gradeCourse = info.gradeCourse;
        mParams.setCourseId(gradeCourse.courseId);
        mParams.setGradeId(gradeCourse.gradeId);
        
        String courseGrade = gradeCourse.gradeName + " " + gradeCourse.courseName;
        mItemCourseGrade.setValue(courseGrade);
        mItemCourseGrade.setValueCompleted(true);
        
        // 重置上门方式和上课地址
        mParams.setSiteType(-1);
        mParams.setAddress(null);
        mItemSiteType.setValue(getString(R.string.text_select_site_type));
        mItemSiteType.setValueCompleted(false);
        mItemSite.setValue(getString(R.string.text_select_site));
        mItemSite.setValueCompleted(false);

        // 课程单价
        mParams.calUnitPrice(info.priceInfo);
        setPayAmount();
    }
    
    /**
     * 上课地点类型
     */
    protected void selectSiteType() {
        
        if (!checkInputParams(1)) {
            return;
        }
        
        // 过滤老师不支持的上门方式
        ArrayList<Integer> siteTypeList = mParams.getSiteTypeListOfSelectedGrade();
        if (siteTypeList != null) {
            
            Bundle bundle = new Bundle();
            bundle.putIntegerArrayList(BaseParamKeys.PARAM_INT_ARRAY_SITE_TYPE,
                    siteTypeList);
            bundle.putInt(BaseParamKeys.PARAM_INT_SITE_TYPE_VALUE, mParams.getSiteType());
            Intent intent = new Intent(getActivity(), SelectSiteTypeActivity.class);
            intent.putExtras(bundle);
            startActivityForResult(intent, REQUEST_CODE_SEL_SITE_TYPE);
        }
        else {
            Logger.w(TAG, "get site type list of sel course failed");
        }
    }
    
    /** 选择上门方式后更新 */
    protected abstract void updateOnSiteTypeSelected(int siteType);
    
    /**
     * 选择上课地址
     */
    protected abstract void selectSite();
    
    /** 检查输入 1-course&grade、2-siteType、3-address */
    protected boolean checkInputParams(int level) {
        
        // 科目年级
        if (level >= 1) {
            if (mParams.getCourseId() <= 0 || mParams.getGradeId() <= 0) {
                ToastWrapper.show(R.string.toast_select_grade_first);
                return false;
            }
        }
        else {
            return true;
        }
        // 上门方式
        if (level >= 2) {
            if (mParams.getSiteType() < 0) {
                ToastWrapper.show(R.string.toast_select_site_type_first);
                return false;
            }
        }
        else {
            return true;
        }
        // 上课地址
        if (level >= 3) {
            if (mParams.getSiteType() != OrderCommonEnum.OrderSiteType.live_ost
                    && TextUtils.isEmpty(mParams.getAddress())) {
                ToastWrapper.show(R.string.toast_please_set_address);
                return false;
            }

            if(mParams.getTimeList().size() == 0){
                ToastWrapper.show(R.string.toast_select_time_first);
                return false;
            }
        }
        else {
            return true;
        }
        
        return true;
    }
    
    /** 提交订单 */
    protected abstract void commit();
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_SEL_SITE_TYPE:
                    int siteType = data
                            .getIntExtra(BaseParamKeys.PARAM_INT_SITE_TYPE_VALUE, 0);
                    updateOnSiteTypeSelected(siteType);
                    break;
                
                case REQUEST_CODE_SEL_COURSE_GRADE:
                    int gradeId = data.getIntExtra(BaseParamKeys.PARAM_INT_GRADE_ID, 0);
                    if (gradeId != mParams.getGradeId()) {
                        mParams.setGradeId(gradeId);
                        updateOnCourseGradeSelected();
                    }
                    break;
            }
        }
    }
    
    /** 处理点击事件 */
    protected void onClick(int id) {}
    
    class ClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            
            final int id = v.getId();
            if (id == R.id.fragment_order_commit_info_course_grade) {
                selectCourseGrade();
            }
            else if (id == R.id.fragment_order_commit_info_site_type) {
                selectSiteType();
            }
            else if (id == R.id.fragment_order_commit_info_site) {
                selectSite();
            }
            else if (id == R.id.fragment_order_commit_btn_commit) {
                commit();
            }
            else {
                AbsOrderCommitFragment.this.onClick(id);
            }
        }
    }
}

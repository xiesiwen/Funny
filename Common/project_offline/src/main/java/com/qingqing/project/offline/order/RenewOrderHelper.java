package com.qingqing.project.offline.order;

import android.content.Context;

import com.qingqing.api.proto.v1.GradeCourseProto;
import com.qingqing.api.proto.v1.OrderCommonEnum;
import com.qingqing.api.proto.v1.OrderDetail;
import com.qingqing.api.proto.v1.Time;
import com.qingqing.api.proto.v1.UserProto;
import com.qingqing.api.proto.v1.app.AppCommon;
import com.qingqing.api.proto.v1.order.Order;
import com.qingqing.base.BaseApplication;
import com.qingqing.qingqingbase.ui.BaseActionBarActivity;
import com.qingqing.base.config.CommonUrl;
import com.qingqing.base.constant.SpecialCode;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.http.HttpUrl;
import com.qingqing.base.http.error.HttpError;
import com.qingqing.base.http.req.ProtoListener;
import com.qingqing.base.http.req.ProtoReq;
import com.qingqing.base.log.Logger;
import com.qingqing.base.view.ToastWrapper;
import com.qingqing.project.offline.R;
import com.qingqing.project.offline.order.v2.OrderParams;
import com.qingqing.project.offline.seltime.SelectTimeUtils;
import com.qingqing.project.offline.seltime.TimeSlice;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * 提供获取续课信息等功能
 *
 * Created by tanwei on 2016/1/18.
 */
public class RenewOrderHelper {

    public static final int REQUEST_CODE_RENEW_ORDER = 150;// 续课

    /** 5.2.5新增续课前置接口 */
    public static void getInfoForRenew(String teacherId, String studentId,
            ProtoListener listener) {
        UserProto.SimpleQingqingTeacherStudentIdPairRequest request = new UserProto.SimpleQingqingTeacherStudentIdPairRequest();
        request.qingqingStudentId = studentId;
        request.qingqingTeacherId = teacherId;
        HttpUrl url = BaseData.getClientType() == AppCommon.AppType.qingqing_ta
                ? CommonUrl.TA_GET_ORDER_INFO_FOR_RENEW_GROUP_URL.url()
                : CommonUrl.ORDER_INFO_FOR_RENEW_GROUP_URL.url();
        new ProtoReq(url).setSendMsg(request).setRspListener(listener).req();
    }

    /** 5.2.5新增续课前置接口 */
    public static void getInfoForRenew(final BaseActionBarActivity activity,
            String teacherId, String studentId, final GetRenewInfoListener listener) {
        GetRenewInfoRspListener l = new GetRenewInfoRspListener(activity, listener);
        getInfoForRenew(teacherId, studentId, l);
    }
    
    /** 家长端获取续课前置接口信息用来判断老的朋友团续课类型是否支持 */
    public static void checkInfoForRenew(final Context context, String teacherId,
            String studentId, final RenewOrderCheckListener listener) {
        GetRenewInfoRspListener l = new GetRenewInfoRspListener(context, listener);
        getInfoForRenew(teacherId, studentId, l);
    }

    private static class GetRenewInfoRspListener extends ProtoListener {

        private BaseActionBarActivity mActivity;

        private GetRenewInfoListener mListener;// 其他端获取续课前置接口信息的监听

        private Context mContext;

        private RenewOrderCheckListener mCheckListener;// 家长端检查朋友团续课限制（1017）的监听

        private GetRenewInfoRspListener(Class clazz) {
            super(clazz);
        }

        private GetRenewInfoRspListener(BaseActionBarActivity activity, GetRenewInfoListener listener){
            this(Order.GroupOrderInfoForRenewResponse.class);
            this.mActivity = activity;
            this.mContext = activity;
            this.mListener = listener;
        }

        private GetRenewInfoRspListener(Context context, RenewOrderCheckListener listener){
            this(Order.GroupOrderInfoForRenewResponse.class);
            mContext = context;
            mCheckListener = listener;
        }

        @Override
        public boolean onDealError(int errorCode, Object result) {
            
            switch (errorCode) {
                case 1002:
                    ToastWrapper.show(getErrorHintMessage(
                            BaseData.getClientType() == AppCommon.AppType.qingqing_teacher
                                    ? R.string.toast_teacher_self_change_course
                                    : R.string.toast_teacher_change_course));
                    break;
                
                case 1600:
                    ToastWrapper.show(getErrorHintMessage(
                            R.string.toast_zhikang_teacher_can_not_repeat_order));
                    break;
                case SpecialCode.CODE_CLOSE_DOWN:
                    int remindId = mContext.getResources().getIdentifier(
                            "teacher_is_colse_down_can_not_renew", "string",
                            BaseApplication.getCtx().getPackageName());
                    Logger.i("packageName:" + BaseApplication.getCtx().getPackageName()
                            + ", remindId:" + remindId);
                    if (remindId > 0) {
                        ToastWrapper.show(getErrorHintMessage(remindId));
                    }
                    else {
                        ToastWrapper.show(getErrorHintMessage(
                                R.string.default_remind_teacher_is_colse_down_can_not_renew));
                    }
                    break;
                
                case 1802:
                    ToastWrapper.show(getErrorHintMessage(
                            R.string.default_remind_teacher_is_put_off));
                    break;
                
                case 1011:
                    // 老师当前不支持该课程
                    ToastWrapper.show(getErrorHintMessage(
                            R.string.toast_renew_error_course_not_exist));
                    break;
                
                case 1017:
                    // 老师暂不支持该朋友团方式
                    if (mCheckListener != null) {
                        mCheckListener.showTip();
                    }
                    else {
                        ToastWrapper.show(getErrorHintMessage(
                                R.string.toast_renew_error_group_course_not_exist));
                    }
                    break;
                
                case 1021:
                    if (mCheckListener != null) {
                        mCheckListener.showTip();
                    }
                    else {
                        ToastWrapper.show(getErrorHintMessage(
                                R.string.toast_renew_error_history_order_not_exist));
                    }
                    break;
                //若老师学生最后一个订单是小组课，弹错误码1030 ， 前端需根据该错误码跳转老师主页。
                case 1030:
                    if (mCheckListener != null) {
                        mCheckListener.showTip();
                    }else{
                        ToastWrapper.show(getErrorHintMessage(
                                R.string.tip_order_get_renew_info_failed));
                    }
                    break;
                default:
                    ToastWrapper.show(getErrorHintMessage(
                            R.string.tip_order_get_renew_info_failed));
                    break;
            }
            
            return true;
        }

        @Override
        public void onDealResult(Object result) {
            OrderParams params = createOrderParams((Order.GroupOrderInfoForRenewResponse) result);

            // 检查参数
            checkRenewInfoExists(params);

            if(mListener != null){
                mListener.done(params);
            }

            if(mCheckListener != null){
                mCheckListener.toRenew(params);
            }
        }

        @Override
        public void onDealError(HttpError error, boolean isParseOK, int errorCode, Object result) {
            super.onDealError(error, isParseOK, errorCode, result);
            if (mActivity != null && mActivity.couldOperateUI()) {
                mActivity.finish();
            }
        }

    }
    /** 根据续课前置接口返回的内容填充OrderParams */
    public static OrderParams createOrderParams(Order.GroupOrderInfoForRenewResponse response){
        OrderParams params = new OrderParams();

        params.setTeacherInfo(response.teacher);
        params.setStudentInfoList(Arrays.asList(response.students));

        params.setOrderId(response.qingqingOrderId);

        params.setCourseId(response.courseId);
        params.setGradeId(response.gradeId);
        int siteType = response.siteType;
        params.setSiteType(siteType);
        params.setAddress(response.address);

        switch (siteType) {
            case OrderCommonEnum.OrderSiteType.student_home_ost:
                params.setStudentAddressId(response
                        .studentAddressId);
                break;

            case OrderCommonEnum.OrderSiteType.thirdpartplace_ost:
                params.setThirdSiteId(response.thirdpartplaceId);
                params.setThirdSitePrice(response
                        .priceOfThirdpartplace);
                break;
        }

        // 保存上次课程时间用来自动生成新的周期时间
        Time.TimeParam[] timeParams = response.defaultTimeParams;
        if (timeParams != null && timeParams.length > 0) {

            Time.TimeParam time = timeParams[0];
            TimeSlice timeSlice = SelectTimeUtils.parseToTimeSlice(time);
            ArrayList<TimeSlice> list = new ArrayList<>(1);
            list.add(timeSlice);
            params.setTimeList(list);
        }

        // 5.1新增课程类型和对应课程
        params.addTeacherCoursePrice(response.coursePrice);
        params.setCourseType(response.coursePrice.priceType);
        params.calUnitPrice();

        // 5.3.5新增续课次数
        OrderDetail.OrderCourseCountConfig config = new OrderDetail.OrderCourseCountConfig();
        config.orderType = params.isGroupOrderV2()
                ? OrderCommonEnum.OrderType.group_order_type
                : OrderCommonEnum.OrderType.general_order_type;
        config.defaultCount = response.defaultCourseCount;
        config.minCount = response.minCourseCount;
        config.maxCount = response.maxClassesCountPerOrder;
        params.setCourseCountConfig(config);

        // 5.4.0新增课程包
        params.setPacketList(Arrays.asList(response.coursePackageUnits));

        // 5.4.5新增老师最新价格，用于续课时去下单优惠包展示
        params.setTeacherNewPriceInfo(response.currentCoursePrice);
        // 5.7.5新增 续课价格、年级是否修改
        params.setPriceChanged(response.isPriceChanged);
        params.setGradeChanged(response.isGradeChanged);
        //家长端首课优惠
        if(BaseData.getClientType() == AppCommon.AppType.qingqing_student){
            params.setFirstCourseDiscount(response.canStudentFirstCourseDiscount);
            params.setFirstCourseDiscountRate(response.firstCourseDiscountRate);
        }
        return params;
    }

    /** 判断续课的年级或者上门方式是否还存在(如果不存在会重置为-1方便判断) */
    public static boolean checkRenewInfoExists(OrderParams params) {
        GradeCourseProto.GradeCoursePriceInfoV2 gradeCoursePriceInfoV2 = params
                .getSelectedCourseGradePriceInfo();
        // 年级不存在
        if (gradeCoursePriceInfoV2 == null) {
            // 清空年级
            params.setGradeId(-1);
        }
        else {
            params.calUnitPrice(gradeCoursePriceInfoV2.priceInfo);
            
            // 上门方式不存在
            if (params.getUnitCoursePrice() == 0) {
                // 清空上门方式
                params.setSiteType(-1);
            }
        }
        
        return params.getGradeId() < 0 || params.getSiteType() < 0;
    }

    public interface GetRenewInfoListener {

        void done(OrderParams params);
    }

    public interface RenewOrderCheckListener {
        void showTip();
        void toRenew(OrderParams params);
    }
}

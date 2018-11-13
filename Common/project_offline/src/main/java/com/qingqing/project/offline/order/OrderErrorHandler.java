package com.qingqing.project.offline.order;

import android.text.TextUtils;

import com.qingqing.base.view.ToastWrapper;
import com.qingqing.project.offline.R;

/**
 * 下单、续课参数保存，错误码处理等
 *
 * Created by tanwei on 2015/10/28.
 */
public class OrderErrorHandler {
    
    /**
     * 下单错误码处理
     */
    public static void dealAddOrderResponse(int errorCode, String orderId,
            String errorHintMessage) {

        if (!TextUtils.isEmpty(errorHintMessage)) {
            // 优先显示服务器给的提示
            ToastWrapper.show(errorHintMessage);
            return;
        }
        
        switch (errorCode) {
            case 1600:
                // 老师受限
                ToastWrapper.show(R.string.tip_order_error_teacher_limited);
                break;
            case 1700:
                // 优惠券不可用
                ToastWrapper.show(R.string.tip_order_error_coupons_invalid);
                break;
            case 1303:
                // 时间冲突
                ToastWrapper.show(R.string.tip_order_error_course_time_conflict);
                break;
            case 1302:
                // 家长时间冲突
                ToastWrapper.show(R.string.tip_order_error_student_time_conflict);
                break;
            case 1301:
                // 第三方场地时间冲突
                ToastWrapper.show(R.string.tip_order_error_thirdsite_time_conflict);
                break;
            case 1300:
                // 老师时间冲突
                ToastWrapper.show(R.string.tip_order_error_teacher_time_conflict);
                break;
            case 1015:
                // 第三方场地不存在
                ToastWrapper.show(R.string.tip_order_error_thirdsite_not_exist);
                break;
            case 1014:
                // 一次订单不能超过20节
                ToastWrapper.show(R.string.tip_order_error_course_count_over_20);
                break;
            case 1013:
                // 时间不正确
                ToastWrapper.show(R.string.tip_order_error_course_time_error);
                break;
            case 1012:
                // 家长的地址不存在
                ToastWrapper.show(R.string.tip_order_error_student_address_not_exists);
                break;
            case 1011:
                // 老师的课程信息不存在
                ToastWrapper
                        .show(R.string.tip_order_error_teacher_course_info_not_exists);
                break;
            case 1009:// 已存在试听课
                ToastWrapper.show(R.string.tip_order_error_teacher_audition_invalid);
                break;
            case 1008:
                // 单次课不能大于14小时
                ToastWrapper.show(R.string.tip_order_error_time_over_14h);
                break;
            case 1007:
                // 家长不存在
                ToastWrapper.show(R.string.tip_order_error_student_not_exist);
                break;
            case 1003:
                // 老师不存在
                ToastWrapper.show(R.string.tip_order_error_teacher_not_exist);
                break;
            case 1000:
                // 所有订单第一节课时间必须大于当前时间
                ToastWrapper.show(R.string.tip_order_error_time_out_of_date);
                break;
            default:
                // 下单失败
                ToastWrapper.show(R.string.add_order_fail);
                break;
        }
    }
    
    /**
     * 续课错误码处理
     */
    public static void dealRenewOrderResponse(int errorCode, String orderId,
            String errorHintMessage) {

        if (!TextUtils.isEmpty(errorHintMessage)) {
            // 优先显示服务器给的提示
            ToastWrapper.show(errorHintMessage);
        }
        
        switch (errorCode) {
            case 1303:
                // 时间冲突
                ToastWrapper.show(R.string.tip_order_error_course_time_conflict);
                break;
            case 1302:
                // 家长时间冲突
                ToastWrapper.show(R.string.tip_order_error_student_time_conflict);
                break;
            case 1301:
                // 第三方场地时间冲突
                ToastWrapper.show(R.string.tip_order_error_thirdsite_time_conflict);
                break;
            case 1300:
                // 老师时间冲突
                ToastWrapper.show(R.string.tip_order_error_teacher_time_conflict);
                break;
            case 1015:
                // 第三方场地不存在
                ToastWrapper.show(R.string.tip_order_error_thirdsite_not_exist);
                break;
            case 1014:
                // 一次订单不能超过20节
                ToastWrapper.show(R.string.tip_order_error_course_count_over_20);
                break;
            case 1013:
                // 时间不正确
                ToastWrapper.show(R.string.tip_order_error_course_time_error);
                break;
            case 1012:
                // 家长的地址不存在
                ToastWrapper.show(R.string.tip_order_error_student_address_not_exists);
                break;
            case 1011:
                // 老师的课程信息不存在
                ToastWrapper
                        .show(R.string.tip_order_error_teacher_course_info_not_exists);
                break;
            case 1007:
                // 家长不存在
                ToastWrapper.show(R.string.tip_order_error_student_not_exist);
                break;
            case 1008:
                // 单次课不能大于14小时
                ToastWrapper.show(R.string.tip_order_error_time_over_14h);
                break;
            case 1003:
                // 老师不存在
                ToastWrapper.show(R.string.tip_order_error_teacher_not_exist);
                break;
            case 1000:
                // 所有订单第一节课时间必须大于当前时间
                ToastWrapper.show(R.string.tip_order_error_time_out_of_date);
                break;
            default:
                // 续课失败
                ToastWrapper.show(R.string.text_renew_order_failed);
                break;
        }
    }
}

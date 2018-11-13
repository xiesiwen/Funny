package com.qingqing.project.offline.order;

import android.content.Context;
import android.text.SpannableString;

import com.qingqing.api.proto.v1.OrderCommonEnum;
import com.qingqing.api.proto.v1.course.OrderCourse;
import com.qingqing.api.proto.v1.order.Order;
import com.qingqing.base.config.LogicConfig;
import com.qingqing.base.utils.SpanUtil;
import com.qingqing.project.offline.R;

import java.math.BigDecimal;


/**
 * Created by wangxiaxin on 2016/8/25.
 */
public class OrderCourseUtil {
    
    public static String getGroup(Context ctx, int group) {
        String mGroup = null;
        switch (group) {
            case OrderCommonEnum.TeacherCoursePriceType.group_two_teacher_course_price_type:
            case OrderCommonEnum.TeacherCoursePriceType.two_teacher_course_price_type:
                mGroup = ctx.getString(R.string.two_group);
                break;
            case OrderCommonEnum.TeacherCoursePriceType.group_three_teacher_course_price_type:
            case OrderCommonEnum.TeacherCoursePriceType.three_teacher_course_price_type:
                mGroup = ctx.getString(R.string.three_group);
                break;
            case OrderCommonEnum.TeacherCoursePriceType.group_four_teacher_course_price_type:
                mGroup = ctx.getString(R.string.four_group);
                break;
            case OrderCommonEnum.TeacherCoursePriceType.group_five_teacher_course_price_type:
                mGroup = ctx.getString(R.string.five_group);
                break;
        }
        return mGroup;
    }
    
    public static String getOrderCourseChargeString(Context ctx, int type) {
        switch (type) {
            case OrderCommonEnum.OrderCourseChargeType.new_audition_occt:
            case OrderCommonEnum.OrderCourseChargeType.audition_occt:
            case OrderCommonEnum.OrderCourseChargeType.unknown_occt:
                return "";
            case OrderCommonEnum.OrderCourseChargeType.formal_occt:
            default:
                return ctx.getString(R.string.normal_course_text);
        }
    }

    public static SpannableString appendAudition(Context ctx,String originContent) {
        String content = "";
        content += originContent + " "+ctx.getResources().getString(R.string.audition_course_text);
        SpannableString spannableString = SpanUtil.getSpanColorString(content,R.color.primary_blue, content.length() - 3,content.length());
        return spannableString;
    }

    public static boolean isAudition(int type) {
        boolean isAudition = false;
        switch (type) {
            case OrderCommonEnum.OrderCourseChargeType.new_audition_occt:
            case OrderCommonEnum.OrderCourseChargeType.audition_occt:
                isAudition = true;
                break;
        }
        return isAudition;
    }
    
    public static boolean isGroupV2(int groupType) {
        if (groupType == OrderCommonEnum.TeacherCoursePriceType.group_two_teacher_course_price_type
                || groupType == OrderCommonEnum.TeacherCoursePriceType.group_three_teacher_course_price_type
                || groupType == OrderCommonEnum.TeacherCoursePriceType.group_four_teacher_course_price_type
                || groupType == OrderCommonEnum.TeacherCoursePriceType.group_five_teacher_course_price_type) {
            return true;
        }
        else {
            return false;
        }
    }


    public static String getGroupOrderStatus(Context ctx,int type){
        String status = null;
        switch (type) {
            case OrderCommonEnum.ClassOrderStatus.class_not_ready_class_status:
                status = ctx.getString(R.string.class_not_ready_class_status);
                break;
            case OrderCommonEnum.ClassOrderStatus.class_ready_not_full_class_status:
                status = ctx.getString(R.string.class_ready_not_full_class_status);
                break;
            case OrderCommonEnum.ClassOrderStatus.class_ready_full_class_status:
                status = ctx.getString(R.string.class_ready_full_class_status);
                break;
            case OrderCommonEnum.ClassOrderStatus.closed_class_status:
                status = ctx.getString(R.string.closed_class_status);
        }
        return status;
    }

    public static boolean isGroup(int groupType) {
        return groupType != OrderCommonEnum.TeacherCoursePriceType.normal_course_price_type
                && groupType != OrderCommonEnum.TeacherCoursePriceType.unknown_teacher_course_price_type;
    }

    public static boolean isContainOrderLabel(Order.OrderLabelItem[] orderLabelItems, int labelType){
        boolean isContainOrderLabel = false;
        if(orderLabelItems != null && orderLabelItems.length > 0){
            for(Order.OrderLabelItem item:orderLabelItems){
                if(item.labelType == labelType){
                    isContainOrderLabel = true;
                    break;
                }
            }
        }
        return isContainOrderLabel;
    }

    public static boolean isContainOrderLabel(OrderCourse.OrderCourseLabelItem[] orderLabelItems, int labelType){
        boolean isContainOrderLabel = false;
        if(orderLabelItems != null && orderLabelItems.length > 0){
            for(OrderCourse.OrderCourseLabelItem item:orderLabelItems){
                if(item.labelType == labelType){
                    isContainOrderLabel = true;
                    break;
                }
            }
        }
        return isContainOrderLabel;
    }

    /**
     * 获取折扣描述
     * @param discount 50 代表半折，75代表7.5折
     * @return
     */
    public static String getDiscountString(int discount) {
        String s = "";
        if (discount == 50) {
            s = "半价";
        }
        else if (discount > 0) {
            s = LogicConfig.getFormatDotString((double) discount / 10, 1) + "折";
        }
        return s;
    }

    /**
     *
     * @param length 课次长度
     * @param unitPrice 单价
     * @param discount 折扣
     * @return 首次课优惠折扣金额
     */
    public static int getDiscountAmount(float length, double unitPrice,
            int discount) {
        return new BigDecimal(Float.toString(length))
                .multiply(new BigDecimal(Double.toString(unitPrice)))
                .multiply(new BigDecimal(Double.toString((100 - discount) / 100d)))
                .intValue();

    }
}

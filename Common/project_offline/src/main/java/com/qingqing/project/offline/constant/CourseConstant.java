package com.qingqing.project.offline.constant;

import android.content.Context;

import com.qingqing.api.proto.v1.OrderCommonEnum;
import com.qingqing.base.BaseApplication;
import com.qingqing.project.offline.R;

/**
 * Created by wangxiaxin on 2016/6/15.
 *
 * 课程相关的一些常量
 *
 */
public final class CourseConstant {
    
    public static String getCourseType(int type) {
        final Context ctx = BaseApplication.getCtx();
        switch (type) {
            case OrderCommonEnum.TeacherCoursePriceType.normal_course_price_type:
                return ctx.getString(R.string.course_type_1v1);
            case OrderCommonEnum.TeacherCoursePriceType.two_teacher_course_price_type:
            case OrderCommonEnum.TeacherCoursePriceType.group_two_teacher_course_price_type:
                return ctx.getString(R.string.course_type_1v2);
            case OrderCommonEnum.TeacherCoursePriceType.three_teacher_course_price_type:
            case OrderCommonEnum.TeacherCoursePriceType.group_three_teacher_course_price_type:
                return ctx.getString(R.string.course_type_1v3);
            case OrderCommonEnum.TeacherCoursePriceType.group_four_teacher_course_price_type:
                return ctx.getString(R.string.course_type_1v4);
            case OrderCommonEnum.TeacherCoursePriceType.group_five_teacher_course_price_type:
                return ctx.getString(R.string.course_type_1v5);
            case OrderCommonEnum.TeacherCoursePriceType.unknown_teacher_course_price_type:
            default:
                return ctx.getString(R.string.course_type_unknown);
        }
    }
    
    public static int getCourseType(String courseTypeString) {
        final Context ctx = BaseApplication.getCtx();
        if (ctx.getString(R.string.course_type_1v1).equals(courseTypeString)) {
            return OrderCommonEnum.TeacherCoursePriceType.normal_course_price_type;
        }
        else if (ctx.getString(R.string.course_type_1v2).equals(courseTypeString)) {
            return OrderCommonEnum.TeacherCoursePriceType.group_two_teacher_course_price_type;
        }
        else if (ctx.getString(R.string.course_type_1v3).equals(courseTypeString)) {
            return OrderCommonEnum.TeacherCoursePriceType.group_three_teacher_course_price_type;
        }
        else if (ctx.getString(R.string.course_type_1v4).equals(courseTypeString)) {
            return OrderCommonEnum.TeacherCoursePriceType.group_four_teacher_course_price_type;
        }
        else if (ctx.getString(R.string.course_type_1v5).equals(courseTypeString)) {
            return OrderCommonEnum.TeacherCoursePriceType.group_five_teacher_course_price_type;
        }
        else {
            return OrderCommonEnum.TeacherCoursePriceType.unknown_teacher_course_price_type;
        }
    }
    
    public static String getSiteType(int typeValue) {
        final Context ctx = BaseApplication.getCtx();
        switch (typeValue) {
            case OrderCommonEnum.OrderSiteType.student_home_ost:
                return ctx.getString(R.string.site_type_student_home);
            case OrderCommonEnum.OrderSiteType.teacher_home_ost:
                return ctx.getString(R.string.site_type_teacher_home);
            case OrderCommonEnum.OrderSiteType.thirdpartplace_ost:
                return ctx.getString(R.string.site_type_third_home);
            case OrderCommonEnum.OrderSiteType.live_ost:
                return ctx.getString(R.string.site_type_online);
            default:
                return null;
        }
    }
    
    public static int getSiteType(String typeValueString) {
        final Context ctx = BaseApplication.getCtx();
        if (ctx.getString(R.string.site_type_student_home).equals(typeValueString)) {
            return OrderCommonEnum.OrderSiteType.student_home_ost;
        }
        else if (ctx.getString(R.string.site_type_teacher_home).equals(typeValueString)) {
            return OrderCommonEnum.OrderSiteType.teacher_home_ost;
        }
        else if (ctx.getString(R.string.site_type_third_home).equals(typeValueString)) {
            return OrderCommonEnum.OrderSiteType.thirdpartplace_ost;
        }
        else if (ctx.getString(R.string.site_type_online).equals(typeValueString)) {
            return OrderCommonEnum.OrderSiteType.live_ost;
        }
        else {
            return -1;
        }
    }

    public static boolean isGrouponCourse(int type) {
        return type == OrderCommonEnum.TeacherCoursePriceType.group_two_teacher_course_price_type
                || type == OrderCommonEnum.TeacherCoursePriceType.two_teacher_course_price_type
                || type == OrderCommonEnum.TeacherCoursePriceType.group_three_teacher_course_price_type
                || type == OrderCommonEnum.TeacherCoursePriceType.three_teacher_course_price_type
                || type == OrderCommonEnum.TeacherCoursePriceType.group_four_teacher_course_price_type
                || type == OrderCommonEnum.TeacherCoursePriceType.group_five_teacher_course_price_type;
    }

}

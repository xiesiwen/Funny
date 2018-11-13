package com.qingqing.project.offline.order.v2;

import android.os.Parcel;

import com.qingqing.api.proto.v1.OrderCommonEnum;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * 封装订单相关计算逻辑
 *
 * Created by tanwei on 2017/1/17.
 */

public class OrderLogic {

    /** 以10位单位向上取整 */
    public static int ceilByTen(double num) {
        if (num > 0) {
            
            final double floor = Math.floor(num);
            String numStr = String.valueOf((int) floor);
            if (num - floor > 0 || numStr.charAt(numStr.length() - 1) > '0') {
                StringBuilder sb = new StringBuilder(numStr);
                sb.setCharAt(numStr.length() - 1, '0');
                return Integer.parseInt(sb.toString()) + 10;
            }
            else {
                return (int) floor;
            }
            
        }
        else {
            return 0;
        }
    }

    /**
     * 计算不同朋友团类型对应的人数
     *
     * @param groupType OrderCommonEnum.TeacherCoursePriceType中的朋友团类型
     * @return 如果非朋友团返回1
     */
    public static int getCountOfGroupType(int groupType) {
        int count = 1;
        switch (groupType) {
            case OrderCommonEnum.TeacherCoursePriceType.group_two_teacher_course_price_type:
                count = 2;
                break;
            case OrderCommonEnum.TeacherCoursePriceType.group_three_teacher_course_price_type:
                count = 3;
                break;
            case OrderCommonEnum.TeacherCoursePriceType.group_four_teacher_course_price_type:
                count = 4;
                break;
            case OrderCommonEnum.TeacherCoursePriceType.group_five_teacher_course_price_type:
                count = 5;
                break;
        }

        return count;
    }

    /** 保存续课时部分可变参数（课次，时长，时间，上门方式等） */
    public static void storeOrderParamsForRenew(OrderParams src, OrderParams dst){
        dst.setSiteType(src.getSiteType());
        dst.setStudentAddressId(src.getStudentAddressId());
        dst.setCourseCount(src.getCourseCount());
        dst.setCourseLength(src.getCourseLength());
        dst.setTimeList(src.getTimeList());
        // 考虑到可能会存在续课信息不存在，需要保存gradeId，在返回时刷新
        dst.setGradeId(src.getGradeId());
    }

    /**
     * <p>复制下单参数{@linkplain OrderParams}对象（继承Parcelable的类可以类似复制）</p>
     *
     * <b>反序列化存在bug</b>
     */
    @Deprecated
    public static OrderParams deepClone(OrderParams params) {
        Parcel parcel = Parcel.obtain();
        params.writeToParcel(parcel, 0);
        OrderParams newParams = OrderParams.CREATOR.createFromParcel(parcel);
        return newParams;
    }

    /** 深层复制对象（需要实现Serializable接口） */
    public static <T extends Serializable> T deepClone(T src) {
        ByteArrayOutputStream memoryBuffer = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        T dist = null;
        try {
            out = new ObjectOutputStream(memoryBuffer);
            out.writeObject(src);
            out.flush();
            in = new ObjectInputStream(
                    new ByteArrayInputStream(memoryBuffer.toByteArray()));
            dist = (T) in.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (out != null)
                try {
                    out.close();
                    out = null;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            if (in != null)
                try {
                    in.close();
                    in = null;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
        }
        return dist;
    }
}

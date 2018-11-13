package com.qingqing.project.offline.order.v2;

import com.qingqing.api.proto.v1.OrderCommonEnum;
import com.qingqing.api.proto.v1.UserProto;
import com.qingqing.api.proto.v1.coursecontentpackage.CourseContentPackageProto;
import com.qingqing.base.config.LogicConfig;
import com.qingqing.base.data.DefaultDataCache;
import com.qingqing.base.log.Logger;
import com.qingqing.base.view.ToastWrapper;
import com.qingqing.project.offline.R;
import com.qingqing.project.offline.order.OrderCourseUtil;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

/**
 * 下单界面封装展示内容包信息的view
 *
 * Created by tanwei on 2017/1/18.
 */

public class OrderContentPackInfoHolder {
    
    private TextView packName, courseType, nick, summary, price;
    
    public void initView(View teacherView) {
        packName = (TextView) teacherView
                .findViewById(R.id.layout_content_pack_info_name);
        
        courseType = (TextView) teacherView
                .findViewById(R.id.layout_content_pack_info_course_type);
        nick = (TextView) teacherView.findViewById(R.id.layout_content_pack_info_nick);
        summary = (TextView) teacherView
                .findViewById(R.id.layout_content_pack_info_summary);
        price = (TextView) teacherView.findViewById(R.id.layout_content_pack_info_price);
    }
    
    public void updateContentPackInfo(Context context, OrderParams mParams) {
        
        UserProto.SimpleUserInfoV2 teacherInfo = mParams.getTeacherInfo();
        
        String group = OrderCourseUtil.getGroup(context, mParams.getCourseType());
        if (group == null) {
            courseType.setVisibility(View.GONE);
        }
        else {
            courseType.setVisibility(View.VISIBLE);
            courseType.setText(group);
        }
        
        nick.setText(teacherInfo.nick);
        
        final CourseContentPackageProto.CourseContentPackageForOrder contentPack = mParams
                .getSelectedContentPack();
        if (contentPack == null) {
            Logger.v("content pack not exists " + mParams.getContentPackId());
            ToastWrapper.show(R.string.text_content_pack_not_exists);
            ((Activity) context).finish();
            return;
        }
        
        packName.setText(contentPack.name);
        
        final int count = mParams.getCourseCount();
        final float totalHours = count * mParams.getCourseLength();
        double totalAmount = totalHours * mParams.getUnitCoursePrice();
        
        // 资料费
        if (contentPack.discountType == OrderCommonEnum.DiscountType.official_content_package_discount_type) {
            final CourseContentPackageProto.CourseContentPackagePriceForOrder packPrice = mParams
                    .getSelectedContentPackPrice(contentPack);
            if (packPrice != null && packPrice.materialPrice > 0) {
                totalAmount += packPrice.materialPrice;
            }
        }
        
        DefaultDataCache cache = DefaultDataCache.INSTANCE();
        String des = context.getString(R.string.text_format_3_str,
                cache.getCourseNameById(mParams.getCourseId()),
                cache.getGradeNameById(mParams.getGradeId()),
                context.getString(R.string.text_content_pack_time_info_format2, count,
                        LogicConfig.getFormatDotString(totalHours)));
        summary.setText(des);

        //如果有首课优惠,目前仅家长端支持
        if(mParams.isFirstCourseDiscount()){
            double d = OrderCourseUtil.getDiscountAmount(
                    mParams.getCourseLength(), mParams.getUnitCoursePrice(),
                    mParams.getFirstCourseDiscountRate());
            totalAmount -= d;
        }

        // setPrice(context, totalAmount, isTotal);
        this.price.setText(context.getString(R.string.text_format_amount_total,
                LogicConfig.getFormatDotString(totalAmount)));
    }
}

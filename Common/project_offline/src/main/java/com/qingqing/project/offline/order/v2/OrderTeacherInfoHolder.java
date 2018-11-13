package com.qingqing.project.offline.order.v2;

import android.app.Activity;
import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.widget.TextView;

import com.qingqing.api.proto.v1.GradeCourseProto;
import com.qingqing.api.proto.v1.OrderCommonEnum;
import com.qingqing.api.proto.v1.UserProto;
import com.qingqing.api.proto.v1.app.AppCommon;
import com.qingqing.api.proto.v1.coursecontentpackage.CourseContentPackageProto;
import com.qingqing.api.proto.v1.coursepackage.CoursePackageProto;
import com.qingqing.base.config.LogicConfig;
import com.qingqing.project.offline.constant.CourseConstant;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.data.DefaultDataCache;
import com.qingqing.base.log.Logger;
import com.qingqing.base.utils.ImageUrlUtil;
import com.qingqing.base.view.AsyncImageViewV2;
import com.qingqing.base.view.ToastWrapper;
import com.qingqing.project.offline.R;
import com.qingqing.project.offline.order.OrderCourseUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 下单界面封装展示老师信息的view
 *
 * Created by tanwei on 2016/9/21.
 */
public class OrderTeacherInfoHolder {
    
    private AsyncImageViewV2 avatar;
    
    private TextView summary, courseType,freeCourse, nick, price, packet;
    
    public void initView(View teacherView) {
        avatar = (AsyncImageViewV2) teacherView
                .findViewById(R.id.layout_teacher_info_avatar);
        summary = (TextView) teacherView.findViewById(R.id.layout_teacher_info_summary);
        courseType = (TextView) teacherView
                .findViewById(R.id.layout_teacher_info_course_type);
        freeCourse = (TextView) teacherView.findViewById(R.id.tv_online_free);
        nick = (TextView) teacherView.findViewById(R.id.layout_teacher_info_nick);
        price = (TextView) teacherView.findViewById(R.id.layout_teacher_info_price);
        packet = (TextView) teacherView.findViewById(R.id.layout_teacher_info_packet_des);
    }
    
    public void updateTeacherInfo(Context context, OrderParams params) {

        // 老师端显示家长信息 @5.7.5
        //老师端显示家长头像和昵称@5.8.0
        if (BaseData.getClientType() == AppCommon.AppType.qingqing_teacher) {

            if (params.getStudentInfoList().size() > 0) {
                UserProto.SimpleUserInfoV2 studentInfo = params.getStudentInfoList().get(0);
                avatar.setImageUrl(ImageUrlUtil.getHeadImg(studentInfo),
                        LogicConfig.getDefaultHeadIcon(studentInfo));
                nick.setText(studentInfo.nick);
            }

        }else{

            UserProto.SimpleUserInfoV2 teacherInfo = params.getTeacherInfo();

            avatar.setImageUrl(ImageUrlUtil.getHeadImg(teacherInfo),
                    LogicConfig.getDefaultHeadIcon(teacherInfo));
            nick.setText(teacherInfo.nick);

        }

        DefaultDataCache cache = DefaultDataCache.INSTANCE();
        double unitPrice = 0;// 优惠包和内容包表示总价
        
        String group = OrderCourseUtil.getGroup(context, params.getCourseType());
        if (group == null) {
            courseType.setVisibility(View.GONE);
        }
        else {
            courseType.setVisibility(View.VISIBLE);
            courseType.setText(group);
        }

        freeCourse.setVisibility(params.isFree() ? View.VISIBLE : View.GONE);
        if (params.getPacketId() > 0) {
            
            // 不显示上门方式
            summary.setText(cache.getCourseNameById(params.getCourseId()) + " "
                    + cache.getGradeNameById(params.getGradeId()));
            
            packet.setVisibility(View.VISIBLE);
            // 优惠包信息
            CoursePackageProto.CoursePackageUnits units = params.getPacketById();
            
            if (units == null) {
                Logger.v("favourable pack not exists " + params.getPacketId());
                ToastWrapper.show(R.string.text_favourable_pack_not_exists);
                ((Activity) context).finish();
                return;
            }
            
            CoursePackageProto.CoursePackageUnit unit;
            
            // 价格信息
            final GradeCourseProto.GradeCoursePriceInfoV2 newPriceInfo = params
                    .getSelectedNewPriceInfo();
            GradeCourseProto.CourseUnitPrice priceInfo;
            if (newPriceInfo != null) {
                priceInfo = newPriceInfo.priceInfo;
            }
            else {
                priceInfo = params.getSelectedCourseGradePriceInfo().priceInfo;
            }
            double price = params.calUnitPriceForPacket(priceInfo);
            
            switch (units.packageType) {
                
                case CoursePackageProto.CoursePackageType.charge_offline_free_offline_cpt:
                case CoursePackageProto.CoursePackageType.charge_online_free_online_cpt:
                    unit = units.packageUnits[0];
                    unitPrice = price * unit.chargeCourseCount * 2;
                    break;
                
                case CoursePackageProto.CoursePackageType.charge_offline_free_online_cpt:
                    unit = units.packageUnits[0];
                    unitPrice = price * unit.chargeCourseCount * 2;
                    break;
                
                case CoursePackageProto.CoursePackageType.composite_charge_offline_online_cpt:
                    int chargeCountOnline = 0;
                    int chargeCountOffline = 0;
                    for (CoursePackageProto.CoursePackageUnit packetUnit : units.packageUnits) {
                        switch (packetUnit.chargeContactType) {
                            case OrderCommonEnum.OrderSiteTypeContactType.online_ostct:
                                chargeCountOnline += packetUnit.chargeCourseCount;
                                break;
                            
                            case OrderCommonEnum.OrderSiteTypeContactType.offline_ostct:
                                chargeCountOffline += packetUnit.chargeCourseCount;
                                break;
                        }
                    }

                    unitPrice = price * chargeCountOffline * 2
                            + priceInfo.priceForLiving * chargeCountOnline;

                    break;
            }

            //如果有首课优惠,目前仅家长端支持
            if(params.isFirstCourseDiscount()){
                double d = OrderCourseUtil.getDiscountAmount(
                        params.getCourseLength(), params.getUnitCoursePrice(),
                        params.getFirstCourseDiscountRate());
                unitPrice -= d;
            }
            String title, des;
            if (BaseData.getClientType() == AppCommon.AppType.qingqing_teacher) {
                title = context.getString(R.string.text_favourable_packet);
            }
            else {
                title = context.getString(R.string.title_course_packet);
            }
            des = units.name;
            packet.setText(title + ":" + des);
        }
        else if (params.getContentPackId() > 0) {
            final CourseContentPackageProto.CourseContentPackageForOrder contentPack = params
                    .getSelectedContentPack();
            
            // packet.initText(context.getString(R.string.title_content_packet)
            // + ":"
            // + contentPack.name);
            // packet.initText(contentPack.name);
            packet.setVisibility(View.GONE);
            
            unitPrice = params.getCourseCount() * params.getCourseLength()
                    * params.getUnitCoursePrice();
            // 资料费
            if (contentPack.discountType == OrderCommonEnum.DiscountType.official_content_package_discount_type) {
                final CourseContentPackageProto.CourseContentPackagePriceForOrder packPrice = params
                        .getSelectedContentPackPrice(contentPack);
                if (packPrice != null && packPrice.materialPrice > 0) {
                    unitPrice += packPrice.materialPrice;
                }
            }
            summary.setText(contentPack.name);

        }
        else {
            packet.setVisibility(View.GONE);
        }
        
        // 续课的年级或者上门方式不存在，隐藏年级和价格
        if (params.getGradeId() < 0 || params.getSiteType() < 0) {
            summary.setText(cache.getCourseNameById(params.getCourseId()));
            price.setVisibility(View.GONE);
        }
        else {
            if (params.getPacketId() == 0 && params.getContentPackId() == 0) {
                summary.setText(context.getString(R.string.text_format_3_str,
                        cache.getCourseNameById(params.getCourseId()),
                        cache.getGradeNameById(params.getGradeId()),
                        CourseConstant.getSiteType(params.getSiteType())));
            }
            
            if (!params.isFree()) {
                boolean isTotal = false;
                if (params.getPacketId() > 0) {
                    isTotal = true;
                }
                else if (params.getContentPackId() > 0) {
                    isTotal = true;
                }
                else {
                    unitPrice = params.getUnitCoursePrice();
                }
                setPrice(context, unitPrice, isTotal);
            }else{
                SpannableString spannableString = new SpannableString("试听价￥0/小时");
                spannableString.setSpan(new RelativeSizeSpan(1.5f), 4, 5,
                        Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                this.price.setText(spannableString);
            }

        }
    }

    private void setPrice(Context context, double price, boolean isTotal) {
        String priceStr = LogicConfig.getFormatDotString(price);
        String string;
        if (isTotal) {
            string = context.getString(R.string.text_format_amount, priceStr);
        }
        else {
            string = context.getString(R.string.text_format_price, priceStr);
        }
        Pattern pattern = Pattern.compile(priceStr);
        Matcher matcher = pattern.matcher(string);
        if (matcher.find()) {
            int start = matcher.start();
            int end = start + priceStr.length();
            SpannableString spannableString = new SpannableString(string);
            spannableString.setSpan(new RelativeSizeSpan(1.5f), start, end,
                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            this.price.setText(spannableString);
        }
        else {
            this.price.setText(string);
        }
    }
    
    private void updateStudentInfo(OrderParams params){//TODO:
        UserProto.SimpleUserInfoV2 studentInfo;
        // 做下判断，如果取不到学生信息（理论上不会出现），还是显示老师信息（毕竟显示了好多版本了）
        if (params.getStudentInfoList().size() > 0) {
            studentInfo = params.getStudentInfoList().get(0);
        }
        else {
            studentInfo = params.getTeacherInfo();
        }

        avatar.setImageUrl(ImageUrlUtil.getHeadImg(studentInfo),
                LogicConfig.getDefaultHeadIcon(studentInfo));

        DefaultDataCache cache = DefaultDataCache.INSTANCE();
        summary.setText(cache.getCourseNameById(params.getCourseId()));

        nick.setText(studentInfo.nick);

        courseType.setVisibility(View.GONE);
        packet.setVisibility(View.GONE);
        price.setVisibility(View.GONE);
    }
}

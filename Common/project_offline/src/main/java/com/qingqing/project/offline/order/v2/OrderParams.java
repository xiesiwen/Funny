package com.qingqing.project.offline.order.v2;

import android.os.Parcel;
import android.os.Parcelable;

import com.qingqing.api.proto.v1.Geo;
import com.qingqing.api.proto.v1.GradeCourseProto;
import com.qingqing.api.proto.v1.OrderCommonEnum;
import com.qingqing.api.proto.v1.OrderDetail;
import com.qingqing.api.proto.v1.UserProto;
import com.qingqing.api.proto.v1.coursecontentpackage.CourseContentPackageProto;
import com.qingqing.api.proto.v1.coursepackage.CoursePackageProto;
import com.qingqing.base.log.Logger;
import com.qingqing.project.offline.constant.CourseConstant;
import com.qingqing.project.offline.seltime.TimeSlice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 下单相关参数v2  5.3重新整理
 *
 * Created by tanwei on 2016/9/20.
 */
public class OrderParams implements Parcelable {

    private String teacherId, studentId;// 老师、家长的id

    private UserProto.SimpleUserInfoV2 teacherInfo;// 老师信息

    private List<UserProto.SimpleUserInfoV2> studentInfoList;// 家长信息列表

    private int createType, courseType, siteType;// 几个常用枚举类型：订单来源、订单课程类型、上门方式

    private int courseId, gradeId;// 选择的科目 年级

    private List<GradeCourseProto.TeacherCoursePrice> teacherCoursePriceList;// 老师课程信息列表

    private boolean isFree;// 免费课

    private String address;// 上课地址

    private String orderId, groupOrderId;// 订单id，团课订单id

    private ArrayList<TimeSlice> timeList;// 选择的上课时间段

    private double thirdSitePrice;// 第三方场地费用（如果是使用第三方场地）

    private long thirdSiteId;// 第三方场地id（如果是使用第三方场地）

    private long studentAddressId;// 家长的常用地址id

    private double unitCoursePrice;// 订单的课程单价

    // ---------选择奖学券相关变量------------

    private double couponsAmount;// 奖学券金额

    private int couponId;// 奖学券id

    private long[] couponIdList;//优惠券id列表

    private int couponsCount;// 奖学券数量

    // ---------5.3.5新增下单、续课次数相关变量------------

    private OrderDetail.OrderCourseCountConfig[] courseCountConfig;// 下单、续课（最大、最小、默认）次数配置

    // ---------5.4.0新增课程包相关变量------------

    private long coursePacketId;// 课程包id

    private List<CoursePackageProto.CoursePackageUnits> packetList;// 课程包列表

    // ---------5.4.5新增续课时老师最新价格（默认的为老师上次订单价格）------------

    private GradeCourseProto.TeacherCoursePrice teacherNewPriceInfo;

    // ---------5.5.0新增内容包------------

    private long contentPackRelationId;// 内容包id

    private List<CourseContentPackageProto.CourseContentPackageForOrder> contentPackList;// 内容包列表

    // ---------5.5.5新增官方内容包------------

    private List<CourseContentPackageProto.CourseContentPackageForOrder> officialPackList;// 官方内容包列表

    private int[] reasonList; //选择老师的原因列表

    private String groupOrderCourseId;// 为支持调课添加group课程id

    // ---------5.7.5新增------------
    private boolean isPriceChanged;// 续课价格是否更改

    private boolean isGradeChanged;// 续课年级是否更改

    // ---------其他相关变量------------

    private String teacherAddress;// 缓存老师地址

    private String studentAddress;// 缓存学生地址

    private Geo.GeoPoint teacherGeoPoint;// 老师地址经纬度

    private int courseCount;// 选择的课次

    private float courseLength;// 单次课程时长，单位小时

    private double newUnitPrice;// 改价后的新单价

    private double urgentChangeAmountOfStudent;//6.1 学生职责调课惩罚金额
    private double urgentChangeAmountOfTeacher;//6.1 老师职责调课惩罚金额
    private boolean isWinterPackage;//6.2 是否为创建寒假包
    private boolean isFirstCourseDiscount = false;//是有享有首课优惠
    private int firstCourseDiscountRate;//首课优惠优惠比例，75 代表7.5折

    public OrderParams(){
        studentInfoList = new ArrayList<>();
        teacherCoursePriceList = new ArrayList<>();
        timeList = new ArrayList<>();
        courseCountConfig = new OrderDetail.OrderCourseCountConfig[2];
        packetList = new ArrayList<>();
        contentPackList = new ArrayList<>();
        officialPackList = new ArrayList<>();
        reasonList = new int[]{};
        couponIdList = new long[]{};
    }

    // ---------封装部分逻辑计算------------

    /** 获取选择的科目年级价格信息 不存在返回null */
    public GradeCourseProto.GradeCoursePriceInfoV2 getSelectedCourseGradePriceInfo(){
        for(GradeCourseProto.TeacherCoursePrice coursePrice : teacherCoursePriceList){
            if(courseType == coursePrice.priceType){
                for(GradeCourseProto.GradeCoursePriceInfoV2 info : coursePrice.priceInfos){
                    if(courseId == info.gradeCourse.courseId && gradeId == info.gradeCourse.gradeId){
                        return info;
                    }
                }
                break;
            }
        }

        return null;
    }

    /** 获取选择的课程类型对应的信息 不存在返回null */
    public GradeCourseProto.TeacherCoursePrice getSelectedCoursePrice(){

        for(GradeCourseProto.TeacherCoursePrice coursePrice : teacherCoursePriceList){
            if(courseType == coursePrice.priceType){
                return coursePrice;
            }
        }

        return null;
    }

    /** 获取老师支持的所有课程类型 不存在返回null */
    public ArrayList<Integer> getCourseTypeList() {
        if (teacherCoursePriceList.size() > 0) {
            ArrayList<Integer> courseTypeList = new ArrayList<>();

            for (GradeCourseProto.TeacherCoursePrice coursePrice : teacherCoursePriceList) {
                courseTypeList.add(coursePrice.priceType);
            }

            // 排序
            Collections.sort(courseTypeList);

            return courseTypeList;
        }
        else {
            return null;
        }
    }

    /** 获取选择的课程类型对应的所有年级 不存在返回null */
    public ArrayList<Integer> getGradeListOfSelectedCourseType() {
        GradeCourseProto.TeacherCoursePrice info = getSelectedCoursePrice();
        if (info != null) {
            ArrayList<Integer> gradeList = new ArrayList<>();

            for (GradeCourseProto.GradeCoursePriceInfoV2 gradeCourse : info
                    .priceInfos) {
                gradeList.add(gradeCourse.gradeCourse.gradeId);
            }

            return gradeList;
        }

        return null;
    }

    /** 获取选择的课程类型和年级对应的所有上门方式 不存在返回null */
    public ArrayList<Integer> getSiteTypeListOfSelectedGrade() {
        GradeCourseProto.GradeCoursePriceInfoV2 info = getSelectedCourseGradePriceInfo();
        if (info != null) {
            ArrayList<Integer> siteTypeList = new ArrayList<>();

            GradeCourseProto.CourseUnitPrice priceInfo = info.priceInfo;
            if (priceInfo.priceToStudentHome > 0) {
                siteTypeList.add(OrderCommonEnum.OrderSiteType.student_home_ost);
            }

            if (priceInfo.priceToTeacherHome > 0) {
                siteTypeList.add(OrderCommonEnum.OrderSiteType.teacher_home_ost);
            }
            if (priceInfo.priceForLiving > 0) {
                siteTypeList.add(OrderCommonEnum.OrderSiteType.live_ost);
            }

            return siteTypeList;
        }

        return null;
    }

    /** 计算课程单价 */
    public void calUnitPrice(){
        GradeCourseProto.GradeCoursePriceInfoV2 info = getSelectedCourseGradePriceInfo();

        if(info != null){
            calUnitPrice(info.priceInfo);
        }else{
            Logger.w("sel course grade not exists");
            unitCoursePrice = 0;
        }
    }

    /** 计算课程单价 */
    public void calUnitPrice(GradeCourseProto.CourseUnitPrice unitPrice) {
        unitCoursePrice = calUnitPriceForPacket(unitPrice);
    }

    /** 续课课程类型老师是否支持 */
    public boolean isRenewCourseTypeExists(){
        for (GradeCourseProto.TeacherCoursePrice coursePrice : teacherCoursePriceList) {
            if(coursePrice.priceType == courseType){
                return true;
            }
        }

        return false;
    }

    /** 老师是否支持朋友团 */
    public boolean isTeacherSupportFriendGroup() {
        
        boolean isTeacherSupportFriendGroup = false;
        
        // 根据老师开课信息判断是否支持朋友团拼课
        for (GradeCourseProto.TeacherCoursePrice coursePrice : teacherCoursePriceList) {
            if (CourseConstant.isGrouponCourse(coursePrice.priceType)) {
                isTeacherSupportFriendGroup = true;
                break;
            }
        }
        
        return isTeacherSupportFriendGroup;
    }

    /** 获取总课时：单位小时 */
    public float getTotalTimeInHour() {
        float hours = 0;
        for (TimeSlice time : timeList) {
            hours += time.getTimeInHour();
        }
        
        return hours;
    }

    /** 是否为朋友团续课 */
    public boolean isGroupOrderV2() {
        switch (courseType) {
            case OrderCommonEnum.TeacherCoursePriceType.group_two_teacher_course_price_type:
            case OrderCommonEnum.TeacherCoursePriceType.group_three_teacher_course_price_type:
            case OrderCommonEnum.TeacherCoursePriceType.group_four_teacher_course_price_type:
            case OrderCommonEnum.TeacherCoursePriceType.group_five_teacher_course_price_type:
                return true;
        }

        return false;
    }

    public int getOrderType(){
        int orderType;
        if (isGroupOrderV2()) {
            orderType = OrderCommonEnum.OrderType.group_order_type;
        }
        else if (getCourseType() == OrderCommonEnum.TeacherCoursePriceType.live_class_teacher_course_price_type) {
            orderType= OrderCommonEnum.OrderType.live_class_order_type;
        }
        else {
            orderType = OrderCommonEnum.OrderType.general_order_type;
        }
        return orderType;
    }

    /**
     * 获取当前选择的优惠包信息
     *
     * @return {@link CoursePackageProto.CoursePackageUnits}
     */
    public CoursePackageProto.CoursePackageUnits getPacketById() {
        for (CoursePackageProto.CoursePackageUnits units : packetList) {
            if (units.packageId == coursePacketId) {
                return units;
            }
        }

        return null;
    }

    /**
     * 获取当前年级支持的优惠包（判断上门方式是否满足优惠包）
     *
     * @return 当前选择的（科目）年级下对应可以选的优惠包
     */
    public List<CoursePackageProto.CoursePackageUnits> getSupportPacket() {
        List<CoursePackageProto.CoursePackageUnits> list = new ArrayList<>();
        final GradeCourseProto.GradeCoursePriceInfoV2 selectedNewPriceInfo = getSelectedNewPriceInfo();
        if (siteType == OrderCommonEnum.OrderSiteType.live_ost
                || selectedNewPriceInfo == null) {
            return list;
        }
        final GradeCourseProto.CourseUnitPrice priceInfo = selectedNewPriceInfo.priceInfo;
        double priceOffline = calUnitPriceForPacket(priceInfo);
        for (CoursePackageProto.CoursePackageUnits units : packetList) {
            switch (units.packageType) {
                case CoursePackageProto.CoursePackageType.charge_offline_free_offline_cpt:
                    if (priceOffline > 0) {
                        list.add(units);
                        continue;
                    }
                    break;
                
                case CoursePackageProto.CoursePackageType.charge_offline_free_online_cpt:
                case CoursePackageProto.CoursePackageType.composite_charge_offline_online_cpt:
                    if ((unitCoursePrice > 0) && priceInfo.priceForLiving > 0) {
                        list.add(units);
                        continue;
                    }
                    break;
            }
        }
        
        return list;
    }

    /**
     * 获取已选择的年级对应的老师最新的价格信息（续课时下单优惠包用到）
     *
     * <b>由于续课使用的上次课订单价格，老师改价了体现不出来，续课下单优惠包时，需要使用新价格</b>
     *
     * @return
     */
    public GradeCourseProto.GradeCoursePriceInfoV2 getSelectedNewPriceInfo() {
        if (teacherNewPriceInfo != null) {
            for (GradeCourseProto.GradeCoursePriceInfoV2 info : teacherNewPriceInfo.priceInfos) {
                if (courseId == info.gradeCourse.courseId
                        && gradeId == info.gradeCourse.gradeId) {
                    return info;
                }
            }
        }
        return null;
    }

    /** 计算老师单价 */
    public double calUnitPriceForPacket(GradeCourseProto.CourseUnitPrice unitPrice) {
        double price = 0;
        switch (siteType) {
            case OrderCommonEnum.OrderSiteType.student_home_ost:
                price = unitPrice.priceToStudentHome;
                break;
            case OrderCommonEnum.OrderSiteType.teacher_home_ost:
                price = unitPrice.priceToTeacherHome;
                break;
            case OrderCommonEnum.OrderSiteType.thirdpartplace_ost:
                price = unitPrice.priceToThirdPlace;
                break;
            case OrderCommonEnum.OrderSiteType.live_ost:
                price = unitPrice.priceForLiving;
                break;
        }
        
        return price;
    }

    /** 获取当前选择的内容包信息 */
    public CourseContentPackageProto.CourseContentPackageForOrder getSelectedContentPack() {
        for (CourseContentPackageProto.CourseContentPackageForOrder units : contentPackList) {
            if (units.contentPackageRelationId == contentPackRelationId) {
                return units;
            }
        }

        for (CourseContentPackageProto.CourseContentPackageForOrder units : officialPackList) {
            if (units.contentPackageRelationId == contentPackRelationId) {
                return units;
            }
        }

        return null;
    }

    /** 获取当前选择的内容包价格信息 */
    public CourseContentPackageProto.CourseContentPackagePriceForOrder getSelectedContentPackPrice(
            CourseContentPackageProto.CourseContentPackageForOrder units) {
        if (units != null) {
            for (CourseContentPackageProto.CourseContentPackagePriceForOrder price : units.price) {
                if (price.coursePriceType == courseType) {
                    return price;
                }
            }
        }
        return null;
    }

    /** 获取当前选择的内容包类型 */
    public int getContentPakcType() {
        final CourseContentPackageProto.CourseContentPackageForOrder contentPack = getSelectedContentPack();
        if (contentPack == null) {
            Logger.w("orderParams", "get sel content pack null " + contentPackRelationId);
            return OrderCommonEnum.DiscountType.content_package_discount_type;
        }
        else {
            
            return contentPack.discountType;
        }
    }

    // ---------封装部分逻辑计算 end------------

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {

        parcel.writeString(studentId);
        parcel.writeString(teacherId);
        parcel.writeParcelable(teacherInfo, flags);
        parcel.writeTypedList(studentInfoList);

        parcel.writeInt(createType);
        parcel.writeInt(courseType);
        parcel.writeInt(siteType);
        parcel.writeInt(courseId);
        parcel.writeInt(gradeId);

        parcel.writeTypedList(teacherCoursePriceList);
        parcel.writeTypedList(timeList);

        parcel.writeInt(isFree ? 1 : 0);
        parcel.writeString(address);

        parcel.writeString(orderId);
        parcel.writeString(groupOrderId);

        parcel.writeLong(thirdSiteId);
        parcel.writeLong(studentAddressId);
        parcel.writeDouble(thirdSitePrice);
        parcel.writeDouble(unitCoursePrice);

        parcel.writeInt(couponsCount);
        parcel.writeInt(couponId);
        parcel.writeLongArray(couponIdList);
        parcel.writeDouble(couponsAmount);

        parcel.writeTypedArray(courseCountConfig, flags);

        parcel.writeLong(coursePacketId);
        parcel.writeTypedList(packetList);
        parcel.writeParcelable(teacherNewPriceInfo, flags);
        parcel.writeLong(contentPackRelationId);
        parcel.writeTypedList(contentPackList);
        parcel.writeTypedList(officialPackList);
        parcel.writeIntArray(reasonList);
        parcel.writeString(groupOrderCourseId);

        parcel.writeInt(courseCount);
        parcel.writeFloat(courseLength);
        parcel.writeDouble(newUnitPrice);
        parcel.writeInt(isPriceChanged ? 1 : 0);
        parcel.writeInt(isGradeChanged ? 1 : 0);

        parcel.writeInt(isWinterPackage ? 1 : 0);
        parcel.writeInt(isFirstCourseDiscount ? 1 : 0);
        parcel.writeInt(firstCourseDiscountRate);
    }

    public static final Parcelable.Creator<OrderParams> CREATOR = new Creator<OrderParams>() {

        @Override
        public OrderParams createFromParcel(Parcel source) {
            
            OrderParams info = new OrderParams();
            
            info.studentId = source.readString();
            info.teacherId = source.readString();
            info.teacherInfo = source
                    .readParcelable(UserProto.SimpleUserInfoV2.class.getClassLoader());
            source.readTypedList(info.studentInfoList,
                    UserProto.SimpleUserInfoV2.CREATOR);
            
            info.createType = source.readInt();
            info.courseType = source.readInt();
            info.siteType = source.readInt();
            info.courseId = source.readInt();
            info.gradeId = source.readInt();
            
            source.readTypedList(info.teacherCoursePriceList,
                    GradeCourseProto.TeacherCoursePrice.CREATOR);
            source.readTypedList(info.timeList, TimeSlice.CREATOR);
            
            info.isFree = source.readInt() == 1;
            info.address = source.readString();

            info.orderId = source.readString();
            info.groupOrderId = source.readString();
            
            info.thirdSiteId = source.readLong();
            info.studentAddressId = source.readLong();
            info.thirdSitePrice = source.readDouble();
            info.unitCoursePrice = source.readDouble();

            info.couponsCount = source.readInt();
            info.couponId = source.readInt();
            info.couponIdList = source.createLongArray();
            info.couponsAmount = source.readDouble();

            info.courseCountConfig = source.createTypedArray(OrderDetail.OrderCourseCountConfig.CREATOR);

            info.coursePacketId = source.readLong();
            source.readTypedList(info.packetList,
                    CoursePackageProto.CoursePackageUnits.CREATOR);
            info.teacherNewPriceInfo = source.readParcelable(
                    GradeCourseProto.TeacherCoursePrice.class.getClassLoader());
            info.contentPackRelationId = source.readLong();
            source.readTypedList(info.contentPackList,
                    CourseContentPackageProto.CourseContentPackageForOrder.CREATOR);
            source.readTypedList(info.officialPackList,
                    CourseContentPackageProto.CourseContentPackageForOrder.CREATOR);
            info.reasonList = source.createIntArray();
            info.groupOrderCourseId = source.readString();

            info.courseCount = source.readInt();
            info.courseLength = source.readFloat();
            info.newUnitPrice = source.readDouble();
            info.isPriceChanged = source.readInt() == 1;
            info.isGradeChanged = source.readInt() == 1;

            info.isWinterPackage = source.readInt() == 1;
            info.isFirstCourseDiscount = source.readInt() == 1;
            info.firstCourseDiscountRate = source.readInt();

            return info;
        }

        @Override
        public OrderParams[] newArray(int size) {
            return new OrderParams[size];
        }
    };

    public void clear(){
        Logger.v("orderParam", "clear param");

        studentId = null;
        teacherId = null;
        teacherInfo = null;
        studentInfoList.clear();

        createType = OrderCommonEnum.OrderCreateType.mobile_search_oct;
        courseType = OrderCommonEnum.TeacherCoursePriceType.normal_course_price_type;
        siteType = OrderCommonEnum.OrderSiteType.student_home_ost;
        courseId = 0;
        gradeId = 0;

        teacherCoursePriceList.clear();
        timeList.clear();

        isFree = false;
        address = null;

        orderId = null;
        groupOrderId = null;

        thirdSiteId = 0;
        studentAddressId = 0;
        thirdSitePrice = 0;
        unitCoursePrice = 0;

        couponsAmount = 0;
        couponId = 0;
        couponIdList = null;
        couponsCount = 0;

        courseCountConfig = null;

        teacherAddress = null;
        studentAddress = null;

        teacherGeoPoint = null;
        teacherNewPriceInfo = null;

        packetList.clear();
        contentPackList.clear();
        officialPackList.clear();
        reasonList = null;
        groupOrderCourseId = null;
        isFirstCourseDiscount = false;
        firstCourseDiscountRate = 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("teacherId:");
        sb.append(teacherId);
        sb.append(",studentId:");
        sb.append(studentId);
        sb.append(",createType:");
        sb.append(createType);
        sb.append(",courseType:");
        sb.append(courseType);
        sb.append(",siteType:");
        sb.append(siteType);
        sb.append(",studentSize:");
        sb.append(studentInfoList.size());
        sb.append(",orderId:");
        sb.append(orderId);
        sb.append(",courseId:");
        sb.append(",couponList.size:");
        sb.append(couponIdList!=null?couponIdList.length:0);
        sb.append(courseId);
        sb.append(",gradeId:");
        sb.append(gradeId);
        sb.append(",contentPackRelationId:");
        sb.append(contentPackRelationId);
        sb.append(",coursePacketId:");
        sb.append(coursePacketId);
        sb.append(",time.size:");
        sb.append(timeList.size());
        sb.append(",reason.size:");
        sb.append(reasonList.length);
        sb.append(",firstCourseDiscountRate:");
        sb.append(firstCourseDiscountRate);
        return sb.toString();
    }

    public String getStudentId() {
        return studentId;
    }

    public OrderParams setStudentId(String studentId) {
        this.studentId = studentId;
        return this;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public OrderParams setTeacherId(String teacherId) {
        this.teacherId = teacherId;
        return this;
    }

    public UserProto.SimpleUserInfoV2 getTeacherInfo() {
        return teacherInfo;
    }

    public OrderParams setTeacherInfo(UserProto.SimpleUserInfoV2 teacherInfo) {
        this.teacherInfo = teacherInfo;
        return this;
    }

    public List<UserProto.SimpleUserInfoV2> getStudentInfoList() {
        return studentInfoList;
    }

    public OrderParams setStudentInfoList(List<UserProto.SimpleUserInfoV2> studentInfoList) {
        if(this.studentInfoList.size() > 0){
            this.studentInfoList.clear();
        }
        this.studentInfoList.addAll(studentInfoList);
        return this;
    }

    public void addStudentInfo(UserProto.SimpleUserInfoV2 info){

        studentInfoList.add(info);
    }

    public int getCreateType() {
        return createType;
    }

    public OrderParams setCreateType(int createType) {
        this.createType = createType;
        return this;
    }

    public int getCourseType() {
        return courseType;
    }

    public OrderParams setCourseType(int courseType) {
        this.courseType = courseType;
        return this;
    }

    public int getSiteType() {
        return siteType;
    }

    public OrderParams setSiteType(int siteType) {
        this.siteType = siteType;
        return this;
    }

    public int getCourseId() {
        return courseId;
    }

    public OrderParams setCourseId(int courseId) {
        this.courseId = courseId;
        return this;
    }

    public int getGradeId() {
        return gradeId;
    }

    public OrderParams setGradeId(int gradeId) {
        this.gradeId = gradeId;
        return this;
    }

    public List<GradeCourseProto.TeacherCoursePrice> getTeacherCoursePriceList() {
        return teacherCoursePriceList;
    }

    public OrderParams setTeacherCoursePriceList(
            List<GradeCourseProto.TeacherCoursePrice> teacherCoursePriceList) {
        if (teacherCoursePriceList == null) {
            this.teacherCoursePriceList.clear();
            return this;
        }
        else {
            if (this.teacherCoursePriceList.size() > 0) {
                this.teacherCoursePriceList.clear();
            }
            this.teacherCoursePriceList.addAll(teacherCoursePriceList);
        }
        
        return this;
    }

    public void addTeacherCoursePrice(GradeCourseProto.TeacherCoursePrice price){
        if(price != null){
            this.teacherCoursePriceList.add(price);
        }
    }

    public boolean isFree() {
        return isFree;
    }

    public OrderParams setFree(boolean free) {
        isFree = free;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public OrderParams setAddress(String address) {
        this.address = address;
        return this;
    }

    public String getOrderId() {
        return orderId;
    }

    public OrderParams setOrderId(String orderId) {
        this.orderId = orderId;
        return this;
    }

    public String getGroupOrderId() {
        return groupOrderId;
    }

    public OrderParams setGroupOrderId(String groupOrderId) {
        this.groupOrderId = groupOrderId;
        return this;
    }

    public ArrayList<TimeSlice> getTimeList() {
        return timeList;
    }

    public OrderParams setTimeList(ArrayList<TimeSlice> timeList) {
        if (timeList != null) {
            if (!this.timeList.isEmpty()) {
                this.timeList.clear();
            }
            
            this.timeList.addAll(timeList);
        }
        else {
            this.timeList.clear();
        }
        return this;
    }

    public double getThirdSitePrice() {
        return thirdSitePrice;
    }

    public OrderParams setThirdSitePrice(double thirdSitePrice) {
        this.thirdSitePrice = thirdSitePrice;
        return this;
    }

    public long getThirdSiteId() {
        return thirdSiteId;
    }

    public OrderParams setThirdSiteId(long thirdSiteId) {
        this.thirdSiteId = thirdSiteId;
        return this;
    }

    public long getStudentAddressId() {
        return studentAddressId;
    }

    public OrderParams setStudentAddressId(long studentAddressId) {
        this.studentAddressId = studentAddressId;
        return this;
    }

    public double getUnitCoursePrice() {
        return unitCoursePrice;
    }

    public OrderParams setUnitCoursePrice(double unitCoursePrice) {
        this.unitCoursePrice = unitCoursePrice;
        return this;
    }

    public double getCouponsAmount() {
        return couponsAmount;
    }

    public OrderParams setCouponsAmount(double couponsAmount) {
        this.couponsAmount = couponsAmount;
        return this;
    }

    public int getCouponId() {
        return couponId;
    }

    public OrderParams setCouponId(int couponId) {
        this.couponId = couponId;
        return this;
    }

    public long[] getCouponIdList(){
        return couponIdList;
    }

    public OrderParams setCouponIdList(long[] couponIdList){
        this.couponIdList = couponIdList;
        return this;
    }

    public int getCouponsCount() {
        return couponsCount;
    }

    public OrderParams setCouponsCount(int couponsCount) {
        this.couponsCount = couponsCount;
        return this;
    }

    public boolean isFirstCourseDiscount() {
        return isFirstCourseDiscount;
    }

    public void setFirstCourseDiscount(boolean firstCourseDiscount) {
        isFirstCourseDiscount = firstCourseDiscount;
    }

    public int getFirstCourseDiscountRate() {
        return firstCourseDiscountRate;
    }

    public void setFirstCourseDiscountRate(int firstCourseDiscountRate) {
        this.firstCourseDiscountRate = firstCourseDiscountRate;
    }

    public OrderDetail.OrderCourseCountConfig getCourseCountConfig() {
        for (OrderDetail.OrderCourseCountConfig config : courseCountConfig) {
            switch (config.orderType) {
                case OrderCommonEnum.OrderType.group_order_type:
                    
                    if (isGroupOrderV2()) {
                        return config;
                    }
                    
                    break;
                
                case OrderCommonEnum.OrderType.general_order_type:
                    if (!isGroupOrderV2()) {
                        return config;
                    }
                    break;
            }
            
        }
        
        return null;
    }

    public OrderParams setCourseCountConfig(
            OrderDetail.OrderCourseCountConfig[] courseCountConfig) {
        this.courseCountConfig = courseCountConfig;
        return this;
    }
    
    public OrderParams setCourseCountConfig(
            OrderDetail.OrderCourseCountConfig courseCountConfig) {
        this.courseCountConfig[0] = courseCountConfig;
        return this;
    }

    public String getTeacherAddress() {
        return teacherAddress;
    }

    public OrderParams setTeacherAddress(String teacherAddress) {
        this.teacherAddress = teacherAddress;
        return this;
    }

    public String getStudentAddress() {
        return studentAddress;
    }

    public OrderParams setStudentAddress(String studentAddress) {
        this.studentAddress = studentAddress;
        return this;
    }

    public Geo.GeoPoint getTeacherGeoPoint() {
        return teacherGeoPoint;
    }

    public OrderParams setTeacherGeoPoint(Geo.GeoPoint teacherGeoPoint) {
        this.teacherGeoPoint = teacherGeoPoint;
        return this;
    }

    public int getCourseCount() {
        return courseCount;
    }

    public OrderParams setCourseCount(int courseCount) {
        this.courseCount = courseCount;
        return this;
    }

    public float getCourseLength() {
        return courseLength;
    }

    public OrderParams setCourseLength(float courseLength) {
        this.courseLength = courseLength;
        return this;
    }

    public long getPacketId() {
        return coursePacketId;
    }

    public OrderParams setPacketId(long packetId) {
        this.coursePacketId = packetId;
        return this;
    }

    public List<CoursePackageProto.CoursePackageUnits> getPacketList() {
        return packetList;
    }

    public OrderParams setPacketList(
            List<CoursePackageProto.CoursePackageUnits> packetList) {
        if (this.packetList.size() > 0) {
            this.packetList.clear();
        }
        if (packetList != null && packetList.size() > 0) {
            this.packetList.addAll(packetList);
        }
        return this;
    }

    public void setTeacherNewPriceInfo(GradeCourseProto.TeacherCoursePrice teacherNewPriceInfo) {
        this.teacherNewPriceInfo = teacherNewPriceInfo;
    }

    public long getContentPackId() {
        return contentPackRelationId;
    }

    public void setContentPackId(long contentPackId) {
        this.contentPackRelationId = contentPackId;
    }

    public List<CourseContentPackageProto.CourseContentPackageForOrder> getContentPackList() {
        return contentPackList;
    }
    
    public OrderParams setContentPackList(
            List<CourseContentPackageProto.CourseContentPackageForOrder> contentPackList) {
        if (this.contentPackList.size() > 0) {
            this.contentPackList.clear();
        }
        if (contentPackList != null && contentPackList.size() > 0) {
            this.contentPackList.addAll(contentPackList);
        }
        return this;
    }
    
    public List<CourseContentPackageProto.CourseContentPackageForOrder> getOfficialPackList() {
        return officialPackList;
    }
    
    public OrderParams setOfficialPackList(
            List<CourseContentPackageProto.CourseContentPackageForOrder> officialPackList) {
        if (this.officialPackList.size() > 0) {
            this.officialPackList.clear();
        }
        if (officialPackList != null && officialPackList.size() > 0) {
            this.officialPackList.addAll(officialPackList);
        }
        return this;
    }

    public int[] getReasonList() {
        return reasonList;
    }

    public OrderParams setReasonList(int[] reasonList) {
        if (reasonList != null && reasonList.length >= 0) {
            this.reasonList = reasonList.clone();
        }
        return this;
    }

    public String getGroupOrderCourseId() {
        return groupOrderCourseId;
    }
    
    public OrderParams setGroupOrderCourseId(String groupOrderCourseId) {
        this.groupOrderCourseId = groupOrderCourseId;
        return this;
    }

    public double getNewUnitPrice() {
        return newUnitPrice;
    }

    public void setNewUnitPrice(double newUnitPrice) {

        this.newUnitPrice = newUnitPrice;
    }

    public boolean isPriceChanged() {
        return isPriceChanged;
    }

    public void setPriceChanged(boolean priceChanged) {
        isPriceChanged = priceChanged;
    }

    public boolean isGradeChanged() {
        return isGradeChanged;
    }

    public void setGradeChanged(boolean gradeChanged) {
        isGradeChanged = gradeChanged;
    }


    public double getUrgentChangeAmountOfStudent() {
        return urgentChangeAmountOfStudent;
    }

    public void setUrgentChangeAmountOfStudent(double urgentChangeAmountOfStudent) {
        this.urgentChangeAmountOfStudent = urgentChangeAmountOfStudent;
    }

    public double getUrgentChangeAmountOfTeacher() {
        return urgentChangeAmountOfTeacher;
    }

    public void setUrgentChangeAmountOfTeacher(double urgentChangeAmountOfTeacher) {
        this.urgentChangeAmountOfTeacher = urgentChangeAmountOfTeacher;
    }
    
    public boolean isWinterPackage() {
        return isWinterPackage;
    }
    
    public OrderParams setWinterPackage(boolean winterPackage) {
        isWinterPackage = winterPackage;
        return this;
    }
}

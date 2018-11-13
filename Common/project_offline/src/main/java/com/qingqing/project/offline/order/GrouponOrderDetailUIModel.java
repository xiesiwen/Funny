package com.qingqing.project.offline.order;

import android.content.Context;
import android.databinding.Bindable;
import android.text.Html;
import android.view.View;

import com.google.protobuf.nano.MessageNano;
import com.qingqing.api.proto.v1.OrderCommonEnum;
import com.qingqing.api.proto.v1.OrderDetail;
import com.qingqing.api.proto.v1.app.AppCommon;
import com.qingqing.api.proto.v1.course.OrderCourse;
import com.qingqing.api.proto.v1.order.Order;
import com.qingqing.base.time.NetworkTime;
import com.qingqing.base.config.CommonUrl;
import com.qingqing.base.config.LogicConfig;
import com.qingqing.base.core.CountDownCenter;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.http.HttpUrl;
import com.qingqing.base.http.req.ProtoListener;
import com.qingqing.qingqingbase.ui.BaseUIModel;
import com.qingqing.base.utils.ImageUrlUtil;
import com.qingqing.project.offline.BR;
import com.qingqing.project.offline.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by wangxiaxin on 2016/8/29.
 *
 * 朋友团的详情
 */
public class GrouponOrderDetailUIModel extends BaseUIModel {
    
    private String orderId;
    private String grouponOrderId;
    private Order.GroupOrderInfoDetailV2 grouponDetailInfo;
    private List<OrderCourse.OrderCourseInfoForOrderInfoDetail> courseTimeShowList = new ArrayList<>();
    private List<Order.GroupUserOrderInfo> memberList = new ArrayList<>();
    private static final int TIME_FOLD_COUNT = 4;
    private static final String TIME_TAG = "groupon_detail";
    private int leftSeconds;
    
    private String mShareUserId;
    
    @Bindable
    public boolean share;
    @Bindable
    public boolean reopenGroupon;
    @Bindable
    public boolean joinGroup;
    @Bindable
    public boolean payOrder;
    
    @Bindable
    public boolean showRemarkInd;
    
    @Bindable
    public boolean helpToPay;
    
    public GrouponOrderDetailUIModel(Context ctx) {
        super(ctx);
    }
    
    public GrouponOrderDetailUIModel setOrderId(String orderId) {
        this.orderId = orderId;
        return this;
    }
    
    public String getGrouponOrderId() {
        return grouponOrderId;
    }
    
    public boolean isStudentClient() {
        return BaseData.getClientType() == AppCommon.AppType.qingqing_student;
    }
    
    public String getTeacherId() {
        return grouponDetailInfo.teacherInfo.qingqingUserId;
    }
    

    public boolean isCoursePackOrContentPack(){
        return grouponDetailInfo.discountType == OrderCommonEnum.DiscountType.discount_package_discount_type || grouponDetailInfo.discountType == OrderCommonEnum.DiscountType.content_package_discount_type;
    }
    @Bindable
    public String getTeacherHeadUrl() {
        return ImageUrlUtil.getHeadImg(
                grouponDetailInfo != null ? grouponDetailInfo.teacherInfo : null);
    }
    
    @Bindable
    public int getTeacherDefaultHeadResId() {
        return LogicConfig.getDefaultHeadIcon(
                grouponDetailInfo != null ? grouponDetailInfo.teacherInfo : null);
    }
    
    @Bindable
    public String getOrderCourseTitle() {
        if (grouponDetailInfo == null) {
            return "";
        }
        else {
            if(grouponDetailInfo.courseContentPackageBrief != null && grouponDetailInfo.courseContentPackageBrief.name != null){
                return grouponDetailInfo.courseContentPackageBrief.name;
            }else{
                return grouponDetailInfo.gradeCourseInfo.courseName + " "
                        + grouponDetailInfo.gradeCourseInfo.gradeShortName;
            }
        }
    }

    @Bindable
    public String getCourseTitle() {
        if (grouponDetailInfo == null) {
            return "";
        }
        else {
            return grouponDetailInfo.gradeCourseInfo.courseName + " "
                    + grouponDetailInfo.gradeCourseInfo.gradeShortName;
        }
    }

    public long getDeadlineTime() {
        return grouponDetailInfo != null ? grouponDetailInfo.effectTime : 0;
    }
    
    @Bindable
    public String getRemarkContent() {
        if (grouponDetailInfo == null) {
            return "";
        }
        else {
            return grouponDetailInfo.remark;
        }
    }
    
    @Bindable
    public int getLeftSeconds() {
        return leftSeconds;
    }
    
    private void startCountDown() {
        if (CountDownCenter.INSTANCE().isDuringCountDown(TIME_TAG)) {
            CountDownCenter.INSTANCE().cancelTask(TIME_TAG);
        }
        
        if (getDeadlineTime() <= NetworkTime.currentTimeMillis()) {
            // 已超时
            leftSeconds = 0;
            grouponDetailInfo.groupOrderStatus = OrderCommonEnum.GroupOrderStatus.cancel_group_order_status;
            
            notifyPropertyChanged(BR.leftSeconds);
            updateUIWithStatus();
        }
        else {
            CountDownCenter.INSTANCE().addTask(TIME_TAG,
                    (int) ((getDeadlineTime() - NetworkTime.currentTimeMillis()) / 1000),
                    new CountDownCenter.CountDownListener() {
                        @Override
                        public void onCountDown(String tag, int leftCount) {
                            if (TIME_TAG.equals(tag)) {
                                leftSeconds = leftCount;
                                notifyPropertyChanged(BR.leftSeconds);
                                if (leftSeconds == 0) {
                                    grouponDetailInfo.groupOrderStatus = OrderCommonEnum.GroupOrderStatus.cancel_group_order_status;
                                    updateUIWithStatus();
                                }
                            }
                        }
                    });
        }
    }
    
    public void stopCountDown() {
        if (CountDownCenter.INSTANCE().isDuringCountDown(TIME_TAG)) {
            CountDownCenter.INSTANCE().cancelTask(TIME_TAG);
        }
    }
    
    /** 获取所有课次 */
    public int getTotalCourseCount() {
        return grouponDetailInfo != null ? grouponDetailInfo.orderCourses.length : 0;
    }
    
    @Bindable
    public boolean isGrouponDone() {
        return grouponDetailInfo != null
                && grouponDetailInfo.groupOrderStatus == OrderCommonEnum.GroupOrderStatus.made_up_group_order_status;
    }
    
    /** 朋友团是否进行中 */
    @Bindable
    public boolean isGrouponGoing() {
        return grouponDetailInfo == null
                || grouponDetailInfo.groupOrderStatus == OrderCommonEnum.GroupOrderStatus.wait_to_make_up_group_order_status;
    }
    
    @Bindable
    public CharSequence getMemberCountDown() {
        return (grouponDetailInfo != null
                && grouponDetailInfo.bookStudentCount < grouponDetailInfo.makeUpStudentCount)
                        ? (Html.fromHtml(
                                mCtx.getString(R.string.groupon_detail_member_count_down,
                                        grouponDetailInfo.makeUpStudentCount
                                                - grouponDetailInfo.bookStudentCount)))
                        : "";
    }
    
    /** 时间块是否可折叠 */
    @Bindable
    public boolean isCourseTimeFoldable() {
        return grouponDetailInfo != null
                && grouponDetailInfo.orderCourses.length > TIME_FOLD_COUNT;
    }
    
    /** 时间是否处于折叠状态 */
    @Bindable
    public boolean isCourseTimeFolded() {
        return grouponDetailInfo != null
                && grouponDetailInfo.orderCourses.length > courseTimeShowList.size();
    }
    
    @Bindable
    public String getGroupTypeString() {
        if (grouponDetailInfo == null) {
            return "";
        }
        else {
            return OrderCourseUtil.getGroup(mCtx, grouponDetailInfo.friendGroupType);
        }
    }
    
    public int getFriendGroupType() {
        if (grouponDetailInfo != null)
            return grouponDetailInfo.friendGroupType;
        return OrderCommonEnum.GroupOrderCourseStatus.unknown_group_order_course_status;
    }
    
    @Bindable
    public String getTeacherNick() {
        return grouponDetailInfo == null ? null : grouponDetailInfo.teacherInfo.nick;
    }
    
    @Bindable
    public String getCourseAddress() {
        if (grouponDetailInfo != null && grouponDetailInfo.orderModeUnits != null) {
            return grouponDetailInfo.orderModeUnits[0].address;
        }
        else {
            return null;
        }
    }

    @Bindable
    public boolean isContentPack(){
        if (grouponDetailInfo != null
                && grouponDetailInfo.courseContentPackageBrief != null
                && grouponDetailInfo.courseContentPackageBrief.name != null) {
            return true;
        }
        return false;
    }
    
    public boolean isSelfJoined(){
        if(isStudentClient()){
            Order.GroupUserOrderInfo orderInfo = getStudentSubOrder();
            return orderInfo != null;
        }
        return false;
    }

    /** 自己是否是已下单，未支付状态 */
    public boolean isSelfUnPay() {
        if (isStudentClient()) {
            Order.GroupUserOrderInfo orderInfo = getStudentSubOrder();
            return orderInfo != null
                    && orderInfo.userOrderStatus == OrderCommonEnum.GroupSubOrderStatus.wait_to_pay_group_user_order_status;
        }
        
        return false;
    }
    
    @Bindable
    public String getButtonText() {
        if (grouponDetailInfo == null)
            return null;
        else {
            int lack = grouponDetailInfo.makeUpStudentCount
                    - grouponDetailInfo.bookStudentCount;
            boolean going = isGrouponGoing();
            if (isStudentClient()) {
                if (going) {
                    // 家长端添加我要参团和支付
                    Order.GroupUserOrderInfo orderInfo = getStudentSubOrder();
                    if (orderInfo != null) {
                        if (orderInfo.userOrderStatus == OrderCommonEnum.GroupSubOrderStatus.wait_to_pay_group_user_order_status) {
                            return mCtx.getString(R.string.text_order_pay);
                        }
                        else {
                            return mCtx.getString(R.string.groupon_detail_share_default);
                        }
                    }
                    else {
                        if (lack > 0) {
                            return mCtx.getString(R.string.groupon_detail_join_group);
                        }
                        else {
                            return mCtx.getString(R.string.groupon_detail_share_default);
                        }
                    }
                }
                else {
                    return mCtx.getString(R.string.groupon_create_one);
                }
            }
            else if (going) {
                return mCtx.getString(R.string.groupon_detail_share_default_to_student);
            }
            
            return null;
        }
    }
    
    @Bindable
    public boolean isExistUnPayUser() {
        if (isStudentClient()) {
            if (grouponDetailInfo != null) {
                if (grouponDetailInfo.bookStudentCount == grouponDetailInfo.paidStudentCount
                        + 1) {
                    boolean isMeWaitToPay = false;
                    for (Order.GroupUserOrderInfo userInfo : grouponDetailInfo.waitToPayUserOrderInfos) {
                        if (BaseData.getSafeUserId()
                                .equals(userInfo.userInfo.qingqingUserId))
                            isMeWaitToPay = true;
                    }
                    
                    if (!isMeWaitToPay) {
                        boolean isMePaid = false;
                        for (Order.GroupUserOrderInfo userInfo : grouponDetailInfo.paiedUserOrderInfos) {
                            if (BaseData.getSafeUserId()
                                    .equals(userInfo.userInfo.qingqingUserId))
                                isMePaid = true;
                        }
                        return isMePaid;
                    }
                    else {
                        return false;
                    }
                }
                else {
                    return grouponDetailInfo.bookStudentCount > grouponDetailInfo.paidStudentCount;
                }
            }
        }
        
        return false;
    }
    
    public List<OrderCourse.OrderCourseInfoForOrderInfoDetail> getCourseTimeShowList() {
        return courseTimeShowList;
    }
    
    @Bindable
    public List<Order.GroupUserOrderInfo> getGrouponMemberList() {
        return memberList;
    }
    
    public int getTotalMemberCount() {
        return grouponDetailInfo != null ? grouponDetailInfo.makeUpStudentCount : 0;
    }
    
    public void operate(View v) {
        if (isGrouponGoing()) {
            // 家长端需要判断家长是否参团或者支付
            if (isStudentClient()) {

                String text = getButtonText();
                if(text.equals(mCtx.getString(R.string.groupon_detail_join_group))){
                    //我要参团
                    notifyPropertyChanged(BR.joinGroup);
                }else if(text.equals(mCtx.getString(R.string.text_order_pay))){
                    //去支付
                    if(isExistUnPayUser()){
                        payForOthers(v);
                    }else{
                        notifyPropertyChanged(BR.payOrder);
                    }
                }else{
                    notifyPropertyChanged(BR.share);
                }

//                if (going) {
//                    // 家长端添加我要参团和支付
//                    Order.GroupUserOrderInfo orderInfo = getStudentSubOrder();
//                    if (orderInfo != null) {
//                        if (orderInfo.userOrderStatus == OrderCommonEnum.GroupSubOrderStatus.wait_to_pay_group_user_order_status) {
//                            return mCtx.getString(R.string.text_order_pay);
//                        }
//                        else {
//                            return mCtx.getString(R.string.groupon_detail_share_default);
//                        }
//                    }
//                    else {
//                        if (lack > 0) {
//                            return mCtx.getString(R.string.groupon_detail_join_group);
//                        }
//                        else {
//                            return mCtx.getString(R.string.groupon_detail_share_default);
//                        }
//                    }
//                }
//                else {
//                    return mCtx.getString(R.string.groupon_create_one);
//                }


//                Order.GroupUserOrderInfo orderInfo = getStudentSubOrder();
//                if (orderInfo != null) {
//                    if (!isSelfJoinedButUnPay() &&isExistUnPayUser()) {
//                        payForOthers(v);
//                    }
//                    else if (orderInfo.userOrderStatus == OrderCommonEnum.GroupSubOrderStatus.wait_to_pay_group_user_order_status) {
//                        notifyPropertyChanged(BR.payOrder);
//                    }
//                    else {
//                        notifyPropertyChanged(BR.share);
//                    }
//                }
//                else {
//                    if (grouponDetailInfo.makeUpStudentCount > grouponDetailInfo.bookStudentCount) {
//                        notifyPropertyChanged(BR.joinGroup);
//                    }
//                    else {
//                        notifyPropertyChanged(BR.share);
//                    }
//                }
            }
            else {
                notifyPropertyChanged(BR.share);
            }
        }
        else {
            notifyPropertyChanged(BR.reopenGroupon);
        }
    }
    
    public void payForOthers(View v) {
        notifyPropertyChanged(BR.helpToPay);
    }
    
    // 获取当前家长的子订单信息，如果未参团则返回null
    public Order.GroupUserOrderInfo getStudentSubOrder() {
        final String userId = BaseData.qingqingUserId();
        for (Order.GroupUserOrderInfo orderInfo : memberList) {
            if (userId.equals(orderInfo.userInfo.qingqingUserId)) {
                return orderInfo;
            }
        }
        
        return null;
    }
    
    public void toggleTime(View v) {
        
        boolean isFolded = isCourseTimeFolded();
        courseTimeShowList.clear();
        
        if (isFolded) {
            courseTimeShowList.addAll(Arrays.asList(grouponDetailInfo.orderCourses));
        }
        else {
            courseTimeShowList.addAll(Arrays.asList(grouponDetailInfo.orderCourses)
                    .subList(0, TIME_FOLD_COUNT));
        }
        
        notifyPropertyChanged(BR.courseTimeFolded);
    }
    
    /** 把团长拎到第一个 */
    private void pickChiefMemberToFirst() {
        
        Order.GroupUserOrderInfo chiefMember = null;
        for (Order.GroupUserOrderInfo userInfo : memberList) {
            if (userInfo.isLeader) {
                chiefMember = userInfo;
                memberList.remove(userInfo);
                break;
            }
        }
        
        if (chiefMember != null) {
            memberList.add(0, chiefMember);
        }
    }
    
    private ProtoListener protoListenerForTeacher = new ProtoListener(
            Order.GroupOrderInfoDetailV2Response.class) {
        @Override
        public void onDealResult(Object result) {
            // 清空部分数据，避免多次刷新重复
            if (courseTimeShowList.size() > 0) {
                courseTimeShowList.clear();
            }
            if (memberList.size() > 0) {
                memberList.clear();
            }
            
            grouponDetailInfo = ((Order.GroupOrderInfoDetailV2Response) result).orderInfo;
            grouponOrderId = grouponDetailInfo.qingqingGroupOrderId;
            courseTimeShowList.addAll(
                    Arrays.asList(grouponDetailInfo.orderCourses).subList(0, Math.min(
                            TIME_FOLD_COUNT, grouponDetailInfo.orderCourses.length)));
            memberList.addAll(Arrays.asList(grouponDetailInfo.paiedUserOrderInfos));
            memberList.addAll(Arrays.asList(grouponDetailInfo.waitToPayUserOrderInfos));
            pickChiefMemberToFirst();
            
            if (BaseData.getClientType() == AppCommon.AppType.qingqing_teacher) {
                mShareUserId = grouponDetailInfo.leaderUserInfo.qingqingUserId;
            }
            else {
                mShareUserId = BaseData.getSafeUserId();
            }
            
            updateUI();
            if (isGrouponGoing()) {
                startCountDown();
            }
            else if (!isGrouponDone()) {
                leftSeconds = 0;
                notifyPropertyChanged(BR.leftSeconds);
            }
        }
    };
    
    public String getShareUserId() {
        return mShareUserId;
    }
    
    public void reqDetail() {
        
        MessageNano request;
        HttpUrl url;
        if (BaseData.getClientType() == AppCommon.AppType.qingqing_ta) {
            request = new OrderDetail.SimpleQingqingGroupOrderIdRequest();
            ((OrderDetail.SimpleQingqingGroupOrderIdRequest) request).qingqingGroupOrderId = orderId;
            url = CommonUrl.TA_GET_FRIEND_ORDER_DETAIL_V2_URL.url();
        }
        else {
            request = new OrderDetail.SimpleQingqingGroupOrderIdRequest();
            ((OrderDetail.SimpleQingqingGroupOrderIdRequest) request).qingqingGroupOrderId = orderId;
            url = CommonUrl.GROUPON_ORDER_DETAIL_URL.url();
        }
        ProtoListener listener = protoListenerForTeacher;
        newProtoReq(url).setSendMsg(request).setRspListener(listener).req();
    }
    
    private void updateUIWithStatus() {
        notifyPropertyChanged(BR.grouponGoing);
        notifyPropertyChanged(BR.grouponDone);
        notifyPropertyChanged(BR.buttonText);
        notifyPropertyChanged(BR.existUnPayUser);
    }
    
    private void updateUI() {
        notifyPropertyChanged(BR.orderCourseTitle);
        notifyPropertyChanged(BR.contentPack);
        notifyPropertyChanged(BR.courseTitle);
        notifyPropertyChanged(BR.teacherHeadUrl);
        notifyPropertyChanged(BR.memberCountDown);
        notifyPropertyChanged(BR.courseAddress);
        notifyPropertyChanged(BR.teacherNick);
        notifyPropertyChanged(BR.groupTypeString);
        notifyPropertyChanged(BR.courseTimeFolded);
        notifyPropertyChanged(BR.courseTimeFoldable);
        notifyPropertyChanged(BR.grouponMemberList);
        notifyPropertyChanged(BR.remarkContent);
        updateUIWithStatus();
        notifyPropertyChanged(BR.showRemarkInd);
    }
}

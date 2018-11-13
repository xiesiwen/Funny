package com.qingqing.base.news;

import com.qingqing.api.proto.v1.msg.Mqtt;

/**
 * Created by huangming on 2016/11/29.
 *
 * 消息会话类型
 */

public enum NewsConversationType {
    
    /**
     * 待办事项消息
     */
    TODO("待办事项") {
        @Override
        protected int[] createNewsTypeRange() {
            // ?????????待结课的课程
            
            return new int[] {
                    // 8: 待支付订单（老师发起的续课订单，TA发起的订单）， 老师头像， 跳转订单详情页面， 红点点击对应消息后消失
                    Mqtt.StudentMsgType.s_need_to_pay_msg_type,
                    
                    // 229: 待支付订单（团课）， 老师头像， 跳转订单详情页面， 红点点击对应消息后消失
                    Mqtt.StudentMsgType.s_new_add_sub_group_order };
        }
    },
    
    /**
     * 通知消息
     */
    NOTIFICATION("通知消息") {
        @Override
        protected int[] createNewsTypeRange() {
            // ?????????在线课程购买成功提示
            // ?????????调课成功提示
            // ?????????奖学券
            
            return new int[] {
                    // 9: 老师上课通知， 老师头像，红点点击或进入后消失
                    Mqtt.StudentMsgType.s_teacher_start_class_msg_type,
                    
                    // 202: 订单超时未支付取消， 老师头像，红点点击或进入后消失
                    Mqtt.StudentMsgType.s_order_unpay_or_confirm_before_dead_line_msg_type,
                    
                    // 204: 老师取消订单， 老师头像，跳转进入被取消的订单详情页， 红点点击或进入后消失
                    Mqtt.StudentMsgType.s_teacher_cancel_unpay_order_msg_type,
                    
                    // 206: 调课退还奖学券， 设计icon， 红点点击或进入后消失
                    Mqtt.StudentMsgType.s_return_back_voucher_after_change_course_apply_processed_msg_type,
                    
                    // 207: 家长收到了奖学券， 设计icon，跳转进奖学券界面， 红点点击或进入后消失
                    Mqtt.StudentMsgType.s_invite_student_register_msg_type,
                    
                    // 208: 收到待激活奖学券（邀请人）， 设计icon，跳转进奖学券界面， 红点点击或进入后消失
                    Mqtt.StudentMsgType.s_invite_student_active_msg_type,
                    
                    // 209: 家长收到了奖学券（被邀请人）， 设计icon，跳转进奖学券界面， 红点点击或进入后消失
                    Mqtt.StudentMsgType.s_invited_student_self_active_msg_type,
                    
                    // 219: 调课成功提示， 设计icon，跳转调整后的课程详情界面， 红点点击或进入后消失
                    Mqtt.StudentMsgType.s_msg_change_order_course_apply_no_need_pay_msg_type,
                    
                    // 221: 老师发布了教学心得, 设计icon, 跳转资讯详情页，红点点击或进入后消失
                    Mqtt.StudentMsgType.s_teacher_publish_information_msg_type,
                    
                    // 222: 关注了某个直播课堂, 设计icon, 跳转讲堂详情页，红点点击或进入后消失
                    Mqtt.StudentMsgType.s_follow_lecture_msg_type,
                    
                    // 224: 直播课堂支付成功, 设计icon, 跳转讲堂详情页，红点点击或进入后消失
                    Mqtt.StudentMsgType.s_pay_lecture_success_msg_type,
                    
                    // 225: 轻轻问答被回答后通知家长, 设计icon, 跳转分答详情页，红点点击或进入后消失
                    Mqtt.StudentMsgType.s_fanta_answered_msg_type,
                    
                    // 228: 轻轻问答被偷听, 设计icon, 红点点击或进入后消失
                    Mqtt.StudentMsgType.s_fanta_peeped_msg_type,
                    
                    // 231: 朋友团开团通成，所有人支付成功, 老师头像, 跳转订单详情页面，红点点击或进入后消失
                    Mqtt.StudentMsgType.s_group_order_made_up,
                    
                    // 232: 朋友团失败——订单取消，时间到期，所有人支付成功, 老师头像, 跳转订单详情页面，红点点击或进入后消失
                    Mqtt.StudentMsgType.s_group_order_cancel };
        }
    },
    
    /**
     * 退款消息
     */
    REFUND("退款消息") {
        @Override
        protected int[] createNewsTypeRange() {
            
            return new int[] {
                    // 201: 家长的退课申请被同意， 老师头像， 红点点击或进入后消失
                    Mqtt.StudentMsgType.s_assistant_apply_for_cancel_course_msg_type,
                    
                    // 213: 订单支付失败，退款给用户， 老师头像， 红点点击或进入后消失
                    Mqtt.StudentMsgType.s_return_bill_for_general_order_msg_type,
                    
                    // 214: 订单支付失败，退款给用户， 老师头像， 红点点击或进入后消失
                    Mqtt.StudentMsgType.s_return_bill_for_change_course_msg_type,
                    
                    // 227: 分答退款通知，退款给用户， 设计icon， 红点点击或进入后消失
                    Mqtt.StudentMsgType.s_fanta_refund_msg_type,
                    
                    // 230: 已支付的朋友团订单被取消， 老师头像， 跳转订单详情页面， 红点点击或进入后消失
                    Mqtt.StudentMsgType.s_group_sub_order_cancel };
        }
    },
    
    /**
     * 教学任务消息
     */
    TEACHING_TASK("教学任务提醒") {
        @Override
        protected int[] createNewsTypeRange() {
            
            return new int[] {
                    // 203: 老师填写了课堂反馈（废弃， 见236）， 老师头像， 跳转学迹卡详情， 红点点击对应消息后消失
                    Mqtt.StudentMsgType.s_teacher_feed_back_msg_type,
                    
                    // 205: 老师填写了追加反馈， 老师头像， 跳转学迹卡详情， 红点点击对应消息后消失
                    Mqtt.StudentMsgType.s_teacher_additional_feed_back_msg_type,
                    
                    // 233: 收到新的教学计划， 老师头像， 跳转xxx老师教学计划列表， 红点点击对应消息后消失
                    Mqtt.StudentMsgType.s_new_teach_plan,
                    
                    // 234: 收到新的教学总结， 老师头像， 跳转xxx老师阶段总结列表， 红点点击对应消息后消失
                    Mqtt.StudentMsgType.s_new_summarize,
                    
                    // 235: 老师线上布置了预习/作业， 老师头像， 跳转作业详情页， 红点点击对应消息后消失
                    Mqtt.StudentMsgType.s_teacher_assign_homework,
                    
                    // 236: 老师填写了课堂反馈， 老师头像， 跳转学迹卡详情， 红点点击对应消息后消失
                    Mqtt.StudentMsgType.s_teacher_finish_studytrace };
        }
    },

    /**
     * 老师端教学任务
     */
    TEACHER_TEACHING_TASK("教学任务") {
        @Override
        protected int[] createNewsTypeRange() {
            return new int[0];
        }
    },

    /**
     * 课程提醒
     */
    COURSE_REMINDER("课程提醒") {
        @Override
        protected int[] createNewsTypeRange() {
            return new int[0];
        }
    },

    /**
     * 平台通知
     */
    PLATFORM_NOTIFICATION("平台通知") {
        @Override
        protected int[] createNewsTypeRange() {
            return new int[0];
        }
    },

    /**
     * 订单消息
     */
    ORDER_MESSAGE("订单消息") {
        @Override
        protected int[] createNewsTypeRange() {
            return new int[0];
        }
    },

    /**
     * 轻轻学院
     */
    QQ_COLLEGE("轻轻学院") {
        @Override
        protected int[] createNewsTypeRange() {
            return new int[0];
        }
    },
    
    /**
     * 聊天消息
     */
    SINGLE_CHAT("single_chat") {
        @Override
        protected int[] createNewsTypeRange() {
            return new int[0];
        }
    },

    /**
     * 聊天消息
     */
    GROUP_CHAT("group_chat") {
        @Override
        protected int[] createNewsTypeRange() {
            return new int[0];
        }
    },
    
    /**
     * 轻轻活动消息
     */
    ACTIVITY("轻轻活动") {
        @Override
        protected int[] createNewsTypeRange() {
            return new int[] {
                    
                    // -------------600-700批量推送消息体（区分用户）-----------
                    // 600: 轻轻活动推送撤消
                    // Mqtt.DevicePushProtoMsgType.d_qingqing_activity_revert_msg_type
                    
                    // 601: 轻轻活动推送, 设计icon, 跳转h5页面, 轻轻提醒, 红点点击或进入后消失，
                    Mqtt.DevicePushProtoMsgType.d_qingqing_h5_activity_msg_type,
                    
                    // 602: 讲座活动推送, 设计icon, 跳转讲座详情页, 轻轻提醒, 红点点击或进入后消失，
                    Mqtt.DevicePushProtoMsgType.d_lecture_detail_msg_type,
                    
                    // 603: 分答推送, 设计icon, 跳转分答详情页, 轻轻提醒, 红点点击或进入后消失，
                    Mqtt.DevicePushProtoMsgType.d_fanta_msg_type,
                    
                    // -------------700-800设备推送消息体（不区分用户）-----------
                    // 700: 轻轻活动推送撤消
                    // Mqtt.UserBatchPushProtoMsgType.b_qingqing_activity_revert_msg_type
                    
                    // 701: 轻轻活动推送, 设计icon, 跳转h5页面, 轻轻提醒, 红点点击或进入后消失，
                    Mqtt.UserBatchPushProtoMsgType.b_qingqing_h5_activity_msg_type,
                    
                    // 702: 讲座活动推送, 设计icon, 跳转讲座详情页, 轻轻提醒, 红点点击或进入后消失，
                    Mqtt.UserBatchPushProtoMsgType.b_lecture_detail_msg_type,
                    
                    // 703: 分答推送,设计icon, 跳转分答详情页, 轻轻提醒, 红点点击或进入后消失，
                    Mqtt.UserBatchPushProtoMsgType.b_fanta_msg_type, };
        }
    },

    /**
     * TA, 我的消息
     */
    MY_NEWS("我的消息") {
        @Override
        protected int[] createNewsTypeRange() {
            return new int[0];
        }
    },

    /**
     * 公司消息
     */
    COMPANY_NEWS("公司消息") {
        @Override
        protected int[] createNewsTypeRange() {
            return new int[0];
        }
    },

    /**
     * 信息资讯
     */
    INFORMATION("信息资讯") {
        @Override
        protected int[] createNewsTypeRange() {
            return new int[0];
        }
    },

    /**
     * 教研任务通知
     */
    TRM_TASK("教研任务通知") {
        @Override
        protected int[] createNewsTypeRange() {
            return new int[0];
        }
    },

    /**
     * 未知消息
     */
    UNKNOWN("unknown") {
        @Override
        protected int[] createNewsTypeRange() {
            return new int[0];
        }
    };
    
    private final int[] newsTypeRange;
    
    protected abstract int[] createNewsTypeRange();
    
    private final String value;
    
    NewsConversationType(String value) {
        this.value = value;
        newsTypeRange = createNewsTypeRange();
    }
    
    protected int[] getNewsTypeRange() {
        return newsTypeRange;
    }
    
    private boolean isInRange(int newsType) {
        if (newsTypeRange == null || newsTypeRange.length <= 0) {
            return false;
        }
        
        for (int type : newsTypeRange) {
            if (type == newsType) {
                return false;
            }
        }
        return false;
    }
    
    public static NewsConversationType getTypeBy(int newsType) {
        NewsConversationType[] values = NewsConversationType.values();
        for (NewsConversationType type : values) {
            if (type.isInRange(newsType)) {
                return type;
            }
        }
        return UNKNOWN;
        
        /*
         * if ((newsType > 600 && newsType < 700) || (newsType > 700 && newsType
         * < 800)) {
         * 
         * // -------------600-700----------- // 600: 轻轻活动推送撤消, //
         * Mqtt.DevicePushProtoMsgType.d_qingqing_activity_revert_msg_type
         * 
         * // 601: 轻轻活动推送, 设计icon, 跳转h5页面, 轻轻提醒, 红点点击或进入后消失， //
         * Mqtt.DevicePushProtoMsgType.d_qingqing_h5_activity_msg_type
         * 
         * // 602: 讲座活动推送, 设计icon, 跳转讲座详情页, 轻轻提醒, 红点点击或进入后消失， //
         * Mqtt.DevicePushProtoMsgType.d_lecture_detail_msg_type
         * 
         * // 603: 分答推送, 设计icon, 跳转分答详情页, 轻轻提醒, 红点点击或进入后消失， //
         * Mqtt.DevicePushProtoMsgType.d_fanta_msg_type
         * 
         * // -------------700-800----------- // 700: 轻轻活动推送撤消, //
         * Mqtt.UserBatchPushProtoMsgType.b_qingqing_activity_revert_msg_type
         * 
         * // 701: 轻轻活动推送, 设计icon, 跳转h5页面, 轻轻提醒, 红点点击或进入后消失， //
         * Mqtt.UserBatchPushProtoMsgType.b_qingqing_h5_activity_msg_type
         * 
         * // 702: 讲座活动推送, 设计icon, 跳转讲座详情页, 轻轻提醒, 红点点击或进入后消失， //
         * Mqtt.UserBatchPushProtoMsgType.b_lecture_detail_msg_type
         * 
         * // 703: 分答推送,设计icon, 跳转分答详情页, 轻轻提醒, 红点点击或进入后消失， //
         * Mqtt.UserBatchPushProtoMsgType.b_fanta_msg_type
         * 
         * return ACTIVITY; }
         * 
         * switch (newsType) { //
         * -------------------通知消息start------------------- //
         * ?????????在线课程购买成功提示 // ?????????调课成功提示 // ?????????奖学券 // 9: 老师上课通知，
         * 老师头像，红点点击或进入后消失 case
         * Mqtt.StudentMsgType.s_teacher_start_class_msg_type:
         * 
         * // 202: 订单超时未支付取消， 老师头像，红点点击或进入后消失 case Mqtt.StudentMsgType.
         * s_order_unpay_or_confirm_before_dead_line_msg_type:
         * 
         * // 204: 老师取消订单， 老师头像，跳转进入被取消的订单详情页， 红点点击或进入后消失 case
         * Mqtt.StudentMsgType.s_teacher_cancel_unpay_order_msg_type:
         * 
         * // 206: 调课退还奖学券， 设计icon， 红点点击或进入后消失 case Mqtt.StudentMsgType.
         * s_return_back_voucher_after_change_course_apply_processed_msg_type:
         * 
         * // 207: 家长收到了奖学券， 设计icon，跳转进奖学券界面， 红点点击或进入后消失 case
         * Mqtt.StudentMsgType.s_invite_student_register_msg_type:
         * 
         * // 208: 收到待激活奖学券（邀请人）， 设计icon，跳转进奖学券界面， 红点点击或进入后消失 case
         * Mqtt.StudentMsgType.s_invite_student_active_msg_type:
         * 
         * // 209: 家长收到了奖学券（被邀请人）， 设计icon，跳转进奖学券界面， 红点点击或进入后消失 case
         * Mqtt.StudentMsgType.s_invited_student_self_active_msg_type:
         * 
         * // 219: 调课成功提示， 设计icon，跳转调整后的课程详情界面， 红点点击或进入后消失 case
         * Mqtt.StudentMsgType.
         * s_msg_change_order_course_apply_no_need_pay_msg_type:
         * 
         * // 221: 老师发布了教学心得, 设计icon, 跳转资讯详情页，红点点击或进入后消失 case
         * Mqtt.StudentMsgType.s_teacher_publish_information_msg_type:
         * 
         * // 222: 关注了某个直播课堂, 设计icon, 跳转讲堂详情页，红点点击或进入后消失 case
         * Mqtt.StudentMsgType.s_follow_lecture_msg_type:
         * 
         * // 224: 直播课堂支付成功, 设计icon, 跳转讲堂详情页，红点点击或进入后消失 case
         * Mqtt.StudentMsgType.s_pay_lecture_success_msg_type:
         * 
         * // 225: 轻轻问答被回答后通知家长, 设计icon, 跳转分答详情页，红点点击或进入后消失 case
         * Mqtt.StudentMsgType.s_fanta_answered_msg_type:
         * 
         * // 228: 轻轻问答被偷听, 设计icon, 红点点击或进入后消失 case
         * Mqtt.StudentMsgType.s_fanta_peeped_msg_type:
         * 
         * // 231: 朋友团开团通成，所有人支付成功, 老师头像, 跳转订单详情页面，红点点击或进入后消失 case
         * Mqtt.StudentMsgType.s_group_order_made_up:
         * 
         * // 232: 朋友团失败——订单取消，时间到期，所有人支付成功, 老师头像, 跳转订单详情页面，红点点击或进入后消失 case
         * Mqtt.StudentMsgType.s_group_order_cancel: return NOTIFICATION; //
         * -------------------通知消息end-------------------
         * 
         * // -------------------退款消息start------------------- // 201:
         * 家长的退课申请被同意， 老师头像， 红点点击或进入后消失 case
         * Mqtt.StudentMsgType.s_assistant_apply_for_cancel_course_msg_type:
         * 
         * // 213: 订单支付失败，退款给用户， 老师头像， 红点点击或进入后消失 case
         * Mqtt.StudentMsgType.s_return_bill_for_general_order_msg_type:
         * 
         * // 214: 订单支付失败，退款给用户， 老师头像， 红点点击或进入后消失 case
         * Mqtt.StudentMsgType.s_return_bill_for_change_course_msg_type:
         * 
         * // 227: 分答退款通知，退款给用户， 设计icon， 红点点击或进入后消失 case
         * Mqtt.StudentMsgType.s_fanta_refund_msg_type:
         * 
         * // 230: 已支付的朋友团订单被取消， 老师头像， 跳转订单详情页面， 红点点击或进入后消失 case
         * Mqtt.StudentMsgType.s_group_sub_order_cancel: return REFUND; //
         * -------------------退款消息end-------------------
         * 
         * // -------------------教学任务消息start------------------- // 203:
         * 老师填写了课堂反馈（废弃， 见236）， 老师头像， 跳转学迹卡详情， 红点点击对应消息后消失 case
         * Mqtt.StudentMsgType.s_teacher_feed_back_msg_type:
         * 
         * // 205: 老师填写了追加反馈， 老师头像， 跳转学迹卡详情， 红点点击对应消息后消失 case
         * Mqtt.StudentMsgType.s_teacher_additional_feed_back_msg_type:
         * 
         * // 233: 收到新的教学计划， 老师头像， 跳转xxx老师教学计划列表， 红点点击对应消息后消失 case
         * Mqtt.StudentMsgType.s_new_teach_plan:
         * 
         * // 234: 收到新的教学总结， 老师头像， 跳转xxx老师阶段总结列表， 红点点击对应消息后消失 case
         * Mqtt.StudentMsgType.s_new_summarize:
         * 
         * // 235: 老师线上布置了预习/作业， 老师头像， 跳转作业详情页， 红点点击对应消息后消失 case
         * Mqtt.StudentMsgType.s_teacher_assign_homework:
         * 
         * // 236: 老师填写了课堂反馈， 老师头像， 跳转学迹卡详情， 红点点击对应消息后消失 case
         * Mqtt.StudentMsgType.s_teacher_finish_studytrace: return
         * TEACHING_TASK; // -------------------教学任务消息end-------------------
         * 
         * // -------------------待办事项消息start------------------- //
         * ?????????待结课的课程 // 8: 待支付订单（老师发起的续课订单，TA发起的订单）， 老师头像， 跳转订单详情页面，
         * 红点点击对应消息后消失 case Mqtt.StudentMsgType.s_need_to_pay_msg_type:
         * 
         * // 229: 待支付订单（团课）， 老师头像， 跳转订单详情页面， 红点点击对应消息后消失 case
         * Mqtt.StudentMsgType.s_new_add_sub_group_order: return TODO; //
         * -------------------待办事项消息end------------------- }
         * 
         * return UNKNOWN;
         */
    }
    
    public static NewsConversationType getDefault() {
        return UNKNOWN;
    }
    
    public String getValue() {
        return value;
    }
    
    public static NewsConversationType mapStringToValue(final String value) {
        for (NewsConversationType type : NewsConversationType.values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        return getDefault();
    }
    
}

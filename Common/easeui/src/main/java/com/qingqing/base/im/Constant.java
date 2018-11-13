package com.qingqing.base.im;

import com.easemob.easeui.EaseConstant;

public class Constant extends EaseConstant {
    public static final String NEW_FRIENDS_USERNAME = "item_new_friends";
    public static final String GROUP_USERNAME = "item_groups";
    public static final String CHAT_ROOM = "item_chatroom";
    public static final String ACCOUNT_REMOVED = "account_removed";
    public static final String ACCOUNT_CONFLICT = "conflict";
    public static final String CHAT_ROBOT = "item_robots";
    public static final String MESSAGE_ATTR_ROBOT_MSGTYPE = "msgtype";
    public static final String ACTION_GROUP_CHANAGED = "action_group_changed";
    public static final String ACTION_CONTACT_CHANAGED = "action_contact_changed";
    public static final String ACTION_NEW_MESSAGE = "action_new_message";
    
    public static final String ACTION_IM_MESSAGE = "com.qingqing.ACTION_IM_MESSAGE";
    
    public static final String EXTRA_CHAT_TYPE = EaseConstant.EXTRA_CHAT_TYPE;
    public static final String EXTRA_USER_ID = EaseConstant.EXTRA_USER_ID;
    public static final String EXTRA_CHAT_SCENE = "chat_scene";
    public static final String EXTRA_CONSULTED_TEACHER_ID = "consulted_teacher_id";
    public static final String EXTRA_SHARE_FEEDBACK_AMOUNT = "share_feedback_amount";
    public static final String EXTRA_SHARE_FEEDBACK_AMOUNT_OF_LECTURE = "share_feedback_amount_of_lecture";
    
    public static final String EXTRA_LATITUDE = "latitude";
    public static final String EXTRA_LONGITUDE = "longitude";
    /**
     * 立刻初始化
     */
    public static final String EXTRA_CHAT_INIT_IMM = "init_immediate";
    
    public static final int CHATTYPE_SINGLE = EaseConstant.CHATTYPE_SINGLE;
    public static final int CHATTYPE_GROUP = EaseConstant.CHATTYPE_GROUP;
    public static final int CHATTYPE_CHATROOM = EaseConstant.CHATTYPE_CHATROOM;
    
    public static final int CHATSCNE_NEW_CREATE = 1;
    public static final int CHATSCNE_CONSULT = 2;
    
    public static final int CHAT_ROOM_TYPE_NORMAL = 0;
    public static final int CHAT_ROOM_TYPE_LECTURE = 1;
    
    public static final String EXTRA_LECTURE_ID = "lecture_id";
    public static final String EXTRA_LECTURE_INFO = "lecture_info";
    
    public static final String EXTRA_CHAT_ROOM_TYPE = "chat_room_type";
    
    /**
     * 以下字段仅是为了处理老师主页跳聊天界面需要强制显示昵称
     */
    public static final String EXTRA_USER_NAME = "chat_user_name";
    
    /**
     * 进入聊天界面时，自动发送的消息
     */
    public static final String EXTRA_MSG_TO_BE_SENT = "msg_to_be_sent";
}

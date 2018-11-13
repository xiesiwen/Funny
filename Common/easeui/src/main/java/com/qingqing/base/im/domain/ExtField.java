package com.qingqing.base.im.domain;

import java.util.List;

/**
 * 消息的额外字段，详见
 *
 * http://wiki.changingedu.com/pages/viewpage.action?pageId=2627831
 *
 * Created by lihui on 2017/9/8.
 */

public class ExtField {
    /**
     * 是否展示在聊天窗口，值为true 的时候 不展示（发送消息通知），值为false 展示，默认为false
     */
    public boolean noShow;
    /**
     * 显示时，是否需要显示发送方
     */
    public boolean needShowFrom;
    /**
     * qingqingUserId逗号分隔。如果target_users不为空，优先判断。如果包含才显示，不考虑filter_users
     */
    public List<String> targetUsers;
    /**
     * 如果target_users为空，判断filter_users。不为空的情况下，不包含才显示。两者都为空，都显示
     */
    public List<String> filterUsers;
    /**
     * 显示时，是否需要显示发送方
     */
    public boolean isSelfMock;
    /**
     * true表示需要上报
     */
    public boolean needReport;
    /**
     * 轻轻messageId
     */
    public String qingqingMsgId;
    /**
     * 是否需要ta快速回复，不填表示需要
     */
    public boolean needQuickResponse;
    
    public FromUserInfo fromUserInfo;
    
    public class Attr {
        public static final String NO_SHOW = "no_show";
        public static final String NEED_SHOW_FROM = "need_show_from";
        public static final String TARGET_USERS = "target_users";
        public static final String FILTER_USERS = "filter_users";
        public static final String IS_SELF_MOCK = "is_self_mock";
        public static final String NEED_REPORT = "need_report";
        public static final String QINGQING_MSG_ID = "qingqing_msg_id";
        public static final String NEED_QUICK_RESPONSE = "need_quick_response";
        public static final String ACTION = "action";
        public static final String FROM_USER_INFO = "from_user_info";
        public static final String QINGQING_USER_ID = "qingqing_user_id";
        public static final String NICK = "nick";
        public static final String HEAD_IMG = "head_img";
        public static final String USER_TYPE = "user_type";
        public static final String SEX_TYPE = "sex_type";
        public static final String CHAT_ROOM_AUTH = "chat_room_auth";
        public static final String CHAT_ROOM_AUTH_V2 = "chat_room_auth_v2";
        public static final String TEACHER_EXTEND = "teacher_extend";
        public static final String TEACHER_ROLE = "teacher_role";
        public static final String EM_APNS_EXT = "em_apns_ext";
        public static final String EM_PUSH_TITLE = "em_push_title";
    }
    
    public static class FromUserInfo {
        public String qingqingUserId;
        public String nick;
        public String headImg;
        public int userType;
        public int sexType;
        /**
         * {@link com.qingqing.api.proto.v1.im.ImProto.ChatRoomRoleType}
         */
        public List<Integer> roleType;
        public TeacherExtend teacherExtend;
        
        public static class TeacherExtend {
            /**
             * 用户教研管理角色（非老师用户不返回teacher_extend，teacher_role可能为空）
             * {@link com.qingqing.api.proto.base.TeacherCommonProto.TeacherTeachingResearchRoleType}
             */
            public List<Integer> teacherRole;
        }
    }
}

package com.qingqing.base.im.domain;

/**
 * Created by huangming on 2015/12/25.
 */
public class CmdMsg {
    
    public static final String TYPE = "t";
    
    // 所有端：{"text":"我是文本","t":"500"}
    public final static int CMD_TYPE_TEXT = 500;
    // 咨询学生端：{"qingqing_teacher_id":"123456","format_course_grade":"小学*初中语文","address":"地址","nick":"昵称","teacher_second_id":"second_id","new_head_image":"/pic/201511/3d/{0}3d78909e-1e5a-402f-b456-d60bb229de4c.jpg","t":"216"}
    public final static int CMD_TYPE_CONSULT_ST = 216;
    // 咨询助教端：{"qingqing_teacher_id":"123456","real_name":"老师实名","format_course_grade":"小学*初中语文","ta_real_name":"老师的助教名","address":"地址","nick":"昵称","teacher_second_id":"second_id","new_head_image":"/pic/201511/3d/{0}3d78909e-1e5a-402f-b456-d60bb229de4c.jpg","t":"125"}
    public final static int CMD_TYPE_CONSULT_TA = 125;
    // 老师推荐学生&助教端：{"qingqing_teacher_id":"123456","nick":"昵称","teacher_second_id":"second_id","head_icon":"/pic/201511/3d/{0}3d78909e-1e5a-402f-b456-d60bb229de4c.jpg","good_appraise_count":10,"descrption":"老师描述","course_name":"语文","t":"212"}
    public final static int CMD_TYPE_REC_TEACHER = 212;
    // 新学生绑定助教端:
    // {"qingqing_student_id":"123456","grade_name":"二年级","new_head_image":"/pic/201511/3d/{0}3d78909e-1e5a-402f-b456-d60bb229de4c.jpg","phone_number":"13912312311","t":"217"}
    public final static int CMD_TYPE_ST_BIND_TA = 131;
    
    public final static int CMD_TYPE_REMIND_ST = 249;

    // 过期未上课申请退课："{"text":"自测1实您好，瓜老师瓜老师瓜老师瓜老师放放(186****0909)10月24日15:00~17:00小学三年级 语文 的课程已过期但未上课。\n现需要退课，请帮忙处理","t":"395","ct":"其他"}"
    public final static int CMD_TYPE_OVERDUE_APPLY_CANCEL_COURSE = 395;
    
    // --------------群组 Start
    // u表示用户轻轻userid
    // 更新群信息:{"u":"123456", "group_name":"群名称","t":"501"}
    public final static int CMD_TYPE_GROUP_UPDATE = 501;
    // 用户退出群：{"u":"123456","t":"504"}
    public final static int CMD_TYPE_GROUP_EXIT = 504;
    // 用户被移出（踢出）群：{"u":"123456", "members":["654321","654321"],"t":"502"}
    public final static int CMD_TYPE_GROUP_REMOVED = 502;
    // 用户被加入：{"u":"123456", "members":["654321","654321"],"t":"503"}
    public final static int CMD_TYPE_GROUP_INVITED = 503;
    // 组创建：{"u":"123456", "group_name":"群名称",
    // "members":["654321","654321"],"t":"505"}
    public final static int CMD_TYPE_GROUP_CREATED = 505;
    // 群公告更新：{"t":"511", "f":''群公告"}
    public final static int CMD_TYPE_GROUP_ANNOUNCE = 511;
    // 消息撤回：{"t":"512", "u":''13213213","msgid":"1136545532"}
    public final static int CMD_TYPE_GROUP_REVOKE_MESSAGE = 512;
    // 课程报告：{"u":"527455153","shareCode":"drkmesJF4VDMcaYGpuNCtgidid","headImage":"","nick":"123","reportTitle":"我的小学五年级语文课程报告-质量一般","totalWordsCount":29,"totalAudioTimeLength":20,"totalPictureCount":4,"t":"513","ct":"其他"}
    public final static int CMD_TYPE_GROUP_COURSE_REPORT = 513;
    // 指标排名信息：{"msgTitle":"家长留存率排行榜",
    // "infoList":[{"userId":2992,"userType":"teacher","indexValue":"100","indexRank":1,"indexTrend":1,"headImage":"/headimg/201605/89/{0}898aaf40-43fe-4643-bebd-f06e8e449f16.jpg","nick":"222221-教龄21年","qingqingUserId":"366511753"},
    // {"userId":128,"userType":"teacher","indexValue":"16","indexRank":2,"indexTrend":0,"headImage":"/headimg/201510/df/{0}df14a271-4f3d-4ffa-b538-adbfa2a267e2.jpg","nick":"呵呵-教龄1年","qingqingUserId":"709361801"},
    // {"userId":28013,"userType":"teacher","indexValue":"15","indexRank":3,"indexTrend":2,"headImage":"/headimg/201510/df/{0}df14a271-4f3d-4ffa-b538-adbfa2a267e2.jpg","nick":"dfsaf","qingqingUserId":"625816025"}],
    // "shareCode":"drkmfHOTFZ4MsOI6gASnDKQPnUmbPsoN0y68Nrgu4g7ME9cTlfEyeqcRiaSGFB6QKnVMid","t":"514","ct":"其他"}
    public final static int CMD_TYPE_GROUP_RANK = 514;
    // 内容分享：{"t":“516“,""ctt:"{teach_plan|summarize}","tt":"标题","tv":"内容","refid":“关联ID“,"u":"12345"}
    public final static int CMD_TYPE_SHARE_PLAN_SUMMARIZE = 516;
    // 单聊消息撤回：{"t":"517", "u":''13213213","msgid":"1136545532"}
    public final static int CMD_TYPE_SINGLE_REVOKE_MESSAGE = 517;
    // --------------群组 end
    
    // --------------讲堂聊天室
    // to_stop_qingqing_user_id 不存在为全局禁言
    public final static int CMD_TYPE_LECTURE_STOP_TALK = 506;
    public final static int CMD_TYPE_LECTURE_DESTROYED = 507;
    public final static int CMD_TYPE_LECTURE_PLAY_PPT = 508;
    public final static int CMD_TYPE_LECTURE_CHANGE_ROOM = 509;
    public final static int CMD_TYPE_LECTURE_CHANGE_PPT = 510;
    // --------------讲堂聊天室
    
    // TODO: ext 的解析，普通消息也需要。待统一替换为 ExtFieldParser
    public boolean isReceiveMsg;
    public int msgType;
    
    public String body;
    
    public String from;
    public String to;
    // 撤回消息的目标 userId
    public String revokeMsgUserId;
    
    public static final class Text {
        public static final String TEXT = "text";
    }
    
    public static final class Consult {
        
        public static final String QQ_TEACHER_ID = "qingqing_teacher_id";
        public static final String FORMAT_COURSE_GRADE = "format_course_grade";
        public static final String TEACHER_REAL_NAME = "real_name";
        public static final String TA_REAL_NAME = "ta_real_name";
        public static final String ADDRESS = "address";
        public static final String NICK = "nick";
        public static final String TEACHER_SECOND_ID = "teacher_second_id";
        public static final String SECOND_ID = "second_id";
        public static final String HEAD_IMG = "new_head_image";
        
    }
    
    public static final class RecTeacher {
        public static final String QQ_TEACHER_ID = "qingqing_teacher_id";
        public static final String NICK = "nick";
        public static final String TEACHER_SECOND_ID = "teacher_second_id";
        public static final String SECOND_ID = "second_id";
        public static final String HEAD_IMG = "head_icon";
        public static final String GOOD_APPRAISE_COUNT = "good_appraise_count";
        public static final String DESCRPTION = "descrption";
        public static final String COURSE_NAME = "course_name";
        public static final String GRADE_COURSE = "grade_course_format_name";
        public static final String MIN_PRICE = "min_price";
        public static final String MAX_PRICE = "max_price";
        
    }
    
    public static final class BindTA {
        /**
         * 学生ID
         */
        public static final String QQ_STUDENT_ID = "qingqing_student_id";
        public static final String PHONE_NUMBER = "phone_number";
        public static final String GRADE_NAME = "grade_name";
        public static final String HEAD_IMG = "new_head_image";
        public static final String FLOW_TYPE = "flow_type";
        
    }
    
    public static final class Group {
        
        public static final String QQ_USER_ID = "u";
        public static final String GROUP_NAME = "group_name";
        public static final String GROUP_MEMBERS = "members";
        public static final String GROUP_ANNOUNCE = "f";
        public static final String MSG_ID = "msgid";
        public static final String SHARE_CODE = "shareCode";
        public static final String HEAD_IMAGE = "headImage";
        public static final String NICK = "nick";
        public static final String REPORT_TITLE = "reportTitle";
        public static final String TOTAL_WORDS_COUNT = "totalWordsCount";
        public static final String TOTAL_AUDIO_TIME_LENGTH = "totalAudioTimeLength";
        public static final String TOTAL_PICTURE_COUNT = "totalPictureCount";
        public static final String RANK_TITLE = "msgTitle";
        public static final String RANK_INFO_LIST = "infoList";
        public static final String RANK_INDEX_VALUE = "indexValue";
        public static final String RANK_INDEX_RANK = "indexRank";
        public static final String RANK_INDEX_TREND = "indexTrend";
        public static final String RANK_USER_TYPE = "userType";
        public static final String RANK_HEAD_IMAGE = "headImage";
        public static final String RANK_NICK = "nick";
        public static final String RANK_QQ_USER_ID = "qqUserId";
        public static final String SHARE_PLAN_SUMMARIZE_TYPE = "ctt";
        public static final String SHARE_PLAN_SUMMARIZE_TITLE = "tt";
        public static final String SHARE_PLAN_SUMMARIZE_CONTENT = "tv";
        public static final String SHARE_PLAN_SUMMARIZE_REF_ID = "refid";
        
    }
    
    public static final class Lecture {
        
        public static final String TO_STOP_USER_ID = "to_stop_qingqing_user_id";
        public static final String TO_STOP_USER_NICK = "to_stop_user_nick";
        public static final String TO_STOP_USER_TYPE = "to_stop_user_type";
        public static final String IS_ALLOW = "is_allow";
        public static final String PPT_IMG_URL = "image_url";
        public static final String PPT_IMG_INDEX = "image_index";
        public static final String SEND_TIME = "send_time";
        public static final String CHAT_ROOM_ID = "chatroom_id";
        public static final String PPT_IMG_URLS = "img_urls";
    }
    
    public boolean isGroupCmdMsg() {
        return msgType == CMD_TYPE_GROUP_UPDATE || msgType == CMD_TYPE_GROUP_EXIT
                || msgType == CMD_TYPE_GROUP_REMOVED || msgType == CMD_TYPE_GROUP_INVITED
                || msgType == CMD_TYPE_GROUP_CREATED || msgType == CMD_TYPE_GROUP_ANNOUNCE
                || msgType == CMD_TYPE_GROUP_REVOKE_MESSAGE
                || msgType == CMD_TYPE_GROUP_COURSE_REPORT
                || msgType == CMD_TYPE_SHARE_PLAN_SUMMARIZE
                || msgType == CMD_TYPE_GROUP_RANK;
    }
    
    public boolean isLectureCmdMsg() {
        return msgType == CMD_TYPE_LECTURE_STOP_TALK
                || msgType == CMD_TYPE_LECTURE_DESTROYED
                || msgType == CMD_TYPE_LECTURE_PLAY_PPT
                || msgType == CMD_TYPE_LECTURE_CHANGE_ROOM
                || msgType == CMD_TYPE_LECTURE_CHANGE_PPT;
    }
    
    public boolean isLectureNeedShowCmdMsg() {
        return isLectureNeedShowCmdMsg(msgType);
    }
    
    public static boolean isLectureNeedShowCmdMsg(int cmdType) {
        return cmdType == CMD_TYPE_LECTURE_STOP_TALK
                || cmdType == CMD_TYPE_LECTURE_DESTROYED
                || cmdType == CMD_TYPE_LECTURE_PLAY_PPT;
    }
}

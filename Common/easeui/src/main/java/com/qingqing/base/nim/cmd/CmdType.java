package com.qingqing.base.nim.cmd;

/**
 * Created by huangming on 2016/8/27.
 */
public class CmdType {

    //所有端：{"text":"我是文本","t":"500"}
    public final static int CMD_TYPE_TEXT = 500;
    //咨询学生端：{"qingqing_teacher_id":"123456","format_course_grade":"小学*初中语文","address":"地址","nick":"昵称","teacher_second_id":"second_id","new_head_image":"/pic/201511/3d/{0}3d78909e-1e5a-402f-b456-d60bb229de4c.jpg","t":"216"}
    public final static int CMD_TYPE_CONSULT_ST = 216;
    //咨询助教端：{"qingqing_teacher_id":"123456","real_name":"老师实名","format_course_grade":"小学*初中语文","ta_real_name":"老师的助教名","address":"地址","nick":"昵称","teacher_second_id":"second_id","new_head_image":"/pic/201511/3d/{0}3d78909e-1e5a-402f-b456-d60bb229de4c.jpg","t":"125"}
    public final static int CMD_TYPE_CONSULT_TA = 125;
    //老师推荐学生&助教端：{"qingqing_teacher_id":"123456","nick":"昵称","teacher_second_id":"second_id","head_icon":"/pic/201511/3d/{0}3d78909e-1e5a-402f-b456-d60bb229de4c.jpg","good_appraise_count":10,"descrption":"老师描述","course_name":"语文","t":"212"}
    public final static int CMD_TYPE_REC_TEACHER = 212;
    //新学生绑定助教端: {"qingqing_student_id":"123456","grade_name":"二年级","new_head_image":"/pic/201511/3d/{0}3d78909e-1e5a-402f-b456-d60bb229de4c.jpg","phone_number":"13912312311","t":"217"}
    public final static int CMD_TYPE_ST_BIND_TA = 131;

    //--------------群组 Start
    //u表示用户轻轻userid
    //更新群信息:{"u":"123456", "group_name":"群名称","t":"501"}
    public final static int CMD_TYPE_GROUP_UPDATE = 501;
    //用户退出群：{"u":"123456","t":"504"}
    public final static int CMD_TYPE_GROUP_EXIT = 504;
    //用户被移出（踢出）群：{"u":"123456", "members":["654321","654321"],"t":"502"}  //members被移出（踢出）群
    public final static int CMD_TYPE_GROUP_REMOVED = 502;
    //用户被加入：{"u":"123456", "members":["654321","654321"],"t":"503"}
    public final static int CMD_TYPE_GROUP_INVITED = 503;
    //组创建：{"u":"123456", "group_name":"群名称", "members":["654321","654321"],"t":"505"}
    public final static int CMD_TYPE_GROUP_CREATED = 505;
    //群公告更新：{"t":"511", "f":''群公告"}
    public final static int CMD_TYPE_GROUP_ANNOUNCE = 512;
    //消息撤回：{"t":"512", "u":''13213213","msgid":"1136545532"}
    public final static int CMD_TYPE_GROUP_REVOKE_MESSAGE = 512;
    //--------------群组 end

    //--------------讲堂聊天室
    //to_stop_qingqing_user_id 不存在为全局禁言
    public final static int CMD_TYPE_LECTURE_STOP_TALK = 506;
    public final static int CMD_TYPE_LECTURE_DESTROYED = 507;
    public final static int CMD_TYPE_LECTURE_PLAY_PPT = 508;
    public final static int CMD_TYPE_LECTURE_CHANGE_ROOM = 509;
    public final static int CMD_TYPE_LECTURE_CHANGE_PPT = 510;
    //--------------讲堂聊天室

}

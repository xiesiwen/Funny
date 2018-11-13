package com.qingqing.base.data;

/**
 * Created by dubo on 15/11/2.
 */

public class StatisticalDataConstants {
    
    /**
     * 请求的url
     */
    public static final String REQ_URL_USER_BEHAVIOR = "/log.html";
    public static final String REQ_URL_NETWORK_ERROR = "/blog.html";
    
    /**
     * 数据统计 日志type
     */
    
    public static int USER_LOG = 0;
    public static int NETWORK_ERROR = 1;
    
    /**
     * 数据统计 网络日志错误类型
     */
    
    public static String SUCCESS = "success";
    public static String TIMEOUT = "timeout";
    public static String CLINETERROR = "clienterror";
    public static String SERVERERROR = "servererror";
    public static String NETWORKERROR = "networkerror";
    public static String DNSERROR = "dnserror";

    /**
     * 是否点击
     */
    public static String CLICK = "1";
    public static String NOTCLICK = "0";
    
    /**
     * 数据统计 日志埋点ID
     */
    public static final String LOG_EVENT_ID = "10000";
    public static final String LOG_PAGE_ID = "20000";
    public static final String LOG_PAGE_ACTION_ID = "20001";
    
    /**
     * 数据统计 日志埋点行为 事件级
     */
    public static final String LOG_OPEN_APP = "o_use_app";
    public static final String LOG_CLOSE_APP = "o_quit_app";
    public static final String LOG_SERVER_REQUEST = "o_server_request";
    public static final String LOG_START_APP = "o_app_start";
    public static final String LOG_USER_REGISTER = "o_download_register";
    public static final String LOG_QUIT_LECTURE = "o_invited_speakers_im";
    public static final String LOG_QUIT_AV = "o_av_qos";
    public static final String LOG_TEACHER_REGION = "o_tr_region";//老师地理围栏
    public static final String LOG_SURVIVAL_TIME = "o_app_survival_time";//app存活时间
    
    /**
     * 数据统计 页面 页面级
     */
    // 智能匹配
    public static final String LOG_PAGE_PARM = "intelligent_request";
    public static final String LOG_PAGE_SEARCH = "intellligent_search";
    public static final String LOG_PAGE_LIST = "intellligent_teacher_list";
    public static final String LOG_PAGE_DETAIL = "intellligent_teacher_detail";
    
    // 4.6版本 家长端 wiki
    // http://wiki.changingedu.com/pages/viewpage.action?pageId=2628405
    public static final String LOG_PAGE_HOME = "home"; // 首页
    public static final String LOG_PAGE_NEW_STUDENT_HOME = "new_student_home"; // 新首页
    public static final String LOG_PAGE_TR_LIST = "tr_list"; // 老师列表
    // 4.8版本去掉注释 public static final String LOG_PAGE_LOGIN_IN_TR_LIST =
    // "login_in_tr_list"; //列表登陆
    public static final String LOG_PAGE_TR_PAGE = "tr_page"; // 主页
    public static final String LOG_PAGE_NEW_TR_PAGE_STU_APP_PAGE = "new_tr_page_stu_app"; // 新版主页
    public static final String LOG_PAGE_CONFIRM_ORDER = "confirm_order"; // o
    public static final String LOG_PAGE_ORDER_PAYMENT = "order_payment";
    public static final String LOG_PAGE_TA_HELP_REQUEST = "ta_help_request";
    public static final String LOG_PAGE_TA_HELP_SENT = "ta_help_sent";
    // 4.7
    // 分享
    public static final String LOG_PAGE_SHARE_TR = "share_tr_page";
    public static final String LOG_PAGE_SHARE_STU_APP = "share_stu_app";
    public static final String LOG_PAGE_SHARE_TEA_APP = "share_tea_app";
    // 页面
    public static final String LOG_PAGE_COURSE = "course";
    public static final String LOG_PAGE_COURSE_UN = "course_un";
    public static final String LOG_PAGE_FEEDBACK = "feedback";
    public static final String LOG_PAGE_COURSE_DETAIL = "course_det";
    public static final String LOG_PAGE_TEACHER_DETAIL = "tr_det";
    public static final String LOG_PAGE_ORDER_LIST = "me_orderlist";
    public static final String LOG_PAGE_ORDER_DETAIL = "me_orderdet";
    
    // 4.8
    public static final String LOG_PAGE_MY_TEACHER = "me_tr_list";
    public static final String LOG_PAGE_TA_RECOMMEND_TEACHER = "me_ta_rec";
    public static final String LOG_PAGE_FRIEND_RECOMMEND_TEACHER = "me_friend_rec";
    public static final String LOG_PAGE_ORDER_SELECT_TIME_NUM = "me_order_time";
    public static final String LOG_PAGE_ORDER_SUCCESS = "me_order_success";// 订单预约成功
    public static final String LOG_PAGE_ME = "me_pcenter";
    public static final String LOG_PAGE_ME_WALLET = "me_wallet";
    public static final String LOG_PAGE_ME_FEEDBACK = "me_opinion";
    
    // 4.8.2
    public static final String LOG_PAGE_SEARCH_SUG = "search_sug_stu";
    
    // 4.9
    public static final String LOG_PAGE_ATTENTION = "me_follow";
    public static final String LOG_PAGE_ME_MESSAGE = "me_message";
    public static final String LOG_PAGE_SYS_MESSAGE = "me_sysmessage";
    public static final String LOG_PAGE_IM_DETAIL = "me_im_del";
    
    // 5.0.5
    public static final String LOG_PAGE_TEACHER_ALBUM = "tr_album";
    public static final String LOG_PAGE_LECTURE_LIST = "invited_speakers_list";
    public static final String LOG_PAGE_LECTURE_DETAIL = "invited_speakers_introduce";
    public static final String LOG_PAGE_LECTURE_ROOM = "invited_speakers_room";
    
    // 5.1.0
    public static final String LOG_PAGE_HEADLINES_CHANNEL = "qq_headlines_channel";
    public static final String LOG_PAGE_TEACHING_EXPERIENCE = "teaching_experience";
    public static final String LOG_PAGE_FRIENDS = "friends";
    
    // 5.1.5
    public static final String LOG_PAGE_SEARCH_RESULT = "search_result";
    
    // 5.2.5
    public static final String LOG_PAGE_FRIENDS_BUY = "friends_buy";
    public static final String LOG_PAGE_FRIENDS_DETAIL = "friends_detail";
    public static final String LOG_PAGE_FRIENDS_SUCCESS = "friends_success";
    public static final String LOG_PAGE_ASSESSMENT_CENTER = "assessment_center";
    public static final String LOG_PAGE_ASSESSMENT_DETAIL = "assessment_detail";
    public static final String LOG_PAGE_FANTA_LIST = "fanta_list";
    public static final String LOG_PAGE_FANTA_QUESTIONING = "fanta_questioning";
    public static final String LOG_PAGE_FANTA_PAY_SUCCESS = "fanta_pay_success";
    public static final String LOG_PAGE_FANTA_SEARCH_SUG = "fanta_search_sug";
    public static final String LOG_PAGE_FANTA_DETAIL = "fanta_detail";
    public static final String LOG_PAGE_FANTA_EXPERT_HOMEPAGE = "fanta_expert_homepage";
    
    // 5.3.0
    public static final String LOG_PAGE_RESERVATION_COURSE = "reservation_course";
    public static final String LOG_PAGE_CONFIRM_ORDER_TIME = "confirm_order_time";
    public static final String LOG_PAGE_NEW_CONFIRM_ORDER_TIME = "new_confirm_order_time";
    public static final String LOG_PAGE_CHOICE_ORDER_TIME = "choice_order_time";
    public static final String LOG_PAGE_FRIENDS_REORDER = "friends_reorder";
    public static final String LOG_PAGE_TEACHING_PLAN = "teaching_plan";
    public static final String LOG_PAGE_TEACHING_PLAN_DETAIL = "teaching_plan_detail";
    
    // 5.3.5
    public static final String LOG_PAGE_TOP_UP = "top_up";
    public static final String LOG_PAGE_TOP_UP_SUCCESS = "top_up_success";
    public static final String LOG_PAGE_COURSE_TIME = "course_time";
    public static final String LOG_PAGE_COURSE_FEEDBACK = "course_feedback";
    public static final String LOG_PAGE_COURSE_NO_FEEDBACK = "course_no_feedback";
    public static final String LOG_PAGE_COURSE_CALENDAR = "course_calendar";
    public static final String LOG_PAGE_HOMEWORK_DETAIL = "homework_detail";
    public static final String LOG_PAGE_HOMEWORK_UPLOAD = "homework_upload";
    public static final String LOG_PAGE_ASSESSMENT_SUCCESS = "assessment_success";
    
    // 5.4.0
    public static final String LOG_PAGE_TR_COMPARE = "tr_compare";
    public static final String LOG_PAGE_ME_TA = "me_ta";
    
    // 5.4.5
    public static final String LOG_PAGE_FRIENDS_PAYING_SERVICE = "friends_paying_service";
    public static final String LOG_PAGE_BACKLOG_MESSAGE = "backlog_message";
    public static final String LOG_PAGE_TEACHING_TASK_MESSAGE = "teaching_task_message";
    public static final String LOG_PAGE_INFORM_MESSAGE = "inform_message";
    public static final String LOG_PAGE_REFUND_MESSAGE = "refund_message";
    public static final String LOG_PAGE_ACTIVITY_MESSAGE = "activity_message";
    
    // 5.5.0
    public static final String LOG_PAGE_COURSE_CONTENT_PKG_LIST = "course_content_pkg_list";
    public static final String LOG_PAGE_COURSE_CONTENT_PKG = "course_content_pkg";
    
    // 5.5.5
    public static final String LOG_PAGE_ONLINE_AUDIT = "online_audit";
    public static final String LOG_PAGE_MY_SELECTED_COURSE_LIST = "my_selected_course_list";
    
    // 5.6.5
    public static final String LOG_PAGE_ME_REORDER = "me_reorder";
    
    // 5.7.0
    public static final String LOG_PAGE_LEARNING_CENTER = "learning_center";
    public static final String LOG_PAGE_HOMEWORK_LIST = "homework_list";
    public static final String LOG_PAGE_COURSE_FEEDBACK_DETAIL = "course_feedback_detail";
    public static final String LOG_PAGE_ONLINE_AUDIT_LIST = "online_audit_list";
    public static final String LOG_PAGE_REORDER_LIST = "reorder_list";

    //5.8.5
    public static final String LOG_PAGE_VIP = "vip";

    //5.9.0
    public static final String LOG_PAGE_ALL_QUESTIONS = "all_questions";
    public static final String LOG_PAGE_CUSTOMER_SERVICE_PROBLEM_SEARCH = "customer_service_problem_search";
    public static final String LOG_PAGE_EVALUATION_ASSISTANT = "evaluation_assistant";
    public static final String LOG_PAGE_TEACHER_SUGGEST = "teacher_suggest";
    public static final String LOG_PAGE_QUESTION_ANSWER = "question_answer";
    public static final String LOG_PAGE_ASSESSMENT_ASSISTANT_HISTORY = "assessment_assistant_history";
    public static final String LOG_PAGE_EXCLUSIVE_TUTOR = "exclusive_tutor";

    //5.9.5
    public static final String LOG_PAGE_START = "start";
    //5.9.6
    public static final String LOG_PAGE_LOGIN = "login";

    //6.0.0
    public static final String LOG_PAGE_APPOINTMENT_DETAIL = "appointment_detail";
    public static final String LOG_PAGE_APPOINTMENT_MAKE  = "appointment_make";//预约填写页面
    public static final String LOG_PAGE_APPOINTMENT_LIST  = "appointment_list";//预约列表

    //6.1.0
    public static final String LOG_PAGE_NEW_TEACHING_PLAN_LIST = "teach_plan_list";//教学计划列表
    public static final String LOG_PAGE_NEW_STAGE_SUMMARY_LIST = "stage_summary_list";//阶段总结列表
    public static final String LOG_PAGE_MY_HOMEWORK = "my_homework";//我的作业

    //6.2.0
    public static final String LOG_PAGE_TA_GAS_STATION_LIST = "ta_gas_station_list";//助教加油站列表页
    public static final String LOG_PAGE_TA_GAS_STATION_DETAIL = "ta_gas_station_detail";//助教加油站详情页
    public static final String LOG_PAGE_TA_GAS_STATION_LIVE = "ta_gas_station_live";//助教加油站直播页
    public static final String LOG_PAGE_TA_GAS_STATION_HISTORY = "ta_gas_station_history";//助教加油站重播页
    // 老师端
    // 4.8 页面
    public static final String LOG_PAGE_TEACHER_SCHEDULE = "tr_schedule";
    public static final String LOG_PAGE_TEACHER_COURSE_INFO = "tr_courseinfo";
    public static final String LOG_PAGE_TEACHER_CONTACT_LIST = "tr_contact";
    public static final String LOG_PAGE_TEACHER_STUDENT_DETAIL = "tr_finfo";
    public static final String LOG_PAGE_TEACHER_ALL_ORDER = "tr_allorder";
    public static final String LOG_PAGE_TEACHER_ORDER_INFO = "tr_orderinfo";
    public static final String LOG_PAGE_TEACHER_FEEDBACK = "tr_feedback";
    public static final String LOG_PAGE_TEACHER_ME = "tr_pcenter";
    public static final String LOG_PAGE_TEACHER_TIME_MANAGER = "tr_tmanage";
    public static final String LOG_PAGE_TEACHER_MY_INCOME = "tr_income";
    public static final String LOG_PAGE_TEACHER_MY_WALLET_LIST = "tr_wallet_detail";
    
    // 4.9
    public static final String LOG_PAGE_TEACHER_THIRD_PLACE_DETAIl = "tr_csrinfo";
    public static final String LOG_PAGE_TEACHER_STUDENT_RESOURCE = "tr_shengyuanbao";
    public static final String LOG_PAGE_TEACHER_STUDENT_RESOURCE_DETAIL = "tr_sybdet";
    public static final String LOG_PAGE_TEACHER_ADD_BANK_CARD = "tr_addcard";
    public static final String LOG_PAGE_TEACHER_WITHDRAW = "tr_withdraw";
    public static final String LOG_PAGE_TEACHER_MESSAGE = "tr_allmessage";
    public static final String LOG_PAGE_TEACHER_IM_DETAIL = "tr_chat";
    public static final String LOG_PAGE_TEACHER_SYS_MESSAGE = "tr_sysmessage";
    public static final String LOG_PAGE_TEACHER_REORDER_TIME = "tr_reorderop";
    public static final String LOG_PAGE_TEACHER_UN_PAY_OR = "tr_unpaidorder";
    public static final String LOG_PAGE_TEACHER_REODERLIST = "tr_reorderlist";
    public static final String LOG_PAGE_TEACHER_HOME = "tr_home";
    
    // 5.0
    public static final String LOG_PAGE_TEACHER_BASIC = "tr_basics";
    
    // 5.2.0
    public static final String LOG_PAGE_START_PROCESS = "tr_start_process";
    public static final String LOG_PAGE_START_CLASS = "tr_start_class";
    public static final String LOG_PAGE_MY_TA = "tr_my_ta";
    public static final String LOG_PAGE_REGISTER_SUCCESS = "tr_register_success";
    public static final String LOG_PAGE_UNBIND_TA = "tr_unbind_ta";
    
    // 5.2.5
    public static final String LOG_PAGE_TEACHER_SYB_TEACHING_TIME = "tr_syb_teaching_time";
    public static final String LOG_PAGE_TEACHER_TEACHING_TIME = "tr_teaching_time";
    public static final String LOG_PAGE_TEACHER_SYB_ENROLL = "tr_syb_enroll";
    public static final String LOG_PAGE_TEACHER_INVITED_SPEAKERS_LIST = "tr_invited_speakers_list";
    public static final String LOG_PAGE_TEACHER_INVITED_SPEAKERS_INTRODUCE = "tr_invited_speakers_introduce";
    public static final String LOG_PAGE_TEACHER_INVITED_SPEAKERS_ROOM = "tr_invited_speakers_room";
    public static final String LOG_PAGE_TEACHER_QQ_COLLEGE = "tr_qq_college";
    
    // 5.3.0
    public static final String LOG_PAGE_TEACHER_TEACHING_TASK = "tr_teaching_task";
    
    // 5.3.5
    public static final String LOG_PAGE_TEACHER_BILL_DETAIL = "tr_bill_detail";
    public static final String LOG_PAGE_TEACHER_TEACHING_COURSE = "tr_teaching_course";
    
    // 5.4.0
    public static final String LOG_PAGE_FAVORABLE_COURSE_PACKAGE = "favorable_course_package";
    
    // 5.5.0
    public static final String LOG_PAGE_SERVICE_STANDARDS = "service_standards";
    public static final String LOG_PAGE_QQ_PLATFORM_SERVICE_STANDARDS = "qq_platform_service_standards";
    public static final String LOG_PAGE_TEACHER_COURSE_CONTENT_PKG_INSTRUCTIONS = "course_content_pkg_instructions";
    public static final String LOG_PAGE_TEACHER_COURSE_CONTENT_PKG_APPLY = "course_content_pkg_apply";
    public static final String LOG_PAGE_TEACHER_COURSE_CONTENT_PKG_LIST = "course_content_pkg_list";
    public static final String LOG_PAGE_TEACHER_COURSE_CONTENT_PKG = "course_content_pkg";
    
    // 5.5.5
    public static final String LOG_PAGE_TEACHER_ONLINE_AUDIT = "online_audit";
    public static final String LOG_PAGE_COURSE_CONTENT_PKG_APPLY_LIST = "course_content_pkg_apply_list";

    // 5.6.0
    public static final String LOG_PAGE_TEACHER_COURSE_BILL = "course_bill";
    public static final String LOG_PAGE_TEACHER_COURSE_BILL_DETAIL = "course_bill_detail";
    
    // 5.6.5
    public static final String LOG_PAGE_TEACHER_ONLINE_TEACHING = "online_teaching";
    public static final String LOG_PAGE_TEACHER_ONLINE_TEACHING_APPLY_STANDARDS = "online_teaching_apply_standards";
    public static final String LOG_PAGE_TEACHER_ONLINE_TEACHING_APPLY = "online_teaching_apply";
    
    // 5.7.0
    public static final String LOG_PAGE_COURSE_REPORT_DETAIL = "course_report_detail";
    public static final String LOG_PAGE_COURSE_REPORT_CONFIRM = "course_report_confirm";
    
    // 5.7.5
    public static final String LOG_PAGE_REORDER_SETTING = "reorder_setting";

    // 5.8.0
    public static final String LOG_PAGE_GROUP_CHAT_SETTING = "group_chat_setting";
    public static final String LOG_PAGE_GROUP_CHAT = "group_chat";
    public static final String LOG_PAGE_TA_GROUP_CHAT_SETTING = "group_chat_setting";
    public static final String LOG_PAGE_TA_GROUP_CHAT = "group_chat";

    //5.8.5
    public static final String LOG_PAGE_MY_STUDENTS = "my_students";

    // TA端
    // 4.8.2 页面
    public static final String LOG_PAGE_TA_SEARCH_SUG = "search_sug_ta";
    
    // 5.3.0
    public static final String LOG_PAGE_TA_HOME = "ta_home";
    public static final String LOG_PAGE_TA_BIND_STUDENT = "ta_bind_student";
    public static final String LOG_PAGE_TA_CONTACT_DETAIL = "ta_contact_detail";
    public static final String LOG_PAGE_TA_SYB = "ta_syb";
    
    // 5.4.5
    public static final String LOG_PAGE_TA_BIND_TEACHER = "ta_bind_teacher";
    public static final String LOG_PAGE_TA_OPINION = "ta_opinion";
    public static final String LOG_PAGE_TA_BIND_STUDENT_DETAIL = "ta_bind_student_detail";
    public static final String LOG_PAGE_TA_BIND_TEACHER_DETAIL = "ta_bind_teacher_detail";
    public static final String LOG_PAGE_TA_TEACHER_DETAIL = "ta_teacher_detail";
    public static final String LOG_PAGE_TA_STUDENT_DETAIL = "ta_student_detail";

    //5.9.6
    public static final String LOG_PAGE_BASICS = "basics";
    public static final String LOG_APP_RECOMMEND_TR = "app_recommend_tr";
    public static final String LOG_APP_RECOMMEND_TR_ALL = "app_recommend_tr_all";

    //6.1.0
    public static final String LOG_PAGE_STAGE_SUMMARY_LIST = "stage_summary_list"; //阶段总结列表页面
    public static final String LOG_PAGE_STAGE_SUMMARY_EDIT = "stage_summary_edit"; //阶段总结编辑页面
    public static final String LOG_PAGE_TEACH_PLAN_EDIT = "teach_plan_edit"; //教学计划填写页面

    
    /****************************************************************************************/
    
    /**
     * 点击记录 点击级
     */
    
    // public
    public static final String CLICK_COURSE_REODER = "reorder";
    
    /***************************************
     * 家长
     ***************************************/
    // 主页
    // home === citychange：用户点击了城市切换按钮
    public static final String CLICK_HOME_CITY = "citychange";
    // home === searchbox：用户点击了用户点击了搜索按钮
    public static final String CLICK_HOME_SEARCHBOX = "searchbox";
    // home === subject：用户点击了科目
    public static final String CLICK_HOME_SUBJECT = "subject";
    // home === hottr：用户点击了热门老师
    public static final String CLICK_HOME_TEACHER = "hottr";
    // home === slide：用户点击了幻灯片
    public static final String CLICK_HOME_SLIDE = "slide";
    // home === c_headline：用户点击了轻轻头条
    public static final String CLICK_HEAD_LINE = "c_headline";
    // home === c_op_check：用户点击了运营方格
    public static final String CLICK_OP_CHECK = "c_op_check";
    // home === c_invited_speakers：用户点击了直播课堂
    public static final String CLICK_HOME_INVITED_SPEAKERS = "c_invited_speakers";
    // home === c_close：用户点击了关闭提醒
    public static final String CLICK_HOME_CLOSE = "c_close";
    // home === c_close：用户点击了提醒
    public static final String CLICK_NEWS_INFO = "c_news_info";
    // home === c_opinion：用户点击了校长信箱
    public static final String CLICK_HOME_OPINION = "c_opinion";
    // home === c_online_course：用户点击了在线课
    public static final String CLICK_HOME_C_ONLINE_COURSE = "c_online_course";


    // 首页进入的老师列表
    // tr_list === enter_tr_pape：用户点击了进入老师主页的入口
    public static final String CLICK_TEACHER_LIST_ON_TEACHER_HOME = "trpape";
    // tr_list === gradechoice：用户点击了年级选择
    public static final String CLICK_TEACHER_LIST_GRADE = "gradechoice";
    // tr_list === c_subject_change：用户点击了科目切换
    public static final String CLICK_TEACHER_LIST_SUBJECT_CHANGE = "c_subject_change";
    // tr_list === c_address_change：用户点击了地址切换
    public static final String CLICK_TEACHER_LIST_ADDRESS_CHANGE = "c_address_change";
    // tr_list === c_level_two_subject：用户点击了二级科目
    public static final String CLICK_TEACHER_LIST_LEVEL_TWO_SUBJECT = "c_level_two_subject";
    // tr_list === c_teaching_contents：用户点击了教学内容
    public static final String CLICK_TEACHER_LIST_TEACHING_CONTENTS = "c_teaching_contents";
    // tr_list === c_teaching_features：用户点击了教学特点
    public static final String CLICK_TEACHER_LIST_TEACHING_FEATURES = "c_teaching_features";
    // tr_list === c_search：用户点击了搜索
    public static final String CLICK_TEACHER_LIST_SEARCH = "c_search";
    // tr_list === c_site_type：用户点击了上门方式
    public static final String CLICK_TEACHER_LIST_SITE_TYPE = "c_site_type";
    // tr_list === c_screen：用户点击了筛选
    public static final String CLICK_TEACHER_LIST_SCREEN = "c_screen";
    
    // 老师主页
    // tr_page === trconsult：用户点击了和老师一对一咨询按钮
    public static final String CLICK_TEACHER_HOME_TRCONSULT = "trconsult";
    // tr_page === tr_avatar：用户点击了老师头像
    public static final String CLICK_TEACHER_HOME_TR_AVATAR = "tr_avatar";
    // tr_page === c_seniority：用户点击了教龄
    public static final String CLICK_TEACHER_HOME_SENIORITY = "c_seniority";
    // tr_page === c_stu_cnt：用户点击了学生数
    public static final String CLICK_TEACHER_HOME_STU_CNT = "c_stu_cnt";
    // tr_page === c_class_hour：用户点击了课时
    public static final String CLICK_TEACHER_HOME_CLASS_HOUR = "c_class_hour";
    // tr_page === c_reorder_rate：用户点击了好评数
    public static final String CLICK_TEACHER_HOME_PRAISE_CNT = "c_praise_cnt";
    // tr_page === c_star_level：用户点击了星级
    public static final String CLICK_TEACHER_HOME_STAR_LEVEL = "c_star_level";
    // tr_page === c_cert：用户点击了老师认证栏
    public static final String CLICK_TEACHER_HOME_CERT = "c_cert";
    // tr_page === c_tr_address：用户点击了老师地址
    public static final String CLICK_TEACHER_HOME_TR_ADDRESS = "c_tr_address";
    // tr_page === c_stu_appraise：用户点击了学生评论板块
    public static final String CLICK_TEACHER_HOME_STU_APPRAISE = "c_stu_appraise";
    // tr_page === c_glory：用户点击了荣耀板块
    public static final String CLICK_TEACHER_HOME_GLORY = "c_glory";
    // tr_page === c_experience：用户点击了教学经历板块
    public static final String CLICK_TEACHER_HOME_EXPERIENCE = "c_experience";
    // tr_page === c_success_example：用户点击了成功案例板块
    public static final String CLICK_TEACHER_HOME_SUCCESS_EXAMPLE = "c_success_example";
    // tr_page === c_order：用户点击了预约按钮
    public static final String CLICK_TEACHER_HOME_ORDER = "c_order";
    // tr_page === c_max_card：用户点击了二维码名片
    public static final String CLICK_TEACHER_HOME_MAX_CARD = "c_max_card";
    // tr_page === c_collect：用户点击了收藏按钮
    public static final String CLICK_TEACHER_HOME_COLLECT = "c_collect";
    // tr_page === c_vodio：用户点击了视频
    public static final String CLICK_TEACHER_HOME_VIDEO = "c_vodio";
    // tr_page === c_audio：用户点击了音频
    public static final String CLICK_TEACHER_HOME_AUDIO = "c_audio";
    // tr_page === c_teaching_experience：用户点击了教学心得板块
    public static final String CLICK_TEACHER_HOME_TEACHING_EXPERIENCE = "c_teaching_experience";
    // tr_page === c_friend：用户点击了朋友团按钮
    public static final String CLICK_TEACHER_HOME_FRIEND = "c_friend";
    // tr_page === c_compare：用户点击了加入对比
    public static final String CLICK_TEACHER_HOME_ADD_COMPARE = "c_add_compare";
    // tr_page === c_buy_tips：用户点击了购买提示
    public static final String CLICK_TEACHER_HOME_BUY_TIPS = "c_buy_tips";
    // tr_page === c_course：用户点击了课程
    public static final String CLICK_TEACHER_HOME_COURSE = "c_course";
    // tr_page === c_online_course：用户点击了在线课
    public static final String CLICK_TEACHER_HOME_ONLINE_COURSE = "c_online_course";
    // tr_page === c_teaching_features：用户点击了教学特点
    public static final String CLICK_TEACHER_HOME_TEACHING_FEATURES = "c_teaching_features";
    // tr_page === c_recommend_tr：用户点击了推荐老师
    public static final String CLICK_TEACHER_HOME_RECOMMEND_TR = "c_recommend_tr";
    // tr_page === c_teach_students：用户点击了教过的学生
    public static final String CLICK_TEACHER_HOME_TEACH_STUDENTS = "c_teach_students";
    
    public static final String CLICK_GO_TEACHER = "tr_pape";

    // tr_page === c_appointment：用户点击了预约悬浮位
    public static final String CLICK_APPOINTMENT = "c_appointment";
    
    // 支付页面
    // order_payment === payment_confirm：用户点击确认支付
    public static final String CLICK_ORDER_PAY_CONFIRM = "confirm_payment";
    // order_payment === third_payment_choice：选择第三方支付的一种
    public static final String CLICK_ORDER_PAY_CHOICE = "third_payment_choice";
    // order_payment === c_top_up：充值
    public static final String CLICK_ORDER_PAY_TOP_UP = "c_top_up";
    
    // 课程详情
    // course_det === reorder：用户点击了续课按钮
    public static final String CLICK_COURSE_DETAIL_REP_COURSE = "reorder";
    // course_det === tr_avatar：用户点击了老师头像
    public static final String CLICK_COURSE_DETAIL_TEACHER_HEAD = "tr_avatar";
    // course_det === im：用户点击了私聊入口
    public static final String CLICK_COURSE_DETAIL_SEND_MESSAGE = "im";
    // course_det === trphone：用户点击了老师电话
    public static final String CLICK_COURSE_DETAIL_TRPHONE = "trphone";
    // course_det === c_import_calender：用户点击了导入日历
    public static final String CLICK_COURSE_DETAIL_IMPORT_CALENDER = "c_import_calender";
    // course_det === c_contact_ta：用户点击了联系助教
    public static final String CLICK_COURSE_DETAIL_CONTACT_TA = "c_contact_ta";
    // course_det === c_preview：用户点击了课前预习
    public static final String CLICK_COURSE_DETAIL_PREVIEW = "c_preview";
    // course_det === c_homework：用户点击了课后作业
    public static final String CLICK_COURSE_DETAIL_HOMEWORK = "c_homework";
    // course_det === c_show_feedback：用户点击了查看全部课程反馈
    public static final String CLICK_COURSE_DETAIL_SHOW_FEEDBACK = "c_show_feedback";
    // course_det === c_appraise：用户点击了评价
    public static final String CLICK_COURSE_DETAIL_APPRAISE = "c_appraise";
    // course_det === c_finish_class：用户点击了结课
    public static final String CLICK_COURSE_DETAIL_FINISH_CLASS = "c_finish_class";
    // course_det === c_additional：用户点击了附加按钮
    public static final String CLICK_COURSE_DETAIL_ADDITIONAL = "c_additional";
    // course_det === c_follow_service：用户点击了关注服务号
    public static final String CLICK_COURSE_DETAIL_FOLLOW_SERVICE = "c_follow_service";
    
    // 老师详情
    // tr_det === reorder：用户点击了续课
    public static final String CLICK_TEACHER_DETAIL_REP_COURSE = "reorder";
    // tr_det === trpage：用户点击了老师主页入口
    public static final String CLICK_TEACHER_DETAIL_TEACHER_HOME = "trpape";
    // tr_det === im：用户点击了发送消息
    public static final String CLICK_TEACHER_DETAIL_SEND_MESSAGE = "im";
    // tr_det === phone：用户点击了电话老师按钮
    public static final String CLICK_TEACHER_DETAIL_PHONE = "phone";
    // tr_det === c_teaching_plan：用户点击了教学计划与总结
    public static final String CLICK_TEACHER_DETAIL_TEACHING_PLAN = "c_teaching_plan";
    // tr_det === c_auto_end：用户点击了自动结课
    public static final String CLICK_TEACHER_DETAIL_AUTO_END = "c_auto_end";
    
    // 我的订单
    // home === reorder：用户点击了续课按钮
    public static final String CLICK_MY_ORDER_REP_COURSE = "reorder";
    
    // 预约成功页
    // me_order_success === taphone：助教电话按钮
    public static final String CLICK_ORDER_SUC_TA_PHONE = "taphone";
    // me_order_success === trphone：老师电话按钮
    public static final String CLICK_ORDER_SUC_TEACHER_PHONE = "trphone";
    // me_order_success === home：回首页
    public static final String CLICK_ORDER_SUC_BACK_HOME = "home";
    // me_order_success === orderdet：订单详情
    public static final String CLICK_ORDER_SUC_ORDER_DETAIl = "orderdet";
    // me_order_success === c_banner：用户点击了banner图片
    public static final String CLICK_ORDER_SUC_BANNER = "c_banner";
    // me_order_success === c_online_course_help：用户点击了在线课程帮助
    public static final String CLICK_ORDER_SUC_ONLINE_COURSE_HELP = "c_online_course_help";
    // me_order_success === c_follow_service：用户点击了关注服务号
    public static final String CLICK_ORDER_SUC_FOLLOW_SERVICE = "c_follow_service";
    
    // 我的老师
    // me_tr_list === trpage：用户点击了老师主页入口
    public static final String CLICK_MY_TEACHER_TEACHER_HOME = "trpape";
    // me_tr_list === reorder：用户点击了续课
    public static final String CLICK_MY_TEACHER_REP_COURSE = "reorder";
    // me_tr_list === im：用户点击了发送消息
    public static final String CLICK_MY_TEACHER_SEND_MESSAGE = "im";
    // me_tr_list === phone：用户点击了电话老师按钮
    public static final String CLICK_MY_TEACHER_PHONE = "phone";
    
    // 选择课次、时间界面
    // me_order_time === once：每周一次按钮
    public static final String CLICK_SELECT_TIME_ORDER_ONCE = "once";
    // me_order_time === twice：每周二次按钮
    public static final String CLICK_SELECT_TIME_ORDER_TWICE = "twice";
    // me_order_time === custom：自定义时间
    public static final String CLICK_SELECT_TIME_ORDER_CUSTOM = "custom";
    // me_order_time === custom：自定义时间
    public static final String CLICK_SELECT_TIME_ORDER_NUM = "c_choice_course_num";
    
    // 我的订单详情
    // me_orderdet === reorder：用户点击了续课按钮
    public static final String CLICK_MY_ORDER_DETAIL_REP_COURSE = "reorder";
    // me_orderdet === confirm_payment：确认支付按钮
    public static final String CLICK_MY_ORDER__SUCCESS_CONFIRM = "confirm_payment";
    // me_orderdet === cancel_order：取消支付按钮
    public static final String CLICK_MY_ORDER_SUCCESS_CANCEL = "cancel_order";
    // me_orderdet === c_invite_friend：用户点击了邀请好友付款按钮
    public static final String CLICK_MY_ORDER_SUCCESS_INVITE_FRIEND = "c_invite_friend";
    
    // 我页面
    // me_pcenter === order_all：全部订单
    public static final String CLICK_ME_ORDER_ALL = "order_all";
    // me_pcenter === order_pend_pay：待付款订单
    public static final String CLICK_ME_WAIL_PAY_ORDER = "order_pend_pay";
    // me_pcenter === order_pend_confirm：待确认订单
    public static final String CLICK_ME_WAIL_CONFIRM = "order_pend_confirm";
    // me_pcenter === coupon：奖学券
    public static final String CLICK_ME_COUPON = "coupon";
    // me_pcenter === usehelp：使用帮助
    public static final String CLICK_ME_HELP = "usehelp";
    // me_pcenter === c_face_area：用户点击了头像区域
    public static final String CLICK_ME_FACE_AREA = "c_face_area";
    // me_pcenter === c_my_order：用户点击了我的订单
    public static final String CLICK_ME_MY_ORDER = "c_my_order";
    // me_pcenter === c_wallet：用户点击了我的钱包
    public static final String CLICK_ME_WALLET = "c_wallet";
    // me_pcenter === c_appraise：用户点击了我的课程评论
    public static final String CLICK_ME_APPRAISE = "c_appraise";
    // me_pcenter === c_fenta：用户点击了我的问答
    public static final String CLICK_ME_FENTA = "c_fenta";
    // me_pcenter === c_teaching_plan：教学计划与总结
    public static final String CLICK_ME_TEACHING_PLAN = "c_teaching_plan";
    // me_pcenter === c_my_teacher：我的老师
    public static final String CLICK_ME_MY_TEACHER = "c_my_teacher";
    // me_pcenter === c_my_ta_teacher：助教推荐的老师
    public static final String CLICK_ME_MY_TA_TEACHER = "c_my_ta_teacher";
    // me_pcenter === c_my_browse_teacher：我浏览的老师
    public static final String CLICK_ME_MY_BROWSE_TEACHER = "c_my_browse_teacher";
    // me_pcenter === c_my_favorite_teacher：我收藏的老师
    public static final String CLICK_ME_MY_FAVORITE_TEACHER = "c_my_favorite_teacher";
    // me_pcenter === c_invite_studtent：邀请家长
    public static final String CLICK_ME_INVITE_STUDTENT = "c_invite_studtent";
    // me_pcenter === c_i_am_teacher：我是老师
    public static final String CLICK_ME_I_AM_TEACHER = "c_i_am_teacher";
    // me_pcenter === c_opinion：校长信箱
    public static final String CLICK_ME_OPINION = "c_opinion";
    // me_pcenter === c_complain：客服电话
    public static final String CLICK_ME_COMPLAIN = "c_complain";
    // me_pcenter === c_set：设置
    public static final String CLICK_ME_SET = "c_set";
    // me_pcenter === c_message：点击消息
    public static final String CLICK_ME_MESSAGE = "c_message";
    // me_pcenter === c_course_content_pkg：点击内容课程包
    public static final String CLICK_ME_COURSE_CONTENT_PKG = "c_course_content_pkg";
    // me_pcenter === c_follow_service：点击关注服务号
    public static final String CLICK_ME_FOLLOW_SERVICE = "c_follow_service";
    // me_pcenter === c_integral：点击积分
    public static final String CLICK_ME_INTEGRAL = "c_integral";
    // me_pcenter === c_vip：点击会员
    public static final String CLICK_ME_C_VIP = "c_vip";
    // me_pcenter === c_my_homework：我的作业
    public static final String CLICK_ME_C_MY_HOMEWORK = "c_my_homework";
    // vip === c_vip：点击成长值
    public static final String CLICK_ME_C_GROWTH_VALUE_EXPLAIN = "c_growth_value_explain";
    // vip === c_privilege：点击特权
    public static final String CLICK_ME_C_PRIVILEGE = "c_privilege";
    // vip === c_my_ta：点击助教
    public static final String CLICK_ME_C_MY_TA = "c_my_ta";


    
    /**
     * 4.8.2
     */
    // me_follow === trpape：我的关注用户点击了进入老师主页的入口
    public static final String CLICK_STUDENT_ATTENTIO = "trpape";
    // me_im_del === reorder：用户点击了续课按钮 复用
    
    /**
     * 4.9
     */
    // search_sug_stu === c_search_choice：搜索老师、ta
    public static final String CLICK_STUDENT_SEARCH_TEACHER_TA = "c_search_choice";
    // search_sug_stu === c_search_info：点击了搜索
    public static final String CLICK_STUDENT_SEARCH_INFO = "c_search_info";
    
    /**
     * 5.0.5
     */
    // 老师相册
    // tr_album === c_vodio：用户点击了视频
    public static final String CLICK_TEACHER_ALBUM_VIDEO = "c_vodio";
    // tr_album === c_audio：用户点击了视频
    public static final String CLICK_TEACHER_ALBUM_AUDIO = "c_audio";
    
    // 家长学堂列表
    // invited_speakers_list === c_set_remind：用户点击了设置提醒
    public static final String CLICK_LECTURE_LIST_SET_REMIND = "c_set_remind";
    // invited_speakers_list === share_friends：与朋友分享
    public static final String CLICK_LECTURE_LIST_SHARE_FRIENDS = "share_friends";
    // invited_speakers_list === gradechoice：用户点击了年级选择
    public static final String CLICK_LECTURE_LIST_GRADECHOICE = "gradechoice";
    // 用户点击进入直播详情
    public static final String CLICK_LECTURE_LIST_LIVE_DETAIL = "c_live_detail";
    // 助教加油站详情页点击进入直播页
    public static final String CLICK_LECTURE_ENTER_LIVE = "c_enter_live";
    // 直播页点击播放音频
    public static final String CLICK_LECTURE_PLAY_AUDIO = "c_play_audio";
    
    // 家长学堂介绍页
    // invited_speakers_introduce === share_friends：与朋友分享
    public static final String CLICK_LECTURE_DETAIL_SHARE_FRIENDS = "share_friends";
    // invited_speakers_introduce === c_say_expert：用户点击了查看主讲专家
    public static final String CLICK_LECTURE_DETAIL_SAY_EXPERT = "c_say_expert";
    // invited_speakers_introduce === c_enter_room：用户点击了进入直播间
    public static final String CLICK_LECTURE_DETAIL_ENTER_ROOM = "c_enter_room";
    // invited_speakers_introduce === c_pay：用户点击了去支付
    public static final String CLICK_LECTURE_DETAIL_PAY = "c_pay";
    
    // 家长学堂播放页
    // invited_speakers_room === c_only_expert：用户点击了只看专家按钮
    public static final String CLICK_LECTURE_ROOM_ONLY_EXPERT = "c_only_expert";
    // invited_speakers_room === c_expert_avatar：用户点击了专家头像
    public static final String CLICK_LECTURE_ROOM_EXPERT_AVATAR = "c_expert_avatar";
    // invited_speakers_room === c_voice：用户点击了语音
    public static final String CLICK_LECTURE_ROOM_VOICE = "c_voice";
    // invited_speakers_room === c_picture：用户点击了图片
    public static final String CLICK_LECTURE_ROOM_PICTURE = "c_picture";
    // invited_speakers_room === c_word：用户点击了文字
    public static final String CLICK_LECTURE_ROOM_WORD = "c_word";
    // invited_speakers_room === c_progress_bar：用户拖动了进度条
    public static final String CLICK_LECTURE_ROOM_PROGRESS_BAR = "c_progress_bar";
    // invited_speakers_room === c_pause：用户点击了暂停/播放按钮
    public static final String CLICK_LECTURE_ROOM_PAUSE = "c_pause";
    // invited_speakers_room === c_get_back：用户点击了回到播放中按钮
    public static final String CLICK_LECTURE_ROOM_GET_BACK = "c_get_back";
    // invited_speakers_room === v_c_exit： 用户离开了该页面（虚拟按钮）
    public static final String CLICK_LECTURE_EXIT_ROOM = "v_c_exit";
    // invited_speakers_room === c_page_turning： 用户点击了PPT翻页
    public static final String CLICK_LECTURE_PAGE_TURNING = "c_page_turning";
    // invited_speakers_room === c_ppt_magnify： 用户点击了PPT放大（全屏）
    public static final String CLICK_LECTURE_PPT_MAGNIFY = "c_ppt_magnify";
    // invited_speakers_room === c_ppt_return： 用户点击了PPT返回播放
    public static final String CLICK_LECTURE_PPT_RETURN = "c_ppt_return";
    // invited_speakers_room === c_ppt_unfold： 用户点击了PPT展开(收起后的展开)
    public static final String CLICK_LECTURE_PPT_UNFOLD = "c_ppt_unfold";
    // invited_speakers_room === c_ppt_stop： 用户点击了PPT收起
    public static final String CLICK_LECTURE_PPT_STOP = "c_ppt_stop";
    // invited_speakers_room === c_ppt_click： 用户点击了PPT(触发按钮框)
    public static final String CLICK_LECTURE_PPT_CLICK = "c_ppt_click";
    
    // 轻轻头条频道页
    // qq_headlines_channel === gradechoice：用户点击了年级选择
    public static final String CLICK_HEADLINES_CHANNEL_GRADE_CHOICE = "gradechoice";
    // qq_headlines_channel === slide：用户点击了幻灯片
    public static final String CLICK_HEADLINES_CHANNEL_SLIDE = "slide";
    // qq_headlines_channel === c_headline：用户点击了轻轻头条
    public static final String CLICK_HEADLINES_CHANNEL_HEADLINE = "c_headline";
    
    // 搜索结果页
    // search_result === c_subject_change：用户点击了科目切换
    public static final String CLICK_SEARCH_RESULT_SUBJECT_CHANGE = "c_subject_change";
    // search_result === gradechoice：用户点击了年级选择
    public static final String CLICK_SEARCH_RESULT_SUBJECT_GRADECHOICE = "gradechoice";
    // search_result === c_sort：用户点击了智能排序
    public static final String CLICK_SEARCH_RESULT_SORT = "c_sort";
    // search_result === c_teaching_area：用户点击了授课区域
    public static final String CLICK_SEARCH_RESULT_ADDRESS_CHANGE = "c_address_change";
    // search_result === c_screen： 用户点击了筛选项
    public static final String CLICK_SEARCH_RESULT_SCREEN = "c_screen";
    
    // 5.1.0
    // 朋友团
    // qq_headlines_channel === c_join：用户点击了参加按钮
    public static final String CLICK_GROUP_JOIN = "c_join";
    
    // 5.2.5
    // 朋友团2.0购买页面
    public static final String CLICK_FRIENDS_BUY_JOIN = "c_join";
    public static final String CLICK_FRIENDS_BUY_QUESTION_MARK = "c_question_mark";
    // 朋友团详情
    public static final String CLICK_FRIENDS_DETAIL_AFRESH_GROUP = "c_afresh_group";
    // 朋友团开团成功
    public static final String CLICK_FRIENDS_SUCCESS_INVITE_FRIEND = "c_invite_friend";
    public static final String CLICK_FRIENDS_SUCCESS_ORDERDET = "c_orderdet";
    public static final String CLICK_FRIENDS_SUCCESS_RETURN_HOMEPAGE = "c_return_homepage";
    
    // 分答列表页
    public static final String CLICK_FANTA_LIST_GRADECHOICE = "gradechoice";
    public static final String CLICK_FANTA_LIST_FENTA_SCREEN = "c_fenta_screen";
    public static final String CLICK_FANTA_LIST_INFORMATION_SCREEN = "c_information_screen";
    public static final String CLICK_FANTA_LIST_SORT = "c_sort";
    public static final String CLICK_FANTA_LIST_FENTA = "c_fenta";
    public static final String CLICK_FANTA_LIST_INFORMATION = "c_information";
    public static final String CLICK_FANTA_LIST_SEARCHBOX = "searchbox";
    public static final String CLICK_FANTA_LIST_QUESTIONING = "c_questioning";
    // 我要提问页面
    public static final String CLICK_FANTA_QUESTIONING_PAY = "c_pay";
    // 提问支付成功页
    public static final String CLICK_FANTA_PAY_SUCCESS_MY_QUESTION = "c_my_question";
    public static final String CLICK_FANTA_PAY_SUCCESS_MORE = "c_more";
    // 分答搜索页
    public static final String CLICK_FANTA_SEARCH_SUG_SEARCHBOX = "searchbox";
    public static final String CLICK_FANTA_SEARCH_SUG_FENTA_SCREEN = "c_fenta_screen";
    public static final String CLICK_FANTA_SEARCH_SUG_INFORMATION_SCREEN = "c_information_screen";
    public static final String CLICK_FANTA_SEARCH_SUG_FENTA = "c_fenta";
    public static final String CLICK_FANTA_SEARCH_SUG_INFORMATION = "c_information";
    // 问题详情页
    public static final String CLICK_FANTA_DETAIL_AVAIL = "c_avail";
    public static final String CLICK_FANTA_DETAIL_UNAVAIL = "c_unavail";
    public static final String CLICK_FANTA_DETAIL_ALL = "c_all";
    public static final String CLICK_FANTA_DETAIL_VOICE = "c_voice";
    public static final String CLICK_FANTA_DETAIL_SHARE_FRIENDS = "share_friends";
    // 专家问答主页
    public static final String CLICK_FANTA_EXPERT_HOMEPAGE_SORT = "c_sort";
    public static final String CLICK_FANTA_EXPERT_HOMEPAGE_EXPERT_AVATAR = "c_expert_avatar";
    public static final String CLICK_FANTA_EXPERT_HOMEPAGE_FENTA = "c_fenta";
    
    // 5.3.0
    // 预约课程页面
    public static final String CLICK_RESERVATION_COURSE_COURSE_NUM = "c_course_num";
    public static final String CLICK_RESERVATION_COURSE_CLASS_TIME = "c_class_time";
    public static final String CLICK_RESERVATION_COURSE_BACK_ANY_TIME = "c_back_any_time";
    public static final String CLICK_RESERVATION_COURSE_BUY = "c_buy";
    // 确认上课时间页面
    public static final String CLICK_CONFIRM_ORDER_TIME_CHOICE_ORDER_TIME = "c_choice_order_time";
    //5.9.6下单-旧页面选择时间开始
    public static final String CLICK_CONFIRM_ORDER_TIME_CALNEXT = "c_calnext";
    //5.9.6 login
    public static final String CLICK_C_IDENTIFYINGCODE = "c_identifyingcode";

    public static final String CLICK_C_IDENTIFYINGLOGIN = "c_identifyinglogin";



    // 朋友团续课页面
    public static final String CLICK_FRIENDS_REORDER_RECORDER = "c_reorder";
    public static final String CLICK_FRIENDS_REORDER_CHANGE_MEMBER = "c_change_member";
    
    // 5.3.5
    // 轻轻钱包
    public static final String CLICK_ME_WALLET_SET = "c_set";
    public static final String CLICK_ME_WALLET_TOP_UP = "c_top_up";
    public static final String CLICK_ME_WALLET_WALLET_DETAIL = "c_wallet_detail";
    public static final String CLICK_ME_WALLET_WITHDRAW = "c_withdraw";
    // 充值页面
    public static final String CLICK_TOP_UP_FIXED_AMOUNT = "c_fixed_amount";
    public static final String CLICK_TOP_UP_OTHER_AMOUNT = "c_other_amount";
    public static final String CLICK_TOP_UP_PROTOCOL = "c_protocol";
    public static final String CLICK_TOP_UP_TOP_UP = "c_top_up";
    public static final String CLICK_TOP_UP_RULES = "c_rules";
    // 充值成功页面
    public static final String CLICK_TOP_UP_SUCCESS_FINISH = "c_finish";
    public static final String CLICK_TOP_UP_SUCCESS_COUPON = "c_coupon";
    // 课程表页面
    public static final String CLICK_COURSE_REORDER = "reorder";
    public static final String CLICK_COURSE_CONTACT_TR = "c_contact_tr";
    public static final String CLICK_COURSE_APPRAISE = "c_appraise";
    public static final String CLICK_COURSE_FINISH_CLASS = "c_finish_class";
    // 课程未反馈页面
    public static final String CLICK_COURSE_NO_FEEDBACK_FEEDBACK = "c_feedback";
    // 作业详情页面
    public static final String CLICK_HOMEWORK_DETAIL_PICTURE = "c_picture";
    public static final String CLICK_HOMEWORK_DETAIL_VOICE = "c_voice";
    public static final String CLICK_HOMEWORK_DETAIL_QUESTION_BANK = "c_question_bank";
    public static final String CLICK_HOMEWORK_DETAIL_FINISH = "c_finish";
    public static final String CLICK_HOMEWORK_DETAIL_UPLOAD = "c_upload";
    public static final String CLICK_HOMEWORK_DETAIL_FOLLOW_SERVICE = "c_follow_service";
    public static final String CLICK_HOMEWORK_DETAIL_COURSE_DETAIL = "c_course_detail";
    public static final String CLICK_HOMEWORK_DETAIL_SHOW = "c_show";
    // 评价成功
    public static final String CLICK_ASSESSMENT_SUCCESS_RETURN_HOMEPAGE = "c_return_homepage";
    public static final String CLICK_ASSESSMENT_SUCCESS_SHOW_ALL = "c_show_all";
    public static final String CLICK_ASSESSMENT_SUCCESS_MY_ASSESSMENT = "c_my_assessment";
    public static final String CLICK_ASSESSMENT_SUCCESS_FINISH = "c_finish";
    public static final String CLICK_ASSESSMENT_SUCCESS_INTEGRAL = "c_integral";
    public static final String CLICK_ASSESSMENT_SUCCESS_ASSESSMENT = "c_assessment";
    
    // 5.4.0
    // 老师对比
    public static final String CLICK_TR_COMPARE_ADD_TR = "c_add_tr";
    
    // 5.4.5
    // 消息
    public static final String CLICK_ME_MESSAGE_IM = "im";
    // 轻轻活动 / 退款消息 / 通知消息 / 教学任务 / 待办事项 统一使用一个字段
    public static final String CLICK_ME_MESSAGE_MESSAGE = "c_message";
    
    // 5.5.0
    // 精选课程列表
    public static final String CLICK_COURSE_LIST_COURSE_CONTENT_PKG = "c_course_content_pkg";
    // 内容课程包
    public static final String CLICK_COURSE_CONTENT_PKG_INTRODUCTION = "c_introduction";
    public static final String CLICK_COURSE_CONTENT_PKG_OUTLINE = "c_outline";
    public static final String CLICK_COURSE_CONTENT_PKG_BUY = "c_buy";
    public static final String CLICK_COURSE_CONTENT_PKG_FRIENDS_BUY = "c_friends_buy";
    public static final String CLICK_COURSE_CONTENT_PKG_IM = "im";
    public static final String CLICK_COURSE_CONTENT_PKG_TR_AVATAR = "tr_avatar";
    
    // 5.6.0
    // 找老师的搜索页面
    public static final String CLICK_SEARCH_FOR_FIND_TEACHER_HOT_SEARCH = "c_hot_search";
    public static final String CLICK_SEARCH_FOR_FIND_TEACHER_HIST_SEARCH = "c_hist_search";
    public static final String CLICK_SEARCH_FOR_FIND_TEACHER_SEARCH = "c_search";
    // 在线旁听重播页
    public static final String CLICK_ONLINE_AUDIT_REPLAY_REPLAY = "c_replay";
    public static final String CLICK_ONLINE_AUDIT_REPLAY_PAUSE = "c_pause";
    public static final String CLICK_ONLINE_AUDIT_REPLAY_PAGE_TURNING = "c_page_turning";
    
    // 5.6.5
    // 续课确认页
    public static final String CLICK_ME_REORDER_EDIT = "c_edit";

    // 5.7.0
    // 续课列表页
    public static final String CLICK_REORDER_LIST_CLOSE = "c_close";
    public static final String CLICK_REORDER_LIST_REORDER = "c_reorder";
    // 学习中心
    public static final String CLICK_LEARNING_CENTER_CLASS_DROP = "c_class_drop";
    public static final String CLICK_LEARNING_CENTER_CLASS_ADJUSTMENT = "c_class_adjustment";
    public static final String CLICK_LEARNING_CENTER_CONTACT_TA = "c_contact_ta";
    public static final String CLICK_LEARNING_CENTER_TEACHING_PLAN = "c_teaching_plan";
    public static final String CLICK_LEARNING_CENTER_STAGE_SUMMARY = "c_stage_summary";
    public static final String CLICK_LEARNING_CENTER_ONLINE_AUDIT = "c_online_audit";
    public static final String CLICK_LEARNING_CENTER_PREVIEW = "c_preview";
    public static final String CLICK_LEARNING_CENTER_FEEDBACK = "c_feedback";
    public static final String CLICK_LEARNING_CENTER_HOMEWORK = "c_homework";
    public static final String CLICK_LEARNING_CENTER_MORE = "c_more";
    public static final String CLICK_LEARNING_CENTER_PREVIEW_COURSE = "c_preview_course";
    public static final String CLICK_LEARNING_CENTER_HOMEWORK_COURSE = "c_homework_course";
    public static final String CLICK_LEARNING_CENTER_FEEDBACK_COURSE = "c_feedback_course";
    public static final String CLICK_LEARNING_CENTER_FINISH_COURSE = "c_finish_course";
    public static final String CLICK_LEARNING_CENTER_ONLINE_AUDIT_COURSE = "c_online_audit_course";
    public static final String CLICK_LEARNING_CENTER_REORDER = "c_reorder";
    public static final String CLICK_LEARNING_CENTER_SUBJECT = "c_subject";
    public static final String CLICK_LEARNING_CENTER_DO_HOMEWORK_LATER = "c_do_homework_later";


    //5.9.5
    public static final String CLICK_START = "c_start";

    //6.0.0
    public static final String CLICK_ENROLLED_TR = "c_enrolled_tr";
    public static final String CLICK_CANCEL = "c_cancel";

    //6.1.0
    public static final String CLICK_PREVIEW_HIST = "c_preview_hist";
    public static final String CLICK_HOMEWORK_HIST = "c_homework_hist";

    // 教学计划与总结列表页
    public static final String CLICK_TEACHING_PLAN_DETAIL = "c_detail";
    // 作业列表面
    public static final String CLICK_HOMEWORK_LIST_DETAIL = "c_detail";
    // 课程反馈列表页
    public static final String CLICK_COURSE_FEEDBACK_DETAIL = "c_detail";
    // 课程反馈详情页
    public static final String CLICK_COURSE_FEEDBACK_DETAIL_THANK = "c_thank";
    public static final String CLICK_COURSE_FEEDBACK_DETAIL_REPLY = "c_reply";
    // 在线旁听列表页
    public static final String CLICK_ONLINE_AUDIT_LIST_DETAIL = "c_detail";
    //推荐老师
    public static final String CLICK_RECOMMEND_TEACHER = "c_recommend_teacher";


    /***************************************
     * 老师
     ***************************************/
    // 老师课程表
    // tr_schedule === schedule_cb：用户点击了上课考勤
    public static final String CLICK_TEACHER_SCHEDULE_HAVE_CLASS = "class_begin";
    public static final String CLICK_TEACHER_SCHEDULE_FACE_AREA = "c_face_area";
    public static final String CLICK_TEACHER_SCHEDULE_TIPS = "c_tips";
    public static final String CLICK_TEACHER_SCHEDULE_COURSEINFO = "c_courseinfo";
    public static final String CLICK_TEACHER_SCHEDULE_SYB_TAB = "c_syb_tab";
    
    // 老师课程详情
    // tr_courseinfo === courseinfo_reorder：用户点击了续课按钮
    public static final String CLICK_TEACHER_COURSE_DETAIL_REP_COURSE = "reorder";
    // tr_courseinfo === courseinfo_cb：用户点击了上课考勤
    public static final String CLICK_TEACHER_COURSE_DETAIL_HAVE_CLASS = "courseinfo_cb";
    // tr_courseinfo === c_navigation：导航栏按钮
    public static final String CLICK_TEACHER_NAVIGATION = "c_navigation";
    public static final String CLICK_TEACHER_COURSE_DETAIL_TEACHING_PLAN = "c_teaching_plan";
    public static final String CLICK_TEACHER_COURSE_DETAIL_STAGE_SUMMARY = "c_stage_summary";
    public static final String CLICK_TEACHER_COURSE_DETAIL_REPORT = "c_report";
    public static final String CLICK_TEACHER_COURSE_DETAIL_ONLINE_AUDIT_RECORDING = "c_online_audit_recording";
    public static final String CLICK_TEACHER_COURSE_DETAIL_ONLINE_AUDIT_REPLAY = "c_online_audit_replay";
    public static final String CLICK_TEACHER_COURSE_DETAIL_PREVIEW_HIST = "c_preview_hist";
    public static final String CLICK_TEACHER_COURSE_DETAIL_HOMEWORK_HIST = "c_homework_hist";
    public static final String CLICK_TEACHER_COURSE_DETAIL_FEEDBACK_HIST = "c_feedback_hist";
    public static final String CLICK_TEACHER_COURSE_DETAIL_TEACHING_PLAN_HIST = "c_teaching_plan_hist";
    public static final String CLICK_TEACHER_COURSE_DETAIL_STAGE_SUMMARY_HIST = "c_stage_summary_hist";
    public static final String CLICK_TEACHER_COURSE_DETAIL_STUDENT_INFO = "c_student_info";
    public static final String CLICK_TEACHER_COURSE_DETAIL_ADDRESS = "c_address";
    public static final String CLICK_TEACHER_COURSE_DETAIL_CLASS_ADJUSTMENT = "c_class_adjustment";
    public static final String CLICK_TEACHER_COURSE_DETAIL_HELP = "c_help";
    public static final String CLICK_C_AGE_LIMIT_CHANGE = "c_age_limit_change";
    public static final String CLICK_C_GRADECHOICE = "gradechoice";


    
    // 老师我的联系人
    // tr_contact === contact_reorder：用户点击了续课按钮
    public static final String CLICK_TEACHER_CONTACT_REP_COURSE = "reorder";
    
    // 老师家长详情
    // tr_finfo === finfo_reorder：用户点击了续课按钮
    public static final String CLICK_TEACHER_STUDENT_DETAIL_REP_COURSE = "reorder";
    
    // 老师订单列表
    // tr_allorder === reorder：用户点击了续课按钮
    public static final String CLICK_TEACHER_ALL_ORDER_REP_COURSE = "reorder";
    
    // 老师订单详情
    // tr_orderinfo === orderinfo_reorder：用户点击了续课按钮
    public static final String CLICK_TEACHER_ORDER_REP_COURSE = "reorder";
    // tr_orderinfo === c_qq_service_explain：用户点击了轻轻家教平台服务费说明
    public static final String CLICK_TEACHER_ORDER_QQ_SERVICE_EXPLAIN = "c_qq_service_explain";
    // tr_orderinfo === c_order_ta：用户点击了下单助教
    public static final String CLICK_TEACHER_ORDER_ORDER_TA = "c_order_ta";
    
    // 老师自我分享
    // tr_pcenter === share_friends：用户点击了自我分享
    public static final String CLICK_TEACHER_SHARE = "share_friends";
    // 分享app
    // tr_pcenter === share_app：用户点击了分享app
    public static final String CLICK_TEACHER_SHARE_APP = "share_app";
    // tr_pcenter === c_syb_tab：用户点击了生源宝
    public static final String CLICK_TEACHER_SYB_TAB = "c_syb_tab";
    // tr_pcenter === c_service_standards：用户点击了服务准则
    public static final String CLICK_TEACHER_SERVICE_STANDARDS = "c_service_standards";
    // tr_pcenter === c_personal_homepage：用户点击了分享自己的主页
    public static final String CLICK_TEACHER_PERSONAL_HOMEPAGE = "c_personal_homepage";
    // tr_pcenter === c_privilege_center：用户点击了特权中心
    public static final String CLICK_TEACHER_PRIVILEGE_CENTER = "c_privilege_center";

    // 老师钱包
    // tr_income === tr_withdraw_deposit：用户点击了提现
    public static final String CLICK_TEACHER_WALLET_WITHDRAW = "tr_withdraw_deposit";
    // tr_income === tr_wallet_detail：用户点击了钱包明细
    public static final String CLICK_TEACHER_WALLET_LIST = "tr_wallet_detail";
    // tr_income === tr_bank_card：我的银行卡
    public static final String CLICK_TEACHER_WALLET_BANK_CARD = "tr_bank_card";
    // tr_income === conceal_money：隐藏金额
    public static final String CLICK_TEACHER_WALLET_CONCEAL_MONEY = "conceal_money";
    // tr_income === c_question_mark：用户点击了问号
    public static final String CLICK_TEACHER_WALLET_QUESTION_MARK = "c_question_mark";
    // tr_income === c_bill_detail：用户点击了账单详情
    public static final String CLICK_TEACHER_WALLET_BILL_DETAIL = "c_bill_detail";
    
    // 生元宝
    // tr_shengyuanbao === c_all：全部标签
    public static final String CLICK_TEACHER_STUDENT_RESOURCE_ALL = "c_all";
    // tr_shengyuanbao === c_subjects：科目标签
    public static final String CLICK_TEACHER_STUDENT_RESOURCE_SUBJECTS = "c_subjects";
    // tr_shengyuanbao === c_enrolled：已报名标签
    public static final String CLICK_TEACHER_STUDENT_RESOURCE_ENROLLED = "c_enrolled";
    // tr_shengyuanbao === c_screen：用户点击了筛选项
    public static final String CLICK_TEACHER_STUDENT_RESOURCE_SCREEN = "c_screen";
    // tr_shengyuanbao === c_question_mark：用户点击了问号
    public static final String CLICK_TEACHER_STUDENT_RESOURCE_QUESTION_MARK = "c_question_mark";
    // tr_shengyuanbao === c_set：用户点击了设置
    public static final String CLICK_TEACHER_STUDENT_RESOURCE_SET = "c_set";
    // tr_shengyuanbao === c_enroll：用户点击了报名按钮
    public static final String CLICK_TEACHER_STUDENT_RESOURCE_ENROLL = "c_enroll";
    // tr_shengyuanbao === c_disinterest：用户点击了不感兴趣按钮
    public static final String CLICK_TEACHER_STUDENT_RESOURCE_DISINTEREST = "c_disinterest";
    // tr_shengyuanbao === c_sort：用户点击了排序
    public static final String CLICK_TEACHER_STUDENT_RESOURCE_SORT = "c_sort";
    // tr_shengyuanbao === c_middle_set：用户点击了设置（页面中间）
    public static final String CLICK_TEACHER_STUDENT_RESOURCE_MIDDLE_SET = "c_middle_set";
    // tr_shengyuanbao === c_middle_question_mark：用户点击了问号（页面中间）
    public static final String CLICK_TEACHER_STUDENT_RESOURCE_MIDDLE_QUESTION_MARK = "c_middle_question_mark";
    // tr_shengyuanbao === c_card：卡片
    public static final String CLICK_TEACHER_STUDENT_RESOURCE_CARD = "c_card";
    // tr_shengyuanbao === c_detail：查看详情
    public static final String CLICK_TEACHER_STUDENT_RESOURCE_DETAIL = "c_detail";
    
    // 生元宝详情
    // tr_sybdet === c_enroll：报名按钮
    public static final String CLICK_TEACHER_STUDENT_RESOURCE_DETAIL_ENROLL = "c_enroll";
    // tr_sybdet === c_disinterest：不感兴趣按钮
    public static final String CLICK_TEACHER_STUDENT_RESOURCE_DETAIL_DISINTEREST = "c_disinterest";
    // tr_sybdet === c_face_area：用户点击了头像区域
    public static final String CLICK_TEACHER_STUDENT_RESOURCE_DETAIL_FACE_AREA = "c_face_area";
    // tr_sybdet === c_orderdet：查看订单按钮
    public static final String CLICK_TEACHER_STUDENT_RESOURCE_DETAIL_ORDERDET = "c_orderdet";
    // tr_sybdet === c_selected_tr：被选中的老师
    public static final String CLICK_TEACHER_STUDENT_RESOURCE_DETAIL_SELECTED_TR = "c_selected_tr";
    // tr_sybdet === c_enrolled_tr：已报名老师
    public static final String CLICK_TEACHER_STUDENT_RESOURCE_DETAIL_ENROLLED_TR = "c_enrolled_tr";
    
    // 添加银行卡
    // tr_addcard === c_addcard：确认添加
    public static final String CLICK_TEACHER_ADD_BANKCARD = "c_addcard";
    
    // 提现
    // tr_withdraw === usehelp：帮助
    public static final String CLICK_TEACHER_WITHDRAW_HELP = "usehelp";
    
    // 聊天详情
    public static final String CLICK_TEACHER_IM_VOICE = "c_send_voice";
    public static final String CLICK_TEACHER_IM_PHONE = "phone";
    public static final String CLICK_CHAT_EXPAND = "c_chat_expand";
    
    // 消息主页
    // tr_allmessage === c_tips：用户点击了轻轻小贴士
    public static final String CLICK_MESSAGE_TIPS = "c_tips";
    // tr_allmessage === c_delete：用户点击了删除轻轻小贴士按钮
    public static final String CLICK_MESSAGE_DELETE = "c_delete";
    // tr_allmessage === c_syb_tab：用户点击了 生源宝
    public static final String CLICK_MESSAGE_SYB_TAB = "c_syb_tab";
    
    // 首页
    public static final String CLICK_TEACHER_HOME_BANNER = "c_banner";
    public static final String CLICK_TEACHER_HOME_AVATAR = "c_my_avatar";
    public static final String CLICK_TEACHER_HOME_CERTITICATION = "c_certification";
    public static final String CLICK_TEACHER_HOME_CONTACT = "c_contact";
    public static final String CLICK_TEACHER_HOME_NEW_ORDER = "c_neworder";
    public static final String CLICK_TEACHER_HOME_LECTURE = "c_invited_speakers";
    public static final String CLICK_TEACHER_HOME_OPEN_COURSE = "c_curriculum";
    public static final String CLICK_TEACHER_HOME_INCOME = "c_income";
    public static final String CLICK_TEACHER_HOME_OPINION = "c_opinion";
    public static final String CLICK_TEACHER_HOME_DATA_INTEGRITY_TEXT_LINK = "c_data_integrity_text_link";
    public static final String CLICK_TEACHER_HOME_BASIC_INFO_INTEGRITY = "c_basic_info_integrity";
    public static final String CLICK_TEACHER_HOME_CERTIFICATION_INFO_INTEGRITY = "c_certification_info_integrity";
    public static final String CLICK_TEACHER_HOME_COURSE_INFO_INTEGRITY = "c_course_info_integrity";
    public static final String CLICK_TEACHER_HOME_ALLORDER = "c_allorder";
    public static final String CLICK_TEACHER_HOME_INVITE_REWARDS = "c_invite_rewards";

    public static final String CLICK_TEACHER_HOME_QQ_COLLEGE = "c_qq_college";
    public static final String CLICK_TEACHER_HOME_TEACHER_EXPERIENCE = "c_teaching_experience";
    public static final String CLICK_TEACHER_HOME_MY_TA = "c_my_ta";
    public static final String CLICK_TEACHER_HOME_RANK = "c_rank";
    public static final String CLICK_TEACHER_HOME_REMIND_REVIEW = "c_remind_review";
    public static final String CLICK_TEACHER_HOME_AVG_COURSE_TIME = "c_avg_course_time";
    public static final String CLICK_TEACHER_HOME_POOL_ACCEPT_RATE = "c_pool_accept_rate";
    public static final String CLICK_TEACHER_HOME_INFO_INTEGRITY = "c_info_integrity";
    public static final String CLICK_TEACHER_HOME_CODE_EXPLAIN = "c_code_explain";
    public static final String CLICK_TEACHER_HOME_SYB_TAB = "c_syb_tab";
    public static final String CLICK_TEACHER_HOME_ASSIGN = "c_assign";
    public static final String CLICK_TEACHER_HOME_MORE = "c_more";
    public static final String CLICK_TEACHER_HOME_EXCEPTION_MESSAGE = "c_exception_message";
    public static final String CLICK_TEACHER_HOME_CLASS_REMIND_BOUNCED = "c_class_remind_bounced";
    public static final String CLICK_TEACHER_HOME_TEACHING_TASK_BOUNCED = "c_teaching_task_bounced";
    public static final String CLICK_TEACHER_HOME_OPERATION_BOUNCED = "c_operation_bounced";
    
    // 5.2
    // 开课流程
    public static final String CLICK_START_PROCESS_APPLY_INTERVIEW = "c_apply_interview";
    public static final String CLICK_START_PROCESS_CONSULT = "c_consult";
    public static final String CLICK_START_PROCESS_AUDIO_LINK = "c_audio_link";
    public static final String CLICK_START_PROCESS_CERTIFICATION = "c_certification";
    public static final String CLICK_START_PROCESS_TEACHING_EXPERIENCE = "c_teaching_experience";
    public static final String CLICK_START_PROCESS_BASIC_INFO_INTEGRITY = "c_basic_info_integrity";
    public static final String CLICK_START_PROCESS_SUBJECT_PRICE = "c_subject_price";
    public static final String CLICK_START_PROCESS_TEACHING_ADDRESS = "c_teaching_address";
    public static final String CLICK_START_PROCESS_TEACHING_TIME = "c_teaching_time";
    public static final String CLICK_START_PROCESS_PLATFORM_RULE = "c_platform_rule";
    public static final String CLICK_START_PROCESS_TRAINING_VIDEO = "c_training_video";
    public static final String CLICK_START_PROCESS_SERVICE_STANDARDS = "c_service_standards";
    
    // 我要开课
    public static final String CLICK_START_CLASS_FRIEND = "c_friend";
    public static final String CLICK_START_CLASS_FREE_DEMO = "c_free_demo";
    public static final String CLICK_START_CLASS_BREAK = "c_break";
    public static final String CLICK_START_CLASS_CERTIFICATION = "c_certification";
    public static final String CLICK_START_CLASS_TEACHING_EXPERIENCE = "c_teaching_experience";
    public static final String CLICK_START_CLASS_BASIC_INFO_INTEGRITY = "c_basic_info_integrity";
    public static final String CLICK_START_CLASS_SUBJECT_PRICE = "c_subject_price";
    public static final String CLICK_START_CLASS_TEACHING_ADDRESS = "c_teaching_address";
    public static final String CLICK_START_CLASS_TEACHING_TIME = "c_teaching_time";
    public static final String CLICK_START_CLASS_PLATFORM_RULE = "c_platform_rule";
    public static final String CLICK_START_CLASS_TRAINING_VIDEO = "c_training_video";
    public static final String CLICK_START_CLASS_FAVORABLE_COURSE_PACKAGE = "c_favorable_course_package";
    public static final String CLICK_START_CLASS_COURSE_CONTENT_PKG = "c_course_content_pkg";
    public static final String CLICK_START_CLASS_ONLINE = "c_online";
    
    // 我的助教
    public static final String CLICK_MY_TA_IM = "im";
    public static final String CLICK_MY_TA_PHONE = "phone";
    public static final String CLICK_MY_TA_EXPAND = "c_expand";
    public static final String CLICK_MY_TA_CHANGE_MEMBER = "c_change_member";
    //老师建议
    public static final String CLICK_MY_TA_TEACHER_SUGGESTION = "c_teacher_suggestion";
    //校长信箱
    public static final String CLICK_MY_TA_C_OPINION = "c_opinion";
    //查看所有
    public static final String CLICK_MY_TA_C_SHOW_AL = "c_show_all";
    //问题分类
    public static final String CLICK_MY_TA_C_QUESTION_CATEGORY = "c_question_category";
    //问题
    public static final String CLICK_MY_TA_C_QUESTION = "c_question";
    //评价
    public static final String CLICK_MY_TA_C_APPRAISE = "c_appraise";




    
    // 注册成功页面
    public static final String CLICK_REGISTER_SUCCESS_IMMEDIATE_APPLY = "c_immediate_apply";
    public static final String CLICK_REGISTER_SUCCESS_JUST_LOOKING = "c_just_looking";
    
    // 解绑助教关系
    public static final String CLICK_UNBIND_TA_UNBIND = "c_unbind";
    
    // 5.2.5
    // 生源宝授课时间页面
    public static final String CLICK_TR_SYB_TEACHING_TIME_PAST = "c_past";
    public static final String CLICK_TR_SYB_TEACHING_TIME_SAFE = "c_safe";
    
    // 预设授课时间页面
    public static final String CLICK_TR_TEACHING_TIME_ALL = "c_all";
    public static final String CLICK_TR_TEACHING_TIME_SAVE = "c_save";
    
    // 5.3.0
    // 教学任务
    public static final String CLICK_TR_TEACHING_TASK_FACE_AREA = "c_face_area";
    
    // 5.3.5
    // 账单明细
    public static final String CLICK_TR_BILL_DETAIL_FACE_AREA = "c_face_area";
    
    // 设置课酬
    public static final String CLICK_TR_TEACHING_COURSE_QQ_SERVICE_EXPLAIN = "c_qq_service_explain";
    
    // 5.4.0
    // 优惠课程包
    public static final String CLICK_FAVORABLE_COURSE_PACKAGE_FAVORABLE_COURSE_PACKAGE_COMMENT = "c_favorable_course_package_comment";
    public static final String CLICK_FAVORABLE_COURSE_PACKAGE_ONLINE_OFFLINE = "c_online_offline";
    public static final String CLICK_FAVORABLE_COURSE_PACKAGE_ONLINE_OFFLINE_COMBINED = "c_online_offline_combined";
    
    // 5.5.0
    // 服务准则
    public static final String CLICK_SERVICE_STANDARDS_AFFIRM = "c_affirm";
    public static final String CLICK_SERVICE_STANDARDS_RETURN = "c_return";
    // 轻轻家教服务标准
    public static final String CLICK_QQ_PLATFORM_SERVICE_STANDARDS_SERVICE_STANDARDS = "c_service_standards";
    public static final String CLICK_QQ_PLATFORM_SERVICE_STANDARDS_TEACHING_TASK = "c_teaching_task";
    // 内容课程包申请
    public static final String CLICK_COURSE_CONTENT_PKG_APPLY_COMMIT = "c_commit";
    public static final String CLICK_COURSE_CONTENT_PKG_APPLY_PREVIEW = "c_preview";
    // 内容课程包列表
    public static final String CLICK_COURSE_CONTENT_PKG_LIST_QUESTION_MARK = "c_question_mark";
    public static final String CLICK_COURSE_CONTENT_PKG_LIST_ADD = "c_add";
    // 内容课程包
    public static final String CLICK_COURSE_CONTENT_PKG_SET_PRICE = "c_set_price";
    public static final String CLICK_COURSE_CONTENT_PKG_EDIT = "c_edit";
    
    // 5.5.5
    // 内容课程包申请列表
    public static final String CLICK_COURSE_CONTENT_PKG_APPLY_LIST_IMMEDIATE_APPLY = "c_immediate_apply";
    
    // 5.6.0
    // 在线旁听重播页
    public static final String CLICK_ONLINE_AUDIT_REPLAY_TEACHER_REPLAY = "c_replay";
    public static final String CLICK_ONLINE_AUDIT_REPLAY_TEACHER_PAUSE = "c_pause";
    public static final String CLICK_ONLINE_AUDIT_REPLAY_TEACHER_PAGE_TURNING = "c_page_turning";
    
    // 5.6.5
    // 在线授课
    public static final String CLICK_ONLINE_TEACHING_AUDIO = "c_audio";
    public static final String CLICK_ONLINE_TEACHING_SET = "c_set";
    public static final String CLICK_ONLINE_TEACHING_COPY = "c_copy";
    // 在线授课申请准则
    public static final String CLICK_ONLINE_TEACHING_APPLY_STANDARDS_AFFIRM = "c_affirm";
    // 在线授课申请
    public static final String CLICK_ONLINE_TEACHING_APPLY_COMMIT = "c_commit";
    
    // 5.7.0
    // 生源宝报名页面
    public static final String CLICK_TR_SYB_ENROLL_ENROLL = "c_enroll";
    // 课程报告页
    public static final String CLICK_COURSE_REPORT_DETAIL_COURSE_UNFOLD = "c_course_unfold";
    public static final String CLICK_COURSE_REPORT_DETAIL_CASE_UNFOLD = "c_case_unfold";
    public static final String CLICK_COURSE_REPORT_DETAIL_PROGRESS_BAR = "c_progress_bar";
    public static final String CLICK_COURSE_REPORT_DETAIL_TAKE_PIC = "c_take_pic";
    public static final String CLICK_COURSE_REPORT_DETAIL_OFFLINE_ASSIGN = "c_offline_assign";
    public static final String CLICK_COURSE_REPORT_DETAIL_NO_ASSIGN = "c_no_assign";
    // 课程报告确认页
    public static final String CLICK_COURSE_REPORT_CONFIRM_EDIT = "c_edit";
    public static final String CLICK_COURSE_REPORT_CONFIRM_RETURN = "c_return";
    public static final String CLICK_COURSE_REPORT_CONFIRM_IMPROVE = "c_improve";
    public static final String CLICK_COURSE_REPORT_CONFIRM_VOICE = "c_voice";
    public static final String CLICK_COURSE_REPORT_CONFIRM_PICTURE = "c_picture";
    
    // 5.7.5
    // 续课设置页面
    public static final String CLICK_REORDER_SETTING_SAVE = "c_save";

    // 5.8.0
    // 群设置
    public static final String CLICK_GROUP_CHAT_SETTING_ADD = "c_add";
    public static final String CLICK_GROUP_CHAT_SETTING_DELETE = "c_delete";
    public static final String CLICK_GROUP_CHAT_SETTING_SHOW_ALL = "c_show_all";
    public static final String CLICK_GROUP_CHAT_FACE_AREA = "c_face_area";
    public static final String CLICK_GROUP_CHAT_SET = "c_set";

    public static final String CLICK_TA_GROUP_CHAT_SETTING_ADD = "c_add";
    public static final String CLICK_TA_GROUP_CHAT_SETTING_DELETE = "c_delete";
    public static final String CLICK_TA_GROUP_CHAT_SETTING_SHOW_ALL = "c_show_all";
    public static final String CLICK_TA_GROUP_CHAT_FACE_AREA = "c_face_area";
    public static final String CLICK_TA_GROUP_CHAT_SET = "c_set";

    //5.9.0
    public static final String CLICK_COMPLAIN = "c_complain";
    public static final String CLICK_CLEAR_SEARCH_HISTORY = "c_clear_search_history";
    public static final String CLICK_QUESTION_CATEGORY = "c_question_category";
    public static final String CLICK_QUESTION = "c_question";
    public static final String CLICK_APPRAISE = "c_appraise";
    public static final String CLICK_COMMIT = "c_commit";
    public static final String CLICK_EVALUATION_HISTORY = "c_evaluation_history";
    public static final String CLICK_HIST_SEARCH = "c_hist_search";
    public static final String CLICK_SEARCH = "c_search";
    public static final String CLICK_ADD_WECHAT = "c_add_wechat";

    public static final String CLICK_C_PASSWORD = "c_password";

    public static final String CLICK_C_FORGETPASSWORD = "c_forgetpassword";

    public static final String CLICK_C_REGISTER = "c_register";


    public static final String CLICK_C_NEXT = "c_next";







    /***************************************
     * TA
     ***************************************/
    // 5.4.5
    // 首页
    public static final String CLICK_TA_HOME_MY_TEACHER = "c_my_teacher";
    public static final String CLICK_TA_HOME_MY_STUDENT = "c_my_student";
    public static final String CLICK_TA_HOME_WEB_RECOMMEND = "c_web_recommend";
    public static final String CLICK_TA_HOME_OPINION = "c_opinion";
    public static final String CLICK_TA_HOME_FIND_TEACHER = "c_find_teacher";
    
    // 家长详情（新）
    public static final String CLICK_TA_STUDENT_DETAIL_CREATE_SYB = "c_create_syb";
    public static final String CLICK_TA_STUDENT_DETAIL_CREATE_ORDER = "c_create_order";
    public static final String CLICK_TA_STUDENT_DETAIL_IM = "im";
    public static final String CLICK_TA_STUDENT_DETAIL_RECOMMEND_TEACHER = "c_recommend_teacher";
    public static final String CLICK_TA_STUDENT_DETAIL_VIEW_ORDER = "c_view_order";
    public static final String CLICK_TA_STUDENT_DETAIL_RECOMMENDED_TEACHER = "c_recommended_teacher";
    public static final String CLICK_TA_STUDENT_DETAIL_STUDENT_QUESTIONNAIRE = "c_student_questionnaire";
    public static final String CLICK_TA_STUDENT_DETAIL_COMMUNICATION_RECORD = "c_communication_record";
    public static final String CLICK_TA_STUDENT_DETAIL_PHONE = "phone";
    public static final String CLICK_TA_STUDENT_DETAIL_MODIFY_NICK = "c_modify_nick";
    
    // 老师详情（新）
    public static final String CLICK_TA_TEACHER_DETAIL_KABC = "c_kabc";
    public static final String CLICK_TA_TEACHER_DETAIL_TRPAGE = "trpage";
    public static final String CLICK_TA_TEACHER_DETAIL_TRPHONE = "trphone";
    public static final String CLICK_TA_TEACHER_DETAIL_TAPHONE = "taphone";
    
    // 老师详情
    public static final String CLICK_TA_BIND_TEACHER_DETAIL_KABC = "c_kabc";
    public static final String CLICK_TA_BIND_TEACHER_DETAIL_TRPAGE = "trpage";
    public static final String CLICK_TA_BIND_TEACHER_DETAIL_PHONE = "phone";
    public static final String CLICK_TA_BIND_TEACHER_DETAIL_SET_NOTE = "c_set_note";
    public static final String CLICK_TA_BIND_TEACHER_DETAIL_VIEW_STUDENT = "c_view_student";
    public static final String CLICK_TA_BIND_TEACHER_DETAIL_VIEW_ORDER = "c_view_order";
    
    /************************************************************************************/
    // 日志通用字段
    public static final String LOG_KEY_TIMESTAMP = "ts";
    public static final String LOG_KEY_LOG_TYPE = "logtype";
    public static final String LOG_KEY_EVENT_CODE = "eventcode";
    public static final String LOG_KEY_USER_ID = "uid";
    public static final String LOG_KEY_NETWORK_TYPE = "network_type";
    public static final String LOG_KEY_ACTION_CODE = "actioncode";
    public static final String LOG_KEY_IMEI = "imei";
    public static final String LOG_KEY_CLIENT_ID = "clientid";
    public static final String LOG_KEY_APP_NAME = "appname";
    public static final String LOG_KEY_APP_PLATFORM = "appplatform";
    public static final String LOG_KEY_APP_VERSION = "appversion";
    public static final String LOG_KEY_OS_VERSION = "osversion";
    public static final String LOG_KEY_CHANNEL_ID = "channelid";
    public static final String LOG_KEY_DEVICE_COMPANY = "devicecompany";
    public static final String LOG_KEY_DEVICE_MODEL = "devicemodel";
    public static final String LOG_KEY_HOST = "host";
    public static final String LOG_KEY_HOST_IP = "hostip";
    public static final String LOG_KEY_STATUS = "status";
    public static final String LOG_KEY_DNS_STATUS = "dnsstatus";
    public static final String LOG_KEY_COST = "cost";
    public static final String LOG_KEY_START = "start";
    public static final String LOG_KEY_STEP = "step";
    public static final String LOG_KEY_END = "end";
    public static final String LOG_KEY_RESOLVED_IP = "resolved_ip";
    public static final String LOG_KEY_ERROR_CODE = "error_code";

    /************************************************************************************/
    // 日志特殊字段 事件级
    public static final String LOG_EXTRA_UA = "ua";
    public static final String LOG_EXTRA_RESOLUTION = "resolution";
    /**1：系统播放器，2：第三方播放器*/
    public static final String LOG_EXTRA_AV_PLAYER_TYPE = "player_type";
    /**1：直播，2：点播，3：录制*/
    public static final String LOG_EXTRA_AV_PLAY_TYPE = "play_type";
    /**1：音频 ，2：音视频*/
    public static final String LOG_EXTRA_AV_CONTENT_TYPE = "content_type";
    public static final String LOG_EXTRA_AV_PLAY_TIME = "play_time";
    public static final String LOG_EXTRA_AV_STATUS_CODE = "status_code";
    public static final String LOG_EXTRA_AV_START_TIME = "play_start_time";
    public static final String LOG_EXTRA_AV_STUCK_COUNT = "play_stuck_num";
    public static final String LOG_EXTRA_AV_STUCK_TIME = "play_stuck_time";
    public static final String LOG_EXTRA_AV_PLAY_URL = "play_url";
    public static final String LOG_STUDENT_LONGITUDE = "stu_longitude";
    public static final String LOG_STUDENT_LATITUDE = "stu_latitude";
    public static final String LOG_TEACHER_LONGITUDE = "tr_longitude";
    public static final String LOG_TEACHER_LATITUDE = "tr_latitude";
    public static final String LOG_EXT_INFO = "ext_info";
    public static final String LOG_LOG_DATE = "log_date";
    public static final String LOG_DURATION = "dur";
    // 日志特殊字段 页面级
    public static final String LOG_EXTRA_SEARCH_INFO = "search_info";
    public static final String LOG_EXTRA_SEARCH_CHOICE_MODE = "search_choice_mode";
    public static final String LOG_EXTRA_TR_ID = "tr_id";
    public static final String LOG_EXTRA_ORDER_ID = "order_id";
    public static final String LOG_EXTRA_GRADE_ID = "grade_id";
    public static final String LOG_EXTRA_CITY_ID = "city_id";
    public static final String LOG_EXTRA_ACTIVE_STATE = "active_state";
    public static final String LOG_EXTRA_LECTURE_ID = "lecture_id";
    public static final String LOG_EXTRA_SEARCH_TERMS = "search_terms";
    public static final String LOG_EXTRA_STATUS = "status";
    public static final String LOG_EXTRA_ABTEST = "abTest";
    public static final String LOG_EXTRA_EXPERT_ID = "expert_id";
    public static final String LOG_EXTRA_OBJECT_ID = "object_id";
    public static final String LOG_EXTRA_EXPERT_TYPE = "expert_type";
    public static final String LOG_EXTRA_ORDER_STATUS = "order_status";
    public static final String LOG_EXTRA_IM_TOTAL_CNT = "im_total_cnt";
    public static final String LOG_EXTRA_IM_SHOW_CNT = "im_show_cnt";
    public static final String LOG_EXTRA_IM_COVER_CNT = "im_cover_cnt";
    public static final String LOG_EXTRA_IM_COVER_SUCESS_CNT = "im_cover_sucess_cnt";
    public static final String LOG_EXTRA_IM_COVER_REQUEST_CNT = "im_cover_request_cnt";
    public static final String LOG_EXTRA_IM_LOSE_CNT = "im_lose_cnt";
    public static final String LOG_EXTRA_MQTT_CNT = "mqtt_cnt";
    public static final String LOG_EXTRA_PAGE_TYPE = "page_type";
    public static final String LOG_EXTRA_TA_PAGE_TYPE = "page_type";
    public static final String LOG_EXTRA_USER_TYPE = "user_type";
    public static final String LOG_EXTRA_TA_USER_TYPE = "user_type";
    public static final String LOG_EXTRA_STATE = "state";
    public static final String LOG_EXTRA_AB_TEST = "abTest";
    public static final String LOG_EXTRA_TYPE = "type";
    public static final String LOG_EXTRA_SOURCE_TYPE = "source_type";
    public static final String LOG_EXTRA_SUBJECT_ID = "subject_id";
    public static final String LOG_EXTRA_STUDENT_ID = "student_id";
    public static final String LOG_EXTRA_AUDIO_LENGTH = "e_audio_length";
    public static final String LOG_EXTRA_AUDIO_ID = "e_audio_id";

    // 日志特殊字段 点击级需要额外加 e_ 的
    public static final String LOG_EXTRA_E_TYPE = "e_type";
    public static final String LOG_EXTRA_E_SHARE_MODE = "e_share_mode";
    public static final String LOG_EXTRA_E_OBJECT_ID = "e_object_id";

    public static final String LOG_EXTRA_E_STATUS = "e_status";
    public static final String LOG_EXTRA_E_TR_ID = "e_tr_id";
    public static final String LOG_EXTRA_E_CLICK_TYPE = "e_click_type";
    public static final String LOG_EXTRA_E_SEARCH_TERMS = "e_search_terms";
    public static final String LOG_EXTRA_E_COURSE_NUM = "e_course_num";
    public static final String LOG_EXTRA_E_THIRD_PAYMENT_MODE = "e_third_payment_mode";
    public static final String LOG_EXTRA_E_PAYMENT_TYPE = "e_payment_type";
    public static final String LOG_EXTRA_E_GRADE_ID = "e_grade_id";
    public static final String LOG_EXTRA_E_SEARCH_RESULT = "e_search_result";
    public static final String LOG_EXTRA_E_COLLECT = "e_collect";
    public static final String LOG_EXTRA_E_ENTER_HOME = "e_enter_home";
    public static final String LOG_EXTRA_E_ENROLL_STATUS = "e_enroll_status";
    public static final String LOG_EXTRA_E_ENROLL_SCHEDULE = "e_enroll_schedule";
    public static final String LOG_EXTRA_E_ENROLL_TEACHING_METHOD = "e_enroll_teaching_method";
    public static final String LOG_EXTRA_E_ENROLL_TEACHING_TIME = "e_enroll_teaching_time";
    public static final String LOG_EXTRA_E_ENROLL_MATCHING_SCHEDULE = "e_enroll_matching_schedule";
    public static final String LOG_EXTRA_E_ADD_STATE = "e_add_state";
    public static final String LOG_EXTRA_E_CHAT_EXPAND_TYPE = "e_chat_expand_type";
    public static final String LOG_EXTRA_E_HYPERLINK = "e_hyperlink";
    public static final String LOG_EXTRA_CLICK_E_UNBIND = "e_unbind";
    public static final String LOG_EXTRA_E_VIP_LEVEL = "e_vip_level";
    public static final String LOG_EXTRA_E_ADD_WECHAT = "e_add_wechat";

    /************************************************************************************/
    // 分享
    public static final String SHARE_WECHAT = "wechat";
    public static final String SHARE_WECHAT_CIRCLE = "wechat_circle";
    public static final String SHARE_QQ = "qq";
    public static final String SHARE_QQ_SPACE = "qq_space";
    public static final String SHARE_WEIBO = "weibo";
    public static final String SHARE_MESSAGE = "message";
    public static final String SHARE_EMAIL = "email";
    public static final String SHARE_COPY_URL = "copyurl";
    
}

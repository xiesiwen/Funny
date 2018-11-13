package com.qingqing.base.data;

import static com.qingqing.api.proto.v1.CourseCommonProto.GradeGroupType.unknown_school_grade_group_type;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.qingqing.api.proto.ta.v1.WinterVacationForTaProto;
import com.qingqing.api.proto.v1.CityDistrictProto;
import com.qingqing.api.proto.v1.CityDistrictProto.CityDistrict;
import com.qingqing.api.proto.v1.CityProto;
import com.qingqing.api.proto.v1.GradeCourseProto;
import com.qingqing.api.proto.v1.app.AppCommon;
import com.qingqing.api.proto.v1.appconfig.AppConfig;
import com.qingqing.base.BaseApplication;
import com.qingqing.base.bean.Address;
import com.qingqing.base.bean.City;
import com.qingqing.base.config.CommonUrl;
import com.qingqing.base.dns.DNSManager;
import com.qingqing.base.dns.HostManager;
import com.qingqing.base.http.HttpMethod;
import com.qingqing.base.http.error.HttpError;
import com.qingqing.base.http.req.ProtoListener;
import com.qingqing.base.http.req.ProtoReq;
import com.qingqing.base.log.Logger;
import com.qingqing.base.utils.DateUtils;
import com.qingqing.base.utils.NetworkUtil;
import com.qingqing.base.utils.PBUtil;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.SparseArray;

/**
 * Created by Wangxiaxin on 2015/12/21.
 *
 * 数据缓存以及对数据库的常用访问封装
 */
public class DefaultDataCache {
    
    private static DefaultDataCache sInstance;
    private static final String TAG = "DefaultDataCache";
    
    private static final String SP_KEY_RSP_APP_COMMON_CONFIG = "key_rsp_app_common_config";
    private static final String SP_KEY_IS_TIME_EXPAND = "key_is_time_expand";
    public  static final String SP_KEY_TEST_ENV_NAME = "key_test_env";//仅限测试环境有效
    private static final String SP_KEY_APP_TYPE = "key_app_type";
    private static final String SP_KEY_DEFAULT_RENEW_COURSE_COUNT = "key_default_renew_course_count";
    private static final String SP_KEY_IS_H5_CACHE_OPTIMIZE = "key_is_h5_cache_optimize";
    private static final String SP_KEY_IS_H5_CACHE_OPTIMIZE_TA = "key_is_h5_cache_optimize_ta";
    private static final String SP_KEY_IS_UPLOAD_BEHAVIOR_LOG = "key_is_upload_behavior_log";
    private static final String SP_KEY_IS_UPLOAD_NETWORK_LOG = "key_is_upload_network_log";
    
    private static final String SP_KEY_HTTP_REQUEST_TIMEOUT = "key_http_request_timeout";
    private static final String SP_KEY_HTTP_REQUEST_RETRY_COUNT = "key_http_request_retry_count";
    
    private static final String SP_KEY_MAX_RENEW_COURSE_COUNT = "key_max_renew_course_count";
    private static final String SP_KEY_MIN_RENEW_COURSE_COUNT = "key_min_renew_course_count";
    
    private static final String SP_KEY_API_DOMAIN_HTTPS = "key_api_do_https";
    
    private static final String SP_KEY_STU_RES_SETTING_URL = "key_stu_res_setting_url";
    
    public static final String KEY_TA_PAGE_SHARE = "ta_page_share";
    public static final String KEY_STUDENT_MINE_INVITE_FRIEND = "student_mine_invite_friend";
    public static final String KEY_TR_HOME_INVITE_STUDENT = "tr_home_invite_student";
    public static final String KEY_TR_WALLET_BOTTOM_BANNER = "tr_wallet_bottom_banner";
    public static final String KEY_STUDENT_POOL_PAGE_BANNER = "student_pool_page_banner";
    public static final String KEY_SUCCESS_PAGE_FLOAT_PIC = "success_page_float_pic";
    public static final String KEY_LEARN_CENTER_BOTTOM_BANNER = "learn_center_bottom_banner";
    public static final String KEY_SUCCESS_PAGE_FLOAT_PIC_V2 = "success_page_float_pic_v2";

    public static final String KEY_WINTER_PACK_CONFIG = "api_winter_vacation_config";// 寒假包配置

    private static final String SP_KEY_SMALL_GROUPON_CITIES = "key_small_group_cities";
    private static final String SP_KEY_BIG_GROUPON_CITIES = "key_big_group_cities";

    public static DefaultDataCache INSTANCE() {
        if (sInstance == null) {
            synchronized (DefaultDataCache.class) {
                if (sInstance == null) {
                    sInstance = new DefaultDataCache();
                }
            }
        }
        return sInstance;
    }
    
    private DefaultDataCache() {
        mDistrictsSArray = new SparseArray<>();
        mDBDao = new DefaultDBDao(BaseApplication.getCtx(), "DefaultDataCache");
    }
    
    public void checkAndRestart() {
        // 目前 AppConfig 存入了 Sp 中，城市列表和区域列表存入了 db 中，暂不需要重新拉取，待后续有需求时再加入
        
        // if (mAllCityList == null || mAllCityList.size() <= 0) {
        // reqCommonData(null);
        // }
    }
    
    public interface DefaultDataReqListener {
        void onBaseDataReqDone(boolean isOk);
    }
    
    public interface DefaultDataCourseGradeReqListener {
        void onCourseGradeReqDone();
    }
    
    private DefaultDataReqListener mDataReqListener;
    private DefaultDBDao mDBDao;

    // --------------------------通用配置信息----------------------------------
    private int mAppType;// 当前应用的类型
    private boolean mIsTimeExpand;// 当前是否支持时间扩展
    private int mDefaultRenewCourseCount;// 默认续课次数
    private boolean mNeedH5CacheOptimize;// 是否启用H5缓存优化
    private boolean mNeedH5CacheOptimizeTa;// 是否启用H5缓存优化Ta
    private boolean mNeedUploadUserBehaviorLog;// 是否允许上传用户行为日志
    private boolean mNeedUploadNetworkLog;// 是否允许上传网络日志
    private boolean mIsShowTeacherRank = false;// 老师主页五维图是否展示优于x%
    private boolean mIsShowSelectedComment = true;// 是否展示课程评价的精选评价标签
    private int mLectureAttendanceSyncInterval = 30; // 聊天室同步人数间隔。单位s 默认30.
    private int mLectureNormalUserSendMsgCountPreMin = 5; // 聊天室普通用户每分钟可发消息数
                                                          // 默认5.
    private boolean mHuanXinAudioUpgrade = false; // 环信音频格式aac或者arm
    
    private int mMaxRenewCourseCount;// 最大续课次数
    private int mMinRenewCourseCount;// 最小续课次数
    
    private String mStuResSettingUrl;// 生源宝设置h5链接
    
    private int mHttpRequestTimeoutSeconds;// 前端http超时时间，单位秒,默认10秒
    private int mHttpRequestRetryCount;// 前端重试次数,默认2次（包含实际需要请求的一次，实际重试次数需要减一）
    
    private String mSearchListAbTest = "a";// 家长端搜索AbTest
    
    private String mCustomPhoneNumber = "4000177040";
    private String mLiveCloudDownloadUrl = "";
    private String mTestLecutreId;
    private String mTeacherProfileExplainInformationId;
    private double mLiveShareBonus = 0;
    private double mLiveRegisterBonus = 0;
    private boolean mIsSupportThirdPartHomework = false;
    private boolean mIsApiHttps = false;// api接口是否使用https
    
    private int mStudentResourceNotEnrollPopChance = -1;
    private int mStudentPollRefuseTimegap = -1;
    private float mAllStudentPoolPenalty = 2;// 生源宝扣费金额
    
    private float mForewarningShowTimeDays = 7;
    private float mAllTeacherLevelDefinition[] = new float[] { 50, 100, 150, 200 };
    private String mTeacherOnlineCourseMediaId = "";// 老师申请在线授课MediaId
    private int mStudentPoolBlackListExpiredDays = 15;// 生源宝小黑屋惩罚时间
    private boolean mIsShowOrderNeedConfirm = false;// 是否显示待确认订单
    private int mTaStudentPoolAuditionDaysLimit = 30;// 生源宝试听有效时间，限制30天内
    
    // 课程报告文字，录音等的计算分值的权重
    private int mCourseReportTextScore = 4; // 每个字4分
    private int mCourseReportAlphabetScore = 2; // 每个字母2分
    private int mCourseReportQuizScore = 80; // 题库80分
    private int mCourseReportAudioScore = 5; // 录音一秒5分
    private int mCourseReportPicScore = 60; // 照片一张60分
    private int mCourseReportLevelBadCeiling = 30; // 等级较差的最高值 30分 0-30就是较差
    private int mCourseReportLevelOrdinaryCeiling = 60; // 等级一般 60分 30-60就是一般
    private int mCourseReportLevelGoodCeiling = 80; // 等级较好80分
                                                    // 60-80就是较好，80-100就是优秀，100就是完美
    private int mCourseReportOfflineScore = 1; // 线下布置的分数
    private int mCourseReportNoNeedScore = 1; // 不需要布置的分数

    // 6.1 调退课罚款配置
    private int penaltyHour = 24;
    private int teacherResponsibilityPenaltyRate = 50; //教师责任惩罚比例
    private int teacherResponsibilityBountyRate = 40; // 教师责任奖励比例
    private int studentResponsibilityPenaltyRate = 20;// 学生责任惩罚比例
    private int studentResponsibilityBountyRate = 10;// 学生责任奖励比例

    private boolean api_order_course_action_switch = false;

    private long api_order_course_action_start_time; //格式 YYYY-MM-dd HH:mm:ss 表示： 该时间之后的课程才会走 退课调课 扣费的逻辑。默认数值 2017-11-15 00:00:00

    // 教学计划、阶段总结使用电脑填写的配置
    // {"is_open":"1","text":"电脑填写","link":""}
    private String teachPlanOnPcOptions;

    // 寒假包课次及折扣配置
    private Map<Integer, WinterVacationForTaProto.WinterVacationPackageRule> winterPackConfigMap;

    private String onlineNewOrderDiscount;// 6.3新增新人新享活动

    // --------------------------通用基础数据----------------------------------
    /** */
    /** 所有城市列表 */
    private ArrayList<City> mAllCityList;
    /** 开通城市列表 */
    private ArrayList<City> mOpenedCityList;
    /** 驻点城市列表 */
    private ArrayList<City> mStationedCityList;
    /** 热门城市列表 */
    private ArrayList<City> mHotCityList;
    private ArrayList<GradeCourseProto.SecondaryCourse> mSubCourseList;
    /** 年级类型列表 */
    private ArrayList<GradeCourseProto.GradeGroup> mGradeGroupTypeList;
    /** 有效年级类型列表 */
    private ArrayList<GradeCourseProto.GradeGroup> mValidGradeGroupTypeList;
    
    /** 年级科目的对应关系 */
    private Map<Integer, Set<Integer>> mGradeCourseSet;
    
    private SparseArray<List<CityDistrict>> mDistrictsSArray;
    
    private ArrayList<String> mTeacherFeatureList;// 过滤找老师标签
    
    private HashSet<Integer> mSupportGrouponCities;// 支持朋友团的城市
    private HashSet<Integer> mSupportSmallGrouponV2Cities;// 支持朋友团2.0 2人团，3人团
                                                          // 的城市
    private HashSet<Integer> mSupportBigGrouponV2Cities;// 支持朋友团2.0 4人团，5人团 的城市
    
    private String mDefaultAppraiseContent;// 默认评价内容
    
    private String mAppWithdrawNewpolicyContent;// 提现提醒内容
    
    private boolean mStudentDisplayOnlineAfterCourses = false;
    
    private GradeCourseProto.StudentCourseLearningStatusConfig[] mLearningStageStatuses;
    
    private ArrayList<String> ivrPhoneNumberList;
    
    @Nullable
    private ArrayList<Integer> mNotShowingPhoneIconCityIdList; // 590需求，产品原话“若老师的助教城市ID属于这配置的，则不显示拨打电话按钮。不配置默认显示”
    
    private int mTeacher_report_share = 1; // 590需求，课程报告分享到教研管理群，
                                           // 产品原话：“值为1默认勾选，为0默认不勾选。不配置默认勾选”
    
    private String mTeacherIndexExpireTimeString;// 检验管理群老师指标过期时间
    
    private boolean mIsOnlineAuditionOpened = false; // 在线免费试听开关
    
    public void reqCommonData(DefaultDataReqListener listener) {
        reqCommonData(null, listener);
    }
    
    public void reqCommonData(Context ctx, DefaultDataReqListener listener) {
        
        if (!NetworkUtil.isNetworkAvailable()) {
            if (listener != null) {
                listener.onBaseDataReqDone(false);
            }
            return;
        }
        
        mDataReqListener = listener;
        ProtoReq req1 = new ProtoReq(CommonUrl.COMMON_DATA_URL.url());
        req1.setLoadingIndication(ctx).setReqMethod(HttpMethod.GET)
                .setRspListener(new ProtoListener(AppConfig.AppConfigResponseV3.class) {
                    @Override
                    public void onDealResult(Object result) {
                        AppConfig.AppConfigResponseV3 rsp = (AppConfig.AppConfigResponseV3) result;
                        boolean ret = parseCommonData(rsp);
                        if (mDataReqListener != null) {
                            mDataReqListener.onBaseDataReqDone(ret);
                            mDataReqListener = null;
                        }
                    }
                    
                    @Override
                    public void onDealError(HttpError error, boolean isParseOK,
                                            int errorCode, Object result) {
                        // super.onDealError(error, isParseOK, errorCode,
                        // result);
                        if (mDataReqListener != null) {
                            mDataReqListener.onBaseDataReqDone(false);
                            mDataReqListener = null;
                        }
                    }
                }).req();
    }
    
    private boolean parseCommonData(AppConfig.AppConfigResponseV3 rsp) {
        
        boolean ret = false;
        Logger.o(TAG, "parseCommonData  begin");
        try {
            if (rsp.response != null && rsp.response.errorCode == 0) {
                parseConfigItems(rsp.configItems);
                PBUtil.saveMsgArray(BaseData.getInstance().getSPWrapper(),
                        SP_KEY_RSP_APP_COMMON_CONFIG, rsp.configItems);
                
                // 解析城市信息
                if (rsp.cities.length > 0) {
                    mDBDao.clearCityList();
                    if (mAllCityList == null) {
                        mAllCityList = new ArrayList<>();
                    }
                    else {
                        mAllCityList.clear();
                    }
                    if (mOpenedCityList == null) {
                        mOpenedCityList = new ArrayList<>();
                    }
                    else {
                        mOpenedCityList.clear();
                    }
                    if (mStationedCityList == null) {
                        mStationedCityList = new ArrayList<>();
                    }
                    else {
                        mStationedCityList.clear();
                    }
                    if (mHotCityList == null) {
                        mHotCityList = new ArrayList<>();
                    }
                    else {
                        mHotCityList.clear();
                    }
                    for (CityProto.City city : rsp.cities) {
                        City localCity = City.valueOf(city.cityId, city.cityName,
                                city.gaodeCityCode, city.isOpen, city.isVisible,
                                city.isStationed, city.isHot, city.isTest);
                        
                        mAllCityList.add(localCity);
                        if (localCity.isVisible && localCity.isStationed) {
                            mOpenedCityList.add(localCity);
                            
                        }
                        if (localCity.isStationed) {
                            mStationedCityList.add(localCity);
                        }
                        if (city.isHot) {
                            mHotCityList.add(localCity);
                        }
                    }
                    mDBDao.addCity(mAllCityList);
                    // 增加 判断 Address 中的 cityid 是否有效，无效择设置
                    Address address = Address.getLocation();
                    if (address != null && address.city != null && address.city.code > 0
                            && address.city.id <= 0) {
                        Address.setLocation(new Address(address.detail, address.latLng,
                                getCityByCode(address.city.code), address.district));
                    }
                }
                
                // 解析城市的区域信息
                if (rsp.districts.length > 0) {
                    mDBDao.clearDistricts();
                    mDistrictsSArray.clear();
                    for (CityDistrict district : rsp.districts) {
                        addDistrictInternal(district);
                        mDBDao.addDistrict(district);
                    }
                }
                
                Logger.o(TAG, "parseCommonData  done");
                ret = true;
            }
        } catch (Exception e) {
            Logger.w(e);
        }
        return ret;
    }
    
    private void parseConfigItems(AppConfig.AppConfigItem[] configItems) {
        if (configItems == null) {
            return;
        }
        for (AppConfig.AppConfigItem item : configItems) {
            if (item == null) {
                continue;
            }
            switch (item.key) {
                case "is_time_table_expand":
                    saveIsTimeExpand("1".equals(item.value[0]));
                    break;
                case "is_h5_cache_optimize":
                    setIsNeedH5CacheOptimize("true".equals(item.value[0]));
                    break;
                case "is_upload_activity_log":
                    setNeedUploadUserBehaviorLog("true".equals(item.value[0]));
                    break;
                case "is_upload_network_log":
                    setNeedUploadNetworkLog("true".equals(item.value[0]));
                    break;
                case "stu_teacher_feature_phrases":
                    setTeacherFeaturePhrases(item.value[0]);
                    break;
                case "stu_search_list_abtest_1":
                    setSearchListAbTest(item.value[0]);
                    break;
                case "all_service_phone_number":
                    mCustomPhoneNumber = item.value[0];
                    break;
                case "all_is_show_teacher_rank":
                    setIsShowTeacherRank(item.value[0]);
                    break;
                case "all_is_show_order_course_appraise_is_selection":
                    setIsShowSelectedComment(item.value[0]);
                    break;
                
                case "stu_default_class_count_in_renew_order":
                    if (BaseData.getClientType() == AppCommon.AppType.qingqing_student) {
                        setDefaultRenewCourseCount(Integer.parseInt(item.value[0]));
                    }
                    break;
                case "stu_max_class_count_in_renew_order":
                    if (BaseData.getClientType() == AppCommon.AppType.qingqing_student) {
                        setMaxRenewCourseCount(Integer.parseInt(item.value[0]));
                    }
                    break;
                case "stu_min_class_count_in_renew_order":
                    if (BaseData.getClientType() == AppCommon.AppType.qingqing_student) {
                        setMinRenewCourseCount(Integer.parseInt(item.value[0]));
                    }
                    break;
                
                case "tea_default_class_count_in_renew_order":
                    if (BaseData.getClientType() == AppCommon.AppType.qingqing_teacher) {
                        setDefaultRenewCourseCount(Integer.parseInt(item.value[0]));
                    }
                    break;
                case "tea_max_class_count_in_renew_order":
                    if (BaseData.getClientType() == AppCommon.AppType.qingqing_teacher) {
                        setMaxRenewCourseCount(Integer.parseInt(item.value[0]));
                    }
                    break;
                case "tea_min_class_count_in_renew_order":
                    if (BaseData.getClientType() == AppCommon.AppType.qingqing_teacher) {
                        setMinRenewCourseCount(Integer.parseInt(item.value[0]));
                    }
                    break;
                
                case "ta_default_class_count_in_renew_order":
                    if (BaseData.getClientType() == AppCommon.AppType.qingqing_ta) {
                        setDefaultRenewCourseCount(Integer.parseInt(item.value[0]));
                    }
                    break;
                case "ta_max_count_in_renew_order":
                    if (BaseData.getClientType() == AppCommon.AppType.qingqing_ta) {
                        setMaxRenewCourseCount(Integer.parseInt(item.value[0]));
                    }
                    break;
                case "ta_min_count_in_renew_order":
                    if (BaseData.getClientType() == AppCommon.AppType.qingqing_ta) {
                        setMinRenewCourseCount(Integer.parseInt(item.value[0]));
                    }
                    break;
                case "all_chatroom_msg_count_per_minute":
                    setLectureNormalUserSendMsgCountPreMin(item.value[0]);
                    break;
                case "all_chatroom_attendance_sync_interval":
                    setLectureAttendanceSyncInterval(item.value[0]);
                    break;
                case "ios_huanxin_audio_upgrade":
                    setHuanXinAudioUpgrade(item.value[0]);
                    break;
                case "all_friend_group_order_cities":
                    if (mSupportGrouponCities == null) {
                        mSupportGrouponCities = new HashSet<>();
                    }
                    else {
                        mSupportGrouponCities.clear();
                    }
                    saveGrouponCities(mSupportGrouponCities, item.value[0]);
                    break;
                case "all_friend_group_order_v2_cities_small":
                    if (mSupportSmallGrouponV2Cities == null) {
                        mSupportSmallGrouponV2Cities = new HashSet<>();
                    }
                    else {
                        mSupportSmallGrouponV2Cities.clear();
                    }
                    saveGrouponCities(mSupportSmallGrouponV2Cities, item.value[0]);
                    SPManager.put(SP_KEY_SMALL_GROUPON_CITIES, item.value[0]);
                    break;
                case "all_friend_group_order_v2_cities_big":
                    if (mSupportBigGrouponV2Cities == null) {
                        mSupportBigGrouponV2Cities = new HashSet<>();
                    }
                    else {
                        mSupportBigGrouponV2Cities.clear();
                    }
                    saveGrouponCities(mSupportBigGrouponV2Cities, item.value[0]);
                    SPManager.put(SP_KEY_BIG_GROUPON_CITIES, item.value[0]);
                    break;
                case "stu_test_qingqing_lecture_id":
                    mTestLecutreId = item.value[0];
                    break;
                case "all_livecloud_download_url":
                    mLiveCloudDownloadUrl = item.value[0];
                    break;
                case "teacher_profile_explain_information_id":
                    mTeacherProfileExplainInformationId = item.value[0];
                    break;
                case "http_request_timeout":
                    setHttpRequestTimeoutSeconds(Integer.parseInt(item.value[0]));
                    break;
                case "http_request_retry_count":
                    setHttpRequestRetryCount(Integer.parseInt(item.value[0]));
                    break;
                case "lecture_share_register_amount":
                    saveLiveRegisterBonus(item.value[0]);
                    break;
                case "teacher_is_support_thirdpart_homework":
                    setIsSupportThirdPartHomework(Boolean.parseBoolean(item.value[0]));
                    break;
                case "https_enable":
                    setApiHttps(item.value[0]);
                    SPManager.put(SP_KEY_API_DOMAIN_HTTPS, item.value[0]);
                    break;
                case "hosts_backup":
                    HostManager.INSTANCE().parseAndSave(item.value[0]);
                    break;
                case "student_pool_setting_url":
                    setStuResSettingUrl(item.value[0]);
                    break;
                case "student_pool_refuse_reason_prob":
                    mStudentResourceNotEnrollPopChance = Integer.parseInt(item.value[0]);
                    break;
                case "student_pool_refuse_timegap":
                    mStudentPollRefuseTimegap = Integer.parseInt(item.value[0]);
                    break;
                case "all_student_pool_penalty":
                    mAllStudentPoolPenalty = Float.parseFloat(item.value[0]);
                    break;
                
                case "all_teacher_level_definition":
                    float end;
                    
                    JSONArray jsonArray = null;
                    try {
                        jsonArray = new JSONArray(item.value[0]);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (jsonArray != null) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject json = (JSONObject) jsonArray.opt(i);
                            String level = json.optString("level");
                            end = (float) json.optDouble("end");
                            if ("k".equalsIgnoreCase(level)) {
                                mAllTeacherLevelDefinition[3] = end;
                            }
                            else if ("a".equalsIgnoreCase(level)) {
                                mAllTeacherLevelDefinition[2] = end;
                            }
                            else if ("b".equalsIgnoreCase(level)) {
                                mAllTeacherLevelDefinition[1] = end;
                            }
                            else if ("c".equalsIgnoreCase(level)) {
                                mAllTeacherLevelDefinition[0] = end;
                            }
                        }
                    }
                    break;
                case "stu_appraise_default_comment":
                    mDefaultAppraiseContent = item.value[0];
                    break;
                case "forewarning_appinfo":
                    mForewarningShowTimeDays = Float.parseFloat(item.value[0]);
                    break;
                case "teacher_live_cloud_tools_description_encode_media_id":
                    mTeacherOnlineCourseMediaId = item.value[0];
                    break;
                case "student_pool_black_list_expired_days":
                    mStudentPoolBlackListExpiredDays = Integer.parseInt(item.value[0]);
                    break;
                case "all_paied_order_need_confirm":
                    setIsShowOrderNeedConfirm(item.value[0]);
                    break;
                case "course_report_quality_setting": // 课程报告评分设置
                    try {
                        JSONObject jsonObject = new JSONObject(item.value[0]);
                        mCourseReportTextScore = jsonObject.optInt("text");
                        mCourseReportAlphabetScore = jsonObject.optInt("letter");
                        mCourseReportQuizScore = jsonObject.optInt("quiz");
                        mCourseReportAudioScore = jsonObject.optInt("audio");
                        mCourseReportPicScore = jsonObject.optInt("pic");
                        mCourseReportLevelBadCeiling = jsonObject.optInt("bad");
                        mCourseReportLevelOrdinaryCeiling = jsonObject.optInt("ordinary");
                        mCourseReportLevelGoodCeiling = jsonObject.optInt("good");
                        mCourseReportOfflineScore = jsonObject.optInt("offline");
                        mCourseReportNoNeedScore = jsonObject.optInt("noneed");
                    } catch (Exception e) {}
                    break;
                case "teacher_withdraw_newpolicy_hint":
                    mAppWithdrawNewpolicyContent = item.value[0];
                    break;
                case "student_display_online_after_courses":
                    try {
                        mStudentDisplayOnlineAfterCourses = Integer
                                .parseInt(item.value[0]) != 0;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case "teacher_ivr_phone_number":
                    if (ivrPhoneNumberList == null) {
                        ivrPhoneNumberList = new ArrayList<>();
                    }
                    ivrPhoneNumberList.clear();
                    Collections.addAll(ivrPhoneNumberList, item.value);
                    break;
                case "teacher_report_share":
                    try {
                        mTeacher_report_share = Integer.parseInt(item.value[0]);
                    } catch (Exception e) {
                        mTeacher_report_share = 1;
                    }
                    break;
                case "teacher_daiming_city":
                    setTeacherDaimingCity(item.value[0]);
                    break;
                case "ta_student_pool_audition_days_limit":
                    try {
                        mTaStudentPoolAuditionDaysLimit = Integer.parseInt(item.value[0]);
                    } catch (Exception e) {
                        mTaStudentPoolAuditionDaysLimit = 30;
                    }
                    break;
                case "teacher_index_expire_time_for_teaching_research_group":
                    setTeacherIndexExpireTime(item.value[0]);
                    break;
                case "student_show_live_audition":
                    try {
                        mIsOnlineAuditionOpened = Integer.parseInt(item.value[0]) == 1;
                    } catch (Exception e) {
                        mIsOnlineAuditionOpened = false;
                    }
                    break;
                case KEY_TA_PAGE_SHARE:// 专属助教分享得奖
                    parseAndSaveActiveInfo(KEY_TA_PAGE_SHARE, item.value[0]);
                    break;
                case KEY_STUDENT_MINE_INVITE_FRIEND:
                    parseAndSaveActiveInfo(KEY_STUDENT_MINE_INVITE_FRIEND, item.value[0]);
                    break;
                case KEY_TR_WALLET_BOTTOM_BANNER:
                    parseAndSaveActiveInfo(KEY_TR_WALLET_BOTTOM_BANNER, item.value[0]);
                    break;
                case KEY_STUDENT_POOL_PAGE_BANNER:
                    parseAndSaveActiveInfo(KEY_STUDENT_POOL_PAGE_BANNER, item.value[0]);
                    break;
                case KEY_SUCCESS_PAGE_FLOAT_PIC:
                    parseAndSaveActiveInfo(KEY_SUCCESS_PAGE_FLOAT_PIC, item.value[0]);
                    break;
                case KEY_LEARN_CENTER_BOTTOM_BANNER:
                    parseAndSaveActiveInfo(KEY_LEARN_CENTER_BOTTOM_BANNER, item.value[0]);
                    break;
                case KEY_TR_HOME_INVITE_STUDENT:
                    parseAndSaveActiveInfo(KEY_TR_HOME_INVITE_STUDENT, item.value[0]);
                    break;
                case KEY_SUCCESS_PAGE_FLOAT_PIC_V2:
                    parseAndSaveActiveInfoV2(KEY_SUCCESS_PAGE_FLOAT_PIC_V2,
                            item.value[0]);
                    break;
                case "api_order_course_action_config": // 调退课罚款配置
                    try {
                        JSONObject jsonObject = new JSONObject(item.value[0]);
                        penaltyHour = jsonObject.optInt("penaltyHour");
                        teacherResponsibilityPenaltyRate = jsonObject.optInt("teacherResponsibilityPenaltyRate");
                        teacherResponsibilityBountyRate = jsonObject.optInt("teacherResponsibilityBountyRate");
                        studentResponsibilityPenaltyRate = jsonObject.optInt("studentResponsibilityPenaltyRate");
                        studentResponsibilityBountyRate = jsonObject.optInt("studentResponsibilityBountyRate");
                    } catch (Exception e) {}
                    break;
                case "api_order_course_action_switch":
                    try {
                        api_order_course_action_switch = Boolean.parseBoolean(item.value[0]);
                    } catch (Exception e) {
                        api_order_course_action_switch = false;
                    }
                    break;
                case "api_order_course_action_start_time":
                    try {
                        api_order_course_action_start_time = DateUtils.ymdhmsSdf.parse(item.value[0]).getTime();
                    } catch (ParseException e) {
                    }
                    break;

                case "pc_teach_plan":
                    teachPlanOnPcOptions = item.value[0];
                    break;

                case KEY_WINTER_PACK_CONFIG:
                    break;
                case "is_h5_cache_optimize_ta":
                    setIsNeedH5CacheOptimizeTa("true".equals(item.value[0]));
                    break;
                case "teach_plan_word_num":
					break;
                
                case "pull_stock_discount_config":
                    onlineNewOrderDiscount = item.value[0];
                    break;
                default:
                    break;
            }
            
        }
    }
    
    private void setTeacherIndexExpireTime(String dateString) {
        mTeacherIndexExpireTimeString = dateString;
    }
    
    private void saveGrouponCities(HashSet<Integer> citySet, String string) {
        
        if ("-1".equals(string)) {
            citySet.add(-1);
        }
        else {
            String[] cities = string.split(";");
            for (String s : cities) {
                try {
                    citySet.add(Integer.parseInt(s));
                } catch (Exception e) {
                    
                }
            }
        }
    }
    
    private void saveLiveRegisterBonus(String string) {
        try {
            String[] bonus = string.split(";");
            if (bonus.length >= 2) {
                mLiveShareBonus = Double.parseDouble(bonus[0]);
                mLiveRegisterBonus = Double.parseDouble(bonus[1]);
            }
        } catch (Exception e) {
            mLiveShareBonus = 0;
            mLiveRegisterBonus = 0;
        }
    }
    
    private void setIsSupportThirdPartHomework(boolean supportThirdPartHomework) {
        mIsSupportThirdPartHomework = supportThirdPartHomework;
    }
    
    public String getTestLecutreId() {
        return mTestLecutreId;
    }
    
    public String getTeacherProfileExplainInformationId() {
        return mTeacherProfileExplainInformationId;
    }
    
    public void setApiHttps(String isHttpsEnable) {
        boolean isHttps = false;
        try {
            isHttps = Integer.parseInt(isHttpsEnable) != 0;
        } catch (NumberFormatException e) {
            isHttps = Boolean.parseBoolean(isHttpsEnable);
        }
        
        mIsApiHttps = isHttps;
        DNSManager.INSTANCE().updateApiDomain();
    }
    
    /** api 的接口是否访问HTTPS */
    public boolean isApiHttps() {
        return mIsApiHttps;
    }
    
    /**
     * 指定的城市 是否支持朋友团 1.0 or 2.0
     *
     * @param cityId
     *            城市id
     */
    public boolean isSupportAnyGroupon(int cityId) {
        return isSupportGroupon(cityId) || isSupportGrouponV2(cityId);
    }
    
    /**
     * 指定的城市 是否支持朋友团 1.0
     *
     * @param cityId
     *            城市id
     */
    public boolean isSupportGroupon(int cityId) {
        return isSupportGroupon(mSupportGrouponCities, cityId);
    }
    
    /**
     * 指定的城市 是否支持朋友团 2.0
     *
     * @param cityId
     *            城市id
     */
    public boolean isSupportGrouponV2(int cityId) {
        return isSupportSmallGrouponV2(cityId) || isSupportBigGrouponV2(cityId);
    }
    
    /**
     * 指定的城市 是否支持朋友团2.0的 2人团 or 3人团
     *
     * @param cityId
     *            城市id
     */
    public boolean isSupportSmallGrouponV2(int cityId) {
        
        if (mSupportSmallGrouponV2Cities == null) {
            mSupportSmallGrouponV2Cities = new HashSet<>();
        }
        
        if (mSupportSmallGrouponV2Cities.isEmpty()
                && SPManager.contains(SP_KEY_SMALL_GROUPON_CITIES)) {
            saveGrouponCities(mSupportSmallGrouponV2Cities,
                    SPManager.getString(SP_KEY_SMALL_GROUPON_CITIES, ""));
        }
        
        return isSupportGroupon(mSupportSmallGrouponV2Cities, cityId);
    }
    
    /**
     * 指定的城市 是否支持朋友团2.0的 4人团 or 5人团
     *
     * @param cityId
     *            城市id
     */
    public boolean isSupportBigGrouponV2(int cityId) {
        if (mSupportBigGrouponV2Cities == null) {
            mSupportBigGrouponV2Cities = new HashSet<>();
        }
        
        if (mSupportBigGrouponV2Cities.isEmpty()
                && SPManager.contains(SP_KEY_BIG_GROUPON_CITIES)) {
            saveGrouponCities(mSupportBigGrouponV2Cities,
                    SPManager.getString(SP_KEY_BIG_GROUPON_CITIES, ""));
        }
        
        return isSupportGroupon(mSupportBigGrouponV2Cities, cityId);
    }
    
    /** 获取默认评价内容 */
    public String getDefaultAppraiseContent() {
        if (!TextUtils.isEmpty(mDefaultAppraiseContent)) {
            return mDefaultAppraiseContent;
        }
        else {
            return "默认评价";
        }
    }
    
    /** 获取提现提醒内容 */
    public String getWithdrawNewpolicyContent() {
        if (!TextUtils.isEmpty(mAppWithdrawNewpolicyContent)) {
            return mAppWithdrawNewpolicyContent;
        }
        else {
            return "";
        }
    }
    
    private boolean isSupportGroupon(HashSet<Integer> citySet, int cityId) {
        return !(citySet == null || citySet.isEmpty())
                && (citySet.contains(-1) || citySet.contains(cityId));
    }
    
    private void setIsShowSelectedComment(String isShowSelectedComment) {
        mIsShowSelectedComment = "true".equals(isShowSelectedComment);
    }
    
    private void setIsShowTeacherRank(String isShowTeacherRank) {
        mIsShowTeacherRank = "true".equals(isShowTeacherRank);
    }
    
    private void setIsShowOrderNeedConfirm(String isShowOrderNeedConfirm) {
        mIsShowOrderNeedConfirm = "true".equals(isShowOrderNeedConfirm);
    }
    
    private void setSearchListAbTest(String abTest) {
        abTest = abTest.toLowerCase(Locale.US);
        
        if ("b".equals(abTest) || "a".equals(abTest)) {
            mSearchListAbTest = abTest;
        }
    }
    
    private void setTeacherFeaturePhrases(String featurePhrases) {
        if (mTeacherFeatureList == null) {
            mTeacherFeatureList = new ArrayList<>();
        }
        mTeacherFeatureList.clear();
        Collections.addAll(mTeacherFeatureList, featurePhrases.split(";"));
    }
    
    public ArrayList<String> getIVRPhoneList() {
        return ivrPhoneNumberList;
    }
    
    /**
     * 590需求，我的助教页面根据通用配置显示/隐藏电话，存一个list，若老师的的助教城市id在这个list中，不显示拨打电话按钮
     */
    private void setTeacherDaimingCity(String string) {
        if ("-1".equals(string)) {
            mNotShowingPhoneIconCityIdList = null;
        }
        else {
            try {
                String[] cityIds = string.split(",");
                int length = cityIds.length;
                mNotShowingPhoneIconCityIdList = new ArrayList<>(length);
                for (int i = 0; i < length; i++) {
                    mNotShowingPhoneIconCityIdList.add(Integer.parseInt(cityIds[i]));
                }
            } catch (Exception e) {
                mNotShowingPhoneIconCityIdList = null;
            }
        }
    }
    
    @Nullable
    public List<Integer> getTeacherDaiminCityIdList() {
        return mNotShowingPhoneIconCityIdList;
    }
    

    // since 5.0 拉取某个城市的 grade course 列表
    public void reqGradeCourseList(int cityId) {
        reqGradeCourseList(cityId, null);
    }
    
    public void reqGradeCourseList(int cityId,
            final DefaultDataCourseGradeReqListener listener) {
        Logger.d(TAG, "reqGradeCourseList -- city=" + cityId);
        ProtoReq req = new ProtoReq(CommonUrl.COMMON_GRADE_COURSE_LIST_URL.url());
        req.setReqMethod(HttpMethod.GET);
        if (cityId > 0) {
            req.setUrlParam("city_id", String.valueOf(cityId));
        }
    }
    
    public GradeCourseProto.StudentCourseLearningStatusConfig[] getLearningStageStatuses() {
        return mLearningStageStatuses;
    }
    
    private void addDistrictInternal(CityDistrict district) {
        if (district.isOpen) {
            List<CityDistrict> list = mDistrictsSArray.get(district.cityId);
            if (list == null) {
                list = new ArrayList<>();
                mDistrictsSArray.put(district.cityId, list);
            }
            list.add(district);
        }
    }
    
    private void syncOpenCityList() {
        
        if (mOpenedCityList == null) {
            mOpenedCityList = new ArrayList<>();
        }
        else {
            mOpenedCityList.clear();
        }
        if (mStationedCityList == null) {
            mStationedCityList = new ArrayList<>();
        }
        else {
            mStationedCityList.clear();
        }
        if (mHotCityList == null) {
            mHotCityList = new ArrayList<>();
        }
        else {
            mHotCityList.clear();
        }
        Logger.d(TAG, "all city size is " + mAllCityList.size());
        for (City city : mAllCityList) {
            if (city.isVisible && city.isStationed) {
                mOpenedCityList.add(city);
            }
            if (city.isStationed) {
                mStationedCityList.add(city);
            }
            if (city.isHot) {
                Logger.d(TAG, "city is hot");
                mHotCityList.add(city);
            }
        }
    }
    
    /** 预加载数据 */
    public void loadDataFromDB() {
        Logger.d(TAG, "load data from db  --->");
        
        mIsTimeExpand = SPManager.getBoolean(SP_KEY_IS_TIME_EXPAND, false);
        mAppType = SPManager.getInt(SP_KEY_APP_TYPE, 0);
        mDefaultRenewCourseCount = SPManager.getInt(SP_KEY_DEFAULT_RENEW_COURSE_COUNT, 4);
        mNeedH5CacheOptimize = SPManager.getBoolean(SP_KEY_IS_H5_CACHE_OPTIMIZE, false);
        mNeedH5CacheOptimizeTa = SPManager.getBoolean(SP_KEY_IS_H5_CACHE_OPTIMIZE_TA, false);
        mNeedUploadUserBehaviorLog = SPManager.getBoolean(SP_KEY_IS_UPLOAD_BEHAVIOR_LOG,
                true);
        mNeedUploadNetworkLog = SPManager.getBoolean(SP_KEY_IS_UPLOAD_NETWORK_LOG, true);
        
        mMaxRenewCourseCount = SPManager.getInt(SP_KEY_MAX_RENEW_COURSE_COUNT);
        mMinRenewCourseCount = SPManager.getInt(SP_KEY_MIN_RENEW_COURSE_COUNT);
        
        mStuResSettingUrl = SPManager.getString(SP_KEY_STU_RES_SETTING_URL);
        mHttpRequestTimeoutSeconds = SPManager.getInt(SP_KEY_HTTP_REQUEST_TIMEOUT, 10);
        mHttpRequestRetryCount = SPManager.getInt(SP_KEY_HTTP_REQUEST_RETRY_COUNT, 1);
        
        setApiHttps(SPManager.getString(SP_KEY_API_DOMAIN_HTTPS, "0"));
        
        parseConfigItems((AppConfig.AppConfigItem[]) PBUtil.loadMsgArray(
                BaseData.getInstance().getSPWrapper(), SP_KEY_RSP_APP_COMMON_CONFIG,
                AppConfig.AppConfigItem.class));
        
        mDBDao.execTask(new DBProcessTask<Boolean>(null) {
            
            @Override
            protected Boolean doJob() {
                
                mAllCityList = mDBDao.getCityList();
                syncOpenCityList();
                mGradeCourseSet = mDBDao.getGradeCourseInfo();
                mSubCourseList = mDBDao.getSubCourseList();
                mGradeGroupTypeList = mDBDao.getGradeGroupTypeList();
                syncValidGradeGroupTypeList();
                
                List<CityDistrict> districtList = mDBDao
                        .getDistrictList();
                Logger.d("district count = " + districtList.size());
                if (!districtList.isEmpty()) {
                    for (CityDistrictProto.CityDistrict district : districtList) {
                        addDistrictInternal(district);
                    }
                }
                
                Logger.d("district array size = " + mDistrictsSArray.size());
                return true;
            }
        });
    }
    
    // ------------------------------------------通用配置数据--------------------------------------------
    /** 获取时间扩展开关 是否打开 */
    public boolean isTimeExpand() {
        return mIsTimeExpand;
    }
    
    private void saveIsTimeExpand(boolean flag) {
        mIsTimeExpand = flag;
        SPManager.put(SP_KEY_IS_TIME_EXPAND, mIsTimeExpand);
    }

    public void setDefaultRenewCourseCount(int count) {
        mDefaultRenewCourseCount = count;
        SPManager.put(SP_KEY_DEFAULT_RENEW_COURSE_COUNT, count);
    }

    public void setIsNeedH5CacheOptimize(boolean flag) {
        mNeedH5CacheOptimize = flag;
        SPManager.put(SP_KEY_IS_H5_CACHE_OPTIMIZE, mNeedH5CacheOptimize);
    }

    public void setIsNeedH5CacheOptimizeTa(boolean flag) {
        mNeedH5CacheOptimizeTa = flag;
        SPManager.put(SP_KEY_IS_H5_CACHE_OPTIMIZE_TA, mNeedH5CacheOptimizeTa);
    }

    public void setNeedUploadUserBehaviorLog(boolean flag) {
        mNeedUploadUserBehaviorLog = flag;
        SPManager.put(SP_KEY_IS_UPLOAD_BEHAVIOR_LOG, mNeedUploadUserBehaviorLog);
    }
    
    /** 获取是否需要上传网络日志 */
    public boolean needUploadNetworkLog() {
        return mNeedUploadNetworkLog;
    }
    
    public void setNeedUploadNetworkLog(boolean flag) {
        mNeedUploadNetworkLog = flag;
        SPManager.put(SP_KEY_IS_UPLOAD_NETWORK_LOG, mNeedUploadNetworkLog);
    }
    
    /** 获取当前的应用类型 */
    public int getAppType() {
        return mAppType;
    }
    
    public void saveAppType(int type) {
        
        if (mAppType == type)
            return;
        
        mAppType = type;
        SPManager.put(SP_KEY_APP_TYPE, mAppType);
    }
    
    public void setMinRenewCourseCount(int minRenewCourseCount) {
        mMinRenewCourseCount = minRenewCourseCount;
        SPManager.put(SP_KEY_MIN_RENEW_COURSE_COUNT, minRenewCourseCount);
    }
    
    public int getHttpRequestTimeoutSeconds() {
        return mHttpRequestTimeoutSeconds;
    }
    
    public void setHttpRequestTimeoutSeconds(int httpRequestTimeoutSeconds) {
        if (httpRequestTimeoutSeconds < 2) {
            return;
        }
        this.mHttpRequestTimeoutSeconds = httpRequestTimeoutSeconds;
        SPManager.put(SP_KEY_HTTP_REQUEST_TIMEOUT, httpRequestTimeoutSeconds);
    }
    
    public int getHttpRequestRetryCount() {
        return mHttpRequestRetryCount;
    }
    
    public void setHttpRequestRetryCount(int httpRequestRetryCount) {
        if (httpRequestRetryCount < 0) {
            return;
        }
        this.mHttpRequestRetryCount = httpRequestRetryCount;
        SPManager.put(SP_KEY_HTTP_REQUEST_RETRY_COUNT, httpRequestRetryCount);
    }
    
    public void setMaxRenewCourseCount(int maxRenewCourseCount) {
        mMaxRenewCourseCount = maxRenewCourseCount;
        SPManager.put(SP_KEY_MAX_RENEW_COURSE_COUNT, maxRenewCourseCount);
    }
    
    public int getLectureAttendanceSyncInterval() {
        return mLectureAttendanceSyncInterval;
    }
    
    public void setLectureAttendanceSyncInterval(String lectureAttendanceSyncInterval) {
        try {
            this.mLectureAttendanceSyncInterval = Integer
                    .parseInt(lectureAttendanceSyncInterval);
        } catch (Exception e) {}
    }
    
    public void setHuanXinAudioUpgrade(String HuanXinAudioUpgrade) {
        try {
            this.mHuanXinAudioUpgrade = Boolean.parseBoolean(HuanXinAudioUpgrade);
        } catch (Exception e) {}
    }
    
    public boolean isHuanXinAudioUpgrade() {
        return mHuanXinAudioUpgrade;
    }
    
    public int getLectureNormalUserSendMsgCountPreMin() {
        return mLectureNormalUserSendMsgCountPreMin;
    }
    
    public void setLectureNormalUserSendMsgCountPreMin(
            String lectureNormalUserSendMsgCountPreMin) {
        try {
            this.mLectureAttendanceSyncInterval = Integer
                    .parseInt(lectureNormalUserSendMsgCountPreMin);
        } catch (Exception e) {}
    }
    
    /** 客服电话 */
    public String getCustomPhoneNumber() {
        return mCustomPhoneNumber;
    }
    
    /** 直播云地址 */
    public String getLiveCloudDownloadUrl() {
        return mLiveCloudDownloadUrl;
    }
    
    /** 生源宝设置h5地址 */
    public String getStuResSettingUrl() {
        return mStuResSettingUrl;
    }
    
    public void setStuResSettingUrl(String url) {
        this.mStuResSettingUrl = url;
        SPManager.put(SP_KEY_STU_RES_SETTING_URL, url);
    }
    

    /**
     * 保存活动信息
     *
     * @param key
     * @param data
     */
    private void parseAndSaveActiveInfo(String key, String data) {
        if (!TextUtils.isEmpty(data) && !TextUtils.isEmpty(key)) {
        }
    }
    
    /**
     * 保存活动信息V2,支持多个活动
     *
     * @param key
     * @param data
     */
    private void parseAndSaveActiveInfoV2(String key, String data) {
        if (!TextUtils.isEmpty(data) && !TextUtils.isEmpty(key)) {
        }
    }
    
    /**
     * 5.8.0 家长端首页 驻点城市是否要显示在线课item
     */
    public boolean getDisplayOnlineCourse() {
        return mStudentDisplayOnlineAfterCourses;
    }
    
    // ------------------------------------------通用基础数据--------------------------------------------
    /** 获取城市列表 */
    public ArrayList<City> getCityList() {
        if (BaseData.isTestUser()) {
            return mAllCityList;
        }
        else {
            // 非测试用户需要过滤掉测试城市
            ArrayList<City> list = new ArrayList<>();
            
            for (City city : mAllCityList) {
                if (!city.isTest) {
                    list.add(city);
                }
            }
            
            return list;
        }
    }
    
    /** 获取已开通城市列表 */
    public ArrayList<City> getOpenCityList() {
        if (BaseData.isTestUser()) {
            return mOpenedCityList;
        }
        else {
            // 非测试用户需要过滤掉测试城市
            ArrayList<City> list = new ArrayList<>();
            
            for (City city : mOpenedCityList) {
                if (!city.isTest) {
                    list.add(city);
                }
            }
            
            return list;
        }
    }
    
    /** 获取驻点城市列表 */
    public ArrayList<City> getStationedCityList() {
        if (BaseData.isTestUser()) {
            return mStationedCityList;
        }
        else {
            // 非测试用户需要过滤掉测试城市
            ArrayList<City> list = new ArrayList<>();
            
            for (City city : mStationedCityList) {
                if (!city.isTest) {
                    list.add(city);
                }
            }
            
            return list;
        }
    }
    
    /** 获取已开通城市列表 */
    public ArrayList<City> getHotCityList() {
        Logger.d(TAG, " hot size is " + mHotCityList.size());
        if (BaseData.isTestUser()) {
            return mHotCityList;
        }
        else {
            // 非测试用户需要过滤掉测试城市
            ArrayList<City> list = new ArrayList<>();
            
            for (City city : mHotCityList) {
                Logger.d(TAG, " hot test is " + city.isTest);
                if (!city.isTest) {
                    list.add(city);
                }
            }
            
            return list;
        }
    }
    
    /**
     * 是否为驻点城市
     *
     * @param cityId
     * @return
     */
    public boolean isCityStationed(int cityId) {
        if (mStationedCityList == null || mStationedCityList.isEmpty())
            return false;
        
        for (City city : mStationedCityList) {
            if (cityId == city.id) {
                
                if (BaseData.isTestUser() || !city.isTest) {
                    return true;
                }
                else {
                    return false;
                }
            }
        }
        return false;
    }
    
    /**
     * 是否为热门城市
     *
     * @param cityId
     * @return
     */
    public boolean isCityHot(int cityId) {
        if (mStationedCityList == null || mStationedCityList.isEmpty())
            return false;
        
        for (City city : mHotCityList) {
            if (cityId == city.id) {
                
                if (BaseData.isTestUser() || !city.isTest) {
                    return true;
                }
                else {
                    return false;
                }
            }
        }
        return false;
    }
    
    public boolean isCityOpen(int cityId) {
        
        if (mOpenedCityList == null || mOpenedCityList.isEmpty())
            return false;
        
        for (City city : mOpenedCityList) {
            if (cityId == city.id) {
                
                if (BaseData.isTestUser() || !city.isTest) {
                    return true;
                }
                else {
                    return false;
                }
            }
        }
        return false;
    }
    
    /** 通过city id获取city */
    public City getCityById(int id) {
        if (mAllCityList != null) {
            for (City city : mAllCityList) {
                if (id == city.id) {
                    return city;
                }
            }
        }
        return null;
    }
    
    /** 通过city id获取开通city */
    public City getOpenCityById(int id) {
        for (City city : mOpenedCityList) {
            if (id == city.id) {
                return city;
            }
        }
        return null;
    }
    
    /** 通过city code获取city */
    public City getCityByCode(int code) {
        
        if (mAllCityList != null) {
            for (City city : mAllCityList) {
                if (code == city.code) {
                    return city;
                }
            }
        }
        
        return null;
    }
    
    public City getCityByCode(String name, int code) {
        City city = getCityByCode(code);
        if (city == null) {
            Logger.w("City Over : code = " + code + ", name + " + name);
            return City.unopen(0, name, code);
        }
        else {
            return city;
        }
    }
    
    /** 通过city id获取city code */
    public int getCityCodeById(int id) {
        City city = getCityById(id);
        if (city == null) {
            Logger.w(TAG, "getCityNameById null with id:" + id);
            return 0;
        }
        else {
            return city.code;
        }
    }
    
    /** 通过city code获取city id */
    public int getCityIdByCode(int code) {
        City city = getCityByCode(code);
        if (city == null) {
            Logger.w(TAG, "getCityIdByCode null with code:" + code);
            return 0;
        }
        else {
            return city.id;
        }
    }
    
    public City getCity(int id, String name) {
        City city = getCityById(id);
        if (city == null) {
            Logger.w("City Over : id = " + id + ", name + " + name);
            return City.unopen(id, name, 0);
        }
        else {
            return city;
        }
    }
    
    /** 通过city id获取city name */
    public String getCityNameById(int id) {
        City city = getCityById(id);
        if (city == null) {
            Logger.w(TAG, "getCityNameById null with id:" + id);
            return null;
        }
        else {
            return city.name;
        }
    }
    
    public City getCityByName(String name) {
        if (TextUtils.isEmpty(name)) {
            return null;
        }
        for (City city : mAllCityList) {
            if (name.equals(city.name)) {
                return city;
            }
        }
        return null;
    }
    

    public ArrayList<GradeCourseProto.GradeGroup> getValidGradeGroupTypeList() {
        return mValidGradeGroupTypeList;
    }
    
    /** 内部同步有效的 grade group type list */
    private void syncValidGradeGroupTypeList() {
        if (mValidGradeGroupTypeList == null) {
            mValidGradeGroupTypeList = new ArrayList<>();
        }
        else {
            mValidGradeGroupTypeList.clear();
        }
        
        for (GradeCourseProto.GradeGroup gradeGroup : mGradeGroupTypeList) {
            if (unknown_school_grade_group_type != gradeGroup.gradeGroupType
                    && gradeGroup.isOpen) {
                mValidGradeGroupTypeList.add(gradeGroup);
            }
        }
    }
}

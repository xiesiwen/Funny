package com.qingqing.base.dns;

/**
 * Created by Wangxiaxin on 2015/10/15.
 *
 * URL 标记类
 * */
public final class UrlTag {
    
    private static final ThreadLocal<UrlTag> INSTANCE = new ThreadLocal<UrlTag>() {
        @Override
        protected UrlTag initialValue() {
            return new UrlTag();
        }
    };
    
    public static UrlTag getInstance() {
        return INSTANCE.get();
    }
    
    private StringBuilder mStringBuilder;
    
    private UrlTag() {
        mStringBuilder = new StringBuilder();
    }
    
    public static final int TAG_API = 2;
    // public static final int TAG_QQ_API = TAG_QQ | TAG_API;
    public static final int TAG_SVC = 4;
    public static final int TAG_SVC_API = TAG_SVC | TAG_API;
    public static final int TAG_TA = 8;
    public static final int TAG_TA_API = TAG_TA | TAG_API;
    public static final int TAG_ACTIVITY_SVC = 0x10;
    public static final int TAG_PLAY_SVC = 0x20;
    public static final int TAG_PROMOTION_SVC = 0x40;
    public static final int TAG_COMMENT_SVC = 0x80;
    public static final int TAG_PUSH_SVC = 0x100;
    public static final int TAG_IMG_SVC = 0x200;
    public static final int TAG_TA_PERFORMANCE = 0x400;
    public static final int TAG_SCORE_SVC = 0x800;
    public static final int TAG_USER_CENTER = 0x1000;
    public static final int TAG_SPIDERCLK = 0x2000;
    public static final int TAG_LIVE_SVC = 0x4000;
    public static final int TAG_TA_PERFORMANCE_API = TAG_TA_PERFORMANCE | TAG_API;
    public static final int TAG_ACTIVITY_SVC_API = TAG_ACTIVITY_SVC | TAG_API;
    public static final int TAG_PLAY_SVC_API = TAG_PLAY_SVC | TAG_API;
    public static final int TAG_PUSH_SVC_API = TAG_PUSH_SVC | TAG_API;
    public static final int TAG_IMG_SVC_API = TAG_IMG_SVC | TAG_API;
    public static final int TAG_SCORE_SVC_API = TAG_SCORE_SVC | TAG_API;
    public static final int TAG_USER_CENTER_API = TAG_USER_CENTER | TAG_API;
    public static final int TAG_LIVE_SVC_API = TAG_LIVE_SVC | TAG_API;
    
    public static final int TAG_ADS_SVC_API = TAG_PROMOTION_SVC | TAG_API;
    public static final int TAG_COMMENT_SVC_API = TAG_COMMENT_SVC | TAG_API;

    private static final String TAG_URL_SVC = "/svc";
    private static final String TAG_URL_API = "/api";
    private static final String TAG_URL_TA = "/taapi";
    private static final String TAG_URL_TA_PERFORMANCE = "/performance";
    private static final String TAG_URL_ACTIVITY_SVC = "/activitysvc";
    private static final String TAG_URL_PLAY_SVC = "/playsvc";
    private static final String TAG_URL_ADS_SVC = "/promotionsvc";
    private static final String TAG_URL_COMMENT_SVC = "/commentsvc";
    private static final String TAG_URL_PUSH_SVC = "/pushsvc";
    private static final String TAG_URL_IMG_SVC = "/imgsvc";
    private static final String TAG_URL_SCORE_SVC = "/scoresvc";
    private static final String TAG_URL_USER_CENTER = "/usercenter";
    private static final String TAG_URL_SPIDERCLK = "/spiderclk";
    private static final String TAG_URL_LIVE_SVC = "/livesvc";
    public static boolean containsTag(int tag, int subTag) {
        return (tag & subTag) > 0;
    }
    
    public static String getTagString(int tag) {
        
        if (tag <= 0)
            return "";
        
        UrlTag ut = getInstance();
        ut.mStringBuilder.setLength(0);
        
        if ((tag & TAG_SVC) > 0) {
            ut.mStringBuilder.append(TAG_URL_SVC);
        }
        
        if ((tag & TAG_TA) > 0) {
            ut.mStringBuilder.append(TAG_URL_TA);
        }

        if ((tag & TAG_TA_PERFORMANCE) > 0) {
            ut.mStringBuilder.append(TAG_URL_TA_PERFORMANCE);
        }
        
        if ((tag & TAG_ACTIVITY_SVC) > 0) {
            ut.mStringBuilder.append(TAG_URL_ACTIVITY_SVC);
        }

        if ((tag & TAG_PLAY_SVC) > 0) {
            ut.mStringBuilder.append(TAG_URL_PLAY_SVC);
        }

        if ((tag & TAG_PROMOTION_SVC) > 0) {
            ut.mStringBuilder.append(TAG_URL_ADS_SVC);
        }

        if ((tag & TAG_COMMENT_SVC) > 0) {
            ut.mStringBuilder.append(TAG_URL_COMMENT_SVC);
        }

        if ((tag & TAG_PUSH_SVC) > 0) {
            ut.mStringBuilder.append(TAG_URL_PUSH_SVC);
        }

        if ((tag & TAG_IMG_SVC) > 0) {
            ut.mStringBuilder.append(TAG_URL_IMG_SVC);
        }

        if ((tag & TAG_SCORE_SVC)>0){
            ut.mStringBuilder.append(TAG_URL_SCORE_SVC);
        }

        if ((tag & TAG_USER_CENTER)>0){
            ut.mStringBuilder.append(TAG_URL_USER_CENTER);
        }
        if ((tag & TAG_SPIDERCLK) > 0) {
            ut.mStringBuilder.append(TAG_URL_SPIDERCLK);
        }

        if ((tag & TAG_LIVE_SVC)>0){
            ut.mStringBuilder.append(TAG_URL_LIVE_SVC);
        }

        // at the end
        if ((tag & TAG_API) > 0) {
            ut.mStringBuilder.append(TAG_URL_API);
        }
        
        return ut.mStringBuilder.toString();
    }
    
}

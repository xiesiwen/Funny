package com.qingqing.base.config;

import com.qingqing.base.data.StatisticalDataConstants;
import com.qingqing.base.dns.DNSManager;
import com.qingqing.base.dns.UrlTag;
import com.qingqing.base.http.HttpUrl;

/**
 * Created by wangxiaxin on 2016/9/13.
 *
 * 一些通用的url
 */
public enum CommonUrl {
    
    /**
     * 逻辑接口
     */
    V1_MAIN_URL(DNSManager.DomainType.API, UrlTag.TAG_SVC, "/login.do"),
    
    /** 时间 */
    TIME(DNSManager.DomainType.TIME, 0, ""),
    
    /**
     * 基础数据获取
     */
    COMMON_DATA_URL(DNSManager.DomainType.CDN, "/cpb/v2/config/app_common"),
    
    /**
     * 某个城市的 年级科目信息
     */
    COMMON_GRADE_COURSE_LIST_URL(DNSManager.DomainType.CDN, "/cpb/v2/config/grade_course_list"),

    /**
     * IMAGE URL
     */
    IMAGE_URL(DNSManager.DomainType.IMAGE, 0, ""),
    
    /**
     * 活动注册
     */
    ACTIVITY_REGISTER_URL(DNSManager.DomainType.ACTIVITY, UrlTag.TAG_ACTIVITY_SVC_API, "/pb/v1/invite_student/register_by_invite_code"),

    /**
     * 登录
     */
    LOGIN_URL(DNSManager.DomainType.PASSPORT, "/pb/v1/login"),

    /**
     * 登录 V2
     */
    LOGIN_URL_V2(DNSManager.DomainType.PASSPORT, "/pb/v2/login"),

    /**
     * 登录和注册
     */
    REGISTER_OR_LOGIN(DNSManager.DomainType.PASSPORT, "/pb/v2/register_or_login"),
    /**
     * token登录
     */
    TOKEN_LOGIN_URL(DNSManager.DomainType.PASSPORT, "/pt/v1/login"),

    /**
     * TA登录
     */
    TA_GET_BASE_INFO(UrlTag.TAG_TA_API, "/pt/v1/assistant/base_info"),

    /**
     * 重置密码
     */
    RESET_PASSWORD("/pb/v1/resetpasswd"),

    ;
    
    private HttpUrl httpUrl;
    
    CommonUrl(String subPath) {
        httpUrl = new HttpUrl(subPath);
    }
    
    CommonUrl(DNSManager.DomainType type, String subPath) {
        httpUrl = new HttpUrl(type, subPath);
    }

    CommonUrl(int tag,String subPath){
        httpUrl = new HttpUrl(tag,subPath);
    }
    
    CommonUrl(DNSManager.DomainType type, int tag, String subPath) {
        httpUrl = new HttpUrl(type, tag, subPath);
    }
    
    public HttpUrl url() {
        return httpUrl;
    }
}

package com.qingqing.base.http;

import android.text.TextUtils;

import com.qingqing.base.config.LogicConfig;
import com.qingqing.base.data.DefaultDataCache;
import com.qingqing.base.data.SPManager;
import com.qingqing.base.dns.DNSManager;
import com.qingqing.base.dns.DomainConfig;
import com.qingqing.base.dns.HostManager;
import com.qingqing.base.dns.UrlTag;
import com.qingqing.base.log.Logger;
import com.qingqing.base.utils.PackageUtil;
import com.qingqing.base.utils.UrlUtil;

import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * Created by wangxiaxin on 2016/9/13.
 *
 * url封装类
 */
public class HttpUrl {
    
    private DNSManager.DomainType mType;
    private int mTag;
    private String mSubPath;
    private HashMap<String,String> urlParam;
    
    private static final StringBuilder sUrlBuilder = new StringBuilder();
    public static boolean supportHostSwitch = false;

    /**
     * 默认 DomainType.API
     */
    public HttpUrl(String subPath) {
        this(DNSManager.DomainType.API, subPath);
    }
    
    /**
     * 默认 DomainType.API
     */
    public HttpUrl(int tag, String subPath) {
        this(UrlTag.containsTag(tag, UrlTag.TAG_TA) ? DNSManager.DomainType.TA
                : DNSManager.DomainType.API, tag, subPath);
    }
    
    /**
     * 默认添加 /svc/api
     */
    public HttpUrl(DNSManager.DomainType type, String subPath) {
        this(type, UrlTag.TAG_SVC_API, subPath);
    }
    
    /**
     * url 分为三部分组成： domain + path1 + path2
     *
     * @param type
     *            PASSPORT, API, MQTT, CDN, IMAGE, LOG, TIME, PUSH, H5,
     *            IMAGE_DEPRECATED, PIC_DEPRECATED, LOG_UPLOAD_DEPRECATED
     *            <p/>
     *            用来寻找对应的域名或者IP，默认域名的配置在DomainConfig中
     * @param tag
     *            TAG_QQ, TAG_API, TAG_QQ_API 用来标明在domain后面 追加 /QQ or /api
     * @param subPath
     *            后面具体的url部分
     *            <p/>
     *            示例： 注册接口
     *            REGISTER_URL(DNSManager.DomainType.PASSPORT,UrlTag.TAG_QQ_API,
     *            "/pb/v1/register"), 对应的使用默认域名的url即为
     *            https://passport.changingedu.com/QQ/api/pb/v1/register
     */
    public HttpUrl(DNSManager.DomainType type, int tag, String subPath) {
        mType = type;
        mTag = tag;
        mSubPath = subPath;
    }

    /**
     * 添加url 参数
     * */
    public HttpUrl addUrlParam(String key,String value){
        if(urlParam == null){
            urlParam = new HashMap<>();
        }
        urlParam.put(key,value);
        return this;
    }
    
    public boolean usingDomain() {
        DNSManager.HostList hl = DNSManager.INSTANCE().getHostList(mType);
        return hl.isUseDomain();
    }
    
    public String domain() {
        DNSManager.HostList hl = DNSManager.INSTANCE().getHostList(mType);
        return hl.getDefaultHost();
    }
    
    public String url() {
        String domain = DNSManager.INSTANCE().getHost(mType);
        final String tag = UrlTag.getTagString(mTag);

        if (TextUtils.isEmpty(domain)) {
            Logger.w("UrlConfig", "  getUrl failed : domain type=" + mType + "  tag="
                    + mTag + " subUrl=" + mSubPath);
            return null;
        }

        synchronized (sUrlBuilder) {
            sUrlBuilder.setLength(0);
            final boolean isHttps = DNSManager.isHttpsDomain(mType);
            sUrlBuilder.append(isHttps ? DomainConfig.HTTPS : DomainConfig.HTTP);
            sUrlBuilder.append(domain);
            sUrlBuilder.append(tag);
            sUrlBuilder.append(mSubPath);
            return UrlUtil.addParamToUrl(sUrlBuilder.toString(),urlParam);
        }
    }
    
    public boolean cdn() {
        String domain = DNSManager.INSTANCE().getHost(mType);
        return !TextUtils.isEmpty(domain) && domain.startsWith("cdn");
//        return mType == DNSManager.DomainType.CDN;
    }
    
    public boolean https() {
        return DNSManager.isHttpsDomain(mType);
    }
    
    @Override
    public String toString() {
        return url();
    }
}

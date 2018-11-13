package com.qingqing.base.dns;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.text.TextUtils;

import com.qingqing.base.BaseApplication;
import com.qingqing.base.config.LogicConfig;
import com.qingqing.base.core.CountDownCenter;
import com.qingqing.base.data.DefaultDataCache;
import com.qingqing.base.data.SPManager;
import com.qingqing.base.http.HttpMethod;
import com.qingqing.base.http.error.HttpError;
import com.qingqing.base.http.req.HttpReq;
import com.qingqing.base.http.req.StringReq;
import com.qingqing.base.log.Logger;
import com.qingqing.base.time.NetworkTime;
import com.qingqing.base.utils.DeviceUtil;
import com.qingqing.base.utils.NetworkUtil;
import com.qingqing.base.utils.PackageUtil;
import com.qingqing.base.utils.StringUtil;
import com.qingqing.base.utils.TripleDesUtil;
import com.qingqing.base.view.ToastWrapper;
import com.qingqing.qingqingbase.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * Created by Wangxiaxin on 2015/10/14.
 * <p/>
 * DNS 管理类
 */
public class DNSManager {
    
    public enum DomainType {
        PASSPORT, API, CDN, IMAGE, LOG, TIME, PUSH, H5, DOWNLOAD, FRONT, API2,
        TA, TA_PERFORMANCE, M, M_ARTICLE, IMAGE_DEPRECATED, ACTIVITY, CDN_ACTIVITY,
        PLAY, ADS, COMMENT, SCORE, CDN_SCORE, USER_CENTER,CDN_USER_CENTER, WECHAT_FOLLOW, SPIDER,LIVE
    }

    private static final String TAG = "DNSManager";
    private static DNSManager sInstance;
    private boolean mIsIntranet;
    private HashMap<DomainType, HostList> mHostListMap;// 保存域名和IP的对应表
    private HashMap<String, Integer> mFailedHostMap;// 记录对应IP的失败表
    private long mLastDNSCheckTime;// 上次成功检测到DNS的时间

    private static final String KEY_INTRANET = "INTRANET";
    private static final String KEY_LAST_CHECK_TIME = "last_dns_check";
    private static final String KEY_HOST_LIST = "local_hlist";

    private boolean mEnable = false;
    private DNSConfig mConfig;
    private static final String REQ_URL = "http://211.148.18.200/httpdns?dn=changingedu.com&ttl=1";
    private static final String REQ_SLAVE_URL = " http://101.251.110.156/httpdns?dn=changingedu.com&ttl=1";

    private static final int DEFAULT_CHECK_INTERVAL = 2 * 3600;// 默认检测的时间间隔为2小时
    private static final int DEFAULT_FAILED_COUNT = 5;// 默认连续失败次数
    private static final int DEFAULT_CHECK_PRIMARY_IP_INTERVAL = 5 * 60;// 默认每隔5分钟检测下主IP是否可用
    private static final int DEFAULT_GRAY_LEVEL = 0;// 默认dns为关闭
    private final boolean isDNSLogicValid = false;// 是否启动DNS逻辑

    private NetworkUtil.ConnectionType mLastNetType;

    public static DNSManager INSTANCE() {
        if (sInstance == null) {
            synchronized (DNSManager.class) {
                if (sInstance == null) {
                    sInstance = new DNSManager();
                }
            }
        }
        return sInstance;
    }

    private class DNSConfig {
        int maxTimeoutCount;// 超时时间，间隔此时间后，重新请求DNS接口 单位：S
        int checkMainIPInterval;// 当切换到备用IP的情况下，隔多长时间重新检测主IP是否可用
        int grayLevel;// 灰度开关，0 ~ 100
        int maxFailedCount;// 连续失败N次后需要，从主切到备，已经在备用了，如果连续失败N次，则使用local domain
    }

    private DNSManager() {
        String channel = PackageUtil.getMetaData("qingqing.net");
        mIsIntranet = "true".equals(channel);
        Logger.d(TAG, "intranet = " + mIsIntranet);
        mConfig = new DNSConfig();

        mLastNetType = NetworkUtil.getConnectionType();
        initDefaultDomain();
        if (mIsIntranet)
            ToastWrapper.show(String.format("%s %s",
                    BaseApplication.getCtx().getString(R.string.base_use_test_domain),
                    BaseApplication.getAppNameInternal()));

        if (!readHostList()) {
            mLastDNSCheckTime = 0;
        }
        else {
            mLastDNSCheckTime = SPManager.getLong(KEY_LAST_CHECK_TIME, 0);
        }

        IntentFilter it = new IntentFilter();
        it.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        BroadcastReceiver networkReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                    NetworkUtil.ConnectionType type = NetworkUtil.getConnectionType();
                    Logger.i(TAG, "network change : new type = " + type + "   old type = "
                            + mLastNetType);
                    if (type != null && mLastNetType != type) {
                        mLastNetType = type;
                    }
                    else {
                        return;
                    }

                    if (!needCheckDNS()) {
                        Logger.i(TAG,
                                "reqDNS ignore because of interval : last="
                                        + mLastDNSCheckTime + "  now="
                                        + NetworkTime.currentTimeMillis());
                        return;
                    }

                    reqDNS(null);
                }
            }
        };

        BaseApplication.getCtx().registerReceiver(networkReceiver, it);
        mFailedHostMap = new HashMap<>();

        CountDownCenter.INSTANCE().addRepeatTask(TAG, mConfig.maxTimeoutCount,
                new CountDownCenter.CountDownListener() {
                    @Override
                    public void onCountDown(String tag, int leftCount) {
                        if (TAG.equals(tag) && leftCount == 0) {
                            if (NetworkUtil.isNetworkAvailable() && needCheckDNS()) {
                                reqDNS(null);
                            }
                        }
                    }
                });
    }

    public boolean isIntranet() {
        return mIsIntranet;
    }

    public boolean needCheckDNS() {
        if (!isDNSLogicValid)
            return false;

        final long curTime = NetworkTime.currentTimeMillis();
        return !existLocalHostList() || (curTime
                - mLastDNSCheckTime) > (mConfig.maxTimeoutCount * 1000 * 0.95f);
    }

    /**
     * 初始化域名
     */
    private void initDefaultDomain() {

        if (mHostListMap == null) {
            mHostListMap = new HashMap<>();
        }
        else {
            mHostListMap.clear();
        }

        mHostListMap.put(DomainType.PASSPORT, new HostList(
                mIsIntranet ? DomainConfig.PASSPORT_TEST : DomainConfig.PASSPORT));
        updateApiDomain();
        mHostListMap.put(DomainType.CDN,
                new HostList(mIsIntranet ? DomainConfig.CDN_TEST : DomainConfig.CDN));
        mHostListMap.put(DomainType.IMAGE,
                new HostList(mIsIntranet ? DomainConfig.IMAGE_TEST : DomainConfig.IMAGE));
        mHostListMap.put(DomainType.PUSH,
                new HostList(mIsIntranet ? DomainConfig.PUSH_TEST : DomainConfig.PUSH));
        mHostListMap.put(DomainType.H5,
                new HostList(mIsIntranet ? DomainConfig.H5_TEST : DomainConfig.H5));
        mHostListMap.put(DomainType.LOG,
                new HostList(mIsIntranet ? DomainConfig.LOG_TEST : DomainConfig.LOG));
        mHostListMap.put(DomainType.TIME,
                new HostList(mIsIntranet ? DomainConfig.TIME_TEST : DomainConfig.TIME));
        mHostListMap.put(DomainType.ACTIVITY, new HostList(
                mIsIntranet ? DomainConfig.ACTIVITY_TEST : DomainConfig.ACTIVITY));
        mHostListMap.put(DomainType.CDN_ACTIVITY, new HostList(mIsIntranet
                ? DomainConfig.CDN_ACTIVITY_TEST : DomainConfig.CDN_ACTIVITY));
        mHostListMap.put(DomainType.PLAY,
                new HostList(mIsIntranet ? DomainConfig.PLAY_TEST : DomainConfig.PLAY));
        mHostListMap.put(DomainType.ADS,
                new HostList(mIsIntranet ? DomainConfig.ADS_TEST : DomainConfig.ADS));
        mHostListMap.put(DomainType.TA,
                new HostList(mIsIntranet ? DomainConfig.TA_TEST : DomainConfig.TA));
        mHostListMap.put(DomainType.TA_PERFORMANCE,
                new HostList(mIsIntranet ? DomainConfig.TA_PREFORMANCE_TEST
                        : DomainConfig.TA_PREFORMANCE));
        mHostListMap.put(DomainType.M,
                new HostList(mIsIntranet ? DomainConfig.M_TEST : DomainConfig.M));
        mHostListMap.put(DomainType.M_ARTICLE, new HostList(DomainConfig.M));
        mHostListMap.put(DomainType.COMMENT, new HostList(
                mIsIntranet ? DomainConfig.COMMENT_TEST : DomainConfig.COMMENT));
        mHostListMap.put(DomainType.SCORE,new HostList(
                mIsIntranet ? DomainConfig.SCORE_TEST :DomainConfig.SCORE
        ));
        mHostListMap.put(DomainType.CDN_SCORE,new HostList(
                mIsIntranet ? DomainConfig.CDN_SCORE_TEST :DomainConfig.CDN_SCORE
        ));
        mHostListMap.put(DomainType.USER_CENTER,new HostList(
                mIsIntranet? DomainConfig.USER_CENTER_TEST:DomainConfig.USER_CENTER
        ));
        mHostListMap.put(DomainType.CDN_USER_CENTER,new HostList(
                mIsIntranet? DomainConfig.CDN_USER_CENTER_TEST:DomainConfig.CDN_USER_CENTER
        ));
        mHostListMap.put(DomainType.WECHAT_FOLLOW, new HostList(
                mIsIntranet? DomainConfig.WECHAT_FOLLOW_TEST:DomainConfig.WECHAT_FOLLOW
        ));
        mHostListMap.put(DomainType.SPIDER, new HostList(
                mIsIntranet? DomainConfig.SPIDERCLK_TEST:DomainConfig.SPIDERCLK
        ));
        mHostListMap.put(DomainType.LIVE,new HostList(
                mIsIntranet?DomainConfig.LIVE_TEST:DomainConfig.LIVE
        ));
        mHostListMap.put(DomainType.DOWNLOAD,new HostList(
                mIsIntranet?DomainConfig.DOWNLOAD_TEST:DomainConfig.DOWNLOAD
        ));
        mHostListMap.put(DomainType.FRONT,new HostList(
                mIsIntranet?DomainConfig.FRONT_TEST:DomainConfig.FRONT
        ));
        mHostListMap.put(DomainType.API2,new HostList(
                mIsIntranet?DomainConfig.API2_TEST:DomainConfig.API2
        ));
        // 陈旧的域名配置
        mHostListMap.put(DomainType.IMAGE_DEPRECATED,
                new HostList(mIsIntranet ? DomainConfig.API_TEST : DomainConfig.IMAGE));
    }

    /** 更新api的域名配置 */
    public void updateApiDomain() {
        mHostListMap.remove(DomainType.API);
        mHostListMap.put(DomainType.API,
                new HostList(mIsIntranet ? DomainConfig.API_TEST : DomainConfig.API));
    }

    // 切换IP，同一IP对应的所有域名都要切换，如果切到备用域名，则需要开启熔断机制
    private void switchIP(String host) {

        Logger.o(TAG, "begin switch ip : " + host);
        // 1，判断是否是默认域名，是的话不需要再切换了
        if (!StringUtil.checkNumberDot(host))
            return;

        // 2，判断是否是主IP，是的话 所有使用该主IP的HostList都要切到 备1IP上，并开启熔断
        // 3，如果已经是备IP，如果后面还有备用IP，则继续切到下一个备用IP，否则切到默认域名上
        for (Object o : mHostListMap.entrySet()) {
            HashMap.Entry entry = (HashMap.Entry) o;
            HostList dl = (HostList) entry.getValue();
            // 切换逻辑
            if (dl.hasIPList()) {
                final int idx = dl.getIPIndex(host);
                if (idx == 0) {
                    Logger.o(TAG, "switch ip : " + host + " is primary ip");
                    CountDownCenter.INSTANCE().addTask(host, mConfig.checkMainIPInterval,
                            new CountDownCenter.CountDownListener() {
                                @Override
                                public void onCountDown(String tag, int leftCount) {
                                    if (leftCount == 0) {
                                        tryRestorePrimaryIP(tag);
                                    }
                                }
                            });
                }
                
                dl.useNextIP();// 使用下一个IP
            }
        }
    }
    
    // 尝试恢复主IP
    private void tryRestorePrimaryIP(String mainIP) {
        // 1，判断是否是默认域名，是的话不需要再切换了
        
        Logger.o(TAG, "begin try restore primary ip : " + mainIP);
        
        if (!StringUtil.checkNumberDot(mainIP))
            return;
        
        for (Object o : mHostListMap.entrySet()) {
            HashMap.Entry entry = (HashMap.Entry) o;
            HostList dl = (HostList) entry.getValue();
            if (dl.hasIPList()) {
                if (mainIP.equals(dl.getPrimaryIP())) {
                    Logger.o(TAG, "restore primary ip , domain=" + dl.getDefaultHost());
                    dl.usePrimaryIP();
                }
            }
        }
    }
    
    // 使用默认配置
    private void applyDefaultConfig() {
        Logger.o(TAG, "apply default config");
        applyConfig(DEFAULT_CHECK_INTERVAL, DEFAULT_CHECK_PRIMARY_IP_INTERVAL,
                DEFAULT_GRAY_LEVEL, DEFAULT_FAILED_COUNT);
        // 清除已有的IP信息
        clearAllIP();
    }
    
    /**
     * 应用配置信息
     */
    private void applyConfig(int timeoutCount, int interval, int grayLevel,
            int maxFailedCount) {
        
        // TODO test
        // grayLevel = 100;
        // maxFailedCount = 20;
        // interval = 60;
        // timeoutCount = 30;
        
        Logger.d(TAG,
                "applyConfig    timeoutCount=" + timeoutCount + "   interval=" + interval
                        + "   grayLevel=" + grayLevel + "   maxFailedCount="
                        + maxFailedCount);
        mConfig.checkMainIPInterval = interval;
        mConfig.grayLevel = grayLevel;
        mConfig.maxTimeoutCount = timeoutCount;
        mConfig.maxFailedCount = maxFailedCount;
        
        String deviceid = DeviceUtil.getIdentification();
        int hashCode = Math.abs(deviceid.hashCode());
        final int localGray = hashCode % 100;
        mEnable = (localGray < mConfig.grayLevel);
        Logger.d(TAG, "applyConfig    deviceid=" + deviceid + "  hashCode=" + hashCode
                + "   mEnable=" + mEnable);
    }
    
    public HostList getHostList(DomainType type) {
        return mHostListMap.get(type);
    }
    
    /**
     * 根据类型，获得对应的IP或者域名
     */
    public String getHost(DomainType type) {
        
        HostList dl = mHostListMap.get(type);
        if (dl != null) {
            return dl.getHost();
        }
        else {
            Logger.w(TAG, "domain type=" + type + "  not found");
            return null;
        }
    }
    
    public String getHost(String domain) {
        
        if (TextUtils.isEmpty(domain))
            return null;
        
        for (Object o : mHostListMap.entrySet()) {
            HashMap.Entry entry = (HashMap.Entry) o;
            HostList dl = (HostList) entry.getValue();
            if (dl.getDefaultHost().equals(domain)) {
                return dl.getHost();
            }
        }
        return null;
    }
    
    /**
     * 通知 指定的 host 连接超时
     */
    public void notifyHostTimeout(String host) {
        
        if (!mEnable)
            return;
        
        // 内部 操作计数累加，以及计数超过预定值之后的切换IP的动作
        Integer count = mFailedHostMap.get(host);
        if (count == null) {
            // new
            mFailedHostMap.put(host, 1);
        }
        else {
            
            Logger.d(TAG, "notifyHostTimeout  host=" + host + "  fail count=" + count);
            if (count >= mConfig.maxFailedCount) {
                // 切换HOST
                switchIP(host);
                mFailedHostMap.remove(host);
            }
            else {
                mFailedHostMap.put(host, count + 1);
            }
        }
    }
    
    /**
     * 通知 指定的host 连接OK
     */
    public void notifyHostOK(String host) {
        mFailedHostMap.remove(host);
    }
    
    /**
     * 切换内外网
     */
    public void switchNet() {
        
        final boolean old = mIsIntranet;
        mIsIntranet = !old;
        SPManager.put(KEY_INTRANET, mIsIntranet);
        Logger.d(TAG, "flag = " + mIsIntranet);
        
        ToastWrapper.show("切换网络操作成功-----" + (mIsIntranet ? "内网域名" : "外网域名")
                + "----应用5S后自动关闭，请重新进入");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        }, 5000);
    }
    
    /**
     * 判断域名是否使用HTTPS 协议
     */
    public static boolean isHttpsDomain(DomainType type) {
        
        if (type == DomainType.PASSPORT || type == DomainType.TIME || type == DomainType.SPIDER)
            return true;
        
        return DefaultDataCache.INSTANCE().isApiHttps();
    }
    
    // /**
    // * 发起dns请求
    // */
    // public void reqDNS(final Response.Listener<String> listener,
    // final Response.ErrorListener errorListener) {
    //
    // if (!isDNSLogicValid)
    // return;
    // Logger.o(TAG, "begin reqDNS");
    // SimpleStringReq req = new
    // SimpleStringReq(getReqUrl(REQ_URL)).abortHostHeader();
    // req.setNeedShowNoNetError(false).setReqMethod(ReqMethod.GET)
    // .setRspListener(listener != null ? listener : mRspListener)
    // .setErrorListener(new Response.ErrorListener() {
    // @Override
    // public void onErrorResponse(VolleyError volleyError) {
    // Logger.o(TAG, "reqDNS master url error,begin req slave");
    // reqDNSWithSlaveUrl(listener, errorListener);
    // }
    // }).reqSlave();
    // }
    
    private void reqDNSWithSlaveUrl(final HttpReq.HttpListener<String> listener) {
        Logger.o(TAG, "begin reqDNS with slave url");
        StringReq req = new StringReq(getReqUrl(REQ_SLAVE_URL));
        req.setNeedShowNoNetError(false).setReqMethod(HttpMethod.GET)
                .setListener(new HttpReq.HttpListener<String>() {
                    @Override
                    public void onError(HttpReq<String> request, HttpError error) {
                        applyDefaultConfig();
                        if (listener != null) {
                            listener.onError(request, error);
                        }
                    }
                    
                    @Override
                    public void onResponse(HttpReq<String> request, String data) {
                        applyDNSResult(data);
                        if (listener != null) {
                            listener.onResponse(request, data);
                        }
                    }
                }).reqSilent();
    }
    
    public void reqDNS(final HttpReq.HttpListener<String> listener) {
        if (!isDNSLogicValid)
            return;
        Logger.o(TAG, "begin reqDNS");
        
        StringReq req = new StringReq(getReqUrl(REQ_URL));
        req.setNeedShowNoNetError(false).setReqMethod(HttpMethod.GET)
                .setListener(new HttpReq.HttpListener<String>() {
                    @Override
                    public void onError(HttpReq<String> request, HttpError error) {
                        Logger.o(TAG, "reqDNS master url error,begin req slave");
                        reqDNSWithSlaveUrl(listener);
                    }
                    
                    @Override
                    public void onResponse(HttpReq<String> request, String data) {
                        applyDNSResult(data);
                        if (listener != null) {
                            listener.onResponse(request, data);
                        }
                    }
                }).req();
    }
    
    private String getReqUrl(String oriUrl) {
        String url = oriUrl;
        if (mIsIntranet) {
            url += "&test=2";
        }
        else {
            if ("QingQing-RC".equals(NetworkUtil.getWifiSSID())) {
                url += "&test=1";
            }
        }
        
        return url;
    }
    
    // private void reqDNSWithSlaveUrl(Response.Listener<String> listener,
    // Response.ErrorListener errorListener) {
    // Logger.o(TAG, "begin reqDNS with slave url");
    // SimpleStringReq req = new SimpleStringReq(getReqUrl(REQ_SLAVE_URL))
    // .abortHostHeader();
    // req.setNeedShowNoNetError(false).setReqMethod(ReqMethod.GET)
    // .setRspListener(listener != null ? listener : mRspListener)
    // .setErrorListener(errorListener != null ? errorListener : mErrListener)
    // .reqSlave();
    // }
    
    /**
     * 根据域名 找到对应的DomainList
     */
    private ArrayList<HostList> getHostList(String domain) {
        
        if (TextUtils.isEmpty(domain))
            return null;
        
        ArrayList<HostList> list = new ArrayList<>();
        
        for (Object o : mHostListMap.entrySet()) {
            HashMap.Entry entry = (HashMap.Entry) o;
            HostList dl = (HostList) entry.getValue();
            if (dl != null && domain.equals(dl.getDefaultHost())) {
                list.add(dl);
            }
        }
        
        return list;
    }
    
    /**
     * 清除所有的IP设置
     */
    public void clearAllIP() {
        SPManager.put(KEY_HOST_LIST, "");
        for (Object o : mHostListMap.entrySet()) {
            HashMap.Entry entry = (HashMap.Entry) o;
            HostList dl = (HostList) entry.getValue();
            dl.clearAllIP();
        }
    }
    
    /**
     * 解析dns返回结果
     */
    public void applyDNSResult(String str) {
        
        Logger.o(TAG, "apply DNS Result");
        try {
            JSONObject obj = new JSONObject(str);
            int ret = obj.getInt("status");
            // 清除已有的IP信息
            clearAllIP();
            Logger.o(TAG, "apply DNS Result   ret=" + ret);
            // 当ret == 1时，根据下发的IP列表，配置到相应的域名
            if (ret == 1) {
                int grayLevel = obj.getInt("rate");// 开关值 0~100
                int failRestoreInterval = obj.getInt("rtime");
                int ttl = obj.getInt("ttl");
                int failMaxCount = obj.getInt("tries");
                applyConfig(ttl, failRestoreInterval, grayLevel, failMaxCount);
                
                JSONArray dataArray = obj.getJSONArray("data");
                final int len = dataArray.length();
                for (int i = 0; i < len; i++) {
                    // 找到对应的域名，填充IP进去
                    JSONObject cfg = (JSONObject) dataArray.get(i);
                    String domain = cfg.getString("dn");
                    // TODO fortest HTTPS的域名不是使用IP
                    // if (isHttpsDomain(domain))
                    // continue;
                    
                    ArrayList<HostList> dl = getHostList(domain);
                    if (dl != null && !dl.isEmpty()) {
                        JSONArray IPList = cfg.getJSONArray("ip");
                        for (HostList l : dl) {
                            l.updateIPList(IPList);
                        }
                    }
                    else {
                        Logger.w(TAG, "HostList not found . dn=" + domain);
                    }
                }
                
                mLastDNSCheckTime = NetworkTime.currentTimeMillis();
                SPManager.put(KEY_LAST_CHECK_TIME, mLastDNSCheckTime);
                saveHostList(str);
                Logger.o(TAG, "apply DNS Result done");
            }
            else {
                Logger.w(TAG, "apply DNS Result failed ,ret error");
                applyDefaultConfig();
            }
        } catch (JSONException e) {
            Logger.w(TAG, "applyDNSResult exception. str=" + str, e);
            applyDefaultConfig();
        }
    }
    
    private void saveHostList(String str) {
        try {
            String des = TripleDesUtil.encode(str, 5);
            SPManager.put(KEY_HOST_LIST, des);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private boolean existLocalHostList() {
        String des = SPManager.getString(KEY_HOST_LIST, "");
        return !TextUtils.isEmpty(des);
    }
    
    private boolean readHostList() {
        String des = SPManager.getString(KEY_HOST_LIST, "");
        boolean ret = false;
        if (!TextUtils.isEmpty(des)) {
            try {
                String hostList = TripleDesUtil.decode(des, 5);
                applyDNSResult(hostList);
                ret = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return ret;
    }
    
    public class HostList {
        public int selIdx;
        public String domain;
        public ArrayList<String> ipList;
        
        public HostList(String defaultDomain) {
            ipList = new ArrayList<>();
            domain = defaultDomain;
            Pattern pattern = Pattern.compile("_\\w*");
            if (pattern.matcher(PackageUtil.getVersionName()).find()) {
                // since 6.1.0 测试环境中，支持多域名的切换
                if (HostManager.INSTANCE().isIntranet()) {
                    domain = domain.replace("-idc",
                            "-" + SPManager.getString(
                                    DefaultDataCache.SP_KEY_TEST_ENV_NAME,
                                    LogicConfig.TEST_DEFAULT_ENV));
                }
            }
            selIdx = -1;
        }
        
        public boolean hasIPList() {
            return !ipList.isEmpty();
        }
        
        public String getPrimaryIP() {
            if (ipList.isEmpty())
                return null;
            
            return ipList.get(0);
        }
        
        public int getIPIndex(String ip) {
            return ipList.indexOf(ip);
        }
        
        // 使用下一个IP
        public void useNextIP() {
            
            Logger.o(TAG, "begin use next ip , domain=" + domain);
            
            if (ipList.isEmpty())
                return;
            
            if (selIdx < 0) {
                selIdx = 0;
            }
            else if (selIdx >= (ipList.size() - 1)) {
                selIdx = -1;
            }
            else {
                ++selIdx;
            }
            
            Logger.o(TAG, "use next ip , idx=" + selIdx);
        }
        
        public boolean isUseDomain() {
            return selIdx == -1;
        }
        
        public void usePrimaryIP() {
            if (ipList.isEmpty())
                selIdx = -1;
            else
                selIdx = 0;
        }
        
        public String getDefaultHost() {
            return domain;
        }
        
        public String getHost() {
            if (mEnable) {
                if (selIdx < 0) {
                    return domain;
                }
                else {
                    int idx = Math.min(selIdx, ipList.size() - 1);
                    return ipList.get(idx);
                }
            }
            else {
                return domain;
            }
        }
        
        public void clearAllIP() {
            ipList.clear();
            selIdx = -1;
        }
        
        public void updateIPList(JSONArray array) {
            
            if (array == null)
                return;
            
            final int count = array.length();
            clearAllIP();
            if (count > 0) {
                for (int j = 0; j < count; j++) {
                    try {
                        ipList.add(array.getString(j));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                
                Logger.d(TAG, "updateIPList   array=" + array.toString() + "  enable ="
                        + mEnable);
                if (mEnable)
                    selIdx = 0;
                else
                    selIdx = -1;
            }
        }
    }
}

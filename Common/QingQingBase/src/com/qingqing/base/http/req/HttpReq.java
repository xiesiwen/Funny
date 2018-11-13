package com.qingqing.base.http.req;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import java.util.HashMap;

import com.qingqing.base.BaseApplication;
import com.qingqing.base.core.AccountManager;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.data.DefaultDataCache;
import com.qingqing.base.data.StatisticalDataConstants;
import com.qingqing.base.http.HttpCallback;
import com.qingqing.base.http.HttpManager;
import com.qingqing.base.http.HttpMethod;
import com.qingqing.base.http.HttpRequest;
import com.qingqing.base.http.HttpRequestBuilder;
import com.qingqing.base.http.HttpUrl;
import com.qingqing.base.http.error.HttpClientError;
import com.qingqing.base.http.error.HttpError;
import com.qingqing.base.http.error.HttpNetworkError;
import com.qingqing.base.http.error.HttpPermissionError;
import com.qingqing.base.http.error.HttpServerError;
import com.qingqing.base.http.error.HttpTimeOutError;
import com.qingqing.base.log.Logger;
import com.qingqing.base.utils.NetworkUtil;
import com.qingqing.base.utils.PermissionUtil;
import com.qingqing.qingqingbase.R;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

/**
 * Created by wangxiaxin on 2016/9/28.
 * 
 * 基于{@link HttpRequest} <br>
 * 封装了 loading提示<br>
 * 一些基本逻辑参数设置<br>
 * 网络日志的汇报<br>
 * 增加了返回数据的解析<br>
 */

public abstract class HttpReq<T> {
    
    public interface HttpListener<T> {
        void onError(HttpReq<T> request, HttpError error);
        
        void onResponse(HttpReq<T> request, T data);
    }
    
    protected HttpRequestBuilder requestBuilder;
    protected HttpUrl url;
    protected HttpRequest request;
    
    private boolean needCheckUserID = true;
    private boolean needShowLoadingIndication = false;
    private boolean needCheckSession = true;
    private boolean needShowNoNetError = true;
    
    public static final int TAG_NEED_SESSION = 0x1;
    public static final int TAG_NEED_TOKEN = 0x2;
    protected boolean mNeedToken = true;
    protected boolean mNeedSession = true;
    protected String mCurrentRandomID = "default";
    protected HttpListener<T> outListener;
    protected HttpError failedError;
    
    private static final String TAG = "HttpReq";
    
    public static final String ERROR_NO_NETWORK = BaseApplication.getCtx()
            .getString(R.string.base_req_no_network);
    public static final String ERROR_USER_INVALID = BaseApplication.getCtx()
            .getString(R.string.base_request_no_user_id);
    public static final String ERROR_NO_INTERNET_PERMISSION = BaseApplication.getCtx()
            .getString(R.string.base_request_no_internet_permission);
    public static final String ERROR_NETWORK_ERROR = BaseApplication.getCtx()
            .getString(R.string.base_req_network_error);
    
    private String networkStatus = StatisticalDataConstants.SUCCESS;
    private String loadingContent;
    private boolean loadingIndicationCancelable;
    private WeakReference<Context> mCtxRef;
    private boolean hasClient = false;
    private static StringBuffer urlLogString = new StringBuffer();
    
    private HttpCallback internalCallback = new HttpCallback() {
        @Override
        public void onError(HttpRequest request) {
            
            // error 鉴定
            // 汇报日志
            // loading 处理
            // 回调处理
            Exception failedException = request.failedException();
            boolean needReport = true;
            Logger.w(TAG, "[err: " + failedException.getClass().getName() + "  " + failedException.getMessage() + "] url = "
                    + request.url());

            //如果是个被取消的请求，则忽略不计
            if(failedException instanceof IOException && "Canceled".equals(failedException.getMessage())){
                outListener = null;
                return;
            }
            else if (failedException instanceof HttpPermissionError) {
                needReport = false;
                failedError = (HttpPermissionError) failedException;
            }
            else if (failedException instanceof SocketTimeoutException) {
                failedError = new HttpTimeOutError();
                networkStatus = StatisticalDataConstants.TIMEOUT;
            }
            else if(ERROR_NO_NETWORK.equals(failedException.getMessage())){
                //无网络
                failedError = new HttpNetworkError(ERROR_NO_NETWORK);
                networkStatus = StatisticalDataConstants.NETWORKERROR;
            }
            else {
                //网络不通
                // UnknownHostException
                failedError = new HttpNetworkError(ERROR_NETWORK_ERROR);
                networkStatus = StatisticalDataConstants.DNSERROR;
            }
            
            failedError.statusCode(request.statusCode());

            if (needReport)
                addNetworkLog(0);
            
            if (outListener != null) {
                outListener.onError(HttpReq.this, failedError);
            }
            outListener = null;
        }
        
        @Override
        public void onResponse(HttpRequest request) {
            
            final int statusCode = request.statusCode();
            if (request.isSuccess()) {
                
                if (!request.url()
                        .contains(StatisticalDataConstants.REQ_URL_USER_BEHAVIOR)
                        && !request.url().contains(
                                StatisticalDataConstants.REQ_URL_NETWORK_ERROR)) {
                    Logger.o(TAG, "[rsp:" + statusCode + "] url = " + request.url());
                }
                else {
                    Logger.v(TAG, "[rsp:" + statusCode + "] url = " + request.url());
                }
                
                if (outListener != null) {
                    T data = parseResponse(request.responseBody().binary());
                    outListener.onResponse(HttpReq.this, data);
                }
                networkStatus = StatisticalDataConstants.SUCCESS;
                if (needCheckSession && (BaseData.getUserSession() != HttpManager
                        .instance().session())) {
                    Logger.w(TAG,
                            "session conflict : user session=" + BaseData.getUserSession()
                                    + "  req session="
                                    + HttpManager.instance().session());
                    AccountManager.INSTANCE().logout(true);
                }
            }
            else {
                if (!request.url()
                        .contains(StatisticalDataConstants.REQ_URL_USER_BEHAVIOR)
                        && !request.url().contains(
                                StatisticalDataConstants.REQ_URL_NETWORK_ERROR)) {
                    Logger.w(TAG, "[rsp:" + statusCode + "] url = " + request.url());
                }
                else {
                    Logger.v(TAG, "[rsp:" + statusCode + "] url = " + request.url());
                }
                
                failedError = new HttpClientError();
                networkStatus = StatisticalDataConstants.CLINETERROR;
                if (statusCode >= 500 && statusCode < 600) {
                    failedError = new HttpServerError();
                    networkStatus = StatisticalDataConstants.SERVERERROR;
                }
                
                if (outListener != null) {
                    outListener.onError(HttpReq.this, failedError);
                }
            }
            
            addNetworkLog(statusCode);
            outListener = null;
        }
    };
    
    HttpReq(HttpUrl url) {
        this.url = url;
        requestBuilder = new HttpRequestBuilder().url(url);
        setReqMethod(HttpMethod.POST);
    }
    
    HttpReq(String url) {
        requestBuilder = new HttpRequestBuilder().url(url);
        setReqMethod(HttpMethod.POST);
    }
    
    public HttpReq<T> setUrlParam(String key, String value) {
        requestBuilder.urlParam(key, value);
        return this;
    }
    
    public HttpReq<T> setUrlParam(HashMap<String, String> urlParam) {
        requestBuilder.urlParam(urlParam);
        return this;
    }
    
    public HttpReq<T> setUrlData(String data) {
        requestBuilder.urlData(data);
        return this;
    }
    
    public HttpReq<T> setHeader(String key, String value) {
        requestBuilder.header(key, value);
        return this;
    }
    
    public HttpReq<T> addHeader(String key, String value) {
        requestBuilder.addHeader(key, value);
        return this;
    }
    
    public HttpReq<T> setClient(int client) {
        requestBuilder.client(client);
        hasClient = true;
        return this;
    }
    
    /**
     * 设置 是否检测user id
     */
    public HttpReq<T> setNeedCheckUserID(boolean need) {
        needCheckUserID = need;
        if (!needCheckUserID)
            needCheckSession = false;
        return this;
    }
    
    /**
     * 设置 是否需要检测session，用于多终端登录时，保证只有一个终端登录
     */
    public HttpReq<T> setNeedCheckSession(boolean need) {
        needCheckSession = need;
        return this;
    }
    
    /**
     * 设置 是否需要显示网络错误的提示框
     */
    public HttpReq<T> setNeedShowNoNetError(boolean need) {
        needShowNoNetError = need;
        return this;
    }
    
    /**
     * 设置请求方法 {@link HttpMethod}
     */
    public HttpReq<T> setReqMethod(int method) {
        requestBuilder.method(method);
        return this;
    }
    
    public HttpReq<T> setShouldCache(boolean flag) {
        if (!flag) {
            requestBuilder.noCache();
        }
        return this;
    }
    
    public HttpReq<T> setNeedCheckCache() {
        requestBuilder.checkCache();
        return this;
    }
    
    /**
     * 根据url获取是否需要添加token或者session<br>
     * /pb/*, 不传token， 不传sessionid<br>
     * /pt/*, 传token，传sessionid<br>
     * /pts/*,传token，传sessionid<br>
     * /pi/*, 内部接口，不允许客户端访问<br>
     */
    public static int getDefaultConfig(String url) {
        int ret = 0;
        if (TextUtils.isEmpty(url))
            return ret;
        
        if (url.contains("/pt/")) {
            ret |= TAG_NEED_SESSION;
            ret |= TAG_NEED_TOKEN;
        }
        return ret;
    }
    
    public HttpReq<T> setReqTag(Object reqTag) {
        requestBuilder.tag(reqTag);
        return this;
    }
    
    public HttpReq<T> setLoadingIndication(Context ctx) {
        return setLoadingIndication(ctx, true);
    }
    
    public HttpReq<T> setLoadingIndication(Context ctx, boolean cancelable) {
        return setLoadingIndication(ctx, "正在加载……", cancelable);
    }
    
    public HttpReq<T> setLoadingIndication(Context ctx, String content,
            boolean cancelable) {
        needShowLoadingIndication = true;
        loadingContent = content;
        mCtxRef = new WeakReference<>(ctx);
        loadingIndicationCancelable = cancelable;
        return this;
    }
    
    public HttpReq<T> setListener(HttpListener<T> callback) {
        outListener = callback;
        return this;
    }
    
    public HttpReq<T> setRspListener(HttpListener<T> callback) {
        return setListener(callback);
    }
    
    protected abstract T parseResponse(byte[] rawBody);
    
    public int getStatusCode() {
        return request.statusCode();
    }
    
    public void reqSilent() {
        needShowLoadingIndication = false;
        req();
    }
    
    public void req() {
        
        if (!hasClient) {
            setClient(HttpManager.HttpClient.LOGIC);
        }
        request = requestBuilder.build();
        request.startTime(System.nanoTime());
        if (!PermissionUtil.hasInternetPermission()) {
            request.setFailException(
                    new HttpPermissionError(ERROR_NO_INTERNET_PERMISSION));
            internalCallback.onError(request);
            return;
        }
        
        if (needShowNoNetError && !NetworkUtil.isNetworkAvailable()) {
            request.setFailException(new HttpNetworkError(ERROR_NO_NETWORK));
            internalCallback.onError(request);
            return;
        }
        
        if (needCheckUserID && !BaseData.isUserIDValid()) {
            request.setFailException(new HttpPermissionError(ERROR_USER_INVALID));
            internalCallback.onError(request);
            return;
        }
        

        if (!request.url().contains(StatisticalDataConstants.REQ_URL_USER_BEHAVIOR)
                && !request.url()
                        .contains(StatisticalDataConstants.REQ_URL_NETWORK_ERROR)) {
            urlLogString.setLength(0);
            urlLogString.append("[req] url = ").append(request.url());
            if (needCheckUserID) {
                urlLogString.append("   token=").append(BaseData.getUserToken());
            }
            if (needCheckSession) {
                urlLogString.append(" si=").append(HttpManager.instance().session());
            }
            Logger.o(TAG, urlLogString.toString());
        }
        
        request.async(internalCallback);
    }
    
    private void addNetworkLog(int statusCode) {
        
        if (!DefaultDataCache.INSTANCE().needUploadNetworkLog()) {
            Logger.d(TAG, "----no need add network log.");
            return;
        }

        try {
            Uri uri = Uri.parse(request.url());
            String path = uri.getPath();
            if (path.contains(StatisticalDataConstants.REQ_URL_USER_BEHAVIOR)
                    || path.contains(StatisticalDataConstants.REQ_URL_NETWORK_ERROR))
                return;

            long time = (System.nanoTime() - request.startTime()) / 1000_000;
            Logger.d(TAG,
                    "[req time cost: t1=" + time + ",t2=" + request.costTimeAtMillis()
                            + "]  random=" + mCurrentRandomID + "  url=  "
                            + request.url());

            if ((time - request.costTimeAtMillis()) >= (request.retryCount()
                    * request.connectTimeoutSeconds() + 1) * 1000) {
                // 请求时间 大于最大时间，一定有问题
                Logger.e(TAG,
                        "**[req time cost: t1=" + time + ",t2="
                                + request.costTimeAtMillis() + "]  random="
                                + mCurrentRandomID + "  url=  " + request.url());
            }
            else if ((time - request.costTimeAtMillis()) > 600) {
                // 两个时间差 相差600ms以上，说明队列中的消耗时间较大，需要记录下
                Logger.w(TAG,
                        "**[req time cost: t1=" + time + ",t2="
                                + request.costTimeAtMillis() + "]  random="
                                + mCurrentRandomID + "  url=  " + request.url());
            }
        } catch (Exception e) {
            Logger.w("addNetworkLog exception.", e);
        }
    }
}

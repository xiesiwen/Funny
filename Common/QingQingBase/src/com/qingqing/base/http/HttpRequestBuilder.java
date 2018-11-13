package com.qingqing.base.http;

import android.text.TextUtils;

import com.qingqing.base.data.DefaultDataCache;
import com.qingqing.base.dns.HostManager;
import com.qingqing.base.utils.UrlUtil;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.CacheControl;

/**
 * Created by wangxiaxin on 2017/5/12.
 *
 * 用于构建HttpRequest
 * 
 */
public class HttpRequestBuilder {
    
    private static final String DEFAULT_CHARSET = "UTF-8";
    protected int method = HttpMethod.GET;
    protected int client;
    
    protected HttpRequestBody requestBody;

    protected int retryCount = DefaultDataCache.INSTANCE().getHttpRequestRetryCount();
    protected HashMap<String, String> urlParamMap;
    protected String urlData;
    protected String urlString;
    protected String urlHost;
    protected int connectTimeoutSeconds = DefaultDataCache.INSTANCE()
            .getHttpRequestTimeoutSeconds();
    final static int DEFAULT_READ_TIMEOUT = 90;
    final static int DEFAULT_WRITE_TIMEOUT = 90;
    protected int readTimeoutSeconds = DEFAULT_READ_TIMEOUT;
    protected int writeTimeoutSeconds = DEFAULT_WRITE_TIMEOUT;
    private boolean isResponseStreamMode;//是否需要response为stream模式

    protected okhttp3.Request.Builder okBuilder;
    
    public HttpRequestBuilder() {
        okBuilder = new okhttp3.Request.Builder();
        urlParamMap = new HashMap<>();
    }
    
    public HttpRequestBuilder client(int client) {
        this.client = client;
        return this;
    }
    
    public HttpRequestBuilder method(int method) {
        this.method = method;
        return this;
    }
    
    public HttpRequestBuilder url(String url) {
        urlString = url;
        return this;
    }
    
    public HttpRequestBuilder url(HttpUrl url) {
        urlString = url.url();
        return this;
    }
    
    public HttpRequestBuilder header(String key, String value) {
        okBuilder.header(key, value);
        return this;
    }
    
    public HttpRequestBuilder addHeader(String key, String value) {
        okBuilder.addHeader(key, value);
        return this;
    }
    
    public HttpRequestBuilder removeHeader(String key) {
        okBuilder.removeHeader(key);
        return this;
    }
    
    public HttpRequestBuilder urlParam(String key, String value) {
        urlParamMap.put(key, value);
        return this;
    }
    
    public HttpRequestBuilder urlParam(HashMap<String, String> paramMap) {
        urlParamMap.putAll(paramMap);
        return this;
    }
    
    public HttpRequestBuilder removeUrlParam(String key) {
        urlParamMap.remove(key);
        return this;
    }
    
    public HttpRequestBuilder urlData(String data) {
        urlData = data;
        return this;
    }
    
    public HttpRequestBuilder tag(Object tag) {
        okBuilder.tag(tag);
        return this;
    }

    public boolean isResponseStreamMode() {
        return isResponseStreamMode;
    }

    public HttpRequestBuilder setResponseStreamMode(boolean responseStreamMode) {
        isResponseStreamMode = responseStreamMode;
        return this;
    }

    /**
     * 设置 二进制的请求内容
     *
     */
    public HttpRequestBuilder requestBody(HttpRequestBody reqBody) {
        requestBody = reqBody;
        return this;
    }
    
    public HttpRequestBuilder retryCount(int count) {
        this.retryCount = count;
        return this;
    }
    
    public HttpRequestBuilder connectTimeout(int seconds) {
        connectTimeoutSeconds = seconds;
        return this;
    }
    
    public HttpRequestBuilder readTimeout(int seconds) {
        readTimeoutSeconds = seconds;
        return this;
    }
    
    public HttpRequestBuilder writeTimeout(int seconds) {
        writeTimeoutSeconds = seconds;
        return this;
    }
    
    public HttpRequestBuilder noCache() {
        okBuilder.cacheControl(CacheControl.FORCE_NETWORK);
        return this;
    }
    
    public HttpRequestBuilder checkCache() {
        okBuilder.cacheControl(
                new CacheControl.Builder().maxAge(0, TimeUnit.SECONDS).build());
        return this;
    }
    
    public HttpRequest build() {
        
        dealUrlStringWithHostManager();
        // 更新url，将url Param 设置进去
        if (!urlParamMap.isEmpty()) {
            urlString = UrlUtil.addParamToUrl(urlString, urlParamMap);
        }
        else if (!TextUtils.isEmpty(urlData)) {
            urlString += "?" + urlData;
        }
        okBuilder.url(urlString).header("User-Agent", UAHelper.dataUA());
        setConnectionParametersForRequest(okBuilder, method);
        return new HttpRequest(this);
    }
    
    private void dealUrlStringWithHostManager() {
        urlHost = UrlUtil.getHost(urlString);
        String curHost = HostManager.INSTANCE().currentHost(urlHost);
        urlString = UrlUtil.setHost(urlString, curHost);
    }
    
    private void setConnectionParametersForRequest(okhttp3.Request.Builder builder,int method) {
        switch (method) {
            case HttpMethod.GET:
                builder.get();
                break;
            case HttpMethod.DELETE:
                builder.delete();
                break;
            case HttpMethod.POST:
                builder.post(HttpRequestBody.generate(requestBody));
                break;
            case HttpMethod.PUT:
                builder.put(HttpRequestBody.generate(requestBody));
                break;
            case HttpMethod.HEAD:
                builder.head();
                break;
            case HttpMethod.OPTIONS:
                builder.method("OPTIONS", null);
                break;
            case HttpMethod.TRACE:
                builder.method("TRACE", null);
                break;
            case HttpMethod.PATCH:
                builder.patch(HttpRequestBody.generate(requestBody));
                break;
            default:
                throw new IllegalStateException("Unknown method type.");
        }
    }
}

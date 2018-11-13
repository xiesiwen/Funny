package com.qingqing.base.http;

import com.qingqing.base.data.DefaultDataCache;
import com.qingqing.base.dns.DNSManager;
import com.qingqing.base.dns.HostManager;
import com.qingqing.base.log.Logger;
import com.qingqing.base.utils.AppUtil;
import com.qingqing.base.utils.ExecUtil;
import com.qingqing.base.utils.UrlUtil;
import com.qingqing.base.view.ToastWrapper;
import com.qingqing.qingqingbase.BuildConfig;
import com.qingqing.qingqingbase.R;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Response;

/**
 * Created by wangxiaxin on 2016/9/7.
 * 
 * http 请求的封装类
 *
 * 还需要对超时重试 做处理
 */
public class HttpRequest {
    
    private static final String TAG = "Http.Request";
    
    private okhttp3.Request okRequest;
    private okhttp3.Call okCall;
    private okhttp3.Response okResponse;
    private int client;
    private String urlHost;
    private HttpCallback callback;
    private Exception failException;
    /** 请求是否发生在ui线程，如果是，回调会在ui线程中处理 ，仅对异步请求有效 */
    
    private boolean requestInUIThread;
    private final int retryCount;// 重试次数
    private int currentRetryCount = 0;// 当前的重试次数
    private boolean isAsync;// 是否是异步请求
    private long startTime;
    private int connectTimeoutSeconds;
    private int readTimeoutSeconds;
    private int writeTimeoutSeconds;
    private boolean isTryingBackupHost;
    private HttpResponseBody responseBody;
    private boolean responseStream;

    private HttpCallback internalCallback = new HttpCallback() {
        
        private void retry() {
            if (isAsync) {
                if (requestInUIThread) {
                    ExecUtil.executeUI(new Runnable() {
                        @Override
                        public void run() {
                            async(callback);
                        }
                    });
                }
                else {
                    async(callback);
                }
            }
            else {
                if (requestInUIThread) {
                    ExecUtil.executeUI(new Runnable() {
                        @Override
                        public void run() {
                            sync(callback);
                        }
                    });
                }
                else {
                    sync(callback);
                }
            }
        }
        
        @Override
        public void onError(final HttpRequest request) {
            
            // 此时的 failException 必不为空
            if (!okRequest.url().toString().contains("blog.html"))
                Logger.w(TAG, "url = " + okRequest.url().toString() + "  failed! ",
                        failException);
            
            if (failException instanceof SocketTimeoutException) {
                if (currentRetryCount < retryCount) {
                    ++currentRetryCount;
                    Logger.w(TAG, "begin to retry----" + currentRetryCount + "/"
                            + retryCount + "[" + okRequest.url().toString() + "]");
                    retry();
                    return;
                }
                else {
                    // 超时处理
                    Logger.w(TAG, "TimeoutError  url=" + okRequest.url().toString());
                    DNSManager.INSTANCE().notifyHostTimeout(okRequest.url().host());
                    
                    // 如果可以切换到备用域名，则切换到备用域名再尝试
                    if (HostManager.INSTANCE().hasHostList(urlHost)) {
                        String curHost = HostManager.INSTANCE().currentHost(urlHost);
                        boolean needRetry = false;
                        if (!curHost.equals(okRequest.url().host())) {
                            needRetry = true;
                        }
                        else if (!HostManager.INSTANCE().isUsingLastHost(urlHost)) {
                            // HostManager.INSTANCE().useNextHost(urlHost);
                            HostManager.INSTANCE().allUseBackupHost();
                            curHost = HostManager.INSTANCE().currentHost(urlHost);
                            needRetry = true;
                        }
                        if (needRetry) {
                            String urlString = UrlUtil
                                    .setHost(okRequest.url().toString(), curHost);
                            okRequest = okRequest.newBuilder().url(urlString).build();
                            currentRetryCount = 0;
                            isTryingBackupHost = true;
                            retry();
                            return;
                        }
                        else {
                            isTryingBackupHost = false;
                        }
                    }
                }
            }
            else {
                DNSManager.INSTANCE().notifyHostOK(okRequest.url().host());
            }
            
            if (callback != null) {
                if (requestInUIThread) {
                    ExecUtil.executeUI(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError(request);
                            callback = null;
                        }
                    });
                }
                else {
                    callback.onError(request);
                    callback = null;
                }
            }
        }
        
        @Override
        public void onResponse(final HttpRequest request) {
            DNSManager.INSTANCE().notifyHostOK(okRequest.url().host());
            
            if (statusCode() == 503) {
                ToastWrapper.show(R.string.base_http_error_503);
            }
            
            if (hasResponse()) {
                if (BuildConfig.DEBUG) {
                    String string = "[unknown rsp] ";
                    if (okResponse.networkResponse() != null) {
                        string = "[net rsp] ";
                    }
                    else if (okResponse.cacheResponse() != null) {
                        string = "[cache rsp] ";
                    }
                    else if (okResponse.priorResponse() != null) {
                        string = "[prior rsp] ";
                    }
                    Logger.d(TAG, string + " [url]=" + okRequest.url());
                }
                responseBody = new HttpResponseBody(okResponse.body());
                if(!responseStream){
                    //直接在线程中获取结果
                    Logger.d(TAG,"prepareByteBody  " + okRequest.url().toString());
                    try{
                        final long contentLength = responseBody.contentLength();
                        if(contentLength < 1024*1024){
                            responseBody.prepareByteBody();
                    }
                    }catch (Exception e){
                        Logger.w(TAG,"prepareByteBody",e);
                    }
                }
            }
            
            if (callback != null) {
                if (requestInUIThread) {
                    ExecUtil.executeUI(new Runnable() {
                        @Override
                        public void run() {
                            callback.onResponse(request);
                            callback = null;
                        }
                    });
                }
                else {
                    callback.onResponse(request);
                    callback = null;
                }
            }
        }
    };
    
    protected HttpRequest(HttpRequestBuilder builder) {
        okRequest = builder.okBuilder.build();
        client = builder.client;
        urlHost = builder.urlHost;
        retryCount = builder.retryCount;
        connectTimeoutSeconds = builder.connectTimeoutSeconds;
        readTimeoutSeconds = builder.readTimeoutSeconds;
        writeTimeoutSeconds = builder.writeTimeoutSeconds;
        responseStream = builder.isResponseStreamMode();
    }
    
    private void prepareOkCall(HttpCallback callback) {
        OkHttpClient okClient = HttpManager.instance().client(client);
        if (connectTimeoutSeconds != DefaultDataCache.INSTANCE()
                .getHttpRequestTimeoutSeconds()
                || readTimeoutSeconds != HttpRequestBuilder.DEFAULT_READ_TIMEOUT
                || writeTimeoutSeconds != HttpRequestBuilder.DEFAULT_WRITE_TIMEOUT) {
            okClient = okClient.newBuilder()
                    .connectTimeout(connectTimeoutSeconds, TimeUnit.SECONDS)
                    .readTimeout(readTimeoutSeconds, TimeUnit.SECONDS)
                    .writeTimeout(writeTimeoutSeconds, TimeUnit.SECONDS).build();
        }
        
        okCall = okClient.newCall(okRequest);
        
        if (currentRetryCount == 0 && !isTryingBackupHost)
            startTime(System.nanoTime());
        this.callback = callback;
    }
    
    /**
     * 发起同步请求
     *
     * @param callback
     *            请求的回调
     */
    public void sync(HttpCallback callback) {
        isAsync = false;
        prepareOkCall(callback);
        try {
            okResponse = okCall.execute();
            internalCallback.onResponse(this);
        } catch (IOException e) {
            failException = e;
            internalCallback.onError(this);
        }
    }

    public void async(HttpCallback callback){
        async(AppUtil.isMainThread(),callback);
    }

    /**
     * 发起异步请求
     *
     * @param callback
     *            请求的回调
     */
    public void async(boolean callbackInUI,HttpCallback callback) {
        isAsync = true;
        prepareOkCall(callback);
        requestInUIThread = callbackInUI;
        okhttp3.Callback okCallback = new okhttp3.Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                failException = e;
                internalCallback.onError(HttpRequest.this);
            }
            
            @Override
            public void onResponse(final Call call, final Response response)
                    throws IOException {
                okResponse = response;
                internalCallback.onResponse(HttpRequest.this);
            }
        };
        
        okCall.enqueue(okCallback);
    }
    
    public void cancel() {
        if (okCall != null && !okCall.isCanceled()) {
            okCall.cancel();
        }
    }
    
    public void setFailException(Exception e) {
        failException = e;
    }
    
    public Exception failedException() {
        return failException;
    }
    
    public boolean isCanceled() {
        return okCall != null && okCall.isCanceled();
    }
    
    /** 2xx 的情况，都属于成功 */
    public boolean isSuccess() {
        return isExecuted() && okResponse.isSuccessful();
    }
    
    public boolean isExecuted() {
        return okCall != null && okCall.isExecuted();
    }
    
    public boolean hasResponse() {
        return isExecuted() && okResponse != null;
    }
    
    public String url() {
        return okRequest.url().toString();
    }

    public Object tag(){
        return okRequest.tag();
    }
    
    public boolean responseOk() {
        return hasResponse() && okResponse.isSuccessful();
    }
    
    public int statusCode() {
        return hasResponse() ? okResponse.code() : -1;
    }

    public void closeResponse(){
        if(hasResponse()){
            okResponse.close();
        }
    }
    
    public long startTime() {
        return startTime;
    }
    
    public void startTime(long startTime) {
        this.startTime = startTime;
    }
    
    public int connectTimeoutSeconds() {
        return connectTimeoutSeconds;
    }
    
    public int retryCount() {
        return retryCount;
    }
    
    public HttpResponseBody responseBody() {
        return responseBody;
    }

    /*** 请求的耗时 */
    public long costTimeAtMillis() {
        if (hasResponse()) {
            return okResponse.receivedResponseAtMillis()
                    - okResponse.sentRequestAtMillis();
        }
        else {
            return 0;
        }
    }
    
    public boolean isFailed() {
        return isExecuted() && failException != null;
    }
}

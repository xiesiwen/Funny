package com.qingqing.base.http;

import com.qingqing.base.cache.DiskCacheManager;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.log.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Dispatcher;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by wangxiaxin on 2016/9/6.
 * 
 * http请求管理类，替代之前的RequestManager
 */
public class HttpManager {
    
    public interface HttpClient {
        int LOGIC = 1;
        int BIP = 2;
        int UD = 3;
    }
    
    private static final String TAG = "HttpManager";
    private volatile long mResponseSession;
    private OkHttpClient mLogicClient;// 逻辑请求
    private OkHttpClient mBIPClient;// 统计请求
    private OkHttpClient mUploadOrDownloadClient;// 上传或者下载请求
    
    private ConcurrentHashMap<String, String> mRouteIpAddressMap = new ConcurrentHashMap<>();
    
    private final class RouteIPConnectInterceptor implements Interceptor {
        
        @Override
        public Response intercept(Chain chain) throws IOException {
            okhttp3.Request request = chain.request();
            okhttp3.Response response = chain.proceed(request);
            InetAddress address = chain.connection().socket().getInetAddress();
            if (address != null) {
                Logger.v("RouteIPConnect",
                        address.getHostName() + " -- " + address.getHostAddress());
                mRouteIpAddressMap.put(address.getHostName(), address.getHostAddress());
            }
            return response;
        }
    }
    
    private static class LoggingInterceptor implements Interceptor {
        
        @Override
        public Response intercept(Chain chain) throws IOException {
            
            long t1 = System.nanoTime();
            Request request = chain.request();
            
            Logger.v(TAG, String.format("Sending request %s on %s%n%s", request.url(),
                    chain.connection(), request.headers()));
            
            Response response = chain.proceed(request);
            
            long t2 = System.nanoTime();
            Logger.v(TAG,
                    String.format(Locale.CHINA, "Received response for %s in %.1fms%n%s",
                            request.url(), (t2 - t1) / 1e6d, response.headers()));
            
            return response;
        }
    }
    
    private static HttpManager sInstance;
    
    public static HttpManager instance() {
        if (sInstance == null) {
            synchronized (HttpManager.class) {
                if (sInstance == null) {
                    sInstance = new HttpManager();
                }
            }
        }
        return sInstance;
    }
    
    private HttpManager() {
        initOkHttpClient();
        mResponseSession = BaseData.getUserSession();
    }
    
    private void initOkHttpClient() {
        
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .addNetworkInterceptor(new RouteIPConnectInterceptor())
                /*.addInterceptor(new LoggingInterceptor())*/;
        
        okhttp3.Cache logicCache = new okhttp3.Cache(
                DiskCacheManager.instance().dirNetData(),
                DiskCacheManager.instance().maxSize(DiskCacheManager.TYPE_NETWORK_DATA));
        mLogicClient = builder.cache(logicCache).build();
        
        mBIPClient = new OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS).writeTimeout(60, TimeUnit.SECONDS)
                .build();
        
        final long uploadOrDownloadTimeout = 45;
        mUploadOrDownloadClient = new OkHttpClient.Builder()
                .connectTimeout(uploadOrDownloadTimeout, TimeUnit.SECONDS)
                .readTimeout(uploadOrDownloadTimeout, TimeUnit.SECONDS)
                .writeTimeout(uploadOrDownloadTimeout, TimeUnit.SECONDS).build();
    }
    
    public void clearCache() {
        try{
            mLogicClient.cache().evictAll();
        }catch (Exception e){
            Logger.w(TAG,"clear cache ",e);
        }
    }
    
    /** 根据host 返回 当前使用的 ip地址，如未找到，则为host自身 */
    public String getIPAddressByHost(String hostName) {
        if (mRouteIpAddressMap.containsKey(hostName)) {
            return mRouteIpAddressMap.get(hostName);
        }
        else {
            return hostName;
        }
    }
    
    /** 设置实时的session */
    public void session(long session) {
        Logger.o(TAG, "syncSession " + session);
        mResponseSession = session;
    }
    
    /** 获取实时的session */
    public long session() {
        return mResponseSession;
    }
    
    public OkHttpClient client(int client) {
        switch (client) {
            case HttpClient.LOGIC:
            default:
                return mLogicClient;
            case HttpClient.BIP:
                return mBIPClient;
            case HttpClient.UD:
                return mUploadOrDownloadClient;
        }
    }
    
    /**
     * 取消某个tag的网络请求
     * 
     * @param client
     *            指定的client类型{@link HttpManager.HttpClient}
     * @param tag
     *            特定的tag
     * 
     */
    public void cancelRequest(int client, Object tag) {
        OkHttpClient okClient = client(client);
        if (okClient != null) {
            Dispatcher dispatcher = okClient.dispatcher();
            if (tag == null) {
                dispatcher.cancelAll();
            }
            else {
                List<okhttp3.Call> callList = dispatcher.queuedCalls();
                for (okhttp3.Call call : callList) {
                    if (tag.equals(call.request().tag())) {
                        call.cancel();
                    }
                }
                
                callList = dispatcher.runningCalls();
                for (okhttp3.Call call : callList) {
                    if (tag.equals(call.request().tag())) {
                        call.cancel();
                    }
                }
            }
        }
    }

    public void cancelLogicRequest(Object tag) {
        cancelRequest(HttpClient.LOGIC, tag);
    }

    /**
     * 取消某个tag的网络请求
     *
     * @param client
     *            指定的client类型{@link HttpManager.HttpClient}
     * @param tag
     *            特定的tag
     *
     */
    public boolean isRequestRunning(int client, Object tag) {
        OkHttpClient okClient = client(client);
        if (okClient != null) {
            Dispatcher dispatcher = okClient.dispatcher();
            if (tag != null){
                List<okhttp3.Call> callList = dispatcher.queuedCalls();
                for (okhttp3.Call call : callList) {
                    if (tag.equals(call.request().tag())) {
                        return true;
                    }
                }

                callList = dispatcher.runningCalls();
                for (okhttp3.Call call : callList) {
                    if (tag.equals(call.request().tag())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean isRequestRunning(Object tag) {
        return isRequestRunning(HttpClient.LOGIC, tag);
    }

}

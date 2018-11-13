package com.qingqing.base.http;

/**
 * Created by wangxiaxin on 2017/4/25.
 *
 * HttpRequest 接口定义
 */
public interface IHttpRequest {
    
    /**
     * 发起同步请求
     *
     * @param callback
     *            请求的回调
     */
    void sync(HttpCallback callback);
    
    /**
     * 发起异步请求
     *
     * @param callback
     *            请求的回调
     */
    void async(HttpCallback callback);
    
    /**
     * 取消请求
     */
    void cancel();
    
    /**
     * 请求的耗时
     * 
     */
    long costTimeAtMillis();
}

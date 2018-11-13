package com.qingqing.base.http;

/**
 * Created by wangxiaxin on 2016/9/9.
 *
 * 发起{@link HttpRequest} 的回调定义
 */
public interface HttpCallback {
    
    void onError(final HttpRequest request);
    
    void onResponse(final HttpRequest request);
}

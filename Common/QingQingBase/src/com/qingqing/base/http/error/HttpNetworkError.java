package com.qingqing.base.http.error;

/**
 * Created by wangxiaxin on 2016/9/28.
 * 
 * 网络错误，包括无网络
 */

public class HttpNetworkError extends HttpError {
    
    public HttpNetworkError() {
        super();
    }
    
    public HttpNetworkError(String detail) {
        super(detail);
    }
}

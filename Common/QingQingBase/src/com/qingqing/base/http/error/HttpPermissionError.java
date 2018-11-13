package com.qingqing.base.http.error;

/**
 * Created by wangxiaxin on 2016/9/28.
 * 
 * 无权限的情况
 */

public class HttpPermissionError extends HttpError {
    
    public HttpPermissionError(String detail) {
        super(detail);
    }
}

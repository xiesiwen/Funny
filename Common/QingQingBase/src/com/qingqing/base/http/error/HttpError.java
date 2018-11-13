package com.qingqing.base.http.error;

/**
 * Created by wangxiaxin on 2016/9/29.
 */

public class HttpError extends Exception {

    private int statusCode = 0;
    
    public HttpError() {
        super();
    }
    
    public HttpError(String detail) {
        super(detail);
    }
    
    public HttpError(Throwable throwable) {
        super(throwable);
    }

    public HttpError statusCode(int statusCode){
        this.statusCode = statusCode;
        return  this;
    }

    public int statusCode(){
        return statusCode;
    }
    
}

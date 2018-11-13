package com.qingqing.base.http.req;

import android.text.TextUtils;

import com.qingqing.base.http.HttpRequestBody;
import com.qingqing.base.http.HttpUrl;

/**
 * Created by wangxiaxin on 2016/9/29.
 * 
 * 简单的string处理
 */

public class StringReq extends HttpReq<String> {
    
    public StringReq(HttpUrl url) {
        super(url);
    }
    
    public StringReq(String url) {
        super(url);
    }
    
    public StringReq setBody(String body) {
        if (!TextUtils.isEmpty(body))
            requestBuilder.requestBody(HttpRequestBody.fromString(body));
        return this;
    }
    
    @Override
    protected String parseResponse(byte[] rawBody) {
        if (rawBody == null) {
            return "";
        }
        else
            return new String(rawBody);
    }
    
    @Override
    public void req() {
        setNeedCheckUserID(false).setNeedCheckSession(false);
        super.req();
    }
}

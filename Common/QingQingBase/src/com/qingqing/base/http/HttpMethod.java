package com.qingqing.base.http;

/**
 * Created by wangxiaxin on 2016/9/7.
 *
 * http 请求方法
 */
public interface HttpMethod {
    int GET = 0;
    int POST = 1;
    int PUT = 2;
    int DELETE = 3;
    int HEAD = 4;
    int OPTIONS = 5;
    int TRACE = 6;
    int PATCH = 7;
}
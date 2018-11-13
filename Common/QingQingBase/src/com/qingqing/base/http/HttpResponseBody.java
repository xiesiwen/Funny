package com.qingqing.base.http;

import com.qingqing.base.log.Logger;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.ResponseBody;

/**
 * Created by wangxiaxin on 2017/12/27.
 *
 * Http Response body content
 */
public class HttpResponseBody {

    private static final String TAG = "HttpResponseBody";

    private ResponseBody responseBody;
    private byte[] byteBody;

    HttpResponseBody(ResponseBody okResponseBody){
        responseBody = okResponseBody;
    }

    void prepareByteBody(){
        try {
            byteBody= responseBody.bytes();
        } catch (IOException e) {
            Logger.w(TAG,"binary",e);
            byteBody = new byte[0];
        }
    }

    public byte[] binary(){
       return byteBody;
    }

    public String string(){
        return new String(byteBody);
    }

    public InputStream stream(){
        return responseBody.byteStream();
    }

    public long contentLength(){
        return responseBody.contentLength();
    }
}

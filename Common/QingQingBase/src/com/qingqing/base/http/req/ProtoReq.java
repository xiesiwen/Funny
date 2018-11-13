package com.qingqing.base.http.req;

import android.text.TextUtils;

import com.google.protobuf.nano.MessageNano;
import com.qingqing.base.BaseApplication;
import com.qingqing.base.core.CountDownCenter;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.http.HttpManager;
import com.qingqing.base.http.HttpRequestBody;
import com.qingqing.base.http.HttpUrl;
import com.qingqing.base.log.Logger;
import com.qingqing.base.utils.AppUtil;
import com.qingqing.base.utils.MD5Util;
import com.qingqing.base.utils.PackageUtil;
import com.qingqing.base.view.ToastWrapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by wangxiaxin on 2016/9/29.
 * 
 * proto buffer 格式的 请求
 */

public class ProtoReq extends HttpReq<byte[]> {
    
    private MessageNano mSendMsg;
    private String mSign;
    
    public ProtoReq(HttpUrl url) {
        super(url);
    }
    
    public ProtoReq setSendMsg(MessageNano sendMsg) {
        mSendMsg = sendMsg;
        requestBuilder.requestBody(HttpRequestBody
                .fromByte(MessageNano.toByteArray(mSendMsg), "application/x-protobuf"));
        return this;
    }
    
    @Override
    protected byte[] parseResponse(byte[] rawBody) {
        return rawBody;
    }
    
    public ProtoReq setSign(String sign) {
        this.mSign = sign;
        return this;
    }
    
    @Override
    public void req() {
        
        String urlString = this.url.url();
        if (requestMap != null) {
            logRequest(urlString);
        }
        
        int defaultCfg = getDefaultConfig(urlString);
        if (defaultCfg <= 0) {
            setNeedCheckUserID(false).setNeedCheckSession(false);
        }
        
        // 统一处理header部分
        if (mNeedToken && ((defaultCfg & TAG_NEED_TOKEN) == TAG_NEED_TOKEN)) {
            requestBuilder.header("tk", BaseData.getUserToken());
        }
        
        if (mNeedSession && ((defaultCfg & TAG_NEED_SESSION) == TAG_NEED_SESSION)) {
            requestBuilder.header("si", String.valueOf(HttpManager.instance().session()));
            requestBuilder.header("sg", getVerifyCode());
        }
        
        // 如果使用的IP，则需要添加Host
        if (this.url != null && !this.url.usingDomain()) {
            requestBuilder.header("Host", this.url.domain());
        }
        
        if (!TextUtils.isEmpty(mSign)) {
            requestBuilder.header("sign", mSign);
        }
        
        requestBuilder.urlParam("appplatform", AppUtil.getAppPlatformInternal());
        requestBuilder.urlParam("appname", BaseApplication.getAppNameInternal());
        requestBuilder.urlParam("appversion", PackageUtil.getVersionName());
        if (this.url != null && !this.url.cdn()) {
            mCurrentRandomID = UUID.randomUUID().toString();
            requestBuilder.urlParam("guid", mCurrentRandomID);
            if (BaseData.isUserIDValid()) {
                requestBuilder.urlParam("qquid", BaseData.getSafeUserId());
            }
        }
        
        super.req();
    }
    
    private String getVerifyCode() {
        
        String session = String.valueOf(HttpManager.instance().session());
        
        if (mSendMsg == null) {
            return MD5Util.encode(session);
        }
        
        final byte[] data = MessageNano.toByteArray(mSendMsg);
        final byte[] sessionBytes = session.getBytes(Charset.forName("UTF-8"));
        
        final ByteArrayInputStream dataStream = new ByteArrayInputStream(data);
        final ByteArrayInputStream sessionStream = new ByteArrayInputStream(sessionBytes);
        
        InputStream inputStream = new InputStream() {
            private int i = 0;
            
            public int read() throws IOException {
                int mod = i % 2;
                try {
                    if (data.length < sessionBytes.length) {
                        int count = data.length * 2;
                        if (mod == 0 && i < count) {
                            return dataStream.read();
                        }
                        else {
                            return sessionStream.read();
                        }
                    }
                    else {
                        int count = sessionBytes.length * 2;
                        if (mod == 0 && i < count) {
                            return sessionStream.read();
                        }
                        else {
                            return dataStream.read();
                        }
                    }
                } finally {
                    i++;
                }
            }
        };
        
        return MD5Util.encode(inputStream);
    }
    
    // ========================请求次数监控===============================//
    
    private static final int LIMIT_INTERVAL_SECOND = 2;// 监控时间段
    private static final int LIMIT_COUNT = 5;// 最大限制次数
    
    // 记录每个请求在一段时间内的请求次数
    private static Map<String, Integer> requestMap;
    private static CountDownCenter.CountDownListener requestCountListener;
    
    static {
        if ("true".equals(PackageUtil.getMetaData("qing.local.test"))) {
            requestMap = new HashMap<>();
            requestCountListener = new CountDownCenter.CountDownListener() {
                
                @Override
                public void onCountDown(String tag, int leftCount) {
                    if (leftCount <= 0 && requestMap.containsKey(tag)) {
                        requestMap.remove(tag);
                    }
                }
            };
        }
    }
    
    private void logRequest(String url) {
        
        if (requestMap.containsKey(url)) {
            int count = requestMap.get(url) + 1;
            if (count >= LIMIT_COUNT) {
                Logger.w("url request (" + count + ") times : " + url);
                ToastWrapper.show("接口多次请求警告\n" + url);
            }
            
            requestMap.put(url, count);
        }
        else {
            requestMap.put(url, 1);
            CountDownCenter.INSTANCE().addTask(url, LIMIT_INTERVAL_SECOND,
                    requestCountListener);
        }
    }
}

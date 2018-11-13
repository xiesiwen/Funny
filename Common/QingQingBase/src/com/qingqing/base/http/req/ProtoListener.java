package com.qingqing.base.http.req;

import android.text.TextUtils;

import com.google.protobuf.nano.MessageNano;
import com.qingqing.api.proto.v1.ProtoBufResponse;
import com.qingqing.base.BaseApplication;
import com.qingqing.qingqingbase.ui.BaseActivity;
import com.qingqing.base.core.AccountManager;
import com.qingqing.qingqingbase.ui.BaseFragment;
import com.qingqing.base.http.HttpRequest;
import com.qingqing.base.http.error.HttpClientError;
import com.qingqing.base.http.error.HttpError;
import com.qingqing.base.http.error.HttpNetworkError;
import com.qingqing.base.http.error.HttpServerError;
import com.qingqing.base.http.error.HttpTimeOutError;
import com.qingqing.base.log.Logger;
import com.qingqing.base.view.ToastWrapper;
import com.qingqing.qingqingbase.R;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;

/**
 * Created by wangxiaxin on 2016/9/29.
 * 
 * 配合于 {@link ProtoReq} 的响应回调
 */

public abstract class ProtoListener implements HttpReq.HttpListener<byte[]> {
    
    private static final String TAG = "ProtoListener";
    
    private Class mRspClazz;
    private String mErrorHintMsg;
    private boolean mSilentWhenError;// 当遇到错误的时候，不做任何提示
    private Object requestTag;
    
    // 用于处理与UI相关的逻辑
    private WeakReference<Object> mReqUIUnitRef;
    
    public ProtoListener(Class clazz) {
        mRspClazz = clazz;
    }
    
    public ProtoListener(Object ui, Class clazz) {
        this(clazz);
        mReqUIUnitRef = new WeakReference<>(ui);
    }
    
    public ProtoListener setSilentWhenError() {
        mSilentWhenError = true;
        return this;
    }
    
    public final Class getResponseClazz() {
        return mRspClazz;
    }
    
    /**
     * 处理请求返回的结果
     */
    public void onDealResult(Object result) {
        onDealResultData(result);
        if (couldNotifyUI()) {
            onDealResultUI(result);
        }
    }
    
    private boolean couldNotifyUI() {
        return mReqUIUnitRef != null && mReqUIUnitRef.get() != null
                && ((mReqUIUnitRef.get() instanceof BaseActivity
                        && ((BaseActivity) mReqUIUnitRef.get()).couldOperateUI())
                        || (mReqUIUnitRef.get() instanceof BaseFragment
                                && ((BaseFragment) mReqUIUnitRef.get()).couldOperateUI()));
    }
    
    /**
     * 处理请求返回的结果 相关的逻辑
     */
    public void onDealResultData(Object result) {
        
    }
    
    /***
     * 处理请求返回的结果 相关的UI
     */
    public void onDealResultUI(Object result) {
        
    }

    public Object requestTag(){
        return requestTag;
    }
    
    @Override
    public void onError(HttpReq<byte[]> request, HttpError error) {
        
        HttpRequest httpRequest = request.request;
        requestTag = httpRequest.tag();
        if (httpRequest.hasResponse()) {
            error.statusCode(httpRequest.statusCode());
            switch (httpRequest.statusCode()) {
                /**
                 * 2015-10-13
                 *
                 * 根据会议讨论，401（token失效的问题，在这里简单做跟412同样的处理即可，在应用每次启动的时候，
                 * 会做一次login操作）
                 */
                case 401:// token失效，需要重新执行登录动作，尝试登录请求成功则重新做一上一次请求，失败则跳回登录页面
                    ToastWrapper.show(R.string.base_login_token_invalid);
                    Logger.o(TAG, "req error . token invalid , auto logout");
                    AccountManager.INSTANCE().logout(true);
                    break;
                case 412:// session冲突，需要跳到登录页面
                    // 提示进入登录界面
                    ToastWrapper.show(R.string.base_login_session_conflict);
                    Logger.o(TAG, "req error . session conflict , auto logout");
                    AccountManager.INSTANCE().logout(true);
                    break;
                default:
                    Logger.w(TAG,
                            "req error.  status code = " + httpRequest.statusCode());
                    if (httpRequest.statusCode() == 404) {
                        Logger.w(TAG, "404   ");
                        onDealError(error, false, -1, null);
                        break;
                    }
                    if (mRspClazz != null && httpRequest.responseBody() != null) {
                        try {
                            MessageNano obj = MessageNano.mergeFrom(
                                    (MessageNano) mRspClazz.newInstance(),
                                    httpRequest.responseBody().binary());
                            Field getResponseField = obj.getClass().getField("response");
                            ProtoBufResponse.BaseResponse baseRsp = (ProtoBufResponse.BaseResponse) getResponseField
                                    .get(obj);
                            Logger.w(TAG, "req error.  errorCode=" + baseRsp.errorCode
                                    + "  errorMsg=" + baseRsp.errorMessage);
                            onDealError(error, true, baseRsp.errorCode, obj);
                        } catch (Exception e) {
                            Logger.w(TAG, "req error.  ", e);
                            onDealError(error, false, -1, null);
                        }
                    }
                    break;
            }
        }
        else {
            Logger.w(TAG, "req error.  status code = " + httpRequest.statusCode()
                    + "   no response");
            onDealError(error, false, -1, null);
        }
    }
    
    @Override
    public void onResponse(HttpReq<byte[]> request, byte[] data) {
        requestTag = request.request.tag();
        try {
            MessageNano obj = MessageNano.mergeFrom((MessageNano) mRspClazz.newInstance(),
                    data);
            Field getResponseField = obj.getClass().getField("response");
            ProtoBufResponse.BaseResponse baseRsp = (ProtoBufResponse.BaseResponse) getResponseField
                    .get(obj);
            if (baseRsp != null) {
                final int errorCode = baseRsp.errorCode;
                if (errorCode == 0) {
                    onDealResult(obj);
                }
                else {
                    if (baseRsp.hintMessage != null)
                        mErrorHintMsg = baseRsp.hintMessage;
                    Logger.w("code = " + errorCode + "  msg=" + baseRsp.errorMessage);
                    onDealError(null, true, errorCode, obj);
                }
            }
            else {
                Logger.w("base response is null");
                onDealError(null, false, -1, false);
            }
            
        } catch (Exception e) {
            Logger.w("req error.  ", e);
            onDealError(null, false, -1, false);
        }
    }
    
    /**
     * <strong>~~~慎用~~~</strong><br/>
     * 正常情况下， 请优先使用
     * {@link com.qingqing.base.http.req.ProtoListener#onDealError(int, Object)}
     * 和 {@link com.qingqing.base.http.req.ProtoListener#onDealParseError()}
     * <br/>
     * 只有 在要处理 ***所有的错误*** 的情况下，才需要覆写改方法 <br/>
     * 统一处理请求错误，包括无网络，服务器错误．．
     *
     * @param error
     *            当出现网络错误的时候error非空，否则error为null
     * @param isParseOK
     *            在返回200的基础上，解析proto数据正常则isParseOK为true，否则为false
     * @param errorCode
     *            在 isParseOK 为true的前提下，errorCode为具体的有效值，否则为-1
     * @param result
     *            在isParseOk为true的前提下，result可能为有效值
     */
    public void onDealError(HttpError error, boolean isParseOK, int errorCode,
            Object result) {
        
        if (error != null) {
            // 无网络的错误在AbsWebReq中已经提示，这里就不用重复提示了
            if (HttpReq.ERROR_NO_NETWORK.equals(error.getMessage()))
                return;
            
            int errorStringId = R.string.base_proto_rsp_error_default_msg;
            if (error instanceof HttpNetworkError) {
                // networkResponse == null
                errorStringId = R.string.base_request_network_error;
            }
            else if (error instanceof HttpTimeOutError) {
                // java SocketTimeoutException || ConnectTimeoutException
                // 网络超时
                errorStringId = R.string.base_request_time_out_error;
            }
            else if (error instanceof HttpClientError) {
                errorStringId = R.string.base_request_client_error;
            }
            else if (error instanceof HttpServerError) {
                // 大部分情况下 为 5xx
                errorStringId = R.string.base_request_server_error;
            }
            
            if (!mSilentWhenError)
                ToastWrapper.show(errorStringId);
        }
        else {
            if (isParseOK) {
                if (!onDealError(errorCode, result)) {
                    if (!mSilentWhenError)
                        ToastWrapper.show(getErrorHintMessage(
                                R.string.base_proto_rsp_error_default_msg));
                }
            }
            else {
                onDealParseError();
            }
        }
    }
    
    /**
     * 获取 Error 提示
     *
     * @param errorMessage
     *            外部传入的error信息
     */
    public String getErrorHintMessage(String errorMessage) {
        if (!TextUtils.isEmpty(mErrorHintMsg)) {
            return mErrorHintMsg;
        }
        else {
            return errorMessage;
        }
    }
    
    public String getErrorHintMessage(int messageId) {
        return getErrorHintMessage(BaseApplication.getCtx().getString(messageId));
    }
    
    /**
     * 用来处理数据解析OK，服务器返回指定的errorCode的情况下的逻辑 <br/>
     * 可以通过使用
     * {@link com.qingqing.base.http.req.ProtoListener#getErrorHintMessage(int)}
     * 来获取需要提示用的errorMessage
     *
     *
     * @see com.qingqing.base.http.req.ProtoListener#onDealParseError()
     *
     * @param errorCode
     *            错误码
     * @param result
     *            可以使用的response对象
     * @return false: 会toast 默认提示<br/>
     *         true: 上层做错误处理
     */
    public boolean onDealError(int errorCode, Object result) {
        return false;
    }
    
    /**
     * 用来处理网络请求OK，但是数据解析错误的情况
     *
     * @see com.qingqing.base.http.req.ProtoListener#onDealError(int, Object)
     */
    public void onDealParseError() {
        if (!mSilentWhenError)
            ToastWrapper.show(R.string.base_proto_rsp_parse_fail);
    }
}

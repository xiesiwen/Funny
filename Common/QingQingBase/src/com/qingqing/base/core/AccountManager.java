package com.qingqing.base.core;

import java.lang.ref.SoftReference;

import com.google.protobuf.nano.MessageNano;
import com.qingqing.api.proto.v1.AssistantProto;
import com.qingqing.api.proto.v1.Geo;
import com.qingqing.api.proto.v1.Login;
import com.qingqing.api.proto.v1.Register;
import com.qingqing.api.proto.v1.UserProto;
import com.qingqing.base.BaseApplication;
import com.qingqing.base.bean.Address;
import com.qingqing.base.bean.LatLng;
import com.qingqing.base.config.CommonUrl;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.data.SPManager;
import com.qingqing.base.http.HttpManager;
import com.qingqing.base.http.HttpUrl;
import com.qingqing.base.http.error.HttpError;
import com.qingqing.base.http.req.ProtoListener;
import com.qingqing.base.http.req.ProtoReq;
import com.qingqing.base.log.Logger;
import com.qingqing.base.time.NetworkTime;
import com.qingqing.base.utils.DeviceUtil;
import com.qingqing.base.utils.NetworkUtil;
import com.qingqing.base.utils.PackageUtil;
import com.qingqing.base.view.ToastWrapper;
import com.qingqing.qingqingbase.R;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.text.TextUtils;

/**
 * 单例，处理登陆，注册，登出. 获取userId
 * <p/>
 * 管理 登录和注册
 *
 * @author huangming
 */
public final class AccountManager {
    
    private static AccountManager sInstance;
    public static final String CLIENT_TYPE = "android";
    private int mUserType = UserProto.UserType.ta;
    
    private AccountManagerListener mListener;
    private SoftReference<LoginOrRegResponseListener> mRegisterListener;
    private SoftReference<LoginOrRegResponseListener> mLoginListener;
    private SoftReference<LoginOrRegResponseListener> mUpdateTokenListener;
    
    private OptionSyncListener mOptionSyncListener;
    private BaseInfoReqListener mBaseInfoReqListener;
    private long mLastOptionReqTime;
    private long mLastBaseInfoReqTime;
    
    private HttpUrl mOptionUrl;
    private HttpUrl mBaseInfoUrl;
    
    private static final String TAG = "AccountManager";
    private Class<? extends MessageNano> mOptionRspClazz;// option处理类
    private Class<? extends MessageNano> mBaseInfoRspClazz;// baseInfo处理类
    private static final String OPTION_TAG = "AccountOption";
    private static final String BASE_INFO_TAG = "BaseInfo";
    private static final int CHECK_INTERVAL = 60 * 30;
    private boolean mIsLogoutInternal = false;

    public interface AccountManagerListener {
        void onLogout();
        
        void onSaveSuccessInfo();
    }
    
    public interface LoginOrRegResponseListener {
        void onLoginOrRegResponse(int errorCode, String errorMsg);
        
        void onLoginResponse(int errorCode, String errorMsg);
        
        void onRegResponse(int errorCode, String errorMsg);
    }
    
    public interface OptionSyncListener {
        void onOptionSyncDone(MessageNano rsp);
    }
    
    public interface BaseInfoReqListener {
        void onBaseInfoReqDone(MessageNano rsp);
    }
    
    public interface ResetPasswordListener {
        void onResetDone();
    }
    
    public static AccountManager INSTANCE() {
        if (sInstance == null) {
            synchronized (AccountManager.class) {
                if (sInstance == null) {
                    sInstance = new AccountManager();
                }
            }
        }
        return sInstance;
    }
    
    private AccountManager() {
        
        mLastOptionReqTime = SPManager.getLong(OPTION_TAG, 0L);
        mLastBaseInfoReqTime = SPManager.getLong(BASE_INFO_TAG, 0L);
        
        mLastNetType = NetworkUtil.getConnectionType();
        BroadcastReceiver networkReceiver = new BroadcastReceiver() {
            
            @Override
            public void onReceive(Context context, Intent intent) {
                if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                    NetworkUtil.ConnectionType type = NetworkUtil.getConnectionType();
                    Logger.i(TAG, "network change : new type = " + type + "   old type = "
                            + mLastNetType);
                    if (type != null && mLastNetType != type) {
                        mLastNetType = type;
                    }
                    else {
                        return;
                    }
                    reqOption();
                    reqBaseInfo();
                }
            }
        };
        
        BaseApplication.getCtx().registerReceiver(networkReceiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }
    
    public boolean isLogoutInternal() {
        final boolean ret = mIsLogoutInternal;
        mIsLogoutInternal = false;
        return ret;
    }
    
    public AccountManager setUserType(int type) {
        mUserType = type;
        // initReqUrl();
        return this;
    }
    
    public AccountManager setBaseInfoUrl(HttpUrl url) {
        mBaseInfoUrl = url;
        return this;
    }
    
    public AccountManager setOptionUrl(HttpUrl url) {
        mOptionUrl = url;
        return this;
    }
    
    public AccountManager setListener(AccountManagerListener listener) {
        mListener = listener;
        return this;
    }
    
    public AccountManager setOptionDealClass(Class<? extends MessageNano> clazz) {
        mOptionRspClazz = clazz;
        return this;
    }
    
    public AccountManager setOptionSyncListener(OptionSyncListener listener) {
        mOptionSyncListener = listener;
        return this;
    }
    
    public AccountManager setBaseInfoDealClass(Class<? extends MessageNano> clazz) {
        mBaseInfoRspClazz = clazz;
        return this;
    }
    
    public AccountManager setBaseInfoReqListener(BaseInfoReqListener listener) {
        mBaseInfoReqListener = listener;
        return this;
    }

    public static class LoginInfoBuilder{
        String userName;
        String nickName;
        String password;
        LatLng latLng = Address.getLocation().latLng != null ? Address.getLocation().latLng : LatLng.NONE;
        String captchaCode;
        HttpUrl url;
        Integer enterType = Register.EnterType.app_register;
        String chid;
        String registerSource;
        String activityNo;
        String inviteCode;
        int cityId;
        LoginOrRegResponseListener listener;

        public LoginInfoBuilder setUserName(String userName) {
            this.userName = userName;
            return this;
        }

        public LoginInfoBuilder setNickName(String nickName) {
            this.nickName = nickName;
            return this;
        }

        public LoginInfoBuilder setPassword(String password) {
            this.password = password;
            return this;
        }

        public LoginInfoBuilder setLatLng(LatLng latLng) {
            this.latLng = latLng;
            return this;
        }

        public LoginInfoBuilder setListener(LoginOrRegResponseListener listener) {
            this.listener = listener;
            return this;
        }

        public LoginInfoBuilder setCaptchaCode(String captchaCode) {
            this.captchaCode = captchaCode;
            return this;
        }

        public LoginInfoBuilder setUrl(HttpUrl url) {
            this.url = url;
            return  this;
        }

        public LoginInfoBuilder setEnterType(Integer enterType) {
            this.enterType = enterType;
            return this;
        }

        public LoginInfoBuilder setChid(String chid) {
            this.chid = chid;
            return this;
        }

        public LoginInfoBuilder setRegisterSource(String registerSource) {
            this.registerSource = registerSource;
            return this;
        }

        public LoginInfoBuilder setActivityNo(String activityNo) {
            this.activityNo = activityNo;
            return this;
        }

        public LoginInfoBuilder setInviteCode(String inviteCode) {
            this.inviteCode = inviteCode;
            return this;
        }

        public LoginInfoBuilder setCityId(int cityId) {
            this.cityId = cityId;
            return this;
        }
    }

    /**
     * 发起登录请求
     */
    @Deprecated
    public void login(Context ctx, LoginInfoBuilder builder) {
        mLoginListener = new SoftReference<>(builder.listener);
        Login.LoginRequest loginRequest = new Login.LoginRequest();
        loginRequest.name = builder.userName;
        loginRequest.password = builder.password;
        loginRequest.client = CLIENT_TYPE;
        loginRequest.userType = mUserType;
        loginRequest.hasUserType = true;
        loginRequest.clientVersion = PackageUtil.getVersionName();
        Geo.GeoPoint geoPoint = new Geo.GeoPoint();
        geoPoint.latitude = builder.latLng.latitude;
        geoPoint.longitude = builder.latLng.longitude;
        loginRequest.geoPoint = geoPoint;

        String loginLogStr = String.format(
                "clinet==%s,userType==%s,clientVersion==%s,longitude=%s,latitude=%s",
                CLIENT_TYPE, mUserType, PackageUtil.getVersionName(), geoPoint.longitude,
                geoPoint.latitude);

        BaseData.saveUserName(loginRequest.name);
        Logger.o(TAG, "start login : " + loginLogStr);
        new ProtoReq(CommonUrl.LOGIN_URL.url()).setSendMsg(loginRequest)
                .setLoadingIndication(ctx).setNeedCheckUserID(false)
                .setRspListener(mInternalLoginListener).req();
    }

    /**
     * 密码登录接口
     */
    public void loginV2(Context ctx, LoginInfoBuilder builder) {
        mLoginListener = new SoftReference<>(builder.listener);
        Login.LoginRequestV2 loginRequestBuilder = new Login.LoginRequestV2();
        if (builder.latLng.latitude != LatLng.INVALID_VALUE
                && builder.latLng.longitude == LatLng.INVALID_VALUE) {
            Geo.GeoPoint geoPoint = new Geo.GeoPoint();
            geoPoint.latitude = builder.latLng.latitude;
            geoPoint.longitude = builder.latLng.longitude;
            loginRequestBuilder.geoPoint = geoPoint;
        }

        loginRequestBuilder.name = builder.userName;
        loginRequestBuilder.password = builder.password;
        loginRequestBuilder.deviceId = DeviceUtil.getIdentification();
        loginRequestBuilder.userType = mUserType;
        loginRequestBuilder.hasUserType = true;

        String loginLogStr = String.format(
                "clinet==%s,userType==%s,clientVersion==%s,longitude=%s,latitude=%s",
                CLIENT_TYPE, mUserType, PackageUtil.getVersionName(), builder.latLng.longitude,
                builder.latLng.latitude);

        BaseData.saveUserName(builder.userName);
        Logger.o(TAG, "start login V2 : " + loginLogStr);

        new ProtoReq(CommonUrl.LOGIN_URL_V2.url()).setSendMsg(loginRequestBuilder)
                .setLoadingIndication(ctx, true).setNeedCheckUserID(false)
                .setRspListener(mInternalLoginListener).req();
    }

    /** 新的登录注册通用接口--- 只能用于验证码登录或者注册 */
    public void loginOrRegister(Context context, LoginInfoBuilder builder) {
        if (builder == null || (TextUtils.isEmpty(builder.password) && TextUtils.isEmpty(builder.captchaCode))) {
            return;
        }
        mLoginListener = new SoftReference<>(builder.listener);
        Register.RegisterRequestV2 registerRequest = new Register.RegisterRequestV2();
        
        if (builder.latLng.latitude != LatLng.INVALID_VALUE && builder.latLng.longitude == LatLng.INVALID_VALUE) {
            Geo.GeoPoint geoPoint = new Geo.GeoPoint();
            geoPoint.latitude = builder.latLng.latitude;
            geoPoint.longitude = builder.latLng.longitude;
            registerRequest.geoPoint = geoPoint;
        }
        registerRequest.username = builder.userName;
        if(!TextUtils.isEmpty(builder.nickName)) {
            registerRequest.nickname = builder.nickName;
        }
        BaseData.saveUserName(registerRequest.username);
        if (!TextUtils.isEmpty(builder.password)) {
            registerRequest.password = builder.password;
        }
        if (!TextUtils.isEmpty(builder.captchaCode)) {
            registerRequest.captchaCode = builder.captchaCode;
        }
        registerRequest.userType = mUserType;
        registerRequest.hasUserType = true;
        registerRequest.enterType = builder.enterType;
        registerRequest.hasEnterType = true;
        if (!TextUtils.isEmpty(builder.chid)) {
            registerRequest.spreadSource = builder.chid;
        }

        registerRequest.channelNo= PackageUtil.getChannel();
        registerRequest.deviceId = DeviceUtil.getIdentification();
        if (!TextUtils.isEmpty(builder.registerSource)) {
            registerRequest.registerSource = builder.registerSource;
            registerRequest.hasRegisterSource = true;
        }
        if (!TextUtils.isEmpty(builder.activityNo)) {
            registerRequest.activityNo = builder.activityNo;
            registerRequest.hasActivityNo = true;
        }

        if(builder.url == null){
            builder.url = CommonUrl.REGISTER_OR_LOGIN.url();
        }
        new ProtoReq(builder.url).setSendMsg(registerRequest).setLoadingIndication(context, true)
                .setNeedCheckUserID(false)
                .setRspListener(mInternalRegisterOrLoginListener).req();
    }

    
    public void loginWithToken(Context context, LoginInfoBuilder builder) {
        mUpdateTokenListener = new SoftReference<>(builder.listener);
        Login.TkLoginRequest loginRequest = new Login.TkLoginRequest();
        Geo.GeoPoint geoPoint = new Geo.GeoPoint();
        geoPoint.latitude = builder.latLng.latitude;
        geoPoint.longitude = builder.latLng.longitude;
        loginRequest.geoPoint = geoPoint;
        
        String loginLogStr = String.format(
                "clinet==%s,userType==%s,clientVersion==%s,longitude=%s,latitude=%s",
                CLIENT_TYPE, mUserType, PackageUtil.getVersionName(), builder.latLng.longitude,
                builder.latLng.latitude);
        
        Logger.o(TAG, "start loginWithToken : " + loginLogStr);
        ProtoReq protoReq = new ProtoReq(CommonUrl.TOKEN_LOGIN_URL.url());
        if (context != null) {
            protoReq.setLoadingIndication(context, context.getString(R.string.auto_login), false);
        }
        protoReq.setSendMsg(loginRequest)
                .setRspListener(mInternalUpdateTokenListener).reqSilent();
    }
    
    public void resetPassword(String userName, String newPassword, String captchaCode,
            double latitude, double longitude, final ResetPasswordListener listener) {
        Register.ResetPasswordRequest request = new Register.ResetPasswordRequest();
        request.userType = mUserType;
        request.hasUserType = true;
        request.username = userName;
        request.password = newPassword;
        request.captchaCode = captchaCode;
        Geo.GeoPoint geoPoint = new Geo.GeoPoint();
        geoPoint.latitude = latitude;
        geoPoint.longitude = longitude;
        request.geoPoint = geoPoint;
        BaseData.saveUserName(userName);
        
        new ProtoReq(CommonUrl.RESET_PASSWORD.url()).setSendMsg(request)
                .setRspListener(new ProtoListener(Login.LoginResponseV2.class) {
                    @Override
                    public void onDealResult(Object result) {
                        
                        Login.LoginResponseV2 loginResponse = (Login.LoginResponseV2) result;
                        saveSuccessInfo(BaseData.getUserId(),
                                loginResponse.token, loginResponse.sessionId,
                                loginResponse.qingqingUserId, loginResponse.userSecondId,
                                false);
                        if (listener != null) {
                            listener.onResetDone();
                        }
                    }
                    
                    @Override
                    public boolean onDealError(int errorCode, Object result) {
                        
                        boolean ret = true;
                        switch (errorCode) {
                            case 1001:
                                ToastWrapper.show(getErrorHintMessage(
                                        R.string.base_reset_password_error_1001));
                                break;
                            case 1002:
                                ToastWrapper.show(getErrorHintMessage(
                                        R.string.base_reset_password_error_1002));
                                break;
                            case 1003:
                                ToastWrapper.show(getErrorHintMessage(
                                        R.string.base_captcha_code_error));
                                break;
                            default:
                                ret = false;
                                break;
                        }
                        return ret;
                    }
                }).req();
    }
    
    /**
     * 有活动时 发起的注册请求
     */
    public void registerWithActivity(Context ctx, LoginInfoBuilder infoBuilder) {

        mRegisterListener = new SoftReference<>(infoBuilder.listener);
        BaseData.saveUserName(infoBuilder.userName);
        
        Register.InviteCodeRegisterStudentRequest builder = new Register.InviteCodeRegisterStudentRequest();
        
        if (!TextUtils.isEmpty(infoBuilder.inviteCode)) {
            builder.inviteCode = infoBuilder.inviteCode;
        }
        
        builder.username = infoBuilder.userName;
        builder.deviceId = DeviceUtil.getIdentification();
        builder.channelNo = PackageUtil.getChannel();
        builder.captchaCode = infoBuilder.captchaCode;
        builder.cityId = infoBuilder.cityId;
        Geo.GeoPoint geoPoint = new Geo.GeoPoint();
        geoPoint.latitude = infoBuilder.latLng.latitude;
        geoPoint.longitude = infoBuilder.latLng.longitude;
        builder.geoPoint = geoPoint;
        
        new ProtoReq(CommonUrl.ACTIVITY_REGISTER_URL.url()).setSendMsg(builder)
                .setNeedCheckUserID(false).setLoadingIndication(ctx, true)
                .setRspListener(mInternalActivityRegisterListener).req();
    }
    
    /** 保存注册登录成功后的信息 */
    public void saveSuccessInfo(long uid, String token, String session,
            String qingQingUserId, String secondId, boolean needNotify) {
        
        long s = Long.parseLong(session);
        BaseData.getInstance().saveToLocal(token, uid, session, qingQingUserId,
                secondId);
        HttpManager.instance().session(s);
        
        startSync();
        
        if (needNotify) {
            // 登录成功后启动mqtt
            if (mListener != null) {
                mListener.onSaveSuccessInfo();
            }
        }
    }
    
    private void saveSuccessInfo(String name, String token, String session,
            String qingQingUserId, String secondId, boolean needNotify) {
        
        long s = Long.parseLong(session);
        BaseData.getInstance().saveToLocal(token, -1, session, qingQingUserId,
                secondId);
        HttpManager.instance().session(s);
        
        startSync();
        
        if (needNotify) {
            // 登录成功后启动mqtt
            if (mListener != null) {
                mListener.onSaveSuccessInfo();
            }
        }
    }
    
    /**
     * H5 页面同步登录信息（H5 登录后传给 Native）
     */
    public void syncUserInfoFromH5(String userName, long uid, String token,
            String session, String qingQingUserId, String secondId) {
        BaseData.saveUserName(userName);
        saveSuccessInfo(uid, token, session, qingQingUserId, secondId, true);
        reqBaseInfo();
        reqOption();
    }
    
    /**
     * 退出到登录界面
     */
    public void logout() {
        logout(false);
    }
    
    public void logout(boolean isInternal) {
        
        Logger.o(TAG, " logout  flag=" + isInternal);
        mIsLogoutInternal = isInternal;
        // 取消option的定时获取
        stopSync();
        
        if (mListener != null) {
            mListener.onLogout();
        }
    }
    
    private ProtoListener mInternalUpdateTokenListener = new ProtoListener(
            Login.LoginResponse.class) {
        
        @Override
        public void onDealResult(Object result) {
            Logger.o(TAG, "loginWithToken  onDealResult");
            Login.LoginResponse loginResponse = (Login.LoginResponse) result;
            saveSuccessInfo(loginResponse.userId, loginResponse.token,
                    loginResponse.sessionId, loginResponse.qingqingUserId,
                    loginResponse.userSecondId, false);
            
            if (mUpdateTokenListener != null && mUpdateTokenListener.get() != null) {
                mUpdateTokenListener.get().onLoginOrRegResponse(0, null);
                mUpdateTokenListener = null;
            }
        }
        
        @Override
        public void onDealError(HttpError error, boolean isParseOK, int errorCode,
                Object result) {
            Logger.o(TAG, "loginWithToken  onDealError : errorCode=" + errorCode
                    + "  error=" + error);
            if (mUpdateTokenListener != null && mUpdateTokenListener.get() != null) {
                mUpdateTokenListener.get().onLoginOrRegResponse(errorCode, null);
                mUpdateTokenListener = null;
            }
        }
    };
    
    /** 内部的登录结果处理 */
    private ProtoListener mInternalLoginListener = new ProtoListener(
            Login.LoginResponse.class) {
        
        @Override
        public void onDealResult(Object result) {
            Login.LoginResponse loginResponse = (Login.LoginResponse) result;
            saveSuccessInfo(loginResponse.userId, loginResponse.token,
                    loginResponse.sessionId, loginResponse.qingqingUserId,
                    loginResponse.userSecondId, true);
            forceReqBaseInfo(new BaseInfoReqListener() {
                @Override
                public void onBaseInfoReqDone(MessageNano rsp) {
                    forceReqOption(new InternalSyncOptionCallback(0, null,
                            mLoginListener != null ? mLoginListener.get() : null));
                }
            });
        }
        
        @Override
        public void onDealError(HttpError error, boolean isParseOK, int errorCode,
                Object result) {
            
            String errorMsg = BaseApplication.getCtx()
                    .getString(R.string.base_login_error_1001);
            if (error == null) {
                switch (errorCode) {
                    // case 1001:
                    // default:
                    // errorMsg = BaseApplication.getCtx().getString(
                    // R.string.base_login_error_1001);
                    // break;
                    case 1002:
                        errorMsg = BaseApplication.getCtx()
                                .getString(R.string.base_login_error_1002);
                        break;
                    case 1003:
                        errorMsg = BaseApplication.getCtx()
                                .getString(R.string.base_login_error_1003);
                        break;
                    case 1005:
                        errorMsg = BaseApplication.getCtx()
                                .getString(R.string.base_captcha_code_error);
                        break;
                }
            }
            else {
                if (!TextUtils.isEmpty(error.getMessage())) {
                    errorMsg = error.getMessage();
                }
            }
            
            if (mLoginListener != null && mLoginListener.get() != null) {
                mLoginListener.get().onLoginOrRegResponse(errorCode, errorMsg);
                mLoginListener = null;
            }
        }
    };
    
    /** 内部新版登录注册结果处理 */
    private ProtoListener mInternalRegisterOrLoginListener = new ProtoListener(
            Register.RegisterResponseV2.class) {
        @Override
        public void onDealResult(Object result) {
            Register.RegisterResponseV2 rsp = (Register.RegisterResponseV2) result;
            final boolean isRegister = rsp.newRegistered;
            Logger.o(TAG, "register response : done");
            saveSuccessInfo(rsp.userId, rsp.token, rsp.sessionId,
                    rsp.qingqingUserId, rsp.userSecondId, true);
            forceReqBaseInfo(new BaseInfoReqListener() {
                @Override
                public void onBaseInfoReqDone(MessageNano rsp) {
                    forceReqOption(new InternalSyncOptionCallback(0, null,
                            mLoginListener != null ? mLoginListener.get() : null,
                            isRegister));
                }
            });
        }
        
        @Override
        public void onDealError(HttpError error, boolean isParseOK, int errorCode,
                Object result) {
            
            String errorMsg = "未知错误";
            if (result != null && result instanceof Register.RegisterResponseV2) {
                Register.RegisterResponseV2 rsp = (Register.RegisterResponseV2) result;
                final boolean isRegister = rsp.newRegistered;
                Logger.o(TAG, "register response : code = " + errorCode + " msg = "
                        + rsp.response.errorMessage);
                boolean success = errorCode == 1002;
                if (success) {
                    saveSuccessInfo(rsp.userId, rsp.token, rsp.sessionId,
                            rsp.qingqingUserId, rsp.userSecondId, true);
                    final int _errorCode = errorCode;
                    forceReqBaseInfo(new BaseInfoReqListener() {
                        @Override
                        public void onBaseInfoReqDone(MessageNano rsp) {
                            forceReqOption(
                                    new InternalSyncOptionCallback(_errorCode,
                                            null, mLoginListener != null
                                                    ? mLoginListener.get() : null,
                                            isRegister));
                        }
                    });
                    return;
                }
                else {
                    switch (errorCode) {
                        case 1001:// 注册失败
                        default:
                            errorMsg = BaseApplication.getCtx()
                                    .getString(R.string.base_login_error_1001);
                            break;
                        case 1003:
                            errorMsg = BaseApplication.getCtx()
                                    .getString(R.string.base_register_error_1003);
                            break;
                        case 1005:
                            errorMsg = BaseApplication.getCtx()
                                    .getString(R.string.base_captcha_code_error);
                            break;
                    }
                }
            }
            
            if (mLoginListener != null && mLoginListener.get() != null) {
                mLoginListener.get().onLoginOrRegResponse(errorCode, errorMsg);
                mLoginListener = null;
            }
            // super.onDealError(error, isParseOK, errorCode, result);
        }
    };
    

    private ProtoListener mInternalActivityRegisterListener = new ProtoListener(
            Register.RegisterResponse.class) {
        @Override
        public void onDealResult(Object result) {
            Register.RegisterResponse rsp = (Register.RegisterResponse) result;
            saveSuccessInfo(rsp.userId, rsp.token, rsp.sessionId,
                    rsp.qingqingUserId, rsp.userSecondId, true);
            forceReqBaseInfo(new BaseInfoReqListener() {
                @Override
                public void onBaseInfoReqDone(MessageNano rsp) {
                    forceReqOption(new InternalSyncOptionCallback(0, null,
                            mRegisterListener != null ? mRegisterListener.get() : null));
                }
            });
        }
        
        @Override
        public boolean onDealError(int errorCode, Object result) {
            
            String errMsg;
            switch (errorCode) {
                case 1001:
                    errMsg = BaseApplication.getCtx()
                            .getString(R.string.base_register_error_1001);
                    break;
                case 1002:
                    errMsg = BaseApplication.getCtx()
                            .getString(R.string.base_register_error_1002);
                    break;
                case 1003:
                    errMsg = BaseApplication.getCtx()
                            .getString(R.string.base_register_error_1003);
                    break;
                case 1005:
                    errMsg = BaseApplication.getCtx()
                            .getString(R.string.base_captcha_code_error);
                    break;
                case 2004:
                case 2005: {
                    errMsg = getErrorHintMessage(R.string.base_register_error_1001);
                    Register.RegisterResponse rsp = (Register.RegisterResponse) result;
                    saveSuccessInfo(rsp.userId, rsp.token, rsp.sessionId,
                            rsp.qingqingUserId, rsp.userSecondId, true);
                    final int _errorCode = errorCode;
                    final String _errMsg = errMsg;
                    
                    forceReqBaseInfo(new BaseInfoReqListener() {
                        @Override
                        public void onBaseInfoReqDone(MessageNano rsp) {
                            forceReqOption(new InternalSyncOptionCallback(_errorCode,
                                    _errMsg, mRegisterListener != null
                                            ? mRegisterListener.get() : null));
                        }
                    });
                }
                    return true;
                default:
                    errMsg = getErrorHintMessage(R.string.base_register_error_1001);
                    break;
            }
            
            if (mRegisterListener != null && mRegisterListener.get() != null) {
                mRegisterListener.get().onLoginOrRegResponse(errorCode, errMsg);
                mRegisterListener = null;
            }
            return true;
        }
    };
    
    /**************
     * option 相关
     ********************/
    private NetworkUtil.ConnectionType mLastNetType;
    
    public void startSync() {
        // 如果倒计时任务中已存在，说明之前已经调用过startSyncOption
        if (CountDownCenter.INSTANCE().isDuringCountDown(OPTION_TAG))
            return;
        
        // 定时获取OPTION
        CountDownCenter.INSTANCE().addRepeatTask(OPTION_TAG, CHECK_INTERVAL,
                new CountDownCenter.CountDownListener() {
                    @Override
                    public void onCountDown(String tag, int leftCount) {
                        if (leftCount == 0) {
                            reqBaseInfo();
                            reqOption();
                        }
                    }
                });
    }
    
    public void stopSync() {
        CountDownCenter.INSTANCE().cancelTask(OPTION_TAG);
        mLastBaseInfoReqTime = 0L;
        mLastOptionReqTime = 0L;
        SPManager.put(OPTION_TAG, mLastOptionReqTime);
        SPManager.put(BASE_INFO_TAG, mLastBaseInfoReqTime);
    }
    
    private class InternalSyncOptionCallback {
        
        private boolean registerOrLoginMode;
        private boolean isRegister;
        private int mErrorCode;
        private String mErrorMsg;
        private LoginOrRegResponseListener mRspListener;
        
        InternalSyncOptionCallback(int errorCode, String errorMsg,
                LoginOrRegResponseListener rspListener) {
            mErrorCode = errorCode;
            mErrorMsg = errorMsg;
            mRspListener = rspListener;
        }

        InternalSyncOptionCallback(int errorCode, String errorMsg,
                                   LoginOrRegResponseListener rspListener,boolean isRegister) {
            mErrorCode = errorCode;
            mErrorMsg = errorMsg;
            mRspListener = rspListener;
            registerOrLoginMode = true;
            this.isRegister = isRegister;
        }
        
        void onInternalSyncOptionDone() {
            if (mRspListener != null) {
                mRspListener.onLoginOrRegResponse(mErrorCode, mErrorMsg);
                if(registerOrLoginMode){
                    if(isRegister){
                        mRspListener.onRegResponse(mErrorCode,mErrorMsg);
                    }else{
                        mRspListener.onLoginResponse(mErrorCode,mErrorMsg);
                    }
                }
                mRspListener = null;
            }
        }
    }
    
    private boolean couldReqInfo(long lastTime) {
        final long curTime = NetworkTime.currentTimeMillis();
        return curTime > lastTime + CHECK_INTERVAL * 1000;
    }
    
    /**
     * 请求option
     */
    public void reqOption() {
        if (!couldReqInfo(mLastOptionReqTime)) {
            if (mOptionSyncListener != null) {
                mOptionSyncListener.onOptionSyncDone(null);
            }
            return;
        }
        forceReqOption();
    }
    
    /**
     * 强制请求option
     */
    public void forceReqOption(final InternalSyncOptionCallback callback) {
        
        if (mOptionUrl == null || !BaseData.isUserIDValid()) {
            if (callback != null) {
                callback.onInternalSyncOptionDone();
            }
            return;
        }
        
        Logger.o(OPTION_TAG, "begin sync option");
        new ProtoReq(mOptionUrl)
                .setRspListener(new ProtoListener(mOptionRspClazz) {
                    @Override
                    public void onDealResult(Object result) {
                        mLastOptionReqTime = NetworkTime.currentTimeMillis();
                        SPManager.put(OPTION_TAG, mLastOptionReqTime);
                        if (mOptionSyncListener != null) {
                            mOptionSyncListener.onOptionSyncDone((MessageNano) result);
                        }
                        
                        if (callback != null) {
                            callback.onInternalSyncOptionDone();
                        }
                    }
                    
                    @Override
                    public void onDealError(HttpError error, boolean isParseOK,
                            int errorCode, Object result) {
                        if (mOptionSyncListener != null) {
                            mOptionSyncListener.onOptionSyncDone(null);
                        }
                        if (callback != null) {
                            callback.onInternalSyncOptionDone();
                        }
                    }
                }).reqSilent();
    }
    
    // 强制同步option信息
    public void forceReqOption() {
        forceReqOption(null);
    }
    
    public void reqBaseInfo() {
        reqBaseInfo(null);
    }
    
    /**
     * 请求baseInfo
     */
    public void reqBaseInfo(final BaseInfoReqListener listener) {
        
        if (!couldReqInfo(mLastBaseInfoReqTime)) {
            if (mBaseInfoReqListener != null) {
                mBaseInfoReqListener.onBaseInfoReqDone(null);
            }
            if (listener != null) {
                listener.onBaseInfoReqDone(null);
            }
            return;
        }
        
        forceReqBaseInfo(listener);
    }
    
    public void forceReqBaseInfo() {
        forceReqBaseInfo(null);
    }
    
    /** 强制请求BaseInfo */
    public void forceReqBaseInfo(final BaseInfoReqListener listener) {
        new ProtoReq(CommonUrl.TA_GET_BASE_INFO.url()).setRspListener(new ProtoListener(AssistantProto.AssistantBaseInfoResponse.class) {
            @Override
            public void onDealResult(Object result) {
                mLastBaseInfoReqTime = NetworkTime.currentTimeMillis();
                SPManager.put(BASE_INFO_TAG, mLastBaseInfoReqTime);
                AccountOption.INSTANCE().saveBaseInfo((MessageNano) result);
                if (listener != null) {
                    listener.onBaseInfoReqDone((MessageNano) result);
                }
            }
            
            @Override
            public void onDealError(HttpError error, boolean isParseOK, int errorCode,
                    Object result) {
                if (mBaseInfoReqListener != null) {
                    mBaseInfoReqListener.onBaseInfoReqDone(null);
                }
                if (listener != null) {
                    listener.onBaseInfoReqDone(null);
                }
            }
        }).reqSilent();
    }
}

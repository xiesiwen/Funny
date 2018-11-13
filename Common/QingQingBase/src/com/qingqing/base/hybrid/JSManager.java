package com.qingqing.base.hybrid;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.qingqing.api.proto.v1.app.AppCommon;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.utils.AppUtil;
import com.qingqing.base.utils.DeviceUtil;
import com.qingqing.base.utils.NetworkUtil;
import com.qingqing.base.utils.PackageUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public enum JSManager {
    
    INSTANCE;
    
    public static final String CONTENT_CHECK_JS_API = "checkjsapi";
    public static final String CONTENT_TK = "tk";
    public static final String CONTENT_SI = "si";
    public static final String CONTENT_PLT = "plt";
    public static final String CONTENT_AT = "at";
    public static final String CONTENT_VER = "ver";
    public static final String CONTENT_DID = "did";
    public static final String CONTENT_DEVICE_INFO = "deviceinfo";
    public static final String CONTENT_NETWORK_STATUS = "networkstatus";
    
    boolean mIsJsCallbackAsync = true;
    ArrayList<String> mAvailableJsApiList = new ArrayList<>();
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private HashMap<String, HashMap<String, WeakReference<JSCallback>>> mCallbackMap;
    private ArrayList<JsAsyncCallbackListener> mAsyncCallbackList;
    private CustomerJSContentGetter mCustomJSContentGetter;
    
    JSManager() {
        mCallbackMap = new HashMap<>();
        mAsyncCallbackList = new ArrayList<>();
        initAvailableJsApiList();
    }
    
    public void addAsyncCallbackListener(JsAsyncCallbackListener listener) {
        if (mAsyncCallbackList == null) {
            mAsyncCallbackList = new ArrayList<>();
        }
        mAsyncCallbackList.add(listener);
    }
    
    public void removeAsyncCallbackListener(JsAsyncCallbackListener listener) {
        if (mAsyncCallbackList != null) {
            mAsyncCallbackList.remove(listener);
        }
    }
    
    public JSManager addCallback(String fragmentId, String method, JSCallback callback) {
        // 过滤空 id
        if (TextUtils.isEmpty(fragmentId)) {
            return this;
        }
        
        // 当前页面已有时，添加
        for (String key : mCallbackMap.keySet()) {
            if (key.equals(fragmentId)) {
                mCallbackMap.get(fragmentId).put(method, new WeakReference<>(callback));
                return this;
            }
        }
        
        // 当前页面没有时，新建
        HashMap<String, WeakReference<JSCallback>> aa = new HashMap<>();
        aa.put(method, new WeakReference<>(callback));
        mCallbackMap.put(fragmentId, aa);
        return this;
    }
    
    public void setCustomerJSContentGetter(CustomerJSContentGetter getter) {
        mCustomJSContentGetter = getter;
    }
    
    public void qqJSCallBackWithContentWithMethodName(String fragmentId,
            final String param, final String methodName) {
        Log.i("JSManager", "call from js " + "---" + methodName + "---" + param);
        if (mCallbackMap.get(fragmentId) != null) {
            WeakReference<JSCallback> callbackRef = mCallbackMap.get(fragmentId)
                    .get(methodName);
            final JSCallback callback = callbackRef != null ? callbackRef.get() : null;
            if (callback != null) {
                Log.i("JSManager", "find callback" + "---" + methodName);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onCallback(methodName, param);
                    }
                });
            }
        }
    }
    
    public String qqJSAsyncGetContentCallBack(String param, String callback) {
        if (mIsJsCallbackAsync) {
            String result = getContentResult(param);
            
            if (!TextUtils.isEmpty(callback) && mAsyncCallbackList != null) {
                for (JsAsyncCallbackListener listener : mAsyncCallbackList) {
                    listener.onCallbackFunction(callback, URLEncoder.encode(result));
                }
            }
        }
        return null;
    }
    
    public String qqJSCallBackGetContent(String param) {
        return mIsJsCallbackAsync ? null : URLEncoder.encode(getContentResult(param));
    }
    
    private String getContentResult(String param) {
        if (mCustomJSContentGetter != null) {
            String content = mCustomJSContentGetter.onGetContent(param);
            if (!TextUtils.isEmpty(content))
                return content;
        }
        
        if (CONTENT_TK.equals(param)) {
            // 获取token
            return BaseData.getUserToken();
        }
        else if (CONTENT_SI.equals(param)) {
            // 获取session
            return String.valueOf(BaseData.getUserSession());
        }
        else if (CONTENT_PLT.equals(param)) {
            // 获取平台
            return "android";
        }
        else if (CONTENT_AT.equals(param)) {
            // 获取应用类别
            switch (BaseData.getClientType()) {
                case AppCommon.AppType.qingqing_student:
                    return "student";
                case AppCommon.AppType.qingqing_teacher:
                    return "teacher";
                case AppCommon.AppType.qingqing_ta:
                    return "ta";
            }
        }
        else if (CONTENT_VER.equals(param)) {
            // 获取版本
            return PackageUtil.getVersionName();
        }
        else if (CONTENT_DID.equals(param)) {
            return DeviceUtil.getIdentification();
        }
        else if (CONTENT_DEVICE_INFO.equals(param)) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("appid", PackageUtil.getPackageName());
                obj.put("appversion", PackageUtil.getVersionName());
                obj.put("deviceid", DeviceUtil.getIdentification());
                obj.put("devicemodel", DeviceUtil.getDeviceModel());
                obj.put("devicetype", "android");
                obj.put("platform", AppUtil.getAppPlatformInternal());
                obj.put("osversion", DeviceUtil.getBuildVersion());
                obj.put("tunnel", PackageUtil.getChannel());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return obj.toString();
        }
        else if (CONTENT_NETWORK_STATUS.equals(param)) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("type", NetworkUtil.getConnectionTypeString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return obj.toString();
        }
        else if (CONTENT_CHECK_JS_API.equals(param)) {
            // 获取内容部分
            JSONArray jsonArray = new JSONArray();
            for (String method : mAvailableJsApiList) {
                jsonArray.put(method);
            }
            
            // js 回调方法部分
            for (String method : mCallbackMap.keySet()) {
                jsonArray.put(method);
            }
            
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("jsapi", jsonArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonObject.toString();
        }
        
        return "null";
    }
    
    private void initAvailableJsApiList() {
        Collections.addAll(mAvailableJsApiList, CONTENT_CHECK_JS_API, CONTENT_TK,
                CONTENT_SI, CONTENT_PLT, CONTENT_AT, CONTENT_VER, CONTENT_DID,
                CONTENT_DEVICE_INFO, CONTENT_NETWORK_STATUS);
    }
    
    public void clearCallback(String fragmentId) {
        mCallbackMap.remove(fragmentId);
    }
    
    public interface JSCallback {
        void onCallback(String method, String param);
    }
    
    public interface JsAsyncCallbackListener {
        void onCallbackFunction(String method, String param);
    }
    
    public interface CustomerJSContentGetter {
        String onGetContent(String param);
    }
    
    public class JsAsyncMethodCallback {
        public String id;
        public String method;
        public String success;
        public String fail;
        public String cancel;
    }
}

package com.qingqing.base.data;

import android.text.TextUtils;

import com.qingqing.api.proto.v1.UserProto;
import com.qingqing.base.cookie.BaseCookieManager;
import com.qingqing.base.http.HttpManager;
import com.qingqing.base.log.Logger;

/**
 * <p>
 * 这里只保存 用户登录后返回的数据
 * </p>
 *
 * user name
 *
 * user token
 *
 * user id
 *
 * session id
 *
 * qing qing user id
 *
 * user second id
 * 
 * @author tanwei
 *
 */
public class BaseData {
    
    public static final String INVALID_USER_ID = "-1";
    
    /** 登录成功之后的通用返回 */
    private static final String SP_KEY_USER_NAME = "key_ss_uname";
    private static final String SP_KEY_USER_ID = "key_ss_uid";
    private static final String SP_KEY_USER_TOKEN = "key_ss_utoken";
    private static final String SP_KEY_USER_SESSION = "key_ss_usession";
    private static final String SP_KEY_USER_QING_ID = "key_ss_uquid";
    private static final String SP_KEY_USER_SECOND_ID = "key_ss_usid";
    private static final String SP_KEY_LAST_UPDATE_CONTACTS_LIST_TIME = "last_update_contacts_time";
    
    private static BaseData sInstance;
    private SPWrapper mSPWrapper;
    private static final String SP_NAME = "acc_opt";
    private static final String TAG = "BaseData";
    
    // -----------------------注册登录返回的数据
    /** 用户token */
    protected String mUserToken;
    /** 用户id */
    protected long mUserID;
    /** session id */
    protected String mSessionID;
    /** qingqing user id */
    protected String mQingQingUserID;
    /** user second id */
    protected String mUserSecondID;
    
    private long lastUpdateContactsListTime;

    /** 用户类型 */
    private int userRoleType;

    public static boolean isForeground = false;
    
    private BaseData() {
        mSPWrapper = new SPWrapper(SP_NAME);
        loadLoginInfo();
    }
    
    public static BaseData getInstance() {
        if (sInstance == null) {
            synchronized (BaseData.class) {
                if (sInstance == null) {
                    sInstance = new BaseData();
                }
            }
        }
        return sInstance;
    }
    
    public SPWrapper getSPWrapper() {
        return mSPWrapper;
    }
    
    private void loadLoginInfo() {
        mUserToken = mSPWrapper.getString(SP_KEY_USER_TOKEN, "");
        mUserID = mSPWrapper.getLong(SP_KEY_USER_ID, 0);
        mSessionID = mSPWrapper.getString(SP_KEY_USER_SESSION, "0");
        mQingQingUserID = mSPWrapper.getEncryptString(SP_KEY_USER_QING_ID, "");
        mUserSecondID = mSPWrapper.getString(SP_KEY_USER_SECOND_ID, "");
        
        lastUpdateContactsListTime = mSPWrapper
                .getLong(SP_KEY_LAST_UPDATE_CONTACTS_LIST_TIME);
    }
    
    public static int getClientType() {
        return DefaultDataCache.INSTANCE().getAppType();
    }
    
    private Class<?> mForegroundMsgReceiverClass;
    
    public static void setForegroundMsgReceiverClass(Class<?> receiverClass) {
        getInstance().mForegroundMsgReceiverClass = receiverClass;
    }
    
    public static Class<?> getForegroundMsgReceiverClass() {
        return getInstance().mForegroundMsgReceiverClass;
    }
    
    public static long getUserId() {
        return getInstance().mUserID;
    }
    
    public static boolean isUserIDValid() {
        return !INVALID_USER_ID.equals(getSafeUserId());
    }
    
    public static String getSafeUserId() {
        if (TextUtils.isEmpty(BaseData.qingqingUserId()))
            return INVALID_USER_ID;
        else
            return BaseData.qingqingUserId();
    }
    
    public static long getUserSession() {
        long session = 0;
        try {
            session = Long.parseLong(getInstance().mSessionID);
        } catch (Exception e) {
            Logger.w(TAG, "user session not long :" + getInstance().mSessionID);
        }
        return session;
    }
    
    public static String getUserName() {
       return  SPManager.getString(SP_KEY_USER_NAME, "");
    }

    public static void saveUserName(String userName){
        SPManager.put(SP_KEY_USER_NAME, userName);
    }
    
    public static String getUserToken() {
        return getInstance().mUserToken;
    }
    
    public static String qingqingUserId() {
        return getInstance().mQingQingUserID;
    }
    
    public static String getUserSecondId() {
        return getInstance().mUserSecondID;
    }
    
    public static boolean isTimeExpand() {
        return DefaultDataCache.INSTANCE().isTimeExpand();
    }
    
    public static long getLastUpdateContactsListTime() {
        return getInstance().lastUpdateContactsListTime;
    }
    
    public static void setLastUpdateContactsListTime(long lastUpdateContactsListTime) {
        getInstance().lastUpdateContactsListTime = lastUpdateContactsListTime;
        getInstance().mSPWrapper.put(SP_KEY_LAST_UPDATE_CONTACTS_LIST_TIME,
                lastUpdateContactsListTime);
    }

    /**
     * 是否是测试用户
     */
    public static boolean isTestUser() {
        return getInstance().userRoleType == UserProto.UserRoleType.test_user_role_type;
    }
    
    public static int getUserRoleType() {
        return getInstance().userRoleType;
    }
    
    public void setUserRoleType(int userType) {
        userRoleType = userType;
    }

    public void saveToLocal(String token, long userID, String session,
                            String qingUserID, String secondID) {
        mUserToken = token;
        mUserID = userID;
        mSessionID = session;
        mQingQingUserID = qingUserID;
        mUserSecondID = secondID;
        
        mSPWrapper.put(SP_KEY_USER_TOKEN, mUserToken);
        mSPWrapper.put(SP_KEY_USER_ID, mUserID);
        mSPWrapper.put(SP_KEY_USER_SESSION, mSessionID);
        mSPWrapper.putEncryptString(SP_KEY_USER_QING_ID, mQingQingUserID);
        mSPWrapper.put(SP_KEY_USER_SECOND_ID, mUserSecondID);
        
        BaseCookieManager.setUserCookie(token, userID, session, qingUserID, secondID);
    }
    
    /** 跟用户相关的基础信息，全部清除 */
    public void clear() {
        mUserSecondID = mQingQingUserID = mUserToken = "";
        mUserID = 0;
        mSessionID = "0";
        userRoleType = UserProto.UserRoleType.normal_user_role_type;
        mSPWrapper.clear();
        HttpManager.instance().session(0);
        BaseCookieManager.removeUserCookie();
    }
}

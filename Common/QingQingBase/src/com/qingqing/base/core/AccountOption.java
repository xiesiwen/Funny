package com.qingqing.base.core;

import java.util.ArrayList;
import java.util.List;

import com.google.protobuf.nano.MessageNano;
import com.qingqing.api.proto.ta.v1.WorkTaskForTa;
import com.qingqing.api.proto.v1.AssistantProto;
import com.qingqing.base.data.DefaultDataCache;
import com.qingqing.base.utils.PBUtil;

import android.text.TextUtils;

/**
 * Created by Wangxiaxin on 2015/11/2.
 * <p/>
 * 用户设置信息
 */
public final class AccountOption extends AbsAccountOption {
    
    /** 用于存储base info */
    private static final String SP_KEY_RSP_BASEINFO = "key_rsp_bi";
    /** BaseInfo 的返回数据 */
    private AssistantProto.AssistantBaseInfoResponse mBaseInfoRsp = new AssistantProto.AssistantBaseInfoResponse();
    
    private static volatile AccountOption sInstance;
    
    // -----------------------Simple user info V2 的接口数据
    /**
     * 拥有的角色列表
     */
    private List<Integer> mAuthList = new ArrayList<>();

    /**
     * 是否boss 首页显示boss专属大数据实验室的时候用到
     */
    private boolean mIsBoss = false;

    // -----------------------TA角色类型: TA/TAM/TAD
    /**
     * 当前角色
     */
    private int mCurrentAuthLevel = AssistantProto.AssistantAuthType.ta_assistant_auth_type;
    
    /**
     * 是否已手动设置过角色
     */
    private boolean mIsManuallyModifiedAuthLevel = false;
    
    private ArrayList<WorkTaskForTa.WorkTaskTopicDetail> workBenchTaskTopicList = new ArrayList<>();
    private WorkTaskForTa.WorkTaskTopicDetail workBenchTaskTopicAll;
    
    private AccountOption() {
        loadFromLocal();
        
        if (workBenchTaskTopicAll == null) {
            workBenchTaskTopicAll = new WorkTaskForTa.WorkTaskTopicDetail();
            workBenchTaskTopicAll.taskId = 0;
            workBenchTaskTopicAll.hasTaskId = true;
            workBenchTaskTopicAll.hasTaskTopic = true;
            workBenchTaskTopicAll.taskTopic = "全部任务主题";
        }
    }
    
    public static AccountOption INSTANCE() {
        if (sInstance == null) {
            synchronized (AccountOption.class) {
                if (sInstance == null) {
                    sInstance = new AccountOption();
                }
            }
        }
        return sInstance;
    }
    
    private void loadFromLocal() {
        mBaseInfoRsp = (AssistantProto.AssistantBaseInfoResponse) PBUtil.loadMsg(
                mSPWrapper, SP_KEY_RSP_BASEINFO,
                AssistantProto.AssistantBaseInfoResponse.class);
        
        if (mBaseInfoRsp != null && mBaseInfoRsp.assistantBaseInfo != null
                && mBaseInfoRsp.assistantBaseInfo.authType != null) {
            setAuthType(mBaseInfoRsp.assistantBaseInfo.authType);
            DefaultDataCache.INSTANCE().reqGradeCourseList(getCityID());
        }
    }
    
    private void saveToLocal() {
        PBUtil.saveMsg(mSPWrapper, SP_KEY_RSP_BASEINFO, mBaseInfoRsp);
    }
    
    /**
     * 获取头像icon
     */
    public String getHeadIcon() {
        if (mBaseInfoRsp != null && mBaseInfoRsp.assistantBaseInfo != null
                && mBaseInfoRsp.assistantBaseInfo.assistantLimitInfo != null
                && mBaseInfoRsp.assistantBaseInfo.assistantLimitInfo.userInfo != null) {
            return TextUtils.isEmpty(
                    mBaseInfoRsp.assistantBaseInfo.assistantLimitInfo.userInfo.newHeadImage)
                            ? ""
                            : mBaseInfoRsp.assistantBaseInfo.assistantLimitInfo.userInfo.newHeadImage;
        }
        else {
            return "";
        }
    }
    
    /**
     * 获取拥有的 TA 角色列表
     *
     * @return 拥有的 TA 角色列表
     */
    public List<Integer> getAuthList() {
        return mAuthList;
    }
    
    /**
     * 获取当前 TA 角色类型
     *
     * @return 当前 TA 角色类型
     */
    public int getCurrentAuthLevel() {
        return mCurrentAuthLevel;
    }
    
    /**
     * 设置当前 TA 角色类型
     *
     * @param authType
     *            TA 角色类型
     */
    public void setCurrentAuthLevel(Integer authType) {
        mCurrentAuthLevel = authType;
        mIsManuallyModifiedAuthLevel = true;
    }
    
    /**
     * 根据指定的 TA 角色类型，获取相应的字符串
     *
     * @param authType
     *            指定的 TA 角色类型
     * @return 相应的字符串
     */
    public String getAuthName(Integer authType) {
        String authName = "";
        
        switch (authType) {
            case AssistantProto.AssistantAuthType.ta_assistant_auth_type:
                authName = "TA";
                break;
            case AssistantProto.AssistantAuthType.tam_assistant_auth_type:
                authName = "TAM";
                break;
            case AssistantProto.AssistantAuthType.tad_assistant_auth_type:
                authName = "TAD";
                break;
        }
        
        return authName;
    }
    
    /**
     * 获取昵称
     */
    public String getNickName() {
        if (mBaseInfoRsp != null && mBaseInfoRsp.assistantBaseInfo != null
                && mBaseInfoRsp.assistantBaseInfo.assistantLimitInfo != null
                && mBaseInfoRsp.assistantBaseInfo.assistantLimitInfo.userInfo != null) {
            return TextUtils.isEmpty(
                    mBaseInfoRsp.assistantBaseInfo.assistantLimitInfo.userInfo.nick) ? ""
                            : mBaseInfoRsp.assistantBaseInfo.assistantLimitInfo.userInfo.nick;
        }
        else {
            return "";
        }
    }
    
    // /**
    // * 设置昵称
    // */
    // public AccountOption setNickName(String name) {
    // mBaseInfoRsp.assistantBaseInfo.assistantLimitInfo.userInfo.nick = name;
    // return this;
    // }
    
    /**
     * 获取城市id
     */
    public int getCityID() {
        if (mBaseInfoRsp != null && mBaseInfoRsp.assistantBaseInfo != null
                && mBaseInfoRsp.assistantBaseInfo.hasCityId
                && mBaseInfoRsp.assistantBaseInfo.cityId > 0) {
            return mBaseInfoRsp.assistantBaseInfo.cityId;
        }
        else {
            return 0;
        }
    }
    
    // /**
    // * 设置城市id
    // */
    // public AccountOption setCityID(int cityid) {
    // if (cityid > 0) {
    // mBaseInfoRsp.assistantBaseInfo.cityId = cityid;
    // }
    // return this;
    // }
    
    public void saveBaseInfo(MessageNano result) {
        
        if (result == null)
            return;
        
        mBaseInfoRsp = (AssistantProto.AssistantBaseInfoResponse) result;
        saveToLocal();
        
        setAuthType(mBaseInfoRsp.assistantBaseInfo.authType);
        DefaultDataCache.INSTANCE().reqGradeCourseList(getCityID());
    }
    
    private void setAuthType(int[] authList) {
        mAuthList.clear();
        mIsBoss = false;
        for (int anAuthList : authList) {
            // 只保存 ta tam tad 三种身份，其余过滤
            if (anAuthList == AssistantProto.AssistantAuthType.ta_assistant_auth_type
                    || anAuthList == AssistantProto.AssistantAuthType.tam_assistant_auth_type
                    || anAuthList == AssistantProto.AssistantAuthType.tad_assistant_auth_type) {
                mAuthList.add(anAuthList);
            }else if (anAuthList == AssistantProto.AssistantAuthType.boss_assistant_auth_type){
                mIsBoss = true;
            }
        }
        
        // 已经手动修改过角色类型，不取最高
        if (mIsManuallyModifiedAuthLevel) {
            return;
        }
        
        mCurrentAuthLevel = AssistantProto.AssistantAuthType.ta_assistant_auth_type;
        // 当前角色类型，优先取高权限的
        for (Integer authType : mAuthList) {
            if (authType == AssistantProto.AssistantAuthType.tad_assistant_auth_type) {
                mCurrentAuthLevel = AssistantProto.AssistantAuthType.tad_assistant_auth_type;
            }
            else if (authType == AssistantProto.AssistantAuthType.tam_assistant_auth_type
                    && mCurrentAuthLevel != AssistantProto.AssistantAuthType.tad_assistant_auth_type) {
                mCurrentAuthLevel = AssistantProto.AssistantAuthType.tam_assistant_auth_type;
            }
        }
    }

    public void clear() {
        mBaseInfoRsp = new AssistantProto.AssistantBaseInfoResponse();
        mCurrentAuthLevel = AssistantProto.AssistantAuthType.ta_assistant_auth_type;
        mIsManuallyModifiedAuthLevel = false;
        mAuthList.clear();
        mIsBoss = false;
    }
}

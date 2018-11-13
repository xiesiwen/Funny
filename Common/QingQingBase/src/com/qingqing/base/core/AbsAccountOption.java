package com.qingqing.base.core;

import com.qingqing.base.data.BaseData;
import com.qingqing.base.data.SPWrapper;

/**
 * Created by Wangxiaxin on 2015/12/2.
 *
 * 抽象 用户信息类，主要用来存放和登录相关的信息
 */
public abstract class AbsAccountOption {
    
    public enum ReqInfoState {
        DEFAULT, INIT, REQING, DONE, FAIL
    }
    
    protected SPWrapper mSPWrapper;
    protected final String TAG = "AccountOption";
    
    protected AbsAccountOption() {
        mSPWrapper = BaseData.getInstance().getSPWrapper();
    }
}

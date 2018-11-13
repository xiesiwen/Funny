package com.qingqing.qingqingbase.ui;

import android.graphics.drawable.Drawable;
import android.view.View;

import com.qingqing.base.BaseApplication;
import com.qingqing.base.http.HttpManager;
import com.qingqing.base.http.HttpUrl;
import com.qingqing.base.ui.AbstractFragment;
import com.qingqing.base.view.Toolbar;

/**
 * Fragment 基础类
 */
public abstract class BaseFragment extends AbstractFragment {
    
    @Override
    public void onDestroy() {
        HttpManager.instance().cancelLogicRequest(mReqTag);
        super.onDestroy();
    }
    
    @Override
    public void onPause() {
        super.onPause();
        if (BaseApplication.getOnPageStatisticListener() != null) {
            BaseApplication.getOnPageStatisticListener().onPause(this);
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (BaseApplication.getOnPageStatisticListener() != null) {
            BaseApplication.getOnPageStatisticListener().onResume(this);
        }
    }
    
    /**
     * 获取 ProtoReq
     */
    public com.qingqing.base.http.req.ProtoReq newProtoReq(HttpUrl url) {
        com.qingqing.base.http.req.ProtoReq req = new com.qingqing.base.http.req.ProtoReq(
                url);
        req.setReqTag(mReqTag);
        return req;
    }
    
}

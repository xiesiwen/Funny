package com.qingqing.qingqingbase.ui;

import com.qingqing.base.BaseApplication;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.data.StatisticalDataConstants;
import com.qingqing.base.http.HttpManager;
import com.qingqing.base.http.HttpUrl;
import com.qingqing.base.hybrid.SchemeUtil;
import com.qingqing.base.ui.AbstractActivity;

/**
 * @author huangming date 2015-6-26
 */
public abstract class BaseActivity extends AbstractActivity {

    private boolean isReceiveMsg = true;// false 时只处理登出状态

    private static final String TAG = "AbstractActivity";
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // callbacks
        HttpManager.instance().cancelLogicRequest(mReqTag);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (BaseApplication.getOnPageStatisticListener() != null) {
            BaseApplication.getOnPageStatisticListener().onPause(this);
        }
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        if (!BaseData.isForeground) {
            // app 从后台唤醒，进入前台
            BaseData.isForeground = true;
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (BaseApplication.getOnPageStatisticListener() != null) {
            BaseApplication.getOnPageStatisticListener().onResume(this);
        }
    }
    
    protected void disableMsgReceiver() {
        isReceiveMsg = false;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (SchemeUtil.hasScheme()) {
            SchemeUtil.invokeByH5(this);
        }
    }
    
    /**
     * 获取 ProtoReq 2.0
     */
    public com.qingqing.base.http.req.ProtoReq newProtoReq(HttpUrl url) {
        com.qingqing.base.http.req.ProtoReq req = new com.qingqing.base.http.req.ProtoReq(
                url);
        req.setReqTag(mReqTag);
        return req;
    }
}

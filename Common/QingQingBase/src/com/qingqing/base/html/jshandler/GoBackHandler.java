package com.qingqing.base.html.jshandler;

/**
 * Created by xiejingwen on 2017/11/9.
 */

public class GoBackHandler extends AbstractJSHandler {
    
    @Override
    public String getJSMethodName() {
        return JSHandlerDispatcher.CB_GOBACK;
    }
    
    @Override
    public void handleJSMethod(String methodName, String params) {
        if (mHtmlFragment.couldOperateUI()) {
            mHtmlFragment.getActivity().onBackPressed();
        }
    }
}

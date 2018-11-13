package com.qingqing.base.html.jshandler;

/**
 * Created by xiejingwen on 2017/11/9.
 */

public class ShowShareHandler extends AbstractJSHandler{

    @Override
    public String getJSMethodName() {
        return JSHandlerDispatcher.METHOD_SHOW_SHARE;
    }

    @Override
    public void handleJSMethod(String methodName, String params) {
        mHtmlFragment.mMenuManager.showMenuShareItem(params);
    }
}

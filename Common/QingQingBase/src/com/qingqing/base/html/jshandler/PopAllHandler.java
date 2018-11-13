package com.qingqing.base.html.jshandler;

/**
 * Created by xiejingwen on 2017/11/9.
 */

public class PopAllHandler extends AbstractJSHandler{

    @Override
    public String getJSMethodName() {
        return JSHandlerDispatcher.CB_POPALL;
    }

    @Override
    public void handleJSMethod(String methodName, String params) {
        mHtmlFragment.mForceQuit = true;
                if (mHtmlFragment.couldOperateUI()) {
                    mHtmlFragment.getActivity().onBackPressed();
                }
    }
}

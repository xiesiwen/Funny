package com.qingqing.base.html.jshandler;

/**
 * Created by xiejingwen on 2017/11/9.
 */

public class HideMenuTextHandler extends ShowMenuTextHandler{

    @Override
    public String getJSMethodName() {
        return JSHandlerDispatcher.METHOD_HIDE_MENU_TEXT;
    }

    @Override
    public void handleJSMethod(String methodName, String params) {
        mHtmlFragment.mMenuManager.showMenuTextItem("");
    }
}

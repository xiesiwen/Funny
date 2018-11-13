package com.qingqing.base.html.jshandler;

import com.qingqing.base.html.HtmlFragment;
import com.qingqing.base.view.html.BaseJSWebView;

/**
 * Created by xiejingwen on 2017/11/9.
 */

public interface BaseJSHandler {
    
    void initHandlerOnViewCreated(BaseJSWebView webView, HtmlFragment htmlFragment);
    
    String getJSMethodName();
    
    void handleJSMethod(String methodName, String params);

    void onDetach();
}

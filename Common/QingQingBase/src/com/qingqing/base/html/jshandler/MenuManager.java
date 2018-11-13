package com.qingqing.base.html.jshandler;

import com.qingqing.base.html.HtmlFragment;

import android.view.Menu;
import android.view.MenuItem;

/**
 * Created by xiejingwen on 2017/11/9.
 */

public class MenuManager {
    private Menu mMenu;
    private JSHandlerDispatcher mJSHandlerDispatcher;
    private HtmlFragment mHtmlFragment;

    public MenuManager(Menu menu, JSHandlerDispatcher jsHandlerDispatcher) {
        mMenu = menu;
        mJSHandlerDispatcher = jsHandlerDispatcher;
        mHtmlFragment = mJSHandlerDispatcher.getHtmlFragment();
    }


    public void onOptionsItemSelected(MenuItem item) {
        int groupId = item.getGroupId();
    }

    // 分享页面
    public void share() {
    }


    public void loadPageCache(String url) {
    }
    
    private void refreshMenuItem() {
    }



    public void showMenuCallbackItem(String method, String param) {
    }

    public void showMenuTextItem(String param) {
    }

    public void showMenuShareItem(String param) {
    }

    private void preloadShareIcon() {
    }

    public void addMenuNativePageCache(String url, MenuItem menuItem) {
    }

}

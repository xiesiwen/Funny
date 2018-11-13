package com.qingqing.base.im.ui;

import android.os.Bundle;

import com.easemob.easeui.R;
import com.qingqing.qingqingbase.ui.BaseActivity;

/**
 * Created by xiejingwen on 2017/8/7.
 */

public class SearchActivity extends BaseActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_fragment);
        SearchFragment searchFragment = new SearchFragment();
        if(getIntent() != null){
            searchFragment.setArguments(getIntent().getExtras());
        }
        mFragAssist.setGroupID(R.id.full_screen_fragment_container);
        mFragAssist.setBottom(searchFragment);
        setStatusBarColor(R.color.white, false);
    }

}

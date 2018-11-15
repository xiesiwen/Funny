package com.qingqing.funnyman.ui.main;

import android.os.Bundle;

import com.qingqing.funnyman.R;
import com.qingqing.qingqingbase.ui.BaseActivity;

public class MainActivity extends BaseActivity {
    private VideoFragment mVideoFragment;
    private MessageFragment mMessageFragment;
    private ShotFragment mShotFragment;
    private DiscoveryFragment mDiscoveryFragment;
    private MeFragment mMeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

}

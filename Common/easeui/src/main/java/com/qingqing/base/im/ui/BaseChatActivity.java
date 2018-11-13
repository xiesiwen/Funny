package com.qingqing.base.im.ui;

import android.content.Intent;
import android.os.Bundle;

import com.easemob.chat.EMChatManager;
import com.easemob.easeui.R;
import com.easemob.easeui.ui.EaseBaseActivity;
import com.qingqing.api.proto.v1.app.AppCommon;
import com.qingqing.base.core.UserBehaviorLogManager;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.data.StatisticalDataConstants;
import com.qingqing.base.im.ChatManager;
import com.qingqing.base.im.Constant;

/**
 * Created by huangming on 2015/12/24.
 * <p/>
 * LauncherMode:singleTop
 */
public abstract class BaseChatActivity extends EaseBaseActivity {

    protected BaseChatFragment chatFragment;
    protected String toChatUsername;
    protected int chatType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        if (!EMChatManager.getInstance().isConnected()) {
            ChatManager.getInstance().login(true, null);
        }
        //聊天人或群id
        toChatUsername = getIntent().getExtras().getString(Constant.EXTRA_USER_ID);
        chatType = getIntent().getExtras().getInt(Constant.EXTRA_CHAT_TYPE, Constant.CHATTYPE_SINGLE);
        chatFragment = createChatFragment();
        //传入参数
        chatFragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_chat_layout, chatFragment).commit();

    }

    protected abstract BaseChatFragment createChatFragment();

    @Override
    protected void onResume() {
        super.onResume();
        switch (BaseData.getClientType()) {
            case AppCommon.AppType.qingqing_student:
                UserBehaviorLogManager.INSTANCE().savePageLog(StatisticalDataConstants.LOG_PAGE_IM_DETAIL);
                break;
            case AppCommon.AppType.qingqing_teacher:
                UserBehaviorLogManager.INSTANCE().savePageLog(StatisticalDataConstants.LOG_PAGE_TEACHER_IM_DETAIL);
                break;
            case AppCommon.AppType.qingqing_ta:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // 点击notification bar进入聊天页面，保证只有一个聊天页面
        String username = intent.getStringExtra(Constant.EXTRA_USER_ID);
        if (toChatUsername.equals(username))
            super.onNewIntent(intent);
        else {
            finish();
            startActivity(intent);
        }

    }

    @Override
    public void onBackPressed() {
        chatFragment.onBackPressed();
    }

    public String getToChatUsername() {
        return toChatUsername;
    }

}

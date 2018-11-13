package com.qingqing.base.nim.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.WindowManager;

import com.easemob.easeui.R;
import com.qingqing.api.proto.v1.app.AppCommon;
import com.qingqing.qingqingbase.ui.BaseActionBarActivity;
import com.qingqing.base.core.UserBehaviorLogManager;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.data.StatisticalDataConstants;
import com.qingqing.base.nim.domain.ChatManager;
import com.qingqing.base.nim.utils.Constants;
import com.qingqing.base.nim.domain.ChatType;

/**
 * Created by huangming on 2016/8/23.
 */
public abstract class BaseChatActivity extends BaseActionBarActivity {
    
    private BaseChatFragment chatFragment;
    private String conversationId;
    private ChatType chatType;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_chat);
        
        onInitializeArguments(getIntent().getExtras());
        
        chatFragment = createChatFragment();
        chatFragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_chat_layout, chatFragment).commit();
    }
    
    protected BaseChatFragment getChatFragment() {
        return chatFragment;
    }
    
    protected abstract BaseChatFragment createChatFragment();
    
    protected ChatType getChatType() {
        return chatType;
    }
    
    private void setChatType(ChatType chatType) {
        this.chatType = chatType;
    }
    
    protected String getConversationId() {
        return conversationId;
    }
    
    protected void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
    
    protected void onInitializeArguments(Bundle bundle) {
        setConversationId(bundle.getString(Constants.EXTRA_CONVERSATION_ID));
        setChatType(
                ChatType.mapStringToValue(bundle.getString(Constants.EXTRA_CHAT_TYPE)));
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        ChatManager.getInstance().getNotifier().reset();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        switch (BaseData.getClientType()) {
            case AppCommon.AppType.qingqing_student:
                UserBehaviorLogManager.INSTANCE()
                        .savePageLog(StatisticalDataConstants.LOG_PAGE_IM_DETAIL);
                break;
            case AppCommon.AppType.qingqing_teacher:
                UserBehaviorLogManager.INSTANCE()
                        .savePageLog(StatisticalDataConstants.LOG_PAGE_TEACHER_IM_DETAIL);
                break;
            case AppCommon.AppType.qingqing_ta:
                break;
        }
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        // 点击notification bar进入聊天页面，保证只有一个聊天页面
        String newConversationId = intent.getStringExtra(Constants.EXTRA_CONVERSATION_ID);
        if (TextUtils.isEmpty(newConversationId)
                || newConversationId.equals(getConversationId()))
            super.onNewIntent(intent);
        else {
            finish();
            startActivity(intent);
        }
    }
    
    @Override
    public void onBackPressed() {
        if (getChatFragment() != null) {
            getChatFragment().onBackPressed();
        }
        else {
            super.onBackPressed();
        }
    }
}

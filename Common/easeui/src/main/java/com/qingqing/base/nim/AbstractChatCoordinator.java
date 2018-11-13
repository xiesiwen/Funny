package com.qingqing.base.nim;

import android.text.TextUtils;

import com.qingqing.base.BaseApplication;
import com.qingqing.base.interfaces.AbstractCoordinator;
import com.qingqing.base.nim.domain.ChatManager;
import com.qingqing.base.nim.domain.Message;

import java.util.List;

/**
 * Created by huangming on 2016/8/18.
 */
public abstract class AbstractChatCoordinator extends AbstractCoordinator
        implements ChatCoordinator {
    private String conversationId;
    private ChatCoordinationListener CoordinationListener;
    
    protected AbstractChatCoordinator(String conversationId) {
        this.conversationId = conversationId;
    }
    
    public String getConversationId() {
        return conversationId;
    }
    
    protected void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
    
    @Override
    protected void setInitialized(boolean initialized) {
        super.setInitialized(initialized);
        if (getCoordinationListener() != null) {
            if (initialized) {
                getCoordinationListener().onInitialized();
            }
            else {
                getCoordinationListener().onInitializeFailed();
            }
        }
    }
    
    @Override
    public ChatCoordinationListener getCoordinationListener() {
        return CoordinationListener;
    }
    
    @Override
    public void setCoordinationListener(ChatCoordinationListener coordinationListener) {
        CoordinationListener = coordinationListener;
    }
    
    @Override
    public void initialize() {
        onInitialize();
    }
    
    @Override
    public void destroy() {
        setDestroyed(true);
        onDestroy();
    }
    
    public String getCurrentUserId() {
        return ChatManager.getInstance().getCurrentUserId();
    }
    
    protected String getErrorHintMessage(String hintMessage, int msgResId) {
        return TextUtils.isEmpty(hintMessage)
                ? BaseApplication.getCtx().getString(msgResId) : hintMessage;
    }
    
    protected String getErrorHintMeesage(String hintMessage, String message) {
        return TextUtils.isEmpty(hintMessage) ? message : hintMessage;
    }
    
    protected abstract void onInitialize();
    
    protected abstract void onDestroy();
    
    @Override
    public List<Message> getConversationMessages() {
        return ChatManager.getInstance().getConversationService()
                .getConversationMessages(getConversationId());
    }
}

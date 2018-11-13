package com.qingqing.base.nim;

/**
 * Created by huangming on 2016/8/17.
 */
public class ChatRoomCoordinator extends AbstractChatCoordinator {
    
    public ChatRoomCoordinator(String conversationId) {
        super(conversationId);
    }
    
    public String getChatRoomId() {
        return getConversationId();
    }
    
    @Override
    public ChatRoomCoordinationListener getCoordinationListener() {
        return (ChatRoomCoordinationListener) super.getCoordinationListener();
    }

    @Override
    protected void onInitialize() {

    }

    @Override
    protected void onDestroy() {
    }

}

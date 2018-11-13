package com.qingqing.base.nim.ui;

import com.qingqing.base.nim.ChatCoordinationListener;
import com.qingqing.base.nim.ChatRoomCoordinationListener;
import com.qingqing.base.nim.ChatRoomCoordinator;

/**
 * Created by huangming on 2016/8/17.
 */
public class BaseChatRoomFragment extends BaseChatFragment
        implements ChatRoomCoordinationListener {
    
    @Override
    protected ChatRoomCoordinator createCoordinator() {
        return new ChatRoomCoordinator(getConversationId());
    }
    
    @Override
    protected ChatCoordinationListener createChatCoordinationListener() {
        return this;
    }
    
    @Override
    protected ChatRoomCoordinator getCoordinator() {
        return (ChatRoomCoordinator) super.getCoordinator();
    }
}

package com.qingqing.base.nim;

import com.qingqing.base.interfaces.Coordinator;
import com.qingqing.base.nim.domain.Message;

import java.util.List;

/**
 * Created by huangming on 2016/8/17.
 *
 * 初始化，退出，并且监听会话是否断开
 */
public interface ChatCoordinator extends Coordinator {
    
    String getConversationId();
    
    ChatCoordinationListener getCoordinationListener();
    
    void setCoordinationListener(ChatCoordinationListener listener);

    List<Message> getConversationMessages();

}

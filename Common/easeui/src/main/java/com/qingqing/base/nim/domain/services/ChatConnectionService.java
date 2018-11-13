package com.qingqing.base.nim.domain.services;

import com.qingqing.base.nim.domain.ChatConnectionListener;

/**
 * Created by huangming on 2016/9/6.
 */
public interface ChatConnectionService {
    
    void addChatConnectionListener(ChatConnectionListener listener);
    
    void removeChatConnectionListener(ChatConnectionListener listener);
    
}

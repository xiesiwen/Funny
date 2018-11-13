package com.qingqing.base.nim.domain;

import java.util.List;

/**
 * Created by huangming on 2016/8/22.
 */
public interface MessageReceiver {
    
    void onReceive(Message message);
    
    void onReceive(List<Message> messageList);
    
}

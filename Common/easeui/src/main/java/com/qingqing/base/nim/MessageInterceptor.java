package com.qingqing.base.nim;

import com.qingqing.base.nim.domain.Message;

import java.util.List;

/**
 * Created by huangming on 2016/8/27.
 */
public interface MessageInterceptor {
    
    boolean onInterceptMessage(Message message);

    boolean onInterceptMessages(List<Message> messages);
    
}

package com.qingqing.base.nim.domain;

import com.qingqing.base.spec.Spec;

import java.util.List;

/**
 * Created by huangming on 2016/8/26.
 */
public interface MessageRepository {
    
    Message getMessageBy(Spec<Message> spec);
    
    List<Message> getMessagesBy(Spec<Message> spec);
    
}

package com.qingqing.base.nim.domain;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by huangming on 2016/8/23.
 */
abstract class AbstractMessageDispatcher implements MessageDispatcher {
    
    private Set<MessageReceiver> receiverSet = new HashSet<>(1);
    
    @Override
    public void dispatchMessage(final Message message) {
        dispatchMessageInternal(message);
    }
    
    @Override
    public void dispatchMessages(final List<Message> messageList) {
        dispatchMessagesInternal(messageList);
    }
    
    private void dispatchMessageInternal(Message message) {
        for (MessageReceiver receiver : receiverSet) {
            receiver.onReceive(message);
        }
    }
    
    private void dispatchMessagesInternal(List<Message> messageList) {
        for (MessageReceiver receiver : receiverSet) {
            receiver.onReceive(messageList);
        }
    }
    
    @Override
    public void registerReceiver(MessageReceiver receiver) {
        receiverSet.add(receiver);
    }
    
    @Override
    public void unregisterReceiver(MessageReceiver receiver) {
        receiverSet.remove(receiver);
    }

    @Override
    public void unregisterAll() {
        receiverSet.clear();
    }
}

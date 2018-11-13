package com.qingqing.base.nim.domain;

import com.qingqing.base.interfaces.Delivery;

import java.util.List;

/**
 * Created by huangming on 2016/9/9.
 */
public class MessageDelivery implements Delivery<Message> {

    private final MessageManager messageManager;

    MessageDelivery(MessageManager messageManager) {
        this.messageManager = messageManager;
    }

    protected MessageManager getMessageManager() {
        return messageManager;
    }

    @Override
    public void post(Message message) {
        getMessageManager().notifyMessageAdded(message);
    }

    @Override
    public void postBatch(List<Message> messages) {
        getMessageManager().notifyMessagesAdded(messages);
    }
}

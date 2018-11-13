package com.qingqing.base.nim.domain;

import com.qingqing.base.nim.utils.MessageUtils;
import com.qingqing.base.utils.ExecUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by huangming on 2016/8/22.
 *
 * 补全控制器
 */
class CompletionController implements MessageReceiver {
    
    private static final String TAG = "CompletionController";
    
    // 最后处理的MessageReceiver，接收到的消息将被分发显示
    private final MessageReceiver finalReceiver;
    
    private AtomicInteger handlerIndex = new AtomicInteger();
    
    Map<String, MessagePacketHandler> handlers = Collections
            .synchronizedMap(new HashMap<String, MessagePacketHandler>());
    
    CompletionController(MessageReceiver finalReceiver) {
        this.finalReceiver = finalReceiver;
    }
    
    private MessageReceiver getFinalReceiver() {
        return finalReceiver;
    }
    
    private Map<String, MessagePacketHandler> getHandlers() {
        return handlers;
    }

    private AtomicInteger getHandlerIndex() {
        return handlerIndex;
    }

    @Override
    public void onReceive(Message message) {
        handleNewMessage(message);
    }
    
    @Override
    public void onReceive(List<Message> messageList) {
        handleNewMessages(messageList);
    }
    
    private void handleNewMessage(final Message message) {
        ExecUtil.execute(ExecUtil.MODE_COMPUTE, new Runnable() {
            @Override
            public void run() {
                pushMessageToHandler(message);
            }
        });
    }
    
    private void pushMessageToHandler(final Message message) {
        String conversationId = MessageUtils.getConversationId(message);
        MessagePacketHandler handler = getHandlers().get(conversationId);
        if (handler != null) {
            handler.push(message);
        }
    }
    
    private void handleNewMessages(final List<Message> messageList) {
        ExecUtil.execute(ExecUtil.MODE_COMPUTE,  new Runnable() {
            @Override
            public void run() {
                for (Message message : messageList) {
                    pushMessageToHandler(message);
                }
            }
        });
    }
    
    void startCompletionHandler(String conversationId, ChatType chatType) {
        if (!getHandlers().containsKey(conversationId)) {
            MessagePacketHandler handler = new MessagePacketHandler(conversationId,
                    chatType, getHandlerIndex().incrementAndGet());
            handler.registerReceiver(getFinalReceiver());
            getHandlers().put(conversationId, handler);
        }
    }
    
    void stopCompletionHandler(String conversationId) {
        MessagePacketHandler handler = getHandlers().get(conversationId);
        if (handler != null) {
            getHandlers().remove(conversationId);
            handler.stop();
        }
    }
}

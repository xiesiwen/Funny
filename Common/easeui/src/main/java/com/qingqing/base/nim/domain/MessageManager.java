package com.qingqing.base.nim.domain;

import com.qingqing.base.log.Logger;
import com.qingqing.base.nim.domain.services.MessageService;
import com.qingqing.base.utils.ExecUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by huangming on 2016/8/17.
 *
 * Controller : 管理消息的删除与添加，操作数据库（待做）
 */
class MessageManager implements MessageService , MessageReceiver {
    
    private static final String TAG = "whuthmMessageManager";
    
    private final MessagesHolder holder = new MessagesHolder();
    private ConversationManager conversationManager;
    
    private List<WeakReference<Listener>> listeners = new ArrayList<>();
    
    private Map<ChatType, MessageSender> senderMap = new HashMap<>();
    private MessageSender errorSender;
    
    private final MessageBodyLoader bodyLoader;
    
    private MessageDelivery messageDelivery;
    
    MessageManager(MessageBodyLoader bodyLoader) {
        this.bodyLoader = bodyLoader;
        senderMap.put(ChatType.ChatRoom, new ChatRoomMessageSender(this));
        errorSender = new ErrorMessageSender(this);
    }
    
    private MessageDelivery getMessageDelivery() {
        return messageDelivery;
    }
    
    void setMessageDelivery(MessageDelivery messageDelivery) {
        this.messageDelivery = messageDelivery;
    }
    
    private MessageBodyLoader getBodyLoader() {
        return bodyLoader;
    }
    
    private MessagesHolder getHolder() {
        return holder;
    }
    
    private MessageSender getMessageSender(ChatType chatType) {
        MessageSender sender = senderMap.get(chatType);
        if (sender == null) {
            return errorSender;
        }
        return sender;
    }
    
    private ConversationManager getConversationManager() {
        return conversationManager;
    }
    
    void setConversationManager(ConversationManager conversationManager) {
        this.conversationManager = conversationManager;
    }
    
    private void addMessage(final Message message) {
        runOnWorkThread(new Runnable() {
            @Override
            public void run() {
                addMessageInternal(message);
            }
        });
    }
    
    private boolean addMessageInternal(Message message) {
        Logger.i(TAG, "addMessageInternal : " + message.getId());
        if (!getHolder().containsMessage(message)) {
            getConversationManager().addMessage(message);
            getHolder().addMessage(message);
            getBodyLoader().loadMessageBody(message);
            if (getMessageDelivery() != null) {
                getMessageDelivery().post(message);
            }
            return true;
        }
        return false;
    }
    
    void addMessages(final List<Message> messages) {
        runOnWorkThread(new Runnable() {
            @Override
            public void run() {
                addMessagesInternal(messages);
            }
        });
    }
    
    private boolean addMessagesInternal(List<Message> messages) {
        Logger.i(TAG, "addMessageListInternal : " + messages.size());
        if (messages != null && messages.size() > 0) {
            List<Message> messageList = new ArrayList<>();
            for (Message message : messages) {
                if (!getHolder().containsMessage(message)) {
                    getConversationManager().addMessage(message);
                    getHolder().addMessage(message);
                    messageList.add(message);
                    getBodyLoader().loadMessageBody(message);
                }
            }
            if (getMessageDelivery() != null) {
                getMessageDelivery().postBatch(messageList);
            }
            return true;
        }
        return false;
    }
    
    void removeConversationMessages(final String conversationId) {
        runOnWorkThread(new Runnable() {
            @Override
            public void run() {
                getConversationManager().removeConversation(conversationId);
                getHolder().removeMessagesBy(conversationId);
            }
        });
    }
    
    private boolean removeMessageInternal(final Message message) {
        if (getHolder().containsMessage(message)) {
            getConversationManager().removeMessage(message);
            getHolder().removeMessage(message);
            notifyMessageRemoved(message);
            return true;
        }
        return false;
    }
    
    @Override
    public void deleteMessage(final Message message, final Callback callback) {
        runOnWorkThread(new Runnable() {
            @Override
            public void run() {
                removeMessageInternal(message);
                if (callback != null) {
                    runOnWorkThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onCompleted();
                        }
                    });
                }
            }
        });
    }
    
    @Override
    public void sendMessage(Message message, Callback callback) {
        addMessage(message);
        sendMessageImpl(message, callback);
    }
    
    @Override
    public void resendMessage(Message message, Callback callback) {
        setMsgStatus(message, Message.Status.CREATE);
        setMsgBodyStatus(message, Message.Status.CREATE);
        sendMessageImpl(message, callback);
    }
    
    private void sendMessageImpl(final Message message, final Callback callback) {
        getMessageSender(message.getChatType()).send(message, new Callback() {
            @Override
            public void onCompleted() {
                if (callback != null) {
                    callback.onCompleted();
                }
            }
            
            @Override
            public void onError(Throwable e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }
    
    @Override
    public void removeMessageListener(Listener listener) {
        int size = getListeners().size();
        int index = 0;
        while (size > 0 && index < size) {
            WeakReference<Listener> wrf = getListeners().get(index);
            if (wrf.get() == null || wrf.get() == listener) {
                getListeners().remove(index);
                size = getListeners().size();
                continue;
            }
            index++;
        }
    }
    
    @Override
    public void addMessageListener(Listener listener) {
        for (WeakReference<Listener> ListenerRf : getListeners()) {
            if (ListenerRf.get() == listener) {
                return;
            }
        }
        getListeners().add(new WeakReference<>(listener));
    }
    
    @Override
    public void loadMessageBody(Message message) {
        getBodyLoader().loadMessageBody(message);
    }
    
    private List<WeakReference<Listener>> getListeners() {
        return listeners;
    }
    
    void setMsgStatus(final Message message, Message.Status status) {
        if (message.getStatus() != status) {
            message.setStatus(status);
            notifyMessageStatusChanged(message);
        }
    }
    
    void setMsgBodyStatus(final Message message, Message.Status status) {
        if (message.getBody() instanceof FileMessageBody) {
            ((FileMessageBody) message.getBody()).setStatus(status);
            notifyMessageStatusChanged(message);
        }
    }
    
    private void setMsgListened(final Message message, boolean listened) {
        if (listened != message.isListened()) {
            message.setListened(listened);
            notifyMessageStatusChanged(message);
        }
    }
    
    @Override
    public void markMsgAsListened(final Message message) {
        if (message.getMsgType() == Message.Type.AUDIO && !message.isListened()) {
            setMsgListened(message, true);
        }
    }
    
    @Override
    public void loadHistoryMessages(String conversationId, int msgCount,
            Callback callback) {}
    
    private void runOnWorkThread(Runnable r) {
        ExecUtil.execute(ExecUtil.MODE_COMPUTE,r);
    }
    
    private void runOnUiThread(Runnable r) {
        ExecUtil.executeUI(r);
    }
    
    private void notifyMessageStatusChanged(final Message message) {
        if (getListeners().size() > 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Iterator<WeakReference<Listener>> iterator = getListeners()
                            .iterator();
                    while (iterator.hasNext()) {
                        WeakReference<Listener> ListenerRf = iterator.next();
                        if (ListenerRf.get() != null) {
                            ListenerRf.get().onMessageStatusChanged(message);
                        }
                    }
                }
            });
        }
    }
    
    void notifyMessageAdded(final Message message) {
        if (getListeners().size() > 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Iterator<WeakReference<Listener>> iterator = getListeners()
                            .iterator();
                    while (iterator.hasNext()) {
                        WeakReference<Listener> ListenerRf = iterator.next();
                        if (ListenerRf.get() != null) {
                            ListenerRf.get().onMessageAdded(message);
                        }
                    }
                }
            });
        }
    }
    
    void notifyMessagesAdded(final List<Message> messages) {
        if (getListeners().size() > 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Iterator<WeakReference<Listener>> iterator = getListeners()
                            .iterator();
                    while (iterator.hasNext()) {
                        WeakReference<Listener> ListenerRf = iterator.next();
                        if (ListenerRf.get() != null) {
                            ListenerRf.get().onMessagesAdded(messages);
                        }
                    }
                }
            });
        }
    }
    
    private void notifyMessageRemoved(final Message message) {
        if (getListeners().size() > 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Iterator<WeakReference<Listener>> iterator = getListeners()
                            .iterator();
                    while (iterator.hasNext()) {
                        WeakReference<Listener> ListenerRf = iterator.next();
                        if (ListenerRf.get() != null) {
                            ListenerRf.get().onMessageRemoved(message);
                        }
                    }
                }
            });
        }
    }
    
    @Override
    public void onReceive(Message message) {
        addMessage(message);
    }
    
    @Override
    public void onReceive(List<Message> messageList) {
        addMessages(messageList);
    }
}

package com.qingqing.base.nim.domain;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.qingqing.base.log.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by huangming on 2016/9/9.
 */
class MessageDelayedDelivery extends MessageDelivery {
    
    private static final String TAG = "MessageDelayedDelivery";
    
    private static final long DELAYED_MILLIS = 400;
    
    private static final AtomicInteger MSG_WHAT = new AtomicInteger();
    static {
        MSG_WHAT.addAndGet(11000);
    }
    
    private final Handler handler;
    private Set<Message> msgCache = new HashSet<>();
    private final int msgWhat;
    
    MessageDelayedDelivery(MessageManager messageManager) {
        super(messageManager);
        HandlerThread handlerThread = new HandlerThread("MessageDelayedDelivery");
        handlerThread.start();
        handler = new MyHandler(handlerThread.getLooper());
        this.msgWhat = MSG_WHAT.getAndIncrement();
    }
    
    private int getMsgWhat() {
        return msgWhat;
    }
    
    private Handler getHandler() {
        return handler;
    }
    
    private Set<Message> getMsgCache() {
        return msgCache;
    }
    
    @Override
    public void post(Message message) {
        postMessageDelayed(message);
    }
    
    @Override
    public void postBatch(List<Message> messages) {
        postMessagesDelayed(messages);
    }
    
    private void postMessageDelayed(final Message message) {
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                getMsgCache().add(message);
                postDelayedIfNecessary();
            }
        });
    }
    
    private void postMessagesDelayed(final List<Message> messages) {
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                getMsgCache().addAll(messages);
                postDelayedIfNecessary();
            }
        });
    }
    
    private void postDelayedIfNecessary() {
        if (!getHandler().hasMessages(getMsgWhat())) {
            getHandler().sendEmptyMessageDelayed(getMsgWhat(), DELAYED_MILLIS);
        }
    }
    
    private void processMessagesInternal() {
        Logger.i(TAG, "processMessagesInternal : " + getMsgCache().size());
        getMessageManager().notifyMessagesAdded(new ArrayList<>(getMsgCache()));
        getMsgCache().clear();
    }
    
    private class MyHandler extends Handler {
        
        MyHandler(Looper looper) {
            super(looper);
        }
        
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            
            int what = msg.what;
            if (what == getMsgWhat()) {
                processMessagesInternal();
            }
        }
    }
    
}

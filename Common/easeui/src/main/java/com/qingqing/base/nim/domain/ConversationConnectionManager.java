package com.qingqing.base.nim.domain;

import com.qingqing.base.log.Logger;
import com.qingqing.base.mqtt.MqttManager;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by huangming on 2016/9/6.
 */
class ConversationConnectionManager implements ChatConnectionListener {
    
    private static final String TAG = "ConversationConnectionManager";
    
    private Set<String> keptAliveConversations = new HashSet<>();
    
    private Set<String> getKeptAliveConversations() {
        return keptAliveConversations;
    }
    
    void keepConversationAlive(String conversationId) {
        addKeptAliveConversation(conversationId);
        if (MqttManager.getInstance().isConnected()) {
            connectConversation(conversationId);
        }
    }
    
    private void addKeptAliveConversation(String conversationId) {
        getKeptAliveConversations().add(conversationId);
    }
    
    private void removeKeptAliveConversation(String conversationId) {
        getKeptAliveConversations().remove(conversationId);
    }
    
    private void connectKeptAliveConversations() {
        for (String conversationId : getKeptAliveConversations()) {
            connectConversation(conversationId);
        }
    }
    
    void disconnectConversation(final String conversationId) {
        removeKeptAliveConversation(conversationId);
        MqttManager.getInstance().unsubscribe(conversationId, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken iMqttToken) {
                Logger.i(TAG, "conversation(" + conversationId + ") unsubscribe success");
            }
            
            @Override
            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                Logger.e(TAG, "conversation(" + conversationId + ") unsubscribe failed");
            }
        });
    }
    
    private void connectConversation(final String conversationId) {
        MqttManager.getInstance().subscribe(conversationId, 0, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken iMqttToken) {
                Logger.i(TAG, "conversation(" + conversationId + ") subscribe success");
            }
            
            @Override
            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                Logger.e(TAG, "conversation(" + conversationId + ") subscribe failed");
            }
        });
    }
    
    void init() {}
    
    void destroy() {
        getKeptAliveConversations().clear();
    }
    
    @Override
    public void onChatConnected() {
        Logger.i(TAG, "mqtt connected");
        connectKeptAliveConversations();
    }
}

package com.qingqing.base.nim.domain;

import com.qingqing.base.log.Logger;
import com.qingqing.base.mqtt.MqttManager;
import com.qingqing.base.nim.domain.services.ChatConnectionService;
import com.qingqing.base.utils.ExecUtil;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by huangming on 2016/9/6.
 */
class ChatConnectionManager implements ChatConnectionService {

    ChatConnectionManager() {

    }
    
    private Set<ChatConnectionListener> listeners = new HashSet<>();
    
    private static final String TAG = "ChatConnectionManager";
    
    private IMqttActionListener mqttConnectionListener = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken iMqttToken) {
            Logger.i(TAG, "mqtt connected");
            ExecUtil.executeUI(new Runnable() {
                @Override
                public void run() {
                    notifyChatConnected();
                }
            });
        }
        
        @Override
        public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
            Logger.e(TAG, "mqtt disconnected");
        }
    };
    
    private Set<ChatConnectionListener> getListeners() {
        return listeners;
    }

    @Override
    public void addChatConnectionListener(ChatConnectionListener listener) {
        getListeners().add(listener);
    }

    @Override
    public void removeChatConnectionListener(ChatConnectionListener listener) {
        getListeners().remove(listener);
    }
    
    private void notifyChatConnected() {
        for (ChatConnectionListener listener : getListeners()) {
            listener.onChatConnected();
        }
    }
    
    private IMqttActionListener getMqttConnectionListener() {
        return mqttConnectionListener;
    }
    
    void init() {
        MqttManager.getInstance().addConnectListener(getMqttConnectionListener());
    }
    
    void destroy() {
        getListeners().clear();
        MqttManager.getInstance().removeConnectListener(getMqttConnectionListener());
    }
    
}

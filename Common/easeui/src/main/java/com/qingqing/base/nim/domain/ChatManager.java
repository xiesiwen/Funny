package com.qingqing.base.nim.domain;

import android.content.Context;

import com.qingqing.base.data.BaseData;
import com.qingqing.base.log.Logger;
import com.qingqing.base.nim.domain.services.ChatConnectionService;
import com.qingqing.base.nim.domain.services.ChatRoomService;
import com.qingqing.base.nim.domain.services.ConversationService;
import com.qingqing.base.nim.domain.services.LectureService;
import com.qingqing.base.nim.domain.services.MessageService;

/**
 * Created by huangming on 2016/8/15.
 *
 * ChatManager负责服务对象的创建，和注册监听等
 *
 * 处理流程
 *
 * 1.MessageMonitor（MqttCoordinator）注册Matt监听。
 *
 * 2.MessageDispatcher（MqttCoordinator）将MessageMonitor中的监听到的Message分发
 *
 * 3.interceptReceiver（CompletionController）接收MessageDispatcher分发的消息，进行补全
 *
 * 4.使用补全机制（见CompletionController介绍）
 *
 * 5.补全后将消息分发给FinalMessageReceiver
 *
 * 6.FinalMessageReceiver将消息给到MessageManager和ConversationManager处理
 *
 * 7.MessageManager和ConversationManager将消息分发给上层
 */
public class ChatManager {
    
    private static final String TAG = "NewChatManager";
    
    private volatile static ChatManager sChatManager;
    
    private MessageBodyLoader bodyLoader;
    private MessageManager messageManager;
    private ConversationManager conversationManager;
    private ChatRoomManager chatRoomManager;
    private LectureRoomManager lectureManager;
    private ConversationConnectionManager conversationConnectionManager;
    
    private ChatConnectionManager chatConnectionManager;
    
    // 消息的原始的分发（Mqtt）, 消息监听器（Mqtt）
    private MqttCoordinator mqttCoordinator;
    // 拦截接收器（补全Completion）
    private CompletionController completionController;
    
//    // 消息监听器Executor
//    private Executor mqttExecutor;
//    // 补全Executor
//    private Executor completionExecutor;
//    // MessageManager和ConversationManager Executor
//    private Executor messageExecutor;
    
    private ChatNotifier notifier;
    
    private ChatManager() {}
    
    public String getCurrentUserId() {
        return BaseData.qingqingUserId() != null ? BaseData.qingqingUserId() : "";
    }
    
    public static ChatManager getInstance() {
        if (sChatManager == null) {
            synchronized (ChatManager.class) {
                if (sChatManager == null) {
                    sChatManager = new ChatManager();
                }
            }
        }
        return sChatManager;
    }
    
    public void init(Context context) {
        Logger.e(TAG, "init");

        bodyLoader = new MessageBodyLoader();
        messageManager = new MessageManager(getBodyLoader());
        bodyLoader.setMessageManager(getMessageManager());
        conversationManager = new ConversationManager();
        messageManager.setConversationManager(getConversationManager());
        
        MessageDelivery messageDelivery = new MessageDelayedDelivery(getMessageManager());
        getMessageManager().setMessageDelivery(messageDelivery);
        
        conversationConnectionManager = new ConversationConnectionManager();
        chatConnectionManager = new ChatConnectionManager();
        
        chatRoomManager = new ChatRoomManager();
        
        mqttCoordinator = new MqttCoordinator();
        
        completionController = new CompletionController(getMessageManager());
        
        getMqttCoordinator().registerReceiver(getCompletionController());
        getMqttCoordinator().registerMonitor();
        
        lectureManager = new LectureRoomManager(getMessageManager(),
                getConversationConnectionManager(), getCompletionController());
        
        this.notifier = new ChatNotifier(context);
        
        getConversationConnectionManager().init();
        getChatConnectionManager().init();
        getChatConnectionManager()
                .addChatConnectionListener(getConversationConnectionManager());
    }
    
    public void login(Context context) {
        Logger.e(TAG, "login");
        init(context);
    }
    
    private MessageBodyLoader getBodyLoader() {
        return bodyLoader;
    }
    
    public ChatRoomService getChatRoomService() {
        return chatRoomManager;
    }
    
    public LectureService getLectureService() {
        return lectureManager;
    }
    
    public MessageService getMessageService() {
        return messageManager;
    }
    
    public ConversationService getConversationService() {
        return conversationManager;
    }
    
    private MessageManager getMessageManager() {
        return messageManager;
    }
    
    private ChatRoomManager getChatRoomManager() {
        return chatRoomManager;
    }
    
    private ConversationManager getConversationManager() {
        return conversationManager;
    }
    
    private LectureRoomManager getLectureManager() {
        return lectureManager;
    }
    
    private MqttCoordinator getMqttCoordinator() {
        return mqttCoordinator;
    }
    
    private CompletionController getCompletionController() {
        return completionController;
    }
    
    public ChatNotifier getNotifier() {
        return notifier;
    }
    
    private ConversationConnectionManager getConversationConnectionManager() {
        return conversationConnectionManager;
    }
    
    public ChatConnectionService getChatConnectionService() {
        return chatConnectionManager;
    }
    
    private ChatConnectionManager getChatConnectionManager() {
        return chatConnectionManager;
    }
    
    public void logout() {
        Logger.e(TAG, "logout");
        getMqttCoordinator().unregisterMonitor();
        getMqttCoordinator().unregisterReceiver(getCompletionController());
        getNotifier().destroy();
        getConversationConnectionManager().destroy();
        getChatConnectionManager().destroy();
    }
    
}

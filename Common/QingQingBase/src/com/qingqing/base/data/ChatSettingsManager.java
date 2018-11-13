package com.qingqing.base.data;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by huangming on 2017/2/7.
 */

public class ChatSettingsManager {
    
    private SPWrapper spWrapper;
    
    private static String KEY_IGNORED_CONVERSATION_IDS = "ignored_conversation_ids";
    public static final String PREFERENCE_NAME = "chat_settings_";
    private static ChatSettingsManager sInstance;
    
    public ChatSettingsManager(String currentUser) {
        spWrapper = new SPWrapper(PREFERENCE_NAME + currentUser);
    }
    
    public static void init(String currentUser) {
        sInstance = new ChatSettingsManager(currentUser);
    }
    
    public static ChatSettingsManager getInstance() {
        if (sInstance == null) {
            sInstance = new ChatSettingsManager("");
        }
        
        return sInstance;
    }
    
    public void ignoreMsgNotification(String conversationId) {
        Set<String> conversationIds = spWrapper.getStringSet(KEY_IGNORED_CONVERSATION_IDS,
                new HashSet<String>());
        if (!conversationIds.contains(conversationId)) {
            conversationIds.add(conversationId);
            spWrapper.put(KEY_IGNORED_CONVERSATION_IDS, conversationIds);
        }
    }
    
    public void unignoreMsgNotification(String conversationId) {
        Set<String> conversationIds = spWrapper.getStringSet(KEY_IGNORED_CONVERSATION_IDS,
                new HashSet<String>());
        if (conversationIds.contains(conversationId)) {
            conversationIds.remove(conversationId);
            spWrapper.put(KEY_IGNORED_CONVERSATION_IDS, conversationIds);
        }
    }
    
    public boolean isMsgNotificationIgnored(String conversationId) {
        if (spWrapper == null) { // # Fabric 548 Fatal Exception: java.lang.NullPointerException: Attempt to invoke virtual method 'java.util.Set de.k.a(java.lang.String, java.util.Set)' on a null object reference
            return false;
        }
        Set<String> conversationIds = spWrapper.getStringSet(KEY_IGNORED_CONVERSATION_IDS,
                new HashSet<String>());
        return conversationIds != null && conversationIds.contains(conversationId);
    }
    
}

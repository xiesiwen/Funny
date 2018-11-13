package com.qingqing.base.im;

import android.text.TextUtils;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.qingqing.api.proto.v1.UserProto;
import com.qingqing.api.proto.v1.app.AppCommon;
import com.qingqing.api.proto.v1.im.ImProto;
import com.qingqing.base.config.CommonUrl;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.http.error.HttpError;
import com.qingqing.base.http.req.ProtoListener;
import com.qingqing.base.http.req.ProtoReq;
import com.qingqing.base.im.domain.ContactInfo;
import com.qingqing.base.log.Logger;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by huangming on 2017/3/30.
 */

public class ChatConversationInitializer {

    private static final String TAG = ChatConversationInitializer.class.getSimpleName();

    private static final ChatConversationInitializer INSTANCE = new ChatConversationInitializer();

    private Set<Listener> listeners = new HashSet<>(1);

    private Set<String> initializingConversations = new HashSet<>(1);

    private ChatConversationInitializer() {
    }

    public static ChatConversationInitializer getInstance() {
        return INSTANCE;
    }

    void initChatConversationBy(EMMessage newMessage) {
        if (BaseData.getClientType() != AppCommon.AppType.qingqing_ta) {
            //Logger.w(TAG, "initChatConversationBy : ClientType is not TA!");
            return;
        }

        if (newMessage == null) {
            Logger.w(TAG, "initChatConversationBy : message is null!");
            return;
        }
        final String conversationId = ChatManager.getInstance().getConversationId(newMessage);
        if (TextUtils.isEmpty(conversationId)) {
            Logger.w(TAG, "initChatConversationBy :  conversationId is empty");
            return;
        }

        //当会话消息大于1时，则证明此会话存在
        EMConversation conversation = EMChatManager.getInstance().getConversation(conversationId);
        if (conversation != null && conversation.getAllMsgCount() > 1) {
            Logger.w(TAG, "initChatConversationBy: conversation already exist!");
            return;
        }

        if (newMessage.getChatType() != EMMessage.ChatType.Chat) {
            Logger.w(TAG, "initChatConversationBy: ChatType is not single chat!");
            return;
        }

        final ContactService contactService = ChatManager.getInstance().getContactService();
        if (contactService == null) {
            Logger.w(TAG, "contactService : ContactService is null!");
            return;
        }

        ContactInfo contactInfo = contactService.getContactInfo(conversationId);

        //当contactInfo = null 或者 非绑定老师的助教为NULL
        if (contactInfo == null ||
                (contactInfo.getType() == ContactInfo.Type.Teacher && !contactInfo.isFriends() && TextUtils.isEmpty(contactInfo.getTaUserID()))) {
            initSingleChatConversation(conversationId);
        } else {
            Logger.i(TAG, "initChatConversationBy : contactInfo is initialized or not teacher");
        }
    }

    private void initSingleChatConversation(final String conversationId) {
        if (initializingConversations.contains(conversationId)) {
            Logger.w(TAG, "initSingleChatConversation: conversationId(" + conversationId + ") is initializing!");
            return;
        }
        initializingConversations.add(conversationId);
        Logger.i(TAG, "initSingleChatConversation : conversationId = " + conversationId);
        reqInitSingleChat(conversationId, new ProtoListener(ImProto.ChatInitResponse.class) {

            @Override
            public void onDealResult(Object result) {
                ImProto.ChatInitResponse response = (ImProto.ChatInitResponse) result;
                UserProto.ChatUserInfo chatUserInfo = response.member;
                if (chatUserInfo != null && ChatManager.getInstance().getContactService() != null) {
                    ChatManager.getInstance().getContactService().saveContactInfo(chatUserInfo);
                }
                notifyChatConversationInitialized(conversationId);
                initializingConversations.remove(conversationId);
            }

            public void onDealError(HttpError error, boolean isParseOK, int errorCode, Object result) {
                Logger.e(TAG, "reqBindRelation errorCode : " + errorCode, error);
                initializingConversations.remove(conversationId);
            }
        });


    }

    public void reqInitSingleChat(final String conversationId, final ProtoListener protoListener) {
        ImProto.ChatInitRequest builder = new ImProto.ChatInitRequest();
        builder.qingqingUserId = conversationId;
        new ProtoReq(CommonUrl.CHAT_INIT_URL.url()).setSendMsg(builder)
                .setRspListener(protoListener)
                .req();
    }

    public void notifyChatConversationInitialized(String conversationId) {
        for (Listener listener : listeners) {
            listener.onChatConversationInitialized(conversationId);
        }
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public interface Listener {
        void onChatConversationInitialized(String conversationId);
    }

}

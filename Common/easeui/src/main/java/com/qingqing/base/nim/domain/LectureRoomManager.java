package com.qingqing.base.nim.domain;

import android.text.TextUtils;

import com.qingqing.api.proto.v1.LectureProto;
import com.qingqing.api.proto.v1.ProtoBufResponse;
import com.qingqing.api.proto.v1.im.ImProto;
import com.qingqing.base.config.CommonUrl;
import com.qingqing.base.http.error.HttpError;
import com.qingqing.base.http.req.ProtoListener;
import com.qingqing.base.http.req.ProtoReq;
import com.qingqing.base.interfaces.Observer;
import com.qingqing.base.log.Logger;
import com.qingqing.base.nim.domain.services.LectureService;
import com.qingqing.base.nim.exception.LectureJoinError;
import com.qingqing.base.nim.utils.ChatContactUtils;
import com.qingqing.base.utils.ImageUrlUtil;

import java.util.Arrays;
import java.util.List;

/**
 * Created by huangming on 2016/8/22.
 */
class LectureRoomManager implements LectureService {
    private static final String TAG = "LectureRoomManager";
    
    public static final int DEFAULT_HISTORY_MESSAGE_PAGE_SIZE = 10;
    private final MessageManager messageManager;
    private final ConversationConnectionManager conversationConnectionManager;
    private final CompletionController completionController;
    private int earliestHistoryMessage = 0;
    private String chatRoomId;
    private boolean isLoadingHistory = false;
    
    LectureRoomManager(MessageManager messageManager,
            ConversationConnectionManager conversationConnectionManager,
            CompletionController completionController) {
        this.messageManager = messageManager;
        this.conversationConnectionManager = conversationConnectionManager;
        this.completionController = completionController;
    }
    
    private MessageManager getMessageManager() {
        return messageManager;
    }
    
    private ConversationConnectionManager getConversationConnectionManager() {
        return conversationConnectionManager;
    }
    
    private CompletionController getCompletionController() {
        return completionController;
    }
    
    @Override
    public void joinLecture(final String lectureId,
            final Observer<LectureRoom> callback) {
        LectureProto.SimpleQingQingLectureIdRequest request = new LectureProto.SimpleQingQingLectureIdRequest();
        request.qingqingLectureId = lectureId;
        new ProtoReq(CommonUrl.LECTURE_CHAT_ROOM_JOIN_V3.url()).setSendMsg(request)
                .setRspListener(new ProtoListener(
                        LectureProto.LectureChatRoomJoinResponse.class) {
                    @Override
                    public void onDealResult(Object result) {
                        Logger.i(TAG, "join lecture room success");
                        final LectureProto.LectureChatRoomJoinResponse response = (LectureProto.LectureChatRoomJoinResponse) result;
                        if (processResponse(lectureId, response, callback)) {
                            chatRoomId = response.chatRoomId;
                            getCompletionController().startCompletionHandler(chatRoomId,
                                    ChatType.ChatRoom);
                            loadHistoryMessage(chatRoomId, 0);
                            getConversationConnectionManager()
                                    .keepConversationAlive(chatRoomId);
                        }
                    }
                    
                    @Override
                    public void onDealError(HttpError error, boolean isParseOK,
                            int errorCode, Object result) {
                        Logger.e(TAG, "join lecture room(" + lectureId + ") failed");
                        if (callback != null) {
                            callback.onError(new LectureJoinError(errorCode,
                                    getErrorHintMessage("")));
                        }
                    }
                }).req();
    }
    
    @Override
    public void rejoinLecture(final String lectureId,
            final Observer<LectureRoom> callback) {
        LectureProto.SimpleQingQingLectureIdRequest request = new LectureProto.SimpleQingQingLectureIdRequest();
        request.qingqingLectureId = lectureId;
        new ProtoReq(CommonUrl.LECTURE_CHAT_ROOM_JOIN_V3.url()).setSendMsg(request)
                .setRspListener(new ProtoListener(
                        LectureProto.LectureChatRoomJoinResponse.class) {
                    @Override
                    public void onDealResult(Object result) {
                        Logger.i(TAG, "rejoin lecture room success");
                        final LectureProto.LectureChatRoomJoinResponse response = (LectureProto.LectureChatRoomJoinResponse) result;
                        processResponse(lectureId, response, callback);
                    }
                    
                    @Override
                    public void onDealError(HttpError error, boolean isParseOK,
                            int errorCode, Object result) {
                        Logger.e(TAG, "rejoin lecture room(" + lectureId + ") failed");
                        if (callback != null) {
                            callback.onError(new LectureJoinError(errorCode,
                                    getErrorHintMessage("")));
                        }
                    }
                }).req();
    }
    
    private boolean processResponse(String lectureId,
            LectureProto.LectureChatRoomJoinResponse response,
            Observer<LectureRoom> callback) {
        if (response.imType != ImProto.ImType.qingqing_im_type) {
            Logger.e(TAG, "join lecture room(" + lectureId
                    + ") failed, im type is not qingqing : " + response.imType);
            if (callback != null) {
                callback.onError(new LectureJoinError(""));
            }
            return false;
        }
        final LectureRoom lectureRoom = new LectureRoom(lectureId);
        lectureRoom
                .setPptIndex(response.hasCurrentPptIndex ? response.currentPptIndex : 0);
        lectureRoom.setRoomId(response.chatRoomId);
        if (response.ppts != null && response.ppts.length > 0) {
            for (int i = 0; i < response.ppts.length; i++) {
                response.ppts[i] = ImageUrlUtil.getOriginImg(response.ppts[i]);
            }
            lectureRoom.setPpt(Arrays.asList(response.ppts));
        }
        if (response.stopTalkQingqingChatUserIds != null
                && response.stopTalkQingqingChatUserIds.length > 0) {
            lectureRoom.setForbiddenUsers(
                    Arrays.asList(response.stopTalkQingqingChatUserIds));
        }
        
        lectureRoom.setSpeakAllForbidden(response.isAllStopTalk);
        lectureRoom.setName(response.chatRoomName);
        ChatRole chatRole = ChatContactUtils.getChatRoleBy(response.currentUserInfo,
                response.currentRole);
        lectureRoom.setRole(chatRole);
        if (callback != null) {
            callback.onNext(lectureRoom);
            callback.onCompleted();
        }
        return true;
    }
    
    @Override
    public void leaveLecture(final String lectureId, final String chatRoomId,
            final Callback callback) {
        if (!TextUtils.isEmpty(chatRoomId)) {
            getConversationConnectionManager().disconnectConversation(chatRoomId);
            getCompletionController().stopCompletionHandler(chatRoomId);
        }
        LectureProto.SimpleQingQingLectureIdRequest request = new LectureProto.SimpleQingQingLectureIdRequest();
        request.qingqingLectureId = lectureId;
        new ProtoReq(CommonUrl.LECTURE_CHAT_ROOM_LEAVE.url()).setSendMsg(request)
                .setRspListener(new ProtoListener(ProtoBufResponse.SimpleResponse.class) {
                    @Override
                    public void onDealResult(Object result) {
                        Logger.i(TAG, "leave lecture success");
                        getMessageManager().removeConversationMessages(chatRoomId);
                        if (callback != null) {
                            callback.onCompleted();
                        }
                    }
                    
                    @Override
                    public void onDealError(HttpError error, boolean isParseOK,
                            int errorCode, Object result) {
                        Logger.e(TAG, "leave lecture(" + lectureId + ") failed");
                        getMessageManager().removeConversationMessages(chatRoomId);
                        if (callback != null) {
                            callback.onError(new LectureJoinError(errorCode,
                                    getErrorHintMessage("")));
                        }
                    }
                }).req();
    }
    
    @Override
    public void playPpt(final String lectureId, final int pptIndex, String pptImgUrl,
            long playTime, final Callback callback) {
        LectureProto.LecturePptPlayRequest request = new LectureProto.LecturePptPlayRequest();
        request.imgIndex = pptIndex;
        request.hasImgIndex = true;
        request.playTime = playTime;
        request.qingqingLectureId = lectureId;
        request.url = pptImgUrl;
        new ProtoReq(CommonUrl.LECTURE_CHAT_ROOM_PLAY_PPT.url()).setSendMsg(request)
                .setRspListener(new ProtoListener(ProtoBufResponse.SimpleResponse.class) {
                    @Override
                    public void onDealResult(Object result) {
                        Logger.i(TAG, "play ppt(" + pptIndex + ") success");
                        if (callback != null) {
                            callback.onCompleted();
                        }
                    }
                    
                    @Override
                    public void onDealError(HttpError error, boolean isParseOK,
                            int errorCode, Object result) {
                        super.onDealError(error, isParseOK, errorCode, result);
                        Logger.e(TAG, "play ppt(" + pptIndex + ") error : errorCode = "
                                + errorCode);
                        if (callback != null) {
                            callback.onError(new Throwable("lectureId(" + lectureId
                                    + ") play ppt error : pptIndex = " + pptIndex));
                        }
                    }
                }).req();
    }
    
    @Override
    public boolean loadingHistoryMessage() {
        return loadEarliestHistoryMessage(chatRoomId);
    }
    
    private boolean loadEarliestHistoryMessage(String chatRoomId) {
        // 已获取最早的消息（index == 1），不需再请求
        if (earliestHistoryMessage <= 1) {
            return false;
        }
        
        int startIndex = earliestHistoryMessage - DEFAULT_HISTORY_MESSAGE_PAGE_SIZE;
        if (startIndex <= 0) {
            startIndex = 1;
        }
        
        loadHistoryMessage(chatRoomId, startIndex);
        return true;
    }
    
    private void loadHistoryMessage(String chatRoomId, int startIndex) {
        if (isLoadingHistory) {
            return;
        }
        isLoadingHistory = true;

        new MessageByRangeLoader(chatRoomId, ChatType.ChatRoom, startIndex,
                DEFAULT_HISTORY_MESSAGE_PAGE_SIZE)
                        .loadMessages(new Observer<List<Message>>() {
                            @Override
                            public void onCompleted() {
                                Logger.i(TAG, "loadHistoryMessage success");
                                isLoadingHistory = false;
                            }
                            
                            @Override
                            public void onError(Throwable e) {
                                Logger.i(TAG, "loadHistoryMessage failed", e);
                                isLoadingHistory = false;
                            }
                            
                            @Override
                            public void onNext(List<Message> messages) {
                                if (messages != null) {
                                    for (Message message : messages) {
                                        message.setHistory(true);
                                    }
                                    if (messages.size() > 0) {
                                        earliestHistoryMessage = messages.get(0)
                                                .getIndex();
                                    }
                                }
                                getMessageManager().addMessages(messages);
                                isLoadingHistory = false;
                            }
                        });
    }
    
}

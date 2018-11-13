package com.qingqing.base.nim;

import android.text.TextUtils;

import com.easemob.easeui.R;
import com.qingqing.api.commentsvc.proto.v1.CommentCountingProto;
import com.qingqing.api.proto.v1.im.ImProto;
import com.qingqing.base.time.NetworkTime;
import com.qingqing.base.interfaces.Observer;
import com.qingqing.base.log.Logger;
import com.qingqing.base.nim.cmd.CmdType;
import com.qingqing.base.nim.cmd.CmdUtils;
import com.qingqing.base.nim.cmd.LectureCmd;
import com.qingqing.base.nim.domain.ChatConnectionListener;
import com.qingqing.base.nim.domain.ChatManager;
import com.qingqing.base.nim.domain.ChatRole;
import com.qingqing.base.nim.domain.CmdMessageBody;
import com.qingqing.base.nim.domain.LectureRoom;
import com.qingqing.base.nim.domain.Message;
import com.qingqing.base.nim.exception.LectureJoinError;
import com.qingqing.base.nim.utils.MessageUtils;
import com.qingqing.base.task.UserBrowsingCountTask;
import com.qingqing.base.view.ToastWrapper;

import java.util.List;

/**
 * Created by huangming on 2016/8/17.
 */
public class LectureCoordinator extends ChatRoomCoordinator
        implements MessageInterceptor {
    
    private static final String TAG = "LectureCoordinator";
    private final String lectureId;
    private int numberOfParticipants;
    private LectureRoom lectureRoom;
    
    private int curForbiddenIndex;
    private int curPptPlayedIndex;
    private int curChangePptIndex;
    
    private final GettingLectureParticipantsTask gettingLectureParticipantsTask;
    
    public LectureCoordinator(String lectureId) {
        super(null);
        this.lectureId = lectureId;
        this.gettingLectureParticipantsTask = new GettingLectureParticipantsTask(
                getLectureId()) {
            @Override
            public void onNext(Integer integer) {
                if (isDestroyed()) {
                    return;
                }
                setNumberOfParticipants(integer);
            }
        };
    }
    
    private ChatConnectionListener connectionListener = new ChatConnectionListener() {
        @Override
        public void onChatConnected() {
            ChatManager.getInstance().getLectureService().rejoinLecture(getLectureId(),
                    new Observer<LectureRoom>() {
                        @Override
                        public void onCompleted() {}
                        
                        @Override
                        public void onError(Throwable e) {
                            
                        }
                        
                        @Override
                        public void onNext(LectureRoom lectureRoom) {
                            Logger.i(TAG, "rejoin");
                            setLectureRoom(lectureRoom);
                            notifyFinished(false);
                            notifyPptPlayed();
                            notifySpeakForbiddenChanged();
                        }
                    });
        }
    };
    
    private ChatConnectionListener getConnectionListener() {
        return connectionListener;
    }
    
    public LectureRoom getLectureRoom() {
        return lectureRoom;
    }
    
    private void setLectureRoom(LectureRoom lectureRoom) {
        this.lectureRoom = lectureRoom;
    }
    
    @Override
    public LectureCoordinationListener getCoordinationListener() {
        return (LectureCoordinationListener) super.getCoordinationListener();
    }
    
    public String getLectureId() {
        return lectureId;
    }
    
    public int getNumberOfParticipants() {
        return numberOfParticipants;
    }
    
    private void setNumberOfParticipants(int numberOfParticipants) {
        int preNumberOfParticipants = getNumberOfParticipants();
        this.numberOfParticipants = numberOfParticipants;
        
        if (getNumberOfParticipants() != preNumberOfParticipants) {
            notifyNumberOfParticipantsChanged();
        }
    }
    
    private void notifyNumberOfParticipantsChanged() {
        if (getCoordinationListener() != null) {
            getCoordinationListener().onNumberOfParticipantsChanged();
        }
    }
    
    public ChatRole getChatRole() {
        return getLectureRoom() != null ? getLectureRoom().getRole() : null;
    }
    
    private GettingLectureParticipantsTask getGettingLectureParticipantsTask() {
        return gettingLectureParticipantsTask;
    }
    
    private void reqUserBrowsingCount() {
        UserBrowsingCountTask task = new UserBrowsingCountTask(getLectureId(),
                CommentCountingProto.CountRefType.lecture_count_type, true) {
            @Override
            public void onCompleted() {
                if (isDestroyed()) {
                    return;
                }
                getGettingLectureParticipantsTask().execute();
            }
            
            @Override
            public void onNext(Integer integer) {
                if (isDestroyed()) {
                    return;
                }
                setNumberOfParticipants(integer);
            }
        };
        task.execute();
    }
    
    @Override
    public void onInitialize() {
        ChatManager.getInstance().getLectureService().joinLecture(getLectureId(),
                new Observer<LectureRoom>() {
                    @Override
                    public void onCompleted() {
                        Logger.i(TAG,
                                "Lecture room(" + getLectureId() + ") init success");
                        if (isDestroyed()) {
                            return;
                        }
                        setInitialized(true);
                        ChatManager.getInstance().getChatConnectionService()
                                .addChatConnectionListener(getConnectionListener());
                    }
                    
                    @Override
                    public void onError(Throwable e) {
                        Logger.e(TAG, "Lecture room(" + getLectureId() + ") init failed");
                        if (isDestroyed()) {
                            return;
                        }
                        setInitialized(false);
                        LectureJoinError error = (LectureJoinError) e;
                        ToastWrapper.show(getErrorHintMessage(error.getErrorMessage(),
                                R.string.tips_join_lecture_failed));
                    }
                    
                    @Override
                    public void onNext(LectureRoom lectureRoom) {
                        if (isDestroyed()) {
                            return;
                        }
                        setLectureRoom(lectureRoom);
                        setConversationId(lectureRoom.getRoomId());
                        reqUserBrowsingCount();
                    }
                });
    }
    
    public String getLectureName() {
        if (getLectureRoom() != null) {
            return getLectureRoom().getName();
        }
        return "";
    }
    
    public boolean isCurrentUserSpeakForbidden() {
        if (getLectureRoom() == null) {
            return false;
        }
        if (isFinished()) {
            return true;
        }
        if (isAdminOrExpertRole()) {
            return false;
        }
        if (isSpeakAllForbidden()) {
            return true;
        }
        if (isUserInForbiddenList(getCurrentUserId())) {
            return true;
        }
        return false;
    }
    
    private boolean isMessageFilteredOut(Message message) {
        if (getLectureRoom() == null) {
            return false;
        }
        if (MessageUtils.isSendDirect(message)) {
            return false;
        }
        
        if (isSpeakAllForbidden() && message.hasPlayedRole()) {
            boolean isAdminOrExpert = false;
            
            for (int i = 0; i < message.getRole().getRoleType().size(); i++) {
                if (message.getRole().getRoleType()
                        .get(i) == ImProto.ChatRoomRoleType.mc_chat_room_role_type
                        || message.getRole().getRoleType().get(
                                i) == ImProto.ChatRoomRoleType.admin_chat_room_role_type) {
                    isAdminOrExpert = true;
                }
            }
            if (!isAdminOrExpert) {
                return true;
            }
        }
        
        if (isUserInForbiddenList(message.getFrom())) {
            return true;
        }
        
        return false;
    }
    
    public boolean isSpeakAllForbidden() {
        return getLectureRoom() != null && getLectureRoom().isSpeakAllForbidden();
    }
    
    public int getCurForbiddenIndex() {
        return curForbiddenIndex;
    }
    
    private void setCurForbiddenIndex(int curForbiddenIndex) {
        this.curForbiddenIndex = curForbiddenIndex;
    }
    
    public int getCurPptPlayedIndex() {
        return curPptPlayedIndex;
    }
    
    private void setCurPptPlayedIndex(int curPptPlayedIndex) {
        this.curPptPlayedIndex = curPptPlayedIndex;
    }
    
    private int getCurChangePptIndex() {
        return curChangePptIndex;
    }
    
    private void setCurChangePptIndex(int curChangePptIndex) {
        this.curChangePptIndex = curChangePptIndex;
    }
    
    public boolean canSendAudio() {
        return isExpertRole();
    }
    
    public boolean isExpertRole() {
        boolean isExpert = false;
        
        if (getChatRole() != null) {
            for (int i = 0; i < getChatRole().getRoleType().size(); i++) {
                if (getChatRole().getRoleType()
                        .get(i) == ImProto.ChatRoomRoleType.mc_chat_room_role_type) {
                    isExpert = true;
                }
            }
        }
        
        return isExpert;
    }
    
    public boolean isAdminOrExpertRole() {
        boolean isAdminOrExpert = false;
        
        if (getChatRole() != null) {
            for (int i = 0; i < getChatRole().getRoleType().size(); i++) {
                if (getChatRole().getRoleType()
                        .get(i) == ImProto.ChatRoomRoleType.mc_chat_room_role_type
                        || getChatRole().getRoleType().get(
                                i) == ImProto.ChatRoomRoleType.admin_chat_room_role_type) {
                    isAdminOrExpert = true;
                }
            }
        }
        
        return isAdminOrExpert;
    }
    
    public boolean isGuestRole() {
        boolean isGuest = true;
        
        if (getChatRole() != null) {
            for (int i = 0; i < getChatRole().getRoleType().size(); i++) {
                if (getChatRole().getRoleType()
                        .get(i) != ImProto.ChatRoomRoleType.guest_chat_room_role_type
                        && getChatRole().getRoleType().get(
                                i) != ImProto.ChatRoomRoleType.unknown_chat_room_role_type) {
                    isGuest = false;
                }
            }
        }
        
        return isGuest;
    }
    
    public boolean isUserInForbiddenList(String userId) {
        return getLectureRoom() != null && getLectureRoom().isUserInForbiddenList(userId);
    }
    
    public boolean isFinished() {
        return getLectureRoom() != null && getLectureRoom().isFinished();
    }
    
    public void finishLecture(boolean needForceFinish) {
        if (getLectureRoom() != null) {
            getLectureRoom().finish();
            notifyFinished(needForceFinish);
        }
    }
    
    public void changeRole(ChatRole chatRole) {
        if (getLectureRoom() != null) {
            getLectureRoom().changeRole(chatRole);
            notifyRoleChanged();
        }
    }
    
    private void notifyFinished(boolean needForceFinish) {
        if (getCoordinationListener() != null) {
            getCoordinationListener().onFinished(needForceFinish);
        }
    }
    
    private void notifyRoleChanged() {
        if (getCoordinationListener() != null) {
            getCoordinationListener().onRoleChanged();
        }
    }
    
    private void notifyPptChanged() {
        if (getCoordinationListener() != null) {
            getCoordinationListener().onPptChanged();
        }
    }
    
    private void notifyPptPlayed() {
        if (getCoordinationListener() != null) {
            getCoordinationListener().onPptPlayed();
        }
    }
    
    private void notifySpeakForbiddenChanged() {
        if (getCoordinationListener() != null) {
            getCoordinationListener().onSpeakForbiddenChanged();
        }
    }
    
    public void notifyRefreshFromStart() {
        if (getCoordinationListener() != null) {
            boolean hasHistoryMessage = ChatManager.getInstance().getLectureService().loadingHistoryMessage();
            getCoordinationListener().onLoadingHistoryMessage(hasHistoryMessage);
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        setLectureRoom(null);
        getGettingLectureParticipantsTask().cancel();
        ChatManager.getInstance().getLectureService().leaveLecture(getLectureId(),
                getChatRoomId(), null);
        ChatManager.getInstance().getChatConnectionService()
                .removeChatConnectionListener(getConnectionListener());
    }
    
    private long getInitializedTime() {
        return getLectureRoom() != null ? getLectureRoom().getInitializedTime()
                : Long.MAX_VALUE;
    }
    
    @Override
    public boolean onInterceptMessage(Message message) {
        if (isMessageFilteredOut(message)) {
            Logger.e(TAG, "此条消息的发送者被禁言不能说话");
            return true;
        }
        if (message.getMsgType() == Message.Type.CMD) {
            CmdMessageBody body = (CmdMessageBody) message.getBody();
            int cmdType = body.getType();
            if (CmdUtils.isLectureCmd(cmdType)
                    && !message.isHistory()) {
                handleLectureCmdMsg(body, message.getIndex());
            }
        }
        return false;
    }
    
    @Override
    public boolean onInterceptMessages(List<Message> messages) {
        int index = 0;
        int size = messages.size();
        while (index < size) {
            if (onInterceptMessage(messages.get(index))) {
                messages.remove(index);
                size--;
            }
            else {
                index++;
            }
        }
        return messages.size() <= 0;
    }
    
    public void handleLectureCmdMsg(CmdMessageBody body, int msgIndex) {
        int type = body.getType();
        switch (type) {
            case CmdType.CMD_TYPE_LECTURE_STOP_TALK:
                handleStopTalkMsg(body, msgIndex);
                break;
            case CmdType.CMD_TYPE_LECTURE_PLAY_PPT:
                handlePlayPptMsg(body, msgIndex);
                break;
            case CmdType.CMD_TYPE_LECTURE_CHANGE_PPT:
                handleChangPptMsg(body, msgIndex);
                break;
            case CmdType.CMD_TYPE_LECTURE_DESTROYED:
                handleLectureDestroyed();
                break;
            default:
                break;
        }
    }
    
    private void handleChangPptMsg(CmdMessageBody body, int msgIndex) {
        if (getLectureRoom() == null) {
            return;
        }
        if (msgIndex >= getCurChangePptIndex()) {
            setCurChangePptIndex(msgIndex);
            List<String> ppt = LectureCmd.getImgUrls(body);
            getLectureRoom().changePpt(ppt);
            notifyPptChanged();
        }
        
    }
    
    private void handleStopTalkMsg(CmdMessageBody body, int msgIndex) {
        if (getLectureRoom() == null) {
            return;
        }
        
        String toStopUserId = LectureCmd.getToStopUserId(body);
        boolean isAllow = LectureCmd.isAllow(body);
        
        // 单个禁言
        if (!TextUtils.isEmpty(toStopUserId)) {
            if (isAllow) {
                getLectureRoom().removeForbiddenUser(toStopUserId);
            }
            else {
                getLectureRoom().addForbiddenUser(toStopUserId);
            }
        }
        
        if (msgIndex >= getCurForbiddenIndex()) {
            setCurForbiddenIndex(msgIndex);
            // 全局禁言
            if (TextUtils.isEmpty(toStopUserId)) {
                getLectureRoom().setSpeakAllForbidden(!isAllow);
            }
            notifySpeakForbiddenChanged();
        }
    }
    
    private void handlePlayPptMsg(CmdMessageBody body, int msgIndex) {
        if (getLectureRoom() == null) {
            return;
        }
        if (msgIndex >= getCurPptPlayedIndex()) {
            // String imgUrl = LectureCmd.getPptImageUrl(body);
            int imgIndex = LectureCmd.getPptIndex(body);
            if (imgIndex < getLectureRoom().getPptSize()) {
                getLectureRoom().setPptIndex(imgIndex);
                setCurPptPlayedIndex(msgIndex);
                notifyPptPlayed();
            }
        }
    }
    
    private void handleLectureDestroyed() {
        finishLecture(true);
    }
    
    public void playPpt(int pptIndex) {
        if (getLectureRoom() != null && getLectureRoom().hasPpt() && pptIndex >= 0
                && pptIndex < getLectureRoom().getPptSize() && isExpertRole()) {
            ChatManager.getInstance().getLectureService().playPpt(getLectureId(),
                    pptIndex, getLectureRoom().getPptImgUrl(pptIndex),
                    NetworkTime.currentTimeMillis(), null);
        }
    }
}

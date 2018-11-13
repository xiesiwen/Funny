package com.qingqing.base.im;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.easemob.chat.EMMessage;
import com.qingqing.api.proto.v1.LectureProto;
import com.qingqing.api.proto.v1.UserProto;
import com.qingqing.api.proto.v1.im.ImProto;
import com.qingqing.base.im.domain.CmdMsg;
import com.qingqing.base.log.Logger;
import com.qingqing.base.time.NetworkTime;
import com.qingqing.base.utils.ImageUrlUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by huangming on 2016/5/31.
 */
public class ChatRoomModel {
    
    private static Map<String, ChatRoomModel> sChatRoomModelMap = new HashMap<>();
    
    private String roomId;
    private final List<String> speakForbiddenUserList = new ArrayList<>();
    
    private UserProto.SimpleUserInfoV2 curUser;
    
    private ArrayList<Integer> roleType = new ArrayList<>();
    
    private String name;
    private boolean speakAllForbidden;
    
    private boolean isFinished;
    
    private List<String> ppts = new ArrayList<>();
    private int pptIndex = 0;
    
    private Handler sMainHandler = new Handler(Looper.getMainLooper());
    
    private List<Listener> mListeners = new ArrayList<>();
    
    private long mPlayTime;
    private final long mInitialTime;
    
    public ChatRoomModel(LectureProto.LectureChatRoomJoinResponse response) {
        this.roomId = response.chatRoomId;
        if (response.stopTalkQingqingChatUserIds != null) {
            for (String userId : response.stopTalkQingqingChatUserIds) {
                speakForbiddenUserList.add(userId);
            }
        }
        this.curUser = response.currentUserInfo;
        this.roleType.clear();
        this.roleType.add(response.currentRole);
        this.name = response.chatRoomName;
        this.speakAllForbidden = response.isAllStopTalk;
        // test
        // ppts.add("http://www.hangkong.com/uploadfile/2016/0122/20160122055049722.jpg");
        if (response.ppts != null) {
            for (String ppt : response.ppts) {
                ppts.add(ImageUrlUtil.getOriginImg(ppt));
            }
        }
        this.pptIndex = response.currentPptIndex;
        
        this.mPlayTime = this.mInitialTime = NetworkTime.currentTimeMillis();
    }
    
    public long getInitialTime() {
        return mInitialTime;
    }
    
    public List<String> getPPTs() {
        return new ArrayList<>(ppts);
    }
    
    public void addListener(Listener l) {
        if (mListeners.indexOf(l) < 0) {
            mListeners.add(l);
        }
    }
    
    public void removeListener(Listener l) {
        mListeners.remove(l);
    }
    
    public static void putModel(String roomId, ChatRoomModel model) {
        sChatRoomModelMap.put(roomId, model);
    }
    
    public static void removeModel(String roomId) {
        sChatRoomModelMap.remove(roomId);
    }
    
    public static ChatRoomModel getModel(String roomId) {
        return sChatRoomModelMap.get(roomId);
    }
    
    public void addSpeakForbiddenUser(String userName) {
        if (speakForbiddenUserList.indexOf(userName) < 0) {
            speakForbiddenUserList.add(userName);
            notifySpeakForbiddenChanged();
        }
    }
    
    public void removeSpeakForbiddenUser(String userName) {
        if (speakForbiddenUserList.remove(userName)) {
            notifySpeakForbiddenChanged();
        }
    }
    
    public List<String> getSpeakForbiddenUserList() {
        return new ArrayList<>(speakForbiddenUserList);
    }
    
    public boolean isCurrentUserSpeakForbidden() {
        boolean isAdminOrExpert = false;
        for (int i = 0; i < roleType.size(); i++) {
            if (roleType.get(i) == ImProto.ChatRoomRoleType.admin_chat_room_role_type
                    || roleType
                            .get(i) == ImProto.ChatRoomRoleType.mc_chat_room_role_type) {
                isAdminOrExpert = true;
                
            }
        }
        return ((speakAllForbidden || isUserSpeakForbidden(getCurUserName()))
                && !isAdminOrExpert) || isFinished;
    }
    
    public boolean isUserSpeakForbidden(String userName) {
        return speakForbiddenUserList.indexOf(userName) >= 0;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public boolean isSpeakAllForbidden() {
        return speakAllForbidden;
    }
    
    public void setSpeakAllForbidden(boolean speakAllForbidden) {
        this.speakAllForbidden = speakAllForbidden;
        notifySpeakForbiddenChanged();
    }
    
    public UserProto.SimpleUserInfoV2 getCurUser() {
        return curUser;
    }
    
    public String getCurUserName() {
        return curUser.qingqingUserId;
    }
    
    public ArrayList<Integer> getChatRoomType() {
        return roleType;
    }
    
    public int getSexType() {
        return curUser.sex;
    }
    
    public int getUserType() {
        return curUser.userType;
    }
    
    public String getHeadImage() {
        return curUser.newHeadImage;
    }
    
    public String getNick() {
        return curUser.nick;
    }
    
    public boolean isFinished() {
        return isFinished;
    }
    
    public void finishChatRoom() {
        isFinished = true;
        notifyFinished();
    }
    
    public boolean hasPPT() {
        return getPPTSize() > 0;
    }
    
    public int getPPTSize() {
        return ppts.size();
    }
    
    public String getPPTUrlBy(int index) {
        return ppts.get(index);
    }
    
    public void setPPTUrlBy(int index, String url) {
        ppts.remove(index);
        ppts.add(index, url);
    }
    
    public void setPPTIndex(int pptIndex) {
        if (pptIndex >= 0 && pptIndex < getPPTSize()) {
            this.pptIndex = pptIndex;
        }
    }
    
    public int getPPTIndex() {
        return pptIndex;
    }
    
    private void setPlayTime(long playTime) {
        this.mPlayTime = playTime;
    }
    
    public void changeRole(UserProto.SimpleUserInfoV2 curUser,
            ArrayList<Integer> roleType) {
        this.curUser = curUser;
        this.roleType.clear();
        this.roleType.addAll(roleType);
        notifyRoleChanged();
    }
    
    public void generateMsgExtFromUser(EMMessage message) {
        // TODO: 讲堂中暂不需设置 trm 身份
        ExtFieldParser.setExtUser(message, getCurUserName(), getNick(), getHeadImage(),
                getUserType(), getSexType(), getChatRoomType(), null);
    }
    
    private void notifyPPTChanged() {
        for (Listener l : mListeners) {
            l.onPPTChanged();
        }
    }
    
    private void notifyFinished() {
        for (Listener l : mListeners) {
            l.onFinished();
        }
    }
    
    private void notifySpeakForbiddenChanged() {
        for (Listener l : mListeners) {
            l.onSpeakForbiddenChanged();
        }
    }
    
    private void notifyRoleChanged() {
        for (Listener l : mListeners) {
            l.onRoleChanged();
        }
    }
    
    private void notifyRoomChanged() {
        for (Listener l : mListeners) {
            l.onRoomChanged();
        }
    }
    
    private void notifyPPTPlayed(int playedIndex, String playedUrl) {
        for (Listener l : mListeners) {
            l.onPPTPlayed(playedIndex, playedUrl);
        }
    }
    
    public void handleLectureCmdMsg(CmdMsg cmdMsg) {
        if (cmdMsg != null) {
            int type = cmdMsg.msgType;
            switch (type) {
                case CmdMsg.CMD_TYPE_LECTURE_STOP_TALK:
                    handleStopTalkMsg(cmdMsg);
                    break;
                case CmdMsg.CMD_TYPE_LECTURE_PLAY_PPT:
                    handlePlayPPTMsg(cmdMsg);
                    break;
                case CmdMsg.CMD_TYPE_LECTURE_CHANGE_ROOM:
                    handleChangeRoom(cmdMsg);
                    break;
                case CmdMsg.CMD_TYPE_LECTURE_CHANGE_PPT:
                    handleChangPPT(cmdMsg);
                    break;
                default:
                    break;
            }
        }
    }
    
    private void handleChangPPT(CmdMsg cmdMsg) {
        Bundle bundle = CmdMsgParser.parseCmdMsgBody(cmdMsg);
        
        final long sendTime = bundle.getLong(CmdMsg.Lecture.SEND_TIME);
        if (sendTime < mInitialTime) {
            return;
        }
        
        ppts.clear();
        
        String[] imgUrls = bundle.getStringArray(CmdMsg.Lecture.PPT_IMG_URLS);
        if (imgUrls != null) {
            for (String url : imgUrls) {
                ppts.add(ImageUrlUtil.getOriginImg(url));
            }
        }
        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                setPPTIndex(0);
                notifyPPTChanged();
            }
        });
    }
    
    private void handleStopTalkMsg(CmdMsg cmdMsg) {
        Bundle bundle = CmdMsgParser.parseCmdMsgBody(cmdMsg);
        final boolean isSpeakAllForbiddenMsg = CmdMsgParser
                .isLectureSpeakAllForbiddenMsg(bundle);
        final String speakForbiddenUserName = CmdMsgParser
                .getLectureSpeakForbiddenUserName(bundle);
        final boolean isSpeakForbidden = CmdMsgParser.isLectureSpeakForbidden(bundle);
        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (isSpeakAllForbiddenMsg) {
                    setSpeakAllForbidden(isSpeakForbidden);
                }
                else {
                    if (isSpeakForbidden) {
                        addSpeakForbiddenUser(speakForbiddenUserName);
                    }
                    else {
                        removeSpeakForbiddenUser(speakForbiddenUserName);
                    }
                }
            }
        });
    }
    
    private void handlePlayPPTMsg(CmdMsg cmdMsg) {
        Bundle bundle = CmdMsgParser.parseCmdMsgBody(cmdMsg);
        final String imgUrl = bundle.getString(CmdMsg.Lecture.PPT_IMG_URL);
        final int imgIndex = bundle.getInt(CmdMsg.Lecture.PPT_IMG_INDEX);
        final long sendTime = bundle.getLong(CmdMsg.Lecture.SEND_TIME);
        
        final int pptSize = getPPTSize();
        if (imgIndex < 0 || imgIndex >= pptSize) {
            Logger.e("ChatRoomModel", "handlePlayPPTMsg : imgIndex = " + imgIndex
                    + ", pptSize = " + pptSize);
            return;
        }
        // setPPTUrlBy(imgIndex, imgUrl);
        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                setPPTIndex(imgIndex);
                if (mPlayTime < sendTime) {
                    setPlayTime(sendTime);
                    notifyPPTPlayed(imgIndex, imgUrl);
                }
            }
        });
    }
    
    private void handleChangeRoom(CmdMsg cmdMsg) {
        Bundle bundle = CmdMsgParser.parseCmdMsgBody(cmdMsg);
        final String changeRoomId = bundle.getString(CmdMsg.Lecture.CHAT_ROOM_ID);
        if (TextUtils.isEmpty(changeRoomId)) {
            return;
        }
        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                removeModel(roomId);
                putModel(changeRoomId, ChatRoomModel.this);
                setRoomId(changeRoomId);
            }
        });
    }
    
    private void setRoomId(String roomId) {
        this.roomId = roomId;
        notifyRoomChanged();
    }
    
    public String getRoomId() {
        return roomId;
    }
    
    private void runOnMainThread(Runnable r) {
        if (Thread.currentThread().getId() != sMainHandler.getLooper().getThread()
                .getId()) {
            sMainHandler.post(r);
        }
        else {
            r.run();
        }
    }
    
    public interface Listener {
        void onFinished();
        
        void onSpeakForbiddenChanged();
        
        void onRoleChanged();
        
        void onRoomChanged();
        
        void onPPTPlayed(int playedIndex, String playedUrl);
        
        void onPPTChanged();
        
    }
    
}

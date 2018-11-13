package com.qingqing.base.nim;

/**
 * Created by huangming on 2016/8/18.
 */
public interface LectureCoordinationListener extends ChatRoomCoordinationListener {
    
    void onNumberOfParticipantsChanged();
    
    void onFinished(boolean needForceFinish);
    
    void onSpeakForbiddenChanged();
    
    void onRoleChanged();
    
    void onPptPlayed();
    
    void onPptChanged();
    
    void onLoadingHistoryMessage(boolean hasHistoryMessage);
}

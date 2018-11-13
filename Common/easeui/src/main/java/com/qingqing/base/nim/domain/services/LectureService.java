package com.qingqing.base.nim.domain.services;

import com.qingqing.base.interfaces.Observer;
import com.qingqing.base.nim.domain.Callback;
import com.qingqing.base.nim.domain.LectureRoom;

/**
 * Created by huangming on 2016/8/22.
 */
public interface LectureService {
    
    void joinLecture(String lectureId, Observer<LectureRoom> callback);
    
    void rejoinLecture(String lectureId, Observer<LectureRoom> callback);
    
    void leaveLecture(String lectureId, String chatRoomId, Callback callback);
    
    void playPpt(String lectureId, int pptIndex, String pptImgUrl, long playTime,
            Callback callback);
    
    boolean loadingHistoryMessage();
}

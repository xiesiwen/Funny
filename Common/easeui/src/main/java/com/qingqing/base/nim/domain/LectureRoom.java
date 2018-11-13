package com.qingqing.base.nim.domain;


import com.qingqing.base.time.NetworkTime;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by huangming on 2016/8/22.
 */
public class LectureRoom {
    
    private final String lectureId;
    private String roomId;
    private ChatRole role;
    private boolean finished = false;
    private String name;
    private boolean speakAllForbidden;

    private final long initializedTime;
    
    private Set<String> forbiddenUsers = new HashSet<>();
    
    private List<String> ppt = new ArrayList<>();
    private int pptIndex = 0;
    
    public LectureRoom(String lectureId) {
        this.lectureId = lectureId;
        this.initializedTime = NetworkTime.currentTimeMillis();
    }

    public long getInitializedTime() {
        return initializedTime;
    }

    public String getLectureId() {
        return lectureId;
    }
    
    public String getRoomId() {
        return roomId;
    }
    
    void setRoomId(String roomId) {
        this.roomId = roomId;
    }
    
    public ChatRole getRole() {
        return role;
    }
    
    void setRole(ChatRole role) {
        this.role = role;
    }
    
    public void changeRole(ChatRole chatRole) {
        setRole(chatRole);
    }
    
    void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public boolean isFinished() {
        return finished;
    }
    
    public void finish() {
        this.finished = true;
    }
    
    public List<String> getPpt() {
        return new ArrayList<>(ppt);
    }
    
    public boolean hasPpt() {
        return ppt.size() > 0;
    }
    
    public boolean isSpeakAllForbidden() {
        return speakAllForbidden;
    }
    
    public void setSpeakAllForbidden(boolean speakAllForbidden) {
        this.speakAllForbidden = speakAllForbidden;
    }
    
    public void setPptIndex(int pptIndex) {
        this.pptIndex = pptIndex;
    }
    
    public int getPptIndex() {
        return pptIndex;
    }
    
    void setPpt(List<String> ppt) {
        this.ppt.clear();
        if (ppt != null) {
            this.ppt.addAll(ppt);
        }
    }
    
    public void changePpt(List<String> ppt) {
        setPpt(ppt);
        setPptIndex(0);
    }
    
    void setForbiddenUsers(List<String> forbiddenUsers) {
        this.forbiddenUsers.clear();
        if (forbiddenUsers != null) {
            this.forbiddenUsers.addAll(forbiddenUsers);
        }
    }
    
    public void addForbiddenUser(String userId) {
        forbiddenUsers.add(userId);
    }
    
    public void removeForbiddenUser(String userId) {
        forbiddenUsers.remove(userId);
    }
    
    public int getPptSize() {
        return ppt.size();
    }
    
    public String getPptImgUrl(int pptIndex) {
        return pptIndex >= 0 && pptIndex < getPptSize() ? ppt.get(pptIndex) : "";
    }
    
    public boolean isUserInForbiddenList(String userId) {
        return forbiddenUsers.contains(userId);
    }
    
}

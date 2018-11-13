package com.qingqing.base.nim.domain;

import java.util.ArrayList;

/**
 * Created by huangming on 2016/8/19.
 */
public class ChatRole {
    
    private final ArrayList<Integer> roleType = new ArrayList<>();
    private final ChatContact roleUser;
    
    public ChatRole(ArrayList<Integer> roleType, ChatContact roleUser) {
        this.roleType.addAll(roleType);
        this.roleUser = roleUser;
    }
    
    public ArrayList<Integer> getRoleType() {
        return roleType;
    }
    
    public int getRoleSexType() {
        return getRoleUser().getSexType();
    }
    
    private ChatContact getRoleUser() {
        return roleUser;
    }
    
    public String getRoleId() {
        return getRoleUser().getId();
    }
    
    public String getRoleNick() {
        return getRoleUser().getNick();
    }
    
    public String getHeadImage() {
        return getRoleUser().getHeadImage();
    }
    
    public int getUserType() {
        return getRoleUser().getUserType();
    }
    
}

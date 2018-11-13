package com.qingqing.base.nim.domain;

import com.qingqing.api.proto.v1.UserProto;

/**
 * Created by huangming on 2016/8/17.
 */
public class ChatContact {
    
    private final String id;
    private String nick;
    private String headImage;
    private int sexType = UserProto.SexType.unknown;
    private int userType = UserProto.UserType.unknown_user_type;
    
    public ChatContact(String id) {
        this.id = id;
    }
    
    public String getId() {
        return id;
    }
    
    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getHeadImage() {
        return headImage;
    }

    public void setHeadImage(String headImage) {
        this.headImage = headImage;
    }

    public int getSexType() {
        return sexType;
    }
    
    public void setSexType(int sexType) {
        this.sexType = sexType;
    }

    public int getUserType() {
        return userType;
    }

    public void setUserType(int userType) {
        this.userType = userType;
    }

}

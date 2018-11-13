package com.qingqing.base.nim.domain;

/**
 * Created by huangming on 2016/8/17.
 */
public enum ChatType {
    
    Chat("chat"), GroupChat("GroupChat"), ChatRoom("chatroom");
    
    String value;
    
    ChatType(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static ChatType mapStringToValue(String value) {
        for (ChatType type : ChatType.values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        return null;
    }
    
}

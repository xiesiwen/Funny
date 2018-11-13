package com.qingqing.base.nim.domain;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by huangming on 2016/8/15. Information Holder
 */
public class Message {

    public static final int INVALID_INDEX = -1;

    private String msgId;
    private ChatType chatType;
    private Message.Type msgType;
    private Message.Direct direct;

    private ChatContact fromContact;
    private ChatContact toContact;

    private ChatRole role;

    private boolean listened;
    private boolean unread;

    private long msgTime;
    private int index = INVALID_INDEX;
    private MessageBody body;
    private Message.Status status;

    private boolean history;

    private Map<String, Object> attributes;

    Message(String msgId, ChatType chatType, Message.Type msgType) {
        this.msgId = msgId;
        this.chatType = chatType;
        this.msgType = msgType;
        this.msgTime = System.currentTimeMillis();
    }

    public void setHistory(boolean history) {
        this.history = history;
    }

    public boolean isHistory() {
        return history;
    }

    public String getId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public ChatType getChatType() {
        return chatType;
    }

    public Message.Type getMsgType() {
        return msgType;
    }

    public long getMsgTime() {
        return msgTime;
    }

    void setMsgTime(long msgTime) {
        this.msgTime = msgTime;
    }

    public int getIndex() {
        return index;
    }

    void setIndex(int index) {
        this.index = index;
    }

    public MessageBody getBody() {
        return body;
    }

    void setBody(MessageBody body) {
        this.body = body;
    }

    public Status getStatus() {
        return status;
    }

    void setStatus(Status status) {
        this.status = status;
    }

    public Direct getDirect() {
        return direct;
    }

    void setDirect(Direct direct) {
        this.direct = direct;
    }

    public String getFrom() {
        return fromContact.getId();
    }

    public String getTo() {
        return toContact.getId();
    }

    public ChatContact getFromContact() {
        return fromContact;
    }

    void setFromContact(ChatContact fromContact) {
        this.fromContact = fromContact;
    }

    public ChatContact getToContact() {
        return toContact;
    }

    void setToContact(ChatContact toContact) {
        this.toContact = toContact;
    }

    public void setRole(ChatRole role) {
        this.role = role;
    }

    public ChatRole getRole() {
        return role;
    }

    public boolean hasPlayedRole() {
        return role != null;
    }

    public boolean isListened() {
        return listened;
    }

    public void setListened(boolean listened) {
        this.listened = listened;
    }

    public boolean isUnread() {
        return unread;
    }

    void setUnread(boolean unread) {
        this.unread = unread;
    }

    void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    Map<String, Object> getAttributes() {
        return attributes;
    }

    public boolean hasAttribute(String key) {
        return attributes != null && attributes.containsKey(key) && attributes.get(key) != null;
    }

    public void setAttribute(String key, boolean attr) {
        if (this.attributes == null) {
            this.attributes = new HashMap<>();
        }

        this.attributes.put(key, Boolean.valueOf(attr));
    }

    public boolean getBooleanAttribute(String key) {
        return (Boolean) attributes.get(key);
    }

    public enum Status {
        SUCCESS, FAIL, IN_PROGRESS, CREATE
    }

    public enum Direct {
        SEND, RECEIVE
    }

    public enum Type {
        TEXT("txt"), IMAGE("img"), AUDIO("audio"), CMD("cmd"), UNKNOWN("unknown");

        String value;

        Type(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static Type mapStringToValue(String value) {
            for (Type type : Type.values()) {
                if (type.getValue().equals(value)) {
                    return type;
                }
            }
            return UNKNOWN;
        }
    }

}

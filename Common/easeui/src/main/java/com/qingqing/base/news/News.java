package com.qingqing.base.news;

/**
 * Created by huangming on 2016/11/29.
 */

public class News {

    private final String id;
    private final String body;
    private final int type;
    private final String bid;
    private final String conversationType;
    private boolean unread;

    private String from;
    private String to;
    private String conversationId;
    private News.Direct direct;

    private long createdTime;

    public News(String id, int type, String body, String bid, String conversationType) {
        this.id = id;
        this.body = body;
        this.type = type;
        this.bid = bid;
        this.conversationType = conversationType;
    }

    public String getConversationType() {
        return conversationType;
    }

    public String getFrom() {
        return from;
    }

    void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public Direct getDirect() {
        return direct;
    }

    void setDirect(News.Direct direct) {
        this.direct = direct;
    }

    public String getId() {
        return id;
    }

    public String getBody() {
        return body;
    }

    public int getType() {
        return type;
    }

    public boolean isUnread() {
        return unread;
    }

    void setUnread(boolean unread) {
        this.unread = unread;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long time) {
        this.createdTime = time;
    }

    public String getBid() {
        return bid;
    }

    public enum Direct {
        SEND,
        RECEIVE
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("News(");
        sb.append("id=").append(id).append("\n");
        sb.append("type=").append(type).append("\n");
        sb.append("unread=").append(unread).append("\n");
        sb.append("bid=").append(bid).append("\n");
        sb.append("createdTime=").append(createdTime).append("\n");
        sb.append("body=").append(body).append("\n");
        sb.append("conversationType=").append(conversationType).append("\n");
        sb.append(")");
        return sb.toString();
    }
}

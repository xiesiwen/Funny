package com.qingqing.base.news;

/**
 * Created by huangming on 2016/11/30.
 */

public class NewsEvent {

    private final NewsEventAction action;
    private final Object data;

    NewsEvent(NewsEventAction action) {
        this(action, null);
    }

    NewsEvent(NewsEventAction action, Object data) {
        this.action = action;
        this.data = data;
    }

    public NewsEventAction getAction() {
        return action;
    }

    public Object getData() {
        return data;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("News(");
        sb.append("action=").append(action.name()).append("\n");
        sb.append("data=").append(data).append("\n");
        sb.append(")");
        return sb.toString();
    }
}

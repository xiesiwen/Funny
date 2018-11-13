package com.qingqing.base.news;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by huangming on 2016/12/7.
 */

public class SystemNewsBody {

    private String title;
    private String content = "";

    private final Map<String, Object> attributes;

    SystemNewsBody() {
        this.attributes = new HashMap<>();
    }

    public String getTitle() {
        return title;
    }

    void setTitle(String title) {
        this.title = title;
    }

    void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    public String getStringAttribute(String key) {
        return (String) getAttribute(key);
    }

    public int getIntAttribute(String key) {
        return (int) getAttribute(key);
    }

    public long getLongAttribute(String key) {
        Object attribute = getAttribute(key);
        if (attribute instanceof Long) {
            return (long) attribute;
        }
        else {
            // 可能取到的是Integer，需要注意
            try {
                return Long.parseLong(attribute.toString());
            } catch (NumberFormatException e) {
                return 0;
            }
        }
    }

    public boolean containsKey(String key) {
        return attributes.containsKey(key);
    }

}

package com.qingqing.base.nim.domain;

/**
 * Created by huangming on 2016/8/18.
 */
public class TextMessageBody extends MessageBody {
    
    private final String text;
    
    TextMessageBody(String text) {
        this.text = text;
    }
    
    public String getText() {
        return text;
    }
}

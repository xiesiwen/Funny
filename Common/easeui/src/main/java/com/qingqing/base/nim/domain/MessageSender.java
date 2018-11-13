package com.qingqing.base.nim.domain;

/**
 * Created by huangming on 2016/8/19.
 */
public interface MessageSender{
    void send(final Message message, final Callback callback);
}

package com.qingqing.base.nim.domain;

import java.util.List;

/**
 * Created by huangming on 2016/8/23.
 */
interface MessageDispatcher {

    void dispatchMessage(Message message);

    void dispatchMessages(List<Message> messageList);

    void registerReceiver(MessageReceiver receiver);

    void unregisterReceiver(MessageReceiver receiver);

    void unregisterAll();

}

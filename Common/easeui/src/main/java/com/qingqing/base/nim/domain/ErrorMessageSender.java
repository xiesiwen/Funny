package com.qingqing.base.nim.domain;

import com.qingqing.base.nim.exception.SendError;

/**
 * Created by huangming on 2016/8/22.
 */
public class ErrorMessageSender extends AbstractMessageSender {
    
    ErrorMessageSender(MessageManager messageManager) {
        super(messageManager);
    }
    
    @Override
    protected void onSend(Message message, Callback callback) {
        message.setStatus(Message.Status.FAIL);
        if (callback != null) {
            callback.onError(new SendError("ErrorMessageSender failed"));
        }
    }
}

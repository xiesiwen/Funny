package com.qingqing.base.nim.domain;

import com.qingqing.base.interfaces.Observer;
import com.qingqing.base.log.Logger;
import com.qingqing.base.nim.domain.uploaders.FileMessageUploader;

/**
 * Created by huangming on 2016/8/19.
 */
abstract class AbstractMessageSender implements MessageSender {
    private static final String TAG = "AbstractMessageSender";
    
    private MessageManager messageManager;
    
    AbstractMessageSender(MessageManager messageManager) {
        this.messageManager = messageManager;
    }
    
    protected MessageManager getMessageManager() {
        return messageManager;
    }
    
    @Override
    public void send(Message message, Callback callback) {
        if (message.getBody() instanceof FileMessageBody) {
            upload(message, callback);
        }
        else {
            onSend(message, callback);
        }
    }
    
    private void upload(final Message message, final Callback callback) {
        final FileMessageBody body = (FileMessageBody) message.getBody();
        FileMessageUploader uploader = FileMessageUploader.getUploader(body);
        getMessageManager().setMsgBodyStatus(message, Message.Status.IN_PROGRESS);
        uploader.upload(body, new Observer<String>() {
            @Override
            public void onCompleted() {
                getMessageManager().setMsgBodyStatus(message, Message.Status.SUCCESS);
                onSend(message, callback);
            }
            
            @Override
            public void onError(Throwable e) {
                Logger.e(TAG, "Upload error", e);
                getMessageManager().setMsgBodyStatus(message, Message.Status.FAIL);
                getMessageManager().setMsgStatus(message, Message.Status.FAIL);
                if (callback != null) {
                    callback.onError(e);
                }
            }
            
            @Override
            public void onNext(String s) {
                if (body instanceof AudioMessageBody) {
                    ((AudioMessageBody) body).setMediaId(s);
                }
                else {
                    body.setRemoteUrl(s);
                }
            }
        });
    }
    
    protected abstract void onSend(Message message, Callback callback);
    
}

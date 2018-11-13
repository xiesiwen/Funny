package com.qingqing.base.nim.domain;

import com.qingqing.base.interfaces.Observer;
import com.qingqing.base.log.Logger;
import com.qingqing.base.nim.utils.MessageUtils;
import com.qingqing.base.task.DownloadAudioTask;

/**
 * Created by huangming on 2016/8/26.
 */
public class MessageBodyLoader {
    
    private static final String TAG = "MessageBodyLoadManager";
    
    private MessageManager messageManager;
    
    MessageBodyLoader() {
    }

     void setMessageManager(MessageManager messageManager) {
        this.messageManager = messageManager;
    }

    private MessageManager getMessageManager() {
        return messageManager;
    }
    
    public void loadMessageBody(final Message message) {
        if (MessageUtils.isBodyNeededToLoad(message)) {
            if (message.getBody() instanceof AudioMessageBody) {
                final AudioMessageBody body = (AudioMessageBody) message.getBody();
                new DownloadAudioTask(body.getMediaId())
                        .downloadAudio(new Observer<String>() {
                            @Override
                            public void onCompleted() {
                                getMessageManager().setMsgBodyStatus(message,
                                        Message.Status.SUCCESS);
                            }
                            
                            @Override
                            public void onError(Throwable e) {
                                getMessageManager().setMsgBodyStatus(message,
                                        Message.Status.FAIL);
                                Logger.e(TAG, e.getMessage(), e);
                            }
                            
                            @Override
                            public void onNext(String s) {
                                body.setLocalUrl(s);
                            }
                        });
            }
        }
    }
    
}

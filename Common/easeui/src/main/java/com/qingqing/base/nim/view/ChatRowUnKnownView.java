package com.qingqing.base.nim.view;

import android.content.Context;

import com.qingqing.base.nim.domain.Message;

/**
 * Created by huangming on 2016/8/18.
 */
public class ChatRowUnKnownView extends ChatRowDefaultView {

    public ChatRowUnKnownView(Context context, Message message) {
        super(context, message);
    }

    @Override
    protected int onInflateReceivedLayout() {
        return 0;
    }

    @Override
    protected int onInflateSentLayout() {
        return 0;
    }

}

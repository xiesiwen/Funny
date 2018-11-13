package com.qingqing.base.im.widget;

import android.content.Context;
import android.widget.BaseAdapter;

import com.easemob.chat.EMMessage;
import com.easemob.easeui.widget.chatrow.EaseChatRow;

/**
 * Created by huangming on 2015/12/29.
 */
public class ChatRowCmdNone extends EaseChatRow {

    public ChatRowCmdNone(Context context, EMMessage message, int position, BaseAdapter adapter) {
        super(context, message, position, adapter);
    }

    @Override
    protected void onInflatView() {

    }

    @Override
    protected void onFindViewById() {

    }

    @Override
    protected void onUpdateView() {

    }

    @Override
    protected void onSetUpView() {

    }

    @Override
    protected void onBubbleClick() {

    }
}

package com.qingqing.base.nim.view;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import com.qingqing.base.nim.PlayStatusObservable;
import com.qingqing.base.nim.PlayStatusObserver;
import com.qingqing.base.nim.domain.Message;
import com.qingqing.base.nim.utils.MessageUtils;

/**
 * Created by huangming on 2016/8/18.
 */
public abstract class ChatRowView extends RelativeLayout {
    
    private Message message;
    private ChatRowClickListener chatRowClickListener;
    private int position;
    
    private LayoutInflater inflater;
    
    public ChatRowView(Context context, Message message) {
        super(context);
        setMessage(message);
        
        inflater = LayoutInflater.from(getContext());
        initView();
    }
    
    private void initView() {
        inflateLayout();
        onFindViewById();
    }
    
    private void inflateLayout() {
        int layoutResId = isSendDirect() ? onInflateSentLayout()
                : onInflateReceivedLayout();
        if (layoutResId > 0) {
            getInflater().inflate(layoutResId, this);
        }
    }
    
    @LayoutRes
    protected abstract int onInflateReceivedLayout();
    
    @LayoutRes
    protected abstract int onInflateSentLayout();
    
    protected abstract void onFindViewById();
    
    protected void onSetupViewBy(Message preMessage, Message message) {}
    
    public LayoutInflater getInflater() {
        return inflater;
    }
    
    public void update(Message preMessage, Message message, int position,
            ChatRowClickListener chatRowClickListener, PlayStatusObservable playObservable) {
        updatePlayObservable(playObservable);
        setMessage(message);
        setPosition(position);
        setChatRowClickListener(chatRowClickListener);
        onSetupViewBy(preMessage, message);
    }

    protected void updatePlayObservable(PlayStatusObservable playStatusObservable){
    }
    
    private void setChatRowClickListener(ChatRowClickListener chatRowClickListener) {
        this.chatRowClickListener = chatRowClickListener;
    }
    
    public Message getMessage() {
        return message;
    }
    
    private void setMessage(Message message) {
        this.message = message;
    }
    
    protected boolean isSendDirect() {
        return MessageUtils.isSendDirect(getMessage());
    }
    
    private void setPosition(int position) {
        this.position = position;
    }
    
    public int getPosition() {
        return position;
    }
    
    public ChatRowClickListener getChatRowClickListener() {
        return chatRowClickListener;
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        setMessage(null);
        setChatRowClickListener(null);
    }
}

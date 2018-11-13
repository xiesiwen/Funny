package com.easemob.easeui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import com.easemob.chat.EMMessage;
import com.easemob.easeui.R;
import com.easemob.easeui.adapter.EaseMessageAdapter;
import com.easemob.easeui.widget.chatrow.EaseCustomChatRowProvider;
import com.qingqing.base.im.domain.ChatMessage;
import com.qingqing.base.view.NoAnimPtrListView;

public class EaseChatMessageList extends RelativeLayout {
    
    protected static final String TAG = "EaseChatMessageList";
    protected NoAnimPtrListView listView;
    protected Context context;
    protected int chatType;
    protected String toChatUsername;
    protected EaseMessageAdapter messageAdapter;
    protected boolean showUserNick;
    protected boolean showAvatar;
    protected Drawable myBubbleBg;
    protected Drawable otherBuddleBg;
    
    public EaseChatMessageList(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs);
    }
    
    public EaseChatMessageList(Context context, AttributeSet attrs) {
        super(context, attrs);
        parseStyle(context, attrs);
        init(context);
    }
    
    public EaseChatMessageList(Context context) {
        super(context);
        init(context);
    }
    
    private void init(Context context) {
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.ease_chat_message_list, this);
        listView = (NoAnimPtrListView) findViewById(R.id.list);
    }
    
    /**
     * init widget
     * 
     * @param toChatUsername
     * @param chatType
     * @param customChatRowProvider
     */
    public void init(String toChatUsername, int chatType,
            EaseCustomChatRowProvider customChatRowProvider) {
        this.chatType = chatType;
        this.toChatUsername = toChatUsername;
        
        messageAdapter = new EaseMessageAdapter(context, toChatUsername, chatType,
                listView);
        messageAdapter.setShowAvatar(showAvatar);
        messageAdapter.setShowUserNick(showUserNick);
        messageAdapter.setMyBubbleBg(myBubbleBg);
        messageAdapter.setOtherBuddleBg(otherBuddleBg);
        messageAdapter.setCustomChatRowProvider(customChatRowProvider);
        // 设置adapter显示消息
        listView.setAdapter(messageAdapter);
        
        refreshForceSelectLast();
    }
    
    protected void parseStyle(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs,
                R.styleable.EaseChatMessageList);
        showAvatar = ta.getBoolean(R.styleable.EaseChatMessageList_msgListShowUserAvatar,
                true);
        myBubbleBg = ta
                .getDrawable(R.styleable.EaseChatMessageList_msgListMyBubbleBackground);
        otherBuddleBg = ta
                .getDrawable(R.styleable.EaseChatMessageList_msgListMyBubbleBackground);
        showUserNick = ta.getBoolean(R.styleable.EaseChatMessageList_msgListShowUserNick,
                false);
        ta.recycle();
    }
    
    /**
     * 刷新列表
     */
    public void refresh() {
        if (messageAdapter != null) {
            messageAdapter.refresh();
        }
    }
    
    /**
     * 刷新列表，并且跳至最后一个item
     */
    public void refreshSelectLast() {
        if (messageAdapter != null) {
            messageAdapter.refreshSelectLast();
        }
    }
    
    public void refreshForceSelectLast() {
        if (messageAdapter != null) {
            messageAdapter.refreshForceSelectLast();
        }
    }
    
    /**
     * 刷新页面,并跳至给定position
     * 
     * @param position
     */
    public void refreshSeekTo(int position) {
        if (messageAdapter != null) {
            messageAdapter.refreshSeekTo(position);
        }
    }
    
    public void seekHistoryByUnreadNumber(int unreadNumber) {
        if (messageAdapter != null) {
            messageAdapter.seekHistoryByUnreadNumber(unreadNumber);
        }
    }
    
    public EaseMessageAdapter getMessageAdapter() {
        return messageAdapter;
    }
    
    /**
     * 获取listview
     * 
     * @return
     */
    public NoAnimPtrListView getListView() {
        return listView;
    }
    
    public EMMessage getItem(int position) {
        return messageAdapter.getItem(position);
    }
    
    /**
     * 设置是否显示用户昵称
     * 
     * @param showUserNick
     */
    public void setShowUserNick(boolean showUserNick) {
        this.showUserNick = showUserNick;
    }
    
    public boolean isShowUserNick() {
        return showUserNick;
    }
    
    public interface MessageListItemClickListener {
        void onResendClick(EMMessage message);
        
        /**
         * 控件有对气泡做点击事件默认实现，如果需要自己实现，return true。
         * 当然也可以在相应的chatrow的onBubbleClick()方法里实现点击事件
         * 
         * @param message
         * @return
         */
        boolean onBubbleClick(EMMessage message);
        
        void onBubbleLongClick(EMMessage message);
        
        void onUserAvatarClick(ChatMessage message);
        
        void onUserAvatarLongClick(ChatMessage message);
    }
    
    /**
     * 设置list item里控件的点击事件
     * 
     * @param listener
     */
    public void setItemClickListener(MessageListItemClickListener listener) {
        messageAdapter.setItemClickListener(listener);
    }
    
    /**
     * 设置自定义chatrow提供者
     * 
     * @param rowProvider
     */
    public void setCustomChatRowProvider(EaseCustomChatRowProvider rowProvider) {
        messageAdapter.setCustomChatRowProvider(rowProvider);
    }
}

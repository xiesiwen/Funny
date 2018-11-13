package com.qingqing.base.nim.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.qingqing.base.nim.PlayStatusObservable;
import com.qingqing.base.nim.domain.Message;
import com.qingqing.base.nim.domain.MessageRepository;
import com.qingqing.base.nim.view.ChatRowClickListener;
import com.qingqing.base.nim.view.ChatRowProvider;
import com.qingqing.base.nim.view.ChatRowView;
import com.qingqing.base.nim.view.ExtendedChatRowProvider;
import com.qingqing.base.spec.Spec;

import java.util.List;

/**
 * Created by huangming on 2016/8/17.
 */
public class MessageAdapter extends BaseAdapter implements MessageRepository {
    
    private Context context;
    private ChatRowProvider chatRowProvider;
    private ExtendedChatRowProvider extendedChatRowProvider;
    private ChatRowClickListener chatRowClickListener;
    
    private PlayStatusObservable playStatusObservable;
    
    private FilterableMessageList filterableMessages;
    
    public MessageAdapter(Context context, @NonNull ChatRowProvider chatRowProvider,
            ExtendedChatRowProvider extendedChatRowProvider,
            ChatRowClickListener chatRowClickListener) {
        this.context = context;
        this.chatRowProvider = chatRowProvider;
        this.extendedChatRowProvider = extendedChatRowProvider;
        this.chatRowClickListener = chatRowClickListener;
        this.filterableMessages = new FilterableMessageList();
    }
    
    public void setPlayStatusObservable(PlayStatusObservable playStatusObservable) {
        this.playStatusObservable = playStatusObservable;
    }
    
    private PlayStatusObservable getPlayStatusObservable() {
        return playStatusObservable;
    }
    
    public Context getContext() {
        return context;
    }
    
    private FilterableMessageList getFilterableMessages() {
        return filterableMessages;
    }
    
    private Message getMessage(int index) {
        return getFilterableMessages().getFilteredMessage(index);
    }
    
    private ChatRowProvider getChatRowProvider() {
        return chatRowProvider;
    }
    
    private ExtendedChatRowProvider getExtendedChatRowProvider() {
        return extendedChatRowProvider;
    }
    
    private ChatRowClickListener getChatRowClickListener() {
        return chatRowClickListener;
    }
    
    public void filterSpec(Spec<Message> spec) {
        getFilterableMessages().setFilteredSpec(spec);
        notifyDataSetChanged();
    }
    
    public void setMessages(List<Message> messages) {
        clear();
        getFilterableMessages().addAll(messages);
        notifyDataSetChanged();
    }
    
    public void removeMessage(Message message) {
        if (getFilterableMessages().remove(message)) {
            notifyDataSetChanged();
        }
    }
    
    public void addMessage(Message message) {
        if (getFilterableMessages().add(message)) {
            notifyDataSetChanged();
        }
    }
    
    public void addMessages(List<Message> messages) {
        if (messages != null && messages.size() > 0
                && getFilterableMessages().addAll(messages)) {
            notifyDataSetChanged();
        }
    }
    
    private void clear() {
        getFilterableMessages().clear();
    }
    
    public void destroy() {
        clear();
    }
    
    @Override
    public int getCount() {
        return getFilterableMessages().getFilteredSize();
    }
    
    @Override
    public Object getItem(int position) {
        return getMessage(position);
    }
    
    @Override
    public long getItemId(int position) {
        return position;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Message message = getMessage(position);
        if (convertView == null) {
            convertView = createChatRow(message);
        }
        ((ChatRowView) convertView).update(getMessage(position - 1), message, position,
                getChatRowClickListener(), getPlayStatusObservable());
        return convertView;
    }
    
    private ChatRowView createChatRow(Message message) {
        ChatRowView chatRowView = null;
        if (getExtendedChatRowProvider() != null) {
            chatRowView = getExtendedChatRowProvider().getChatRowView(getContext(),
                    message);
        }
        if (chatRowView == null) {
            chatRowView = getChatRowProvider().getChatRowView(getContext(), message);
        }
        return chatRowView;
    }
    
    @Override
    public int getItemViewType(int position) {
        if (getExtendedChatRowProvider() != null) {
            return getExtendedChatRowProvider().getChatRowViewType(getMessage(position));
        }
        if (getChatRowProvider() != null) {
            return getChatRowProvider().getChatRowViewType(getMessage(position));
        }
        return super.getItemViewType(position);
    }
    
    @Override
    public int getViewTypeCount() {
        int extendedChatRowTypeCount = getExtendedChatRowProvider() != null
                ? getExtendedChatRowProvider().getChatRowTypeCount() : 0;
        int chatRowTypeCount = getChatRowProvider() != null
                ? getChatRowProvider().getChatRowTypeCount() : 0;
        return extendedChatRowTypeCount + chatRowTypeCount;
    }
    
    @Override
    public Message getMessageBy(Spec<Message> spec) {
        return getFilterableMessages().getMessageBy(spec);
    }
    
    @Override
    public List<Message> getMessagesBy(Spec<Message> spec) {
        return getFilterableMessages().getMessagesBy(spec);
    }
}

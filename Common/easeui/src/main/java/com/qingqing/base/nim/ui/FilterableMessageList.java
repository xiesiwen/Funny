package com.qingqing.base.nim.ui;

import com.qingqing.base.nim.comparator.MessageIndexComparator;
import com.qingqing.base.nim.domain.Message;
import com.qingqing.base.nim.domain.MessageRepository;
import com.qingqing.base.spec.Spec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by huangming on 2016/8/29.
 */
class FilterableMessageList implements MessageRepository {
    
    private List<Message> messages = new ArrayList<>();
    private Spec<Message> filteredSpec;
    private int filteredSize;
    
    private List<Message> getMessages() {
        return messages;
    }
    
    private Spec<Message> getFilteredSpec() {
        return filteredSpec;
    }
    
    void setFilteredSpec(Spec<Message> filteredSpec) {
        this.filteredSpec = filteredSpec;
        recalculateFilteredSize();
    }
    
    boolean add(Message message) {
        getMessages().add(message);
        Collections.sort(getMessages(), new MessageIndexComparator());
        if (isFiltered(message)) {
            filteredSize++;
            return true;
        }
        return false;
    }
    
    boolean addAll(List<Message> messages) {
        boolean changed = false;
        getMessages().addAll(messages);
        Collections.sort(getMessages(), new MessageIndexComparator());
        for (Message message : messages) {
            if (isFiltered(message)) {
                filteredSize++;
                changed = true;
            }
        }
        return changed;
    }
    
    public boolean remove(Message message) {
        boolean changed = getMessages().remove(message);
        if (isFiltered(message)) {
            filteredSize--;
            return changed;
        }
        return false;
    }
    
    private boolean isFiltered(Message message) {
        return getFilteredSpec() == null || getFilteredSpec().isSatisfiedBy(message);
    }
    
    int getFilteredSize() {
        return filteredSize;
    }
    
    Message getFilteredMessage(int filteredIndex) {
        int size = size();
        int index = 0;
        for (int i = 0; i < size; i++) {
            Message message = getMessage(i);
            if (isFiltered(message)) {
                if (filteredIndex == index) {
                    return getMessage(i);
                }
                index++;
            }
        }
        return null;
    }
    
    private Message getMessage(int index) {
        return index >= 0 && index < size() ? getMessages().get(index) : null;
    }
    
    private int size() {
        return getMessages().size();
    }
    
    void clear() {
        getMessages().clear();
        filteredSize = 0;
    }
    
    @Override
    public Message getMessageBy(Spec<Message> spec) {
        List<Message> messageList = getMessages();
        for (Message message : messageList) {
            if (spec.isSatisfiedBy(message) && isFiltered(message)) {
                return message;
            }
        }
        return null;
    }
    
    @Override
    public List<Message> getMessagesBy(Spec<Message> spec) {
        List<Message> resultMessages = new ArrayList<>();
        List<Message> messageList = getMessages();
        for (Message message : messageList) {
            if (spec.isSatisfiedBy(message) && isFiltered(message)) {
                resultMessages.add(message);
            }
        }
        return resultMessages;
    }
    
    private void recalculateFilteredSize() {
        filteredSize = 0;
        for (Message message : getMessages()) {
            if (isFiltered(message)) {
                filteredSize++;
            }
        }
    }
}

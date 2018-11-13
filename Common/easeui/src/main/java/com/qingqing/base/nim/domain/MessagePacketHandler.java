package com.qingqing.base.nim.domain;

import android.os.Handler;
import android.util.SparseArray;

import com.qingqing.base.bean.UserBehaviorLogExtraData;
import com.qingqing.base.core.UserBehaviorLogManager;
import com.qingqing.base.data.StatisticalDataConstants;
import com.qingqing.base.interfaces.Observer;
import com.qingqing.base.log.Logger;
import com.qingqing.base.utils.ExecUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by huangming on 2016/8/23.
 */
class MessagePacketHandler extends AbstractMessageDispatcher {
    private static final String TAG = "whuthmMessagePacketHandler";
    
    private static final long DELAY_MILLIS = 2 * 1000;
    private static final int INVALID_INDEX = -1;
    
    private final String id;
    private final ChatType chatType;
    private SparseArray<Message> messageArray;
    private SparseArray<Status> statusArray;
    private int currentIndex = INVALID_INDEX;
    private int endIndex = INVALID_INDEX;
    private int startIndex = INVALID_INDEX;
    private Handler timerHandler;
    private long timerDelayMillis = DELAY_MILLIS;
    private final int handlerIndex;
    
    private boolean stopped;
    private boolean started;
    
    private final StatisticsData statisticsData;
    
    public MessagePacketHandler(String id, ChatType chatType,
            int handlerIndex) {
        this.id = id;
        this.chatType = chatType;
        this.messageArray = new SparseArray<>();
        this.statusArray = new SparseArray<>();
        this.timerHandler = new TimerHandler();
        this.handlerIndex = handlerIndex;
        this.statisticsData = new StatisticsData(id);
    }
    
    private StatisticsData getStatisticsData() {
        return statisticsData;
    }
    
    private int getHandlerIndex() {
        return handlerIndex;
    }
    
    public String getId() {
        return id;
    }
    
    public ChatType getChatType() {
        return chatType;
    }
    
    private SparseArray<Message> getMessageArray() {
        return messageArray;
    }
    
    private SparseArray<Status> getStatusArray() {
        return statusArray;
    }
    
    int getCurrentIndex() {
        return currentIndex;
    }
    
    void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
    }
    
    int getEndIndex() {
        return endIndex;
    }
    
    private void setEndIndexWithComparison(int endIndex) {
        this.endIndex = Math.max(this.endIndex, endIndex + 1);
    }
    
    private Handler getTimerHandler() {
        return timerHandler;
    }
    
    private long getTimerDelayMillis() {
        return timerDelayMillis;
    }
    
    private void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }
    
    private int getStartIndex() {
        return startIndex;
    }
    
    private void setStarted(boolean started) {
        this.started = started;
    }
    
    private boolean isStarted() {
        return started;
    }
    
    private void startBy(int msgIndex) {
        setStarted(true);
        setStartIndex(msgIndex);
        setCurrentIndex(msgIndex);
        setEndIndexWithComparison(msgIndex);
        Logger.i(TAG, "start : startIndex = " + getStartIndex() + ", currentIndex = "
                + getCurrentIndex() + ", endIndex = " + getEndIndex());
    }
    
    private boolean isStopped() {
        return stopped;
    }
    
    private void setStopped(boolean stopped) {
        this.stopped = stopped;
    }
    
    void stop() {
        ExecUtil.execute(ExecUtil.MODE_COMPUTE, new Runnable() {
            @Override
            public void run() {
                stopInternal();
            }
        });
    }
    
    private void stopInternal() {
        Logger.i(TAG,
                "stopInternal : startIndex = " + getStartIndex() + ", currentIndex = "
                        + getCurrentIndex() + ", endIndex = " + getEndIndex());
        unregisterAll();
        setStopped(true);
        calculateStatisticalData();
        reportStatisticalData();
        getMessageArray().clear();
        getStatusArray().clear();
        getTimerHandler().removeMessages(getHandlerIndex());
    }
    
    private void calculateStatisticalData() {
        final StatisticsData data = getStatisticsData();
        final int startIndex = getStartIndex();
        final int endIndex = getEndIndex();
        data.setMsgTotalCount(endIndex - startIndex);
        
        int displayedInUIMsgCount = 0;
        int missingMsgCount = 0;
        boolean canRelease = true;
        
        for (int i = startIndex; i < endIndex; i++) {
            Status status = getStatusArray().get(startIndex);
            if (isIndexPulled(status)) {
                if (status == Status.Success && canRelease) {
                    displayedInUIMsgCount++;
                }
                else if (status == Status.Failed) {
                    missingMsgCount++;
                }
            }
            else {
                canRelease = false;
                missingMsgCount++;
            }
        }
        data.setDisplayedInUIMsgCount(displayedInUIMsgCount);
        data.setMissingMsgCount(missingMsgCount);
    }
    
    private void reportStatisticalData() {
        final StatisticsData data = getStatisticsData();
        Logger.i(TAG, "reportStatisticalData : " + data.toString());
        UserBehaviorLogManager.INSTANCE().saveEventLog(
                StatisticalDataConstants.LOG_QUIT_LECTURE,
                new UserBehaviorLogExtraData.Builder()
                        .addExtraData(StatisticalDataConstants.LOG_EXTRA_LECTURE_ID,
                                data.getConversationId())
                        .addExtraData(StatisticalDataConstants.LOG_EXTRA_IM_TOTAL_CNT,
                                Integer.toString(data.getMsgTotalCount()))
                        .addExtraData(StatisticalDataConstants.LOG_EXTRA_IM_SHOW_CNT,
                                Integer.toString(data.getDisplayedInUIMsgCount()))
                        .addExtraData(StatisticalDataConstants.LOG_EXTRA_IM_COVER_CNT,
                                Integer.toString(data.getCompensatedMsgCount()))
                        .addExtraData(
                                StatisticalDataConstants.LOG_EXTRA_IM_COVER_SUCESS_CNT,
                                Integer.toString(data.getCompensatedSuccessMsgCount()))
                        .addExtraData(
                                StatisticalDataConstants.LOG_EXTRA_IM_COVER_REQUEST_CNT,
                                Integer.toString(data.getCompensationRequestedCount()))
                        .addExtraData(StatisticalDataConstants.LOG_EXTRA_IM_LOSE_CNT,
                                Integer.toString(data.getMissingMsgCount()))
                        .addExtraData(StatisticalDataConstants.LOG_EXTRA_MQTT_CNT,
                                Integer.toString(data.getLongConnectionMsgCount()))
                        .build());
    }
    
    void push(Message message) {
        int msgIndex = message.getIndex();
        if (!isStarted()) {
            startBy(msgIndex);
        }
        if (msgIndex >= getStartIndex()) {
            getStatisticsData().increaseLongConnectionMsgCount();
        }
        List<Message> messageList = new ArrayList<>(1);
        messageList.add(message);
        push(msgIndex, msgIndex, messageList, Status.Success);
    }
    
    private void push(int rangeStartIndex, int rangeEndIndex, List<Message> messageList,
            Status status) {
        if (!isStarted()) {
            Logger.e(TAG, "MessagePacketHandler is not started!!!");
            return;
        }
        if (isStopped()) {
            Logger.e(TAG, "MessagePacketHandler is stopped!!!");
            return;
        }
        final int currentIndex = getCurrentIndex();
        Logger.i(TAG,
                "push : " + " rangeStartIndex =  " + rangeStartIndex
                        + ", rangeEndIndex = " + rangeEndIndex + ", currentIndex = "
                        + currentIndex);
        setEndIndexWithComparison(rangeEndIndex);
        if (messageList != null && messageList.size() > 0) {
            for (Message message : messageList) {
                int msgIndex = message.getIndex();
                setEndIndexWithComparison(msgIndex);
                if (msgIndex >= currentIndex) {
                    getMessageArray().put(msgIndex, message);
                }
                else {
                    Logger.e(TAG, "dispose message by index(" + msgIndex + ")");
                }
            }
        }

        for (int index = Math.max(currentIndex,
                rangeStartIndex); index <= rangeEndIndex; index++) {
            Status preStatus = getStatusArray().get(index);
            if (preStatus != Status.Success) {
                getStatusArray().put(index, status);
            }
        }
        
        pop();
        
        if (getCurrentIndex() >= getEndIndex()) {
            getTimerHandler().removeMessages(getHandlerIndex());
        }
        else {
            if (!getTimerHandler().hasMessages(getHandlerIndex())) {
                getTimerHandler().sendEmptyMessageDelayed(getHandlerIndex(),
                        getTimerDelayMillis());
            }
        }
    }
    
    private void pop() {
        final int currentIndex = getCurrentIndex();
        int releasableIndex = getReleasableIndex();
        
        List<Message> messageList = new ArrayList<>();
        for (int i = currentIndex; i <= releasableIndex; i++) {
            Message message = getMessageArray().get(i);
            if (message != null) {
                getMessageArray().remove(i);
                messageList.add(message);
            }
        }
        Logger.i(TAG,
                "pop : currentIndex = " + currentIndex + ", releasableIndex = "
                        + releasableIndex + ", endIndex = " + getEndIndex()
                        + ", releaseSize = " + messageList.size());
        
        setCurrentIndex(releasableIndex + 1);
        if (messageList.size() > 0) {
            dispatchMessages(messageList);
        }
    }
    
    /**
     * 获取可释放的EndIndex
     */
    private int getReleasableIndex() {
        final int currentIndex = getCurrentIndex();
        final int endIndex = getEndIndex();
        for (int i = currentIndex; i < endIndex; i++) {
            Status status = getStatusArray().get(i);
            if (!isIndexPulled(status)) {
                return i - 1;
            }
        }
        return endIndex - 1;
    }
    
    private boolean isIndexPulled(Status status) {
        return status == Status.Success || status == Status.Failed;
    }
    
    /**
     * 获取将要被拉取的IndexArray
     *
     * 并将状态重置为In_Progress
     */
    private int[] getPendingPulledIndexArray() {
        final int currentIndex = getCurrentIndex();
        final int endIndex = getEndIndex();
        List<Integer> indexList = new ArrayList<>();
        for (int i = currentIndex; i < endIndex; i++) {
            if (getStatusArray().get(i) == null) {
                indexList.add(i);
                getStatusArray().put(i, Status.In_Progress);
            }
        }
        int size = indexList.size();
        int[] indexArray = new int[size];
        for (int i = 0; i < size; i++) {
            indexArray[i] = indexList.get(i);
        }
        return indexArray;
    }
    
    /**
     * 通过IndexArray开始补偿Message
     */
    private void pullMessagesIfNeeded() {
        final int[] indexArray = getPendingPulledIndexArray();
        if (indexArray != null && indexArray.length > 0) {
            Logger.i(TAG,
                    "start completion : currentIndex = " + getCurrentIndex()
                            + ", endIndex = " + getEndIndex() + ", indexArray = "
                            + Arrays.toString(indexArray));
            final int rangeStartIndex = indexArray[0];
            final int rangeEndIndex = indexArray[indexArray.length - 1];
            getStatisticsData().addCompensatedMsgCount(indexArray.length);
            getStatisticsData().increaseCompensationRequestedCount();
            new MessageByIndexLoader(getId(), getChatType(), indexArray)
                    .loadMessages(new Observer<List<Message>>() {
                        @Override
                        public void onCompleted() {}
                        
                        @Override
                        public void onError(Throwable e) {
                            Logger.e(TAG,
                                    "completion failed : conversation id = " + getId()
                                            + ", rangeStartIndex = " + rangeStartIndex
                                            + ", rangeEndIndex = " + rangeEndIndex);
                            ExecUtil.execute(ExecUtil.MODE_COMPUTE,
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            push(rangeStartIndex, rangeEndIndex, null,
                                                    Status.Failed);
                                        }
                                    });
                        }
                        
                        @Override
                        public void onNext(final List<Message> messages) {
                            Logger.i(TAG, "completion success : conversation id = "
                                    + getId() + ", rangeStartIndex = " + rangeStartIndex
                                    + ", rangeEndIndex = " + rangeEndIndex + ", size = "
                                    + messages.size() + ", indexArray = "
                                    + Arrays.toString(indexArray));
                            getStatisticsData()
                                    .addCompensatedSuccessMsgCount(indexArray.length);
                            ExecUtil.execute(ExecUtil.MODE_COMPUTE,
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            push(rangeStartIndex, rangeEndIndex, messages,
                                                    Status.Success);
                                        }
                                    });
                        }
                    });
        }
        
    }
    
    private class TimerHandler extends Handler {
        
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            if (msg.what == getHandlerIndex()) {
                pullMessagesIfNeeded();
            }
        }
    }
    
    private enum Status {
        In_Progress, Success, Failed
    }
    
    private static class StatisticsData {
        
        private final String conversationId;
        
        // MQTT消息数
        private int longConnectionMsgCount;
        // 消息展示数
        private int displayedInUIMsgCount;
        // 消息总数(消息结束id - 消息起始id)
        private int msgTotalCount;
        
        // 补偿消息数
        private int compensatedMsgCount;
        // 补偿成功消息数
        private int compensatedSuccessMsgCount;
        // 补偿请求数
        private int compensationRequestedCount;
        
        // 丢失消息数(未能在UI上展示)
        int missingMsgCount;
        
        StatisticsData(String conversationId) {
            this.conversationId = conversationId;
        }
        
        public String getConversationId() {
            return conversationId;
        }
        
        public int getLongConnectionMsgCount() {
            return longConnectionMsgCount;
        }
        
        public void increaseLongConnectionMsgCount() {
            longConnectionMsgCount++;
        }
        
        public int getDisplayedInUIMsgCount() {
            return displayedInUIMsgCount;
        }
        
        public void setDisplayedInUIMsgCount(int displayedInUIMsgCount) {
            this.displayedInUIMsgCount = displayedInUIMsgCount;
        }
        
        public int getMsgTotalCount() {
            return msgTotalCount;
        }
        
        public void setMsgTotalCount(int msgTotalCount) {
            this.msgTotalCount = msgTotalCount;
        }
        
        public int getCompensatedMsgCount() {
            return compensatedMsgCount;
        }
        
        public void addCompensatedMsgCount(int delta) {
            this.compensatedMsgCount += delta;
        }
        
        public int getCompensatedSuccessMsgCount() {
            return compensatedSuccessMsgCount;
        }
        
        public void addCompensatedSuccessMsgCount(int delta) {
            this.compensatedSuccessMsgCount += delta;
        }
        
        public int getCompensationRequestedCount() {
            return compensationRequestedCount;
        }
        
        public void increaseCompensationRequestedCount() {
            this.compensationRequestedCount++;
        }
        
        public int getMissingMsgCount() {
            return missingMsgCount;
        }
        
        public void setMissingMsgCount(int missingMsgCount) {
            this.missingMsgCount = missingMsgCount;
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Conversation(");
            sb.append(getConversationId());
            sb.append(")\n{\n消息总数:");
            sb.append(getMsgTotalCount());
            sb.append("\nMQTT消息数:");
            sb.append(getLongConnectionMsgCount());
            sb.append("\n丢失消息数:");
            sb.append(getMissingMsgCount());
            sb.append("\n消息展示数:");
            sb.append(getDisplayedInUIMsgCount());
            sb.append("\n补偿请求数:");
            sb.append(getCompensationRequestedCount());
            sb.append("\n补偿消息数:");
            sb.append(getCompensatedMsgCount());
            sb.append("\n补偿成功消息数:");
            sb.append(getCompensatedSuccessMsgCount());
            sb.append("\n}");
            return sb.toString();
        }
    }
    
}

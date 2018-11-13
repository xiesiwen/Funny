package com.qingqing.base.im;

import android.content.Context;
import android.text.TextUtils;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.easemob.chat.FileMessageBody;
import com.easemob.chat.VoiceMessageBody;
import com.qingqing.base.data.SPWrapper;
import com.qingqing.base.log.Logger;
import com.qingqing.base.media.AudioBatchController;
import com.qingqing.base.media.MediaFile;
import com.qingqing.base.media.MediaFileFactory;
import com.qingqing.base.media.MediaPlayerController;
import com.qingqing.base.utils.ExecUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by huangming on 2016/5/26.
 * Audio Message批处理器
 * 拉取音频数据
 */
public class AudioBatchProcesser {

    private static final String TAG = "AudioBatchProcesser";

    /**
     * K:MessageId
     * V:Entry ,对应一个EMMessage和MediaPlayerController
     */
    private Map<String, Entry> mAudioMessageMap = new HashMap<>();

    private String mCurConversationId = "";

    private Object mLock = new Object();

    /**
     * K:会话ID
     * 一个会话对应一个音频顺序批量控制器
     */
    private Map<String, AudioBatchController> mBatchControllerMap = new HashMap<>();

    private static AudioBatchProcesser sInstance;

    private Context mContext;

    private SPWrapper mSpWrapper;

    private Map<String, List<ListenedMsgEntry>> mListenedAudioMap = new HashMap<>();

    private static final int LAST_LISTENED_MESSAGE_LIST_SIZE = 10;

    private AudioBatchProcesser(Context context) {
        mContext = context.getApplicationContext();
        mSpWrapper = new SPWrapper("im_audio_listened");
    }

    public static AudioBatchProcesser getInstance(Context context) {
        if (sInstance == null) {
            synchronized (AudioBatchProcesser.class) {
                if (sInstance == null) {
                    sInstance = new AudioBatchProcesser(context);
                }
            }
        }
        return sInstance;
    }

    public void saveListenedAudioMessage(EMMessage message) {
        if (message != null && message.getType() == EMMessage.Type.VOICE && message.direct == EMMessage.Direct.RECEIVE && message.isListened()) {
            String conversationId = ChatManager.getInstance().getConversationId(message);
            if (!TextUtils.isEmpty(conversationId)) {
                boolean change = false;
                List<ListenedMsgEntry> listenedMessageList = getListenedMessageList(conversationId);
                if (!listenedMessageList.contains(message.getMsgId())) {
                    int size = listenedMessageList.size();
                    for (int i = 0; i <= size - LAST_LISTENED_MESSAGE_LIST_SIZE && i < size; i++) {
                        listenedMessageList.remove(0);
                        change = true;
                    }
                    ListenedMsgEntry msgEntry = new ListenedMsgEntry(message.getMsgId(), message.getMsgTime());
                    if (!listenedMessageList.contains(msgEntry)) {
                        change = true;
                        listenedMessageList.add(msgEntry);
                        Collections.sort(listenedMessageList, new ListenedMsgComparator());
                    }
                    if (change) {
                        StringBuilder messageListStr = new StringBuilder();
                        for (ListenedMsgEntry entry : listenedMessageList) {
                            messageListStr.append(entry.getKey() + ":" + entry.getValue() + ";");
                        }
                        mSpWrapper.put(conversationId, messageListStr.toString());
                    }
                }
            }
        }
    }

    public boolean isAudioMessageListened(EMMessage message) {
        if (message != null && message.getType() == EMMessage.Type.VOICE && message.direct == EMMessage.Direct.RECEIVE && !message.isListened()) {
            String conversationId = ChatManager.getInstance().getConversationId(message);
            if (!TextUtils.isEmpty(conversationId)) {
                List<ListenedMsgEntry> listenedMessageList = getListenedMessageList(conversationId);
                return listenedMessageList.contains(new ListenedMsgEntry(message.getMsgId(), message.getMsgTime()));
            }
        }
        return false;
    }

    List<ListenedMsgEntry> getListenedMessageList(String conversationId) {
        List<ListenedMsgEntry> listenedMessageList = mListenedAudioMap.get(conversationId);
        if (listenedMessageList == null) {
            listenedMessageList = new ArrayList<>(0);
            mListenedAudioMap.put(conversationId, listenedMessageList);
            String listStr = mSpWrapper.getString(conversationId, "");
            String[] messageArray = listStr.split(";");
            if (messageArray != null && messageArray.length > 0) {
                int length = messageArray.length;
                for (int i = length - 1; i >= 0 && i >= length - LAST_LISTENED_MESSAGE_LIST_SIZE; i--) {
                    if (!TextUtils.isEmpty(messageArray[i])) {
                        try {
                            String[] msgInfo = messageArray[i].split(":");
                            listenedMessageList.add(0, new ListenedMsgEntry(msgInfo[0], Long.parseLong(msgInfo[1])));
                        } catch (Exception e) {
                        }
                    }
                }
                Collections.sort(listenedMessageList, new ListenedMsgComparator());
            }
        }
        return listenedMessageList;
    }

    static class ListenedMsgEntry implements Map.Entry<String, Long> {

        final String msgId;
        final Long msgTime;

        ListenedMsgEntry(String msgId, Long msgTime) {
            this.msgId = msgId;
            this.msgTime = msgTime;
        }

        @Override
        public String getKey() {
            return msgId;
        }

        @Override
        public Long getValue() {
            return msgTime;
        }

        @Override
        public Long setValue(Long object) {
            throw new RuntimeException("Value final");
        }

        @Override
        public int hashCode() {
            int result = 17;
            result = 37 * result + msgId.hashCode();
            result = 37 * result + msgTime.hashCode();
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (o instanceof ListenedMsgEntry) {
                ListenedMsgEntry other = (ListenedMsgEntry) o;
                return other.msgId.equals(this.msgId) && other.msgId.equals(this.msgId);
            } else {
                return false;
            }
        }
    }

    static class ListenedMsgComparator implements Comparator<ListenedMsgEntry> {

        @Override
        public int compare(ListenedMsgEntry lhs, ListenedMsgEntry rhs) {
            if (lhs.getValue() > rhs.getValue()) {
                return 1;
            } else if (lhs.getValue().equals(rhs.getValue())) {
                return 0;
            } else {
                return -1;
            }
        }
    }

    public void joinConversation(String curConversationId) {
        synchronized (mLock) {
            this.mCurConversationId = curConversationId;
        }
    }

    public void leaveConversation() {
        synchronized (mLock) {
            this.mCurConversationId = "";
        }
    }

    boolean isConversationVisible() {
        return !TextUtils.isEmpty(mCurConversationId);
    }

    /**
     * 异步fetch EMMessage的音频文件
     */
    public void asyncFetchMessageIfNeeded(final EMMessage... messages) {

        if (messages == null || messages.length == 0) {
            return;
        }
        for (final EMMessage message : messages) {
            if (message.getType() == EMMessage.Type.VOICE && message.getBody() instanceof FileMessageBody) {
                addAudioMessageIfNeeded(message);
                final FileMessageBody fileMessageBody = (FileMessageBody) message.getBody();
                File file = new File(fileMessageBody.getLocalUrl());
                if (message.status == EMMessage.Status.SUCCESS && file.isFile() && file.exists()) {
                    Logger.w(TAG, "message have fetched");
                    autoAddAudioToBatchControllerIfNecessary(message);
                } else {
                    fileMessageBody.downloadCallback = new EMCallBack() {
                        @Override
                        public void onSuccess() {
                            processAudioMessage(message);
                            notifyAudioMessage(message);
                            fileMessageBody.downloadCallback = null;
                            autoAddAudioToBatchControllerIfNecessary(message);
                        }

                        @Override
                        public void onError(int i, String s) {
                            fileMessageBody.downloadCallback = null;
                        }

                        @Override
                        public void onProgress(int i, String s) {
                        }
                    };
                    if (message.status == EMMessage.Status.INPROGRESS) {
                        Logger.w(TAG, "message is in progress");
                    } else {
                        EMChatManager.getInstance().asyncFetchMessage(message);
                    }
                }
            }
        }
    }

    void autoAddAudioToBatchControllerIfNecessary(EMMessage message) {
        synchronized (mLock) {
            String conversationId = ChatManager.getInstance().getConversationId(message);
            if (message.direct == EMMessage.Direct.RECEIVE
                    && message.getType() == EMMessage.Type.VOICE
                    && !message.getFrom().equals(ChatManager.getInstance().getCurrentUserName()) && mCurConversationId.equals(conversationId)) {
                AudioBatchController batchController = getAudioBatchController(ChatManager.getInstance().getConversationId(message));
                if (batchController.isStarted()) {
                    startAudioBatchController(conversationId, message.getMsgId());
                }
            }
        }
    }

    void addAudioMessageIfNeeded(final EMMessage message) {
        Entry entry = mAudioMessageMap.get(message.getMsgId());
        if (entry == null) {
            MediaFile mediaFile = createAudioFileBy(message);
            entry = new Entry(message, MediaPlayerController.audio(mContext, mediaFile));
        }
        mAudioMessageMap.put(message.getMsgId(), entry);
    }

    private void processAudioMessage(final EMMessage message) {
        Entry entry = mAudioMessageMap.get(message.getMsgId());
        if (entry == null) {
            MediaFile mediaFile = createAudioFileBy(message);
            entry = new Entry(message, MediaPlayerController.audio(mContext, mediaFile));
            mAudioMessageMap.put(message.getMsgId(), entry);
        }
        updateMediaFile(message, entry.controller.getMediaFile());
    }


    private MediaFile createAudioFileBy(EMMessage message) {
        if (message != null
                && (message.getType() == EMMessage.Type.VOICE)
                && (message.getBody() instanceof VoiceMessageBody)) {
            VoiceMessageBody messageBody = (VoiceMessageBody) message.getBody();
            long duration = messageBody.getLength();
            return MediaFileFactory.audio(message.getMsgId(),
                    messageBody.getRemoteUrl(),
                    duration,
                    duration,messageBody.getLocalUrl());
        }
        return MediaFile.NONE;
    }

    private void notifyAudioMessage(final EMMessage message) {
        //message.direct == EMMessage.Direct.RECEIVE &&
        if (message.status == EMMessage.Status.SUCCESS) {
            ExecUtil.executeUI(new Runnable() {
                @Override
                public void run() {
                    //notify
                }
            });
        }
    }

    void updateMediaFile(EMMessage message, MediaFile mediaFile) {
        if (message != null && mediaFile != null && message.getType() == EMMessage.Type.VOICE && message.getBody() instanceof VoiceMessageBody) {
            VoiceMessageBody voiceMessageBody = (VoiceMessageBody) message.getBody();
            mediaFile.setLocalUrl(voiceMessageBody.getLocalUrl());
            mediaFile.setDuration(voiceMessageBody.getLength());
            mediaFile.setSize(voiceMessageBody.getLength());
        }
    }


    public final MediaPlayerController getAndAddMediaPlayerController(EMMessage message) {
        addAudioMessageIfNeeded(message);
        return getMediaPlayerController(message.getMsgId());
    }

    public EMMessage getMessage(String id) {
        Entry entry = mAudioMessageMap.get(id);
        return entry != null ? entry.message : null;
    }

    public MediaPlayerController getMediaPlayerController(String id) {
        Entry entry = mAudioMessageMap.get(id);
        return entry != null ? entry.controller : null;
    }

    public MediaFile getMediaFile(String id) {
        Entry entry = mAudioMessageMap.get(id);
        return entry != null ? entry.controller.getMediaFile() : null;
    }

    public AudioBatchController getAudioBatchController(String conversationId) {
        AudioBatchController batchController = mBatchControllerMap.get(conversationId);
        if (batchController == null) {
            batchController = new AudioBatchController();
            mBatchControllerMap.put(conversationId, batchController);
        }
        return batchController;
    }

    /**
     * 某一个会话中， 开始从第一个EMMessage顺序播放语音文件
     */
    public void startAudioBatchController(String conversationId) {
        startAudioBatchController(conversationId, null);
    }

    /**
     * 添加一条EMMessage到会话的音频顺序批量播放控制器
     */
    public void addMessageToAudioBatchController(final String conversationId, final EMMessage message) {
        if (message == null || message.direct != EMMessage.Direct.RECEIVE || message.getType() != EMMessage.Type.VOICE) {
            return;
        }
        ExecUtil.execute(ExecUtil.MODE_COMPUTE,new Runnable() {
            @Override
            public void run() {
                EMConversation conversation = EMChatManager.getInstance().getConversation(conversationId);
                if (conversation != null) {
                    // ????防止阻塞
                    final List<EMMessage> messageList = conversation.getAllMessages();
                    EMMessage[] messages = messageList.toArray(new EMMessage[messageList.size()]);
                    AudioBatchController batchController = getAudioBatchController(conversationId);
                    for (EMMessage other : messages) {
                        if (other == message || TextUtils.equals(other.getMsgId(), message.getMsgId())) {
                            batchController.start(getAndAddMediaPlayerController(message));
                            return;
                        }
                    }
                }
            }
        });
    }

    /**
     * 点击某一个会话中EMMessage后，开始从这个EMMessage顺序播放语音文件
     */
    public void startAudioBatchController(final String conversationId, final String startMsgId) {
        ExecUtil.execute(ExecUtil.MODE_COMPUTE, new Runnable() {
            @Override
            public void run() {
                EMConversation conversation = EMChatManager.getInstance().getConversation(conversationId);
                if (conversation != null) {
                    // ????防止阻塞
                    final List<EMMessage> messageList = conversation.getAllMessages();
                    EMMessage[] messages = messageList.toArray(new EMMessage[messageList.size()]);
                    if (messages != null) {
                        boolean canAdd = false;
                        AudioBatchController batchController = getAudioBatchController(conversationId);
                        synchronized (mLock) {
                            if (!mCurConversationId.equals(conversationId)) {
                                return;
                            }
                            for (EMMessage message : messages) {
                                if (message != null && message.getType() == EMMessage.Type.VOICE &&
                                        message.direct == EMMessage.Direct.RECEIVE) {
                                    VoiceMessageBody voiceBody = (VoiceMessageBody) message.getBody();
                                    File file = new File(voiceBody.getLocalUrl());
                                    if (canAdd || TextUtils.isEmpty(startMsgId) || startMsgId.equals(message.getMsgId())) {
                                        canAdd = true;
                                        if (message.status == EMMessage.Status.SUCCESS && file.exists() && file.isFile()) {
                                            MediaPlayerController controller = getAndAddMediaPlayerController(message);
                                            batchController.start(controller);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * 暂停某一会话的音频顺序批量播放控制器
     */
    public void stopAudioBatchController(String conversationId) {
        AudioBatchController batchController = mBatchControllerMap.get(conversationId);
        if (batchController != null) {
            batchController.stop();
        }
    }

    static class EMMessageComparator implements Comparator<EMMessage> {

        @Override
        public int compare(EMMessage lhs, EMMessage rhs) {
            if (lhs.getMsgTime() > rhs.getMsgTime()) {
                return 1;
            } else if (lhs.getMsgTime() == rhs.getMsgTime()) {
                return 0;
            } else {
                return -1;
            }
        }
    }

    static class Entry {
        final EMMessage message;
        final MediaPlayerController controller;

        Entry(EMMessage message, MediaPlayerController controller) {
            this.message = message;
            this.controller = controller;
        }
    }

}

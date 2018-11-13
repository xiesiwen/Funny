package com.qingqing.base.nim.ui.lecture;

import android.content.Context;

import com.easemob.chat.EMMessage;
import com.easemob.chat.VoiceMessageBody;
import com.qingqing.api.proto.v1.Play;
import com.qingqing.api.proto.v1.im.ImProto;
import com.qingqing.base.cache.DiskCacheManager;
import com.qingqing.base.download.DownloadListener;
import com.qingqing.base.download.DownloadManager;
import com.qingqing.base.download.DownloadRequest;
import com.qingqing.base.im.domain.ChatMessage;
import com.qingqing.base.log.Logger;
import com.qingqing.base.media.AudioPlayerController;
import com.qingqing.base.media.MediaControl;
import com.qingqing.base.media.MediaFileFactory;
import com.qingqing.base.media.MediaPlayerController;
import com.qingqing.base.media.MediaUtils;

import java.io.File;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Created by wangxiaxin on 2016/6/1.
 *
 * 讲堂历史的 音频管理类
 */
public class LectureHistoryAudioBatchProcessor {
    
    private static final String TAG = "LectureHistoryAudioBatchProcesser";
    private Context mCtx;
    private boolean mIsAutoPlay;// 当前是否是自动播放状态
    private static DownloadManager sDLMgr = DownloadManager.INSTANCE();
    private ProcessCallback mCallback;
    private HashMap<String, InternalDownloadAudioInfo> mAudioDownloadInfoMap = new HashMap<>();
    private Queue<InternalPlayAudioInfo> mAutoPlayQueue = new PriorityQueue<>(8,
            new Comparator<InternalPlayAudioInfo>() {
                @Override
                public int compare(InternalPlayAudioInfo lhs, InternalPlayAudioInfo rhs) {
                    if(lhs.message.getMsgTime() > rhs.message.getMsgTime())
                        return 1;
                    else if(lhs.message.getMsgTime() == rhs.message.getMsgTime())
                        return 0;
                    else
                        return -1;
                }
            });
    
    private EMMessage mAutoPlayStartMessage;
    private InternalPlayAudioInfo mAutoPlayCurrentInfo;
    private boolean mIsOnlyExpert;// 是否打开专家模式
    private ChatMessage mToolChatMessage;// 用于解析数据
    
    public interface ProcessCallback {
        /**
         * 当 开始播放 一条语音时，回调触发
         * */
        void onPlayStart(EMMessage msg, int index);
        
        /**
         * 当 播放完成 一条语音时，回调触发
         * */
        void onPlayDone(EMMessage msg, int index,boolean isDone);
        
        /**
         * 当 播放异常 一条语音时，回调触发
         * */
        void onPlayError(EMMessage msg, int index, Throwable error);
        
        /**
         * 当 播放队列中，所有的语音播放完成后，回调触发
         * */
        void onPlayAllDone();
    }
    
    public interface DownloadCallback {
        void onDone(EMMessage msg);
        
        void onFailed(EMMessage msg);
    }
    
    enum DownloadStatus {
        NONE, OK, ING, FAIL
    }
    
    private class InternalDownloadAudioInfo {
        String mediaId;
        File localFile;
        EMMessage message;
        int index;
        Play.AudioPlayInfo audioSummary;
        DownloadCallback downloadCallback;
        DownloadStatus downloadStatus;
        
        InternalDownloadAudioInfo(String mediaId, EMMessage msg,
                Play.AudioPlayInfo audioInfo) {
            this.mediaId = mediaId;
            this.message = msg;
            this.audioSummary = audioInfo;
        }
    }
    
    public LectureHistoryAudioBatchProcessor(Context ctx, ProcessCallback callback) {
        mCtx = ctx;
        mCallback = callback;
    }
    
    public void setOnlyExpert(boolean onlyExpert) {
        if (mIsOnlyExpert != onlyExpert) {
            mIsOnlyExpert = onlyExpert;
            stopPlay();
        }
    }
    
    private boolean isExpertMessage(EMMessage msg) {
        // 如果是专家模式，需要对消息的role进行过滤
        syncChatMessage(msg);
        boolean isExpert = false;
        for (int i = 0; i < mToolChatMessage.getChatRoomType().size(); i++) {
            if (mToolChatMessage.getChatRoomType()
                    .get(i) == ImProto.ChatRoomRoleType.mc_chat_room_role_type) {
                isExpert = true;
            }
        }
        return isExpert;
    }
    
    class AudioDownloadListener implements DownloadListener {
        
        @Override
        public void onUpdateProcess(DownloadRequest task) {}
        
        @Override
        public void onFinishDownload(DownloadRequest task) {
            syncAudioInfoDownloadInfo(task, DownloadStatus.OK);
        }
        
        @Override
        public void onPreDownload(DownloadRequest task) {
            syncAudioInfoDownloadInfo(task, DownloadStatus.ING);
        }
        
        @Override
        public void onErrorDownload(DownloadRequest task, int error, Throwable exception) {
            syncAudioInfoDownloadInfo(task, DownloadStatus.FAIL);
        }
        
        private void syncAudioInfoDownloadInfo(DownloadRequest task, DownloadStatus status) {
            
            InternalDownloadAudioInfo info = mAudioDownloadInfoMap.get(task.getUrl());
            if (info != null) {
                info.downloadStatus = status;
                if (status == DownloadStatus.OK) {
                    info.localFile = task.getDownloadFile();
                    if (info.downloadCallback != null) {
                        info.downloadCallback.onDone(info.message);
                        info.downloadCallback = null;
                    }
                    
                    // 确认当前的播放队列中是否已经存在该数据
                    if (mIsAutoPlay) {
                        boolean needAdd = true;
                        if (!mAutoPlayQueue.isEmpty()) {
                            for (InternalPlayAudioInfo playInfo : mAutoPlayQueue) {
                                if (playInfo.message == info.message) {
                                    needAdd = false;
                                    break;
                                }
                            }
                        }
                        
                        if (needAdd)
                            addDownloadedTaskToPlayQueue(info);
                    }
                    
                }
                else if (status == DownloadStatus.FAIL) {
                    if (info.downloadCallback != null) {
                        info.downloadCallback.onFailed(info.message);
                        info.downloadCallback = null;
                    }
                }
            }
        }
    }
    
    /**
     * 添加一个audio下载任务
     * */
    public void addDownload(final EMMessage msg) {
        addDownload(msg, null);
    }
    
    /**
     * 添加一个audio下载任务
     * */
    public void addDownload(final EMMessage msg,
            final DownloadCallback callback) {
        if (msg != null && msg.getType() == EMMessage.Type.VOICE) {
            final VoiceMessageBody body = (VoiceMessageBody) msg.getBody();
            
            InternalDownloadAudioInfo existAudioInfo = null;
            for (Map.Entry<String, InternalDownloadAudioInfo> entry : mAudioDownloadInfoMap
                    .entrySet()) {
                if (entry.getValue().mediaId.equals(body.getRemoteUrl())) {
                    existAudioInfo = entry.getValue();
                    break;
                }
            }
            
            if (existAudioInfo == null) {
                MediaUtils.getAudio(body.getRemoteUrl(),
                        new MediaUtils.AudioReqListener() {
                            @Override
                            public void onSuccess(Play.AudioPlayInfoResponse response) {
                                InternalDownloadAudioInfo info = new InternalDownloadAudioInfo(
                                        body.getRemoteUrl(), msg, response.playInfo);
                                info.downloadCallback = callback;
                                final String url = info.audioSummary
                                        .fixedDownloadUrl;
                                mAudioDownloadInfoMap.put(url, info);
                                sDLMgr.startTask(info.audioSummary.fixedDownloadUrl, DiskCacheManager.instance().createAudio(info.audioSummary.fixedDownloadUrl),
                                        new AudioDownloadListener());
                            }
                            
                            @Override
                            public void onFailure() {
                                if (callback != null) {
                                    callback.onFailed(msg);
                                }
                            }
                        });
            }
            else {
                
                if (existAudioInfo.downloadStatus == DownloadStatus.OK) {
                    if (callback != null) {
                        callback.onDone(msg);
                    }
                }
                else {
                    existAudioInfo.downloadCallback = callback;
                    sDLMgr.startTask(existAudioInfo.audioSummary.fixedDownloadUrl,
                            DiskCacheManager.instance().createAudio(
                                    existAudioInfo.audioSummary.fixedDownloadUrl),
                            new AudioDownloadListener());
                }
            }
        }
        else {
            if (callback != null) {
                callback.onFailed(msg);
            }
        }
    }
    
    /**
     * 停止并删除 未开始的audio下载任务
     * */
    public void cancelWaitingDownload() {
        Iterator<Map.Entry<String, InternalDownloadAudioInfo>> iterator = mAudioDownloadInfoMap
                .entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, InternalDownloadAudioInfo> entry = iterator.next();
            String key = entry.getKey();
            if (sDLMgr.isTaskExist(key)
                    && entry.getValue().downloadStatus == DownloadStatus.NONE) {
                sDLMgr.stopTask(key);
                iterator.remove();
            }
        }
    }
    
    private void syncChatMessage(EMMessage message) {
        if (mToolChatMessage == null) {
            mToolChatMessage = new ChatMessage(message);
        }
        else {
            mToolChatMessage.update(message);
        }
    }
    
    public InternalDownloadAudioInfo getInternalDownloadAudioInfo(EMMessage msg) {
        
        if (msg == null)
            return null;
        
        for (Map.Entry<String, InternalDownloadAudioInfo> entry : mAudioDownloadInfoMap
                .entrySet()) {
            InternalDownloadAudioInfo audioInfo = entry.getValue();
            if (((VoiceMessageBody)audioInfo.message.getBody()).getRemoteUrl().equals(((VoiceMessageBody)msg.getBody()).getRemoteUrl())){
                return audioInfo;
            }
        }
        
        return null;
    }
    
    private class InternalPlayAudioInfo implements Comparator<InternalPlayAudioInfo> {
        EMMessage message;
        MediaPlayerController controller;
        int index;
        boolean isPlayDone;
        
        public void reset(boolean needStop) {
            message = null;
            if (controller != null) {
                if (needStop && controller.isPlaying()) {
                    controller.stop();
                }
                controller.removeMediaControl(mMediaControl);
                controller = null;
            }
        }
        
        @Override
        public int compare(InternalPlayAudioInfo lhs, InternalPlayAudioInfo rhs) {
            if (lhs.message.getMsgTime() - rhs.message.getMsgTime() > 0)
                return 1;
            else if (lhs.message.getMsgTime() - rhs.message.getMsgTime() == 0)
                return 0;
            else
                return -1;
        }
    }
    
    /**
     * 在AutoPlay的情况下， 将下载完成的任务 加入到 播放队列
     * */
    private void addDownloadedTaskToPlayQueue(InternalDownloadAudioInfo downloadInfo) {
        if (!mIsAutoPlay)
            return;
        
        if (downloadInfo == null || downloadInfo.downloadStatus != DownloadStatus.OK)
            return;
        
        // 播放队列中已存在的忽略
        boolean exist = false;
        for (InternalPlayAudioInfo playInfo : mAutoPlayQueue) {
            if (downloadInfo.index == playInfo.index) {
                exist = true;
                break;
            }
        }
        if (exist) {
            return;
        }
        
        if (mIsOnlyExpert && !isExpertMessage(downloadInfo.message)) {
            return;
        }
        
        // 如果要添加的info message时间比指定的message时间早，则忽略
        if (mAutoPlayCurrentInfo == null
                || downloadInfo.message.getMsgTime() >= mAutoPlayCurrentInfo.message
                        .getMsgTime()) {
            offerPlayInfo(downloadInfo);
        }
    }
    
    private MediaControl mMediaControl = new MediaControl() {
        @Override
        public void onError(Throwable err) {
            if (mCallback != null) {
                mCallback.onPlayError(mAutoPlayCurrentInfo.message,
                        mAutoPlayCurrentInfo.index, err);
            }
            // tryPlayNext();
        }
        
        @Override
        public void onStarted() {
            if (mCallback != null) {
                mCallback.onPlayStart(mAutoPlayCurrentInfo.message,
                        mAutoPlayCurrentInfo.index);
            }
        }
        
        @Override
        public void onPrepared() {}
        
        @Override
        public void onStoped() {
            if (mCallback != null) {
                mCallback.onPlayDone(mAutoPlayCurrentInfo.message,
                        mAutoPlayCurrentInfo.index,mAutoPlayCurrentInfo.isPlayDone);
            }
        }
        
        @Override
        public void onCompleted() {
            if (mCallback != null) {
                mAutoPlayCurrentInfo.isPlayDone = true;
                mCallback.onPlayDone(mAutoPlayCurrentInfo.message,
                        mAutoPlayCurrentInfo.index,mAutoPlayCurrentInfo.isPlayDone);
            }
            // tryPlayNext();
        }
    };
    
    private void finishPlay() {
        mIsAutoPlay = false;
        if (mAutoPlayCurrentInfo != null) {
            mAutoPlayCurrentInfo.reset(false);
            mAutoPlayCurrentInfo = null;
        }
        mAutoPlayStartMessage = null;
        if (mCallback != null) {
            mCallback.onPlayAllDone();
        }
    }
    
    private void tryPlayNext() {
        if (mIsAutoPlay) {
            InternalPlayAudioInfo info;
            while (true) {
                info = mAutoPlayQueue.poll();
                if (info == null) {
                    finishPlay();
                    break;
                }
                else {
                    if (mIsOnlyExpert && !isExpertMessage(info.message)) {
                        continue;
                    }
                    
                    if (mAutoPlayCurrentInfo == null
                            || info.index > mAutoPlayCurrentInfo.index) {
                        mAutoPlayCurrentInfo = info;
                        info.controller.start();
                        break;
                    }
                }
            }
        }
        else {
            finishPlay();
        }
    }
    
    /**
     * 停止播放，当前正在播放的声音，会继续播完
     * */
    public void stopPlay() {
        if (!mIsAutoPlay) {
            Logger.w(TAG, "当前不是自动播放状态");
            return;
        }
        
        // 如果当前正在播放，则继续播放完
        mIsAutoPlay = false;
        mAutoPlayStartMessage = null;
        // 播放队列中的内容清除
        mAutoPlayQueue.clear();
    }
    
    /**
     * 清除播放信息
     * */
    public void clearPlay() {
        // 当前没在播放直接清除
        if (mAutoPlayCurrentInfo != null) {
            mAutoPlayCurrentInfo.reset(false);
            mAutoPlayCurrentInfo = null;
        }
    }
    
    /** 强行停止播放 */
    public void forceResetPlay() {
        stopPlay();
        if (mAutoPlayCurrentInfo != null) {
            mAutoPlayCurrentInfo.reset(true);
            mAutoPlayCurrentInfo = null;
        }
    }
    
    /**
     * 开始从 msg 自动播放，当播放队列完成后，会调用 {@link ProcessCallback#onPlayAllDone()}<br/>
     *
     * 播放过程中，开始播放msg时，会调用 {@link ProcessCallback#onPlayStart(EMMessage, int)}<br/>
     *
     * 播放的结果，以下两个会调一个<br/>
     * {@link ProcessCallback#onPlayDone(EMMessage, int, boolean)}<br/>
     * {@link ProcessCallback#onPlayError(EMMessage, int, Throwable)}
     * */
    public void startPlay(final EMMessage msg, int index) {
        
        // 如果当前正在播放，则先停止播放
        if (mIsAutoPlay && mAutoPlayCurrentInfo != null) {
            mAutoPlayCurrentInfo.reset(true);
            mAutoPlayCurrentInfo = null;
            mIsAutoPlay = false;
        }
        
        if (msg != null) {
            
            mIsAutoPlay = true;
            mAutoPlayStartMessage = msg;
            
            // 查看当前的queue中是否有过期的的item
            while (true) {
                if (mAutoPlayQueue.isEmpty())
                    break;
                
                InternalPlayAudioInfo info = mAutoPlayQueue.peek();
                if (info.message.getMsgTime() < mAutoPlayStartMessage.getMsgTime()) {
                    mAutoPlayQueue.poll();
                }
                else {
                    break;
                }
            }
            
            // 查看 已下载的task是否有可以添加进来的
            for (Map.Entry<String, InternalDownloadAudioInfo> entry : mAudioDownloadInfoMap
                    .entrySet()) {
                InternalDownloadAudioInfo audioInfo = entry.getValue();
                addDownloadedTaskToPlayQueue(audioInfo);
            }
            
            InternalDownloadAudioInfo audioInfo = getInternalDownloadAudioInfo(msg);
            if (audioInfo == null || audioInfo.downloadStatus != DownloadStatus.OK) {
                addDownload(msg);
            }
            else {
                offerPlayInfo(audioInfo);
            }
        }
        
        tryPlayNext();
    }
    
    private void offerPlayInfo(InternalDownloadAudioInfo downloadInfo) {
        MediaPlayerController controller = MediaPlayerController.audio(mCtx,
                MediaFileFactory.audio(downloadInfo.mediaId,
                        downloadInfo.audioSummary.fixedDownloadUrl,
                        downloadInfo.audioSummary.timeLength,
                        downloadInfo.localFile.length(),
                        downloadInfo.localFile.getAbsolutePath()));
        controller.addMediaControl(mMediaControl);
        InternalPlayAudioInfo info = new InternalPlayAudioInfo();
        info.message = downloadInfo.message;
        info.index = downloadInfo.index;
        info.controller = controller;
        mAutoPlayQueue.offer(info);
    }
    
    /**
     * 判断 msg 是否是 当前处于播放操作的msg（包括暂停状态）
     * 
     * @param msg
     *            聊天消息
     * */
    public boolean isCurrent(EMMessage msg) {
        return mAutoPlayCurrentInfo != null && mAutoPlayCurrentInfo.message == msg;
    }
    
    public int getCurrentIdx() {
        if (mAutoPlayCurrentInfo != null) {
            return mAutoPlayCurrentInfo.index;
        }
        else {
            return -1;
        }
    }
    
    /**
     * 当前是否处于播放状态
     * */
    public boolean isPlaying() {
        return mAutoPlayCurrentInfo != null && mAutoPlayCurrentInfo.controller != null
                && mAutoPlayCurrentInfo.controller.isPlaying();
    }
    
    /**
     * 点击当前播放的messag，触发的播放状态切换
     * */
    public void togglePlay() {
        if (mAutoPlayCurrentInfo != null) {
            mAutoPlayCurrentInfo.controller.toggleMediaPlayState();
        }
    }
    
    /**
     * 获取当前正在播放的 控制器
     * */
    public MediaPlayerController getCurrentPlayController() {
        if (mAutoPlayCurrentInfo != null) {
            return mAutoPlayCurrentInfo.controller;
        }
        else {
            return null;
        }
    }
    
    public void stopPlayer() {
        AudioPlayerController.pauseCurrentPlayer();
    }
    
    public void destroyPlayer() {
        MediaPlayerController player = getCurrentPlayController();
        if (player != null) {
            player.destroy();
        }
        AudioPlayerController.stopCurrentPlayer();
    }
    
    /*** ----------------------单个播放的实现---------------------------------- */
    
    /***
     * 准备播放信息
     *
     * @param msg
     *            {@link EMMessage}
     * @return boolean true 准备成功
     */
    private boolean preparePlayInfo(EMMessage msg, int index) {
        InternalDownloadAudioInfo downloadInfo = getInternalDownloadAudioInfo(msg);
        if (downloadInfo == null || downloadInfo.audioSummary == null || downloadInfo.localFile == null)
            return false;
        
        MediaPlayerController controller = MediaPlayerController.audio(mCtx,
                MediaFileFactory.audio(downloadInfo.mediaId,
                        downloadInfo.audioSummary.fixedDownloadUrl,
                        downloadInfo.audioSummary.timeLength,
                        downloadInfo.localFile.length(),
                        downloadInfo.localFile.getAbsolutePath()));
        controller.addMediaControl(mMediaControl);
        InternalPlayAudioInfo info = new InternalPlayAudioInfo();
        info.message = downloadInfo.message;
        info.index = index;
        info.controller = controller;
        mAutoPlayCurrentInfo = info;
        return true;
    }
    
    public boolean playSingle(EMMessage msg, int index) {
        
        if (mAutoPlayCurrentInfo != null) {
            stopSingle(true);
        }
        
        Logger.d(TAG, "playSingle----" + index);
        if (!preparePlayInfo(msg, index))
            return false;
        
        mAutoPlayCurrentInfo.controller.start();
        return true;
    }
    
    public boolean stopSingle(boolean immediatelyStop) {
        
        if (mAutoPlayCurrentInfo == null)
            return false;

        mAutoPlayCurrentInfo.reset(immediatelyStop);
        mAutoPlayCurrentInfo = null;
        return true;
    }
    
    public void forceStop() {
        destroyPlayer();
        mAutoPlayCurrentInfo = null;
    }
    
}

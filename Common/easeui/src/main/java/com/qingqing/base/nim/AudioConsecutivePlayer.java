package com.qingqing.base.nim;

import android.content.Context;

import com.qingqing.base.interfaces.AbstractDestroyable;
import com.qingqing.base.log.Logger;
import com.qingqing.base.media.AudioPlayerController;
import com.qingqing.base.media.MediaControl;
import com.qingqing.base.media.MediaFile;
import com.qingqing.base.media.MediaFileFactory;
import com.qingqing.base.media.MediaPlayerController;
import com.qingqing.base.nim.domain.AudioMessageBody;
import com.qingqing.base.nim.domain.Message;
import com.qingqing.base.nim.domain.MessageRepository;
import com.qingqing.base.nim.domain.spec.AudioSpec;
import com.qingqing.base.nim.domain.spec.BodyStatusSuccessSpec;
import com.qingqing.base.nim.domain.spec.IndexAboveSpec;
import com.qingqing.base.nim.domain.spec.ReceivedSpec;
import com.qingqing.base.nim.domain.spec.StatusSuccessSpec;
import com.qingqing.base.nim.utils.MessageUtils;
import com.qingqing.base.spec.CompositeSpec;
import com.qingqing.base.spec.Spec;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by huangming on 2016/8/26.
 *
 * 音频连续播放
 */
public class AudioConsecutivePlayer extends AbstractDestroyable
        implements PlayStatusObservable {
    
    private static final String TAG = "AudioConsecutivePlayer";
    
    private static final Spec<Message> SPEC_AUDIO = new AudioSpec();
    private static final Spec<Message> SPEC_BODY_SUCCESS = new BodyStatusSuccessSpec();
    private static final Spec<Message> SPEC_MSG_SUCCESS = new StatusSuccessSpec();
    private static final Spec<Message> SPEC_RECEIVED = new ReceivedSpec();
    
    private Context context;
    
    private MessageRepository repository;
    
    private Message playingMsg;
    
    private Map<Message, MediaPlayerController> controllers = new HashMap<>();
    
    // 是否连续
    private boolean continuous;
    
    private boolean continuable;
    
    private int startIndex;
    
    private Set<PlayStatusObserver> observers = new HashSet<>();
    
    public AudioConsecutivePlayer(Context context) {
        this.context = context;
    }
    
    public void setRepository(MessageRepository repository) {
        this.repository = repository;
    }
    
    private Context getContext() {
        return context;
    }
    
    private MessageRepository getRepository() {
        return repository;
    }
    
    private Set<PlayStatusObserver> getObservers() {
        return observers;
    }
    
    private void setContinuous(boolean continuous) {
        this.continuous = continuous;
    }
    
    private boolean isContinuous() {
        return continuous;
    }
    
    public void setContinuable(boolean continuable) {
        this.continuable = continuable;
    }
    
    private boolean whetherToContinue() {
        return !isDestroyed() && isContinuable() && isContinuous();
    }
    
    public boolean isContinuable() {
        return continuable;
    }
    
    private Map<Message, MediaPlayerController> getControllers() {
        return controllers;
    }
    
    public void toggleBy(Message message) {
        if (message == getPlayingMsg()) {
            stopPlay();
        }
        else {
            stopPlay();
            setContinuous(!MessageUtils.isSendDirect(message));
            Logger.e(TAG, "toggleBy isContinuous = " + isContinuous());
            playAudio(message);
        }
    }
    
    public void stopPlay() {
        setContinuous(false);
        stopCurrentAudioPlayer();
    }
    
    private void playAudio(final Message message) {
        MediaPlayerController controller = controllers.get(message);
        if (controller == null) {
            AudioMessageBody body = (AudioMessageBody) message.getBody();
            MediaFile mediaFile = MediaFileFactory.audio(body.getMediaId(),
                    body.getLocalUrl(), body.getLength(), 0, body.getLocalUrl());
            controller = MediaPlayerController.audio(getContext(), mediaFile);
            MediaControl mediaControl = new MediaControl() {
                @Override
                public void onError(Throwable err) {}
                
                @Override
                public void onStarted() {
                    Logger.i(TAG, "onStarted");
                    playStared(message);
                }
                
                @Override
                public void onPrepared() {}
                
                @Override
                public void onStoped() {
                    Logger.i(TAG, "onStoped");
                    playCompleted();
                }
                
                @Override
                public void onCompleted() {
                    Logger.i(TAG, "onCompleted");
                    playCompleted();
                }
            };
            
            controller.addMediaControl(mediaControl);
            controllers.put(message, controller);
        }
        setStartIndex(message.getIndex());
        controller.start();
    }
    
    public void playIfNeeded() {
        if (getPlayingMsg() == null && whetherToContinue()) {
            findAudioToContinue(getStartIndex());
        }
    }
    
    private void playStared(Message message) {
        Logger.e(TAG, "playStared");
        setPlayingMsg(message);
        message.setListened(true);
        for (PlayStatusObserver observer : getObservers()) {
            observer.onPlayStatusChanged();
        }
    }
    
    private void playCompleted() {
        Logger.e(TAG, "playCompleted");
        final Message message = getPlayingMsg();
        setPlayingMsg(null);
        for (PlayStatusObserver observer : getObservers()) {
            observer.onPlayStatusChanged();
        }
        if (message != null) {
            setStartIndex(message.getIndex());
            if(whetherToContinue()) {
                findAudioToContinue(getStartIndex());
            }
        }
    }
    
    private int getStartIndex() {
        return startIndex;
    }
    
    private void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }
    
    private void findAudioToContinue(int startIndex) {
        if (getRepository() != null) {
            CompositeSpec<Message> compositeSpec = new CompositeSpec<>();
            compositeSpec.addSpec(SPEC_AUDIO);
            compositeSpec.addSpec(SPEC_BODY_SUCCESS);
            compositeSpec.addSpec(SPEC_MSG_SUCCESS);
            compositeSpec.addSpec(SPEC_RECEIVED);
            compositeSpec.addSpec(new IndexAboveSpec(startIndex));
            Message message = getRepository().getMessageBy(compositeSpec);
            if (message != null) {
                Logger.i(TAG, "findAudioToContinue next");
                playAudio(message);
            }
        }
        
    }
    
    public Message getPlayingMsg() {
        return playingMsg;
    }
    
    private void setPlayingMsg(Message playingMsg) {
        this.playingMsg = playingMsg;
    }
    
    private void stopCurrentAudioPlayer() {
        AudioPlayerController.stopCurrentPlayer();
    }
    
    @Override
    protected void onDestroy() {
        Logger.i(TAG, "onDestroy");
        stopPlay();
        getObservers().clear();
        for (Map.Entry<Message, MediaPlayerController> entry : getControllers()
                .entrySet()) {
            entry.getValue().destroy();
        }
        getControllers().clear();
    }
    
    @Override
    public boolean isPlaying(Message message) {
        return getPlayingMsg() == message;
    }
    
    @Override
    public void registerObserver(PlayStatusObserver observer) {
        getObservers().add(observer);
    }
    
    @Override
    public void unregisterObserver(PlayStatusObserver observer) {
        getObservers().remove(observer);
    }
}

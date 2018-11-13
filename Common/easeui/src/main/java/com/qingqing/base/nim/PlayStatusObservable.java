package com.qingqing.base.nim;

import com.qingqing.base.nim.domain.Message;

/**
 * Created by huangming on 2016/8/26.
 */
public interface PlayStatusObservable {
    
    boolean isPlaying(Message message);
    
    void registerObserver(PlayStatusObserver observer);
    
    void unregisterObserver(PlayStatusObserver observer);
    
}

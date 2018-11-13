package com.qingqing.base.nim.domain;

/**
 * Created by huangming on 2016/8/31.
 */
public interface ChatConfigProvider {
    
    boolean isSpeakerOpened();
    
    boolean isVibrateAllowed(Message message);
    
    boolean isSoundAllowed(Message message);
    
}

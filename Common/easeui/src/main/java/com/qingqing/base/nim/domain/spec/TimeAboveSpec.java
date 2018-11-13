package com.qingqing.base.nim.domain.spec;

import com.qingqing.base.nim.domain.Message;

/**
 * Created by huangming on 2016/8/26.
 */
public class TimeAboveSpec implements MessageSpec {
    
    private long baseTime;
    
    public TimeAboveSpec(long time) {
        this.baseTime = time;
    }
    
    private long getBaseTime() {
        return baseTime;
    }
    
    @Override
    public boolean isSatisfiedBy(Message product) {
        return getBaseTime() < product.getMsgTime();
    }
}

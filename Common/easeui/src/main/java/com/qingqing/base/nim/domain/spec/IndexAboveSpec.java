package com.qingqing.base.nim.domain.spec;

import com.qingqing.base.nim.domain.Message;
import com.qingqing.base.spec.Spec;

/**
 * Created by huangming on 2016/8/30.
 */
public class IndexAboveSpec implements Spec<Message> {
    
    private final int baseIndex;
    
    public IndexAboveSpec(int baseIndex) {
        this.baseIndex = baseIndex;
    }
    
    public int getBaseIndex() {
        return baseIndex;
    }
    
    @Override
    public boolean isSatisfiedBy(Message product) {
        return product.getIndex() > getBaseIndex();
    }
}

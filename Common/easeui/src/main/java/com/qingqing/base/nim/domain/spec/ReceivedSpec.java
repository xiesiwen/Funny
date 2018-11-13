package com.qingqing.base.nim.domain.spec;

import com.qingqing.base.nim.domain.Message;
import com.qingqing.base.nim.utils.MessageUtils;

/**
 * Created by huangming on 2016/8/26.
 */
public class ReceivedSpec implements MessageSpec {
    @Override
    public boolean isSatisfiedBy(Message product) {
        return !MessageUtils.isSendDirect(product);
    }
}

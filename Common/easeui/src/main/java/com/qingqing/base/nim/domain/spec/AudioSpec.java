package com.qingqing.base.nim.domain.spec;

import com.qingqing.base.nim.domain.Message;
import com.qingqing.base.nim.utils.MessageUtils;

/**
 * Created by huangming on 2016/8/26.
 */
public class AudioSpec implements MessageSpec {
    @Override
    public boolean isSatisfiedBy(Message product) {
        return MessageUtils.isAudioMessage(product);
    }
}

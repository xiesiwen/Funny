package com.qingqing.base.nim.domain.spec;

import com.qingqing.base.nim.domain.Message;

/**
 * Created by huangming on 2016/8/26.
 */
public class StatusSuccessSpec implements MessageSpec {
    @Override
    public boolean isSatisfiedBy(Message product) {
        return product.getStatus() == Message.Status.SUCCESS;
    }
}

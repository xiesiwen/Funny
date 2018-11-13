package com.qingqing.base.nim.domain.spec;

import com.qingqing.base.nim.domain.FileMessageBody;
import com.qingqing.base.nim.domain.Message;

/**
 * Created by huangming on 2016/8/26.
 */
public class BodyStatusSuccessSpec implements MessageSpec {

    @Override
    public boolean isSatisfiedBy(Message product) {
        if(product.getBody() instanceof FileMessageBody) {
            return ((FileMessageBody)product.getBody()).getStatus() == Message.Status.SUCCESS;
        }
        return false;
    }
}

package com.qingqing.base.nim.view;

import com.qingqing.base.nim.domain.Message;

/**
 * Created by huangming on 2016/8/18.
 */
public interface ChatRowClickListener {

    void onBubbleClick(Message message);

    void onBubbleLongClick(Message message);

    void onAvatarClick(Message message);

    void onResendClick(Message message);

}

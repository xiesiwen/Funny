package com.qingqing.base.nim.ui;

import com.qingqing.base.im.utils.IMUtils;
import com.qingqing.base.nim.SingleChatCoordinationListener;
import com.qingqing.base.nim.SingleChatCoordinator;

/**
 * Created by huangming on 2016/8/17.
 */
public class BaseSingleChatFragment extends BaseChatFragment
        implements SingleChatCoordinationListener {
    @Override
    protected SingleChatCoordinator createCoordinator() {
        return new SingleChatCoordinator(getConversationId());
    }
    
    @Override
    protected SingleChatCoordinator getCoordinator() {
        return (SingleChatCoordinator) super.getCoordinator();
    }
    
    @Override
    protected SingleChatCoordinationListener createChatCoordinationListener() {
        return this;
    }
    
    @Override
    public void onInitialized() {
        super.onInitialized();
    }
    
    @Override
    public void onInitializeFailed() {}
    
    @Override
    protected String getTitle() {
        return IMUtils.getName(getConversationId());
    }
}

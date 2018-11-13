package com.qingqing.base.nim;

/**
 * Created by huangming on 2016/8/18.
 */
public class SingleChatCoordinator extends AbstractChatCoordinator {
    
    public SingleChatCoordinator(String conversationId) {
        super(conversationId);
    }
    
    @Override
    public SingleChatCoordinationListener getCoordinationListener() {
        return (SingleChatCoordinationListener) super.getCoordinationListener();
    }
    
    @Override
    protected void onInitialize() {
        setInitialized(true);
    }
    
    @Override
    protected void onDestroy() {}
    
}

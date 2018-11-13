package com.qingqing.base.interfaces;

/**
 * Created by huangming on 2016/8/24.
 */
public abstract class AbstractCoordinator implements Coordinator {
    
    private boolean destroyed;
    private boolean initialized;
    
    protected void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }
    
    protected void setDestroyed(boolean destroyed) {
        this.destroyed = destroyed;
    }
    
    @Override
    public boolean isDestroyed() {
        return destroyed;
    }
    
    @Override
    public boolean isInitialized() {
        return initialized;
    }
}

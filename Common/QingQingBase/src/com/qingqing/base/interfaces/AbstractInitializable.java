package com.qingqing.base.interfaces;

/**
 * Created by huangming on 2016/8/23.
 */
public abstract class AbstractInitializable implements Initializable {
    
    private boolean initialized;
    
    protected void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }
    
    @Override
    public void initialize() {
        onInitialize();
    }
    
    @Override
    public boolean isInitialized() {
        return initialized;
    }
    
    protected abstract void onInitialize();
}

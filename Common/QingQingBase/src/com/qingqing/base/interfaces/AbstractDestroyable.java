package com.qingqing.base.interfaces;

/**
 * Created by huangming on 2016/8/23.
 */
public abstract class AbstractDestroyable implements Destroyable {
    
    private boolean destroyed;
    
    @Override
    public boolean isDestroyed() {
        return destroyed;
    }
    
    protected void setDestroyed(boolean destroyed) {
        this.destroyed = destroyed;
    }
    
    @Override
    public void destroy() {
        setDestroyed(true);
        onDestroy();
    }
    
    protected abstract void onDestroy();
}

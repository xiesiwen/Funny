package com.qingqing.base.core;

import java.util.ArrayList;
import java.util.List;

public class ApiAction {
    
    public static final int CODE_OK = 0;
    public static final int CODE_ERROR = 1;
    
    protected List<ApiActionDelegate> mActionDelegates = new ArrayList<ApiActionDelegate>();
    
    public void addListener(ApiActionDelegate delegate) {
        mActionDelegates.add(delegate);
    }
    
    public void removeListener(ApiActionDelegate delegate) {
        mActionDelegates.remove(delegate);
    }
    
    
}

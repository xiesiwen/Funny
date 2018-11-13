package com.qingqing.base.nim.exception;

/**
 * Created by huangming on 2016/8/22.
 */
public class NetworkError extends ChatError {
    
    private int errorCode = -1;
    
    public NetworkError(String errorMessage) {
        super(errorMessage);
    }
    
    public NetworkError(int errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
    }
    
    public int getErrorCode() {
        return errorCode;
    }
    
    public String getErrorMessage() {
        return getMessage();
    }
    
}

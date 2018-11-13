package com.qingqing.base.nim.exception;

/**
 * Created by huangming on 2016/8/22.
 */
public class LectureLeaveError extends NetworkError {
    public LectureLeaveError(int errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }
}

package com.qingqing.base.nim.exception;

/**
 * Created by huangming on 2016/8/22.
 */
public class LectureJoinError extends NetworkError{
    public LectureJoinError(int errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }

    public LectureJoinError(String errorMessage) {
        super(-1, errorMessage);
    }
}

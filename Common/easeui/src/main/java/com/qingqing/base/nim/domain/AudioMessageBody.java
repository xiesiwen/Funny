package com.qingqing.base.nim.domain;

import java.io.File;

/**
 * Created by huangming on 2016/8/18.
 */
public class AudioMessageBody extends FileMessageBody {

    private int length;
    private String mediaId;

    AudioMessageBody(File file, int length) {
        super(file);
        this.length = length;
    }

    AudioMessageBody(String remoteUrl, int length, String mediaId) {
        super(remoteUrl);
        this.length = length;
        this.mediaId = mediaId;
    }

    void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public String getMediaId() {
        return mediaId;
    }

    public int getLength() {
        return length;
    }
}

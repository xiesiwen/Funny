package com.qingqing.base.nim.domain;

import java.io.File;

/**
 * Created by huangming on 2016/8/18.
 */
public class FileMessageBody extends MessageBody {
    
    private String fileName;
    private String localUrl;
    private String remoteUrl;
    private Message.Status status;

    FileMessageBody(File file) {
        this.fileName = file.getName();
        this.localUrl = file.getAbsolutePath();
    }

    FileMessageBody(String remoteUrl) {
        this.remoteUrl = remoteUrl;
    }
    
    public String getLocalUrl() {
        return localUrl;
    }
    
    void setLocalUrl(String localUrl) {
        this.localUrl = localUrl;
    }
    
    public String getRemoteUrl() {
        return remoteUrl;
    }
    
    void setRemoteUrl(String remoteUrl) {
        this.remoteUrl = remoteUrl;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Message.Status getStatus() {
        return status;
    }
    public void setStatus(Message.Status status) {
        this.status = status;
    }
}

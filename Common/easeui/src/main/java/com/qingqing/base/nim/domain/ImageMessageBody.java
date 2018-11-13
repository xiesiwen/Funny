package com.qingqing.base.nim.domain;

import java.io.File;

/**
 * Created by huangming on 2016/8/18.
 */
public class ImageMessageBody extends FileMessageBody {

    private String thumbnailUrl;
    private int width;
    private int height;

    ImageMessageBody(File file) {
        super(file);
    }

    ImageMessageBody(String remoteUrl, String thumbnailUrl, int width, int height) {
        super(remoteUrl);
        this.thumbnailUrl = thumbnailUrl;
        this.width = width;
        this.height = height;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public int getWidth() {
        return width;
    }

    void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}

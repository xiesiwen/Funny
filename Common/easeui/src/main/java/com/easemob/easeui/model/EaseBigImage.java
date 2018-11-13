package com.easemob.easeui.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * 环信点击大图查看并下载的数据结构
 * 
 * Created by lihui on 2017/7/26.
 */

public class EaseBigImage implements Parcelable {
    public String msgId;
    public Uri uri;
    public String secret;
    public String remotePath;
    public int defaultImageRes;
    
    public EaseBigImage(String msgId, Uri uri, String secret, String remotePath,
            int defaultImageRes) {
        this.msgId = msgId;
        this.uri = uri;
        this.secret = secret;
        this.remotePath = remotePath;
        this.defaultImageRes = defaultImageRes;
    }
    
    protected EaseBigImage(Parcel in) {
        msgId = in.readString();
        uri = in.readParcelable(Uri.class.getClassLoader());
        secret = in.readString();
        remotePath = in.readString();
        defaultImageRes = in.readInt();
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(msgId);
        dest.writeParcelable(uri, flags);
        dest.writeString(secret);
        dest.writeString(remotePath);
        dest.writeInt(defaultImageRes);
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    public static final Creator<EaseBigImage> CREATOR = new Creator<EaseBigImage>() {
        @Override
        public EaseBigImage createFromParcel(Parcel in) {
            return new EaseBigImage(in);
        }
        
        @Override
        public EaseBigImage[] newArray(int size) {
            return new EaseBigImage[size];
        }
    };
}

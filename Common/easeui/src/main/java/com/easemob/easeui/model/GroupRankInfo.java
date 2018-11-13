package com.easemob.easeui.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by lihui on 2017/7/24.
 */

public class GroupRankInfo implements Parcelable {
    public static final Creator<GroupRankInfo> CREATOR = new Creator<GroupRankInfo>() {
        @Override
        public GroupRankInfo createFromParcel(Parcel in) {
            return new GroupRankInfo(in);
        }
        
        @Override
        public GroupRankInfo[] newArray(int size) {
            return new GroupRankInfo[size];
        }
    };
    public String userType;
    public String value;
    public int rank;
    public int trend;
    public String headImage;
    public String nick;
    public String qqUserId;
    
    protected GroupRankInfo(Parcel in) {
        userType = in.readString();
        value = in.readString();
        rank = in.readInt();
        trend = in.readInt();
        headImage = in.readString();
        nick = in.readString();
        qqUserId = in.readString();
    }
    
    public GroupRankInfo(String userType, String value, int rank, int trend,
            String headImage, String nick, String qqUserId) {
        this.userType = userType;
        this.value = value;
        this.rank = rank;
        this.trend = trend;
        this.headImage = headImage;
        this.nick = nick;
        this.qqUserId = qqUserId;
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userType);
        dest.writeString(value);
        dest.writeInt(rank);
        dest.writeInt(trend);
        dest.writeString(headImage);
        dest.writeString(nick);
        dest.writeString(qqUserId);
    }
}

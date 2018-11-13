package com.qingqing.project.offline.groupchat;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.qingqing.api.proto.v1.UserProto;
import com.qingqing.api.proto.v1.im.TeachingResearchImProto;

/**
 * 用于在聊天设置页面个聊天成员列表页面中 UI RecyclerView的数据结构，仅此而已
 * 可能是userInfo为null，可能是userInfoAdmin为null，也有可能是两者都为null
 */


public class ChatBean implements Parcelable{
    @Nullable
    public UserProto.ChatUserInfo userInfo;
    @Nullable
    public TeachingResearchImProto.ChatUserInfoForTeachingResearchGroupAdmin userInfoAdmin;

    public boolean inDeleteList;

    public ChatBean(@Nullable UserProto.ChatUserInfo userInfo, @Nullable TeachingResearchImProto.ChatUserInfoForTeachingResearchGroupAdmin userInfoAdmin) {
        this.userInfo = userInfo;
        this.userInfoAdmin = userInfoAdmin;
    }

    protected ChatBean(Parcel in) {
        userInfo = in.readParcelable(UserProto.ChatUserInfo.class.getClassLoader());
        userInfoAdmin = in.readParcelable(TeachingResearchImProto.ChatUserInfoForTeachingResearchGroupAdmin.class.getClassLoader());
        inDeleteList = in.readByte() != 0;
    }

    public static final Creator<ChatBean> CREATOR = new Creator<ChatBean>() {
        @Override
        public ChatBean createFromParcel(Parcel in) {
            return new ChatBean(in);
        }

        @Override
        public ChatBean[] newArray(int size) {
            return new ChatBean[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(userInfo, flags);
        dest.writeParcelable(userInfoAdmin, flags);
        dest.writeByte((byte) (inDeleteList ? 1 : 0));
    }
}

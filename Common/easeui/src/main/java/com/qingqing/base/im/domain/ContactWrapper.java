package com.qingqing.base.im.domain;

import android.os.Parcel;

/**
 * Created by huangming on 2015/12/23.
 */
public class ContactWrapper extends ContactInfo {

    public ContactWrapper(String username, int friends, int sex, String type) {
        super(username, friends, sex, Type.valueOf(type));
    }

    public ContactWrapper(String username, int friends, int sex, Type type) {
        super(username, friends, sex, type);
    }

    public ContactWrapper(ContactInfo contactInfo) {
        super(contactInfo.getUsername(), contactInfo.getFriends(), contactInfo.getSex(), contactInfo.getType());
        setAlias(contactInfo.getAlias());
        setNick(contactInfo.getNick());
        setSex(contactInfo.getSex());
        setAvatar(contactInfo.getAvatar());
        setExtra(contactInfo.getExtra());
        setInitialLetter(contactInfo.getInitialLetter());
        setPhone(contactInfo.getPhone());
        setUserId(contactInfo.getUserId());
        setLever(contactInfo.getLever());
        setNew(contactInfo.isNew());
        setProfileComplete((long)contactInfo.getProfileComplete());
        setHasNew(contactInfo.isHasNew());
    }

    protected ContactWrapper(Parcel in) {
        super(in);
    }

    public static final Creator<ContactWrapper> CREATOR = new Creator<ContactWrapper>() {
        @Override
        public ContactWrapper createFromParcel(Parcel in) {
            return new ContactWrapper(in);
        }

        @Override
        public ContactWrapper[] newArray(int size) {
            return new ContactWrapper[size];
        }
    };

}

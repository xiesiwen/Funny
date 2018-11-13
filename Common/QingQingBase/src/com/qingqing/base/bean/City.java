package com.qingqing.base.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by huangming on 2016/4/1.
 *
 * VO 城市信息
 */
public class City implements Parcelable {
    
    public static final City NONE = new City(0, "", 0, false, false, false, false, false);
    
    /**
     * 轻轻定义
     */
    public final int id;
    public final String name;
    /**
     * 高德定义
     */
    public final int code;
    /**
     * 是否开放， 5.8.0 替换为 isVisible && isStationed
     */
    @Deprecated
    public final boolean isOpen;
    
    /**
     * 是否可见
     */
    public final boolean isVisible;
    
    /**
     * 是否为驻点城市
     */
    public final boolean isStationed;

    /**
     * 是否为驻点城市
     */
    public final boolean isHot;

    /**
     * 是否测试
     */
    public final boolean isTest;
    
    private City(int id, String name, int code, boolean isOpen, boolean isVisible,
                 boolean isStationed, boolean isHot, boolean isTest) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.isOpen = isOpen;
        this.isVisible = isVisible;
        this.isStationed = isStationed;
        this.isTest = isTest;
        this.isHot = isHot;
    }
    
    public static City open(int id, String name, int code) {
        return new City(id, name, code, true, true, true, false, false);
    }
    
    public static City unopen(int id, String name, int code) {
        return new City(id, name, code, false, false, false, false, false);
    }

    public static City valueOf(int id, String name, int code, boolean isOpen,
                               boolean isVisible, boolean isStationed, boolean isTest) {
        //todo add hot params into database add delete this function
        return new City(id, name, code, isOpen, isVisible, isStationed, false, isTest);
    }
    
    public static City valueOf(int id, String name, int code, boolean isOpen,
                               boolean isVisible, boolean isStationed, boolean isHot, boolean isTest) {
        return new City(id, name, code, isOpen, isVisible, isStationed, isHot, isTest);
    }
    
    protected City(Parcel in) {
        id = in.readInt();
        name = in.readString();
        code = in.readInt();
        isOpen = in.readInt() == 1;
        isVisible = in.readInt() == 1;
        isStationed = in.readInt() == 1;
        isHot = in.readInt() == 1;
        isTest = in.readInt() == 1;
    }
    
    @Override
    public String toString() {
        return "City(id =" + id + ", name=" + name + ", code=" + code + ", isOpen="
                + isOpen + ", isVisible=" + isVisible + ", isStationed=" + isStationed
                + ", isHot = " + isHot + ", isTest=" + isTest + ")";
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeInt(code);
        dest.writeInt(isOpen ? 1 : 0);
        dest.writeInt(isVisible ? 1 : 0);
        dest.writeInt(isStationed ? 1 : 0);
        dest.writeInt(isHot ? 1 : 0);
        dest.writeInt(isTest ? 1 : 0);
    }
    
    public static final Creator<City> CREATOR = new Creator<City>() {
        @Override
        public City createFromParcel(Parcel in) {
            return new City(in);
        }
        
        @Override
        public City[] newArray(int size) {
            return new City[size];
        }
    };
    
}

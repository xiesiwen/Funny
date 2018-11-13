package com.qingqing.base.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by huangming on 2016/4/1.
 * VO(ValueObject值对象)不可变
 * 经纬度
 */
public final class LatLng implements Parcelable, Cloneable {

    public static final double INVALID_VALUE = 1000;

    public static final LatLng NONE = new LatLng(INVALID_VALUE, INVALID_VALUE);

    public final double latitude;
    public final double longitude;

    public LatLng(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    protected LatLng(Parcel in) {
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
    }


    public LatLng clone() {
        return new LatLng(this.latitude, this.longitude);
    }

    public int hashCode() {
        int result = 17;
        long f;

        f = Double.doubleToLongBits(this.latitude);
        result = 37 * result + (int) (f ^ (f >>> 32));

        f = Double.doubleToLongBits(this.longitude);
        result = 37 * result + (int) (f ^ (f >>> 32));

        return result;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof LatLng)) {
            return false;
        } else {
            LatLng other = (LatLng) o;
            return Double.doubleToLongBits(this.latitude) == Double.doubleToLongBits(other.latitude) && Double.doubleToLongBits(this.longitude) == Double.doubleToLongBits(other.longitude);
        }
    }

    public String toString() {
        return "lat/lng: (" + this.latitude + "," + this.longitude + ")";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flag) {
        dest.writeDouble(this.latitude);
        dest.writeDouble(this.longitude);
    }

    public static final Creator<LatLng> CREATOR = new Creator<LatLng>() {
        @Override
        public LatLng createFromParcel(Parcel in) {
            return new LatLng(in);
        }

        @Override
        public LatLng[] newArray(int size) {
            return new LatLng[size];
        }
    };

}


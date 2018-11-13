package com.qingqing.base.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by huangming on 2016/4/1.
 * VO值对象:不可变
 * 地址对象
 */
public class Address implements Parcelable {

    public static final Address NONE = new Address("", LatLng.NONE, City.NONE);

    private static Address sLocationAddress = NONE;

    public final String detail;
    public final LatLng latLng;
    public final City city;
    public final String district;

    public Address(String detail, LatLng latLng, City city) {
        this(detail, latLng, city, "");
    }

    public Address(String detail, LatLng latLng, City city, String district) {
        this.detail = detail;
        this.latLng = latLng;
        this.city = city;
        this.district = district;
    }

    protected Address(Parcel in) {
        detail = in.readString();
        latLng = LatLng.CREATOR.createFromParcel(in);
        city = City.CREATOR.createFromParcel(in);
        district = in.readString();
    }

    @Override
    public String toString() {
        return "Address(detail=" + detail + ", city=" + city.toString() + ", latLng=" + latLng.toString() + ", district=" + district + ")";
    }

    public static void setLocation(Address address) {
        sLocationAddress = address;
    }

    public static Address getLocation() {
        return sLocationAddress;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(detail);
        latLng.writeToParcel(dest, 0);
        city.writeToParcel(dest, 0);
        dest.writeString(district);
    }

    public static final Creator<Address> CREATOR = new Creator<Address>() {
        @Override
        public Address createFromParcel(Parcel in) {
            return new Address(in);
        }

        @Override
        public Address[] newArray(int size) {
            return new Address[size];
        }
    };

}

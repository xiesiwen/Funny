package com.qingqing.base.utils;

import android.text.TextUtils;

import com.qingqing.base.bean.Address;
import com.qingqing.base.bean.City;
import com.qingqing.base.bean.LatLng;

/**
 * Created by whuthm on 2016/4/15. 合法性验证以及相关对象的创建
 */
public class AddressUtil {
    
    public static int getCityCode(String cityCode) {
        try {
            return Integer.parseInt(cityCode);
        } catch (Exception e) {}
        return 0;
    }

    public static boolean isLatValid(double latitude) {
        return MathUtil.notLessThan(latitude, -90.0D)
                && MathUtil.notMoreThan(latitude, 90.0D);
    }

    public static boolean isLngValid(double longitude) {
        return MathUtil.notLessThan(longitude, -180.0D)
                && MathUtil.notMoreThan(longitude, 180.0D);
    }
    
    public static boolean isLatLngValid(LatLng latLng) {
        return latLng != null && isLatValid(latLng.latitude)
                && isLngValid(latLng.longitude);
    }
    
    public static boolean isCityValid(City city) {
        return city != null && city.id > 0 && !TextUtils.isEmpty(city.name)
                && city.code > 0;
    }
    
    public static boolean isAddressValid(Address address) {
        return address != null && isCityValid(address.city)
                && isLatLngValid(address.latLng) && !TextUtils.isEmpty(address.detail);
    }
    
    /**
     * 用于跳转到地址编辑页的数据
     */
    public static Address createToLocation(LatLng latLng) {
        return new Address("", latLng, City.NONE, "");
    }
    
    /**
     * 用于跳转到地址编辑页的数据
     */
    public static Address createToLocation(double lat, double lng) {
        return createToLocation(new LatLng(lat, lng));
    }
    
    public static Address createAddress(String detail, double lat, double lng, int cityId,
            String cityName, int cityCode) {
        return createAddress(detail, new LatLng(lat, lng),
                City.unopen(cityId, cityName, cityCode));
    }
    
    public static Address createAddress(String detail, double lat, double lng, int cityId,
            String cityName, int cityCode, String district) {
        return new Address(detail, new LatLng(lat, lng),
                City.unopen(cityId, cityName, cityCode), district);
    }
    
    public static Address createAddress(String detail, double lat, double lng, int cityId,
            String cityName, boolean isOpen, boolean isVisible, boolean isStationed,
            int cityCode, String district) {
        return new Address(detail, new LatLng(lat, lng), City.valueOf(cityId, cityName,
                cityCode, isOpen, isVisible, isStationed, false), district);
    }
    
    public static Address createAddress(String detail, LatLng latLng, City city,
            String district) {
        return new Address(detail, latLng, city, district);
    }
    
    public static Address createAddress(String detail, LatLng latLng, City city) {
        return new Address(detail, latLng, city);
    }
    
}

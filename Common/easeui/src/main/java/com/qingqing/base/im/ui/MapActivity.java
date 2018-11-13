package com.qingqing.base.im.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.easemob.easeui.R;
import com.qingqing.qingqingbase.ui.BaseActionBarActivity;
import com.qingqing.base.im.Constant;
import com.qingqing.base.view.ToastWrapper;

/**
 * Created by huangming on 2015/12/25.
 */
public class MapActivity extends BaseActionBarActivity implements View.OnClickListener, AMapLocationListener {

    private MapView mapView;
    private AMap aMap;
    private AMapLocationClient mLocationClient;

    private double latitude;
    private double longitude;
    private String address;

    private boolean hasData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        findViewById(R.id.btn_ok).setOnClickListener(this);
        mapView = (MapView) findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);// 必须要写
        aMap = mapView.getMap();

        UiSettings uiSettings = aMap.getUiSettings();
        if (uiSettings != null) {
            uiSettings.setZoomControlsEnabled(false);// 隐藏缩放按钮
        }
        aMap.moveCamera(CameraUpdateFactory.zoomTo(18));
        aMap.setOnInfoWindowClickListener(new AMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker arg0) {
                arg0.hideInfoWindow();
            }
        });

        Intent intent = getIntent();
        if (intent != null) {
            latitude = intent.getDoubleExtra(Constant.EXTRA_LATITUDE, 0);
            longitude = intent.getDoubleExtra(Constant.EXTRA_LONGITUDE, 0);
            if (latitude != 0 && longitude != 0) {
                hasData = true;
            }
        }
        if (hasData) {
            findViewById(R.id.btn_ok).setVisibility(View.GONE);
            showLocationMap();
        } else {
            mLocationClient = new AMapLocationClient(this.getApplicationContext());
            AMapLocationClientOption option = new AMapLocationClientOption();
            option.setOnceLocation(true);
            mLocationClient.setLocationOption(option);
            mLocationClient.setLocationListener(this);
            mLocationClient.startLocation();
        }
    }

    private void stopLocation() {
        if (mLocationClient != null) {
            mLocationClient.unRegisterLocationListener(this);
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
        mLocationClient = null;
    }

    private void showLocationMap() {
        LatLng latLng = new LatLng(latitude, longitude);
        aMap.clear();
        aMap.moveCamera(CameraUpdateFactory.changeLatLng(latLng));
        // 添加我的位置
        aMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("")
                .snippet("我的位置:" + latitude + ", " + longitude)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_map_point)));

        // 自定义定位成功后绘制圆形
        aMap.addCircle(new CircleOptions().center(latLng).radius(100)
                .fillColor(Color.argb(60, 67, 137, 225)).strokeColor(0xff4389E1)
                .strokeWidth(2f));
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null)
            mapView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null)
            mapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) {
            mapView.onSaveInstanceState(outState);
        }
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocation();
        if (mapView != null) {
            mapView.onDestroy();
        }
    }

    @Override
    public void onClick(View v) {
        if (latitude > 0 && longitude > 0 && !TextUtils.isEmpty(address)) {
            Intent intent = this.getIntent();
            intent.putExtra("latitude", latitude);
            intent.putExtra("longitude", longitude);
            intent.putExtra("address", address);
            this.setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null
                && aMapLocation.getErrorCode() == 0) {
            latitude = aMapLocation.getLatitude();
            longitude = aMapLocation.getLongitude();
            address = aMapLocation.getAddress();
            ToastWrapper.show("定位成功");
            showLocationMap();
        } else {
            ToastWrapper.show("定位失败");
        }

        stopLocation();

    }
}

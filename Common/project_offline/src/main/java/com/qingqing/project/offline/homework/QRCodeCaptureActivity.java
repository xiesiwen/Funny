package com.qingqing.project.offline.homework;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.MenuItemCompat;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.qingqing.qingqingbase.ui.BaseActionBarActivity;
import com.qingqing.base.activity.HtmlActivity;
import com.qingqing.base.config.CommonUrl;
import com.qingqing.base.constant.BaseParamKeys;
import com.qingqing.base.dns.DNSManager;
import com.qingqing.project.offline.R;

/**
 * Created by wangxiaxin on 2017/6/22.
 *
 * 题库的扫码界面
 */

public class QRCodeCaptureActivity extends BaseActionBarActivity {
    
    private CaptureManager capture;
    private DecoratedBarcodeView barcodeScannerView;
    
    private static final int MENU_ID_HELP = 111;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        barcodeScannerView = initializeContent();
        
        capture = new CaptureManager(this, barcodeScannerView);
        capture.initializeFromIntent(getIntent(), savedInstanceState);
        capture.decode();
    }
    
    /**
     * Override to use a different layout.
     *
     * @return the DecoratedBarcodeView
     */
    protected DecoratedBarcodeView initializeContent() {
        setContentView(R.layout.activity_qccode_capture);
        return (DecoratedBarcodeView) findViewById(R.id.zxing_barcode_scanner);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.add(Menu.NONE, MENU_ID_HELP, Menu.NONE,
                getString(R.string.text_tiku_info));
        MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        
        switch (item.getItemId()) {
            case MENU_ID_HELP:
                Intent intent = new Intent(this, HtmlActivity.class);
                if (DNSManager.INSTANCE().isIntranet()){
                    intent.putExtra(BaseParamKeys.PARAM_STRING_URL, CommonUrl.H5_TIKU_TEST.url().url());
                }else{
                    intent.putExtra(BaseParamKeys.PARAM_STRING_URL,CommonUrl.H5_TIKU.url().url());
                }
                startActivity(intent);
                break;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        capture.onResume();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        capture.onPause();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        capture.onDestroy();
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        capture.onSaveInstanceState(outState);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
            @NonNull int[] grantResults) {
        capture.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeScannerView.onKeyDown(keyCode, event)
                || super.onKeyDown(keyCode, event);
    }
}

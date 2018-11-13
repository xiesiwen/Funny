package com.qingqing.project.offline.view.city;

import com.qingqing.base.constant.BaseParamKeys;
import com.qingqing.project.offline.R;
import com.qingqing.qingqingbase.ui.BaseActionBarActivity;

import android.content.Intent;
import android.os.Bundle;

/**
 * Created by xiejingwen on 2015/9/11.
 * 城市选择页面
 */
public class BaseSelectCityActivity extends BaseActionBarActivity {
    protected BaseSelectCityFragment mBaseSelectCityFragment;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_city);
        mFragAssist.setGroupID(R.id.full_screen_fragment_container);
        setTitle(R.string.select_city_title);
        mBaseSelectCityFragment = createBaseSelectCityFragment();
        mBaseSelectCityFragment
                .setFragListener(new BaseSelectCityFragment.SelectCityFragListener() {
                    @Override
                    public void startLocation() {
                        onStartLocation();
                    }

                    @Override
                    public void chooseCity(int cityId) {
                        onChooseCity(cityId);
                    }
                    
                    @Override
                    public void onStart() {}
                    
                    @Override
                    public void onStop() {}
                });

        Intent intent = getIntent();
        if (intent != null) {
            mBaseSelectCityFragment
                    .setCurrent(intent.getIntExtra(BaseParamKeys.PARAM_INT_CITY_ID, 0));
        }
        mFragAssist.setBottom(mBaseSelectCityFragment);
    }
    
    protected void onChooseCity(int cityId) {

    }
    
    protected void onStartLocation() {

    }

    protected BaseSelectCityFragment createBaseSelectCityFragment(){
        return new BaseSelectCityFragment();
    }
}

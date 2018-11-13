package com.qingqing.project.offline.order.v3;

import android.content.Intent;
import android.os.Bundle;

import com.qingqing.base.constant.BaseParamKeys;
import com.qingqing.project.offline.R;
import com.qingqing.project.offline.seltime.SelectTimeHelper;
import com.qingqing.qingqingbase.ui.BaseActivity;

/**
 * 下单选时间界面v3 @5.9.6
 *
 * <p>
 * 支持下单、续课选择多次时间
 * </p>
 *
 * <p>
 * 多次时间支持选择周期时间并自动生成余下时间、以及自定义选择
 * </p>
 *
 * Created by tanwei on 2017/8/7.
 */

public class SelectTimeActivityV3 extends BaseActivity {
    
    private static final int LIMIT_COUNT = 4;
    
    private SelectTimeCircleFragment mSelTimeCircleFragment;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_fragment);
        mFragAssist.setGroupID(R.id.full_screen_fragment_container);
        
        SelectTimeHelper.updateTimeConfig();
        
        if (getIntent().hasExtra(BaseParamKeys.PARAM_PARCEL_SELECT_TIME)) {
            SelectTimeParamsV3 params = getIntent()
                    .getParcelableExtra(BaseParamKeys.PARAM_PARCEL_SELECT_TIME);
            
            if (params.getCount() <= LIMIT_COUNT) {
                toSelectTimeOptional(params);
            }
            else {
                toSelectTimeCircle(params);
            }
        }
    }
    
    // 选择周期时间
    private void toSelectTimeCircle(SelectTimeParamsV3 params) {
        if (mSelTimeCircleFragment == null) {
            mSelTimeCircleFragment = new SelectTimeCircleFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelable(BaseParamKeys.PARAM_PARCEL_SELECT_TIME, params);
            mSelTimeCircleFragment.setArguments(bundle);
            mSelTimeCircleFragment.setFragListener(new SelectTimeFragListener() {
                
                @Override
                public void done(SelectTimeParamsV3 params) {
                    if (params.isOptional()) {
                        toSelectTimeOptional(params);
                    }
                    else {
                        toSelectTimeResult(params);
                    }
                }

                @Override
                public void onStart() {
                    
                }
                
                @Override
                public void onStop() {
                    
                }
            });
        }
        mFragAssist.setBottom(mSelTimeCircleFragment);
    }
    
    // 选择单次日期
    private void toSelectDateTime(SelectTimeParamsV3 params) {
        SelectDateTimeFragment selectDateTimeFragment = new SelectDateTimeFragment();
        if (params != null) {
            Bundle bundle = new Bundle();
            bundle.putParcelable(BaseParamKeys.PARAM_PARCEL_SELECT_TIME, params);
            selectDateTimeFragment.setArguments(bundle);
            selectDateTimeFragment.setFragListener(new SelectTimeFragListener() {

                @Override
                public void done(SelectTimeParamsV3 params) {
                    mFragAssist.pop();

                }

                @Override
                public void onStart() {

                }

                @Override
                public void onStop() {

                }
            });
        }
        mFragAssist.push(selectDateTimeFragment, false);
    }

    // 选择任意时间
    private void toSelectTimeOptional(SelectTimeParamsV3 params) {
        SelectDateTimeFragment fragment = new SelectDateTimeFragment();
        Bundle bundle = new Bundle();
        params.setOptional(true);
        bundle.putParcelable(BaseParamKeys.PARAM_PARCEL_SELECT_TIME, params);
        fragment.setArguments(bundle);
        fragment.setFragListener(new SelectTimeFragListener() {
            
            @Override
            public void done(SelectTimeParamsV3 params) {
                // put result
                Intent intent = new Intent();
                intent.putParcelableArrayListExtra(BaseParamKeys.PARAM_ARRAY_LIST_SELECTED_TIME,
                        params.getTimeList());
                setResult(RESULT_OK, intent);

                finish();
            }

            @Override
            public void onStart() {
                
            }
            
            @Override
            public void onStop() {
                
            }
        });
        mFragAssist.push(fragment, false);
    }
    
    // 时间列表展示
    private void toSelectTimeResult(SelectTimeParamsV3 params) {
        SelectTimeResultFragment fragment = new SelectTimeResultFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(BaseParamKeys.PARAM_PARCEL_SELECT_TIME, params);
        fragment.setArguments(bundle);
        fragment.setFragListener(new SelectTimeFragListener() {
            
            @Override
            public void done(SelectTimeParamsV3 params) {
                
                // put result
                Intent intent = new Intent();
                intent.putParcelableArrayListExtra(BaseParamKeys.PARAM_ARRAY_LIST_SELECTED_TIME,
                        params.getTimeList());
                setResult(RESULT_OK, intent);

                finish();
            }

            @Override
            public void onStart() {
                
            }
            
            @Override
            public void onStop() {
                
            }
        });
        
        mFragAssist.push(fragment, false);
    }

}

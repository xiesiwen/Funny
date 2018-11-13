package com.qingqing.project.offline.order.v3;

import com.qingqing.base.constant.BaseParamKeys;
import com.qingqing.project.offline.R;
import com.qingqing.project.offline.seltime.SelectTimeHelper;
import com.qingqing.qingqingbase.ui.BaseActionBarActivity;

import android.content.Intent;
import android.os.Bundle;

/**
 * 下单选时间界面 @5.9.6
 *
 * <p>
 * 支持选择单次时间：调课、以及下单选时间流程中选择周期时间、修改时间
 * </p>
 *
 * Created by tanwei on 2017/8/7.
 */

public class InternalSelectTimeActivity extends BaseActionBarActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_fragment);
        mFragAssist.setGroupID(R.id.full_screen_fragment_container);
        
        SelectTimeHelper.updateTimeConfig();
        
        if (getIntent().hasExtra(BaseParamKeys.PARAM_PARCEL_SELECT_TIME)) {
            SelectTimeParamsV3 params = getIntent()
                    .getParcelableExtra(BaseParamKeys.PARAM_PARCEL_SELECT_TIME);
            toSelectDateTime(params);
        }
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
                    
                    // put result
                    Intent intent = new Intent();
                    intent.putExtra(BaseParamKeys.PARAM_ARRAY_LIST_SELECTED_TIME,
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
        }
        mFragAssist.push(selectDateTimeFragment, false);
    }
    
}

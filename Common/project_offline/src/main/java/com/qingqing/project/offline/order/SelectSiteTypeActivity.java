package com.qingqing.project.offline.order;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.qingqing.qingqingbase.ui.BaseActionBarActivity;
import com.qingqing.base.activity.ParamSelectFragment;
import com.qingqing.base.constant.BaseParamKeys;
import com.qingqing.project.offline.constant.CourseConstant;
import com.qingqing.base.view.ToastWrapper;
import com.qingqing.project.offline.R;

import java.util.ArrayList;

/**
 * Created by wangxiaxin on 2016/6/14.
 *
 * 选择 上门方式 的 界面 <br/>
 *
 *
 * @see BaseParamKeys#PARAM_INT_ARRAY_SITE_TYPE
 * @see BaseParamKeys#PARAM_INT_SITE_TYPE_VALUE
 */
public class SelectSiteTypeActivity extends BaseActionBarActivity {
    
    private ArrayList<String> mSiteTypeList = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_fragment);
        setFragGroupID(R.id.full_screen_fragment_container);
        
        Intent intent = getIntent();
        ArrayList<Integer> siteTypeList = intent
                .getIntegerArrayListExtra(BaseParamKeys.PARAM_INT_ARRAY_SITE_TYPE);
        if (siteTypeList == null) {
            ToastWrapper.show("no param");
            finish();
            return;
        }
        
        for (Integer i : siteTypeList) {
            String s = CourseConstant.getSiteType(i);
            if (!TextUtils.isEmpty(s)) {
                mSiteTypeList.add(s);
            }
        }
        
        int defaultValue = intent
                .getIntExtra(BaseParamKeys.PARAM_INT_SITE_TYPE_VALUE, -1);
        String defaultValueString = CourseConstant.getSiteType(defaultValue);
        int defaultIdx = mSiteTypeList.indexOf(defaultValueString);
        
        Bundle bundle = new Bundle();
        bundle.putString(ParamSelectFragment.PARAM_STRING_TITLE,
                getString(R.string.site_type_title));
        bundle.putInt(ParamSelectFragment.PARAM_INT_INDEX, defaultIdx);
        bundle.putStringArrayList(ParamSelectFragment.PARAM_STRING_LIST_PARAMS,
                mSiteTypeList);
        ParamSelectFragment selectFragment = new ParamSelectFragment();
        selectFragment.setFragListener(new ParamSelectFragment.ParamSelectFragListener() {
            @Override
            public void onParamSelected(int idx) {
                String value = mSiteTypeList.get(idx);
                int type = CourseConstant.getSiteType(value);
                Intent intent = new Intent();
                intent.putExtra(BaseParamKeys.PARAM_INT_SITE_TYPE_VALUE, type);
                setResult(RESULT_OK, intent);
            }
            
            @Override
            public void onStart() {}
            
            @Override
            public void onStop() {}
        });
        selectFragment.setArguments(bundle);
        mFragAssist.push(selectFragment);
    }
}

package com.qingqing.project.offline.order;

import android.content.Intent;
import android.os.Bundle;

import com.qingqing.api.proto.v1.OrderCommonEnum;
import com.qingqing.qingqingbase.ui.BaseActionBarActivity;
import com.qingqing.base.activity.ParamSelectFragment;
import com.qingqing.base.constant.BaseParamKeys;
import com.qingqing.project.offline.constant.CourseConstant;
import com.qingqing.base.view.ToastWrapper;
import com.qingqing.project.offline.R;

import java.util.ArrayList;

/**
 * Created by wangxiaxin on 2016/6/14.<br/>
 *
 * 选择 课程类型的 界面 <br/>
 * <br/>
 *
 *
 * {@link BaseParamKeys#PARAM_INT_COURSE_PRICE_TYPE_VALUE} 默认选中的课程类型<br/>
 * {@link BaseParamKeys#PARAM_INT_ARRAY_COURSE_PRICE_TYPE_VALUE} 课程类型列表<br/>
 */
public class SelectCoursePriceTypeActivity extends BaseActionBarActivity {
    
    private ArrayList<String> mCourseTypeList = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_fragment);
        setFragGroupID(R.id.full_screen_fragment_container);
        
        ArrayList<Integer> typeValueList = getIntent().getIntegerArrayListExtra(
                BaseParamKeys.PARAM_INT_ARRAY_COURSE_PRICE_TYPE_VALUE);
        if (typeValueList == null || typeValueList.isEmpty()) {
            ToastWrapper.show("param error");
            finish();
            return;
        }
        
        for (int value : typeValueList) {
            mCourseTypeList.add(CourseConstant.getCourseType(value));
        }
        
        int defaultValue = getIntent()
                .getIntExtra(
                        BaseParamKeys.PARAM_INT_COURSE_PRICE_TYPE_VALUE,
                        OrderCommonEnum.TeacherCoursePriceType.unknown_teacher_course_price_type);
        String defaultValueString = CourseConstant.getCourseType(defaultValue);
        int defaultIdx = mCourseTypeList.indexOf(defaultValueString);
        
        Bundle bundle = new Bundle();
        bundle.putString(ParamSelectFragment.PARAM_STRING_TITLE,
                getString(R.string.course_type_title));
        bundle.putInt(ParamSelectFragment.PARAM_INT_INDEX, defaultIdx);
        bundle.putStringArrayList(ParamSelectFragment.PARAM_STRING_LIST_PARAMS,
                mCourseTypeList);
        bundle.putString(ParamSelectFragment.PARAM_STRING_FOOTER, getResources()
                .getString(R.string.course_type_tips));
        
        ParamSelectFragment selectFragment = new ParamSelectFragment();
        selectFragment.setFragListener(new ParamSelectFragment.ParamSelectFragListener() {
            @Override
            public void onParamSelected(int idx) {
                String value = mCourseTypeList.get(idx);
                int type = CourseConstant
                        .getCourseType(value);
                Intent intent = new Intent();
                intent.putExtra(BaseParamKeys.PARAM_INT_COURSE_PRICE_TYPE_VALUE,
                        type);
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

package com.qingqing.project.offline.order;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.qingqing.api.proto.v1.GradeCourseProto;
import com.qingqing.qingqingbase.ui.BaseActionBarActivity;
import com.qingqing.base.activity.ParamSelectFragment;
import com.qingqing.base.constant.BaseParamKeys;
import com.qingqing.base.view.ToastWrapper;
import com.qingqing.project.offline.R;

import java.util.ArrayList;

/**
 * Created by wangxiaxin on 2016/8/5.
 *
 * 选择 年级组的界面
 *
 * 小学，初中，高中 。。。
 *
 * 输入参数：
 * 
 * @see BaseParamKeys#PARAM_STRING_TITLE
 * @see BaseParamKeys#PARAM_PARCELABLE_ARRAY_GRADE_GROUP
 * @see BaseParamKeys#PARAM_PARCELABLE_GRADE_GROUP 可选（默认选中项）
 * 
 *      返回参数
 * @see BaseParamKeys#PARAM_PARCELABLE_GRADE_GROUP
 */
public class SelectGradeGroupActivity extends BaseActionBarActivity {
    
    private ArrayList<String> mGradeGroupTypeList = new ArrayList<>();
    private ArrayList<GradeCourseProto.GradeGroup> mTypeValueList;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_fragment);
        setFragGroupID(R.id.full_screen_fragment_container);
        
        String title = getIntent().getStringExtra(BaseParamKeys.PARAM_STRING_TITLE);
        if (!TextUtils.isEmpty(title)) {
            setTitle(title);
        }
        
        mTypeValueList = getIntent().getParcelableArrayListExtra(
                BaseParamKeys.PARAM_PARCELABLE_ARRAY_GRADE_GROUP);
        if (mTypeValueList == null || mTypeValueList.isEmpty()) {
            ToastWrapper.show("param error");
            finish();
            return;
        }
        
        for (GradeCourseProto.GradeGroup gradeGroup : mTypeValueList) {
            mGradeGroupTypeList.add(gradeGroup.gradeGroupName);
        }
        
        GradeCourseProto.GradeGroup defaultGradeGroup = getIntent()
                .getParcelableExtra(BaseParamKeys.PARAM_PARCELABLE_GRADE_GROUP);
        
        Bundle bundle = new Bundle();
        if (defaultGradeGroup != null) {
            for (int i = 0; i < mGradeGroupTypeList.size(); i++) {
                if (defaultGradeGroup.gradeGroupType == (int) mTypeValueList
                        .get(i).gradeGroupType) {
                    bundle.putInt(ParamSelectFragment.PARAM_INT_INDEX, i);
                    break;
                }
            }
        }
        
        bundle.putString(ParamSelectFragment.PARAM_STRING_TITLE, title);
        
        bundle.putStringArrayList(ParamSelectFragment.PARAM_STRING_LIST_PARAMS,
                mGradeGroupTypeList);
        
        ParamSelectFragment selectFragment = new ParamSelectFragment();
        selectFragment.setFragListener(new ParamSelectFragment.ParamSelectFragListener() {
            @Override
            public void onParamSelected(int idx) {
                Intent intent = new Intent();
                intent.putExtra(BaseParamKeys.PARAM_PARCELABLE_GRADE_GROUP,
                        mTypeValueList.get(idx));
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

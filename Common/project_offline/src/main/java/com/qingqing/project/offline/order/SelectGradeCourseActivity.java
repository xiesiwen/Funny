package com.qingqing.project.offline.order;

import java.util.ArrayList;
import java.util.List;

import com.qingqing.api.proto.v1.app.AppCommon;
import com.qingqing.qingqingbase.ui.BaseActionBarActivity;
import com.qingqing.base.bean.Course;
import com.qingqing.base.bean.Grade;
import com.qingqing.base.constant.BaseParamKeys;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.data.DefaultDataCache;
import com.qingqing.base.view.AtMostGridView;
import com.qingqing.base.view.BaseAdapter;
import com.qingqing.base.view.TagTextItemView;
import com.qingqing.base.view.ToastWrapper;
import com.qingqing.project.offline.R;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

/**
 * Created by wangxiaxin on 2016/6/15.<br/>
 * <br/>
 *
 * 选择科目年级的界面<br/>
 * <br/>
 *
 *
 * {@link BaseParamKeys#PARAM_INT_ARRAY_COURSE_ID} 可选择的科目列表，数量为1时，不可选<br/>
 * {@link BaseParamKeys#PARAM_INT_COURSE_ID} 默认选中的科目ID，当列表数量为1时，无效<br/>
 * {@link BaseParamKeys#PARAM_INT_ARRAY_GRADE_ID}
 * 可选择的年级列表，不可选择的部分会disable，此列表为空时，表示全部可选<br/>
 * {@link BaseParamKeys#PARAM_INT_GRADE_ID}默认选中的年级ID,如果此ID不在可选列表中，则忽略<br/>
 *
 */
public class SelectGradeCourseActivity extends BaseActionBarActivity {
    
    private TextView mTVPrimaryGrade;
    private TextView mTVMiddleGrade;
    private TextView mTVHighGrade;
    
    private AtMostGridView mGVPrimaryGrade;
    private AtMostGridView mGVMiddleGrade;
    private AtMostGridView mGVHighGrade;
    
    private ArrayList<Integer> mOriCourseIdList;
    private ArrayList<Course> mCourseList;
    private ArrayList<Integer> mDefaultSelectCourseIds = new ArrayList<Integer>();
    
    private ArrayList<Integer> mOriGradeIdList;
    private CourseHolder mHolderLastSelectedCourse;
    private GradeHolder mHolderLastSelectedGrade;
    private ArrayList<CourseHolder> mHolderSelectedCourses = new ArrayList<CourseHolder>();
    
    private int mDefaultSelectCourseId = -1;
    private int mDefaultSelectGradeId = -1;
    
    public static final String DEFAULT_COURSES = "DEFAULTCOURSES";
    public static final String MUTLI_CHOOSE = "MUTLICHOOSE";
    
    private boolean mMutliChooseCourse = false;
    private TextView mTVConfirm;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMutliChooseCourse = getIntent().getBooleanExtra(MUTLI_CHOOSE,false);
        setContentView(R.layout.activity_select_grade_course);
        initView(savedInstanceState);
    }
    
    private void initView(Bundle savedInstanceState) {
        
        setTitle(R.string.grade_course_title);
        
        View layoutFixedCourse = findViewById(R.id.layout_fixed_course);
        View layoutSelectCourse = findViewById(R.id.layout_select_course);
        mTVConfirm = (TextView) findViewById(R.id.tv_confirm);
        switch (BaseData.getClientType()) {
            case AppCommon.AppType.qingqing_student:
                mTVConfirm
                        .setBackgroundResource(R.drawable.bg_corner_rect_green_solid_selector);
                break;
            case AppCommon.AppType.qingqing_teacher:
                mTVConfirm
                        .setBackgroundResource(R.drawable.bg_corner_rect_blue_solid_selector);
                break;
            case AppCommon.AppType.qingqing_ta:
                mTVConfirm
                        .setBackgroundResource(R.drawable.bg_corner_rect_primary_orange_solid_selector);
                break;
        }
        
        mTVConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
                if (mMutliChooseCourse) {
                    if (mHolderSelectedCourses.size() == 0) {
                        ToastWrapper.show(R.string.text_select_course_first);
                        return;
                    }
                }
                else {
                    if (mOriCourseIdList.size() > 1 && mHolderLastSelectedCourse == null) {
                        ToastWrapper.show(R.string.text_select_course_first);
                        return;
                    }
                }
                
                if (mHolderLastSelectedGrade == null) {
                    ToastWrapper.show(R.string.text_select_grade);
                    return;
                }
                
                selectDone();
            }
        });
        
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            bundle = savedInstanceState;
        }
        
        boolean hasError = true;
        do {
            
            if (bundle == null)
                break;
            
            mOriCourseIdList = bundle
                    .getIntegerArrayList(BaseParamKeys.PARAM_INT_ARRAY_COURSE_ID);
            if (mOriCourseIdList == null || mOriCourseIdList.isEmpty()) {
                break;
            }
            
            mOriGradeIdList = bundle
                    .getIntegerArrayList(BaseParamKeys.PARAM_INT_ARRAY_GRADE_ID);
            
            if (mOriCourseIdList.size() == 1) {
                layoutSelectCourse.setVisibility(View.GONE);
                TextView tvFixedCourse = (TextView) findViewById(R.id.tv_fixed_course);
                tvFixedCourse.setText(DefaultDataCache.INSTANCE().getCourseNameById(
                        mOriCourseIdList.get(0)));
                mTVConfirm.setVisibility(View.GONE);
            }
            else {
                if (mMutliChooseCourse){
                    mDefaultSelectCourseIds = bundle.getIntegerArrayList(DEFAULT_COURSES);
                    layoutFixedCourse.setVisibility(View.GONE);
                    AtMostGridView mGVCourse = (AtMostGridView) findViewById(R.id.gv_course);
                    mCourseList = new ArrayList<>();
                    for (int courseId : mOriCourseIdList) {
                        mCourseList.add(DefaultDataCache.INSTANCE().getCourseById(
                                courseId));
                    }
                    CourseAdapter adapter = new CourseAdapter(this, mCourseList);
                    mGVCourse.setAdapter(adapter);
                    mGVCourse
                            .setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view,
                                                        int position, long id) {
                                    View view1 = view.findViewById(R.id.tv_content);
                                    boolean isSelected = view1.isSelected();
                                    if (isSelected) {
                                        view1.setSelected(false);
                                        if (mHolderSelectedCourses
                                                .contains((CourseHolder) view.getTag())) {
                                            mHolderSelectedCourses
                                                    .remove((CourseHolder) view.getTag());
                                        }
                                    }
                                    else {
                                        view1.setSelected(true);
                                        if (!mHolderSelectedCourses
                                                .contains((CourseHolder) view.getTag())) {
                                            mHolderSelectedCourses
                                                    .add((CourseHolder) view.getTag());
                                        }
                                    }
                                }
                            });
                }else{
                    mDefaultSelectCourseId = bundle.getInt(
                            BaseParamKeys.PARAM_INT_COURSE_ID, -1);
                    layoutFixedCourse.setVisibility(View.GONE);
                    AtMostGridView mGVCourse = (AtMostGridView) findViewById(R.id.gv_course);
                    mCourseList = new ArrayList<>();
                    for (int courseId : mOriCourseIdList) {
                        mCourseList.add(DefaultDataCache.INSTANCE().getCourseById(
                                courseId));
                    }
                    CourseAdapter adapter = new CourseAdapter(this, mCourseList);
                    mGVCourse.setAdapter(adapter);
                    mGVCourse
                            .setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view,
                                                        int position, long id) {

                                    if (mHolderLastSelectedCourse != null) {
                                        mHolderLastSelectedCourse.tvName
                                                .setSelected(false);
                                    }

                                    /*if (mHolderLastSelectedGrade != null) {
                                        mHolderLastSelectedGrade.tvName
                                                .setSelected(false);
                                    }*/

                                    CourseHolder holder = (CourseHolder) view.getTag();
                                    holder.tvName.setSelected(true);
                                    mHolderLastSelectedCourse = holder;
                                }
                            });
                }
            }
            
            mDefaultSelectGradeId = bundle.getInt(BaseParamKeys.PARAM_INT_GRADE_ID, -1);
            mTVPrimaryGrade = (TextView) findViewById(R.id.tv_grade_group1);
            mTVMiddleGrade = (TextView) findViewById(R.id.tv_grade_group2);
            mTVHighGrade = (TextView) findViewById(R.id.tv_grade_group3);
            
            mGVPrimaryGrade = (AtMostGridView) findViewById(R.id.gv_primary_grade);
            mGVMiddleGrade = (AtMostGridView) findViewById(R.id.gv_middle_grade);
            mGVHighGrade = (AtMostGridView) findViewById(R.id.gv_high_grade);
            
            ArrayList<ArrayList<Grade>> groupGradeList = DefaultDataCache.INSTANCE()
                    .getClassifiedGradeList();
            if (groupGradeList != null && groupGradeList.size() >= 3) {
                mTVPrimaryGrade.setText(groupGradeList.get(0).get(0).getGroupName());
                GradeAdapter adapter = new GradeAdapter(this, groupGradeList.get(0));
                mGVPrimaryGrade.setAdapter(adapter);
                mGVPrimaryGrade.setOnItemClickListener(mGradeClickListener);
                
                mTVMiddleGrade.setText(groupGradeList.get(1).get(0).getGroupName());
                adapter = new GradeAdapter(this, groupGradeList.get(1));
                mGVMiddleGrade.setAdapter(adapter);
                mGVMiddleGrade.setOnItemClickListener(mGradeClickListener);
                
                mTVHighGrade.setText(groupGradeList.get(2).get(0).getGroupName());
                adapter = new GradeAdapter(this, groupGradeList.get(2));
                mGVHighGrade.setAdapter(adapter);
                mGVHighGrade.setOnItemClickListener(mGradeClickListener);
            }
            
            hasError = false;
        } while (false);
        
        if (hasError) {
            ToastWrapper.show("invalid param");
            finish();
        }
    }
    
    private boolean isGradleIdEnable(int gradeId) {
        
        if (mOriGradeIdList == null || mOriGradeIdList.isEmpty())
            return true;
        
        for (int id : mOriGradeIdList) {
            if (id == gradeId)
                return true;
        }
        return false;
    }
    
    private void selectDone() {
        Intent intent = new Intent();
        
        int courseId;
        if (mMutliChooseCourse) {
            ArrayList<Integer> selectedCourses = new ArrayList<Integer>();
            for (int i = 0; i < mHolderSelectedCourses.size(); i++) {
                selectedCourses.add(mHolderSelectedCourses.get(i).course.getId());
            }
            intent.putExtra(BaseParamKeys.PARAM_INT_ARRAY_COURSE_ID, selectedCourses);
            intent.putExtra(BaseParamKeys.PARAM_INT_GRADE_ID,
                    mHolderLastSelectedGrade.data.getId());
        }
        else {
            if (mOriCourseIdList.size() == 1) {
                courseId = mOriCourseIdList.get(0);
            }
            else {
                courseId = mHolderLastSelectedCourse.course.getId();
            }
            intent.putExtra(BaseParamKeys.PARAM_INT_COURSE_ID, courseId);
            intent.putExtra(BaseParamKeys.PARAM_INT_GRADE_ID,
                    mHolderLastSelectedGrade.data.getId());
        }
        setResult(RESULT_OK, intent);
        finish();
    }
    
    private AdapterView.OnItemClickListener mGradeClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            
            GradeHolder holder = (GradeHolder) view.getTag();
            if (!holder.tvName.isEnabled())
                return;
            if (mMutliChooseCourse) {
                if (mHolderSelectedCourses.size() == 0) {
                    ToastWrapper.show(R.string.text_select_course_first);
                    return;
                }
            }
            else {
                if (mOriCourseIdList.size() > 1 && mHolderLastSelectedCourse == null) {
                    ToastWrapper.show(R.string.text_select_course_first);
                    return;
                }
            }
            if (mHolderLastSelectedGrade != null) {
                mHolderLastSelectedGrade.tvName.setSelected(false);
            }
            
            holder.tvName.setSelected(true);
            mHolderLastSelectedGrade = holder;
            
            if (mOriCourseIdList.size() == 1)
                selectDone();
        }
    };
    
    class CourseAdapter extends BaseAdapter<Course> {
        
        public CourseAdapter(Context context, List<Course> list) {
            super(context, list);
        }
        
        @Override
        public View createView(Context context, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.item_course_grade_grid,
                    parent, false);
        }
        
        @Override
        public BaseAdapter.ViewHolder<Course> createViewHolder() {
            return new CourseHolder();
        }
    }
    
    class CourseHolder extends BaseAdapter.ViewHolder<Course> {
        
        private TagTextItemView tvName;
        private Course course;
        
        @Override
        public void init(Context context, View convertView) {
            tvName = (TagTextItemView) convertView.findViewById(R.id.tv_content);
        }
        
        @Override
        public void update(Context context, Course data) {
            this.course = data;
            tvName.setText(data.getName());
            if (mMutliChooseCourse) {
                if (mDefaultSelectCourseIds.size() > 0) {
                    if (mDefaultSelectCourseIds.contains(data.getId())) {
                        tvName.setSelected(true);
                        mHolderSelectedCourses.add(this);
                        mDefaultSelectCourseIds.remove((Object) data.getId());
                    }
                }
            }
            else {
                if (mDefaultSelectCourseId > 0 && data.getId() == mDefaultSelectCourseId) {
                    mDefaultSelectCourseId = -1;
                    tvName.setSelected(true);
                    mHolderLastSelectedCourse = this;
                }
            }
        }
    }
    
    class GradeAdapter extends BaseAdapter<Grade> {
        
        public GradeAdapter(Context context, List<Grade> list) {
            super(context, list);
        }
        
        @Override
        public View createView(Context context, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.item_course_grade_grid,
                    parent, false);
        }
        
        @Override
        public BaseAdapter.ViewHolder<Grade> createViewHolder() {
            return new GradeHolder();
        }
    }
    
    class GradeHolder extends BaseAdapter.ViewHolder<Grade> {
        
        private TagTextItemView tvName;
        private Grade data;
        
        @Override
        public void init(Context context, View convertView) {
            tvName = (TagTextItemView) convertView.findViewById(R.id.tv_content);
        }
        
        @Override
        public void update(Context context, Grade data) {
            this.data = data;
            tvName.setText(data.getSimpleName());
            tvName.setEnabled(isGradleIdEnable(data.getId()));
            if (mDefaultSelectGradeId > 0 && data.getId() == mDefaultSelectGradeId
                    && isGradleIdEnable(data.getId())) {
                mDefaultSelectGradeId = -1;
                tvName.setSelected(true);
                mHolderLastSelectedGrade = this;
            }
        }
    }
}

package com.qingqing.project.offline.homework;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.qingqing.api.proto.scoresvc.ScoreProto;
import com.qingqing.api.proto.v1.ImageProto.ImageItem;
import com.qingqing.api.proto.v1.ProtoBufResponse;
import com.qingqing.api.proto.v1.app.AppCommon;
import com.qingqing.api.proto.v1.serviceslice.ServiceSliceProto;
import com.qingqing.api.proto.v1.util.Common;
import com.qingqing.base.bean.ActiveInfo;
import com.qingqing.qingqingbase.ui.BaseActionBarActivity;
import com.qingqing.base.activity.HtmlActivity;
import com.qingqing.base.bean.UserBehaviorLogExtraData;
import com.qingqing.base.config.CommonUrl;
import com.qingqing.base.constant.BaseParamKeys;
import com.qingqing.base.core.ScoreManager;
import com.qingqing.base.core.UserBehaviorLogManager;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.data.DefaultDataCache;
import com.qingqing.base.data.StatisticalDataConstants;
import com.qingqing.base.http.req.ProtoListener;
import com.qingqing.base.media.AudioPlayerController;
import com.qingqing.base.media.audiomachine.AudioDownloadMachine;
import com.qingqing.base.utils.DateUtils;
import com.qingqing.base.utils.ImageUrlUtil;
import com.qingqing.base.view.AsyncImageViewV2;
import com.qingqing.base.view.AtMostListView;
import com.qingqing.base.view.BaseAdapter;
import com.qingqing.base.view.ToastWrapper;
import com.qingqing.base.view.audio.AudioDownloadView;
import com.qingqing.base.view.pager.HomeworkGalleryPage;
import com.qingqing.base.view.pager.IconPageIndicator;
import com.qingqing.base.view.pager.NoReuseViewPagerAdapter;
import com.qingqing.base.view.pager.Page;
import com.qingqing.base.view.pager.ViewPagerAdapter;
import com.qingqing.project.offline.R;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Created by tangyutian on 2016/9/21.
 * 作业详情
 */

public abstract class BaseHomeworkDetailActivity extends BaseActionBarActivity {
    protected int homeworkType = -1;
    protected int appType = -1;
    protected long homework_id = -1;
    protected String teacherName = null;
    protected String gradeCourse = null;// 语文一年级这样的
    protected ArrayList<ImageItem> photoList = new ArrayList<>();
    protected ArrayList<ServiceSliceProto.HomeworkAnswerBrief> answerList = new ArrayList<>();
    protected long answer_id = -1;
    protected int mCityId = -1;
    protected int answerStatus = ServiceSliceProto.HomeworkAnswerStatus.unknown_homework_answer_status;

    protected AtMostListView mLvStudent;
    protected TextView mTvMessageTitle;
    protected TextView mTvGradeCourse;
    protected TextView mTvMessage;
    protected RecyclerView mRvPhoto;
    protected TextView mTvCreateTime;
    protected View mLlTiku;
    protected View mLlSeeTiku;
    protected TextView mTvSeeTiku;
    protected View mLlMyHomework;
    protected View mLlSeeMyHomework;
    protected TextView mTvSeeMyHomework;
    protected TextView mTvSubmit;
    protected TextView mTvUpload;
    private View mLlOnlyTiku;
    private ImageView mIvOnlyTiku;
    private TextView mTvOnlyTiku;
    private ImageView mIvOnlyTikuArrow;
    private View mLlPrepOffline;
    private ImageView mIvPrepOffline;
    private TextView mTvPrepOffline;
    private AudioDownloadView mAvAudioPlay;
    private ImageView mIvSeeTiku;
    private ImageView mIvControl;
    private View mLlContent;
    private View mLlNoStudent;
    private View mLlFuwuhao;
    private AsyncImageViewV2 mIvFuwuhao;
    private View mLlMessageTitle;
    private TextView mTvFuwuhao;

    private Dialog mGallerydialog;
    private ViewPager mGalleryViewPager;
    private TextView mTvGallery;
    private List<Page> galleryPageList = new ArrayList<>();
    private NoReuseViewPagerAdapter galleryAdapter;
    private IconPageIndicator mGalleryIndicator;
    protected boolean canAddScore = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homework_detail);
        getType();
        findViews();
        reqDetail();
    }

    // 读取客户端类型、作业类型、order_course_id、家长端还要老师昵称
    protected void getType() {
        appType = DefaultDataCache.INSTANCE().getAppType();
        if (getIntent() != null) {
            homework_id = getIntent().getLongExtra(BaseParamKeys.PARAM_LONG_HOMEWORK_ID,
                    -1);
            if (homework_id == -1 ) {
                finishBecasuseNoType();
            }
            if (appType == AppCommon.AppType.qingqing_student) {
                teacherName = getIntent().getStringExtra(
                        BaseParamKeys.PARAM_STRING_TEACHER_NAME);
            }
        }
    }

    protected void findViews() {
        mLvStudent = (AtMostListView) findViewById(R.id.lv_student);
        mTvMessageTitle = (TextView) findViewById(R.id.tv_message_title);
        mTvGradeCourse = (TextView) findViewById(R.id.tv_grade_course);
        mTvMessage = (TextView) findViewById(R.id.tv_message);
        mRvPhoto = (RecyclerView)findViewById(R.id.rv_photo);
        mTvCreateTime = (TextView) findViewById(R.id.tv_create_time);
        mLlTiku = findViewById(R.id.ll_tiku);
        mLlSeeTiku = findViewById(R.id.ll_see_tiku);
        mTvSeeTiku = (TextView) findViewById(R.id.tv_see_tiku);
        mIvSeeTiku = (ImageView)findViewById(R.id.iv_see_tiku);
        mLlMyHomework = findViewById(R.id.ll_my_homework);
        mLlSeeMyHomework = findViewById(R.id.ll_see_my_homework);
        mTvSeeMyHomework = (TextView) findViewById(R.id.tv_see_my_homework);
        mTvSubmit = (TextView) findViewById(R.id.tv_submit);
        mTvUpload = (TextView) findViewById(R.id.tv_upload);
        mLlOnlyTiku = findViewById(R.id.ll_only_tiku_container);
        mIvOnlyTiku = (ImageView) findViewById(R.id.iv_only_tiku);
        mTvOnlyTiku = (TextView)findViewById(R.id.tv_only_tiku);
        mIvOnlyTikuArrow = (ImageView)findViewById(R.id.iv_only_tiku_arrow);
        mLlPrepOffline = findViewById(R.id.ll_prep_offline_container);
        mIvPrepOffline = (ImageView)findViewById(R.id.iv_prep_offline);
        mTvPrepOffline = (TextView)findViewById(R.id.tv_prep_offline);
        ViewStub av = (ViewStub) findViewById(R.id.av_audio_play);
        av.setLayoutResource(getAudioViewLayout());
        mAvAudioPlay = (AudioDownloadView)av.inflate();
        mAvAudioPlay.setVisibility(GONE);
        mIvControl = (ImageView)findViewById(R.id.iv_control);
        mLlContent = findViewById(R.id.ll_content);
        mLlNoStudent = findViewById(R.id.ll_no_student);
        mLlFuwuhao = findViewById(R.id.ll_fuwuhao);
        mIvFuwuhao = (AsyncImageViewV2) findViewById(R.id.iv_fuwuhao);
        mTvFuwuhao = (TextView)findViewById(R.id.tv_fuwuhao);
        mLlMessageTitle = findViewById(R.id.ll_message_title);
    }

    protected void reqDetail() {
        Common.SimpleLongRequest request = new Common.SimpleLongRequest();
        request.data = homework_id;
        newProtoReq(CommonUrl.GET_HOMEWORK_DETAIL.url()).setSendMsg(request)
                .setRspListener(new ProtoListener(
                        ServiceSliceProto.HomeworkDetailResponse.class) {
                    @Override
                    public void onDealResult(Object result) {
                        ServiceSliceProto.HomeworkDetailResponse response = (ServiceSliceProto.HomeworkDetailResponse) result;
                        homeworkType = response.type;
                        gradeCourse = response.gradeCourseInfo.gradeShortName + " "
                                + response.gradeCourseInfo.courseName;
                        setupView(response);
                        setupViewStyles();
                        
                        UserBehaviorLogManager.INSTANCE().savePageLog(
                                StatisticalDataConstants.LOG_PAGE_HOMEWORK_DETAIL,
                                new UserBehaviorLogExtraData.Builder().addExtraData(
                                        StatisticalDataConstants.LOG_EXTRA_STATUS,
                                        homeworkType == ServiceSliceProto.HomeworkType.preview_homework
                                                ? 1 : 2)
                                        .build());
                    }
                }).req();
    }

    protected void setupView(final ServiceSliceProto.HomeworkDetailResponse response) {
        switch (appType) {
            case AppCommon.AppType.qingqing_student:
                if (response.answers.length > 0) {
                    for (ServiceSliceProto.HomeworkAnswerBrief homeworkAnswerBrief:response.answers){
                        if (homeworkAnswerBrief.studentInfo.qingqingUserId.equals(BaseData.qingqingUserId())){
                            answer_id =homeworkAnswerBrief.answerId;
                            answerStatus = homeworkAnswerBrief.homeworkAnswerStatus;
                            switch (answerStatus) {
                                case ServiceSliceProto.HomeworkAnswerStatus.finished_homework_answer_status:
                                    mLlMyHomework.setVisibility(View.VISIBLE);
                                    break;
                                case ServiceSliceProto.HomeworkAnswerStatus.not_answer_homework_answer_status:
                                    mTvSubmit.setVisibility(View.VISIBLE);
                                    break;
                                case ServiceSliceProto.HomeworkAnswerStatus.ignored_homework_answer_status:
                                    mTvUpload.setVisibility(View.VISIBLE);
                                    break;
                                case ServiceSliceProto.HomeworkAnswerStatus.expired_homework_answer_status:
                                    mTvSubmit.setVisibility(VISIBLE);
                                    break;
                            }
                        }
                    }
                }
                mLlFuwuhao.setVisibility(GONE);
                if (DefaultDataCache.INSTANCE().getActiveInfoByKey(DefaultDataCache.KEY_LEARN_CENTER_BOTTOM_BANNER)!=null){
                    mLlFuwuhao.setVisibility(VISIBLE);
                    ActiveInfo activeInfo = DefaultDataCache.INSTANCE().getActiveInfoByKey(DefaultDataCache.KEY_LEARN_CENTER_BOTTOM_BANNER);
                    mTvFuwuhao.setText(activeInfo.getText());
                    mIvFuwuhao.setImageLoadFailListener(new AsyncImageViewV2.AsyncImageLoadFailListener() {
                        @Override
                        public void onLoadingFailed(String imageUri, View view, String failReason) {
                            mLlFuwuhao.setVisibility(GONE);
                        }
                    });
                    mIvFuwuhao.setImageUrl(ImageUrlUtil.transformWebp(activeInfo.getPic()));
                }
                mIvFuwuhao.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showFuwuhaoDialog();
                    }
                });
                if (teacherName==null){
                    teacherName=response.teacherInfo.nick;
                }
                switch (homeworkType) {
                    case ServiceSliceProto.HomeworkType.preview_homework:
                        setTitle(R.string.title_preview_detail_student);
                        mTvSeeMyHomework
                                .setText(getString(R.string.text_see_my_homework_preview));
                        mTvMessageTitle
                                .setText(getString(
                                        R.string.text_message_title_preview_student,
                                        teacherName));
                        mTvPrepOffline.setText(getString(R.string.text_see_preview_prep_offline_student));
                        mTvOnlyTiku.setText(getString(R.string.text_see_preview_tiku_only_student));
                        break;
                    case ServiceSliceProto.HomeworkType.review_homework:
                        setTitle(R.string.title_review_detail_student);
                        mTvSeeMyHomework
                                .setText(getString(R.string.text_see_my_homework_review));
                        mTvMessageTitle.setText(getString(
                                R.string.text_message_title_review_student, teacherName));
                        mTvPrepOffline.setText(getString(R.string.text_see_review_prep_offline_student));
                        mTvOnlyTiku.setText(getString(R.string.text_see_review_tiku_only_student));
                        break;
                }
                mTvSeeTiku.setText(getString(R.string.text_see_tiku_student));
                mTvUpload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        toUpload();
                    }
                });
                mTvSubmit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (answer_id!=-1) {
                            ServiceSliceProto.CreateHomeworkAnswerRequest request = new ServiceSliceProto.CreateHomeworkAnswerRequest();
                            request.answerId = answer_id;
                            request.status = ServiceSliceProto.HomeworkAnswerStatus.ignored_homework_answer_status;
                            newProtoReq(CommonUrl.SUBMIT_HOMEWORK_ANSWER.url())
                                    .setSendMsg(request)
                                    .setLoadingIndication(BaseHomeworkDetailActivity.this,false)
                                    .setRspListener(new ProtoListener(ProtoBufResponse.SimpleDataResponse.class) {
                                        @Override
                                        public void onDealResult(Object result) {
                                            mTvSubmit.setVisibility(GONE);
                                            mTvUpload.setVisibility(View.VISIBLE);
                                            setResult(RESULT_OK);

                                            UserBehaviorLogManager.INSTANCE().saveClickLog(
                                                    StatisticalDataConstants.LOG_PAGE_HOMEWORK_DETAIL,
                                                    StatisticalDataConstants.CLICK_HOMEWORK_DETAIL_FINISH);
                                            showCompleteDialog();
                                        }
                                    }).req();
                        }else{
                            finishBecasuseNoType();
                        }

                    }
                });
                mLlSeeMyHomework
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                seeHomeworkAnswer(answer_id);
                            }
                        });
                mIvSeeTiku.setVisibility(VISIBLE);
                mIvOnlyTikuArrow.setImageResource(R.drawable.ic_arrow_right_student);
                mIvOnlyTiku.setImageResource(R.drawable.icon_student_exam);
                mIvPrepOffline.setImageResource(R.drawable.icon_student_prep_offline);
                mTvOnlyTiku.setTextColor(getResources().getColor(R.color.primary_green));
                mTvPrepOffline.setTextColor(getResources().getColor(R.color.primary_green));
                boolean isOutOfDate = (answerStatus == ServiceSliceProto.HomeworkAnswerStatus.expired_homework_answer_status);
                canAddScore = !isOutOfDate && (answerStatus== ServiceSliceProto.HomeworkAnswerStatus.ignored_homework_answer_status || answerStatus == ServiceSliceProto.HomeworkAnswerStatus.not_answer_homework_answer_status);
                if (isOutOfDate){
                    mTvUpload.setText(R.string.text_is_out_of_date);
                    mTvSubmit.setText(R.string.text_is_out_of_date);
                    mTvSubmit.setEnabled(false);
                    mTvUpload.setEnabled(false);
                }else {
                    if (canAddScore) {
                        switch (homeworkType){
                            case ServiceSliceProto.HomeworkType.preview_homework:
                                ScoreProto.SCOREScoreTypeEntry previewSubmitScore = ScoreManager.getInstance().getScore(ScoreProto.SCOREScoreType.preview_score_type);
                                ScoreProto.SCOREScoreTypeEntry previewUploadScore = ScoreManager.getInstance().getScore(ScoreProto.SCOREScoreType.preview_advanced_score_type);
                                if (previewSubmitScore!=null){
                                    mTvSubmit.setText(getString(R.string.text_has_completed_preview_can_add_score,previewSubmitScore.scoreAmount));
                                }else{
                                    mTvSubmit.setText(R.string.text_has_completed_homework);
                                }
                                if (previewUploadScore!=null){
                                    mTvUpload.setText(getString(R.string.text_upload_homework_can_add_score,previewUploadScore.scoreAmount));
                                }else{
                                    mTvUpload.setText(R.string.text_upload_homework);
                                }
                                break;
                            case ServiceSliceProto.HomeworkType.review_homework:
                                ScoreProto.SCOREScoreTypeEntry reviewSubmitScore = ScoreManager.getInstance().getScore(ScoreProto.SCOREScoreType.homework_score_type);
                                ScoreProto.SCOREScoreTypeEntry reviewUploadScore = ScoreManager.getInstance().getScore(ScoreProto.SCOREScoreType.homework_advanced_score_type);
                                if (reviewSubmitScore!=null){
                                    mTvSubmit.setText(getString(R.string.text_has_completed_homework_can_add_score,reviewSubmitScore.scoreAmount));
                                }else{
                                    mTvSubmit.setText(R.string.text_has_completed_homework);
                                }
                                if (reviewUploadScore!=null){
                                    mTvUpload.setText(getString(R.string.text_upload_homework_can_add_score,reviewUploadScore.scoreAmount));
                                }else{
                                    mTvUpload.setText(R.string.text_upload_homework);
                                }
                                break;
                        }
                    } else {
                        mTvSubmit.setText(R.string.text_has_completed_homework);
                        mTvUpload.setText(R.string.text_upload_homework);
                    }
                }
                break;
            case AppCommon.AppType.qingqing_teacher:
                if (response.answers.length > 0) {
                    answerList.clear();
                    for (ServiceSliceProto.HomeworkAnswerBrief answerBrief:response.answers) {
                        if (answerBrief.homeworkAnswerStatus== ServiceSliceProto.HomeworkAnswerStatus.ignored_homework_answer_status
                                || answerBrief.homeworkAnswerStatus == ServiceSliceProto.HomeworkAnswerStatus.finished_homework_answer_status){
                            answerList.add(answerBrief);
                        }
                    }
                    if (answerList.size()>0){
                        mLvStudent.setVisibility(View.VISIBLE);
                        mLlNoStudent.setVisibility(GONE);
                        mLvStudent
                                .setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view,
                                                            int position, long id) {
                                        if (answerList.get(position).homeworkAnswerStatus == ServiceSliceProto.HomeworkAnswerStatus.finished_homework_answer_status) {
                                            seeHomeworkAnswer(answerList.get(position).answerId);
                                        }
                                    }
                                });
                        mLvStudent.setAdapter(new StudentListAdapter(this, answerList));
                    }else{
                        mLvStudent.setVisibility(GONE);
                        mLlNoStudent.setVisibility(VISIBLE);
                    }
                }
                mLlFuwuhao.setVisibility(GONE);
                switch (homeworkType) {
                    case ServiceSliceProto.HomeworkType.preview_homework:
                        setTitle(R.string.title_preview_detail_teacher);
                        mTvMessageTitle
                                .setText(getString(R.string.text_message_title_review_preview_teacher));
                        mTvPrepOffline.setText(getString(R.string.text_see_preview_prep_offline_teacher));
                        mTvOnlyTiku.setText(getString(R.string.text_see_tiku_teacher_preview));
                        mTvSeeTiku.setText(getString(R.string.text_see_tiku_teacher_preview));
                        break;
                    case ServiceSliceProto.HomeworkType.review_homework:
                        setTitle(R.string.title_review_detail_teacher);
                        mTvMessageTitle
                                .setText(getString(R.string.text_message_title_review_preview_teacher));
                        mTvPrepOffline.setText(getString(R.string.text_see_review_prep_offline_teacher));
                        mTvOnlyTiku.setText(getString(R.string.text_see_tiku_teacher_review));
                        mTvSeeTiku.setText(getString(R.string.text_see_tiku_teacher_review));
                        break;
                }
                mIvSeeTiku.setVisibility(GONE);
                mIvOnlyTikuArrow.setImageResource(R.drawable.ic_arrow_right_teacher);
                mIvOnlyTikuArrow.setVisibility(GONE);
                mIvOnlyTiku.setImageResource(R.drawable.icon_teacher_exam);
                mIvPrepOffline.setImageResource(R.drawable.icon_teacher_prep_offline);
                mTvOnlyTiku.setTextColor(getResources().getColor(R.color.primary_blue));
                mTvPrepOffline.setTextColor(getResources().getColor(R.color.primary_blue));
                mIvControl.setVisibility(VISIBLE);
                mLlContent.setVisibility(GONE);
                mLlMessageTitle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (Math.abs(mIvControl.getRotation())<1){
                            mIvControl.setRotation(180);
                            mLlContent.setVisibility(VISIBLE);
                        }else{
                            mIvControl.setRotation(0);
                            mLlContent.setVisibility(GONE );
                        }
                    }
                });
                break;
        }
        // 通用的
        switch (homeworkType) {
            case ServiceSliceProto.HomeworkType.preview_homework:
                mTvGradeCourse.setText(getString(R.string.text_grade_course_preview,
                        gradeCourse));
                break;
            case ServiceSliceProto.HomeworkType.review_homework:
                mTvGradeCourse.setText(getString(R.string.text_grade_course_review,
                        gradeCourse));
                break;
        }
        if (response.hasCreateTime) {
            mTvCreateTime.setText(getString(R.string.text_homework_create_time,
                    DateUtils.ymdhmSdf.format(response.createTime)));
        }
        if (response.status == ServiceSliceProto.HomeworkStatus.offline_finish_homework_status){
            mLlPrepOffline.setVisibility(VISIBLE);
            mTvGradeCourse.setVisibility(GONE);
        }else if(TextUtils.isEmpty(response.content) && response.imgs.length==0 && response.audio==null && response.hasThirdPartQuestion && !TextUtils.isEmpty(response.thirdPartQuestion)){
            mLlOnlyTiku.setVisibility(VISIBLE);
            if (appType == AppCommon.AppType.qingqing_student) {
                mLlOnlyTiku.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        toTiku(response.thirdPartQuestion);
                    }
                });
            }
            mTvGradeCourse.setVisibility(GONE);
        }else{
            if (response.hasThirdPartQuestion && !TextUtils.isEmpty(response.thirdPartQuestion)) {
                mLlTiku.setVisibility(View.VISIBLE);
                if (appType == AppCommon.AppType.qingqing_student){
                    mLlSeeTiku.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            toTiku(response.thirdPartQuestion);
                        }
                    });
                }

            }
            if (response.audio != null) {
                mAvAudioPlay.updateMachine(new AudioDownloadMachine(this,
                        response.audio.encodedMediaId, response.audio.timeLength));
                mAvAudioPlay
                        .setPageId(StatisticalDataConstants.LOG_PAGE_HOMEWORK_DETAIL);
                mAvAudioPlay.setVisibility(VISIBLE);
            }else {
                mAvAudioPlay.setVisibility(GONE);
            }
            if (!TextUtils.isEmpty(response.content)) {
                mTvMessage.setVisibility(View.VISIBLE);
                mTvMessage.setText(response.content);
            }
            if (response.imgs.length > 0) {
                photoList.clear();
                photoList.addAll(Arrays.asList(response.imgs));
                mRvPhoto.setVisibility(VISIBLE);
                mRvPhoto.setLayoutManager(new GridLayoutManager(this,4));
                HomeworkPhotoRecyclerAdapter adapter = new HomeworkPhotoRecyclerAdapter(this,photoList);
                adapter.setOnItemClickLitener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        enterGallery(position);
                    }

                    @Override
                    public void onItemLongClick(View view, int position) {

                    }
                });
                mRvPhoto.setAdapter(adapter);
                mRvPhoto.addItemDecoration(new HomeworkPhotoRecyclerAdapter.MyDecoration(this));
            }
        }
        //以下是针对家长端显示不需要布置状态
        if (appType == AppCommon.AppType.qingqing_student && answerStatus == ServiceSliceProto.HomeworkAnswerStatus.no_need_do_homework_answer_status){
            mTvMessage.setText(mTvGradeCourse.getText());
            mTvMessage.setVisibility(View.VISIBLE);
            mTvGradeCourse.setText(getString(homeworkType == ServiceSliceProto.HomeworkType.review_homework?R.string.text_no_need_homework:R.string.text_no_need_preview));
        }
    }

    private void finishBecasuseNoType() {
        ToastWrapper.show("数据错误");
        this.finish();
    }


    private void enterGallery(int position) {
        if (mGallerydialog == null) {
            mGallerydialog = new Dialog(this);
            FrameLayout decorView = (FrameLayout) mGallerydialog.getWindow()
                    .getDecorView();
            decorView.removeAllViews();
            decorView.setBackgroundResource(R.color.translucence_black);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.dlg_homework_detail_gallery, decorView, false);
            decorView.addView(view);
            WindowManager.LayoutParams lp = mGallerydialog.getWindow().getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            mGallerydialog.getWindow().setAttributes(lp);
            mGallerydialog.getWindow().getDecorView().setPadding(0, 0, 0, 0);
            mGalleryViewPager = (ViewPager) mGallerydialog.findViewById(R.id.viewpager);
            mTvGallery = (TextView) mGallerydialog.findViewById(R.id.gallery_count);

            galleryAdapter = new NoReuseViewPagerAdapter(galleryPageList) {
                @Override
                public ImageView getIndicatorIcon(Context context, ViewGroup parent) {
                    return (ImageView) LayoutInflater.from(context).inflate(
                            R.layout.indicator_icon_guide, parent, false);
                }
            };
            galleryAdapter.setOnPageClickListener(new ViewPagerAdapter.OnPageClickListener() {
                @Override
                public void onPageClick(View pageView, int position, Page page) {
                    if (mGallerydialog.isShowing()){
                        mGallerydialog.dismiss();
                    }
                }
            });
            mGalleryViewPager.setAdapter(galleryAdapter);
            mGalleryViewPager
                    .addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                        @Override
                        public void onPageScrolled(int position, float positionOffset,
                                int positionOffsetPixels) {}

                        @Override
                        public void onPageSelected(int position) {
                            mTvGallery.setText((position + 1) + " / "
                                    + galleryAdapter.getItemCount());
                        }

                        @Override
                        public void onPageScrollStateChanged(int state) {}
                    });
            mGalleryIndicator = (IconPageIndicator) mGallerydialog
                    .findViewById(R.id.indicator);
            mGalleryIndicator.setViewPager(mGalleryViewPager);
            mGallerydialog.findViewById(R.id.back).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mGallerydialog.isShowing()) {
                                mGallerydialog.dismiss();
                            }
                        }
                    });
            mGallerydialog.findViewById(R.id.iv_save_image).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String url = ImageUrlUtil.getOriginImg(photoList.get(mGalleryViewPager.getCurrentItem()).imagePath);
                    ImageUrlUtil.getImageCacheType(url, new ImageUrlUtil.getImageCacheTypeListener() {
                        @Override
                        public void inCache(int type) {
                            switch (type){
                                case ImageUrlUtil.FRESCO_CACHE_TYPE_DISK:
                                    String path = ImageUrlUtil.saveImageFileFromDiskCache(url);
                                    if (path!=null){
                                        ToastWrapper.show("保存路径为"+path);
                                    }else{
                                        ToastWrapper.show("保存失败");
                                    }
                                    break;
                                case ImageUrlUtil.FRESCO_CACHE_TYPE_MEMORY:
                                    ImageUrlUtil.saveImageFileFromMemoryCache(url, BaseHomeworkDetailActivity.this, new ImageUrlUtil.saveImageFileFromMemoryCacheListener() {
                                        @Override
                                        public void onSuccess(String path) {
                                            if (path!=null){
                                                ToastWrapper.show("保存路径为"+path);
                                            }else{
                                                ToastWrapper.show("保存失败");
                                            }
                                        }

                                        @Override
                                        public void onFail() {
                                            ToastWrapper.show("保存失败");
                                        }
                                    });
                                    break;
                            }
                        }

                        @Override
                        public void notInCache() {

                        }
                    });
                }
            });

        }
        mGalleryViewPager.removeAllViewsInLayout();
        galleryPageList.clear();
        for (int i = 0; i < photoList.size(); i++) {
            galleryPageList.add(new HomeworkGalleryPage(photoList.get(i)));
        }
        galleryAdapter.notifyDataSetChanged();
        mGalleryIndicator.notifyDataSetChanged();
        mTvGallery.setText(position + 1 + " / " + galleryPageList.size());
        mGalleryViewPager.setCurrentItem(position);
        mGallerydialog.show();
    }

    private class StudentListAdapter extends
            BaseAdapter<ServiceSliceProto.HomeworkAnswerBrief> {

        public StudentListAdapter(Context context,
                List<ServiceSliceProto.HomeworkAnswerBrief> list) {
            super(context, list);
        }

        @Override
        public View createView(Context context, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(
                    R.layout.item_homework_detail_student, parent, false);
        }

        @Override
        public ViewHolder<ServiceSliceProto.HomeworkAnswerBrief> createViewHolder() {
            return new StudentListViewHolder();
        }
    }

    private class StudentListViewHolder extends
            BaseAdapter.ViewHolder<ServiceSliceProto.HomeworkAnswerBrief> {
        private AsyncImageViewV2 head;
        private TextView name;
        private TextView completeTime;
        private View attachment;
        private View seeNow;

        @Override
        public void init(Context context, View convertView) {
            head = (AsyncImageViewV2) convertView.findViewById(R.id.iv_head);
            name = (TextView) convertView.findViewById(R.id.tv_student_name);
            completeTime = (TextView) convertView.findViewById(R.id.tv_complete_time);
            attachment = convertView.findViewById(R.id.iv_attachment);
            seeNow = convertView.findViewById(R.id.tv_see_now);
        }

        @Override
        public void update(Context context, ServiceSliceProto.HomeworkAnswerBrief data) {
            head.setImageUrl(ImageUrlUtil.getHeadImg(data.studentInfo.newHeadImage), R.drawable.user_pic_boy);
            name.setText(data.studentInfo.nick);
            if (data.hasAnswerTime) {
                completeTime.setVisibility(View.VISIBLE);
                completeTime.setText(getString(R.string.text_answer_complete_time,
                        DateUtils.mdsdf.format(data.answerTime)));
            }
            else {
                completeTime.setVisibility(GONE);
            }
            if (data.homeworkAnswerStatus != ServiceSliceProto.HomeworkAnswerStatus.finished_homework_answer_status) {
                attachment.setVisibility(GONE);
                seeNow.setVisibility(GONE);
            }
            else {
                attachment.setVisibility(View.VISIBLE);
                seeNow.setVisibility(View.VISIBLE);
            }
        }
    }

    //家长端点击了上传作业
    protected abstract void toUpload();

    // 查看作业答案
    protected void seeHomeworkAnswer(long answerId){
        try{
            Intent intent = new Intent(this,getAnswerDetailClass());
            intent.putExtra(BaseParamKeys.PARAM_LONG_HOMEWORK_ANSWER_ID,answerId);
            if (appType == AppCommon.AppType.qingqing_student){
                intent.putExtra(BaseParamKeys.PARAM_BOOLEAN_CAN_ADD_SCORE,canAddScore);
            }
            startActivity(intent);

            if (BaseData.getClientType() == AppCommon.AppType.qingqing_student) {
                UserBehaviorLogManager.INSTANCE().saveClickLog(
                        StatisticalDataConstants.LOG_PAGE_HOMEWORK_DETAIL,
                        StatisticalDataConstants.CLICK_HOMEWORK_DETAIL_SHOW);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AudioPlayerController.stopCurrentPlayer();
    }

    // 家长端做完了弹窗
    protected abstract void showCompleteDialog();

    protected void toTiku(String url){
        UserBehaviorLogManager.INSTANCE().saveClickLog(
                StatisticalDataConstants.LOG_PAGE_HOMEWORK_DETAIL,
                StatisticalDataConstants.CLICK_HOMEWORK_DETAIL_QUESTION_BANK);
        Intent intent = new Intent(this,HtmlActivity.class);
        intent.putExtra(BaseParamKeys.PARAM_STRING_URL, url);
        if (mCityId != -1) {
            intent.putExtra(BaseParamKeys.PARAM_INT_CITY_ID, mCityId);
        }
        startActivity(intent);
    }

    // 根据客户端类型设置style
    protected abstract void setupViewStyles();

    protected abstract int getAudioViewLayout();

    protected abstract Class getAnswerDetailClass();

    protected void setCityId(int cityId) {
        mCityId = cityId;
    }

    protected abstract void showFuwuhaoDialog();
}

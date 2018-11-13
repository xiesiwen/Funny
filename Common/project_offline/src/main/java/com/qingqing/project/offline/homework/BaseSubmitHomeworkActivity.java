package com.qingqing.project.offline.homework;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.PermissionChecker;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.qingqing.api.image.proto.v1.MediaResource;
import com.qingqing.api.proto.scoresvc.ScoreProto;
import com.qingqing.api.proto.v1.ImageProto;
import com.qingqing.api.proto.v1.OrderCommonEnum;
import com.qingqing.api.proto.v1.ProtoBufResponse;
import com.qingqing.api.proto.v1.app.AppCommon;
import com.qingqing.api.proto.v1.serviceslice.ServiceSliceProto;
import com.qingqing.api.proto.v1.util.Common;
import com.qingqing.base.BaseApplication;
import com.qingqing.base.SelectPictureManager;
import com.qingqing.base.activity.HtmlActivity;
import com.qingqing.base.bean.Grade;
import com.qingqing.base.config.CommonUrl;
import com.qingqing.base.config.LogicConfig;
import com.qingqing.base.constant.BaseParamKeys;
import com.qingqing.base.core.CountDownCenter;
import com.qingqing.base.core.ScoreManager;
import com.qingqing.base.core.UploadManager;
import com.qingqing.base.data.DefaultDataCache;
import com.qingqing.base.dialog.AudioRecordDialog;
import com.qingqing.base.dialog.CompDefaultDialogBuilder;
import com.qingqing.base.dialog.CompatDialog;
import com.qingqing.base.dns.DNSManager;
import com.qingqing.base.http.error.HttpError;
import com.qingqing.base.http.req.ProtoListener;
import com.qingqing.base.log.Logger;
import com.qingqing.base.media.AudioPlayerController;
import com.qingqing.base.media.audiomachine.AudioDownloadMachine;
import com.qingqing.base.media.audiomachine.AudioMachine;
import com.qingqing.base.media.audiorecorder.AudioRecordListener;
import com.qingqing.base.media.audiorecorder.AudioRecorderFactory;
import com.qingqing.base.time.NetworkTime;
import com.qingqing.base.utils.AppUtil;
import com.qingqing.base.utils.DateUtils;
import com.qingqing.base.utils.DisplayUtil;
import com.qingqing.base.utils.ImageUrlUtil;
import com.qingqing.base.view.AsyncImageViewV2;
import com.qingqing.base.view.BaseAdapter;
import com.qingqing.base.view.LimitedTextWatcher;
import com.qingqing.base.view.ToastWrapper;
import com.qingqing.base.view.audio.AudioDownloadView;
import com.qingqing.base.view.audio.AudioView;
import com.qingqing.base.view.editor.LimitEditText;
import com.qingqing.base.view.listview.HorizontalListView;
import com.qingqing.base.view.pager.HomeworkGalleryPage;
import com.qingqing.base.view.pager.IconPageIndicator;
import com.qingqing.base.view.pager.NoReuseViewPagerAdapter;
import com.qingqing.base.view.pager.Page;
import com.qingqing.base.view.pager.ViewPagerAdapter;
import com.qingqing.project.offline.R;
import com.qingqing.project.offline.order.OrderCourseUtil;
import com.qingqing.qingqingbase.ui.BaseActionBarActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by tangyutian on 2016/9/18.
 * 提交作业，包括老师提交作业，家长提交答案
 */

public abstract class BaseSubmitHomeworkActivity extends BaseActionBarActivity implements View.OnClickListener,SelectPictureManager.SelectPicListener {
    //需要头像、名字、gradeId、courseId、order_course_id
    //还有几小时几天什么的……
    protected int appType = -1;
    protected ArrayList<String> headImages = new ArrayList<String>();
    protected String studentName = null;
    protected String grade_course = null;
    protected long homeworkId = -1;
    protected int homeworkType = -1;
    protected long answer_id = -1;
    protected boolean isJunior = false;
    protected String third_party_url = null;
    protected File audioFile =null;
    protected int audioLength =-1;
    protected String audioId = null;
    protected int groupType =-1;
    private String COUNT_DOWN_TAG = "SUBMIT_COUNT_DOWN";
    private int leftSeconds;
    private int leftDays;
    private int leftHours;
    private int leftMinutes;
    private boolean isOutDated = false;
    private double award;
    private boolean disableSubmit = false;

    private int REQ_CODE_CAPTURE= 1024;
    protected String TAG = BaseSubmitHomeworkActivity.class.getName();

    private SelectPictureManager mSelectPictureManager;
    private ArrayList<Object> photoList = new ArrayList<Object>();
    private photoListAdapter mPhotoListAdapter;

    private Dialog mGalleryDialog;
    private ViewPager mGalleryViewPager;
    private TextView mGalleryTextView;
    private List<Page> galleryPageList = new ArrayList<Page>();
    private NoReuseViewPagerAdapter mGalleryAdapter;
    private IconPageIndicator mGalleryIndicator;
    private HashMap<Integer,ImageProto.ImageItem> uploadPicPathMap = new HashMap<Integer, ImageProto.ImageItem>();
    private boolean needUploadPic = false;
    private boolean needUploadAudio = false;
    private String address;
    private boolean firstClick = true;
    private String contentPackTitle;
    private long courseTime;
    protected boolean canAddScore = false;


    private AsyncImageViewV2 mIvHead;
    private TextView mTvGrade1;
    private TextView mTvGrade2;
    private TextView mTvName;
    private TextView mTvAddress1;
    private TextView mTvAddress2;
    private TextView mTvCourseTime;
    private TextView mTvCreateTime;
    private View mLlUploadAudio;
    private View mLlPlayAudio;
    private LimitEditText mEtPreparation;
    private TextView mTvWordsCount;
    private HorizontalListView mLvPhoto;
    private View mLlTikuContainer;
    private View mLlSeeTiku;
    private View mLlAddTiku;
    protected TextView mTvSubmit;
    private ImageView mIvAudioIcon;
    private View mRlTimeContainer;
    private View mRlStudentInfo;
    private View mLlBottomBar;
    private AudioView mAvAudioPlay;
    private View mLlSingleStudentInfo;
    private View mLlGroupStudentInfo;
    private TextView mTvGroupType;
    private HorizontalListView mLvStudent;
    private TextView mTvDayCount;
    private TextView mTvDay;
    private TextView mTvHourCount;
    private TextView mTvHour;
    private TextView mTvMinuteCount;
    private TextView mTvMinute;
    private TextView mTvSecondCount;
    private TextView mTvSecond;
    private TextView mTvAward;
    private View mLlTime;
    private View mTvGuoqi;
    private View mAvDownloadPlay;
    private TextView mTvAudio;


    private UploadManager.UploadProtoImgListener uploadPicListener = new UploadManager.UploadProtoImgListener() {
        @Override
        public void onUploadImgDone(int tag, long picId, String picPath) {
            ImageProto.ImageItem imageItem = new ImageProto.ImageItem();
            imageItem.imageId = picId;
            imageItem.imagePath = picPath;
            imageItem.hasImageId = true;
            imageItem.hasImagePath=true;
            uploadPicPathMap.put(tag,imageItem);
            checkUpload();
        }

        @Override
        public void onUploadDone(int tag, boolean ret) {
            if (!ret){
                ToastWrapper.show("上传失败");
                disableSubmit=false;
                checkSubmit();
                dismissProgressDialogDialog();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        canAddScore = getIntent().getBooleanExtra(BaseParamKeys.PARAM_BOOLEAN_CAN_ADD_SCORE,false);
        setContentView(R.layout.activity_submit_homework);
        findViews();
        getType();
        setupViews();
        getEditParams();
        mSelectPictureManager = new SelectPictureManager(this);
        mSelectPictureManager.setSelectPicListener(this);
    }

    private void findViews(){
        findViewById(R.id.tv_delete_audio).setOnClickListener(this);
        findViewById(R.id.tv_delete_tiku).setOnClickListener(this);
        findViewById(R.id.tv_has_create_offline).setOnClickListener(this);
        findViewById(R.id.tv_no_need_create).setOnClickListener(this);
        findViewById(R.id.iv_tiku_help).setOnClickListener(this);
        mIvHead = (AsyncImageViewV2) findViewById(R.id.iv_head_image);
        mTvGrade1 = (TextView)findViewById(R.id.tv_grade_course_1);
        mTvGrade2 = (TextView)findViewById(R.id.tv_grade_course_2);
        mTvAddress1 = (TextView)findViewById(R.id.tv_address_1);
        mTvAddress2 = (TextView)findViewById(R.id.tv_address_2);
        mTvCourseTime = (TextView)findViewById(R.id.tv_course_time);
        mTvCreateTime = (TextView)findViewById(R.id.tv_create_time);
        mTvName = (TextView)findViewById(R.id.tv_name);
        mLlUploadAudio = findViewById(R.id.ll_upload_audio);
        mLlPlayAudio = findViewById(R.id.ll_play_audio);
        mEtPreparation = (LimitEditText) findViewById(R.id.et_preparation);
        mTvWordsCount = (TextView)findViewById(R.id.tv_words_count);
        mLvPhoto = (HorizontalListView)findViewById(R.id.lv_photo);
        mLlTikuContainer = findViewById(R.id.ll_tiku_container);
        mLlSeeTiku = findViewById(R.id.ll_see_tiku);
        mLlAddTiku = findViewById(R.id.ll_add_tiku);
        mTvSubmit = (TextView)findViewById(R.id.tv_submit);
        mIvAudioIcon = (ImageView)findViewById(R.id.iv_audio_icon);
        mRlTimeContainer = findViewById(R.id.rl_time_container);
        mRlStudentInfo = findViewById(R.id.rl_student_info);
        mLlBottomBar = findViewById(R.id.ll_bottom_bar);
        ViewStub av = (ViewStub) findViewById(R.id.av_audio_play);
        av.setLayoutResource(getAudioViewLayout());
        mAvAudioPlay = (AudioView)av.inflate();
        mAvAudioPlay.setVisibility(View.GONE);
        mLlSingleStudentInfo = findViewById(R.id.ll_single_student_info);
        mLlGroupStudentInfo = findViewById(R.id.ll_group_student_info);
        mTvGroupType = (TextView)findViewById(R.id.tv_group_type);
        mLvStudent = (HorizontalListView)findViewById(R.id.lv_student);
        mTvDayCount = (TextView)findViewById(R.id.tv_day_count);
        mTvDay = (TextView)findViewById(R.id.tv_day);
        mTvMinuteCount = (TextView)findViewById(R.id.tv_minute_count);
        mTvMinute = (TextView)findViewById(R.id.tv_minute);
        mTvSecondCount = (TextView)findViewById(R.id.tv_second_count);
        mTvSecond = (TextView)findViewById(R.id.tv_second);
        mTvHourCount = (TextView)findViewById(R.id.tv_hour_count);
        mTvHour = (TextView)findViewById(R.id.tv_hour);
        mTvAward = (TextView)findViewById(R.id.tv_award);
        mLlTime = findViewById(R.id.ll_time);
        mTvGuoqi = findViewById(R.id.tv_guoqi);
        mTvAudio = (TextView)findViewById(R.id.tv_audio);
    }

    protected void getType(){
        appType = DefaultDataCache.INSTANCE().getAppType();
        if (getIntent()!=null){
            switch (appType){
                case AppCommon.AppType.qingqing_student:
                    answer_id = getIntent().getLongExtra(BaseParamKeys.PARAM_LONG_HOMEWORK_ANSWER_ID,-1);
                    homeworkType = getIntent().getIntExtra(BaseParamKeys.PARAM_INT_HOMEWORK_TYPE, -1);
                    if (answer_id ==-1){
                        finishBecasuseNoType();
                    }
                    setupViewStyle();
                    break;
                case AppCommon.AppType.qingqing_teacher:
                    homeworkId = getIntent().getLongExtra(BaseParamKeys.PARAM_LONG_HOMEWORK_ID,-1);
                    if (homeworkId==-1){
                        finishBecasuseNoType();
                    }else {
                        Common.SimpleLongRequest request = new Common.SimpleLongRequest();
                        request.data = homeworkId;
                        newProtoReq(CommonUrl.GET_HOMEWORK_DETAIL.url())
                                .setSendMsg(request)
                                .setRspListener(new ProtoListener(ServiceSliceProto.HomeworkDetailResponse.class) {
                                    @Override
                                    public void onDealResult(Object result) {
                                        ServiceSliceProto.HomeworkDetailResponse response = (ServiceSliceProto.HomeworkDetailResponse) result;
                                        groupType = response.friendGroupType;
                                        award = response.award;
                                        if (award<0.01){
                                            mTvAward.setText(getString(R.string.text_create_homework_tip_no_award));
                                        }else{
                                            mTvAward.setText(Html.fromHtml(getString(R.string.text_create_homework_tip_has_award, LogicConfig.getFormatDotString(award))));
                                        }
                                        studentName = response.answers[0].studentInfo.nick;
                                        headImages.clear();
                                        for (int i =0;i<response.answers.length;i++) {
                                            headImages.add(response.answers[i].studentInfo.headImage);
                                        }
                                        courseTime = response.orderCourseTime;
                                        homeworkType = response.type;
                                        address = response.address;
                                        if (response.courseContentPackageBrief==null){
                                            contentPackTitle = null;
                                        }else{
                                            contentPackTitle = response.courseContentPackageBrief.name;
                                        }
                                        grade_course = response.gradeCourseInfo.gradeShortName+" "+response.gradeCourseInfo.courseName;
                                        int gradeId = response.gradeCourseInfo.gradeId;
                                        Grade grade = null;
                                        for (Grade grade1 : DefaultDataCache.INSTANCE().getGradeList()) {
                                            if (grade1.getId() == gradeId) {
                                                grade = grade1;
                                                break;
                                            }
                                        }
                                        if (grade == null) {
                                            finishBecasuseNoType();
                                        } else {
                                            try {
                                                if (getCityName().equals("上海")){
                                                    isJunior = (grade.getGroupName().equals("初中")
                                                            || grade.getGroupName().equals("高中")
                                                    || grade.getName().equals("小学六年级"));
                                                }else{
                                                    isJunior = (grade.getGroupName().equals("初中"))
                                                            || (grade.getGroupName().equals("高中"));
                                                }
                                            }catch (Exception e){
                                                e.printStackTrace();
                                                isJunior = (grade.getGroupName().equals("初中"))
                                                        || (grade.getGroupName().equals("高中"));
                                            }

                                        }
                                        setupViewStyle();
                                        startCountDown(response);
                                    }
                                }).req();
                    }
                    break;
            }
        }else{
            finishBecasuseNoType();
        }
    }

    protected void getEditParams(){
        if (appType == AppCommon.AppType.qingqing_student) {
            if (getIntent().getBooleanExtra(BaseParamKeys.PARAM_BOOLEAN_HOMEWORK_ANSWER_IS_EDIT, false)) {
                String content = getIntent().getStringExtra(BaseParamKeys.PARAM_STRING_ANSWER_CONTENT);
                ArrayList<ImageProto.ImageItem> imgs = getIntent().getParcelableArrayListExtra(BaseParamKeys.PARAM_IMAGEITEM_ARRAY_ANSWER_IMAGE);
                MediaResource.EncodedAudioItem audio = getIntent().getParcelableExtra(BaseParamKeys.PARAM_AUDIO_ANSWER_AUDIO);
                if (content != null) {
                    mEtPreparation.setText(content);
                }
                if (imgs != null) {
                    photoList.addAll(photoList.size()-1,imgs);
                    mPhotoListAdapter.notifyDataSetChanged();
                }
                if (audio != null) {
                    ViewStub viewStub = (ViewStub) findViewById(R.id.av_download_play);
                    viewStub.setLayoutResource(getAudioDownloadLayout());
                    mAvDownloadPlay = viewStub.inflate();
                    mAvAudioPlay.setVisibility(View.GONE);
                    mAvDownloadPlay.setVisibility(View.VISIBLE);
                    mLlUploadAudio.setVisibility(View.GONE);
                    mLlPlayAudio.setVisibility(View.VISIBLE);
                    audioId = audio.encodedMediaId;
                    audioLength = audio.timeLength;
                    ((AudioDownloadView) mAvDownloadPlay).updateMachine(new AudioDownloadMachine(this, audio.encodedMediaId, audio.timeLength));
                }
                checkSubmit();
            }
        }
    }


    private void finishBecasuseNoType() {
        ToastWrapper.show("数据错误");
        this.finish();
    }

    protected void setupViewStyle(){
        switch (appType){
            case AppCommon.AppType.qingqing_student:
                setTitle(R.string.title_upload_homework);
                mRlStudentInfo.setVisibility(View.GONE);
                mTvSubmit.setBackgroundResource(R.drawable.student_button);
                if (canAddScore){
                    switch (homeworkType){
                        case ServiceSliceProto.HomeworkType.preview_homework:
                            ScoreProto.SCOREScoreTypeEntry previewScore = ScoreManager.getInstance().getScore(ScoreProto.SCOREScoreType.preview_advanced_score_type);
                            if (previewScore!=null){
                                mTvSubmit.setText(getString(R.string.text_send_to_teacher_can_add_score,previewScore.scoreAmount));
                            }else{
                                mTvSubmit.setText(getString(R.string.text_send_to_teacher));
                            }
                            break;
                        case ServiceSliceProto.HomeworkType.review_homework:
                            ScoreProto.SCOREScoreTypeEntry reviewScore = ScoreManager.getInstance().getScore(ScoreProto.SCOREScoreType.homework_advanced_score_type);
                            if (reviewScore!=null){
                                mTvSubmit.setText(getString(R.string.text_send_to_teacher_can_add_score,reviewScore.scoreAmount));
                            }else{
                                mTvSubmit.setText(getString(R.string.text_send_to_teacher));
                            }
                            break;
                        default:
                            mTvSubmit.setText(getString(R.string.text_send_to_teacher));
                            break;
                    }
                }else{
                    mTvSubmit.setText(getString(R.string.text_send_to_teacher));
                }
                mIvAudioIcon.setImageResource(R.drawable.icon_sound);
                mLlTikuContainer.setVisibility(View.GONE);
                mRlTimeContainer.setVisibility(View.GONE);
                mEtPreparation.setHint(Html.fromHtml(getString(R.string.hint_preview_review_student)));
                mLlBottomBar.setVisibility(View.GONE);
                mTvAudio.setText(getString(R.string.text_create_audio_preparation_student));
                ViewGroup.LayoutParams layoutParams= mEtPreparation.getLayoutParams();
                layoutParams.height = DisplayUtil.dp2px(250);
                mEtPreparation.setLayoutParams(layoutParams);
                break;
            case AppCommon.AppType.qingqing_teacher:
                mRlTimeContainer.setVisibility(View.VISIBLE);
                mRlStudentInfo.setVisibility(View.VISIBLE);
                mLlBottomBar.setVisibility(View.VISIBLE);
                mTvSubmit.setBackgroundResource(R.drawable.teacher_button);
                mTvSubmit.setText(getString(R.string.text_send_to_parent));
                mIvAudioIcon.setImageResource(R.drawable.icon_preparation_audio);
                if (TextUtils.isEmpty(contentPackTitle)) {
                    mTvGrade1.setText(grade_course);
                    mTvGrade2.setText(grade_course);
                }else{
                    mTvGrade1.setText(contentPackTitle);
                    mTvGrade2.setText(contentPackTitle);
                }
                mTvCourseTime.setText(DateUtils.mdsdf.format(new Date(courseTime)));
                mTvCreateTime.setText(DateUtils.describeTimeWithDayUnit(this, courseTime));
                mTvGroupType.setText(OrderCourseUtil.getGroup(BaseSubmitHomeworkActivity.this,groupType));
                mTvAddress1.setText(getString(R.string.text_give_course_address,address));
                mTvAddress2.setText(getString(R.string.text_give_course_address,address));
                ViewGroup.LayoutParams params= mEtPreparation.getLayoutParams();
                if (isJunior && DefaultDataCache.INSTANCE().getIsSupportThirdPartHomework()){
                    mLlTikuContainer.setVisibility(View.VISIBLE);
                    params.height = DisplayUtil.dp2px(100);
                    mEtPreparation.setLayoutParams(params);
                }else{
                    mLlTikuContainer.setVisibility(View.GONE);
                    params.height = DisplayUtil.dp2px(150);
                    mEtPreparation.setLayoutParams(params);

                }
                switch (homeworkType) {
                    case ServiceSliceProto.HomeworkType.preview_homework:
                        setTitle(R.string.title_create_preview);
                        mEtPreparation.setHint(Html.fromHtml(getString(R.string.hint_preview_teacher)));
                        mTvAudio.setText(getString(R.string.text_create_audio_preview_teacher));
                        break;
                    case ServiceSliceProto.HomeworkType.review_homework:
                        setTitle(R.string.title_create_review);
                        mEtPreparation.setHint(Html.fromHtml(getString(R.string.hint_review_teacher)));
                        mTvAudio.setText(getString(R.string.text_create_audio_review_teacher));
                        break;
                }
                mEtPreparation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (firstClick){
                            mEtPreparation.setHint("");
                            firstClick = false;
                        }
                    }
                });
                if (groupType != OrderCommonEnum.TeacherCoursePriceType.normal_course_price_type) {
                    mLlGroupStudentInfo.setVisibility(View.VISIBLE);
                    mLvStudent.setAdapter(new studentHeadListAdapter(this, headImages));
                }
                else {
                    mLlSingleStudentInfo.setVisibility(View.VISIBLE);
                    mIvHead.setImageUrl(ImageUrlUtil.getHeadImg(headImages.get(0)),R.drawable.user_pic_boy);
                    mTvName.setText(studentName);
                }
                break;
        }

    }
    protected void setupViews(){
        mEtPreparation.addTextChangedListener(new LimitedTextWatcher(1000, LimitedTextWatcher.FilterMode.NO_EMOJI){
            @Override
            public void afterTextChecked(Editable s) {
                checkSubmit();
                mTvWordsCount.setText(s.length()+" | 1000");
            }
        });
        mLlUploadAudio.setOnClickListener(this);
        photoList.add(Integer.valueOf(R.drawable.icon_preparation_picture));
        mPhotoListAdapter = new photoListAdapter(this,photoList);
        mLvPhoto.setAdapter(mPhotoListAdapter);
        mLvPhoto.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position==photoList.size()-1){
                    tryAddPhoto();
                }else{
//                    ToastWrapper.show("第"+(position)+"个");
                    enterGallery(position);
                }
            }
        });
        mLlAddTiku.setOnClickListener(this);
        mTvSubmit.setOnClickListener(this);
    }

    private void checkSubmit(){
        if (!disableSubmit) {
            switch (appType) {
                case AppCommon.AppType.qingqing_student:
                    if (mEtPreparation.getText().length() >0 || photoList.size() > 1 || audioFile != null || audioId != null) {
                        mTvSubmit.setEnabled(true);
                    } else {
                        mTvSubmit.setEnabled(false);
                    }
                    break;
                case AppCommon.AppType.qingqing_teacher:
                    if (isOutDated) {
                        mTvSubmit.setEnabled(false);
                    } else {
                        if (isJunior) {
                            if (mEtPreparation.getText().length() >0 || photoList.size() > 1 || !TextUtils.isEmpty(third_party_url) || audioFile != null) {
                                mTvSubmit.setEnabled(true);
                            } else {
                                mTvSubmit.setEnabled(false);
                            }
                        } else {
                            if (mEtPreparation.getText().length() >0 || photoList.size() > 1 || audioFile != null) {
                                mTvSubmit.setEnabled(true);
                            } else {
                                mTvSubmit.setEnabled(false);
                            }
                        }
                    }
                    break;
            }
        }else{
            mTvSubmit.setEnabled(false);
        }

    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.tv_delete_audio) {
            showAlert(Html.fromHtml(getString(R.string.text_alert_delete_audio)),
                    "确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                audioFile.delete();
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            audioFile = null;
                            audioLength = -1;
                            audioId = null;
                            mLlUploadAudio.setVisibility(View.VISIBLE);
                            mLlPlayAudio.setVisibility(View.GONE);
                            if (mAvDownloadPlay !=null && mAvDownloadPlay.getVisibility() ==View.VISIBLE){
                                mAvDownloadPlay.setVisibility(View.GONE);
                                mAvAudioPlay.setVisibility(View.VISIBLE);
                            }
                            checkSubmit();
                        }
                    }, "取消", null);
        } else if (i == R.id.ll_add_tiku) {
            int result =-1;
            try {
                result = PermissionChecker.checkSelfPermission(getApplication(), "android.permission.CAMERA");
            }finally {
                if (result!=PermissionChecker.PERMISSION_GRANTED){
                    new CompDefaultDialogBuilder(this).setCancelable(true)
                            .setContent("无法扫描二维码添加试卷！\n请在系统的“设置>应用管理”中，找到“轻轻老师”，为其开通相机权限。")
                            .setPositiveButton("去设置", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    AppUtil.showAppDetail();
                                }
                            }).show();

                }else {
                    new IntentIntegrator(this).setCaptureActivity(QRCodeCaptureActivity.class).initiateScan();
//                    startActivityForResult(new Intent(this, CaptureActivity.class), REQ_CODE_CAPTURE);
                }
            }

        } else if (i == R.id.tv_delete_tiku) {
            third_party_url =null;
            mLlAddTiku.setVisibility(View.VISIBLE);
            mLlSeeTiku.setVisibility(View.GONE);
            checkSubmit();
        } else if (i == R.id.tv_has_create_offline) {
            showAlert(Html.fromHtml(getString(homeworkType == ServiceSliceProto.HomeworkType.preview_homework?R.string.text_alert_offline_preview:R.string.text_alert_offline_homework)),
                    "确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            submit(ServiceSliceProto.HomeworkStatus.offline_finish_homework_status);
                        }
                    }, "取消", null);

        } else if (i == R.id.tv_no_need_create) {
            showAlert(Html.fromHtml(getString(homeworkType == ServiceSliceProto.HomeworkType.preview_homework?R.string.text_alert_ignore_preview:R.string.text_alert_ignore_homework)),
                    "确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            submit(ServiceSliceProto.HomeworkStatus.no_need_assign_homework_status);
                        }
                    }, "取消", null);
        } else if (i == R.id.ll_upload_audio) {
            showAudioRecordDialog();
        } else if (i == R.id.tv_submit) {
            switch (appType){
                case AppCommon.AppType.qingqing_student:
                    submit(ServiceSliceProto.HomeworkStatus.finished_homework_status);
                    break;
                case AppCommon.AppType.qingqing_teacher:
                    switch (homeworkType) {
                        case ServiceSliceProto.HomeworkType.preview_homework:
                            if (photoList.size()<=1 && audioId ==null && mEtPreparation.getText().length()<=15){
                                showAlert(getString(R.string.text_dlg_homework_too_short_warning), getString(R.string.base_sure_to_send), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        submit(ServiceSliceProto.HomeworkStatus.finished_homework_status);
                                    }
                                },getString(R.string.base_think_about_it),null);
                            }else{
                                submit(ServiceSliceProto.HomeworkStatus.finished_homework_status);
                            }
                            break;
                        case ServiceSliceProto.HomeworkType.review_homework:
                            if (photoList.size()<=1 && audioId ==null && mEtPreparation.getText().length()<=15){
                                showAlert(getString(R.string.text_dlg_homework_too_short_warning), getString(R.string.base_sure_to_send), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        submit(ServiceSliceProto.HomeworkStatus.finished_homework_status);
                                    }
                                },getString(R.string.base_think_about_it),null);
                            }else{
                                submit(ServiceSliceProto.HomeworkStatus.finished_homework_status);
                            }
                            break;
                    }

                    break;
            }
        } else if (i == R.id.back) {
            if (mGalleryDialog.isShowing()) {
                mGalleryDialog.dismiss();
            }

        } else if (i == R.id.delete) {
            showAlert(getString(R.string.text_alert_confirm_delete),
                    "确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int current = mGalleryViewPager.getCurrentItem();
                            photoList.remove(current);
                            mPhotoListAdapter.notifyDataSetChanged();
                            galleryPageList.remove(current);
                            mGalleryAdapter.notifyDataSetChanged();
                            mGalleryIndicator.notifyDataSetChanged();
                            if (galleryPageList.size() == 0 && mGalleryDialog.isShowing()) {
                                mGalleryDialog.dismiss();
                            } else {
                                mGalleryTextView.setText((mGalleryViewPager.getCurrentItem() + 1) + " / " + mGalleryAdapter.getItemCount());
                            }
                        }
                    }, "取消", null);

        }else if (i == R.id.iv_tiku_help){
            Intent intent = new Intent(this, HtmlActivity.class);
            if (DNSManager.INSTANCE().isIntranet()){
                intent.putExtra(BaseParamKeys.PARAM_STRING_URL,CommonUrl.H5_TIKU_TEST.url().url());
            }else{
                intent.putExtra(BaseParamKeys.PARAM_STRING_URL,CommonUrl.H5_TIKU.url().url());
            }
            startActivity(intent);
        }
    }

    protected  void showAudioRecordDialog() {
        AudioRecordDialog.Builder builder = new AudioRecordDialog.Builder(this,
                getAudioDialogTheme());
        builder.setAudioRecordListener(new AudioRecordListener() {
            @Override
            public void onAmplitudeChanged(int amplitude) {
            }

            @Override
            public void onRecording(int recordedTime) {
                Logger.i(TAG, "onRecording : " + recordedTime);
            }

            @Override
            public void onRecordCompleted(File audioFile, int recordedTime) {
                Logger.i(TAG, "onRecordCompleted : audioFilePath = " + audioFile.getPath()
                        + ", recordedTime = " + recordedTime);
                BaseSubmitHomeworkActivity.this.audioFile = audioFile;
                BaseSubmitHomeworkActivity.this.audioLength = recordedTime;
                mLlUploadAudio.setVisibility(View.GONE);
                mLlPlayAudio.setVisibility(View.VISIBLE);
                mAvAudioPlay.setVisibility(View.VISIBLE);
                mAvAudioPlay
                        .updateMachine(new AudioMachine(BaseSubmitHomeworkActivity.this,
                                BaseSubmitHomeworkActivity.this.audioFile,
                                BaseSubmitHomeworkActivity.this.audioLength));
                checkSubmit();
                // getReadyToUploadAudio(audioFile, recordedTime);
            }
        });
        builder.setCountdownTotalSecs(LogicConfig.AUDIO_RECORD_COUNTDOWN_SECS);
        builder.setAudioRecorder(AudioRecorderFactory.getDefault(this));
        builder.setWindowFullScreen(true).setWindowGravity(Gravity.BOTTOM);
        builder.setCustomView(getAudioDialogLayout()).show();
    }

    private void tryAddPhoto(){
        if (photoList.size()<19){
            mSelectPictureManager.maxSize(19 - photoList.size()).start();
        }else{
            ToastWrapper.show("最多只能上传十八张照片");
        }
    }

    private void startCountDown(ServiceSliceProto.HomeworkDetailResponse detailItem) {
        CountDownCenter.INSTANCE().cancelTask(COUNT_DOWN_TAG);
        long currentTime = NetworkTime.currentTimeMillis();
        if (detailItem.effectTime <= currentTime) {
            leftSeconds = 0;
            calc();
        }
        else {
            leftSeconds = (int) ((detailItem.effectTime - currentTime) / 1000);
            CountDownCenter.INSTANCE().addTask(COUNT_DOWN_TAG, leftSeconds,
                    new CountDownCenter.CountDownListener() {
                        @Override
                        public void onCountDown(String tag, int leftCount) {
                            leftSeconds = leftCount;
                            calc();
                        }
                    });
        }
    }

    public void stopCountDown() {
        CountDownCenter.INSTANCE().cancelTask(COUNT_DOWN_TAG);
    }

    private void calc() {
        if (leftSeconds > 0) {
            leftDays = leftSeconds / 86400;
            leftHours = (leftSeconds - (leftDays * 86400)) / 3600;
            leftMinutes = (leftSeconds / 60) % 60;
        }
        else {
            leftDays = leftHours = leftMinutes = leftSeconds = 0;
        }
        if (leftSeconds==0){
            mLlTime.setVisibility(View.GONE);
            mTvGuoqi.setVisibility(View.VISIBLE);
            isOutDated = true;
            checkSubmit();
        }else if(leftSeconds>=3600){
            mTvDayCount.setVisibility(View.VISIBLE);
            mTvDay.setVisibility(View.VISIBLE);
            mTvHourCount.setVisibility(View.VISIBLE);
            mTvHour.setVisibility(View.VISIBLE);
            mTvMinuteCount.setVisibility(View.GONE);
            mTvMinute.setVisibility(View.GONE);
            mTvSecondCount.setVisibility(View.GONE);
            mTvSecond.setVisibility(View.GONE);
            mTvDayCount.setText(String.format("%02d", leftDays));
            mTvHourCount.setText(String.format("%02d", leftHours));
        }else{
            mTvDayCount.setVisibility(View.GONE);
            mTvDay.setVisibility(View.GONE);
            mTvHourCount.setVisibility(View.GONE);
            mTvHour.setVisibility(View.GONE);
            mTvMinuteCount.setVisibility(View.VISIBLE);
            mTvMinute.setVisibility(View.VISIBLE);
            mTvSecondCount.setVisibility(View.VISIBLE);
            mTvSecond.setVisibility(View.VISIBLE);
            mTvMinuteCount.setText(String.format("%02d", leftMinutes));
            mTvSecondCount.setText(String.format("%02d", leftSeconds-leftMinutes*60));
        }
    }

    private void submit(int status){
        switch (status){
            case ServiceSliceProto.HomeworkStatus.finished_homework_status:
//                DialogUtil.showAlert(this,"wait");
                needUploadAudio = false;
                needUploadPic = false;
                showProgressDialogDialog(false,"上传中");
                disableSubmit=true;
                checkSubmit();
                if (photoList.size()>1) {
                    needUploadPic = true;
                }
                if (audioFile!=null){
                    needUploadAudio = true;
                }else if (audioId!=null){
                    needUploadAudio = true;
                }
                if (needUploadPic){
                    uploadPicPathMap.clear();
                    for (int i = 0; i < photoList.size() - 1; i++) {
                        Object object = photoList.get(i);
                        if (object instanceof File){
                            UploadManager.INSTANCE().uploadImgV2(ImageProto.ImageUploadType.homework_upload_type, i, (File) photoList.get(i), uploadPicListener);
                        }else if (object instanceof ImageProto.ImageItem){
                            uploadPicPathMap.put(i,(ImageProto.ImageItem)object);
                        }

                    }
                }
                if (needUploadAudio){
                    if (audioFile!=null){
                        audioId=null;
                        UploadManager.INSTANCE().uploadTencentCloudAudio(
                                ImageProto.ImageUploadType.homework_upload_type, -1,
                                audioLength, audioFile,
                                new UploadManager.UploadTencentCloudFileListener() {
                                    @Override
                                    public void onUploadTencentCloudFileDone(int tag,
                                            String resourcePath) {
                                        audioId = resourcePath;
                                        checkUpload();
                                    }

                                    @Override
                                    public void onUploadDone(int tag, boolean ret) {
                                        if (!ret) {
                                            ToastWrapper.show("上传失败");
                                            disableSubmit = false;
                                            checkSubmit();
                                            dismissProgressDialogDialog();
                                        }
                                    }
                                });
                    }
                }
                checkUpload();
                break;
            case ServiceSliceProto.HomeworkStatus.no_need_assign_homework_status:
            case ServiceSliceProto.HomeworkStatus.offline_finish_homework_status :
                ServiceSliceProto.CreateHomeworkRequest request = new ServiceSliceProto.CreateHomeworkRequest();
                request.status = status;
                request.homeworkId = homeworkId;
                newProtoReq(CommonUrl.CREATE_HOMEWORK.url())
                        .setSendMsg(request)
                        .setRspListener(new ProtoListener(ProtoBufResponse.SimpleDataResponse.class) {
                            @Override
                            public void onDealResult(Object result) {
                                View view = LayoutInflater.from(BaseSubmitHomeworkActivity.this).inflate(R.layout.dlg_teacher_upload_homework_complete, null);
                                TextView tv =  ((TextView) view.findViewById(R.id.text));
                                if (award>0.01) {
                                    tv.setText(getString(R.string.text_dlg_upload_homework_complete_has_award, LogicConfig.getFormatDotString(award)));
                                }else {
                                    tv.setText(getString(R.string.text_dlg_upload_homework_complete_no_award));
                                }
                                Toast create_homework_toast = new Toast(BaseApplication.getCtx());
                                create_homework_toast.setView(view);
                                create_homework_toast.setGravity(Gravity.CENTER,0,0);
                                create_homework_toast.setDuration(Toast.LENGTH_SHORT);
                                create_homework_toast.show();
                                setResult(RESULT_OK);
                                BaseSubmitHomeworkActivity.this.finish();
                            }
                        }).req();
                break;
            default:
                break;
        }
    }

    private void checkUpload() {
        boolean picDone = false;
        boolean audioDone = false;
        if (needUploadPic) {
            if (uploadPicPathMap.size() == photoList.size() - 1) {
                picDone = true;
            }
        } else {
            picDone = true;
        }
        if (needUploadAudio){
            if (audioId!=null){
                audioDone = true;
            }
        }else{
            audioDone=true;
        }
        if (picDone&&audioDone) {
            switch (appType) {
                case AppCommon.AppType.qingqing_teacher:
                    ServiceSliceProto.CreateHomeworkRequest request = new ServiceSliceProto.CreateHomeworkRequest();
                    request.status = ServiceSliceProto.HomeworkStatus.finished_homework_status;
                    request.homeworkId = homeworkId;
                    request.content = mEtPreparation.getText().toString();
                    if (needUploadPic) {
                        ImageProto.ImageItem[] picPath = new ImageProto.ImageItem[uploadPicPathMap.size()];
                        for (int i = 0; i < uploadPicPathMap.size(); i++) {
                            picPath[i] = uploadPicPathMap.get(i);
                        }
                        request.imgs = picPath;
                    }
                    if (needUploadAudio){
                        MediaResource.EncodedAudioItem audioItem = new MediaResource.EncodedAudioItem();
                        audioItem.timeLength = audioLength;
                        audioItem.encodedMediaId = audioId;
                        request.audio =audioItem ;
                    }
                    if (isJunior && !TextUtils.isEmpty(third_party_url)) {
                        request.thirdPartQuestion = third_party_url;
                    }
                    newProtoReq(CommonUrl.CREATE_HOMEWORK.url())
                            .setSendMsg(request)
                            .setRspListener(new ProtoListener(ProtoBufResponse.SimpleDataResponse.class) {
                                @Override
                                public void onDealResult(Object result) {
                                    dismissProgressDialogDialog();
                                    View view = LayoutInflater.from(BaseSubmitHomeworkActivity.this).inflate(R.layout.dlg_teacher_upload_homework_complete, null);
                                    TextView tv =  ((TextView) view.findViewById(R.id.text));
                                    if (award>0.01) {
                                      tv.setText(getString(R.string.text_dlg_upload_homework_complete_has_award, LogicConfig.getFormatDotString(award)));
                                    }else {
                                        tv.setText(getString(R.string.text_dlg_upload_homework_complete_no_award));
                                    }
                                    Toast create_homework_toast = new Toast(BaseApplication.getCtx());
                                    create_homework_toast.setView(view);
                                    create_homework_toast.setGravity(Gravity.CENTER,0,0);
                                    create_homework_toast.setDuration(Toast.LENGTH_SHORT);
                                    create_homework_toast.show();
                                    setResult(RESULT_OK);
                                    BaseSubmitHomeworkActivity.this.finish();
                                }

                                @Override
                                public void onDealError(HttpError error, boolean isParseOK, int errorCode, Object result) {
                                    super.onDealError(error, isParseOK, errorCode, result);
                                    ToastWrapper.show("上传失败");
                                    disableSubmit=false;
                                    checkSubmit();
                                    dismissProgressDialogDialog();
                                }
                            }).req();
                    break;
                case AppCommon.AppType.qingqing_student:
                    ServiceSliceProto.CreateHomeworkAnswerRequest createHomeworkAnswerRequest = new ServiceSliceProto.CreateHomeworkAnswerRequest();
                    createHomeworkAnswerRequest.answerId = answer_id;
                    createHomeworkAnswerRequest.content = mEtPreparation.getText().toString();
                    createHomeworkAnswerRequest.status = ServiceSliceProto.HomeworkAnswerStatus.finished_homework_answer_status;
                    if (needUploadPic) {
                        ImageProto.ImageItem[] pic = new ImageProto.ImageItem[uploadPicPathMap.size()];
                        for (int i = 0; i < uploadPicPathMap.size(); i++) {
                            pic[i] = uploadPicPathMap.get(i);
                        }
                        createHomeworkAnswerRequest.imgs = pic;
                    }
                    if (needUploadAudio){
                        MediaResource.EncodedAudioItem audioItem = new MediaResource.EncodedAudioItem();
                        audioItem.timeLength = audioLength;
                        audioItem.encodedMediaId = audioId;
                        createHomeworkAnswerRequest.audio =audioItem ;
                    }
                    newProtoReq(CommonUrl.SUBMIT_HOMEWORK_ANSWER.url())
                            .setSendMsg(createHomeworkAnswerRequest)
                            .setRspListener(new ProtoListener(ProtoBufResponse.SimpleResponse.class) {
                                @Override
                                public void onDealResult(Object result) {
                                    dismissProgressDialogDialog();
                                    ToastWrapper.show(getString(R.string.text_upload_homework_answer_complete));
                                    setResult(RESULT_OK);
                                    BaseSubmitHomeworkActivity.this.finish();
                                }

                                @Override
                                public void onDealError(HttpError error, boolean isParseOK, int errorCode, Object result) {
                                    super.onDealError(error, isParseOK, errorCode, result);
                                    ToastWrapper.show("上传失败");
                                    disableSubmit=false;
                                    checkSubmit();
                                    dismissProgressDialogDialog();
                                }
                            }).req();
                    break;
            }
        }
    }

    private void enterGallery(int position){
        if (mGalleryDialog ==null) {
            mGalleryDialog = new Dialog(this);
            FrameLayout decorView = (FrameLayout) mGalleryDialog.getWindow().getDecorView();
            decorView.removeAllViews();
            decorView.setBackgroundResource(R.color.translucence_black);
            View view = LayoutInflater.from(this).inflate(R.layout.dlg_homework_gallery,decorView,false);
            decorView.addView(view);
            WindowManager.LayoutParams lp = mGalleryDialog.getWindow().getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            mGalleryDialog.getWindow().setAttributes(lp);
            mGalleryDialog.getWindow().getDecorView().setPadding(0, 0, 0, 0);
            mGalleryViewPager = (ViewPager) mGalleryDialog.findViewById(R.id.viewpager);
            mGalleryTextView = (TextView) mGalleryDialog.findViewById(R.id.gallery_count);

            mGalleryAdapter = new NoReuseViewPagerAdapter(galleryPageList) {
                @Override
                public ImageView getIndicatorIcon(Context context, ViewGroup parent) {
                    return (ImageView) LayoutInflater.from(context).inflate(
                            R.layout.indicator_icon_guide, parent, false);
                }
            };
            mGalleryAdapter.setOnPageClickListener(new ViewPagerAdapter.OnPageClickListener() {
                @Override
                public void onPageClick(View pageView, int position, Page page) {
                    if (mGalleryDialog.isShowing()){
                        mGalleryDialog.dismiss();
                    }
                }
            });
            mGalleryViewPager.setAdapter(mGalleryAdapter);
            mGalleryViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    mGalleryTextView.setText((position+1)+" / "+ mGalleryAdapter.getItemCount());
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });
            mGalleryIndicator = (IconPageIndicator) mGalleryDialog.findViewById(R.id.indicator);
            mGalleryIndicator.setViewPager(mGalleryViewPager);
            mGalleryDialog.findViewById(R.id.back).setOnClickListener(this);
            mGalleryDialog.findViewById(R.id.delete).setOnClickListener(this);
        }
        mGalleryViewPager.removeAllViewsInLayout();
        galleryPageList.clear();
        for (int i=0;i<photoList.size()-1;i++){
            Object object = photoList.get(i);
           if (object instanceof File){
               galleryPageList.add(new HomeworkGalleryPage((File) photoList.get(i)));
           }else if (object instanceof ImageProto.ImageItem){
               galleryPageList.add(new HomeworkGalleryPage((ImageProto.ImageItem) photoList.get(i)));
           }
        }
        mGalleryAdapter.notifyDataSetChanged();
        mGalleryIndicator.notifyDataSetChanged();
        mGalleryTextView.setText(position+1+" / "+galleryPageList.size());
        mGalleryViewPager.setCurrentItem(position);
        mGalleryDialog.show();
    }








    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(mSelectPictureManager != null){
            mSelectPictureManager.onActivityResult(requestCode, resultCode, data);
        }
        if(result != null) {
            third_party_url = result.getContents();
            if(TextUtils.isEmpty(third_party_url)) {
//                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                mLlSeeTiku.setVisibility(View.VISIBLE);
                mLlAddTiku.setVisibility(View.GONE);
//                Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
            }
            checkSubmit();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onPicSelected(int key, File outputFile) {
        photoList.add(photoList.size()-1,outputFile);
        mPhotoListAdapter.notifyDataSetChanged();
        mLvPhoto.scrollTo(100000);
        checkSubmit();
    }

    private class photoListAdapter extends BaseAdapter<Object>{
        public photoListAdapter(Context context, List<Object> list){
            super(context,list);
        }

        @Override
        public ViewHolder<Object> createViewHolder() {
            return new photoViewHolder();
        }

        @Override
        public View createView(Context context, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.item_photo_list,parent,false);
        }
    }
    private class photoViewHolder extends BaseAdapter.ViewHolder<Object>{
        private AsyncImageViewV2 mImageView;
        @Override
        public void init(Context context, View convertView) {
            mImageView = (AsyncImageViewV2)convertView.findViewById(R.id.iv_photo);
        }

        @Override
        public void update(Context context, Object data) {
            if (data instanceof File){
                mImageView.setImageUrl(Uri.fromFile((File) data));
            }else if (data instanceof Integer){
                mImageView.setImageUrl(null,0,0,(Integer) data);
            }else if (data instanceof ImageProto.ImageItem){
                mImageView.setImageUrl(ImageUrlUtil.getHeadImg(((ImageProto.ImageItem)data).imagePath));
            }else{
                mImageView.setDefaultImageID(R.drawable.default_pic01);
            }
        }
    }

    private class studentHeadListAdapter extends BaseAdapter<String>{

        studentHeadListAdapter(Context context,List<String> list){
            super(context,list);
        }

        @Override
        public View createView(Context context, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.item_homework_student_head,parent,false);
        }

        @Override
        public ViewHolder<String> createViewHolder() {
            return new studentHeadViewHolder();
        }
    }

    private class studentHeadViewHolder extends BaseAdapter.ViewHolder<String>{
        private AsyncImageViewV2 image;

        @Override
        public void init(Context context, View convertView) {
            image = (AsyncImageViewV2)convertView.findViewById(R.id.img_content);
        }

        @Override
        public void update(Context context, String data) {
            image.setImageUrl(ImageUrlUtil.getHeadImg(data),R.drawable.user_pic_boy);
        }
    }

    protected void showAlert(CharSequence message, final String positive, final DialogInterface.OnClickListener positiveButtonListener,
                             String negative , final DialogInterface.OnClickListener negativeButtonListener){
        new CompatDialog.Builder(this, getAlertDialogTheme())
                .setMessage(message)
                .setPositiveButton(positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        if (positiveButtonListener != null) {
                            positiveButtonListener.onClick(dialogInterface, i);
                        }
                    }
                })
                .setNegativeButton(negative,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                if (negativeButtonListener != null) {
                                    negativeButtonListener.onClick(dialogInterface, i);
                                }
                            }
                        }).setCancelable(false).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AudioPlayerController.stopCurrentPlayer();
        stopCountDown();
    }

    protected abstract int getAudioDialogTheme();

    protected abstract int getAudioDialogLayout();

    protected abstract int getAudioViewLayout();

    protected abstract int getAlertDialogTheme();

    //仅家长端进编辑页面用
    protected abstract int getAudioDownloadLayout();

    //老师端拿地址判断是不是上海
    protected abstract String getCityName();
}

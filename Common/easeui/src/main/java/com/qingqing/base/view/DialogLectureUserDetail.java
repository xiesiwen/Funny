package com.qingqing.base.view;

import android.content.Context;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easemob.easeui.R;
import com.qingqing.api.proto.v1.LectureProto;
import com.qingqing.api.proto.v1.TeacherProto;
import com.qingqing.api.proto.v1.UserProto;
import com.qingqing.api.proto.v1.app.AppCommon;
import com.qingqing.api.proto.v1.im.ImProto;
import com.qingqing.base.config.LogicConfig;
import com.qingqing.base.constant.ThemeConstant;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.nim.ui.lecture.OnUserDetailDialogClickListener;
import com.qingqing.base.utils.ImageUrlUtil;
import com.qingqing.base.view.ratingbar.AutoResizeRatingBar;

/**
 * 学堂详情页的用户详情弹窗
 *
 * Created by lihui on 2016/5/31.
 */
public class DialogLectureUserDetail extends RelativeLayout
        implements View.OnClickListener {
    
    private AsyncImageViewV2 mIvHead;
    private TextView mTvName;
    private LinearLayout mLlDetail;
    private ImageView mIvExit;
    private TextView mTvBtnRight;
    private TextView mTvBtnLeft;
    private TextView mTvPrice;
    private TextView mTvEffect;
    private TextView mTvService;
    private AutoResizeRatingBar mRbStar;
    private TextView mTvContent;
    private int mTargetUserType;
    private OnUserDetailDialogClickListener mClickListener;
    private int mTargetRoleType;
    private boolean mIsMute;
    private TextView mTvContentScroll;
    
    public DialogLectureUserDetail(Context context) {
        this(context, null);
    }
    
    public DialogLectureUserDetail(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(context).inflate(R.layout.dlg_lecture_user_detail,
                this);
        initView(view);
    }
    
    private void initView(View view) {
        mIvHead = (AsyncImageViewV2) view.findViewById(R.id.iv_head);
        mTvName = (TextView) view.findViewById(R.id.tv_name);
        mTvContent = (TextView) view.findViewById(R.id.tv_content);
        mTvContentScroll = (TextView) view.findViewById(R.id.tv_content_scroll);
        mLlDetail = (LinearLayout) view.findViewById(R.id.ll_detail);
        mRbStar = (AutoResizeRatingBar) view.findViewById(R.id.rb_star);
        mTvService = (TextView) view.findViewById(R.id.tv_service);
        mTvEffect = (TextView) view.findViewById(R.id.tv_effect);
        mTvPrice = (TextView) view.findViewById(R.id.tv_price);
        mTvBtnLeft = (TextView) view.findViewById(R.id.tv_left);
        mTvBtnRight = (TextView) view.findViewById(R.id.tv_right);
        mIvExit = (ImageView) view.findViewById(R.id.iv_exit);
        mTvBtnLeft.setOnClickListener(this);
        mTvBtnRight.setOnClickListener(this);
        mIvExit.setOnClickListener(this);
        
        mTvBtnLeft.setTextColor(ThemeConstant.getThemeColor(getContext()));
        mTvBtnRight.setTextColor(ThemeConstant.getThemeColor(getContext()));
        
        initRatingBar();
    }
    
    private void initRatingBar() {
        mRbStar.setProgressDrawable(R.drawable.icon_rating_bar_normal,
                R.drawable.icon_rating_bar_selected);
    }
    
    /**
     * 设置用户
     *
     * @param lecturer
     *            点击的用户
     * @param targetRoleType
     *            点击的用户的角色类型
     * @param isMute
     *            该用户是否已被禁言
     * @param listener
     *            点击监听
     */
    public void setLecturer(LectureProto.Lecturer lecturer, int targetRoleType,
            boolean isMute, OnUserDetailDialogClickListener listener) {
        mTargetUserType = lecturer.userInfo.userType;
        mTargetRoleType = targetRoleType;
        mClickListener = listener;
        mIsMute = isMute;
        
        setDialogType(mTargetUserType, mTargetRoleType);
        
        setHead(ImageUrlUtil.getHeadImg(lecturer.userInfo), lecturer.userInfo.sex);
        setName(lecturer.userInfo.nick);
        
        // 仅身份为讲师时才显示内容
        if (mTargetRoleType == ImProto.ChatRoomRoleType.mc_chat_room_role_type) {
            switch (mTargetUserType) {
                case UserProto.UserType.expert_user_type:
                    setContentScroll(lecturer.expertInfo.description);
                    break;
                case UserProto.UserType.teacher:
                    TeacherProto.TeacherInfoForLectuerList teacherInfo = lecturer.teacherInfo;
                    
                    setTeacherDetail((float) (teacherInfo.star),
                            teacherInfo.hasQualityOfService, teacherInfo.qualityOfService,
                            teacherInfo.hasQualityOfEffect, teacherInfo.qualityOfEffect,
                            teacherInfo.hasMinCourseUnitPrice,
                            (int) (teacherInfo.minCourseUnitPrice),
                            teacherInfo.hasMaxCourseUnitPrice,
                            (int) (teacherInfo.maxCourseUnitPrice));
                    break;
                case UserProto.UserType.ta:
                    if (lecturer.assistantInfo != null) {
                        setContent(getResources().getString(
                                R.string.lecture_detail_ta_summary,
                                lecturer.assistantInfo.serveFamilyCount));
                    }
                    break;
            }
        }
        else {
            if (BaseData.getClientType() == AppCommon.AppType.qingqing_ta) {
                if (lecturer.assistantInfo != null) {
                    setContent(
                            getResources().getString(R.string.lecture_detail_ta_summary,
                                    lecturer.assistantInfo.serveFamilyCount));
                }
                else {
                    setContent("");
                }
            }
            else {
                setContent("");
            }
        }
    }
    
    /**
     * 根据用户类型决定 UI 布局
     */
    private void setDialogType(int userType, int targetRoleType) {
        switch (targetRoleType) {
            case ImProto.ChatRoomRoleType.mc_chat_room_role_type:
                // 讲师根据 userType 来区分显示内容
                switch (userType) {
                    case UserProto.UserType.student:
                        // 线上不会有此情况
                        mTvContent.setVisibility(VISIBLE);
                        mTvContentScroll.setVisibility(GONE);
                        mLlDetail.setVisibility(GONE);
                        mTvBtnRight.setVisibility(GONE);
                        
                        mTvBtnLeft.setText(R.string.got_it);
                        break;
                    case UserProto.UserType.expert_user_type:
                        mTvContent.setVisibility(GONE);
                        mTvContentScroll.setVisibility(VISIBLE);
                        mLlDetail.setVisibility(GONE);
                        mTvBtnRight.setVisibility(GONE);
                        
                        mTvBtnLeft.setText(R.string.got_it);
                        break;
                    case UserProto.UserType.teacher:
                        mTvContent.setVisibility(GONE);
                        mTvContentScroll.setVisibility(GONE);
                        mLlDetail.setVisibility(VISIBLE);
                        
                        if (BaseData
                                .getClientType() == AppCommon.AppType.qingqing_student) {
                            mTvBtnRight.setVisibility(VISIBLE);
                            
                            mTvBtnLeft.setText(R.string.user_detail_dlg_text_favourite);
                            mTvBtnRight.setText(
                                    R.string.user_detail_dlg_text_enter_teacher_home);
                        }
                        else if (BaseData
                                .getClientType() == AppCommon.AppType.qingqing_teacher) {
                            mTvBtnRight.setVisibility(GONE);
                            
                            mTvBtnLeft.setText(
                                    R.string.user_detail_dlg_text_enter_teacher_home);
                        }
                        break;
                    case UserProto.UserType.ta:
                        mTvContent.setVisibility(VISIBLE);
                        mTvContentScroll.setVisibility(GONE);
                        mLlDetail.setVisibility(GONE);
                        mTvBtnRight.setVisibility(GONE);
                        
                        if (BaseData
                                .getClientType() == AppCommon.AppType.qingqing_student) {
                            mTvBtnLeft.setText(
                                    R.string.user_detail_dlg_text_enter_assist_home);
                        }
                        else if (BaseData
                                .getClientType() == AppCommon.AppType.qingqing_teacher) {
                            mTvBtnLeft.setText(R.string.got_it);
                        }
                        else if (BaseData
                                .getClientType() == AppCommon.AppType.qingqing_ta) {
                            mTvBtnLeft.setText(R.string.got_it);
                        }
                        break;
                }
                break;
            case ImProto.ChatRoomRoleType.general_chat_room_role_type:
            case ImProto.ChatRoomRoleType.guest_chat_room_role_type:
            default:
                // 听众/游客/未知类型只显示禁言
                mTvContent.setVisibility(VISIBLE);
                mTvContentScroll.setVisibility(GONE);
                mLlDetail.setVisibility(GONE);
                mTvBtnRight.setVisibility(GONE);
                
                if (BaseData.getClientType() == AppCommon.AppType.qingqing_ta) {
                    mTvBtnLeft.setText(mIsMute ? R.string.user_detail_dlg_text_un_mute
                            : R.string.got_it);
                }
                else {
                    mTvBtnLeft.setText(mIsMute ? R.string.user_detail_dlg_text_un_mute
                            : R.string.user_detail_dlg_text_mute);
                }
                break;
        }
    }
    
    /**
     * 设置头像
     */
    private void setHead(String url, int sexType) {
        mIvHead.setImageUrl(url, LogicConfig.getDefaultHeadIcon(sexType));
    }
    
    /**
     * 设置姓名
     */
    private void setName(String name) {
        mTvName.setText(name);
    }
    
    /**
     * 设置显示的内容
     */
    private void setContent(String content) {
        mTvContent.setText(content);
    }
    
    /**
     * 设置显示的内容,可滚动部分
     */
    private void setContentScroll(String content) {
        mTvContentScroll.setText(content);
        mTvContentScroll.setMovementMethod(new ScrollingMovementMethod());
        mTvContentScroll.scrollTo(0, 0);
    }
    
    /**
     * 设置老师的分数等数据
     */
    private void setTeacherDetail(float star, boolean hasQualityService,
            double qualityService, boolean hasQualityEffect, double qualityEffect,
            boolean hasMinPrice, int minPrice, boolean hasMaxPrice, int maxPrice) {
        // 星级
        mRbStar.setRating(star);
        
        // 服务
        mTvService.setText(hasQualityService
                ? getResources().getString(R.string.teacher_home_quality_service,
                        qualityService)
                : getResources()
                        .getString(R.string.teacher_home_quality_service_default));
        // 效果
        mTvEffect.setText(hasQualityEffect
                ? getResources().getString(R.string.teacher_home_quality_effect,
                        qualityEffect)
                : getResources().getString(R.string.teacher_home_quality_effect_default));
        // 价格
        if (hasMinPrice || hasMaxPrice) {
            if (minPrice == maxPrice) {
                mTvPrice.setText(getResources().getString(
                        R.string.teacher_home_same_price, String.valueOf(minPrice)));
            }
            else {
                mTvPrice.setText(getResources().getString(R.string.teacher_home_price,
                        String.valueOf(minPrice), String.valueOf(maxPrice)));
            }
        }
        else {
            mTvPrice.setText(getResources().getString(R.string.no_course_text));
        }
    }
    
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_exit) {
            mClickListener.onExitButton();
        }
        else if (id == R.id.tv_left) {
            if (mClickListener == null) {
                return;
            }
            switch (mTargetRoleType) {
                case ImProto.ChatRoomRoleType.mc_chat_room_role_type:
                    switch (mTargetUserType) {
                        case UserProto.UserType.expert_user_type:
                            mClickListener.onExpertCancel();
                            break;
                        case UserProto.UserType.student:
                            // 线上不会有此情况
                            mClickListener.onExit();
                            break;
                        case UserProto.UserType.teacher:
                            if (BaseData
                                    .getClientType() == AppCommon.AppType.qingqing_student) {
                                mClickListener.onTeacherAttention();
                            }
                            else if (BaseData
                                    .getClientType() == AppCommon.AppType.qingqing_teacher) {
                                mClickListener.onTeacherEnterHomePage();
                            }
                            break;
                        case UserProto.UserType.ta:
                            if (BaseData
                                    .getClientType() == AppCommon.AppType.qingqing_student) {
                                mClickListener.onAssistEnterHomePage();
                            }
                            else if (BaseData
                                    .getClientType() == AppCommon.AppType.qingqing_teacher) {
                                mClickListener.onExit();
                            }
                            else if (BaseData
                                    .getClientType() == AppCommon.AppType.qingqing_ta) {
                                mClickListener.onAssistEnterHomePage();
                            }
                            break;
                    }
                    break;
                case ImProto.ChatRoomRoleType.general_chat_room_role_type:
                case ImProto.ChatRoomRoleType.guest_chat_room_role_type:
                default:
                    if (BaseData.getClientType() == AppCommon.AppType.qingqing_ta) {
                        mClickListener.onExit();
                    }
                    else {
                        mClickListener.onStudentMute(mIsMute);
                    }
                    break;
            }
        }
        else if (id == R.id.tv_right) {
            if (mClickListener == null) {
                return;
            }
            if (mTargetUserType == UserProto.UserType.teacher) {
                mClickListener.onTeacherEnterHomePage();
            }
        }
    }
}

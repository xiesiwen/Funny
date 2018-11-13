package com.qingqing.project.offline.view.course;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qingqing.api.proto.v1.MyComment;
import com.qingqing.api.proto.v1.app.AppCommon;
import com.qingqing.base.activity.ImageShowActivity;
import com.qingqing.base.bean.ImageGroup;
import com.qingqing.base.bean.MultiMediaFactory;
import com.qingqing.base.bean.MultiMediaItem;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.utils.DateUtils;
import com.qingqing.base.utils.DisplayUtil;
import com.qingqing.base.utils.ImageUrlUtil;
import com.qingqing.base.view.AsyncImageViewV2;
import com.qingqing.base.view.AtMostGridView;
import com.qingqing.base.view.TagLayout;
import com.qingqing.base.view.ratingbar.AutoResizeRatingBar;
import com.qingqing.project.offline.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * 课程详情展示评价view
 *
 * Created by tanwei on 2016/4/18.
 */
public class AppraiseDisplayView extends LinearLayout {
    
    private TextView mTvAppraiseTitle, mTvAppraiseTime, mTvAppraiseContent;
    
    private View mViewScoreLayout;// 评分栏
    private AutoResizeRatingBar mRatingBar;
    private TextView mTVServiceScore;
    private TextView mTVEffectScore;
    
    private TagLayout mTagLayout;// 评价标签
    
    private AtMostGridView mGridView;// 图片展示
    
    private ArrayList<String> mUrlList;
    
    private View mTeacherReplyAppraiseLayout;// 老师反馈

    public AppraiseDisplayView(Context context) {
        super(context);
        
        init(context);
    }
    
    public AppraiseDisplayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        init(context);
    }
    
    private void init(final Context context) {
        inflate(context, R.layout.view_appraise_displayer, this);
        
        mTvAppraiseTitle = (TextView) findViewById(
                R.id.view_appraise_teacher_reply_title);
        mTvAppraiseTime = (TextView) findViewById(R.id.view_appraise_tv_appraise_time);
        mTvAppraiseContent = (TextView) findViewById(
                R.id.view_appraise_tv_appraise_content);
        
        mViewScoreLayout = findViewById(R.id.view_appraise_rating_layout);
        mRatingBar = (AutoResizeRatingBar) findViewById(R.id.rb_total);
        mRatingBar.setProgressDrawable(R.drawable.icon_rating_bar_normal,
                R.drawable.icon_rating_bar_selected);
        mTVServiceScore = (TextView) findViewById(R.id.tv_service_score);
        mTVEffectScore = (TextView) findViewById(R.id.tv_effect_score);
        
        mTagLayout = (TagLayout) findViewById(R.id.view_appraise_tag_layout);
        mTagLayout.setTagRejectSelected(true);
        
        mGridView = (AtMostGridView) findViewById(R.id.view_appraise_grid_view);
        // 设置GridVIew占据3/4屏幕宽度
        mGridView.getLayoutParams().width = (int) (DisplayUtil.getScreenWidth() * 0.75);
        
        mTeacherReplyAppraiseLayout = findViewById(
                R.id.view_appraise_layout_teacher_reply_appraise);
    }

    /** 根据数据展示评价 */
    public void displayAppraise(MyComment.OrderCourseCommentForDetail detail,
            int tagBgResId) {
        // 老师回复评价
        if (detail.hasTeacherAppraiseTime) {
            mTeacherReplyAppraiseLayout.setVisibility(VISIBLE);
            TextView time = (TextView) mTeacherReplyAppraiseLayout
                    .findViewById(R.id.view_appraise_teacher_reply_time);
            TextView content = (TextView) mTeacherReplyAppraiseLayout
                    .findViewById(R.id.view_appraise_teacher_reply_content);
            
            if (BaseData.getClientType() == AppCommon.AppType.qingqing_teacher) {
                time.setText(DateUtils.MonthDayhourminuteReplyFormat.format(new Date(detail.teacherAppraiseTime)));
            }
            else {
                time.setText(DateUtils.mdhmsdf.format(new Date(detail.teacherAppraiseTime)));
            }
            content.setText(detail.teacherCommentWord);
        }
        
        mTvAppraiseTime
                .setText(DateUtils.YearMonthDayhourminuteFormat.format(new Date(detail.studentAppraiseTime)));
        mTvAppraiseContent.setText(detail.studentCommentWord);
        
        // 标签
        final Context context = getContext();
        int textColor;
        switch (BaseData.getClientType()) {
            case AppCommon.AppType.qingqing_student:
            default:
                textColor = context.getResources().getColor(R.color.primary_green);
                mRatingBar.setRating(detail.qualityOfCourse / 2.0f);
                mTVServiceScore.setText(
                        context.getString(R.string.text_base_appraise_service_score,
                                detail.qualityOfService / 1.0f));
                mTVEffectScore.setText(
                        context.getString(R.string.text_base_appraise_effect_score,
                                detail.qualityOfEffect / 1.0f));
                break;
            case AppCommon.AppType.qingqing_teacher:
                mViewScoreLayout.setVisibility(GONE);
                textColor = context.getResources().getColor(R.color.primary_blue);
                mTvAppraiseTitle.setText(R.string.text_course_appraise_my_feedback);
                break;
            case AppCommon.AppType.qingqing_ta:
                mViewScoreLayout.setVisibility(GONE);
                textColor = context.getResources().getColor(R.color.primary_orange);
                break;
        }
        
        mTvAppraiseTitle.setTextColor(textColor);
        
        if (detail.studentPhrases.length > 0) {
            mTagLayout.setVisibility(VISIBLE);
            for (String tag : detail.studentPhrases) {
                TextView tv = new TextView(context);
                tv.setText(tag);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                tv.setTextColor(textColor);
                tv.setBackgroundResource(tagBgResId);
                tv.setGravity(Gravity.CENTER_VERTICAL);
                mTagLayout.addTag(tag, tv);
            }
        }
        else {
            mTagLayout.setVisibility(GONE);
        }
        
        // 图片
        if (detail.studentImages.length > 0) {
            BaseAdapter baseAdapter = new BaseAdapter() {
                @Override
                public int getCount() {
                    return mUrlList.size();
                }
                
                @Override
                public Object getItem(int position) {
                    return mUrlList.get(position);
                }
                
                @Override
                public long getItemId(int position) {
                    return position;
                }
                
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    
                    AsyncImageViewV2 imageViewV2;
                    if (convertView != null) {
                        imageViewV2 = (AsyncImageViewV2) convertView
                                .findViewById(R.id.item_appraise_displayer_img);
                    }
                    else {
                        // imageViewV2 = new AsyncImageViewV2(context);
                        // AbsListView.LayoutParams params = new
                        // AbsListView.LayoutParams(
                        // LayoutParams.MATCH_PARENT,
                        // LayoutParams.MATCH_PARENT);
                        // imageViewV2.setLayoutParams(params);
                        // imageViewV2.setScaleType(ImageView.ScaleType.FIT_XY);
                        
                        convertView = LayoutInflater.from(getContext())
                                .inflate(R.layout.item_appraise_displayer_img, null);
                        imageViewV2 = (AsyncImageViewV2) convertView
                                .findViewById(R.id.item_appraise_displayer_img);
                    }
                    
                    String url = mUrlList.get(position);
                    imageViewV2.setImageUrl(ImageUrlUtil.getHeadImg(url),
                            R.drawable.default_pic01);
                    
                    return convertView;
                }
            };
            mUrlList = new ArrayList<String>();
            for (String s : detail.studentImages) {
                mUrlList.add(s);
            }
            mGridView.setVisibility(VISIBLE);
            mGridView.setAdapter(baseAdapter);
            mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position,
                        long id) {
                    Intent intent = new Intent();
                    intent.setClass(getContext(), ImageShowActivity.class);
                    ArrayList<MultiMediaItem> list = new ArrayList<>(mUrlList.size());
                    for (String url : mUrlList) {
                        list.add(MultiMediaFactory
                                .createImage(ImageUrlUtil.getOriginImg(url)));
                    }
                    intent.putExtra(ImageShowActivity.KEY_IMG_GROUP,
                            new ImageGroup(list));
                    intent.putExtra(ImageShowActivity.KEY_IDX_IN_GROUP, position);
                    getContext().startActivity(intent);
                }
            });
        }
        else {
            mGridView.setVisibility(GONE);
        }
    }
}

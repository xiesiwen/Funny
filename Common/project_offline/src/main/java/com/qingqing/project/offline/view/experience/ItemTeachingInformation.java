package com.qingqing.project.offline.view.experience;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import com.facebook.common.references.CloseableReference;
import com.facebook.common.util.UriUtil;
import com.facebook.datasource.DataSource;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.qingqing.api.proto.v1.ArticleProto;
import com.qingqing.api.proto.v1.ImageProto;
import com.qingqing.base.utils.DateUtils;
import com.qingqing.base.utils.ImageUrlUtil;
import com.qingqing.base.utils.ImageUtil;
import com.qingqing.base.utils.TimeUtil;
import com.qingqing.base.view.AsyncImageViewV2;
import com.qingqing.base.view.NinePictureView;
import com.qingqing.project.offline.R;
import com.qingqing.project.offline.teachinformation.InformationListFragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 教学心得 item
 * <p/>
 * Created by lihui on 2016/6/17.
 */
public class ItemTeachingInformation extends RelativeLayout implements View.OnClickListener {
    private static final int DEFAULT_SINGLE_IMAGE_SIZE = 200;

    private final Context mContext;
    private TextView mTvPublishTimeDay;
    private TextView mTvPublishTimeMonth;
    private RelativeLayout mRlContent;
    private LinearLayout mLlContentTextImage;
    private TextView mTvTextImageTitle;
    private TextView mTvTextImageContent;
    private TextView mTvTextImageContentBrief;
    private AsyncImageViewV2 mIvTextImageSingleImage;
    private NinePictureView mItemTextImageMultiImage;
    private LinearLayout mLlContentWebUrl;
    private AsyncImageViewV2 mIvContentWebUrlImage;
    private TextView mTvContentWebUrlTitle;
    private LinearLayout mLlBottomBar;
    private ItemTeachingInformationBottom mItemDelete;
    private ItemTeachingInformationBottom mItemEdit;
    private ItemTeachingInformationBottom mItemShare;
    private ImageView mIvRecommend;

    private int mArticleType;

    private ItemTeacherExperienceClickListener mClickListener;
    private int mContentImageWidth;
    private InformationListFragment.InformationSetDoneListener mListener;
    private String mSingleImageUrl;


    public ItemTeachingInformation(Context context) {
        this(context, null);
    }

    public ItemTeachingInformation(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        View view = LayoutInflater.from(context).inflate(R.layout.item_teaching_information, this);
        initView(view);
    }

    private void initView(View view) {
        mTvPublishTimeDay = (TextView) view.findViewById(R.id.tv_publish_time_day);
        mTvPublishTimeMonth = (TextView) view.findViewById(R.id.tv_publish_time_month);
        mRlContent = (RelativeLayout) view.findViewById(R.id.rl_content);
        mLlContentTextImage = (LinearLayout) view.findViewById(R.id.ll_content_text_image);
        mTvTextImageTitle = (TextView) view.findViewById(R.id.tv_text_image_title);
        mTvTextImageContent = (TextView) view.findViewById(R.id.tv_text_image_content);
        mTvTextImageContentBrief = (TextView) view.findViewById(R.id.tv_text_image_content_brief);
        mIvTextImageSingleImage = (AsyncImageViewV2) view.findViewById(R.id.iv_text_image_single_image);
        mItemTextImageMultiImage = (NinePictureView) view.findViewById(R.id.item_text_image_multi_image);
        mLlContentWebUrl = (LinearLayout) view.findViewById(R.id.ll_content_web_url);
        mIvContentWebUrlImage = (AsyncImageViewV2) view.findViewById(R.id.iv_content_web_url_image);
        mTvContentWebUrlTitle = (TextView) view.findViewById(R.id.tv_content_web_url_title);
        mLlBottomBar = (LinearLayout) view.findViewById(R.id.ll_bottom_bar);
        mItemDelete = (ItemTeachingInformationBottom) view.findViewById(R.id.item_delete);
        mItemEdit = (ItemTeachingInformationBottom) view.findViewById(R.id.item_edit);
        mItemShare = (ItemTeachingInformationBottom) view.findViewById(R.id.item_share);
        mIvRecommend = (ImageView) view.findViewById(R.id.iv_recommend);


        mItemDelete.setImage(R.drawable.icon_delete_grey);
        mItemDelete.setContent(R.string.teacher_information_delete);
        mItemEdit.setImage(R.drawable.icon_edit_grey);
        mItemEdit.setContent(R.string.teacher_information_edit);
        mItemShare.setImage(R.drawable.icon_share_green);
        mItemShare.setContent(R.string.teacher_information_share);

        mIvTextImageSingleImage.setScaleType(ImageView.ScaleType.FIT_START);

        mTvTextImageContentBrief.setOnClickListener(this);
        mIvTextImageSingleImage.setOnClickListener(this);
        mLlContentWebUrl.setOnClickListener(this);
        mItemDelete.setOnClickListener(this);
        mItemEdit.setOnClickListener(this);
        mItemShare.setOnClickListener(this);
        mItemTextImageMultiImage.setPictureClickListener(new NinePictureView.onPictureClickListener() {
            @Override
            public void onPictureClicked(int index) {
                if (mClickListener != null) {
                    mClickListener.onShowImage(index);
                }
            }
        });
    }

    /**
     * 设置数值固定的部分，减少重复计算
     *
     * @param contentImageWidth 图片栏的宽度
     */
    public void setFixedItemWidth(int contentImageWidth) {
        if (contentImageWidth != 0) {
            mContentImageWidth = contentImageWidth;
        }

        if (mContentImageWidth == 0) {
            mLlContentTextImage.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mContentImageWidth = mLlContentTextImage.getWidth() - mLlContentTextImage.getPaddingLeft() - mLlContentTextImage.getPaddingRight();
                    mLlContentTextImage.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                    if (!TextUtils.isEmpty(mSingleImageUrl)) {
                        setSingleImage(mSingleImageUrl);
                    }

                    if (mListener != null) {
                        mListener.onSetDone(mContentImageWidth);
                    }
                }
            });
        }
    }

    /**
     * 存储固定宽度项目的回调
     */
    public void setInformationSetDoneListener(InformationListFragment.InformationSetDoneListener listener) {
        mListener = listener;
    }

    @Override
    public void onClick(View v) {
        int clickId = v.getId();

        if (mClickListener == null) {
            return;
        }

        if (clickId == R.id.item_delete) {
            mClickListener.onDelete();
        } else if (clickId == R.id.item_edit) {
            mClickListener.onEdit();
        } else if (clickId == R.id.item_share) {
            mClickListener.onShare();
        } else if (clickId == R.id.tv_text_image_content_brief
                || clickId == R.id.ll_content_web_url) {
            mClickListener.onEnterDetail();
        } else if (clickId == R.id.iv_text_image_single_image) {
            mClickListener.onShowImage(0);
        }
    }

    /**
     * 是否显示底部栏
     */
    public void setShowBottomBar(boolean isShow) {
        mLlBottomBar.setVisibility(isShow ? VISIBLE : GONE);
    }

    /**
     * 发布时间
     */
    public void setPublishTime(long publishTime) {
        mTvPublishTimeDay.setText(DateUtils.dayFormat.format(publishTime));
        mTvPublishTimeMonth.setText(DateUtils.shortMonthFormat.format(publishTime));
    }

    /**
     * 是否显示发布时间
     */
    public void setIsShowPublishTime(long previousTime, long currentTime) {
        // 上一个没有item时，显示发布时间
        if (previousTime == -1) {
            mTvPublishTimeDay.setVisibility(VISIBLE);
            mTvPublishTimeMonth.setVisibility(VISIBLE);
        } else {
            // 同一天发布的，只显示一个发布时间
            if (TimeUtil.getCountInMinutes(previousTime, currentTime) <= 24 * 60) {
                String previousDay = DateUtils.dayFormat.format(previousTime);
                String currentDay = DateUtils.dayFormat.format(currentTime);
                if (previousDay.equals(currentDay)) {
                    mTvPublishTimeDay.setVisibility(INVISIBLE);
                    mTvPublishTimeMonth.setVisibility(INVISIBLE);
                } else {
                    mTvPublishTimeDay.setVisibility(VISIBLE);
                    mTvPublishTimeMonth.setVisibility(VISIBLE);
                }
            } else {
                mTvPublishTimeDay.setVisibility(VISIBLE);
                mTvPublishTimeMonth.setVisibility(VISIBLE);
            }
        }
    }

    /**
     * 心得主体内容
     *
     * @param article     心得
     * @param isRecommend 是否推荐
     */
    public void setArticle(ArticleProto.Article article, boolean isRecommend) {
        mArticleType = article.articleType;
        setArticleType(mArticleType);
        setIsRecommend(isRecommend);

        if (mArticleType == ArticleProto.ArticleType.txt_image_article_type) {
            setArticleTextTitle(article.title);
            setArticleContent(article.content);
            setArticleContentImage(article.contentImages);
        } /*else if (mArticleType == ArticleProto.ArticleType.out_link_article_type
                || mArticleType == ArticleProto.ArticleType.rich_html_article_type
                || mArticleType == ArticleProto.ArticleType.unknown_article_type) {
            setArticleWebTitle(article.title);
            setArticleHeadImage(article.headImage);
        }*/ else {
            setArticleWebTitle(article.title);
            setArticleHeadImage(article.headImage);
        }
    }

    /**
     * 是否可编辑
     */
    public void setIsEditable(boolean isEditable) {
        mItemEdit.setVisibility(isEditable ? VISIBLE : GONE);
    }

    public void setItemClickListener(ItemTeacherExperienceClickListener listener) {
        mClickListener = listener;
    }

    private void setArticleType(int articleType) {
        switch (articleType) {
            case ArticleProto.ArticleType.txt_image_article_type:
                mLlContentTextImage.setVisibility(VISIBLE);
                mLlContentWebUrl.setVisibility(GONE);
                break;
            case ArticleProto.ArticleType.out_link_article_type:
            case ArticleProto.ArticleType.rich_html_article_type:
            case ArticleProto.ArticleType.unknown_article_type:
            default:
                mLlContentTextImage.setVisibility(GONE);
                mLlContentWebUrl.setVisibility(VISIBLE);
                break;
        }
    }

    private void setArticleTextTitle(String title) {
        mTvTextImageTitle.setText(title);
    }

    private void setArticleContent(String content) {
        if (content.length() > 120) {
            mTvTextImageContent.setVisibility(GONE);
            mTvTextImageContentBrief.setVisibility(VISIBLE);

            mTvTextImageContentBrief.setText(content);
        } else {
            mTvTextImageContent.setVisibility(VISIBLE);
            mTvTextImageContentBrief.setVisibility(GONE);

            mTvTextImageContent.setText(content);
        }
    }


    private void setArticleContentImage(ImageProto.ImageItem[] contentImagesList) {
        if (contentImagesList.length > 1) {
            mIvTextImageSingleImage.setVisibility(GONE);
            mItemTextImageMultiImage.setVisibility(VISIBLE);

            mSingleImageUrl = null;

            mItemTextImageMultiImage.setViewNumber(contentImagesList.length);

            ArrayList<String> urlList = new ArrayList<>();
            for (ImageProto.ImageItem imageItem : contentImagesList) {
                urlList.add(ImageUrlUtil.getDefaultCropImg(imageItem.imagePath));
            }
            mItemTextImageMultiImage.setImages(urlList, R.drawable.default_pic01);
        } else if (contentImagesList.length > 0) {
            mIvTextImageSingleImage.setVisibility(VISIBLE);
            mItemTextImageMultiImage.setVisibility(GONE);

            // 设置默认占位图片
            setSingleImageContent(null, mIvTextImageSingleImage.getMeasuredWidth(), mIvTextImageSingleImage.getMeasuredHeight());

            mSingleImageUrl = ImageUrlUtil.getOriginImg(contentImagesList[0].imagePath);
            setSingleImage(mSingleImageUrl);
        } else {
            mIvTextImageSingleImage.setVisibility(GONE);
            mItemTextImageMultiImage.setVisibility(GONE);

            mSingleImageUrl = null;
        }
    }

    private void setSingleImage(final String url) {
        if (TextUtils.isEmpty(url)) {
            setSingleImageContent(null, DEFAULT_SINGLE_IMAGE_SIZE,
                    DEFAULT_SINGLE_IMAGE_SIZE);
            return;
        }

        ImageUtil.getImageBitmap(url, new BaseBitmapDataSubscriber() {
            @Override
            protected void onNewResultImpl(Bitmap bitmap) {
                float width = bitmap.getWidth();
                float height = bitmap.getHeight();

                // 按照最大宽度缩放
                if (width > mContentImageWidth) {
                    height = height * mContentImageWidth / width;
                    width = mContentImageWidth;
                }

                // 按照最大高度缩放
                if (height > (mContentImageWidth * 2 / 3)) {
                    width = width * (mContentImageWidth * 2 / 3) / height;
                    height = (mContentImageWidth * 2 / 3);
                }

                // 按照最小宽度缩放
                if (width < (mContentImageWidth / 3)) {
                    height = height * (mContentImageWidth / 3) / width;
                    width = (mContentImageWidth / 3);
                }

                // 按照最小高度缩放
                if (height < (mContentImageWidth / 3)) {
                    width = width * (mContentImageWidth / 3) / height;
                    height = (mContentImageWidth / 3);
                }

                if (width != 0 && height != 0) {
                    final int imageWidth = (int) width;
                    final int imageHeight = height > (mContentImageWidth * 2 / 3)
                            ? mContentImageWidth * 2 / 3 : (int) height;
                    post(new Runnable() {
                        @Override
                        public void run() {
                            setSingleImageContent(url, imageWidth, imageHeight);
                        }
                    });
                }
                else {
                    post(new Runnable() {
                        @Override
                        public void run() {
                            if (mContentImageWidth > 0 && mIvTextImageSingleImage != null
                                    && TextUtils.isEmpty(
                                            mIvTextImageSingleImage.getImageUrl())) {
                                setSingleImageContent(null, DEFAULT_SINGLE_IMAGE_SIZE,
                                        DEFAULT_SINGLE_IMAGE_SIZE);
                            }
                        }
                    });
                }
            }

            @Override
            protected void onFailureImpl(
                    DataSource<CloseableReference<CloseableImage>> closeableReferenceDataSource) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        setSingleImageContent(null, DEFAULT_SINGLE_IMAGE_SIZE,
                                DEFAULT_SINGLE_IMAGE_SIZE);
                    }
                });
            }
        });
    }

    private void setSingleImageContent(String url, int width, int height) {
        if (mIvTextImageSingleImage == null) {
            return;
        }

        ViewGroup.LayoutParams lp = mIvTextImageSingleImage.getLayoutParams();
        lp.width = width;
        lp.height = height;
        mIvTextImageSingleImage.setLayoutParams(lp);
        mIvTextImageSingleImage.requestLayout();

        if (!TextUtils.isEmpty(url)) {
            mIvTextImageSingleImage.setImageUrl(url, R.drawable.default_pic01);
        } else {
            mIvTextImageSingleImage.setImageUrl(new Uri.Builder()
                    .scheme(UriUtil.LOCAL_RESOURCE_SCHEME)
                    .path(String.valueOf(R.drawable.default_pic01))
                    .build());
        }
    }

    private void setArticleWebTitle(String title) {
        mTvContentWebUrlTitle.setText(title);
    }

    private void setArticleHeadImage(ImageProto.ImageItem headImage) {
        mIvContentWebUrlImage.setImageUrl(ImageUrlUtil.getDefaultCropImg(headImage.imagePath), R.drawable.default_pic01);
    }

    private void setIsRecommend(boolean isRecommend) {
        mIvRecommend.setVisibility(isRecommend ? VISIBLE : GONE);
    }

    public interface ItemTeacherExperienceClickListener {
        void onDelete();

        void onEdit();

        void onShare();

        void onEnterDetail();

        void onShowImage(int index);
    }
}

package com.qingqing.project.offline.teachinformation;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.protobuf.nano.MessageNano;
import com.qingqing.api.proto.v1.ArticleProto;
import com.qingqing.api.proto.v1.CommonPage;
import com.qingqing.api.proto.v1.ImageProto;
import com.qingqing.api.proto.v1.InformationProto;
import com.qingqing.api.proto.v1.ProtoBufResponse;
import com.qingqing.api.proto.v1.app.AppCommon;
import com.qingqing.base.activity.ImageShowActivity;
import com.qingqing.base.bean.ImageGroup;
import com.qingqing.base.bean.MultiMediaFactory;
import com.qingqing.base.bean.MultiMediaItem;
import com.qingqing.base.config.CommonUrl;
import com.qingqing.base.constant.BaseParamKeys;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.dialog.CompDefaultDialogBuilder;
import com.qingqing.qingqingbase.ui.BaseFragment;
import com.qingqing.base.fragment.PtrListFragment;
import com.qingqing.base.http.HttpUrl;
import com.qingqing.base.http.req.ProtoListener;
import com.qingqing.base.share.ShareShow;
import com.qingqing.base.utils.DisplayUtil;
import com.qingqing.base.utils.ImageUrlUtil;
import com.qingqing.base.utils.UrlUtil;
import com.qingqing.base.view.BaseAdapter;
import com.qingqing.base.view.EmptyView;
import com.qingqing.base.view.ToastWrapper;
import com.qingqing.project.offline.R;
import com.qingqing.project.offline.view.experience.ItemTeachingInformation;

import java.util.ArrayList;
import java.util.List;

/**
 * 教学心得列表页的公用部分
 *
 * Created by lihui on 2016/6/17.
 */
public class InformationListFragment extends PtrListFragment
        implements View.OnClickListener {
    
    private ArrayList<InformationProto.InformationDetailForList> mInformationList = new ArrayList<>();
    private InformationListAdapter mInformationListAdapter;
    private TextView mTvEmptyCreateInformation;
    private boolean mIsClientTeacher = false;
    private String mQqUserId;
    private boolean mHideEdit;
    private EmptyView mViewEmpty;
    private boolean mHideRecommend = false;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_information_list, container, false);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        
        // 仅老师端显示可编辑部分
        mIsClientTeacher = (BaseData
                .getClientType() == AppCommon.AppType.qingqing_teacher);
        
        if (getArguments() != null) {
            mQqUserId = getArguments()
                    .getString(BaseParamKeys.PARAM_STRING_QINGQING_USER_ID);
            mHideEdit = getArguments().getBoolean(BaseParamKeys.PARAM_BOOLEAN_HIDE_EDIT,
                    false);
            mHideRecommend = getArguments()
                    .getBoolean(BaseParamKeys.PARAM_BOOLEAN_HIDE_RECOMMEND, false);
        }
        
        mPtrListView.setDividerHeight(mIsClientTeacher ? DisplayUtil.dp2px(1) : 0);
        refreshFromStart();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    
    @Override
    public void onStart() {
        super.onStart();
        setTitle(R.string.teacher_information_text);
    }
    
    private void initView(View view) {
        mTvEmptyCreateInformation = (TextView) view
                .findViewById(R.id.tv_empty_create_information);
        mTvEmptyCreateInformation.setOnClickListener(this);
        mViewEmpty = (EmptyView) view.findViewById(R.id.view_empty);
        mViewEmpty.setVisibility(mHideEdit ? View.GONE : View.VISIBLE);
        
        mInformationListAdapter = new InformationListAdapter(getActivity(),
                mInformationList);
        mPtrListView.setAdapter(mInformationListAdapter);
    }
    
    @Override
    protected MessageNano getSendMessage(String tag) {
        if (mIsClientTeacher) {
            CommonPage.SimplePageRequest builder = new CommonPage.SimplePageRequest();
            builder.count = 10;
            builder.tag = tag;
            
            return builder;
        }
        else {
            CommonPage.SimpleTeacherPageRequest builder = new CommonPage.SimpleTeacherPageRequest();
            builder.count = 10;
            builder.tag = tag;
            if (!TextUtils.isEmpty(mQqUserId)) {
                builder.qingqingTeacherId = mQqUserId;
            }
            
            return builder;
        }
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        refreshFromStart();
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    @Override
    protected HttpUrl getUrlConfig() {
        if (mIsClientTeacher) {
            return CommonUrl.TEACHER_EXPERIENCE_LIST.url();
        }
        else {
            return CommonUrl.STUDENT_TEACHER_INFORMATION_LIST.url();
        }
    }
    
    @Override
    protected Class<?> getResponseClass() {
        return InformationProto.InformationDetailForListResponse.class;
    }
    
    @Override
    public void onClearData() {
        mInformationList.clear();
    }
    
    @Override
    public void onResponseSuccess(Object rspObj) {
        InformationProto.InformationDetailForListResponse response = (InformationProto.InformationDetailForListResponse) rspObj;
        for (InformationProto.InformationDetailForList informationDetailForList : response.informations) {
            mInformationList.add(informationDetailForList);
        }
        mInformationListAdapter.notifyDataSetChanged();
        
        if (mFragListener != null) {
            ((InformationListListener) mFragListener)
                    .onToggleOptionMenu(mInformationList.size() != 0);
        }
    }
    
    @Override
    public void onClick(View v) {
        int clickId = v.getId();
        
        if (clickId == R.id.tv_empty_create_information) {
            enterInformationEdit(null, false);
        }
    }
    
    private void enterInformationEdit(byte[] informationData, boolean canDelete) {
        if (mFragListener != null) {
            ((InformationListListener) mFragListener).onEnterEdit(informationData,
                    canDelete);
        }
    }
    
    private void enterInformationDetail(String informationId,
            ArticleProto.Article article) {
        if (TextUtils.isEmpty(informationId)) {
            return;
        }
        
        String url = getInformationUrl(informationId, article);
        if (mFragListener != null) {
            ((InformationListListener) mFragListener).onEnterDetail(url);
        }
    }
    
    private String getInformationUrl(String informationId, ArticleProto.Article article) {
        if (article.articleType == ArticleProto.ArticleType.txt_image_article_type
                || article.articleType == ArticleProto.ArticleType.rich_html_article_type) {
            return String.format(CommonUrl.ARTICLE_H5_URL.url().url(), informationId);
        }
        else if (article.articleType == ArticleProto.ArticleType.out_link_article_type) {
            return article.content;
        }
        else {
            return String.format(CommonUrl.ARTICLE_H5_URL.url().url(), informationId);
        }
    }
    
    public interface InformationListListener extends BaseFragment.FragListener {
        void onEnterEdit(byte[] informationData, boolean canDelete);
        
        void onToggleOptionMenu(boolean isShow);
        
        void onEnterDetail(String url);
    }
    
    public interface InformationSetDoneListener {
        void onSetDone(int contentImageWidth);
    }
    
    private class InformationListAdapter
            extends BaseAdapter<InformationProto.InformationDetailForList> {
        private int mContentImageWidth;
        InformationSetDoneListener mListener = new InformationSetDoneListener() {
            @Override
            public void onSetDone(int contentImageWidth) {
                if (mContentImageWidth == 0) {
                    mContentImageWidth = contentImageWidth;
                }
            }
        };
        
        public InformationListAdapter(Context context,
                List<InformationProto.InformationDetailForList> list) {
            super(context, list);
        }
        
        @Override
        public View createView(Context context, ViewGroup parent) {
            return new ItemTeachingInformation(context);
        }
        
        @Override
        public ViewHolder<InformationProto.InformationDetailForList> createViewHolder() {
            return new InformationListViewHolder();
        }
        
        private class InformationListViewHolder extends
                BaseAdapter.ViewHolder<InformationProto.InformationDetailForList> {
            private ItemTeachingInformation mItemTeachingInformation;
            
            @Override
            public void init(Context context, View convertView) {
                mItemTeachingInformation = (ItemTeachingInformation) convertView;
                mItemTeachingInformation.setInformationSetDoneListener(mListener);
                mItemTeachingInformation.setFixedItemWidth(mContentImageWidth);
            }
            
            @Override
            public void update(Context context,
                    final InformationProto.InformationDetailForList data) {
                mItemTeachingInformation.setPublishTime(data.publishTime);
                long previousTime = -1;
                if (position > 0) {
                    previousTime = mInformationList.get(position - 1).publishTime;
                }
                mItemTeachingInformation.setIsShowPublishTime(previousTime,
                        data.publishTime);
                if (mHideRecommend) {
                    mItemTeachingInformation.setArticle(data.article, false);
                }
                else {
                    mItemTeachingInformation.setArticle(data.article, data.isRecommend);
                }
                mItemTeachingInformation.setIsEditable(data.editable);
                mItemTeachingInformation.setShowBottomBar(mIsClientTeacher);
                mItemTeachingInformation.setItemClickListener(
                        new ItemTeachingInformation.ItemTeacherExperienceClickListener() {
                            @Override
                            public void onDelete() {
                                if (couldOperateUI()) {
                                    showDelete(data);
                                }
                            }
                            
                            @Override
                            public void onEdit() {
                                if (couldOperateUI()) {
                                    if (data.article.articleType == ArticleProto.ArticleType.unknown_article_type) {
                                        ToastWrapper.show(
                                                R.string.teacher_information_type_unknown_edit);
                                    }
                                    else {
                                        enterInformationEdit(
                                                MessageNano.toByteArray(data),
                                                mInformationList.size() > 1);
                                    }
                                }
                            }
                            
                            @Override
                            public void onShare() {
                                if (couldOperateUI()) {
                                    if (data.article.articleType == ArticleProto.ArticleType.unknown_article_type) {
                                        ToastWrapper.show(
                                                R.string.teacher_information_type_unknown_share);
                                    }
                                    else {
                                        showShare(data);
                                    }
                                    
                                }
                            }
                            
                            @Override
                            public void onEnterDetail() {
                                if (couldOperateUI()) {
                                    enterInformationDetail(
                                            mInformationList
                                                    .get(position).qingqingInformationId,
                                            mInformationList.get(position).article);
                                }
                            }
                            
                            @Override
                            public void onShowImage(int index) {
                                if (couldOperateUI()) {
                                    ArrayList<MultiMediaItem> imageList = new ArrayList<>();
                                    for (ImageProto.ImageItem item : data.article.contentImages) {
                                        imageList.add(
                                                MultiMediaFactory.createImage(ImageUrlUtil
                                                        .getOriginImg(item.imagePath)));
                                    }
                                    
                                    Intent intent = new Intent(getActivity(),
                                            ImageShowActivity.class);
                                    intent.putExtra(ImageShowActivity.KEY_IDX_IN_GROUP,
                                            index);
                                    intent.putExtra(ImageShowActivity.KEY_IMG_GROUP,
                                            new ImageGroup(imageList));
                                    startActivity(intent);
                                }
                            }
                        });
            }
            
            private void showDelete(
                    final InformationProto.InformationDetailForList data) {
                new CompDefaultDialogBuilder(getActivity())
                        .setTitle(R.string.teacher_information_delete_title)
                        .setContent(getResources()
                                .getString(R.string.teacher_information_delete_content))
                        .setPositiveButton(R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface,
                                            int i) {
                                        deleteInformation(data);
                                        dialogInterface.dismiss();
                                    }
                                })
                        .setNegativeButton(R.string.cancel,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface,
                                            int i) {
                                        dialogInterface.dismiss();
                                    }
                                })
                        .show();
                
            }
            
            private void deleteInformation(
                    final InformationProto.InformationDetailForList data) {
                InformationProto.SimpleQingQingInformationIdRequest simpleQingQingInformationIdRequest = new InformationProto.SimpleQingQingInformationIdRequest();
                simpleQingQingInformationIdRequest.qingqingInformationId = data.qingqingInformationId;
                newProtoReq(CommonUrl.TEACHER_DELETE_TEACH_EXPERIENCE_CASE.url())
                        .setSendMsg(simpleQingQingInformationIdRequest).setRspListener(
                                new ProtoListener(ProtoBufResponse.SimpleResponse.class) {
                                    
                                    @Override
                                    public void onDealResult(Object result) {
                                        if (couldOperateUI()) {
                                            mInformationList.remove(data);
                                            mInformationListAdapter
                                                    .notifyDataSetChanged();
                                        }
                                    }
                                })
                        .req();
            }
            
            private void showShare(InformationProto.InformationDetailForList data) {
                ShareShow share = new ShareShow(getActivity());
                
                String title = data.article.title + "-轻轻家教头条";
                
                String content = BaseData
                        .getClientType() == AppCommon.AppType.qingqing_teacher
                                ? "来自于" + data.createUser.nick : "来自轻轻家教";
                
                String shareUrl = getInformationUrl(data.qingqingInformationId,
                        data.article);
                shareUrl = UrlUtil.addParamToUrl(shareUrl, "chnid",
                        BaseData.getClientType() == AppCommon.AppType.qingqing_teacher
                                ? "tr_share_news" : "stu_share_news");
                
                String chnid = BaseData
                        .getClientType() == AppCommon.AppType.qingqing_teacher
                                ? "news_tr_xinde" : "news_stu_xinde";
                
                String shareIcon = "";
                if (data.article.articleType == ArticleProto.ArticleType.txt_image_article_type) {
                    if (data.article.contentImages.length > 0) {
                        shareIcon = ImageUrlUtil.getDefaultCropImg(
                                data.article.contentImages[0].imagePath);
                    }
                }
                else if (data.article.articleType == ArticleProto.ArticleType.out_link_article_type
                        || data.article.articleType == ArticleProto.ArticleType.rich_html_article_type) {
                    if (data.article.headImage != null) {
                        shareIcon = ImageUrlUtil
                                .getDefaultCropImg(data.article.headImage.imagePath);
                    }
                }
                
                share.setShareLink(shareUrl, chnid).setShareTitle(title)
                        .setShareContent(content).setShareIcon(shareIcon)
                        .setDefaultShareIcon(R.drawable.share).show();
            }
        }
    }
}

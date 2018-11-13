package com.qingqing.project.offline.order;

import java.util.ArrayList;

import com.qingqing.api.proto.v1.app.AppCommon;
import com.qingqing.base.BaseApplication;
import com.qingqing.base.core.UserBehaviorLogManager;
import com.qingqing.base.data.BaseData;
import com.qingqing.base.share.ShareDialogBuilder;
import com.qingqing.base.share.ShareShow;
import com.qingqing.project.offline.R;

import android.app.Activity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;

/**
 * 订单分享弹框
 *
 * Created by lihui on 2017/1/23.
 */

public class ShareOrderShow extends ShareShow {
    private String mShowTitle;
    private int mShowTitleColor;
    private String mShowContent;
    private int mShowContentColor;
    
    public ShareOrderShow(Activity activity, String sharePageId) {
        super(activity, sharePageId);
    }
    
    public ShareOrderShow(Activity activity) {
        super(activity);
    }
    
    public ShareOrderShow setShowTitle(String showTitle) {
        this.mShowTitle = showTitle;
        
        needRebuild = true;
        return this;
    }
    
    public ShareOrderShow setShowContent(String showContent) {
        this.mShowContent = showContent;
        
        needRebuild = true;
        return this;
    }
    
    public ShareOrderShow setShowTitleColor(int color) {
        mShowTitleColor = color;
        
        needRebuild = true;
        return this;
    }
    
    public ShareOrderShow setShowContentColor(int color) {
        mShowContentColor = color;
        
        needRebuild = true;
        return this;
    }
    
    private CharSequence prepareShowTitle() {
        
        final String title = mShowTitle + "\n\n" + mShowContent;
        
        if (mShareAssist.getRefActivity() == null) {
            return title;
        }
        
        SpannableString spanString = new SpannableString(title);
        // 设置字体大小和颜色
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(mShareAssist
                .getRefActivity().getResources().getColor(R.color.gray_dark_deep));
        spanString.setSpan(colorSpan, 0, mShowTitle.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        
        AbsoluteSizeSpan sizeSpan = new AbsoluteSizeSpan(mShareAssist.getRefActivity()
                .getResources().getDimensionPixelSize(R.dimen.font_size_16));
        spanString.setSpan(sizeSpan, 0, mShowTitle.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        
        // 设置字体大小和颜色
        ForegroundColorSpan colorSpan2 = new ForegroundColorSpan(
                mShareAssist.getRefActivity().getResources().getColor(R.color.gray_dark));
        spanString.setSpan(colorSpan2, mShowTitle.length() + 1, title.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        
        AbsoluteSizeSpan sizeSpan2 = new AbsoluteSizeSpan(mShareAssist.getRefActivity()
                .getResources().getDimensionPixelSize(R.dimen.font_size_12));
        spanString.setSpan(sizeSpan2, mShowTitle.length() + 1, title.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        
        return spanString;
    }
    
    @Override
    public void show() {
        if (mShareAssist.getRefActivity() == null) {
            return;
        }
        mShareAssist.prepareDefaultShareIcon();
        
        if (TextUtils.isEmpty(mShowTitle)) {
            mShowTitle = getDefaultTitle();
        }
        if (TextUtils.isEmpty(mShowContent)) {
            mShowContent = getDefaultContent();
        }
        
        if (shareDialog == null || needRebuild) {
            ArrayList<Integer> dataIndex = new ArrayList<>();
            dataIndex.add(0);
            dataIndex.add(2);
            dataIndex.add(5);
            dataIndex.add(7);
            setShareItemData(mShareAssist.createShareItemData(dataIndex));
            
            shareDialog = new ShareDialogBuilder(mShareAssist.getRefActivity())
                    .setShareData(shareDataList, 4)
                    .setChoiceListener(defaultItemClickListener)
                    .setTitle(prepareShowTitle()).build();
        }
        shareDialog.show();
        
        if (!TextUtils.isEmpty(mSharePageId)
                && BaseData.getClientType() == AppCommon.AppType.qingqing_student) {
            UserBehaviorLogManager.INSTANCE().savePageLog(mSharePageId);
        }
    }
    
    private String getDefaultContent() {
        switch (BaseData.getClientType()) {
            case AppCommon.AppType.qingqing_student:
                return BaseApplication.getCtx().getResources()
                        .getString(R.string.order_share_message_student);
            case AppCommon.AppType.qingqing_teacher:
                return BaseApplication.getCtx().getResources()
                        .getString(R.string.order_share_message_teacher);
            case AppCommon.AppType.qingqing_ta:
                return BaseApplication.getCtx().getResources()
                        .getString(R.string.order_share_message_ta);
        }
        return "";
    }
    
    private String getDefaultTitle() {
        switch (BaseData.getClientType()) {
            case AppCommon.AppType.qingqing_student:
                return BaseApplication.getCtx().getResources()
                        .getString(R.string.order_share_title_student);
            case AppCommon.AppType.qingqing_teacher:
                return BaseApplication.getCtx().getResources()
                        .getString(R.string.order_share_title_teacher);
            case AppCommon.AppType.qingqing_ta:
                return BaseApplication.getCtx().getResources()
                        .getString(R.string.order_share_title_ta);
        }
        return "";
    }
}

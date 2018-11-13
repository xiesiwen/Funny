package com.qingqing.project.offline.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qingqing.base.view.shape.ArcView;
import com.qingqing.project.offline.R;

/**
 * 顶部有半圆突出图的 dialog
 * Created by lihui on 2016/5/3.
 */
public class DialogArcImage extends RelativeLayout {
    private ArcView mArcTop;
    private ImageView mIvImage;
    private TextView mTvSubContent;
    private TextView mTvMainContent;
    private View mDivider;
    private TextView mTvButton;

    public DialogArcImage(Context context) {
        this(context, null);
    }

    public DialogArcImage(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(context).inflate(R.layout.dlg_auth_tips, this);
        initView(view);
    }

    private void initView(View view) {
        mArcTop = (ArcView) view.findViewById(R.id.arc_top);
        mIvImage = (ImageView) view.findViewById(R.id.iv_tips_image);
        mTvMainContent = (TextView) view.findViewById(R.id.tv_tips_main_content);
        mTvSubContent = (TextView) view.findViewById(R.id.tv_tips_sub_content);
        mDivider = view.findViewById(R.id.divider_content);
        mTvButton = (TextView) view.findViewById(R.id.tv_click_button);

        setButton(null, null);
    }

    /**
     * 设置顶部的图标
     */
    public void setImageResId(int resId) {
        mIvImage.setImageResource(resId);
    }

    /**
     * 是否显示顶部半圆
     */
    public void setShowArcTop(boolean isShow) {
        mArcTop.setVisibility(isShow ? VISIBLE : GONE);
    }

    /**
     * 设置第一行显示内容
     */
    public void setMainContent(String content) {
        mTvMainContent.setText(content);
    }

    /**
     * 设置第一行内容的颜色
     */
    public void setMainContentColor(int resId) {
        mTvMainContent.setTextColor(getResources().getColor(resId));
    }

    /**
     * 设置第二行显示内容
     */
    public void setSubContent(String content) {
        mTvSubContent.setVisibility(TextUtils.isEmpty(content) ? GONE : VISIBLE);
        mTvSubContent.setText(content);
    }

    /**
     * 设置按钮的文字
     */
    public void setButton(String content, OnClickListener listener) {
        if (TextUtils.isEmpty(content)) {
            mDivider.setVisibility(GONE);
            mTvButton.setVisibility(GONE);
        } else {
            mDivider.setVisibility(VISIBLE);
            mTvButton.setVisibility(VISIBLE);

            mTvButton.setText(content);
            mTvButton.setOnClickListener(listener);
        }
    }

    /**
     * 设置按钮的背景颜色
     */
    public void setButtonBgColor(int pressedResId, int selectedResId, int normalResId) {
        StateListDrawable stateListDrawable = (StateListDrawable) mTvButton.getBackground();
        DrawableContainer.DrawableContainerState drawableContainerState = (DrawableContainer.DrawableContainerState) stateListDrawable.getConstantState();
        Drawable[] children = drawableContainerState.getChildren();

        ((GradientDrawable) children[0]).setColor(getResources().getColor(pressedResId));
        ((GradientDrawable) children[1]).setColor(getResources().getColor(selectedResId));
        ((GradientDrawable) children[2]).setColor(getResources().getColor(normalResId));

        StateListDrawable bg = new StateListDrawable();
        bg.addState(new int[]{android.R.attr.state_pressed}, children[0]);
        bg.addState(new int[]{android.R.attr.state_selected}, children[1]);
        bg.addState(new int[]{}, children[2]);

        mTvButton.setBackgroundDrawable(bg);
    }

    /**
     * 设置按钮的文字颜色
     */
    public void setButtonTextColor(int resId) {
        mTvButton.setTextColor(getResources().getColor(resId));
    }
}

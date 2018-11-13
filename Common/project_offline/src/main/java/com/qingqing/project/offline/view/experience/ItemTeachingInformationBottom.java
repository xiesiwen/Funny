package com.qingqing.project.offline.view.experience;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qingqing.project.offline.R;

/**
 * 教学心得底部栏 item
 * Created by lihui on 2016/6/17.
 */
public class ItemTeachingInformationBottom extends LinearLayout {
    private ImageView mIvImage;
    private TextView mTvContent;

    public ItemTeachingInformationBottom(Context context) {
        this(context, null);
    }

    public ItemTeachingInformationBottom(Context context, AttributeSet attrs) {
        super(context, attrs);

        View view = LayoutInflater.from(context).inflate(R.layout.item_teaching_information_bottom, this);
        initView(view);
    }

    private void initView(View view) {


        mIvImage = (ImageView) view.findViewById(R.id.iv_image);
        mTvContent = (TextView) view.findViewById(R.id.tv_content);

    }

    public void setImage(int resId) {
        mIvImage.setImageResource(resId);
    }

    public void setContent(int resId) {
        mTvContent.setText(resId);
    }
}

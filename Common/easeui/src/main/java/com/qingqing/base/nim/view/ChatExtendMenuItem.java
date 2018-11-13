package com.qingqing.base.nim.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easemob.easeui.R;

/**
 * Created by huangming on 2016/8/23.
 */
public class ChatExtendMenuItem extends RelativeLayout {
    
    private TextView mTitleView;
    private ImageView mIconView;
    
    public ChatExtendMenuItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        
        mTitleView = (TextView) findViewById(R.id.tv_title);
        mIconView = (ImageView) findViewById(R.id.img_icon);
    }
    
    public void setTitle(int resId) {
        if (mTitleView != null && resId > 0) {
            mTitleView.setText(resId);
        }
    }
    
    public void setIcon(int resId) {
        if (mIconView != null && resId > 0) {
            mIconView.setImageResource(resId);
        }
    }
}

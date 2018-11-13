package com.qingqing.project.offline.groupchat;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import com.qingqing.base.activity.HtmlActivity;
import com.qingqing.base.constant.BaseParamKeys;
import com.qingqing.base.log.Logger;


/**
 * 自带url的span
 */
 class CustomUrlSpan extends ClickableSpan {

    String url;

    Context mContext;


    public CustomUrlSpan( Context mContext,String url) {
        this.url = url;
        this.mContext = mContext;
    }

    @Override
    public void onClick(View widget) {
        try {
            if (url.startsWith("www")) {
                url = "http://" + url;
            }
            mContext.startActivity(
                    new Intent(mContext, HtmlActivity.class)
                            .putExtra(
                                    BaseParamKeys.PARAM_STRING_URL,
                                    url));
        } catch (Exception e) {
            Logger.d("launch error");
        }
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        ds.setUnderlineText(true);
        ds.setColor(ContextCompat.getColor(mContext, com.qingqing.qingqingbase.R.color.primary_blue));
    }


}

package com.qingqing.project.offline.groupchat;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qingqing.base.view.AsyncImageViewV2;
import com.qingqing.qingqingbase.R;

/**
 * Created by dubo on 15/12/22.
 */
public class ChatCircleHeadView extends LinearLayout {

    private AsyncImageViewV2 mHeadImageView;
    private TextView mNIck;

    public ChatCircleHeadView(Context context) {
        super(context);
    }

    public void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.views_chat_circle_head, this, true);
        mHeadImageView = (AsyncImageViewV2) findViewById(R.id.chat_head);
        mNIck = (TextView) findViewById(R.id.chat_nick);
    }

}

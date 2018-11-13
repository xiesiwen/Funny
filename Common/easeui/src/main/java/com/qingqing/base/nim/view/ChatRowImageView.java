package com.qingqing.base.nim.view;

import java.io.File;

import com.easemob.easeui.R;
import com.qingqing.base.config.LogicConfig;
import com.qingqing.base.nim.domain.ImageMessageBody;
import com.qingqing.base.nim.domain.Message;
import com.qingqing.base.utils.ImageUrlUtil;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

/**
 * Created by huangming on 2016/8/18.
 */
public class ChatRowImageView extends ChatRowDefaultView {
    
    private BubbleImageViewV2 bubbleImageView;
    
    public ChatRowImageView(Context context, Message message) {
        super(context, message);
    }
    
    @Override
    protected int onInflateReceivedLayout() {
        return R.layout.chat_row_new_received_image;
    }
    
    @Override
    protected int onInflateSentLayout() {
        return R.layout.chat_row_new_sent_image;
    }
    
    @Override
    protected void onFindViewById() {
        super.onFindViewById();
        bubbleImageView = (BubbleImageViewV2) findViewById(R.id.bubble_image);
    }
    
    @Override
    protected void onSetupViewBy(Message message) {
        super.onSetupViewBy(message);
        ImageMessageBody body = (ImageMessageBody) message.getBody();
        getBubbleImageView().setRequiredSize(body.getWidth(), body.getHeight());
        if (getProgressBar() != null) {
            getProgressBar().setVisibility(
                    body.getStatus() == Message.Status.IN_PROGRESS ? VISIBLE : GONE);
        }
        
        final boolean isSendDirect = isSendDirect();
        String localUrl = body.getLocalUrl();
        String remoteUrl = body.getRemoteUrl();
        
        if (isSendDirect && getBubbleLayout() != null) {
            getBubbleLayout().setBackground(null);
        }
        
        if (isSendDirect && !TextUtils.isEmpty(localUrl)) {
            getBubbleImageView().setImageUri(
                    Uri.fromFile(new File(localUrl)),
                    R.drawable.loading_bg,
                    R.drawable.icon_chat04,
                    R.drawable.icon_chat04);
        }
        else if (!TextUtils.isEmpty(remoteUrl)) {
            getBubbleImageView().setImageUri(
                    Uri.parse(ImageUrlUtil.getScaleImg(body.getRemoteUrl(), LogicConfig.BIG_CROP_IMG_WIDTH, LogicConfig.BIG_CROP_IMG_HEIGHT)),
                    R.drawable.loading_bg,
                    R.drawable.icon_chat04,
                    R.drawable.icon_chat04);
        }
        else {
            getBubbleImageView().setImageUrl("", R.drawable.icon_chat04);
        }

    }
    
    private BubbleImageViewV2 getBubbleImageView() {
        return bubbleImageView;
    }
    
}

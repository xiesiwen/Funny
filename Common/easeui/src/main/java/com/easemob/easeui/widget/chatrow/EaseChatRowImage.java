package com.easemob.easeui.widget.chatrow;

import java.io.File;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.ImageMessageBody;
import com.easemob.easeui.R;
import com.easemob.easeui.model.EaseImageCache;
import com.easemob.easeui.utils.EaseCommonUtils;
import com.easemob.easeui.utils.EaseImageUtils;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.AbstractDraweeController;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.view.DraweeHolder;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.image.CloseableStaticBitmap;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.qingqing.base.BaseApplication;
import com.qingqing.base.config.LogicConfig;
import com.qingqing.base.utils.ImageUrlUtil;
import com.qingqing.base.view.BubbleImageView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.schedulers.Schedulers;

public class EaseChatRowImage extends EaseChatRowFile{

    protected BubbleImageView imageView;
    private ImageMessageBody imgBody;

    private DraweeHolder<GenericDraweeHierarchy> mDraweeHolder;
    private EaseChatRowImageListener mEaseChatRowImageListener;

    public EaseChatRowImage(Context context, EMMessage message, int position, BaseAdapter adapter) {
        super(context, message, position, adapter);
    }

    @Override
    protected void onInflatView() {
        inflater.inflate(message.direct == EMMessage.Direct.RECEIVE ? R.layout.ease_row_received_picture : R.layout.ease_row_sent_picture, this);
    }

    @Override
    protected void onFindViewById() {
        percentageView = (TextView) findViewById(R.id.percentage);
        imageView = (BubbleImageView) findViewById(R.id.image);
    }


    @Override
    protected void onSetUpView() {
        imgBody = (ImageMessageBody) message.getBody();
        imageView.setRequiredSize(imgBody.getWidth(), imgBody.getHeight());
        // 接收方向的消息
        if (message.direct == EMMessage.Direct.RECEIVE) {
            if (message.status == EMMessage.Status.INPROGRESS) {
                imageView.setImageResource(R.drawable.icon_chat04);
                setMessageReceiveCallback();
            } else {
                progressBar.setVisibility(View.GONE);
                percentageView.setVisibility(View.GONE);
                imageView.setImageResource(R.drawable.icon_chat04);
                if (imgBody.getLocalUrl() != null) {
                    // String filePath = imgBody.getLocalUrl();
                    String remotePath = imgBody.getRemoteUrl();
                    String filePath = EaseImageUtils.getImagePath(remotePath);
                    String thumbRemoteUrl = imgBody.getThumbnailUrl();
                    String thumbnailPath = EaseImageUtils
                            .getThumbnailImagePath(thumbRemoteUrl);
                    showImageView(thumbnailPath, imageView, filePath, message);
                }
                else {
                    showRemoteImage();
                }
            }
            return;
        }

        String filePath = imgBody.getLocalUrl();
        if (filePath != null) {
            showImageView(EaseImageUtils.getThumbnailImagePath(filePath), imageView,
                    filePath, message);
        }
        else {
            showRemoteImage();
        }
        handleSendMessage();
    }

    // 5.0.5 讲堂显示远程图片
    private void showRemoteImage(){
        if (Build.VERSION.SDK_INT >= 11) {
            this.setLayerType(LAYER_TYPE_SOFTWARE, null);
        }

        GenericDraweeHierarchy hierarchy = new GenericDraweeHierarchyBuilder(
                getResources()).setProgressBarImage(
                getResources().getDrawable(com.qingqing.qingqingbase.R.drawable.loading_bg)).build();

        mDraweeHolder = DraweeHolder.create(hierarchy, getContext());

        String url = ImageUrlUtil.getScaleImg(imgBody.getRemoteUrl(),
                LogicConfig.BIG_CROP_IMG_WIDTH, LogicConfig.BIG_CROP_IMG_HEIGHT);
        ImageRequest imageRequest = ImageRequestBuilder
                .newBuilderWithSource(Uri.parse(url))
                .setAutoRotateEnabled(true).build();
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        final DataSource<CloseableReference<CloseableImage>> dataSource = imagePipeline
                .fetchDecodedImage(imageRequest, BaseApplication.getCtx());
        AbstractDraweeController controller = Fresco.newDraweeControllerBuilder()
                .setOldController(mDraweeHolder.getController())
                .setImageRequest(imageRequest)
                .setControllerListener(new BaseControllerListener<ImageInfo>() {
                    @Override
                    public void onFinalImageSet(String id, ImageInfo imageInfo,
                                                Animatable animatable) {
                        super.onFinalImageSet(id, imageInfo, animatable);

                        CloseableReference<CloseableImage> imageCloseableReference = null;
                        try {
                            imageCloseableReference = dataSource.getResult();
                            if (imageCloseableReference != null) {
                                CloseableImage image = imageCloseableReference.get();
                                if (image != null
                                        && image instanceof CloseableStaticBitmap) {
                                    CloseableStaticBitmap closeableStaticBitmap = (CloseableStaticBitmap) image;
                                    Bitmap bitmap = closeableStaticBitmap
                                            .getUnderlyingBitmap();
                                    if (bitmap != null) {
                                        imageView.setRequiredSize(bitmap.getWidth(), bitmap.getHeight());
                                        imageView.setImageBitmap(bitmap);
                                    }
                                }
                            }
                        } finally {
                            dataSource.close();
                            CloseableReference.closeSafely(imageCloseableReference);
                        }
                    }
                }).build();
        mDraweeHolder.setController(controller);
        imageView.setImageDrawable(mDraweeHolder.getTopLevelDrawable());
    }

    @Override
    protected void onBubbleLayoutBg() {
        // do nothing
    }

    @Override
    protected void onUpdateView() {
        super.onUpdateView();
    }

    @Override
    protected void onBubbleClick() {
        if (mEaseChatRowImageListener != null) {
            mEaseChatRowImageListener.showImage(message);
        }
//        Intent intent = new Intent(context, ShowBigImageActivity.class);
//        File file = new File(imgBody.getLocalUrl());
//        if (file.exists()) {
//            Uri uri = Uri.fromFile(file);
//            intent.putExtra("uri", uri);
//        } else {
//            // The local full size pic does not exist yet.
//            // ShowBigImage needs to download it from the server
//            // first
//            intent.putExtra("secret", imgBody.getSecret());
//            intent.putExtra("remotepath", imgBody.getRemoteUrl());
//        }
        if (message != null && message.direct == EMMessage.Direct.RECEIVE && !message.isAcked
                && message.getChatType() != EMMessage.ChatType.GroupChat) {
            try {
                EMChatManager.getInstance().ackMessageRead(message.getFrom(), message.getMsgId());
                message.isAcked = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
//        context.startActivity(intent);
    }

    public EaseChatRowImage setShowImageListener(EaseChatRowImageListener listener) {
        mEaseChatRowImageListener = listener;
        return this;
    }
    
    /**
     * load image into image view
     *
     * @return the image exists or not
     */
    private boolean showImageView(final String thumbernailPath, final ImageView iv, final String localFullSizePath,final EMMessage message) {
        // first check if the thumbnail image already loaded into cache
        ImageMessageBody imageMessageBody = (ImageMessageBody) message.getBody();
        Bitmap bitmap = EaseImageCache.getInstance().get(thumbernailPath);
        if (bitmap != null) {
            // thumbnail image is already loaded, reuse the drawable
            iv.setImageBitmap(bitmap);
            return true;
        } else {
            new AsyncTask<Object, Void, Bitmap>() {

                @Override
                protected Bitmap doInBackground(Object... args) {
                    File file = new File(thumbernailPath);
                    if (file.exists()) {
                        return EaseImageUtils.decodeScaleImage(thumbernailPath, 160, 160);
                    } else {
                        if (message.direct == EMMessage.Direct.SEND) {
                            return EaseImageUtils.decodeScaleImage(localFullSizePath, 160, 160);
                        } else {
                            return null;
                        }
                    }
                }

                protected void onPostExecute(Bitmap image) {
                    if (image != null) {
                        iv.setImageBitmap(image);
                        EaseImageCache.getInstance().put(thumbernailPath, image);
                    } else {
                        if (message.status == EMMessage.Status.FAIL) {
                            if (EaseCommonUtils.isNetWorkConnected(activity)) {
                                Observable.create(new ObservableOnSubscribe<Object>() {
                                    @Override
                                    public void subscribe(@NonNull ObservableEmitter<Object> e) throws Exception {
                                        EMChatManager.getInstance().asyncFetchMessage(message);
                                        e.onComplete();
                                    }
                                }).subscribeOn(Schedulers.computation()).subscribe();
                            }
                        }

                    }
                }
            }.execute();

            return true;
        }
    }
    
    public interface EaseChatRowImageListener {
        void showImage(EMMessage message);
    }
}

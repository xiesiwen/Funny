package com.qingqing.base.nim.domain.uploaders;

import com.qingqing.api.proto.v1.ImageProto;
import com.qingqing.base.core.UploadManager;
import com.qingqing.base.interfaces.Observer;
import com.qingqing.base.nim.domain.ImageMessageBody;
import com.qingqing.base.nim.exception.UploadError;

import java.io.File;

/**
 * Created by huangming on 2016/8/25.
 */
public class ImageMessageUploader extends FileMessageUploader<ImageMessageBody> {
    
    @Override
    public void upload(ImageMessageBody body, final Observer<String> callback) {
        UploadManager.INSTANCE().uploadImgV2(
                ImageProto.ImageUploadType.chat_upload_type, 1,
                new File(body.getLocalUrl()), new UploadManager.UploadProtoImgListener() {
                    @Override
                    public void onUploadImgDone(int tag, long picId, String picPath) {
                        if (callback != null) {
                            callback.onNext(picPath);
                        }
                    }
                    
                    @Override
                    public void onUploadDone(int tag, boolean ret) {
                        if (ret) {
                            if (callback != null) {
                                callback.onCompleted();
                            }
                        }
                        else {
                            
                            if (callback != null) {
                                callback.onError(
                                        new UploadError("ImageMessageUploader failed"));
                            }
                        }
                    }
                });
    }
}

package com.qingqing.base.nim.domain.uploaders;

import com.qingqing.api.proto.v1.ImageProto;
import com.qingqing.base.core.UploadManager;
import com.qingqing.base.interfaces.Observer;
import com.qingqing.base.nim.domain.AudioMessageBody;

import java.io.File;

/**
 * Created by huangming on 2016/8/25.
 *
 * web.file.myqcloud.com/files/v1/[appid]/[bucket_name]
 *
 * upload
 *
 * POST /files/v1/[appid]/[bucket_name]/[file_path]
 *
 * Host: web.file.myqcloud.com
 * 
 * Content-Type: multipart/form-data
 * 
 * Content-Length: [发送内容的长度 length (Byte)]
 * 
 * Authorization: [生成的签名字符串]
 * 
 * [multipart/form-data 格式内容]
 *
 * /files/v1/[appid]/[bucket_name]/[file_path]
 */
public class AudioMessageUploader extends FileMessageUploader<AudioMessageBody> {
    
    @Override
    public void upload(final AudioMessageBody body, final Observer<String> callback) {
        UploadManager.INSTANCE().uploadTencentCloudAudio(
                ImageProto.ImageUploadType.chat_upload_type, -1,
                body.getLength(), new File(body.getLocalUrl()),
                new UploadManager.UploadTencentCloudFileListener() {
                    @Override
                    public void onUploadTencentCloudFileDone(int tag,
                                                             String resourcePath) {
                        callback.onNext(resourcePath);
                        callback.onCompleted();
                    }

                    @Override
                    public void onUploadDone(int tag, boolean ret) {
                        if (!ret) {
                            callback.onError(new Throwable("upload failed"));
                        }
                    }
                });
    }
    
}

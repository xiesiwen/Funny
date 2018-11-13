package com.qingqing.base.nim.domain.uploaders;

import com.qingqing.base.interfaces.Observer;
import com.qingqing.base.nim.domain.FileMessageBody;
import com.qingqing.base.nim.exception.UploadError;

/**
 * Created by huangming on 2016/8/25.
 */
class ErrorUploader extends FileMessageUploader<FileMessageBody> {
    
    @Override
    public void upload(FileMessageBody body, Observer<String> callback) {
        if (callback != null) {
            callback.onError(new UploadError("ErrorUploader : Unknown Message Body"));
        }
    }
}

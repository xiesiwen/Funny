package com.qingqing.base.nim.domain.uploaders;

import com.qingqing.base.interfaces.Observer;
import com.qingqing.base.nim.domain.AudioMessageBody;
import com.qingqing.base.nim.domain.FileMessageBody;
import com.qingqing.base.nim.domain.ImageMessageBody;

/**
 * Created by huangming on 2016/8/19.
 */
public abstract class FileMessageUploader<T extends FileMessageBody> {
    
    public abstract void upload(T body, Observer<String> callback);
    
    public static FileMessageUploader getUploader(FileMessageBody body) {
        if (body instanceof ImageMessageBody) {
            return new ImageMessageUploader();
        }
        else if (body instanceof AudioMessageBody) {
            return new AudioMessageUploader();
        }
        return new ErrorUploader();
    }
    
}

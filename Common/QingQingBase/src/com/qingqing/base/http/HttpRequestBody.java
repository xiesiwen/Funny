package com.qingqing.base.http;

import android.text.TextUtils;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * Created by wangxiaxin on 2017/12/28.
 *
 * http 请求体
 *
 *
 * content type :
 *
 * text/html ： HTML格式<br>
 * text/plain ：纯文本格式<br>
 * text/xml ： XML格式<br>
 * image/gif ：gif图片格式<br>
 * image/jpeg ：jpg图片格式<br>
 * image/png：png图片格式<br>
 *
 * application/xhtml+xml ：XHTML格式<br>
 * application/xml ： XML数据格式<br>
 * application/atom+xml：Atom XML聚合格式<br>
 * application/json ： JSON数据格式<br>
 * application/pdf ：pdf格式<br>
 * application/msword ： Word文档格式<br>
 * application/octet-stream ： 二进制流数据（如常见的文件下载）<br>
 * application/x-www-form-urlencoded ：<br>
 * <form encType=””>中默认的encType，form表单数据被编码为key/value格式发送到服务器（表单默认的提交数据的格式）
 * multipart/form-data ： 需要在表单中进行文件上传时，就需要使用该格式<br>
 */
public class HttpRequestBody {
    
    private RequestBody okRequestBody;
    private MultipartBody.Builder multiPartBuilder;
    
    HttpRequestBody() {}
    
    static RequestBody generate(HttpRequestBody httpRequestBody) {
        if(httpRequestBody == null){
            return RequestBody.create(MediaType.parse("text/plain"),
                    "".getBytes());
        }

        if (httpRequestBody.okRequestBody == null) {
            if (httpRequestBody.multiPartBuilder != null) {
                httpRequestBody.okRequestBody = httpRequestBody.multiPartBuilder.build();
            }
            else {
                httpRequestBody.okRequestBody = RequestBody.create(MediaType.parse("text/plain"),
                        "".getBytes());
            }
        }
        return httpRequestBody.okRequestBody;
    }
    
    public HttpRequestBody addFormData(String key, String value) {
        if (multiPartBuilder != null) {
            multiPartBuilder.addFormDataPart(key, value);
        }
        return this;
    }
    
    public HttpRequestBody addFormFile(String key, String fileName, File file) {
        if (multiPartBuilder != null && !TextUtils.isEmpty(key)
                && !TextUtils.isEmpty(fileName) && file != null) {
            multiPartBuilder.addFormDataPart(key, fileName,
                    RequestBody.create(null, file));
        }
        return this;
    }

    public HttpRequestBody addFormFile(String key, File file) {
        if (file != null) {
            addFormFile(key, file.getName(), file);
        }
        return this;
    }
    
    private RequestBody createRequestBody(Object body, String contentType) {
        // OkHttp内部默认的的判断逻辑是POST 不能为空，这里做了规避
        if (body != null) {
            if (body instanceof byte[]) {
                return RequestBody.create(MediaType.parse(contentType), (byte[]) body);
            }
            else if (body instanceof String) {
                return RequestBody.create(MediaType.parse(contentType), (String) body);
            }
            else if (body instanceof File) {
                return RequestBody.create(MediaType.parse(contentType), (File) body);
            }
        }
        return RequestBody.create(MediaType.parse(contentType), "".getBytes());
    }
    
    public static HttpRequestBody fromByte(byte[] bytes, String contentType) {
        HttpRequestBody requestBody = new HttpRequestBody();
        requestBody.okRequestBody = RequestBody.create(MediaType.parse(contentType),
                bytes);
        return requestBody;
    }
    
    public static HttpRequestBody fromString(String str, String contentType) {
        HttpRequestBody requestBody = new HttpRequestBody();
        requestBody.okRequestBody = RequestBody.create(MediaType.parse(contentType), str);
        return requestBody;
    }
    
    public static HttpRequestBody fromString(String str) {
        return fromString(str, "text/plain");
    }
    
    public static HttpRequestBody fromFile(File file, String contentType) {
        HttpRequestBody requestBody = new HttpRequestBody();
        requestBody.okRequestBody = RequestBody.create(MediaType.parse(contentType),
                file);
        return requestBody;
    }
    
    public static HttpRequestBody fromFormData() {
        HttpRequestBody requestBody = new HttpRequestBody();
        MultipartBody.Builder bodyBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
        requestBody.multiPartBuilder = bodyBuilder;
        return requestBody;
    }
}

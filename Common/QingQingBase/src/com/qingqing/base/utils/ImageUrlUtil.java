package com.qingqing.base.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;

import com.facebook.binaryresource.BinaryResource;
import com.facebook.binaryresource.FileBinaryResource;
import com.facebook.cache.common.CacheKey;
import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.datasource.DataSubscriber;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.qingqing.api.proto.v1.UserProto;
import com.qingqing.base.BaseApplication;
import com.qingqing.base.config.CommonUrl;
import com.qingqing.base.config.LogicConfig;
import com.qingqing.base.dns.DomainConfig;
import com.qingqing.base.log.Logger;
import com.qingqing.base.time.NetworkTime;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.FileChannel;

/**
 * @author huangming
 * @date 2015-7-9
 */
public class ImageUrlUtil {
    /**
     * 缩放
     */
    public final static String IMG_SCALE = "rs";
    /**
     * 剪切
     */
    public final static String IMG_CROP = "cp";
    /**
     * 不处理
     */
    public final static String IMG_EMPTY = "";
    
    /** 小头像的配置 rs_200x200 */
    private static final String DEFAULT_SMALL_ICON_CONFIG = IMG_SCALE + "_"
            + LogicConfig.HEAD_DEFAULT_SMALL_WIDTH + "x"
            + LogicConfig.HEAD_DEFAULT_SMALL_HEIGHT + "/";
    
    /** 一般裁切图的配置 cp_200x200 */
    private static final String DEFAULT_CROP_IMG_CONFIG = IMG_CROP + "_"
            + LogicConfig.DEFAULT_CROP_IMG_WIDTH + "x"
            + LogicConfig.DEFAULT_CROP_IMG_HEIGHT + "/";
    
    /** 大的裁切图的配置 cp_400x400 */
    private static final String BIG_CROP_IMG_CONFIG = IMG_CROP + "_"
            + LogicConfig.BIG_CROP_IMG_WIDTH + "x" + LogicConfig.BIG_CROP_IMG_HEIGHT
            + "/";
    
    private static final String DEFAULT_OPT_FORMAT = ".webp";
    
    public static final int FRESCO_CACHE_TYPE_DISK = 10086;
    
    public static final int FRESCO_CACHE_TYPE_MEMORY = 100010;
    
    /** 获取头像url */
    public static String getHeadImg(UserProto.SimpleUserInfo userInfo) {
        if (userInfo == null)
            return "";
        
        return convertImgUrl(userInfo.newHeadImage, DEFAULT_SMALL_ICON_CONFIG, true);
    }
    
    /** 获取头像url */
    public static String getHeadImg(UserProto.SimpleUserInfoV2 userInfo) {
        if (userInfo == null)
            return "";
        
        return convertImgUrl(userInfo.newHeadImage, DEFAULT_SMALL_ICON_CONFIG, true);
    }
    
    /** 获取头像url */
    public static String getHeadImg(String path) {
        return getHeadImg(path, true);
    }
    
    public static String getHeadImg(String path, boolean needWebp) {
        return convertImgUrl(path, DEFAULT_SMALL_ICON_CONFIG, true, needWebp);
    }
    
    /** 获取默认正方形裁切图 */
    public static String getDefaultCropImg(String path) {
        return convertImgUrl(path, DEFAULT_CROP_IMG_CONFIG, true);
    }
    
    /** 获取大的正方形裁切图 */
    public static String getBigCropImg(String path) {
        return convertImgUrl(path, BIG_CROP_IMG_CONFIG, true);
    }
    
    /** 获取自定义的裁切图 */
    public static String getCropImg(String path, int cropWidth, int cropHeight) {
        final String config = IMG_CROP + "_" + cropWidth + "x" + cropHeight + "/";
        return convertImgUrl(path, config, true);
    }
    
    /** 获取自定义的缩放图 */
    public static String getScaleImg(String path, int scaleWidth, int scaleHeight) {
        final String config = IMG_SCALE + "_" + scaleWidth + "x" + scaleHeight + "/";
        return convertImgUrl(path, config, true);
    }
    
    /** 获取url原图 */
    public static String getOriginImg(String imgPath) {
        return getOriginImg(imgPath, true);
    }
    
    public static String getOriginImg(String imgPath, boolean needWebp) {
        return convertImgUrl(imgPath, IMG_EMPTY, true, needWebp);
    }
    
    private static String convertImgUrl(String imgPath, String placeholder,
            boolean isPlace) {
        return convertImgUrl(imgPath, placeholder, isPlace, true);
    }
    
    private static String convertImgUrl(String imgPath, String placeholder,
            boolean isPlace, boolean needWebp) {
        
        if (TextUtils.isEmpty(imgPath)) {
            return IMG_EMPTY;
        }
        
        String url;
        if (isPlace && placeholder != null) {
            url = CommonUrl.IMAGE_URL.url().url() + imgPath.replace("{0}", placeholder);
        }
        else {
            url = CommonUrl.IMAGE_URL.url().url() + imgPath;
        }
        
        if (needWebp && !url.endsWith(DEFAULT_OPT_FORMAT))
            url += DEFAULT_OPT_FORMAT;
        return url;
    }
    
    /** 剥离图片url后面的后缀格式，比如.webp */
    public static String removeSuffixFormat(String imgUrl) {
        if (TextUtils.isEmpty(imgUrl))
            return imgUrl;
        
        if (imgUrl.endsWith(DEFAULT_OPT_FORMAT)) {
            int lastDotIdx = imgUrl.lastIndexOf(DEFAULT_OPT_FORMAT);
            return imgUrl.substring(0, lastDotIdx);
        }
        else {
            return imgUrl;
        }
    }
    
    /**
     * 判断图片url 是否可以转换为webp
     *
     * @param imgFullUrl
     *            图片的完整url
     */
    public static boolean couldTransformWebp(String imgFullUrl) {
        return isOurSite(imgFullUrl) && !TextUtils.isEmpty(imgFullUrl)
                && imgFullUrl.startsWith("http") && (imgFullUrl.endsWith(".jpg")
                        || imgFullUrl.endsWith(".jpeg") || imgFullUrl.endsWith(".png"));
    }
    
    /** 将完整的url */
    public static String transformWebp(String imgFullUrl) {
        if (couldTransformWebp(imgFullUrl)) {
            return imgFullUrl + DEFAULT_OPT_FORMAT;
        }
        else {
            return imgFullUrl;
        }
    }
    
    public static boolean isLocalImageUrl(String imgUrl) {
        return !TextUtils.isEmpty(imgUrl) && imgUrl.startsWith("file://");
    }
    
    public static void getImageCacheType(final String url,
            final getImageCacheTypeListener listener) {
        try {
            boolean isInMemory = Fresco.getImagePipeline()
                    .isInBitmapMemoryCache(Uri.parse(url));
            if (isInMemory) {
                listener.inCache(FRESCO_CACHE_TYPE_MEMORY);
            }
            else {
                Fresco.getImagePipeline().isInDiskCache(Uri.parse(url))
                        .subscribe(new DataSubscriber<Boolean>() {
                            @Override
                            public void onNewResult(DataSource<Boolean> dataSource) {
                                if (dataSource.getResult()) {
                                    listener.inCache(FRESCO_CACHE_TYPE_DISK);
                                }
                                else {
                                    onFailure(dataSource);
                                }
                            }
                            
                            @Override
                            public void onFailure(DataSource<Boolean> dataSource) {
                                listener.notInCache();
                            }
                            
                            @Override
                            public void onCancellation(DataSource<Boolean> dataSource) {
                    
                    }
                            
                            @Override
                            public void onProgressUpdate(DataSource<Boolean> dataSource) {
                        
                    }
                        }, CallerThreadExecutor.getInstance());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public interface getImageCacheTypeListener {
        void inCache(int type);
        
        void notInCache();
    }
    
    public static File getImageDiskCacheFile(String url) {
        CacheKey cacheKey = DefaultCacheKeyFactory.getInstance().getEncodedCacheKey(
                ImageRequest.fromUri(Uri.parse(url)), BaseApplication.getCtx());
        File localFile = null;
        if (cacheKey != null) {
            if (ImagePipelineFactory.getInstance().getMainFileCache().hasKey(cacheKey)) {
                BinaryResource resource = ImagePipelineFactory.getInstance()
                        .getMainFileCache().getResource(cacheKey);
                localFile = ((FileBinaryResource) resource).getFile();
            }
            else if (ImagePipelineFactory.getInstance().getSmallImageFileCache()
                    .hasKey(cacheKey)) {
                BinaryResource resource = ImagePipelineFactory.getInstance()
                        .getSmallImageFileCache().getResource(cacheKey);
                localFile = ((FileBinaryResource) resource).getFile();
            }
        }
        return localFile;
    }
    
    // 暂时先都存为webp，fresco缓存的是cnt格式
    public static String saveImageFileFromDiskCache(String url) {
        File localFile = getImageDiskCacheFile(url);
        if (localFile == null) {
            return null;
        }
        File appDir = new File(Environment.getExternalStorageDirectory(), "qingqing");
        if (!appDir.exists()) {
            appDir.mkdirs();
        }
        appDir.setExecutable(true, false);
        File outputFile = new File(appDir, localFile.getName());
        boolean result = false;
        String path = null;
        try {
            FileChannel outputChannel = new FileOutputStream(outputFile).getChannel();
            FileChannel inputChannel = new FileInputStream(localFile).getChannel();
            long count = outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
            inputChannel.close();
            outputChannel.close();
            if (count > 0) {
                if (outputFile.renameTo(new File(outputFile.getPath().substring(0,
                        outputFile.getPath().lastIndexOf(".")) + ".webp"))) {
                    path = outputFile.getPath().substring(0,
                            outputFile.getPath().lastIndexOf(".")) + ".webp";
                }
                else {
                    path = outputFile.getPath();
                }
                result = true;
            }
        } catch (Exception e) {
            Logger.w(e);
            return null;
        }
        if (result) {
            return path;
        }
        else {
            return null;
        }
    }
    
    public static void saveImageFileFromMemoryCache(String url, Context context,
            final saveImageFileFromMemoryCacheListener listener) {
        Fresco.getImagePipeline()
                .fetchDecodedImage(ImageRequest.fromUri(Uri.parse(url)),
                        BaseApplication.getCtx())
                .subscribe(new BaseBitmapDataSubscriber() {
                    @Override
                    protected void onNewResultImpl(Bitmap bitmap) {
                        if (bitmap == null) {
                            listener.onFail();
                        }
                        File appDir = new File(Environment.getExternalStorageDirectory(),
                                "qingqing");
                        if (!appDir.exists()) {
                            appDir.mkdir();
                        }
                        String fileName = NetworkTime.currentTimeMillis() + ".jpg";
                        File file = new File(appDir, fileName);
                        boolean result = false;
                        try {
                            FileOutputStream fos = new FileOutputStream(file);
                            assert bitmap != null;
                            result = bitmap.compress(Bitmap.CompressFormat.JPEG, 100,
                                    fos);
                            fos.flush();
                            fos.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                            listener.onFail();
                        }
                        if (result) {
                            listener.onSuccess(file.getPath());
                        }
                        else {
                            listener.onFail();
                        }
                    }
                    
                    @Override
                    protected void onFailureImpl(
                            DataSource<CloseableReference<CloseableImage>> dataSource) {
                        listener.onFail();
                    }
                }, CallerThreadExecutor.getInstance());
    }
    
    public static boolean isOurSite(String url) {
        try {
            URL _url = new URL(url);
            
            String host = _url.getHost();
            return host.contains(DomainConfig.ROOT);
        } catch (Exception ignore) {}
        return false;
    }
    
    public interface saveImageFileFromMemoryCacheListener {
        void onSuccess(String path);
        
        void onFail();
    }
}

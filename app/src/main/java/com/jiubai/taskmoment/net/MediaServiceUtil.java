package com.jiubai.taskmoment.net;

import android.content.Context;
import android.graphics.Bitmap;

import com.alibaba.sdk.android.AlibabaSDK;
import com.alibaba.sdk.android.callback.InitResultCallback;
import com.alibaba.sdk.android.media.MediaService;
import com.alibaba.sdk.android.media.upload.UploadListener;
import com.alibaba.sdk.android.media.upload.UploadOptions;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.common.UtilBox;

/**
 * 阿里巴巴图片上传工具
 */
public class MediaServiceUtil {
    public static void initMediaService(Context context, InitResultCallback callback) {
        AlibabaSDK.asyncInit(context, callback);
    }

    /**
     * 上传图片
     *
     * @param bitmap    图片bitmap
     * @param listener  回调
     * @param dir       目录
     * @param imageName 图片名
     */
    public static void uploadImage(Bitmap bitmap, String dir, String imageName, UploadListener listener) {
        MediaService mediaService = AlibabaSDK.getService(MediaService.class);
        UploadOptions options = new UploadOptions.Builder().dir(dir)
                .aliases(imageName).build();

        // 上传
        mediaService.upload(UtilBox.bitmap2Bytes(bitmap), imageName,
                Constants.NAMESPACE, options, listener);
    }
}
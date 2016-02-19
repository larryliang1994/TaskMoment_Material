package com.jiubai.taskmoment.presenter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

import com.alibaba.sdk.android.media.upload.UploadListener;
import com.alibaba.sdk.android.media.upload.UploadTask;
import com.alibaba.sdk.android.media.utils.FailReason;
import com.jiubai.taskmoment.App;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.config.Urls;
import com.jiubai.taskmoment.net.BaseUploadListener;
import com.jiubai.taskmoment.net.MediaServiceUtil;
import com.jiubai.taskmoment.common.UtilBox;
import com.jiubai.taskmoment.receiver.UpdateViewEvent;
import com.jiubai.taskmoment.ui.iview.IUploadImageView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by howell on 2015/11/29.
 * UploadPresenter实现类
 */
public class UploadImagePresenterImpl implements IUploadImagePresenter {
    private IUploadImageView iUploadImageView;
    private Context context;
    private int uploadedNum = 0;
    private List<String> pictureList = new ArrayList<>();

    public UploadImagePresenterImpl(Context context, IUploadImageView iUploadImageView) {
        this.context = context;
        this.iUploadImageView = iUploadImageView;
    }

    @Override
    public void doUploadImage(Bitmap bitmap, String dir, final String objectName, final String type) {
        UploadListener listener = new BaseUploadListener() {

            @Override
            public void onUploadFailed(UploadTask uploadTask, FailReason failReason) {
                System.out.println(failReason.getMessage());

                iUploadImageView.onUploadImageResult(Constants.FAILED, "图片上传失败，请重试");
            }

            @Override
            public void onUploadComplete(UploadTask uploadTask) {

                didUploadImageComplete(objectName, type);

                iUploadImageView.onUploadImageResult(Constants.SUCCESS, "");
            }

        };

        MediaServiceUtil.uploadImage(bitmap, dir, objectName, listener);
    }

    public void uploadImages(final List<String> path, final String dir) {
        // 压缩图片
        final Bitmap bitmap = UtilBox.getLocalBitmap(
                path.get(uploadedNum),
                UtilBox.getWidthPixels(context), UtilBox.getHeightPixels(context));

        UploadListener uploadListener = new BaseUploadListener() {
            @Override
            public void onUploadFailed(UploadTask uploadTask, FailReason failReason) {
                iUploadImageView.onUploadImagesResult(Constants.FAILED,
                        "图片上传失败，请重试", pictureList);
            }

            @Override
            public void onUploadComplete(UploadTask uploadTask) {
                // 已上传的图片数加一
                uploadedNum++;

                // 记录已上传的图片的文件名
                pictureList.add(uploadTask.getResult().url);

                if (uploadedNum < path.size()) {
                    // 接着上传下一张图片
                    try {
                        uploadImages(path, dir);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                        iUploadImageView.onUploadImagesResult(Constants.FAILED,
                                "图片上传失败，请重试", pictureList);
                    }
                } else {
                    iUploadImageView.onUploadImagesResult(Constants.SUCCESS, "", pictureList);
                }
            }
        };

        MediaServiceUtil.uploadImage(
                UtilBox.compressImage(bitmap, Constants.SIZE_TASK_IMAGE), dir,
                UtilBox.getMD5Str(Calendar.getInstance().getTimeInMillis() + "") + ".jpg",
                uploadListener);
    }

    @Override
    public void doUploadImages(final List<String> path, final String dir) {
        uploadedNum = 0;
        pictureList.clear();

        uploadImages(path, dir);
    }

    private void didUploadImageComplete(String objectName, String type) {
        SharedPreferences.Editor editor;
        switch (type) {
            case Constants.SP_KEY_COMPANY_BACKGROUND:
                Config.COMPANY_BACKGROUND = Urls.MEDIA_CENTER_BACKGROUND + objectName;

                // 更新时间戳
                Config.TIME = Calendar.getInstance().getTimeInMillis();

                editor = App.sp.edit();
                editor.putString(Constants.SP_KEY_COMPANY_BACKGROUND,
                        Config.COMPANY_BACKGROUND);
                editor.putLong(Constants.SP_KEY_TIME, Config.TIME);
                editor.apply();

                EventBus.getDefault().post(new UpdateViewEvent(Constants.ACTION_CHANGE_BACKGROUND));
                break;

            case Constants.DIR_TASK:

                break;

            case Constants.SP_KEY_PORTRAIT:
                // 更新时间戳
                Config.TIME = Calendar.getInstance().getTimeInMillis();

                Config.PORTRAIT = Urls.MEDIA_CENTER_PORTRAIT + objectName;

                editor = App.sp.edit();
                editor.putString(Constants.SP_KEY_PORTRAIT, Config.PORTRAIT);
                editor.putLong(Constants.SP_KEY_TIME, Config.TIME);
                editor.apply();

                EventBus.getDefault().post(new UpdateViewEvent(Constants.ACTION_CHANGE_PORTRAIT));
                break;
        }
    }
}
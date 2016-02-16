package com.jiubai.taskmoment.ui.iview;

import java.util.List;

/**
 * Created by howell on 2015/11/29.
 * UploadView接口
 */
public interface IUploadImageView {
    void onUploadImageResult(String result, String info);

    void onUploadImagesResult(String result, String info, List<String> pictureList);
}

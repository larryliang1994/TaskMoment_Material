package com.jiubai.taskmoment.presenter;

import android.content.Context;

import java.util.List;

/**
 * Created by howell on 2015/11/30.
 * TaskPresenter接口
 */
public interface ITaskPresenter {
    void doPublishTask(String grade, String desc, String executor, String supervisor,
                       String auditor, String deadline, String publishTime);

    void doUpdateTask(String taskID, List<String> pictureList);

    void doDeleteTask(Context context, String taskID);
}

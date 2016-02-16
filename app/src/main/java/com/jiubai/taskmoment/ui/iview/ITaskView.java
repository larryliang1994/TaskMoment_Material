package com.jiubai.taskmoment.ui.iview;

/**
 * Created by howell on 2015/11/30.
 * TaskView接口
 */
public interface ITaskView {
    void onPublishTaskResult(String result, String info);

    void onDeleteTaskResult(String result, String info);

    void onUpdateTaskResult(String result, String info);
}

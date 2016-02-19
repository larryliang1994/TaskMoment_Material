package com.jiubai.taskmoment.presenter;

import android.content.Context;
import android.content.Intent;

import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.config.Urls;
import com.jiubai.taskmoment.net.VolleyUtil;
import com.jiubai.taskmoment.receiver.UpdateViewEvent;
import com.jiubai.taskmoment.ui.fragment.TimelineFragment;
import com.jiubai.taskmoment.ui.iview.ITaskView;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by howell on 2015/11/30.
 * TaskPresenter实现类
 */
public class TaskPresenterImpl implements ITaskPresenter {
    private ITaskView iTaskView;

    public TaskPresenterImpl(ITaskView iTaskView) {
        this.iTaskView = iTaskView;
    }

    @Override
    public void doPublishTask(String grade, String desc,
                              String executor, String supervisor, String auditor,
                              String deadline, String publishTime) {
        /**
         *  'p1'         => _post('p1'),//任务级别
         *  'comments'   => _post('comments'),//备注
         *  'works'      => _post('works'),//图片
         *  'ext1'       => _post('ext1'),//执行者id
         *  'ext2'       => _post('ext2'),//监督者id
         *  'ext3'       => _post('ext3'),//审核者id
         *  'ext4'       => _post('cid'),//所属公司
         *  'time1'      => _post('time1'),//完成期限
         *  'time2'      => _post('time2'),//发布时间
         */
        String[] key = {"p1", "comments", "ext1", "ext2", "ext3",
                "cid", "time1", "time2"};
        String[] value = {grade, desc, executor, supervisor, auditor,
                Config.CID, deadline, publishTime};

        VolleyUtil.requestWithCookie(Urls.PUBLISH_TASK, key, value,
                response -> {
                    try {
                        JSONObject responseJson = new JSONObject(response);

                        String status = responseJson.getString("status");

                        if (!Constants.SUCCESS.equals(status)) {
                            iTaskView.onPublishTaskResult(status, responseJson.getString("info"));
                        } else {
                            iTaskView.onPublishTaskResult(Constants.SUCCESS, responseJson.getString("taskid"));
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                volleyError -> {
                    volleyError.printStackTrace();

                    iTaskView.onPublishTaskResult(Constants.FAILED, "发布失败，请重试");
                });
    }

    @Override
    public void doUpdateTask(String taskID, List<String> pictureList) {
        String[] key = {"taskid", "works"};
        String[] value = {taskID, new JSONArray(pictureList).toString()};

        VolleyUtil.requestWithCookie(Urls.UPDATE_TASK_PICTURE, key, value,
                response -> {

                    try {
                        JSONObject responseJson = new JSONObject(response);

                        String status = responseJson.getString("status");
                        if (!Constants.SUCCESS.equals(status)) {
                            iTaskView.onUpdateTaskResult(status, responseJson.getString("info"));
                        } else {
                            // TODO 都要清空还是只这里清空？
                            // 清空缓存的图片列表
                            TimelineFragment.pictureList = null;

                            iTaskView.onUpdateTaskResult(Constants.SUCCESS, "");
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                volleyError -> {
                    volleyError.printStackTrace();

                    iTaskView.onUpdateTaskResult(Constants.FAILED, "图片上传失败，请重试");
                });
    }

    @Override
    public void doDeleteTask(final Context context, final String taskID) {
        String[] key = {"taskid"};
        String[] value = {taskID};
        VolleyUtil.requestWithCookie(Urls.TASK_DELETE, key, value,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);

                        String status = jsonObject.getString("status");

                        if (Constants.SUCCESS.equals(status)) {

                            EventBus.getDefault().post(
                                    new UpdateViewEvent(Constants.ACTION_DELETE_TASK, taskID));

                            iTaskView.onDeleteTaskResult(status, "");
                        } else {
                            iTaskView.onDeleteTaskResult(status, jsonObject.getString("info"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                volleyError -> {
                    volleyError.printStackTrace();

                    iTaskView.onDeleteTaskResult(Constants.FAILED, "删除失败，请重试");
                });
    }
}
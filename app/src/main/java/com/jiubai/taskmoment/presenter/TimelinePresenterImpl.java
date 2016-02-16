package com.jiubai.taskmoment.presenter;

import com.jiubai.taskmoment.bean.Comment;
import com.jiubai.taskmoment.bean.News;
import com.jiubai.taskmoment.bean.Task;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.config.Urls;
import com.jiubai.taskmoment.net.VolleyUtil;
import com.jiubai.taskmoment.common.UtilBox;
import com.jiubai.taskmoment.ui.iview.ITimelineView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by howell on 2015/11/29.
 * TimelinePresenter实现类
 */
public class TimelinePresenterImpl implements ITimelinePresenter {
    private ITimelineView iTimelineView;

    public TimelinePresenterImpl(ITimelineView iTimelineView) {
        this.iTimelineView = iTimelineView;
    }

    @Override
    public void doPullTimeline(String request_time, final String type) {
        doPullTimeline(request_time, type, "", "", "");
    }

    @Override
    public void doPullTimeline(String request_time, final String type, String mid, String isAudit, String isInvolved) {
        if ("refresh".equals(type)) {
            iTimelineView.onSetSwipeRefreshVisibility(Constants.VISIBLE);
        }

        String[] key = {"len", "cid", "create_time", "mid", "shenhe", "canyu"};
        String[] value = {Constants.LOAD_NUM + "", Config.CID, request_time, mid, isAudit, isInvolved};

        VolleyUtil.requestWithCookie(Urls.GET_TASK_LIST, key, value,
                response -> {

                    JSONObject responseJson;
                    try {
                        responseJson = new JSONObject(response);

                        String responseStatus = responseJson.getString("status");

                        if (Constants.SUCCESS.equals(responseStatus)) {

                            iTimelineView.onPullTimelineResult(Constants.SUCCESS, type, response);

                        } else {
                            iTimelineView.onPullTimelineResult(responseStatus, type,
                                    responseJson.getString("info"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                volleyError -> iTimelineView.onPullTimelineResult(Constants.FAILED, type, "刷新失败，请重试")

        );
    }

    @Override
    public void doGetNews(String msg) throws JSONException {
        JSONObject msgJson = new JSONObject(msg);

        JSONObject contentJson = new JSONObject(msgJson.getString("content"));

        iTimelineView.onGetNewsResult(1, new News(
                msgJson.getString("mid"),
                decodeTask(msgJson.getString("task")),
                contentJson.getString("title"),
                contentJson.getString("content"),
                UtilBox.getDateToString(
                        Long.valueOf(contentJson.getString("time")) * 1000,
                        UtilBox.TIME)));
    }

    /**
     * 解析Task的Json字符串
     *
     * @param taskJson Task的Json字符串
     * @return 解析出来的任务
     * @throws JSONException
     */
    private Task decodeTask(String taskJson) throws JSONException {
        JSONObject obj = new JSONObject(taskJson);

        String id = obj.getString("id");

        String mid = obj.getString("mid");
        String portraitUrl = Urls.MEDIA_CENTER_PORTRAIT + mid + ".jpg";

        String nickname = obj.getString("show_name");

        char p1 = obj.getString("p1").charAt(0);
        String grade = (p1 - 48) == 1 ? "S" : String.valueOf((char) (p1 + 15));

        String desc = obj.getString("comments");
        String executor = obj.getString("ext1");
        String supervisor = obj.getString("ext2");
        String auditor = obj.getString("ext3");

        ArrayList<String> pictures = decodePictureList(obj.getString("works"));
        ArrayList<Comment> comments
                = decodeCommentList(id, obj.getString("member_comment"));

        long deadline = Long.valueOf(obj.getString("time1")) * 1000;
        long publish_time = Long.valueOf(obj.getString("time2")) * 1000;
        long create_time = Long.valueOf(obj.getString("create_time")) * 1000;

        String audit_result = obj.getString("p2");

        return new Task(id, portraitUrl, nickname, mid, grade, desc,
                executor, supervisor, auditor,
                pictures, comments, deadline, publish_time, create_time,
                audit_result, Task.SUCCESS);
    }

    /**
     * 将json解码成list
     *
     * @param pictures 图片Json
     * @return 图片list
     */
    private ArrayList<String> decodePictureList(String pictures) throws JSONException {
        ArrayList<String> pictureList = new ArrayList<>();

        if (pictures != null && !"null".equals(pictures)) {

            JSONArray jsonArray = new JSONArray(pictures);

            for (int i = 0; i < jsonArray.length(); i++) {
                pictureList.add(Urls.MEDIA_CENTER_TASK + jsonArray.getString(i));
            }

        }

        return pictureList;
    }

    /**
     * 将json解码成list
     *
     * @param comments 评论json
     * @return 图片List
     */
    private ArrayList<Comment> decodeCommentList(String taskID, String comments) throws JSONException {
        ArrayList<Comment> commentList = new ArrayList<>();

        if (!"".equals(comments) && !"null".equals(comments)) {
            JSONArray jsonArray = new JSONArray(comments);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = new JSONObject(jsonArray.getString(i));

                String sender = "null".equals(object.getString("send_real_name")) ?
                        object.getString("send_mobile") : object.getString("send_real_name");

                String receiver = "null".equals(object.getString("receiver_real_name")) ?
                        object.getString("receiver_mobile") : object.getString("receiver_real_name");

                if ("null".equals(receiver)) {
                    Comment comment = new Comment(taskID,
                            sender, object.getString("send_id"),
                            object.getString("content"),
                            Long.valueOf(object.getString("create_time")) * 1000);

                    commentList.add(comment);
                } else {
                    Comment comment = new Comment(taskID,
                            sender, object.getString("send_id"),
                            receiver, object.getString("receiver_id"),
                            object.getString("content"),
                            Long.valueOf(object.getString("create_time")) * 1000);

                    commentList.add(comment);
                }
            }
        }

        return commentList;
    }

    @Override
    public void onSetSwipeRefreshVisibility(int visibility) {
        iTimelineView.onSetSwipeRefreshVisibility(visibility);
    }
}
package com.jiubai.taskmoment.presenter;

import android.content.Context;

import com.jiubai.taskmoment.bean.Comment;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.config.Urls;
import com.jiubai.taskmoment.net.VolleyUtil;
import com.jiubai.taskmoment.receiver.UpdateViewEvent;
import com.jiubai.taskmoment.ui.iview.ICommentView;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Locale;

/**
 * Created by howell on 2015/11/29.
 * CommentPresenter实现类
 */
public class CommentPresenterImpl implements ICommentPresenter {
    private ICommentView mICommentView;
    private Context mContext;

    public CommentPresenterImpl(Context context, ICommentView iCommentView) {
        this.mContext = context;
        this.mICommentView = iCommentView;
    }

    @Override
    public void doSendComment(final String taskID, final String receiver,
                              final String receiverID, final String content) {
        String[] key = {"oid", "pmid", "content"};
        String[] value = {taskID, receiverID, content};

        VolleyUtil.requestWithCookie(Urls.SEND_COMMENT, key, value,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);

                        String status = jsonObject.getString("status");

                        if (!Constants.SUCCESS.equals(status)) {
                            System.out.println(response);

                            mICommentView.onSendCommentResult(status, "发送失败，请重试");

                        } else {
                            Comment comment = null;
                            if (!"".equals(receiver)) {
                                comment = new Comment(
                                        taskID, Config.NICKNAME, Config.MID,
                                        receiver, receiverID, content,
                                        Calendar.getInstance(Locale.CHINA).getTimeInMillis());
                            } else {
                                comment = new Comment(
                                        taskID, Config.NICKNAME, Config.MID, content,
                                        Calendar.getInstance(Locale.CHINA).getTimeInMillis());
                            }

                            EventBus.getDefault().post(
                                    new UpdateViewEvent(Constants.ACTION_SEND_COMMENT, taskID, comment));

                            mICommentView.onSendCommentResult(Constants.SUCCESS, "");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                volleyError -> {
                    volleyError.printStackTrace();

                    mICommentView.onSendCommentResult(Constants.FAILED, "评论发送失败，请重试");
                });
    }
}
package com.jiubai.taskmoment.presenter;

import android.content.Context;
import android.content.Intent;

import com.jiubai.taskmoment.bean.Comment;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.config.Urls;
import com.jiubai.taskmoment.net.VolleyUtil;
import com.jiubai.taskmoment.ui.iview.ICommentView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Locale;

/**
 * Created by howell on 2015/11/29.
 * CommentPresenter实现类
 */
public class CommentPresenterImpl implements ICommentPresenter {
    private ICommentView iCommentView;
    private Context context;

    public CommentPresenterImpl(Context context, ICommentView iCommentView) {
        this.context = context;
        this.iCommentView = iCommentView;
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

                            iCommentView.onSendCommentResult(status, "发送失败，请重试");

                        } else {
                            // 发送更新评论广播
                            Intent intent = new Intent(Constants.ACTION_SEND_COMMENT);
                            intent.putExtra("taskID", taskID);

                            String nickname;
                            if ("".equals(Config.NICKNAME) || "null".equals(Config.NICKNAME)) {
                                nickname = "你";
                            } else {
                                nickname = Config.NICKNAME;
                            }

                            if (!"".equals(receiver)) {
                                intent.putExtra("comment", new Comment(
                                        taskID, nickname, Config.MID,
                                        receiver, receiverID, content,
                                        Calendar.getInstance(Locale.CHINA).getTimeInMillis()));
                            } else {
                                intent.putExtra("comment", new Comment(
                                        taskID, nickname, Config.MID, content,
                                        Calendar.getInstance(Locale.CHINA).getTimeInMillis()));
                            }

                            context.sendBroadcast(intent);

                            iCommentView.onSendCommentResult(Constants.SUCCESS, "");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                volleyError -> {
                    volleyError.printStackTrace();

                    iCommentView.onSendCommentResult(Constants.FAILED, "评论发送失败，请重试");
                });
    }
}
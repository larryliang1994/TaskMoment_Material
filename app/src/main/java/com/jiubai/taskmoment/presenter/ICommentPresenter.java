package com.jiubai.taskmoment.presenter;

/**
 * Created by howell on 2015/11/29.
 * CommentPresenter接口
 */
public interface ICommentPresenter {
    void doSendComment(final String taskID, final String receiver,
                       final String receiverID, final String content);
}

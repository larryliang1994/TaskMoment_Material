package com.jiubai.taskmoment.presenter;

import org.json.JSONException;

/**
 * Created by howell on 2015/11/29.
 * PullTimelinePresenter接口
 */
public interface ITimelinePresenter {
    void doPullTimeline(String request_time, String type);

    void doPullTimeline(String request_time, String type, String mid, String isAudit, String isInvolved);

    void doGetNews(String msg) throws JSONException;

    void onSetSwipeRefreshVisibility(int visibility);
}

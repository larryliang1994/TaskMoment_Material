package com.jiubai.taskmoment.ui.iview;

import com.jiubai.taskmoment.bean.News;

/**
 * Created by howell on 2015/11/29.
 * PullTimelineView接口
 */
public interface ITimelineView {
    void onPullTimelineResult(String result, String type, String info);

    void onGetNewsResult(int result, News news);

    void onSetSwipeRefreshVisibility(int visibility);
}

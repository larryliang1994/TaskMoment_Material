package com.jiubai.taskmoment.ui.iview;

/**
 * Created by howell on 2015/11/29.
 * CompanyView接口类
 */
public interface ICompanyView {
    void onGetMyCompanyResult(String result, String info);

    void onGetJoinedCompanyResult(String result, String info);

    void onSetSwipeRefreshVisibility(int visibility);
}
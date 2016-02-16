package com.jiubai.taskmoment.presenter;

/**
 * Created by howell on 2015/11/29.
 * CompanyPresenter接口类
 */
public interface ICompanyPresenter {
    void getMyCompany();

    void getJoinedCompany();

    void onSetSwipeRefreshVisibility(int visibility);
}

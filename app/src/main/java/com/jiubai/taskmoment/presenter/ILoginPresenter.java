package com.jiubai.taskmoment.presenter;

/**
 * Created by howell on 2015/11/28.
 * <p/>
 * LoginPresenter接口
 */
public interface ILoginPresenter {
    void doLogin(String phoneNum, String verifyCode);

    void onSetRotateLoadingVisibility(int visibility);
}

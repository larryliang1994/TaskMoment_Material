package com.jiubai.taskmoment.ui.iview;

/**
 * Created by howell on 2015/11/28.
 *
 * LoginView接口
 */
public interface ILoginView {
    void onLoginResult(boolean result, String info);

    void onSetRotateLoadingVisibility(int visibility);
}
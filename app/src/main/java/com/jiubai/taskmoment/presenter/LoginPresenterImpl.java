package com.jiubai.taskmoment.presenter;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import com.jiubai.taskmoment.App;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.net.SoapUtil;
import com.jiubai.taskmoment.net.VolleyUtil;
import com.jiubai.taskmoment.ui.iview.ILoginView;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by howell on 2015/11/28.
 * LoginPresenter实现类
 */
public class LoginPresenterImpl implements ILoginPresenter {
    private ILoginView mILoginView;

    public LoginPresenterImpl(ILoginView iLoginView) {
        this.mILoginView = iLoginView;
    }

    @Override
    public void doLogin(final String phoneNum, final String verifyCode) {
        new Handler().post(() -> {

            mILoginView.onSetRotateLoadingVisibility(Constants.VISIBLE);

            final String[] soapKey = {"type", "table_name", "feedback_url", "return"};
            final String[] soapValue = {"mobile_login", Config.RANDOM, "", "1"};
            final String[] httpKey = {"mobile", "check_code"};
            final String[] httpValue = {phoneNum, verifyCode};

            VolleyUtil.requestWithSoap(soapKey, soapValue, httpKey, httpValue,
                    response -> {
                        mILoginView.onSetRotateLoadingVisibility(Constants.INVISIBLE);

                        new Thread(() -> {
                            Looper.prepare();
                            try {
                                JSONObject responseJson = new JSONObject(response);

                                if (Constants.SUCCESS.equals(responseJson.getString("status"))) {
                                    handleLoginResponse(responseJson.getString("memberCookie"));

                                    mILoginView.onLoginResult(true,
                                            responseJson.getString("info"));
                                } else {
                                    mILoginView.onLoginResult(false,
                                            responseJson.getString("info"));
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Looper.loop();
                        }).start();
                    },
                    volleyError -> {
                        mILoginView.onSetRotateLoadingVisibility(Constants.INVISIBLE);

                        mILoginView.onLoginResult(false, "登录失败，请重试");
                    });

        });
    }

    /**
     * 处理登录成功后的cookie
     *
     * @param cookie 登录成功后返回的cookie
     */
    private void handleLoginResponse(final String cookie) {

        // 延长cookie可用时间
        SoapUtil.extendCookieLifeTime(cookie);

        // 保存cookie
        SharedPreferences.Editor editor = App.sp.edit();
        if (Config.COOKIE != null) {
            editor.putString(Constants.SP_KEY_COOKIE, Config.COOKIE);
            editor.putString(Constants.SP_KEY_MID, Config.MID);
            editor.putString(Constants.SP_KEY_NICKNAME, Config.NICKNAME);
            editor.putString(Constants.SP_KEY_PORTRAIT, Config.PORTRAIT);
        }

        editor.apply();
    }

    @Override
    public void onSetRotateLoadingVisibility(int visibility) {
        mILoginView.onSetRotateLoadingVisibility(visibility);
    }
}
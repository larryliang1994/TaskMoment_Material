package com.jiubai.taskmoment.presenter;

import android.content.SharedPreferences;
import android.os.Handler;

import com.jiubai.taskmoment.App;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.config.Urls;
import com.jiubai.taskmoment.net.VolleyUtil;
import com.jiubai.taskmoment.ui.iview.ICompanyView;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by howell on 2015/11/29.
 * CompanyPresenter实现类
 */
public class CompanyPresenterImpl implements ICompanyPresenter {
    private ICompanyView iCompanyView;

    public CompanyPresenterImpl(ICompanyView iCompanyView) {
        this.iCompanyView = iCompanyView;
    }

    @Override
    public void getMyCompany() {
        iCompanyView.onSetSwipeRefreshVisibility(Constants.VISIBLE);

        VolleyUtil.requestWithCookie(Urls.MY_COMPANY, null, null,
                response -> {

                    try {
                        JSONObject responseJson = new JSONObject(response);
                        if (Constants.SUCCESS.equals(responseJson.getString("status"))) {
                            iCompanyView.onGetMyCompanyResult(Constants.SUCCESS, response);
                        } else {
                            // cookie有误，清空cookie
                            SharedPreferences.Editor editor = App.sp.edit();
                            editor.remove(Constants.SP_KEY_COOKIE);
                            editor.apply();

                            Config.COOKIE = null;

                            iCompanyView.onGetMyCompanyResult(Constants.EXPIRE,
                                    "登录信息已过期，请重新登录");

                            new Handler().postDelayed(() -> iCompanyView.onSetSwipeRefreshVisibility(Constants.INVISIBLE), 1000);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                volleyError -> {
                    new Handler().postDelayed(() -> iCompanyView.onSetSwipeRefreshVisibility(Constants.INVISIBLE), 1000);

                    iCompanyView.onGetMyCompanyResult(Constants.FAILED,
                            "获取公司列表失败，下拉重试一下吧？");
                });
    }

    @Override
    public void getJoinedCompany() {
        VolleyUtil.requestWithCookie(Urls.MY_JOIN_COMPANY, null, null,
                response -> {
                    try {
                        JSONObject responseJson = new JSONObject(response);
                        if (Constants.SUCCESS.equals(responseJson.getString("status"))) {
                            iCompanyView.onGetJoinedCompanyResult(Constants.SUCCESS, response);

                            new Handler().postDelayed(() -> iCompanyView.onSetSwipeRefreshVisibility(Constants.INVISIBLE), 1000);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                volleyError -> {
                    new Handler().postDelayed(() -> iCompanyView.onSetSwipeRefreshVisibility(Constants.INVISIBLE), 1000);

                    iCompanyView.onGetJoinedCompanyResult(Constants.FAILED,
                            "获取公司列表失败，下拉重试一下吧？");
                });
    }

    @Override
    public void onSetSwipeRefreshVisibility(int visibility) {
        iCompanyView.onSetSwipeRefreshVisibility(visibility);
    }
}
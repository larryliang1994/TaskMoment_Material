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
    private ICompanyView mICompanyView;

    public CompanyPresenterImpl(ICompanyView iCompanyView) {
        this.mICompanyView = iCompanyView;
    }

    @Override
    public void getMyCompany() {
        mICompanyView.onSetSwipeRefreshVisibility(Constants.VISIBLE);

        VolleyUtil.requestWithCookie(Urls.MY_COMPANY, null, null,
                response -> {

                    try {
                        JSONObject responseJson = new JSONObject(response);
                        if (Constants.SUCCESS.equals(responseJson.getString("status"))) {
                            mICompanyView.onGetMyCompanyResult(Constants.SUCCESS, response);
                        } else {
                            // cookie有误，清空cookie
                            SharedPreferences.Editor editor = App.sp.edit();
                            editor.remove(Constants.SP_KEY_COOKIE);
                            editor.apply();

                            Config.COOKIE = null;

                            mICompanyView.onGetMyCompanyResult(Constants.EXPIRE,
                                    "登录信息已过期，请重新登录");

                            new Handler().postDelayed(() -> mICompanyView.onSetSwipeRefreshVisibility(Constants.INVISIBLE), 1000);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                volleyError -> {
                    new Handler().postDelayed(() -> mICompanyView.onSetSwipeRefreshVisibility(Constants.INVISIBLE), 1000);

                    mICompanyView.onGetMyCompanyResult(Constants.FAILED,
                            "获取公司列表失败，刷新一下吧？");
                });
    }

    @Override
    public void getJoinedCompany() {
        VolleyUtil.requestWithCookie(Urls.MY_JOIN_COMPANY, null, null,
                response -> {
                    try {
                        JSONObject responseJson = new JSONObject(response);
                        if (Constants.SUCCESS.equals(responseJson.getString("status"))) {
                            mICompanyView.onGetJoinedCompanyResult(Constants.SUCCESS, response);

                            new Handler().postDelayed(() -> mICompanyView.onSetSwipeRefreshVisibility(Constants.INVISIBLE), 1000);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                volleyError -> {
                    new Handler().postDelayed(() -> mICompanyView.onSetSwipeRefreshVisibility(Constants.INVISIBLE), 1000);

                    mICompanyView.onGetJoinedCompanyResult(Constants.FAILED,
                            "获取公司列表失败，刷新一下吧？");
                });
    }

    @Override
    public void onSetSwipeRefreshVisibility(int visibility) {
        mICompanyView.onSetSwipeRefreshVisibility(visibility);
    }
}
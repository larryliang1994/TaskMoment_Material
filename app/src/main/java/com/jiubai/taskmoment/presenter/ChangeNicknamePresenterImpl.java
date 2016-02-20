package com.jiubai.taskmoment.presenter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.jiubai.taskmoment.App;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.config.Urls;
import com.jiubai.taskmoment.net.VolleyUtil;
import com.jiubai.taskmoment.receiver.UpdateViewEvent;
import com.jiubai.taskmoment.ui.iview.IChangeNicknameView;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by howell on 2015/12/6.
 * ChangeNicknamePresenter实现类
 */
public class ChangeNicknamePresenterImpl implements IChangeNicknamePresenter{
    private IChangeNicknameView mIChangeNicknameView;
    private Context mContext;

    public ChangeNicknamePresenterImpl(Context context, IChangeNicknameView iChangeNicknameView) {
        this.mContext = context;
        this.mIChangeNicknameView = iChangeNicknameView;
    }

    @Override
    public void doChangeNickname(final String newNickname) {
        String[] key = {"real_name"};
        String[] value = {newNickname};

        VolleyUtil.requestWithCookie(Urls.UPDATE_USER_INFO, key, value,
                response -> {
                    try {
                        JSONObject responseObject = new JSONObject(response);
                        String status = responseObject.getString("status");
                        if (Constants.SUCCESS.equals(status)) {

                            Config.NICKNAME = newNickname;

                            EventBus.getDefault().post(new UpdateViewEvent(Constants.ACTION_CHANGE_NICKNAME));

                            SharedPreferences.Editor editor
                                    = App.sp.edit();
                            editor.putString(
                                    Constants.SP_KEY_NICKNAME, newNickname);
                            editor.apply();

                            mIChangeNicknameView.onChangeNicknameResult(status, newNickname);
                        } else {
                            String info = responseObject.getString("info");

                            mIChangeNicknameView.onChangeNicknameResult(status, info);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                volleyError -> {
                    volleyError.printStackTrace();

                    mIChangeNicknameView.onChangeNicknameResult(Constants.FAILED, "修改失败，请重试");
                });
    }
}
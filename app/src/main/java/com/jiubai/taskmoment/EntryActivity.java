package com.jiubai.taskmoment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;

import com.jiubai.taskmoment.common.UtilBox;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.config.Urls;
import com.jiubai.taskmoment.net.MediaServiceUtil;
import com.jiubai.taskmoment.net.VolleyUtil;
import com.jiubai.taskmoment.ui.activity.LoginActivity;
import com.jiubai.taskmoment.ui.activity.MainActivity;
import com.taobao.tae.sdk.callback.InitResultCallback;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UpdateStatus;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EntryActivity extends Activity {

    @Bind(R.id.ll_no_network)
    LinearLayout ll_no_network;

    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.aty_welcome);

        ButterKnife.bind(this);

        UmengUpdateAgent.setUpdateOnlyWifi(false);
        UmengUpdateAgent.setDeltaUpdate(true);
        UmengUpdateAgent.setUpdateAutoPopup(false);
        UmengUpdateAgent.setUpdateListener((updateStatus, updateInfo) -> {
            switch (updateStatus) {
                case UpdateStatus.NoneWifi:
                case UpdateStatus.Yes: // has update
                    UmengUpdateAgent.showUpdateDialog(EntryActivity.this, updateInfo);
                    break;

                case UpdateStatus.No: // has no update
                case UpdateStatus.Timeout: // time out
                    getStart();
                    break;
            }
        });
        UmengUpdateAgent.setDialogListener(status -> {
            switch (status) {
                case UpdateStatus.Update:
                    UtilBox.showSnackbar(this, "开始下载更新");
                    break;
                case UpdateStatus.Ignore:
                case UpdateStatus.NotNow:
                    getStart();
                    break;
            }
        });
        UmengUpdateAgent.update(this);


        dialog = new ProgressDialog(this);
        dialog.setMessage("连接中...");
    }

    // 进入正式页面
    private void getStart() {
        // 初始化多媒体服务
        MediaServiceUtil.initMediaService(getApplicationContext(), new InitResultCallback() {
            @Override
            public void onSuccess() {
                entry();
            }

            @Override
            public void onFailure(int i, String s) {
                ll_no_network.setVisibility(View.VISIBLE);

                changeLoadingState("dismiss");

                UtilBox.showSnackbar(EntryActivity.this, "初始化失败，请重试");
            }
        });
    }

    private void entry() {
        new Handler().postDelayed(() -> {
            if (Config.COOKIE == null || Config.CID == null) {
                startActivity(new Intent(EntryActivity.this, LoginActivity.class));
                finish();
                overridePendingTransition(R.anim.zoom_in_scale,
                        R.anim.zoom_out_scale);
            } else {
                if (!Config.IS_CONNECTED) {
                    changeLoadingState("dismiss");
                    ll_no_network.setVisibility(View.VISIBLE);

                    UtilBox.showSnackbar(EntryActivity.this, R.string.cant_access_network);
                } else {
                    // 获取用户信息
                    getUserInfo();
                }
            }
        }, 500);
    }

    @OnClick(R.id.btn_reconnect)
    public void onClick(View v) {
        new App().initService();
        getStart();
        changeLoadingState("show");
    }

    /**
     * 显示或隐藏旋转进度条
     *
     * @param which show代表显示, dismiss代表隐藏
     */
    private void changeLoadingState(String which) {
        if ("show".equals(which)) {
            runOnUiThread(dialog::show);
        } else if ("dismiss".equals(which)) {
            runOnUiThread(dialog::dismiss);
        }
    }

    private void getUserInfo() {
        VolleyUtil.requestWithCookie(Urls.GET_USER_INFO, null, null,
                response -> {
                    changeLoadingState("dismiss");

                    try {
                        JSONObject object = new JSONObject(response);

                        if (Constants.SUCCESS.equals(object.getString("status"))) {
                            JSONObject data = new JSONObject(object.getString("data"));

                            Config.MID = data.getString("id");
                            Config.NICKNAME = data.getString("real_name");
                            Config.PORTRAIT = Urls.MEDIA_CENTER_PORTRAIT + Config.MID + ".jpg";
                            Config.MOBILE = data.getString("mobile");

                            if ("".equals(Config.NICKNAME) || "null".equals(Config.NICKNAME)) {
                                Config.NICKNAME = "昵称";
                            }

                            SharedPreferences.Editor editor = App.sp.edit();
                            editor.putString(Constants.SP_KEY_MID, Config.MID);
                            editor.putString(Constants.SP_KEY_NICKNAME, Config.NICKNAME);
                            editor.putString(Constants.SP_KEY_PORTRAIT, Config.PORTRAIT);
                            editor.apply();

                            startActivity(new Intent(EntryActivity.this, MainActivity.class));

                            finish();
                            overridePendingTransition(R.anim.zoom_in_scale,
                                    R.anim.zoom_out_scale);
                        } else {
                            ll_no_network.setVisibility(View.VISIBLE);

                            UtilBox.showSnackbar(this, object.getString("info"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                volleyError -> {
                    ll_no_network.setVisibility(View.VISIBLE);

                    changeLoadingState("dismiss");

                    UtilBox.showSnackbar(this, "获取用户信息失败，请重试");
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
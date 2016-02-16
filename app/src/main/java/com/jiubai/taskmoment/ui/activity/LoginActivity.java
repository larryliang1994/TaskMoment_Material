package com.jiubai.taskmoment.ui.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.widget.RippleView;
import com.jiubai.taskmoment.common.SmsContentUtil;
import com.jiubai.taskmoment.common.UtilBox;
import com.jiubai.taskmoment.presenter.GetVerifyCodePresenterImpl;
import com.jiubai.taskmoment.presenter.IGetVerifyCodePresenter;
import com.jiubai.taskmoment.presenter.ILoginPresenter;
import com.jiubai.taskmoment.presenter.LoginPresenterImpl;
import com.jiubai.taskmoment.ui.iview.IGetVerifyCodeView;
import com.jiubai.taskmoment.ui.iview.ILoginView;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 登录页面
 */
public class LoginActivity extends BaseActivity implements ILoginView, IGetVerifyCodeView, TextWatcher,
        RippleView.OnRippleCompleteListener, View.OnClickListener {
    @Bind(R.id.edt_telephone)
    EditText edt_telephone;

    @Bind(R.id.edt_verifyCode)
    EditText edt_verifyCode;

    @Bind(R.id.btn_getVerifyCode)
    Button btn_getVerifyCode;

    @Bind(R.id.btn_login)
    Button btn_login;

    @Bind(R.id.rv_login)
    RippleView rv_login;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    private boolean isCounting = false;
    private ILoginPresenter loginPresenter;
    private IGetVerifyCodePresenter getVerifyCodePresenter;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UtilBox.setStatusBarTint(this, R.color.colorPrimaryDark);

        setContentView(R.layout.aty_login);

        ButterKnife.bind(this);

        initView();
    }

    /**
     * 初始化界面
     */
    private void initView() {
        setSupportActionBar(toolbar);

        edt_telephone.addTextChangedListener(this);
        edt_verifyCode.addTextChangedListener(this);

        setGetVerifyCodeBtnEnable(false);
        setLoginBtnEnable(false);

        getVerifyCodePresenter = new GetVerifyCodePresenterImpl(this);
        loginPresenter = new LoginPresenterImpl(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("请稍候...");

        btn_login.setFocusable(true);
        btn_login.setFocusableInTouchMode(true);
        btn_login.requestFocus();
    }

    private void setGetVerifyCodeBtnEnable(boolean enable) {
        if (enable) {
            btn_getVerifyCode.setOnClickListener(this);
            btn_getVerifyCode.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
        } else {
            btn_getVerifyCode.setOnClickListener(null);
            btn_getVerifyCode.setTextColor(ContextCompat.getColor(this, R.color.gray));
        }
    }

    private void setLoginBtnEnable(boolean enable) {
        if (enable) {
            rv_login.setOnRippleCompleteListener(this);
            btn_login.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        } else {
            rv_login.setOnRippleCompleteListener(null);
            btn_login.setBackgroundColor(ContextCompat.getColor(this, R.color.gray));
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    /**
     * 监听输入
     */
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String content = edt_telephone.getText().toString();

        if (UtilBox.isTelephoneNumber(content)) {
            if (!isCounting) {
                setGetVerifyCodeBtnEnable(true);
            }

            if (edt_verifyCode.getText().toString().length() == 6) {
                setLoginBtnEnable(true);
            } else {
                setLoginBtnEnable(false);
            }
        } else {
            setGetVerifyCodeBtnEnable(false);
            setLoginBtnEnable(false);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    @Override
    public void onClick(View v) {
        if (!Config.IS_CONNECTED) {
            UtilBox.showSnackbar(this, R.string.cant_access_network);
            return;
        }

        getVerifyCodePresenter.doGetVerifyCode(edt_telephone.getText().toString());

        UtilBox.toggleSoftInput(edt_telephone, false);
    }

    @Override
    public void onComplete(RippleView rippleView) {

        if (!Config.IS_CONNECTED) {
            UtilBox.showSnackbar(this, R.string.cant_access_network);
            return;
        }

        loginPresenter.doLogin(edt_telephone.getText().toString(),
                edt_verifyCode.getText().toString());

        UtilBox.toggleSoftInput(edt_verifyCode, false);
    }

    /**
     * 登录结果回调
     *
     * @param result true表示登录成功
     * @param info   返回的信息
     */
    @Override
    public void onLoginResult(boolean result, String info) {
        if (result) {
            Intent intent = new Intent(this, CompanyActivity.class);
            intent.putExtra("isLogin", true);

            startActivity(intent);
            finish();
        } else {
            UtilBox.showSnackbar(this, info);
        }
    }

    /**
     * 获取验证码结果回调
     *
     * @param result true表示获取成功
     * @param info   返回的信息
     */
    @Override
    public void onGetVerifyCodeResult(boolean result, String info) {
        if (result) {
            UtilBox.showSnackbar(this, info);
        } else {
            UtilBox.showSnackbar(this, "获取失败，请重试");
        }
    }

    /**
     * 点击获取验证码后更新view
     */
    @Override
    public void onUpdateView() {
        // 注册短信变化监听
        SmsContentUtil smsContent = new SmsContentUtil(LoginActivity.this,
                new Handler(), edt_verifyCode);
        LoginActivity.this.getContentResolver().registerContentObserver(
                Uri.parse("content://sms/"), true, smsContent);

        new TimeCount(60000, 1000).start();
    }

    /**
     * 显示或隐藏旋转进度条
     *
     * @param visibility visible代表显示, invisible代表隐藏
     */
    @Override
    public void onSetRotateLoadingVisibility(int visibility) {
        if (visibility == Constants.VISIBLE) {
            runOnUiThread(progressDialog::show);
        } else if (visibility == Constants.INVISIBLE) {
            runOnUiThread(progressDialog::dismiss);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);

        finish();
    }

    /**
     * 倒计时
     */
    class TimeCount extends CountDownTimer {
        // 参数依次为总时长,和计时的时间间隔
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        // 计时完毕时触发
        @Override
        public void onFinish() {
            isCounting = false;
            runOnUiThread(() -> {
                setGetVerifyCodeBtnEnable(true);

                btn_getVerifyCode.setText("重新发送");

                edt_telephone.addTextChangedListener(LoginActivity.this);
            });
        }

        // 计时过程显示
        @Override
        public void onTick(final long millisUntilFinished) {
            isCounting = true;
            runOnUiThread(() -> {
                setGetVerifyCodeBtnEnable(false);

                btn_getVerifyCode.setText("重新发送(" + millisUntilFinished / 1000 + ")");

                edt_telephone.removeTextChangedListener(LoginActivity.this);
            });
        }
    }
}
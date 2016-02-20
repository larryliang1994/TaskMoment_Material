package com.jiubai.taskmoment.ui.activity;

import android.annotation.SuppressLint;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.jiubai.taskmoment.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 关于页面
 */
public class AboutActivity extends BaseActivity {
    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Bind(R.id.tv_version)
    TextView mVersionTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.aty_about);

        ButterKnife.bind(this);

        initView();
    }

    /**
     * 初始化所有view
     */
    @SuppressLint("SetTextI18n")
    private void initView() {
        initToolbar();

        // 获取PackageManager的实例
        PackageManager packageManager = getPackageManager();

        try {
            // getPackageName()是当前类的包名，0代表是获取版本信息
            PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(), 0);
            mVersionTextView.setText(packInfo.versionName + "-beta");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(v -> finish());
    }
}
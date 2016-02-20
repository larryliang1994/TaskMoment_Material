package com.jiubai.taskmoment.ui.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.common.UtilBox;
import com.jiubai.taskmoment.presenter.IUploadImagePresenter;
import com.jiubai.taskmoment.presenter.UploadImagePresenterImpl;
import com.jiubai.taskmoment.receiver.UpdateViewEvent;
import com.jiubai.taskmoment.ui.fragment.MemberFragment;
import com.jiubai.taskmoment.ui.fragment.PreferenceFragment;
import com.jiubai.taskmoment.ui.fragment.TimelineFragment;
import com.jiubai.taskmoment.ui.fragment.UserInfoFragment;
import com.jiubai.taskmoment.ui.iview.IUploadImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.FileNotFoundException;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * 主activity界面
 */
@SuppressLint("SetTextI18n")
public class MainActivity extends BaseActivity implements IUploadImageView {
    @Bind(R.id.dw_main)
    DrawerLayout mDrawerLayout;

    @Bind(R.id.nv_main)
    NavigationView mNavigationView;

    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Bind(R.id.appbar)
    AppBarLayout mAppBarLayout;

    @Bind(R.id.collapsingToolbarLayout)
    CollapsingToolbarLayout mCollapsingToolbarLayout;

    @Bind(R.id.iv_companyBackground)
    ImageView mCompanyBackgroundImageView;

    private CircleImageView mNavigationPortraitImageView;
    private TextView mNicknameTextView;

    private FragmentManager mFragmentManager;
    private int currentItem = 0;
    private IUploadImagePresenter mUploadImagePresenter;
    private Uri imageUri = Constants.TEMP_FILE_LOCATION;

    private TimelineFragment mTimelineFragment = new TimelineFragment();
    private MemberFragment mMemberFragment = new MemberFragment();
    private UserInfoFragment mUserInfoFragment = new UserInfoFragment();
    private PreferenceFragment mPreferenceFragment = new PreferenceFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.aty_main);

        ButterKnife.bind(this);

        initView();

        EventBus.getDefault().register(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_timeline, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_publish:
                mTimelineFragment.startActivityForResult(new Intent(this, TaskPublishActivity.class), Constants.CODE_PUBLISH_TASK);
                break;

            case R.id.action_preference:
                switchFragment(3);
                break;

            case R.id.action_notification:
                startActivity(new Intent(this, NewsActivity.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * 初始化界面
     */
    private void initView() {
        setSupportActionBar(mToolbar);

        // 默认显示任务圈
        mFragmentManager = getSupportFragmentManager();
        mFragmentManager.beginTransaction().replace(R.id.frag_main, mTimelineFragment).commit();

        // 设置NavigationView
        initNavigationView();

        ImageLoader.getInstance().displayImage(
                UtilBox.getThumbnailImageName(Config.COMPANY_BACKGROUND,
                        UtilBox.getWidthPixels(this),
                        UtilBox.dip2px(this, 256))
                        + "?t=" + Config.TIME, mCompanyBackgroundImageView);

        mUploadImagePresenter = new UploadImagePresenterImpl(this, this);
    }

    /**
     * 初始化NavigationView
     */
    private void initNavigationView() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        mNavigationView.getMenu().getItem(0).setChecked(true);
        mNavigationView.setItemTextColor(ColorStateList.valueOf(Color.parseColor("#212121")));
        mNavigationView.setItemIconTintList(null);
        mNavigationView.setNavigationItemSelectedListener(menuItem -> {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            switch (menuItem.getItemId()) {
                case R.id.navItem_timeLine:
                    switchFragment(0);
                    break;

                case R.id.navItem_member:
                    switchFragment(1);
                    break;

                case R.id.navItem_userInfo:
                    switchFragment(2);
                    break;

                case R.id.navItem_preference:
                    switchFragment(3);
                    break;

                case R.id.navItem_chooseCompany:
                    Intent intent = new Intent(MainActivity.this, CompanyActivity.class);
                    intent.putExtra("show", true);
                    startActivityForResult(intent, Constants.CODE_CHANGE_COMPANY);
                    break;
            }

            return true;
        });

        // 设置昵称
        mNicknameTextView = (TextView) mNavigationView.getHeaderView(0).findViewById(R.id.tv_navigation_nickname);
        mNicknameTextView.setText(Config.NICKNAME);

        // 获取抽屉的头像
        mNavigationPortraitImageView = (CircleImageView) mNavigationView.getHeaderView(0).findViewById(R.id.iv_navigation);
        ImageLoader.getInstance().displayImage(Config.PORTRAIT + "?t=" + Config.TIME, mNavigationPortraitImageView);

        mNavigationView.getHeaderView(0).findViewById(R.id.ll_nvHeader).setBackgroundResource(R.drawable.company_background_2);
    }

    private void switchFragment(int targetItem) {
        if (currentItem == targetItem) {
            return;
        }

        mNavigationView.getMenu().getItem(targetItem).setChecked(true);
        mNavigationView.getMenu().getItem(currentItem).setChecked(false);

        switch (targetItem) {
            case 0:
                mAppBarLayout.setExpanded(true, true);
                mCollapsingToolbarLayout.setTitle(getResources().getString(R.string.app_name));
                switchContent(mTimelineFragment);
                break;

            case 1:
                mAppBarLayout.setExpanded(false, true);
                mCollapsingToolbarLayout.setTitle(getResources().getString(R.string.member));
                switchContent(mMemberFragment);
                break;

            case 2:
                mAppBarLayout.setExpanded(false, true);
                mCollapsingToolbarLayout.setTitle(getResources().getString(R.string.userInfo));
                switchContent(mUserInfoFragment);
                break;

            case 3:
                mAppBarLayout.setExpanded(false, true);
                mCollapsingToolbarLayout.setTitle(getResources().getString(R.string.preference));
                switchContent(mPreferenceFragment);
                break;
        }

        currentItem = targetItem;
    }

    /**
     * 切换fragment
     *
     * @param to 需要切换到的fragment
     */
    private void switchContent(Fragment to) {
        Fragment from = null;
        switch (currentItem) {
            case 0:
                from = mTimelineFragment;
                break;

            case 1:
                from = mMemberFragment;
                break;

            case 2:
                from = mUserInfoFragment;
                break;

            case 3:
                from = mPreferenceFragment;
                break;
        }

        @SuppressLint("CommitTransaction")
        FragmentTransaction transaction = mFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.zoom_in, R.anim.zoom_out);

        // 先判断是否被add过
        if (!to.isAdded()) {
            // 隐藏当前的fragment，add下一个到Activity中
            transaction.hide(from).add(R.id.frag_main, to).commit();
        } else {
            transaction.hide(from).show(to).commit(); // 隐藏当前的fragment，显示下一个
        }
    }

    @OnClick({R.id.fab, R.id.iv_companyBackground})
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.fab:
                mTimelineFragment.startActivityForResult(new Intent(this, TaskPublishActivity.class), Constants.CODE_PUBLISH_TASK);
                break;

            case R.id.iv_companyBackground:
                String[] items = {"更换公司封面"};

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setItems(items, (DialogInterface dialog, int which) -> {
                    if (!Config.MID.equals(Config.COMPANY_CREATOR)) {
                        UtilBox.showSnackbar(this, "你不是管理员，不能更换公司封面");
                        return;
                    }

                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);

                    intent.setType("image/*");
                    intent.putExtra("crop", "true");
                    intent.putExtra("scale", true);
                    intent.putExtra("return-data", false);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

                    intent.putExtra("output", imageUri);
                    intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
                    intent.putExtra("noFaceDetection", true);

                    int standardWidth = UtilBox.getWidthPixels(this);
                    int standardHeight = UtilBox.dip2px(this, 256);

                    // 裁剪框比例
                    intent.putExtra("aspectX", standardWidth);
                    intent.putExtra("aspectY", standardHeight);

                    // 输出值
                    intent.putExtra("outputX", standardWidth);
                    intent.putExtra("outputY", standardHeight);

                    startActivityForResult(intent, Constants.CODE_CHOOSE_COMPANY_BACKGROUND);

                }).setCancelable(true);

                Dialog dialog = builder.create();
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Constants.CODE_CHANGE_COMPANY:
                mNavigationView.getMenu().getItem(currentItem).setChecked(true);
                if (resultCode == RESULT_OK) {
                    if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                    }
                    recreate();
                }
                break;

            case Constants.CODE_CHOOSE_COMPANY_BACKGROUND:
                if (resultCode == RESULT_OK) {
                    if (!Config.IS_CONNECTED) {
                        UtilBox.showSnackbar(this, R.string.cant_access_network);
                        return;
                    }

                    // TODO 更换公司背景没有经过服务器
                    if (imageUri != null) {
                        try {
                            final Bitmap bitmap = BitmapFactory.decodeStream(
                                    getContentResolver().openInputStream(imageUri));
                            mCompanyBackgroundImageView.setImageBitmap(bitmap);

                            final String objectName = Config.CID + ".jpg";

                            mUploadImagePresenter.doUploadImage(
                                    UtilBox.compressImage(bitmap, Constants.SIZE_COMPANY_BACKGROUND),
                                    Constants.DIR_BACKGROUND, objectName,
                                    Constants.SP_KEY_COMPANY_BACKGROUND);

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
        }
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);

        if (UtilBox.isApplicationBroughtToBackground(this)) {
            finish();
        }

        super.onDestroy();
    }

    @Subscribe
    public void onEvent(UpdateViewEvent event){
        switch(event.getAction()){
            case Constants.ACTION_CHANGE_NICKNAME:
                mNicknameTextView.setText(Config.NICKNAME);
                mNavigationView.refreshDrawableState();
                break;

            case Constants.ACTION_CHANGE_PORTRAIT:
                ImageLoader.getInstance().displayImage(
                        Config.PORTRAIT + "?t=" + Config.TIME, mNavigationPortraitImageView);
                mNavigationView.refreshDrawableState();
                break;
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {

            if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
            } else if (currentItem == 0 && TimelineFragment.commentWindowIsShow) {
                TimelineFragment.sCommentLinearLayout.setVisibility(View.GONE);
                TimelineFragment.commentWindowIsShow = false;
                UtilBox.toggleSoftInput(TimelineFragment.sCommentLinearLayout, false);
            } else if ((currentItem == 0 && TimelineFragment.auditWindowIsShow)) {
                TimelineFragment.sAuditLinearLayout.setVisibility(View.GONE);
                TimelineFragment.auditWindowIsShow = false;
            } else if (currentItem != 0) {
                switchFragment(0);
            } else {
                finish();
            }

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onUploadImageResult(String result, String info) {
        if (Constants.SUCCESS.equals(result)) {

        } else if (Constants.FAILED.equals(result)) {
            UtilBox.showSnackbar(this, info);
        }

        ImageLoader.getInstance().displayImage(
                Config.COMPANY_BACKGROUND + "?t=" + Config.TIME, mCompanyBackgroundImageView);
    }

    @Override
    public void onUploadImagesResult(String result, String info, List<String> pictureList) {
    }
}
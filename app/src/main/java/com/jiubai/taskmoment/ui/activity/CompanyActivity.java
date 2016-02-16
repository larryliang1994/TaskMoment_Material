package com.jiubai.taskmoment.ui.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.adapter.CompanyAdapter;
import com.jiubai.taskmoment.common.UtilBox;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.net.VolleyUtil;
import com.jiubai.taskmoment.presenter.CompanyPresenterImpl;
import com.jiubai.taskmoment.presenter.ICompanyPresenter;
import com.jiubai.taskmoment.ui.iview.ICompanyView;
import com.jiubai.taskmoment.zxing.activity.CaptureActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.drakeet.materialdialog.MaterialDialog;

/**
 * 我创建的公司与我加入的公司
 */
public class CompanyActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.tabs)
    TabLayout tabLayout;

    @Bind(R.id.viewPager)
    ViewPager viewPager;

    @Bind(R.id.floating_actions)
    FloatingActionsMenu floating_actions;

    private static ProgressDialog progressDialog;
    private SectionsPagerAdapter adapter;
    private static boolean isLogin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.aty_company);

        ButterKnife.bind(this);

        initView();
    }

    /**
     * 初始化界面
     */
    private void initView() {
        initToolbar();

        isLogin = getIntent().getBooleanExtra("isLogin", false);

        adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        tabLayout.setupWithViewPager(viewPager);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("正在获取数据...");
    }

    private void initToolbar() {
        setSupportActionBar(toolbar);
        if(getIntent().getBooleanExtra("show", false)){
            toolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.back));
            toolbar.setNavigationOnClickListener(v -> {
                setResult(RESULT_CANCELED);
                finish();
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_company, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                adapter.refresh();
                break;

            case R.id.action_logout:
                showLogoutDialog();
                break;

            case R.id.action_qr_code:
                JSONObject object = new JSONObject();
                try {
                    object.put("type", Constants.QR_TYPE_MEMBERINFO);
                    object.put("name", Config.NICKNAME);
                    object.put("mobile", Config.MOBILE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                View contentView = LayoutInflater.from(this).inflate(R.layout.dialog_image_desc, null);
                ((ImageView)(contentView.findViewById(R.id.iv)))
                        .setImageBitmap(UtilBox.getQRImage(object.toString(),
                                UtilBox.dip2px(this, 150), UtilBox.dip2px(this, 150)));
                ((TextView)(contentView.findViewById(R.id.tv)))
                        .setText("管理员进入成员列表\n扫码即可添加成员");
                new MaterialDialog(this).setContentView(contentView).setCanceledOnTouchOutside(true).show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showLogoutDialog() {
        final MaterialDialog logoutDialog = new MaterialDialog(this);
        logoutDialog.setMessage("你确定不是手滑了么?")
                .setPositiveButton("注销", v -> {
                    logoutDialog.dismiss();

                    UtilBox.clearAllData(this);

                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                    overridePendingTransition(R.anim.zoom_in_scale,
                            R.anim.zoom_out_scale);
                })
                .setNegativeButton("我手滑了", v -> {
                    logoutDialog.dismiss();
                })
                .setCanceledOnTouchOutside(true)
                .show();
    }

    @OnClick({R.id.fab_qr, R.id.fab_create_company})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab_qr:
                startActivityForResult(new Intent(this, CaptureActivity.class), Constants.CODE_QR_JOIN_COMPANY);
                floating_actions.collapse();
                break;

            case R.id.fab_create_company:
                showCreateCompanyDialog();
                floating_actions.collapse();
                break;
        }
    }

    private void showCreateCompanyDialog() {
        final View contentView = getLayoutInflater().inflate(R.layout.dialog_input, null);

        TextInputLayout til = (TextInputLayout) contentView.findViewById(R.id.til_input);
        til.setHint(getResources().getString(R.string.company_name));

        TextView tv_input = (TextView) contentView.findViewById(R.id.tv_input);

        final EditText edt_companyName = ((EditText) contentView
                .findViewById(R.id.edt_input));
        edt_companyName.setInputType(EditorInfo.TYPE_CLASS_TEXT);
        edt_companyName.requestFocus();

        MaterialDialog dialog = new MaterialDialog(this);
        dialog.setPositiveButton("创建", v -> {
            if (TextUtils.isEmpty(edt_companyName.getText())) {
                tv_input.setVisibility(View.VISIBLE);
                tv_input.setText("请输入公司名称");
            } else if (!Config.IS_CONNECTED) {
                Toast.makeText(this, R.string.cant_access_network,
                        Toast.LENGTH_SHORT).show();
            } else {
                String[] key = {"name"};
                String[] value = {edt_companyName.getText().toString()};
                VolleyUtil.requestWithCookie("create_company", key, value,
                        response -> {
                            try {
                                JSONObject json = new JSONObject(response);
                                String status = json.getString("status");

                                if (Constants.SUCCESS.equals(status)) {
                                    UtilBox.toggleSoftInput(tv_input, false);
                                    dialog.dismiss();
                                    adapter.refresh();
                                } else {
                                    tv_input.setVisibility(View.VISIBLE);
                                    tv_input.setText(json.getString("info"));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        },
                        volleyError -> Toast.makeText(this, "创建失败，请重试",
                                Toast.LENGTH_SHORT).show());
            }
        }).setNegativeButton("取消", v -> {
            dialog.dismiss();
        }).setContentView(contentView)
                .setCanceledOnTouchOutside(true)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Constants.CODE_QR_JOIN_COMPANY:
                if (resultCode == RESULT_OK) {
                    adapter.refresh();
                }
                break;

            default:
                break;
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (getIntent().getBooleanExtra("show", false)) {
                setResult(RESULT_CANCELED);
            } else {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
            }

            finish();

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        public ArrayList<PlaceholderFragment> fragments;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);

            fragments = new ArrayList<>();
            fragments.add(PlaceholderFragment.newInstance(0));
            fragments.add(PlaceholderFragment.newInstance(1));
        }

        public void refresh() {
            fragments.get(0).companyPresenter.getMyCompany();
            fragments.get(1).companyPresenter.getJoinedCompany();
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return position == 0 ? "我的公司" : "加入的公司";
        }
    }

    public static class PlaceholderFragment extends Fragment implements ICompanyView {

        private static final String ARG_SECTION_NUMBER = "section_number";
        private RecyclerView rv_myCompany, rv_joinedCompany;
        private ICompanyPresenter companyPresenter;

        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            companyPresenter = new CompanyPresenterImpl(this);

            View rootView;
            if (getArguments().getInt(ARG_SECTION_NUMBER) == 0) {
                rootView = inflater.inflate(R.layout.vp_frag_company, container, false);

                rv_myCompany = (RecyclerView) rootView.findViewById(R.id.recyclerView);
                rv_myCompany.setHasFixedSize(true);

                companyPresenter.getMyCompany();
            } else {
                rootView = inflater.inflate(R.layout.vp_frag_company, container, false);

                rv_joinedCompany = (RecyclerView) rootView.findViewById(R.id.recyclerView);
                rv_joinedCompany.setHasFixedSize(true);

                companyPresenter.getJoinedCompany();
            }

            return rootView;
        }

        @Override
        public void onGetMyCompanyResult(String result, String info) {
            if (Constants.SUCCESS.equals(result)) { // 获取成功
                rv_myCompany.setAdapter(new CompanyAdapter(getActivity(), info, isLogin));
                rv_myCompany.setLayoutManager(new LinearLayoutManager(getActivity()));
                rv_myCompany.setItemAnimator(new DefaultItemAnimator());
            } else if (Constants.EXPIRE.equals(result)) { // 登录信息过期
                UtilBox.showSnackbar(getActivity(), info);
                startActivity(new Intent(getActivity(), LoginActivity.class));
                getActivity().finish();
            } else if (Constants.FAILED.equals(result)) {
                UtilBox.showSnackbar(getActivity(), info);
            }
        }

        @Override
        public void onGetJoinedCompanyResult(String result, String info) {
            if (Constants.SUCCESS.equals(result)) {
                rv_joinedCompany.setAdapter(new CompanyAdapter(getActivity(), info, isLogin));
                rv_joinedCompany.setLayoutManager(new LinearLayoutManager(getActivity()));
                rv_joinedCompany.setItemAnimator(new DefaultItemAnimator());
            } else if (Constants.FAILED.equals(result)) {
                UtilBox.showSnackbar(getActivity(), info);
            }
        }

        @Override
        public void onSetSwipeRefreshVisibility(int visibility) {
            if (visibility == Constants.VISIBLE) {
                progressDialog.show();
            } else if (visibility == Constants.INVISIBLE) {
                progressDialog.dismiss();
            }
        }
    }
}
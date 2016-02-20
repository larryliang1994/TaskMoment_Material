package com.jiubai.taskmoment.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.adapter.MemberListAdapter;
import com.jiubai.taskmoment.bean.Member;
import com.jiubai.taskmoment.common.UtilBox;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.config.Urls;
import com.jiubai.taskmoment.net.VolleyUtil;
import com.jiubai.taskmoment.zxing.activity.CaptureActivity;
import com.melnykov.fab.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.drakeet.materialdialog.MaterialDialog;

/**
 * 成员管理
 */
public class MemberFragment extends Fragment {
    @Bind(R.id.listView)
    ListView mListView;

    @Bind(R.id.swipeRefreshLayout)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @Bind(R.id.fab)
    FloatingActionButton mFloatingActionButton;

    private MemberListAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_member, container, false);

        ButterKnife.bind(this, view);

        initView();

        return view;
    }

    /**
     * 初始化界面
     */
    private void initView() {
        mSwipeRefreshLayout.setOnRefreshListener(this::refreshListView);
        mSwipeRefreshLayout.setEnabled(true);

        mFloatingActionButton.attachToListView(mListView);

        // 延迟执行才能使旋转进度条显示出来
        new Handler().postDelayed(this::refreshListView, 100);
    }

    /**
     * 刷新ListView
     */
    private void refreshListView() {
        if (!Config.IS_CONNECTED) {
            UtilBox.showSnackbar(getActivity(), R.string.cant_access_network);
            return;
        }

        mSwipeRefreshLayout.setRefreshing(true);

        VolleyUtil.requestWithCookie(Urls.GET_MEMBER + Config.CID, null, null,
                this::showMember,
                volleyError -> {
                    mSwipeRefreshLayout.setRefreshing(false);
                    UtilBox.showSnackbar(getActivity(), "获取成员列表失败，请重试");
                });
    }

    @OnClick({R.id.fab})
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fab:
                final View contentView = getActivity().getLayoutInflater()
                        .inflate(R.layout.dialog_input, null);

                TextInputLayout til = (TextInputLayout) contentView.findViewById(R.id.til_input);
                til.setHint("手机号");

                final MaterialDialog dialog = new MaterialDialog(getActivity());
                dialog.setPositiveButton("添加", v -> {
                    new Handler().post(() -> {
                        if (!Config.IS_CONNECTED) {
                            Toast.makeText(getActivity(),
                                    R.string.cant_access_network,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String mobile = ((EditText) contentView
                                .findViewById(R.id.edt_input))
                                .getText().toString();

                        if (!UtilBox.isTelephoneNumber(mobile)) {
                            TextView tv1 = (TextView) contentView
                                    .findViewById(R.id.tv_input);
                            tv1.setVisibility(View.VISIBLE);
                            tv1.setText("请输入11位手机号");

                            return;
                        }

                        String[] key = {"mobile", "cid"};
                        String[] value = {mobile, Config.CID};

                        VolleyUtil.requestWithCookie(Urls.ADD_MEMBER, key, value,
                                response -> {
                                    String result = createMemberCheck(response);
                                    if (result != null && "成功".equals(result)) {
                                        dialog.dismiss();
                                    } else if (result != null) {
                                        TextView tv1 = (TextView) contentView
                                                .findViewById(R.id.tv_input);
                                        tv1.setVisibility(View.VISIBLE);
                                        tv1.setText(result);
                                    }
                                },
                                volleyError -> Toast.makeText(getActivity(),
                                        "添加失败，请重试",
                                        Toast.LENGTH_SHORT).show());
                    });
                });
                dialog.setNegativeButton("扫码", v -> {
                    dialog.dismiss();

                    startActivityForResult(new Intent(getActivity(), CaptureActivity.class),
                            Constants.CODE_QR_ADD_MEMBER);
                });

                dialog.setContentView(contentView)
                        .setCanceledOnTouchOutside(true)
                        .show();
                break;
        }
    }

    /**
     * 检查添加成员返回的json
     *
     * @param result 请求结果
     * @return 返回的信息内容
     */
    private String createMemberCheck(String result) {
        try {
            JSONObject json = new JSONObject(result);
            String status = json.getString("status");

            if (Constants.SUCCESS.equals(status)) {
                // 插到最后
                MemberListAdapter.memberList.add(new Member(json.getString("real_name"),
                        json.getString("mobile"), json.getString("id"), json.getString("mid")));

                mAdapter.notifyDataSetChanged();
                UtilBox.setListViewHeightBasedOnChildren(mListView);
            }

            return json.getString("info");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 显示成员
     *
     * @param response 通信返回的json
     */
    private void showMember(String response) {
        try {
            JSONObject responseJson = new JSONObject(response);

            String responseStatus = responseJson.getString("status");

            if (Constants.SUCCESS.equals(responseStatus)) {

                mAdapter = new MemberListAdapter(getActivity(), response, mListView);

                if (mListView.getAdapter() == null) {
                    mListView.setAdapter(new MemberListAdapter(getActivity(), response, mListView));
                } else {
                    mAdapter.notifyDataSetChanged();
                }

                UtilBox.setListViewHeightBasedOnChildren(mListView);
            } else {
                UtilBox.showSnackbar(getActivity(), "数据有误，请重试");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new Handler().postDelayed(() -> mSwipeRefreshLayout.setRefreshing(false), 1000);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Constants.CODE_QR_ADD_MEMBER:
                if (resultCode == Activity.RESULT_OK) {
                    VolleyUtil.requestWithCookie(Urls.GET_MEMBER + Config.CID, null, null,
                            this::showMember,
                            volleyError -> {
                                mSwipeRefreshLayout.setRefreshing(false);
                                UtilBox.showSnackbar(getActivity(), "获取成员列表失败，请重试");
                            });
                }
                break;
        }
    }
}
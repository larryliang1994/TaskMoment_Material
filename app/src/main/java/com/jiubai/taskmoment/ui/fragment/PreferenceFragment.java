package com.jiubai.taskmoment.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.common.UtilBox;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.ui.activity.AboutActivity;
import com.jiubai.taskmoment.ui.activity.CompanyInfoActivity;
import com.jiubai.taskmoment.widget.RippleView;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UpdateStatus;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.drakeet.materialdialog.MaterialDialog;

/**
 * 偏好设置
 */
public class PreferenceFragment extends Fragment implements RippleView.OnRippleCompleteListener{
    @Bind(R.id.rv_companyInfo) RippleView mCompanyInfoRippleView;
    @Bind(R.id.rv_share) RippleView mShareRippleView;
    @Bind(R.id.rv_update) RippleView mUpdateRippleView;
    @Bind(R.id.rv_about) RippleView mAboutRippleView;
    @Bind(R.id.rv_exit) RippleView mExitRippleView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_preference, container, false);

        ButterKnife.bind(this, view);

        initView();

        return view;
    }

    private void initView(){
        mCompanyInfoRippleView.setOnRippleCompleteListener(this);
        mShareRippleView.setOnRippleCompleteListener(this);
        mUpdateRippleView.setOnRippleCompleteListener(this);
        mAboutRippleView.setOnRippleCompleteListener(this);
        mExitRippleView.setOnRippleCompleteListener(this);
    }

    @Override
    public void onComplete(RippleView rippleView) {
        switch (rippleView.getId()){
            case R.id.rv_companyInfo:
                startActivity(new Intent(getActivity(), CompanyInfoActivity.class));
                break;

            case R.id.rv_share:
                Intent intent=new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain"); // 纯文本
                intent.putExtra(Intent.EXTRA_TEXT, Constants.SHARE_TEXT);
                intent.putExtra(Intent.EXTRA_SUBJECT, "分享");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(Intent.createChooser(intent, getActivity().getTitle()));
                break;

            case R.id.rv_update:
                UmengUpdateAgent.setUpdateListener((updateStatus, updateInfo) -> {
                    switch (updateStatus) {
                        case UpdateStatus.NoneWifi:
                        case UpdateStatus.Yes: // has update
                            UmengUpdateAgent.showUpdateDialog(getActivity(), updateInfo);
                            break;

                        case UpdateStatus.No: // has no update
                        case UpdateStatus.Timeout: // time out
                            UtilBox.showSnackbar(getActivity(), "您的应用为最新版本");
                            break;
                    }
                });
                UmengUpdateAgent.setDialogListener(status -> {
                    switch (status) {
                        case UpdateStatus.Update:
                            UtilBox.showSnackbar(getActivity(), "开始下载更新");
                            break;
                        case UpdateStatus.Ignore:
                        case UpdateStatus.NotNow:
                            break;
                    }
                });
                UmengUpdateAgent.update(getActivity());
                break;

            case R.id.rv_about:
                startActivity(new Intent(getActivity(), AboutActivity.class));
                break;

            case R.id.rv_exit:
                final MaterialDialog dialog = new MaterialDialog(getActivity());
                dialog.setMessage("真的要关闭任务圈吗")
                        .setNegativeButton("假的", v -> {
                            dialog.dismiss();
                        })
                        .setPositiveButton("真的", v -> {
                            dialog.dismiss();
                            getActivity().finish();
                            //System.exit(0);
                        })
                        .setCanceledOnTouchOutside(true)
                        .show();
                break;
        }
    }
}
package com.jiubai.taskmoment.ui.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.common.UtilBox;
import com.jiubai.taskmoment.presenter.ChangeNicknamePresenterImpl;
import com.jiubai.taskmoment.presenter.IUploadImagePresenter;
import com.jiubai.taskmoment.presenter.UploadImagePresenterImpl;
import com.jiubai.taskmoment.receiver.UpdateViewEvent;
import com.jiubai.taskmoment.ui.activity.CheckPictureActivity;
import com.jiubai.taskmoment.ui.activity.LoginActivity;
import com.jiubai.taskmoment.ui.activity.PersonalTimelineActivity;
import com.jiubai.taskmoment.ui.iview.IChangeNicknameView;
import com.jiubai.taskmoment.ui.iview.IUploadImageView;
import com.jiubai.taskmoment.widget.RippleView;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.drakeet.materialdialog.MaterialDialog;

/**
 * 个人中心
 */
public class UserInfoFragment extends Fragment implements IUploadImageView, IChangeNicknameView,
        RippleView.OnRippleCompleteListener {
    @Bind(R.id.rv_portrait)
    RippleView mPortraitRippleView;

    @Bind(R.id.rv_nickname)
    RippleView mNicknameRippleView;

    @Bind(R.id.rv_logout)
    RippleView mLogoutRippleView;

    @Bind(R.id.rv_publish)
    RippleView mPublishRippleView;

    @Bind(R.id.rv_involved)
    RippleView mInvolvedRippleView;

    @Bind(R.id.rv_audit)
    RippleView mAuditRippleView;

    @Bind(R.id.iv_portrait)
    ImageView mPortraitImageView;

    @Bind(R.id.tv_nickname)
    TextView mNicknameTextView;

    private IUploadImagePresenter mUploadImagePresenter;
    private TextView mInputTextView;
    private MaterialDialog mDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_userinfo, container, false);

        ButterKnife.bind(this, view);

        initView();

        return view;
    }

    /**
     * 初始化界面
     */
    private void initView() {
        mPortraitRippleView.setOnRippleCompleteListener(this);
        mNicknameRippleView.setOnRippleCompleteListener(this);
        mLogoutRippleView.setOnRippleCompleteListener(this);
        mPublishRippleView.setOnRippleCompleteListener(this);
        mInvolvedRippleView.setOnRippleCompleteListener(this);
        mAuditRippleView.setOnRippleCompleteListener(this);

        ImageLoader.getInstance().displayImage(
                UtilBox.getThumbnailImageName(Config.PORTRAIT + "?t=" + Config.TIME,
                        UtilBox.dip2px(getActivity(), 36),
                        UtilBox.dip2px(getActivity(), 36)), mPortraitImageView);

        mPortraitImageView.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CheckPictureActivity.class);

            ArrayList<String> portrait = new ArrayList<>();
            portrait.add(Config.PORTRAIT + "?t=" + Config.TIME);
            intent.putStringArrayListExtra("pictureList", portrait);
            intent.putExtra("index", 0);
            intent.putExtra("fromWhere", "net");

            startActivity(intent);
            getActivity().overridePendingTransition(R.anim.zoom_in_quick, R.anim.scale_stay);

        });

        mNicknameTextView.setText(Config.NICKNAME);

        mUploadImagePresenter = new UploadImagePresenterImpl(getActivity(), this);
    }

    @SuppressLint("InflateParams")
    private void showNicknameDialog(final String nickname) {
        final View contentView = getActivity().getLayoutInflater()
                .inflate(R.layout.dialog_input, null);

        TextInputLayout til = (TextInputLayout) contentView.findViewById(R.id.til_input);
        til.setHint(getActivity().getResources().getString(R.string.nickname));

        final EditText et_nickname = ((EditText) contentView
                .findViewById(R.id.edt_input));
        et_nickname.setText(nickname);
        et_nickname.setInputType(EditorInfo.TYPE_CLASS_TEXT);
        et_nickname.requestFocus();

        mDialog = new MaterialDialog(getActivity()).setPositiveButton("完成", v -> {
            new Handler().post(() -> {
                if (!Config.IS_CONNECTED) {
                    UtilBox.showSnackbar(getActivity(), R.string.cant_access_network);
                    return;
                }

                final String newNickname = et_nickname.getText().toString();
                mInputTextView = (TextView) contentView.findViewById(R.id.tv_input);

                if (newNickname.isEmpty() || newNickname.length() == 0) {
                    mInputTextView.setVisibility(View.VISIBLE);
                    mInputTextView.setText("昵称不能为空");
                } else if (newNickname.getBytes().length > 24) {
                    mInputTextView.setVisibility(View.VISIBLE);
                    mInputTextView.setText("昵称过长");
                } else if (newNickname.equals(nickname)) {
                    mDialog.dismiss();
                } else {
                    new ChangeNicknamePresenterImpl(getActivity(), UserInfoFragment.this)
                            .doChangeNickname(newNickname);
                }
            });
        });
        mDialog.setNegativeButton("取消", v -> {
            mDialog.dismiss();
        });

        mDialog.setContentView(contentView)
                .setCanceledOnTouchOutside(true)
                .show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Subscribe
    public void onEvent(UpdateViewEvent event) {
        switch (event.getAction()) {
            case Constants.ACTION_CHANGE_NICKNAME:
                mNicknameTextView.setText(Config.NICKNAME);
                break;
        }
    }

    @Override
    public void onChangeNicknameResult(String result, String info) {
        if (Constants.SUCCESS.equals(result)) {
            mDialog.dismiss();
        } else {
            mInputTextView.setVisibility(View.VISIBLE);
            mInputTextView.setText(info);
        }
    }

    @Override
    public void onComplete(RippleView rippleView) {
        Intent intent = new Intent(getActivity(), PersonalTimelineActivity.class);

        switch (rippleView.getId()) {
            case R.id.rv_publish:
                intent.putExtra("mid", Config.MID);
                startActivity(intent);
                getActivity().overridePendingTransition(
                        R.anim.in_right_left, R.anim.scale_stay);
                break;

            case R.id.rv_involved:
                intent.putExtra("isInvolved", true);
                intent.putExtra("mid", Config.MID);
                startActivity(intent);
                getActivity().overridePendingTransition(
                        R.anim.in_right_left, R.anim.scale_stay);
                break;

            case R.id.rv_audit:
                intent.putExtra("isAudit", true);
                intent.putExtra("mid", Config.MID);
                startActivity(intent);
                getActivity().overridePendingTransition(
                        R.anim.in_right_left, R.anim.scale_stay);
                break;

            case R.id.rv_portrait:
                String[] items = {"更换头像"};

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setItems(items, (dialog1, which) -> {
                    Intent intent1 = new Intent(Intent.ACTION_GET_CONTENT, null);

                    intent1.setType("image/*");
                    intent1.putExtra("crop", "true");
                    intent1.putExtra("scale", true);
                    intent1.putExtra("return-data", true);
                    intent1.putExtra("outputFormat",
                            Bitmap.CompressFormat.JPEG.toString());
                    intent1.putExtra("noFaceDetection", true);

                    // 裁剪框比例
                    intent1.putExtra("aspectX", 1);
                    intent1.putExtra("aspectY", 1);

                    // 输出值
                    intent1.putExtra("outputX", 255);
                    intent1.putExtra("outputY", 255);

                    startActivityForResult(
                            intent1, Constants.CODE_CHOOSE_PORTRAIT);

                    getActivity().overridePendingTransition(
                            R.anim.in_right_left, R.anim.scale_stay);
                })
                        .setCancelable(true);

                Dialog dialog = builder.create();
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();
                break;

            case R.id.rv_nickname:
                showNicknameDialog(mNicknameTextView.getText().toString());
                break;

            case R.id.rv_logout:
                final MaterialDialog logoutDialog = new MaterialDialog(getActivity());
                logoutDialog.setMessage("你确定不是手滑了么?")
                        .setPositiveButton("注销", v -> {
                            logoutDialog.dismiss();

                            UtilBox.clearAllData(getActivity());

                            startActivity(new Intent(getActivity(), LoginActivity.class));
                            getActivity().finish();
                            getActivity().overridePendingTransition(
                                    R.anim.in_left_right, R.anim.out_left_right);
                        })
                        .setNegativeButton("我手滑了", v -> {
                            logoutDialog.dismiss();
                        })
                        .setCanceledOnTouchOutside(true)
                        .show();
                break;
        }


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Constants.CODE_CHOOSE_PORTRAIT:
                if (resultCode == Activity.RESULT_OK) {
                    if (!Config.IS_CONNECTED) {
                        UtilBox.showSnackbar(getActivity(), R.string.cant_access_network);
                        return;
                    }

                    final Bitmap bitmap = data.getParcelableExtra("data");

                    final String objectName = Config.MID + ".jpg";

                    mUploadImagePresenter.doUploadImage(
                            UtilBox.compressImage(bitmap, Constants.SIZE_PORTRAIT),
                            Constants.DIR_PORTRAIT, objectName, Constants.SP_KEY_PORTRAIT);
                }
                break;
        }
    }

    @Override
    public void onUploadImageResult(String result, String info) {
        if (Constants.SUCCESS.equals(result)) {
            ImageLoader.getInstance().displayImage(Config.PORTRAIT + "?t=" + Config.TIME, mPortraitImageView);
        } else {
            UtilBox.showSnackbar(getActivity(), info);
        }
    }

    @Override
    public void onUploadImagesResult(String result, String info, List<String> pictureList) {

    }
}
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
import com.jiubai.taskmoment.ui.activity.CheckPictureActivity;
import com.jiubai.taskmoment.ui.activity.LoginActivity;
import com.jiubai.taskmoment.ui.activity.PersonalTimelineActivity;
import com.jiubai.taskmoment.ui.iview.IChangeNicknameView;
import com.jiubai.taskmoment.ui.iview.IUploadImageView;
import com.jiubai.taskmoment.widget.RippleView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.drakeet.materialdialog.MaterialDialog;

/**
 * 个人中心
 */
public class UserInfoFragment extends Fragment implements IUploadImageView, IChangeNicknameView,
        RippleView.OnRippleCompleteListener{
    @Bind(R.id.rv_portrait) RippleView rv_portrait;
    @Bind(R.id.rv_nickname) RippleView rv_nickname;
    @Bind(R.id.rv_logout) RippleView rv_logout;
    @Bind(R.id.rv_publish) RippleView rv_publish;
    @Bind(R.id.rv_involved) RippleView rv_involved;
    @Bind(R.id.rv_audit) RippleView rv_audit;
    @Bind(R.id.iv_portrait) ImageView iv_portrait;
    @Bind(R.id.tv_nickname) TextView tv_nickname;

    private IUploadImagePresenter uploadImagePresenter;
    private TextView tv_input;
    private MaterialDialog dialog;

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
        rv_portrait.setOnRippleCompleteListener(this);
        rv_nickname.setOnRippleCompleteListener(this);
        rv_logout.setOnRippleCompleteListener(this);
        rv_publish.setOnRippleCompleteListener(this);
        rv_involved.setOnRippleCompleteListener(this);
        rv_audit.setOnRippleCompleteListener(this);

        ImageLoader.getInstance().displayImage(
                UtilBox.getThumbnailImageName(Config.PORTRAIT + "?t=" + Config.TIME,
                        UtilBox.dip2px(getActivity(), 36),
                        UtilBox.dip2px(getActivity(), 36)),
                iv_portrait);

        iv_portrait.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CheckPictureActivity.class);

            ArrayList<String> portrait = new ArrayList<>();
            portrait.add(Config.PORTRAIT + "?t=" + Config.TIME);
            intent.putStringArrayListExtra("pictureList", portrait);
            intent.putExtra("index", 0);
            intent.putExtra("fromWhere", "net");

            startActivity(intent);
            getActivity().overridePendingTransition(R.anim.zoom_in_quick, R.anim.scale_stay);

        });

        tv_nickname.setText(Config.NICKNAME);

        uploadImagePresenter = new UploadImagePresenterImpl(getActivity(), this);
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

        dialog = new MaterialDialog(getActivity()).setPositiveButton("完成", v -> {
            new Handler().post(() -> {
                if (!Config.IS_CONNECTED) {
                    UtilBox.showSnackbar(getActivity(), R.string.cant_access_network);
                    return;
                }

                final String newNickname = et_nickname.getText().toString();
                tv_input = (TextView) contentView.findViewById(R.id.tv_input);

                if (newNickname.isEmpty() || newNickname.length() == 0) {
                    tv_input.setVisibility(View.VISIBLE);
                    tv_input.setText("昵称不能为空");
                } else if (newNickname.getBytes().length > 24) {
                    tv_input.setVisibility(View.VISIBLE);
                    tv_input.setText("昵称过长");
                } else if (newNickname.equals(nickname)) {
                    dialog.dismiss();
                } else {
                    new ChangeNicknamePresenterImpl(getActivity(), UserInfoFragment.this)
                            .doChangeNickname(newNickname);
                }
            });
        });
        dialog.setNegativeButton("取消", v -> {
            dialog.dismiss();
        });

        dialog.setContentView(contentView)
                .setCanceledOnTouchOutside(true)
                .show();
    }

    @Override
    public void onChangeNicknameResult(String result, String info) {
        if(Constants.SUCCESS.equals(result)){
            dialog.dismiss();
            tv_nickname.setText(Config.NICKNAME);
        } else {
            tv_input.setVisibility(View.VISIBLE);
            tv_input.setText(info);
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
                showNicknameDialog(tv_nickname.getText().toString());
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

                    uploadImagePresenter.doUploadImage(
                            UtilBox.compressImage(bitmap, Constants.SIZE_PORTRAIT),
                            Constants.DIR_PORTRAIT, objectName, Constants.SP_KEY_PORTRAIT);
                }
                break;
        }
    }

    @Override
    public void onUploadImageResult(String result, String info) {
        if(Constants.SUCCESS.equals(result)){
            ImageLoader.getInstance().displayImage(Config.PORTRAIT + "?t=" + Config.TIME, iv_portrait);
        } else {
            UtilBox.showSnackbar(getActivity(), info);
        }
    }

    @Override
    public void onUploadImagesResult(String result, String info, List<String> pictureList) {

    }
}
package com.jiubai.taskmoment.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.config.Urls;

import com.jiubai.taskmoment.presenter.ChangeNicknamePresenterImpl;
import com.jiubai.taskmoment.presenter.IChangeNicknamePresenter;
import com.jiubai.taskmoment.ui.activity.CheckPictureActivity;
import com.jiubai.taskmoment.ui.activity.PersonalTimelineActivity;
import com.jiubai.taskmoment.widget.RippleView;
import com.jiubai.taskmoment.ui.iview.IChangeNicknameView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

import me.drakeet.materialdialog.MaterialDialog;

/**
 * 他人个人信息适配器
 */
@SuppressLint("InflateParams")
public class PersonalInfoAdapter extends BaseAdapter implements IChangeNicknameView {
    private ArrayList<String> itemList;
    private Context mContext;
    private String mid;
    private String nickname;
    private MaterialDialog mDialog;
    private TextView mNicknameTextView, mErrorTextView;

    public PersonalInfoAdapter(Context context, String mid, String nickname) {
        if (itemList == null) {
            itemList = new ArrayList<>();
        }

        itemList.clear();

        itemList.add("昵称");

        if (Config.MID.equals(mid)) {
            itemList.add("我发布的任务");
            itemList.add("我参与的任务");
            itemList.add("我审核的任务");
        } else {
            itemList.add("ta发布的任务");
            itemList.add("ta参与的任务");
            itemList.add("ta审核的任务");
        }

        this.mContext = context;
        this.mid = mid;
        this.nickname = nickname;
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public Object getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (position == 0) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_userinfo_head, null);

            mNicknameTextView = ((TextView) convertView.findViewById(R.id.tv_nickname));
            mNicknameTextView.setText(nickname);

            if(mid.equals(Config.MID)){
                mNicknameTextView.setOnClickListener(v -> showNicknameDialog(nickname));
            }

            final ImageView iv_portrait = (ImageView) convertView.findViewById(R.id.iv_portrait);
            ImageLoader.getInstance().displayImage(
                    Urls.MEDIA_CENTER_PORTRAIT + mid + ".jpg" + "?t=" + Config.TIME, iv_portrait);

            iv_portrait.setOnClickListener(v -> {
                if (!mid.equals(Config.MID)) {
                    ArrayList<String> picture = new ArrayList<>();
                    picture.add(Urls.MEDIA_CENTER_PORTRAIT + mid + ".jpg");

                    Intent intent = new Intent(mContext, CheckPictureActivity.class);
                    intent.putExtra("pictureList", picture);
                    intent.putExtra("fromWhere", "net");

                    mContext.startActivity(intent);
                } else {
                    String[] items = {"更换头像"};

                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setItems(items, (dialog1, which) -> {
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);

                        intent.setType("image/*");
                        intent.putExtra("crop", "true");
                        intent.putExtra("scale", true);
                        intent.putExtra("return-data", true);
                        intent.putExtra("outputFormat",
                                Bitmap.CompressFormat.JPEG.toString());
                        intent.putExtra("noFaceDetection", true);

                        // 裁剪框比例
                        intent.putExtra("aspectX", 1);
                        intent.putExtra("aspectY", 1);

                        // 输出值
                        intent.putExtra("outputX", 255);
                        intent.putExtra("outputY", 255);

                        ((Activity) mContext).startActivityForResult(
                                intent, Constants.CODE_CHOOSE_PORTRAIT);
                    })
                            .setCancelable(true);

                    Dialog dialog1 = builder.create();
                    dialog1.setCanceledOnTouchOutside(true);
                    dialog1.show();
                }
            });
        } else {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_body, null);
            ((TextView) convertView.findViewById(R.id.tv_item_body))
                    .setText(itemList.get(position));

            ((RippleView) convertView.findViewById(R.id.rv_item_body)).setOnRippleCompleteListener(
                    rippleView -> {
                        Intent intent = new Intent(mContext, PersonalTimelineActivity.class);
                        if (position == 3) {
                            // ta审核的任务
                            intent.putExtra("isAudit", true);
                        } else if (position == 2) {
                            // ta发布的任务
                            intent.putExtra("isInvolved", true);
                        }
                        intent.putExtra("mid", mid);
                        mContext.startActivity(intent);
                    });

            // 去掉最后一条分割线
            if (position == getCount() - 1) {
                convertView.findViewById(R.id.iv_item_divider).setVisibility(View.GONE);
            }
        }

        return convertView;
    }


    private void showNicknameDialog(final String nickname) {
        final View contentView = ((Activity) mContext).getLayoutInflater()
                .inflate(R.layout.dialog_input, null);

        TextInputLayout til = (TextInputLayout) contentView.findViewById(R.id.til_input);
        til.setHint(mContext.getResources().getString(R.string.nickname));

        final EditText et_nickname = ((EditText) contentView
                .findViewById(R.id.edt_input));
        et_nickname.setText(nickname);
        et_nickname.setInputType(EditorInfo.TYPE_CLASS_TEXT);
        et_nickname.requestFocus();

        mDialog = new MaterialDialog(mContext);
        mDialog.setPositiveButton("完成", v -> {
            new Handler().post(() -> {
                if (!Config.IS_CONNECTED) {
                    Toast.makeText(mContext, R.string.cant_access_network,
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                final String newNickname = et_nickname.getText().toString();
                mErrorTextView = (TextView) contentView.findViewById(R.id.tv_input);

                if (newNickname.isEmpty() || newNickname.length() == 0) {
                    mErrorTextView.setVisibility(View.VISIBLE);
                    mErrorTextView.setText("昵称不能为空");
                } else if (newNickname.getBytes().length > 24) {
                    mErrorTextView.setVisibility(View.VISIBLE);
                    mErrorTextView.setText("昵称过长");
                } else if (newNickname.equals(nickname)) {
                    mDialog.dismiss();
                } else {
                    IChangeNicknamePresenter changeNicknamePresenter
                            = new ChangeNicknamePresenterImpl(mContext, PersonalInfoAdapter.this);
                    changeNicknamePresenter.doChangeNickname(newNickname);
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
    public void onChangeNicknameResult(String result, String info) {
        if(Constants.SUCCESS.equals(result)){
            mDialog.dismiss();
            mNicknameTextView.setText(Config.NICKNAME);
        } else {
            mErrorTextView.setVisibility(View.VISIBLE);
            mErrorTextView.setText(info);
        }
    }
}
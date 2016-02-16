package com.jiubai.taskmoment.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;

import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.adapter.PersonalInfoAdapter;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.common.UtilBox;
import com.jiubai.taskmoment.presenter.IUploadImagePresenter;
import com.jiubai.taskmoment.presenter.UploadImagePresenterImpl;
import com.jiubai.taskmoment.receiver.UpdateViewReceiver;
import com.jiubai.taskmoment.ui.iview.IUploadImageView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 个人信息页面
 */
public class PersonalInfoActivity extends BaseActivity implements IUploadImageView {
    @Bind(R.id.lv_personalInfo)
    ListView lv;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    private String mid, nickname;
    private PersonalInfoAdapter adapter;
    private IUploadImagePresenter uploadImagePresenter;
    private UpdateViewReceiver nicknameReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.aty_personal_info);

        ButterKnife.bind(this);

        Intent intent = getIntent();
        mid = intent.getStringExtra("mid");
        nickname = intent.getStringExtra("nickname");

        initView();
    }

    /**
     * 初始化所有view
     */
    private void initView() {
        initToolbar();

        adapter = new PersonalInfoAdapter(this, mid, nickname);
        lv.setAdapter(adapter);

        uploadImagePresenter = new UploadImagePresenterImpl(this, this);
    }

    private void initToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setTitle(nickname);
        toolbar.setNavigationOnClickListener(v -> {
            finish();
        });
    }

    @Override
    public void onStart() {
        nicknameReceiver = new UpdateViewReceiver(this,
                (msg, objects) -> adapter.notifyDataSetChanged());
        nicknameReceiver.registerAction(Constants.ACTION_CHANGE_NICKNAME);

        super.onStart();
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(nicknameReceiver);

        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Constants.CODE_CHOOSE_PORTRAIT:
                if (resultCode == RESULT_OK) {
                    if (!Config.IS_CONNECTED) {
                        UtilBox.showSnackbar(this, R.string.cant_access_network);
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
            adapter.notifyDataSetChanged();
        } else {
            UtilBox.showSnackbar(this, info);
        }
    }

    @Override
    public void onUploadImagesResult(String result, String info, List<String> pictureList) {

    }
}
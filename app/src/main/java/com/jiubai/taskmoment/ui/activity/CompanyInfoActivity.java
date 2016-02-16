package com.jiubai.taskmoment.ui.activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.adapter.MemberListAdapter;
import com.jiubai.taskmoment.common.UtilBox;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 公司信息页面
 */
public class CompanyInfoActivity extends BaseActivity {
    @Bind(R.id.tv_companyName)
    TextView tv_companyName;

    @Bind(R.id.tv_companyCreator)
    TextView tv_companyCreator;

    @Bind(R.id.iv_companyInfo_qr)
    ImageView iv_qr;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.aty_company_info);

        ButterKnife.bind(this);

        initView();
    }

    /**
     * 初始化所有view
     */
    private void initView() {
        initToolbar();

        tv_companyName.setText(Config.COMPANY_NAME);

        MemberListAdapter.getMemberList(this, new MemberListAdapter.GetMemberCallBack() {
            @Override
            public void successCallback() {
                tv_companyCreator.setText(MemberListAdapter.getMemberWithID(Config.COMPANY_CREATOR).getName());
            }

            @Override
            public void failedCallback() {
            }
        });

        JSONObject object = new JSONObject();
        try {
            object.put("type", Constants.QR_TYPE_COMPANYINFO);
            object.put("name", Config.COMPANY_NAME);
            object.put("cid", Config.CID);
            object.put("creator", Config.COMPANY_CREATOR);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        iv_qr.setImageBitmap(UtilBox.getQRImage(object.toString(), UtilBox.dip2px(this, 200), UtilBox.dip2px(this, 200)));
    }

    private void initToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            finish();
        });
    }

    @OnClick({R.id.tv_companyName})
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.tv_companyName:
//                String[] key = {"id", "name"};
//                String[] value = {Config.CID, "玖佰网测试"};
//                VolleyUtil.requestWithCookie(Urls.UPDATE_COMPANY_INFO, key, value,
//                        new Response.Listener<String>() {
//                            @Override
//                            public void onResponse(String s) {
//                                System.out.println(s);
//                            }
//                        },
//                        new Response.ErrorListener() {
//                            @Override
//                            public void onErrorResponse(VolleyError volleyError) {
//
//                            }
//                        });
                break;
        }
    }
}

package com.jiubai.taskmoment.presenter;

import android.os.Handler;

import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.net.VolleyUtil;
import com.jiubai.taskmoment.common.UtilBox;
import com.jiubai.taskmoment.ui.iview.IGetVerifyCodeView;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by howell on 2015/11/28.
 * GetVerifyCodePresenter实现类
 */
public class GetVerifyCodePresenterImpl implements IGetVerifyCodePresenter {

    private IGetVerifyCodeView mIGetVerifyCodeView;

    public GetVerifyCodePresenterImpl(IGetVerifyCodeView iGetVerifyCodeView) {
        this.mIGetVerifyCodeView = iGetVerifyCodeView;
    }

    @Override
    public void doGetVerifyCode(final String phoneNum) {
        new Handler().post(() -> {

            mIGetVerifyCodeView.onUpdateView();

            mIGetVerifyCodeView.onSetRotateLoadingVisibility(Constants.VISIBLE);

            if (Config.RANDOM == null) {
                UtilBox.getRandom();
            }

            String[] soapKey = {"type", "table_name", "feedback_url", "return"};
            String[] soapValue = {"sms_send_verifycode", Config.RANDOM, "", "1"};
            String[] httpKey = {"mobile"};
            String[] httpValue = {phoneNum};
            VolleyUtil.requestWithSoap(soapKey, soapValue, httpKey, httpValue,
                    response -> {
                        mIGetVerifyCodeView.onSetRotateLoadingVisibility(Constants.INVISIBLE);

                        try {
                            JSONObject obj = new JSONObject(response);
                            mIGetVerifyCodeView.onGetVerifyCodeResult(true, obj.getString("info"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    },
                    volleyError -> {
                        mIGetVerifyCodeView.onSetRotateLoadingVisibility(Constants.INVISIBLE);

                        mIGetVerifyCodeView.onGetVerifyCodeResult(false, "");

                        if (volleyError != null
                                && volleyError.getMessage() != null) {
                            System.out.println(volleyError.getMessage());
                        }
                    });
        });
    }
}
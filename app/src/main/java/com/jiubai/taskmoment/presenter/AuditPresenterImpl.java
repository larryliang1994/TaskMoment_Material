package com.jiubai.taskmoment.presenter;

import android.content.Context;

import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.config.Urls;
import com.jiubai.taskmoment.net.VolleyUtil;
import com.jiubai.taskmoment.receiver.UpdateViewEvent;
import com.jiubai.taskmoment.ui.iview.IAuditView;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by howell on 2015/11/29.
 * AuditPresenter实现类
 */
public class AuditPresenterImpl implements IAuditPresenter {
    private IAuditView iAuditView;
    private Context context;

    public AuditPresenterImpl(Context context, IAuditView iAuditView) {
        this.context = context;
        this.iAuditView = iAuditView;
    }

    @Override
    public void doAudit(final String taskID, final String audit_result) {
        String[] key = {"id", "level"};
        String[] value = {taskID, audit_result};

        VolleyUtil.requestWithCookie(Urls.SEND_AUDIT, key, value,
                response -> {
                    try {
                        JSONObject responseJson = new JSONObject(response);

                        iAuditView.onAuditResult(
                                responseJson.getString("status"),
                                responseJson.getString("info"));

                        EventBus.getDefault().post(
                                new UpdateViewEvent(Constants.ACTION_AUDIT, taskID, audit_result));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                volleyError -> {
                    volleyError.printStackTrace();

                    iAuditView.onAuditResult(Constants.FAILED, "审核失败，请重试");
                });
    }
}
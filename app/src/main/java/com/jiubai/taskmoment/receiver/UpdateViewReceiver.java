package com.jiubai.taskmoment.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;

/**
 * 新消息接收器
 */
public class UpdateViewReceiver extends BroadcastReceiver {
    private UpdateCallBack callBack;
    private Context context;

    @SuppressWarnings("unused")
    public UpdateViewReceiver() {
    }

    public UpdateViewReceiver(Context context, UpdateCallBack callBack) {
        this.context = context;
        this.callBack = callBack;
    }

    // 注册
    public void registerAction(String action) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(action);
        context.registerReceiver(this, filter);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()){
            case Constants.ACTION_NEWS:
                // 添加一条未读消息
                Config.NEWS_NUM++;

                if (callBack != null) {
                    callBack.updateView(intent.getStringExtra("msg"));
                }

                System.out.println(Config.NEWS_NUM);

                // 防止多次调用
                // noinspection deprecation
                context.removeStickyBroadcast(intent);
                abortBroadcast();
                break;

            case Constants.ACTION_CHANGE_NICKNAME:
                if (callBack != null) {
                    callBack.updateView(intent.getStringExtra("nickname"));
                }
                break;

            case Constants.ACTION_CHANGE_PORTRAIT:
                if (callBack != null) {
                    callBack.updateView(null);
                }
                break;

            case Constants.ACTION_DELETE_TASK:
                if (callBack != null) {
                    callBack.updateView(intent.getStringExtra("taskID"));
                }
                break;

            case Constants.ACTION_SEND_COMMENT:
                if (callBack != null) {
                    callBack.updateView(intent.getStringExtra("taskID"),
                            intent.getSerializableExtra("comment"));
                }
                break;

            case Constants.ACTION_AUDIT:
                if (callBack !=null){
                    callBack.updateView(intent.getStringExtra("taskID"),
                            intent.getStringExtra("auditResult"));
                }
                break;

            case Constants.ACTION_CHANGE_BACKGROUND:
                if (callBack != null) {
                    callBack.updateView(null);
                }
                break;
        }
    }

    public interface UpdateCallBack {
        void updateView(String msg, Object... object);
    }
}
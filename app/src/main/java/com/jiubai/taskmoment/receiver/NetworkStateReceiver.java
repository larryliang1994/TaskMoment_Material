package com.jiubai.taskmoment.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.jiubai.taskmoment.config.Config;

/**
 * 用于监听网络状态
 */
public class NetworkStateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)){
            // 获取网络连接管理器对象（系统服务对象）
            ConnectivityManager cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            // 获取网络状态
            NetworkInfo info = cm.getActiveNetworkInfo();

            Config.IS_CONNECTED = info != null && info.isAvailable();
        }

    }
}
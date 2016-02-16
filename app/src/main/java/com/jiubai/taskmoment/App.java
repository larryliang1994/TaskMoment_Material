package com.jiubai.taskmoment;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.widget.Toast;

import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.net.VolleyUtil;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.utils.L;
import com.tencent.bugly.crashreport.CrashReport;
import com.umeng.message.PushAgent;
import com.umeng.message.UmengMessageHandler;
import com.umeng.message.entity.UMessage;

import java.util.Calendar;

/**
 * 程序入口
 */
public class App extends Application {
    public static SharedPreferences sp;

    @Override
    public void onCreate() {
        super.onCreate();

        sp = getApplicationContext().getSharedPreferences(Constants.SP_FILENAME, Context.MODE_PRIVATE);

        initService();
    }

    public void initService() {
        // 启动崩溃统计
        CrashReport.initCrashReport(getApplicationContext(), "900016169", false);

        // 初始化请求队列
        VolleyUtil.initRequestQueue(getApplicationContext());

        // 初始化网络状态
        getNetworkState();

        // 读取存储好的数据——cookie,公司信息,个人信息
        loadStorageData();

        // 初始化图片加载框架
        initImageLoader();

        // 开启推送服务
        initPushAgent();
    }

    private void loadStorageData() {
        final SharedPreferences sp = getSharedPreferences(Constants.SP_FILENAME, MODE_PRIVATE);
        Config.COOKIE = sp.getString(Constants.SP_KEY_COOKIE, null);
        Config.COMPANY_NAME = sp.getString(Constants.SP_KEY_COMPANY_NAME, null);
        Config.CID = sp.getString(Constants.SP_KEY_COMPANY_ID, null);
        Config.COMPANY_BACKGROUND = sp.getString(Constants.SP_KEY_COMPANY_BACKGROUND, null);
        Config.COMPANY_CREATOR = sp.getString(Constants.SP_KEY_COMPANY_CREATOR, null);
        Config.PORTRAIT = sp.getString(Constants.SP_KEY_PORTRAIT, null);
        Config.MID = sp.getString(Constants.SP_KEY_MID, null);
        Config.NICKNAME = sp.getString(Constants.SP_KEY_NICKNAME, "昵称");
        Config.TIME = sp.getLong(Constants.SP_KEY_TIME, 0);
        if (Config.TIME == 0) {
            Config.TIME = Calendar.getInstance().getTimeInMillis();
            SharedPreferences.Editor editor = sp.edit();
            editor.putLong(Constants.SP_KEY_TIME, Config.TIME);
            editor.apply();
        }
    }

    private void initImageLoader() {
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisk(true).cacheInMemory(true).build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                getApplicationContext()).defaultDisplayImageOptions(defaultOptions)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.FIFO).build();
        L.writeLogs(false);
        ImageLoader.getInstance().init(config);
    }

    private void getNetworkState() {
        // 获取网络连接管理器对象（系统服务对象）
        ConnectivityManager cm
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // 获取网络状态
        NetworkInfo info = cm.getActiveNetworkInfo();

        Config.IS_CONNECTED = info != null && info.isAvailable();
    }

    private void initPushAgent() {
        PushAgent pushAgent = PushAgent.getInstance(getApplicationContext());
        pushAgent.enable();

        // 设置推送服务处理
        pushAgent.setMessageHandler(getMessHandler());

        // 如果不调用此方法，将会导致按照"几天不活跃"条件来推送失效。
        // 可以只在应用的主Activity中调用此方法，但是由于SDK的日志发送策略，不能保证一定可以统计到日活数据。
        PushAgent.getInstance(getApplicationContext()).onAppStart();
    }

    private UmengMessageHandler getMessHandler() {
        return new UmengMessageHandler() {
            @Override
            public void dealWithCustomMessage(final Context context, final UMessage msg) {
                new Handler(getMainLooper()).post(new Runnable() {

                    @Override
                    public void run() {
                        // 接收到推送后交给广播处理
                        Intent intent = new Intent(Constants.ACTION_NEWS);
                        intent.putExtra("msg", msg.custom);
                        // noinspection deprecation
                        context.sendStickyOrderedBroadcast(intent, null, null, 0, null, null);
                    }
                });

                System.out.println("Custom");
                Toast.makeText(getApplicationContext(), "Custom",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void dealWithNotificationMessage(final Context context, final UMessage msg) {
                new Handler(getMainLooper()).post(new Runnable() {

                    @Override
                    public void run() {
                        // 接收到推送后交给广播处理
                        Intent intent = new Intent(Constants.ACTION_NEWS);
                        intent.putExtra("msg", msg.text);
                        // noinspection deprecation
                        context.sendStickyOrderedBroadcast(intent, null, null, 0, null, null);
                    }
                });

                System.out.println("Notification");
                Toast.makeText(getApplicationContext(), "Notification",
                        Toast.LENGTH_SHORT).show();
            }


        };
    }
}
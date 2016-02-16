package com.jiubai.taskmoment.config;

import android.net.Uri;
import android.os.Environment;

import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.common.UtilBox;

import java.io.File;

/**
 * 常量
 */
@SuppressWarnings("ConstantConditions")
public class Constants {
    public static final int CODE_ADD_COMPANY = 1;
    public static final int CODE_CHANGE_COMPANY = 2;
    public static final int CODE_MULTIPLE_PICTURE = 3;
    public static final int CODE_CHECK_PICTURE = 4;
    public static final int CODE_QR_JOIN_COMPANY = 5;
    public static final int CODE_PUBLISH_TASK = 6;
    public static final int CODE_QR_ADD_MEMBER = 7;
    public static final int CODE_CHOOSE_PORTRAIT = 8;
    public static final int CODE_CHOOSE_COMPANY_BACKGROUND = 9;

    public static final String QR_TYPE_COMPANYINFO = "companyInfo";
    public static final String QR_TYPE_MEMBERINFO = "memberInfo";

    public static final String SP_FILENAME = "config";
    public static final String SP_KEY_COOKIE = "cookie";
    public static final String SP_KEY_COMPANY_NAME = "companyName";
    public static final String SP_KEY_COMPANY_ID = "companyId";
    public static final String SP_KEY_COMPANY_CREATOR = "companyCreator";
    public static final String SP_KEY_COMPANY_BACKGROUND = "companyBackground";
    public static final String SP_KEY_PORTRAIT = "portrait";
    public static final String SP_KEY_MID = "mid";
    public static final String SP_KEY_NICKNAME = "nickname";
    public static final String SP_KEY_TIME = "time";

    public static final String NAMESPACE = "taskmoment";
    public static final String DIR_PORTRAIT = "portrait/";
    public static final String DIR_BACKGROUND = "background/";
    public static final String DIR_TASK = "task/";

    public static final String ACTION_NEWS = "com.jiubai.action.news";
    public static final String ACTION_CHANGE_NICKNAME = "com.jiubai.action.change_nickname";
    public static final String ACTION_CHANGE_PORTRAIT = "com.jiubai.action.change_portrait";
    public static final String ACTION_DELETE_TASK = "com.jiubai.action.delete_task";
    public static final String ACTION_SEND_COMMENT = "com.jiubai.action.send_comment";
    public static final String ACTION_AUDIT = "com.jiubai.action.audit";
    public static final String ACTION_CHANGE_BACKGROUND = "com.jiubai.action.change_background";

    public static final Uri TEMP_FILE_LOCATION = Uri.fromFile(UtilBox.getTempFilePath("temp0.jpg"));
    public static final int SIZE_COMPANY_BACKGROUND = 500;
    public static final int SIZE_TASK_IMAGE = 300;
    public static final int SIZE_PORTRAIT = 100;

    public static final String APP_ID_WX = "1";
    public static final String APP_SECRET_WX = "1";
    public static final String APP_ID_QQ = "1";
    public static final String APP_KEY_QQ = "1";
    public static final String APP_ID_REN = "1";
    public static final String APP_KEY_REN = "1";
    public static final String APP_SECRET_REN = "1";

    public static final int IMAGETYPE_BACKGROUND = 0;
    public static final int IMAGETYPE_TASK = 1;
    public static final int IMAGETYPE_PORTRAIT = 2;

    public static final int VISIBLE = 1;
    public static final int INVISIBLE = 0;

    public static final String[] AUDIT_RESULT = {"", "", "完美解决", "普通完成", "任务失败"};

    public static final int[] COMPANY_BACKGROUND =
            {R.drawable.company_background_1, R.drawable.company_background_2, R.drawable.company_background_3};

    public static final String SUCCESS = "900001";
    public static final String NOMORE = "900900";
    public static final String FAILED = "-1";
    public static final String EXPIRE = "2";

    public static final int LOAD_NUM = 8;

    public static final int REQUEST_TIMEOUT = 10000;

    public static final String SHARE_TEXT = "任务圈 http://adm.jiubaiwang.cn/WebSite/20055/uploadfile/webeditor2/android/TaskMoment.apk";
}
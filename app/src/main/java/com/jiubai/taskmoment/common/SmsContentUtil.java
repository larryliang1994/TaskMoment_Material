package com.jiubai.taskmoment.common;

import android.app.Activity;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.widget.EditText;

public class SmsContentUtil extends ContentObserver {

    private Activity activity = null;

    private EditText verifyText = null;

    public SmsContentUtil(Activity activity, Handler handler, EditText verifyText) {
        super(handler);
        this.activity = activity;
        this.verifyText = verifyText;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        String[] projection = new String[] { "_id", "address", "person",
                "body", "date", "type" };
        Cursor cursor = activity.managedQuery(Uri.parse("content://sms/inbox"), projection,
                null, null, "date desc");

        while (cursor.moveToNext()) {
            String smsBody = cursor.getString(cursor.getColumnIndex("body"));

            if(smsBody.contains("【任务圈】")) {
                String smsContent = UtilBox.getDynamicPassword(smsBody);

                verifyText.setText(smsContent);
                verifyText.setSelection(verifyText.getText().toString().trim().length());

                break;
            }
        }
    }
}
package com.jiubai.taskmoment.receiver;

public class UpdateViewEvent {
    private String mAction;
    private String mStringExtra;
    private Object mSerializableExtra;

    public UpdateViewEvent(String action) {
        mAction = action;
    }

    public UpdateViewEvent(String action, String stringExtra) {
        this.mAction = action;
        this.mStringExtra = stringExtra;
    }

    public UpdateViewEvent(String action, String stringExtra, Object serializableExtra) {
        this.mAction = action;
        this.mStringExtra = stringExtra;
        this.mSerializableExtra = serializableExtra;
    }

    public String getStringExtra() {
        return mStringExtra;
    }

    public void setStringExtra(String mStringExtra) {
        this.mStringExtra = mStringExtra;
    }

    public Object getSerializableExtra() {
        return mSerializableExtra;
    }

    public void setSerializableExtra(Object mSerializableExtra) {
        this.mSerializableExtra = mSerializableExtra;
    }

    public String getAction() {
        return mAction;
    }

    public void setAction(String action) {
        mAction = action;
    }
}

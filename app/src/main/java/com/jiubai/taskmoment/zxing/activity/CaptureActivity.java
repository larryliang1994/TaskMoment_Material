/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jiubai.taskmoment.zxing.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.zxing.Result;
import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.common.UtilBox;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.config.Urls;
import com.jiubai.taskmoment.net.VolleyUtil;
import com.jiubai.taskmoment.zxing.camera.CameraManager;
import com.jiubai.taskmoment.zxing.decode.DecodeThread;
import com.jiubai.taskmoment.zxing.utils.BeepManager;
import com.jiubai.taskmoment.zxing.utils.CaptureActivityHandler;
import com.jiubai.taskmoment.zxing.utils.InactivityTimer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Field;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.drakeet.materialdialog.MaterialDialog;

public final class CaptureActivity extends Activity implements SurfaceHolder.Callback {
    @Bind(R.id.capture_preview)
    SurfaceView mScanSurfaceView;

    @Bind(R.id.capture_container)
    RelativeLayout mScanContainerRelativeLayout;

    @Bind(R.id.capture_crop_view)
    RelativeLayout mScanCropViewRelativeLayout;

    @Bind(R.id.capture_scan_line)
    ImageView mScanLineImageView;

    private static final String TAG = CaptureActivity.class.getSimpleName();

    private CameraManager mCameraManager;
    private CaptureActivityHandler mCaptureActivityHandler;
    private InactivityTimer mInactivityTimer;
    private BeepManager mBeepManager;

    private Rect mCropRect = null;
    private boolean isHasSurface = false;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.aty_capture);

        ButterKnife.bind(this);

        mInactivityTimer = new InactivityTimer(this);
        mBeepManager = new BeepManager(this);

        TranslateAnimation animation = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.9f);
        animation.setDuration(4500);
        animation.setRepeatCount(-1);
        animation.setRepeatMode(Animation.RESTART);
        mScanLineImageView.startAnimation(animation);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // CameraManager must be initialized here, not in onCreate().
        mCameraManager = new CameraManager(getApplication());

        mCaptureActivityHandler = null;

        if (isHasSurface) {
            // The activity was paused but not stopped, so the surface still
            // exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            initCamera(mScanSurfaceView.getHolder());
        } else {
            // Install the callback and wait for surfaceCreated() to init the
            // camera.
            mScanSurfaceView.getHolder().addCallback(this);
        }

        mInactivityTimer.onResume();
    }

    @Override
    protected void onPause() {
        if (mCaptureActivityHandler != null) {
            mCaptureActivityHandler.quitSynchronously();
            mCaptureActivityHandler = null;
        }
        mInactivityTimer.onPause();
        mBeepManager.close();
        mCameraManager.closeDriver();
        if (!isHasSurface) {
            mScanSurfaceView.getHolder().removeCallback(this);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {

        mScanSurfaceView.getHolder().removeCallback(this);
        mScanSurfaceView.getHolder().getSurface().release();
        mInactivityTimer.shutdown();
        mCameraManager.closeDriver();
        mCameraManager.stopPreview();

        super.onDestroy();
    }

    public Handler getHandler() {
        return mCaptureActivityHandler;
    }

    public CameraManager getCameraManager() {
        return mCameraManager;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!isHasSurface) {
            isHasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isHasSurface = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    /**
     * A valid barcode has been found, so give an indication of success and show
     * the results.
     *
     * @param rawResult The contents of the barcode.
     * @param bundle    The extras
     */
    public void handleDecode(Result rawResult, Bundle bundle) {
        mInactivityTimer.onActivity();
        mBeepManager.playBeepSoundAndVibrate();

        bundle.putInt("width", mCropRect.width());
        bundle.putInt("height", mCropRect.height());
        bundle.putString("result", rawResult.getText());

        try {
            final JSONObject jsonObject = new JSONObject(rawResult.getText());

            switch (jsonObject.getString("type")) {
                case Constants.QR_TYPE_COMPANYINFO:
                    joinCompany(jsonObject);
                    break;

                case Constants.QR_TYPE_MEMBERINFO:
                    addMember(jsonObject);
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void addMember(JSONObject jsonObject) throws JSONException {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("解析中");
        final MaterialDialog dialog = new MaterialDialog(this);
        dialog.setMessage("真的要添加" + jsonObject.getString("name") + "吗");
        dialog.setPositiveButton("真的", v -> {
            dialog.dismiss();
            progressDialog.show();

            try {
                String[] key = {"mobile", "cid"};
                String[] value = {jsonObject.getString("mobile"), Config.CID};

                VolleyUtil.requestWithCookie(Urls.ADD_MEMBER, key, value,
                        response -> {
                            progressDialog.dismiss();

                            try {
                                JSONObject obj = new JSONObject(response);

                                if (Constants.SUCCESS.equals(obj.getString("status"))) {
                                    CaptureActivity.this.setResult(RESULT_OK);
                                } else {
                                    CaptureActivity.this.setResult(RESULT_CANCELED);
                                    Toast.makeText(this, obj.getString("info"), Toast.LENGTH_SHORT).show();
                                }

                                CaptureActivity.this.finish();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        },
                        volleyError -> {
                            UtilBox.showSnackbar(this, "添加失败，请重试");
                            progressDialog.dismiss();
                        });

            } catch (JSONException e) {
                e.printStackTrace();
                progressDialog.dismiss();

                UtilBox.showSnackbar(this, "添加失败，请重试");

                CaptureActivity.this.setResult(RESULT_CANCELED);
                CaptureActivity.this.finish();
            }
        });
        dialog.setNegativeButton("假的", v -> {
            dialog.dismiss();
        });
        dialog.show();
    }

    private void joinCompany(final JSONObject jsonObject) throws JSONException {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("解析中");
        final MaterialDialog dialog = new MaterialDialog(this);
        dialog.setMessage("真的要加入" + jsonObject.getString("name") + "吗");
        dialog.setPositiveButton("真的", v -> {
            dialog.dismiss();
            progressDialog.show();

            try {
                String[] key = {"mobile", "cid"};
                String[] value = {Config.MOBILE, jsonObject.getString("cid")};

                VolleyUtil.requestWithCookie(Urls.ADD_MEMBER, key, value,
                        response -> {
                            progressDialog.dismiss();

                            try {
                                JSONObject obj = new JSONObject(response);

                                if (Constants.SUCCESS.equals(obj.getString("status"))) {
                                    CaptureActivity.this.setResult(RESULT_OK);
                                } else {
                                    CaptureActivity.this.setResult(RESULT_CANCELED);
                                    Toast.makeText(this, obj.getString("info"), Toast.LENGTH_SHORT).show();
                                }

                                CaptureActivity.this.finish();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        },
                        volleyError -> {
                            UtilBox.showSnackbar(this, "加入失败，请重试");
                            progressDialog.dismiss();
                        });

            } catch (JSONException e) {
                e.printStackTrace();
                progressDialog.dismiss();

                UtilBox.showSnackbar(this, "加入失败，请重试");

                CaptureActivity.this.setResult(RESULT_CANCELED);
                CaptureActivity.this.finish();
                overridePendingTransition(R.anim.scale_stay, R.anim.out_left_right);
            }
        });
        dialog.setNegativeButton("假的", v -> {
            dialog.dismiss();
        });
        dialog.show();
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (mCameraManager.isOpen()) {
            Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
            return;
        }
        try {
            mCameraManager.openDriver(surfaceHolder);
            // Creating the handler starts the preview, which can also throw a
            // RuntimeException.
            if (mCaptureActivityHandler == null) {
                mCaptureActivityHandler = new CaptureActivityHandler(this, mCameraManager, DecodeThread.ALL_MODE);
            }

            initCrop();
        } catch (IOException e) {
            displayFrameworkBugMessageAndExit();
            System.out.println("io");
            e.printStackTrace();
        } catch (RuntimeException e) {
            displayFrameworkBugMessageAndExit();
            System.out.println("runtime");
            e.printStackTrace();
        }
    }

    private void displayFrameworkBugMessageAndExit() {
        // camera error
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.app_name));
        builder.setMessage("相机打开出错，请稍后重试");
        builder.setPositiveButton("确定", (dialog, which) -> {
            finish();
        });
        builder.setOnCancelListener(dialog -> finish());
        builder.show();
    }

    @SuppressWarnings("unused")
    public void restartPreviewAfterDelay(long delayMS) {
        if (mCaptureActivityHandler != null) {
            mCaptureActivityHandler.sendEmptyMessageDelayed(R.id.restart_preview, delayMS);
        }
    }

    public Rect getCropRect() {
        return mCropRect;
    }

    /**
     * 初始化截取的矩形区域
     */
    @SuppressWarnings("SuspiciousNameCombination")
    private void initCrop() {
        int cameraWidth = mCameraManager.getCameraResolution().y;
        int cameraHeight = mCameraManager.getCameraResolution().x;

        /** 获取布局中扫描框的位置信息 */
        int[] location = new int[2];
        mScanCropViewRelativeLayout.getLocationInWindow(location);

        int cropLeft = location[0];
        int cropTop = location[1] - getStatusBarHeight();

        int cropWidth = mScanCropViewRelativeLayout.getWidth();
        int cropHeight = mScanCropViewRelativeLayout.getHeight();

        /** 获取布局容器的宽高 */
        int containerWidth = mScanContainerRelativeLayout.getWidth();
        int containerHeight = mScanContainerRelativeLayout.getHeight();

        /** 计算最终截取的矩形的左上角顶点x坐标 */
        int x = cropLeft * cameraWidth / containerWidth;
        /** 计算最终截取的矩形的左上角顶点y坐标 */
        int y = cropTop * cameraHeight / containerHeight;

        /** 计算最终截取的矩形的宽度 */
        int width = cropWidth * cameraWidth / containerWidth;
        /** 计算最终截取的矩形的高度 */
        int height = cropHeight * cameraHeight / containerHeight;

        /** 生成最终的截取的矩形 */
        mCropRect = new Rect(x, y, width + x, height + y);
    }

    private int getStatusBarHeight() {
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object obj = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = Integer.parseInt(field.get(obj).toString());
            return getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @OnClick({R.id.iBtn_back})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iBtn_back:
                finish();
                break;
        }
    }
}
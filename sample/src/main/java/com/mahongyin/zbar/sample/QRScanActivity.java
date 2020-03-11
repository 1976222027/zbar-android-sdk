package com.mahongyin.zbar.sample;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;

import com.mahongyin.zbar.camera.CameraManager;
import com.mahongyin.zbar.camera.CameraPreview;
import com.mahongyin.zbar.camera.ScanCallback;
import com.mahongyin.zbar.sample.R;

/**
 * <p>Scan UI.</p>
 * Created by Yan Zhenjie on 2017/5/10.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class QRScanActivity extends AppCompatActivity {
    public static final int REQUEST_CAMERA = 120; //相机请求码
    private RelativeLayout mScanCropView;
    private ImageView mScanLine;
    private ValueAnimator mScanAnimator;
    private CameraPreview mPreviewView;
    Context context;
    Camera camera;//zbar 已经初始化了吧
    //    CameraManager mCameraManager;
    //FlashUtils utils;
    Switch flashControl;
    String mCameraId;

    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scan);
        context = this;
//        checkReadPermission(Manifest.permission.CAMERA, REQUEST_CAMERA);
        mPreviewView = (CameraPreview) findViewById(R.id.capture_preview);
        mScanCropView = (RelativeLayout) findViewById(R.id.capture_crop_view);
        mScanLine = (ImageView) findViewById(R.id.capture_scan_line);
        mPreviewView.setScanCallback(resultCallback);
//        mCameraManager = new CameraManager(context);
        //utils = new FlashUtils(this);

        flashControl = findViewById(R.id.flash_control);
        flashControl.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {//打开
                // utils.open();
                // openFlashLight();
                try {
                    // camera = Camera.open();
//                    camera= mCameraManager.getCamera();
                    camera = CameraManager.getCamera();
                    Camera.Parameters mParameters;
                    mParameters = camera.getParameters();
                    mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    camera.setParameters(mParameters);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            } else {
                // closeFlashLight();
                // utils.close();
                try {
                    Camera.Parameters mParameters;
                    mParameters = camera.getParameters();
                    mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    camera.setParameters(mParameters);
                    //camera.release();//扫码还要用 销毁时再re
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });

    }

    /**
     * 打开闪光灯
     */
    public void openFlashLight() {

        if (camera == null) {
//            return;
            camera = CameraManager.getCamera();
        }
//        camera = CameraManager.getCamera();
        Camera.Parameters parameter = camera.getParameters();
        parameter.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        camera.setParameters(parameter);
    }

    /**
     * 关闭闪光灯
     */
    public void closeFlashLight() {
        if (camera == null) {
//            return;
            camera = CameraManager.getCamera();
        }
        Camera.Parameters parameter = camera.getParameters();
        parameter.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        camera.setParameters(parameter);//camera.getCamera()
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (camera != null) {
//            camera.setPreviewCallback(null);
//            camera.stopPreview();
            camera.release();
            camera = null;
            //回收camera
            //页面销毁时关闭灯光
        }
    }

    /**
     * Accept scan result.
     */
    private ScanCallback resultCallback = new ScanCallback() {
        @Override
        public void onScanResult(String result) {
            stopScan();
            //  Toast.makeText(QRScanActivity.this, "扫描内容："+result, Toast.LENGTH_LONG).show();
            Intent resultIntent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putString("result", result);
            // bundle.putParcelable("bitmap", barcode);
            resultIntent.putExtras(bundle);
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (mScanAnimator != null) {
            checkReadPermission(Manifest.permission.CAMERA, REQUEST_CAMERA);//启动时就open camera了
        }
    }

    @Override
    public void onPause() {
        // Must be called here, otherwise the camera should not be released properly.
        stopScan();
        super.onPause();
    }

    /**
     * 判断是否有某项权限
     *
     * @param string_permission 权限
     * @param request_code      请求码
     */
    public boolean checkReadPermission(String string_permission, int request_code) {//通过请求码判断
        boolean flag = false;
        //M 以下版本是不是直接就是已有权限？
        if (ContextCompat.checkSelfPermission(this, string_permission) == PackageManager.PERMISSION_GRANTED) {//已有权限
            flag = true;
            startScanWithPermission();//有权就go
        } else {//没权限 则申请权限
            ActivityCompat.requestPermissions(this, new String[]{string_permission}, request_code);
        }
        return flag;
    }

    /**
     * There is a camera when the direct scan.
     */
    private void startScanWithPermission() {
        if (mPreviewView.start()) {
            mScanAnimator.start();
            //进行扫描扫完后 Toast 该finish 回传值
        } else {//相机被占用
            new AlertDialog.Builder(this)
                    .setTitle(R.string.camera_failure)
                    .setMessage(R.string.camera_hint)
                    .setCancelable(false)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .show();
        }
    }

    /**
     * Stop scan.
     */
    private void stopScan() {
        mScanAnimator.cancel();
        mPreviewView.stop();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (mScanAnimator == null) {
            int height = mScanCropView.getMeasuredHeight() - 25;
            mScanAnimator = ObjectAnimator.ofFloat(mScanLine, "translationY", 0F, height).setDuration(3000);
            mScanAnimator.setInterpolator(new LinearInterpolator());
            mScanAnimator.setRepeatCount(ValueAnimator.INFINITE);
            mScanAnimator.setRepeatMode(ValueAnimator.REVERSE);
            checkReadPermission(Manifest.permission.CAMERA, REQUEST_CAMERA);
        }
    }
}


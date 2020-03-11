package com.mahongyin.zbar.camera;

import android.content.Context;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import java.io.IOException;

/**
 * <p>Camera manager.</p>
 * Created by Yan Zhenjie on 2017/5/10.
 */
public final class CameraManager {

    private final CameraConfiguration mConfiguration;

    private  Camera mCamera;// ADD: 2019/2/13 新添加:增加static  20200311取消

    public CameraManager(Context context) {
        this.mConfiguration = new CameraConfiguration(context);
    }

    /**
     * Opens the mCamera driver and initializes the hardware parameters.
     *
     * @throws Exception ICamera open failed, occupied or abnormal.
     */
    public synchronized void openDriver() throws Exception {
        if (mCamera != null) return;

        mCamera = Camera.open();
        if (mCamera == null) throw new IOException("The camera is occupied.");

        mConfiguration.initFromCameraParameters(mCamera);

        Camera.Parameters parameters = mCamera.getParameters();
        String parametersFlattened = parameters == null ? null : parameters.flatten();
        try {
            mConfiguration.setDesiredCameraParameters(mCamera, false);
        } catch (RuntimeException re) {
            if (parametersFlattened != null) {
                parameters = mCamera.getParameters();
                parameters.unflatten(parametersFlattened);
                try {
                    mCamera.setParameters(parameters);
                    mConfiguration.setDesiredCameraParameters(mCamera, true);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }
        }

    }
    /**
     * Closes the camera driver if still in use.
     */
    public synchronized void closeDriver() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * Camera is opened.
     *
     * @return true, other wise false.
     */
    public boolean isOpen() {
        return mCamera != null;
    }

    /**
     * Get camera configuration.
     *
     * @return {@link CameraConfiguration}.
     */
    public CameraConfiguration getConfiguration() {
        return mConfiguration;
    }

    /**
     * Camera start preview.
     *
     * @param holder          {@link SurfaceHolder}.
     * @param previewCallback {@link Camera.PreviewCallback}.
     * @throws IOException if the method fails (for example, if the surface is unavailable or unsuitable).
     */
    public void startPreview(SurfaceHolder holder, Camera.PreviewCallback previewCallback) throws IOException {
        if (mCamera != null) {
            mCamera.setDisplayOrientation(90);
            mCamera.setPreviewDisplay(holder);
            mCamera.setPreviewCallback(previewCallback);
            mCamera.startPreview();
        }
    }

    /**
     * Camera stop preview.
     */
    public void stopPreview() {
        if (mCamera != null) {
            try {
                mCamera.stopPreview();
            } catch (Exception ignored) {
                // nothing.
            }
            try {
                mCamera.setPreviewDisplay(null);
            } catch (IOException ignored) {
                // nothing.
            }
        }
    }

    /**
     * Focus on, make a scan action.
     *
     * @param callback {@link Camera.AutoFocusCallback}.
     */
    public void autoFocus(Camera.AutoFocusCallback callback) {
        if (mCamera != null)
            try {
                mCamera.autoFocus(callback);
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    /**
     * 获取相机的方法 //调用闪光灯需要和扫码同一camera
     * @return mCamera
     */
    public  Camera getCamera(){
        return mCamera;
    }
    /*
     *闪光灯调开关用完 在你的Activity 的onDestroy()方法里记得 加上 mCamera.release();【页面销毁时关闭闪光灯】
     */
    
    /**
     * 打开闪光灯  //开启camera闪光灯
     */
    public void openFlashLight() {

            if (mCamera == null) {
                return;
            }
//             获取对象 getCamera();
           // mCamera.startPreview();//扫码已开启 单纯闪光灯 不要这句
            Camera.Parameters parameter = mCamera.getParameters();
            parameter.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            mCamera.setParameters(parameter);
        }

    /**
     * 关闭闪光灯//关闭camera闪光灯
     */
    public void closeFlashLight() {
            if (mCamera == null) {
                return;
            }
            Camera.Parameters parameter = mCamera.getParameters();
            parameter.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(parameter);

//        mCamera.stopPreview();//扫码还要用camera 单纯闪光灯 不要这句
//        mCamera = null;

//        mCamera.release();//ADD 调用时在页面销毁onDestroy()里再加这1句  closeDriver()已经加
//        @Override
//        protected void onDestroy() {
//            super.onDestroy();
//            if (camera != null) {
////            camera.setPreviewCallback(null);
////            camera.stopPreview();
//                camera.release();
//                camera = null;
//                //回收camera
//                //页面销毁时关闭灯光
//            }
//        }

    }

}

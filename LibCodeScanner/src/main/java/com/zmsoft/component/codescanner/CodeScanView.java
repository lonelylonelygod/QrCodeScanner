package com.zmsoft.component.codescanner;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.zxing.Result;
import com.zmsoft.component.codescanner.camera.CameraManager;
import com.zmsoft.component.codescanner.decode.CaptureActivityHandler;
import com.zmsoft.component.codescanner.decode.DecodeHandlerDelegate;

import java.io.IOException;

public class CodeScanView extends RelativeLayout implements SurfaceHolder.Callback{
    protected SurfaceView mSurfaceView;
    protected ScanBoxView mScanBoxView;
    protected ScanResultCallback mScanResultCallback;
    private boolean mHasSurface;
    private CaptureActivityHandler mCaptureActivityHandler;
    private DecodeHandlerDelegate mDecodeHandlerDelegate;

    public CodeScanView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public CodeScanView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        CameraManager.init(getContext());

        mSurfaceView = new SurfaceView(getContext());
        mSurfaceView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        mSurfaceView.setId(R.id.bgaqrcode_camera_preview);
        addView(mSurfaceView);

        mScanBoxView = new ScanBoxView(getContext());
        mScanBoxView.initCustomAttrs(context, attrs);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(context, attrs);
        layoutParams.addRule(RelativeLayout.ALIGN_TOP, R.id.bgaqrcode_camera_preview);
        layoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.bgaqrcode_camera_preview);
        addView(mScanBoxView, layoutParams);
    }

    /**
     * 设置扫描二维码的代理
     *
     * @param scanResultCallback 扫描二维码的代理
     */
    public void setScanResultCallback(ScanResultCallback scanResultCallback) {
        mScanResultCallback = scanResultCallback;
    }

    public ScanBoxView getScanBoxView() {
        return mScanBoxView;
    }

    /**
     * 显示扫描框
     */
    public void showScanRect() {
        if (mScanBoxView != null) {
            mScanBoxView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 隐藏扫描框
     */
    public void hiddenScanRect() {
        if (mScanBoxView != null) {
            mScanBoxView.setVisibility(View.GONE);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    /* 检测相机是否存在 */
    private boolean checkCameraHardWare(Context context) {
        PackageManager packageManager = context.getPackageManager();
        return packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!mHasSurface) {
            mHasSurface = true;
            initCamera(holder);
        }
    }

    public void onViewResumed(){
        SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
        if (mHasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mHasSurface = false;
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);
        } catch (IOException e) {
            // 基本不会出现相机不存在的情况
            Toast.makeText(getContext(), getContext().getString(R.string.qr_code_camera_not_found), Toast.LENGTH_SHORT).show();
            return;
        } catch (RuntimeException re) {
            re.printStackTrace();
            return;
        }
        mSurfaceView.setVisibility(View.VISIBLE);
        if (mDecodeHandlerDelegate == null) {
            mDecodeHandlerDelegate = new MyDecodeDelegate();
        }
        if (mCaptureActivityHandler == null) {
            mCaptureActivityHandler = new CaptureActivityHandler(mDecodeHandlerDelegate);
        }
    }


    /**
     * 关闭摄像头预览，并且隐藏扫描框
     */
    public void stopCamera() {
        try {
            if (mCaptureActivityHandler != null) {
                mCaptureActivityHandler.quitSynchronously();
                mCaptureActivityHandler = null;
            }
            CameraManager.get().closeDriver();
        } catch (Exception e) {
        }
    }

    /**
     * 暂停识别内容
     */
    public void pauseScan(){
        if (mCaptureActivityHandler != null) {
            mCaptureActivityHandler.pauseDecode();
        }
    }

    /**
     * 暂停识别内容, 并在一定时间后自动重新开始识别
     *
     *  @param delay 暂停的时间 单位是毫秒
     */
    public void pauseScan(long delay){
        if (mCaptureActivityHandler != null) {
            mCaptureActivityHandler.pauseDecode(delay);
        }
    }


    /**
     * 重行开始识别内容
     */
    public void restartScan(){
        if (null != mCaptureActivityHandler) {
            mCaptureActivityHandler.restartPreviewAndDecode();
        }
    }

    /**
     * 打开闪光灯
     */
    public void openFlashlight() {
        CameraManager.get().setFlashLight(true);
    }

    /**
     * 关闭散光灯
     */
    public void closeFlashlight() {
        CameraManager.get().setFlashLight(false);
    }

    /**
     * 销毁二维码扫描控件
     */
    public void onDestroy() {
        stopCamera();
        mScanResultCallback = null;
    }


    /**
     * 切换成扫描条码样式
     */
    public void changeToScanBarcodeStyle() {
        if (!mScanBoxView.getIsBarcode()) {
            mScanBoxView.setIsBarcode(true);
        }
    }

    /**
     * 切换成扫描二维码样式
     */
    public void changeToScanQRCodeStyle() {
        if (mScanBoxView.getIsBarcode()) {
            mScanBoxView.setIsBarcode(false);
        }
    }

    /**
     * 当前是否为条码扫描样式
     *
     * @return
     */
    public boolean getIsScanBarcodeStyle() {
        return mScanBoxView.getIsBarcode();
    }

    private class MyDecodeDelegate extends DecodeHandlerDelegate{

        @Override
        protected Context getDelegateContext() {
            return getContext();
        }

        @Override
        protected void handleDecode(Result result) {
            mScanResultCallback.onScanQRCodeSuccess(result.getText());
        }
    }

    public interface ScanResultCallback {
        /**
         * 处理扫描结果
         *
         * @param result
         */
        void onScanQRCodeSuccess(String result);

        /**
         * 处理打开相机出错
         */
        void onScanQRCodeOpenCameraError();
    }
}
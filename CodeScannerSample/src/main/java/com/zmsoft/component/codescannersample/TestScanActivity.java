package com.zmsoft.component.codescannersample;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.zmsoft.component.codescanner.CodeScanView;
import com.zmsoft.component.codescanner.decode.DecodeImageCallback;
import com.zmsoft.component.codescanner.decode.DecodeImageThread;
import com.zmsoft.component.codescanner.utils.ImagePicker;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class TestScanActivity extends AppCompatActivity implements CodeScanView.ScanResultCallback {
    public static final int MSG_DECODE_SUCCEED = 1;
    public static final int MSG_DECODE_FAIL = 2;
    private static final String TAG = TestScanActivity.class.getSimpleName();
    private static final int PICK_IMAGE_ID = 234; // the number doesn't matter
    private CodeScanView mCodeScanView;
    private ExecutorService mQrCodeExecutor;
    private Handler mImageDecodeHandler;
    private DecodeImageCallback mDecodeImageCallback = new DecodeImageCallback() {
        @Override
        public void decodeSucceed(String result) {
            mImageDecodeHandler.obtainMessage(MSG_DECODE_SUCCEED, result).sendToTarget();
        }

        @Override
        public void decodeFail(int type, String reason) {
            mImageDecodeHandler.sendEmptyMessage(MSG_DECODE_FAIL);
        }
    };

    public void onPickImage(View view) {
        Intent chooseImageIntent = ImagePicker.getPickOnlyIntent(this);
        startActivityForResult(chooseImageIntent, PICK_IMAGE_ID);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_scan);

        mCodeScanView = (CodeScanView) findViewById(R.id.zxingview);
        mCodeScanView.setScanResultCallback(this);
        mQrCodeExecutor = Executors.newSingleThreadExecutor();
        mImageDecodeHandler = new WeakHandler(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCodeScanView.onViewResumed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCodeScanView.stopCamera();
    }

    @Override
    protected void onDestroy() {
        mCodeScanView.onDestroy();
        super.onDestroy();
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(200);
    }

    @Override
    public void onScanQRCodeSuccess(String result) {
        Log.i(TAG, "result:" + result);
        Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
        vibrate();
        mCodeScanView.restartScan();
    }

    @Override
    public void onScanQRCodeOpenCameraError() {
        Log.e(TAG, "打开相机出错");
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_spot:
                mCodeScanView.restartScan();
                break;
            case R.id.stop_spot:
                mCodeScanView.pauseScan(2000);
                break;
            case R.id.show_rect:
                mCodeScanView.showScanRect();
                break;
            case R.id.hidden_rect:
                mCodeScanView.hiddenScanRect();
                break;
            case R.id.open_flashlight:
                mCodeScanView.openFlashlight();
                break;
            case R.id.close_flashlight:
                mCodeScanView.closeFlashlight();
                break;
            case R.id.scan_barcode:
                mCodeScanView.changeToScanBarcodeStyle();
                break;
            case R.id.scan_qrcode:
                mCodeScanView.changeToScanQRCodeStyle();
                break;
            case R.id.choose_qrcde_from_gallery:
                onPickImage(v);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case PICK_IMAGE_ID:
                Bitmap bitmap = ImagePicker.getImageFromResult(this, resultCode, data);
                if (null != mQrCodeExecutor) {
                    mQrCodeExecutor.execute(new DecodeImageThread(bitmap, mDecodeImageCallback));
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private static class WeakHandler extends Handler {
        private WeakReference<TestScanActivity> mWeakQrCodeActivity;

        public WeakHandler(TestScanActivity imagePickerActivity) {
            super();
            this.mWeakQrCodeActivity = new WeakReference<>(imagePickerActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_DECODE_SUCCEED:
                    String result = (String) msg.obj;
                    if (TextUtils.isEmpty(result)) {
                    } else {
                        handleResult(result);
                    }
                    break;
                case MSG_DECODE_FAIL:
                    Toast.makeText(mWeakQrCodeActivity.get(), "未能识别图中二维码", Toast.LENGTH_SHORT).show();
                    break;
            }
            super.handleMessage(msg);
        }

        private void handleResult(String resultString) {
            TestScanActivity imagePickerActivity = mWeakQrCodeActivity.get();
            imagePickerActivity.onScanQRCodeSuccess(resultString);
        }

    }

}
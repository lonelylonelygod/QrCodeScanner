package com.zmsoft.component.codescannersample;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.UiThread;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;

import com.zmsoft.component.codescanner.decode.QRCodeEncoder;
import com.zmsoft.component.codescanner.utils.UiUtils;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    private static final int REQUEST_CODE_QRCODE_PERMISSIONS = 1;
    private ExecutorService mExecutor;
    private TextInputEditText mTextInputEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextInputEditText = ((TextInputEditText)findViewById(R.id.text_input));
        mTextInputEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    String content = mTextInputEditText.getText().toString().trim();
                    generateBitmap(content);
                    return true;
                }
                return false;
            }
        });
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.test_scan_qrcode:
                startActivity(new Intent(this, TestScanActivity.class));
                break;
            case R.id.test_generate_qrcode:
                String content = mTextInputEditText.getText().toString().trim();
                generateBitmap(content);
                break;
            default:
                break;
        }
    }

    private void generateBitmap(final String content){
        if (TextUtils.isEmpty(content)) {
            return;
        }
        final int px = UiUtils.dp2px(this, 150);
        if (null == mExecutor) {
            mExecutor = Executors.newSingleThreadExecutor();
        }
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final Bitmap bitmap = QRCodeEncoder
                        .syncEncodeQRCode(content, px);
                setQrBitmap(bitmap);
            }
        });
    }

    @UiThread
    void setQrBitmap(final Bitmap bitmap){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((ImageView) findViewById(R.id.image_view)).setImageBitmap(bitmap);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mExecutor != null && !mExecutor.isShutdown() && !mExecutor.isTerminated()) {
            mExecutor.shutdown();
            mExecutor = null;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        requestCodeQRCodePermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
    }

    @AfterPermissionGranted(REQUEST_CODE_QRCODE_PERMISSIONS)
    private void requestCodeQRCodePermissions() {
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, "扫描二维码需要打开相机和散光灯的权限", REQUEST_CODE_QRCODE_PERMISSIONS, perms);
        }
    }
}

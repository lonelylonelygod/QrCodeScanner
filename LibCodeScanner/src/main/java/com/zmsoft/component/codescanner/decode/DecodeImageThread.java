package com.zmsoft.component.codescanner.decode;

import android.graphics.Bitmap;

/**
 * Created by xingli on 1/4/16.
 *
 * 解析图像二维码线程
 */
public class DecodeImageThread implements Runnable {
    private Bitmap mBitmap;
    private DecodeImageCallback mCallback;

    public DecodeImageThread(Bitmap bitmap, DecodeImageCallback callback) {
        mBitmap = bitmap;
        mCallback = callback;
    }

    @Override
    public void run() {
        if (mBitmap == null) {
            if (null != mCallback) {
                mCallback.decodeFail(0, "No image data");
            }
            return;
        }
        final String result = QRCodeDecoder.syncDecodeQRCode(mBitmap);

        if (null != mCallback) {
            if (null != result) {
                mCallback.decodeSucceed(result);
            } else {
                mCallback.decodeFail(0, "Decode image failed.");
            }
        }
    }
}

package com.zmsoft.component.codescanner.decode;

import android.content.Context;

import com.google.zxing.Result;

/**
 * ProjectName:CodeScanner
 * Created by Jiaozi
 * on 26/06/2017.
 */
public abstract class DecodeHandlerDelegate {

    private CaptureActivityHandler mCaptureActivityHandler;

    protected abstract Context getDelegateContext();
    protected abstract void handleDecode(Result result);

    public CaptureActivityHandler getCaptureActivityHandler() {
        return mCaptureActivityHandler;
    }

    public void setCaptureActivityHandler(CaptureActivityHandler captureActivityHandler) {
        mCaptureActivityHandler = captureActivityHandler;
    }
}

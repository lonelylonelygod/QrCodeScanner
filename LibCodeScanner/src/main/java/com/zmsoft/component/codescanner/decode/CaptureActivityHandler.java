/*
 * Copyright (C) 2008 ZXing authors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.zmsoft.component.codescanner.decode;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.zxing.Result;
import com.zmsoft.component.codescanner.R;
import com.zmsoft.component.codescanner.camera.CameraManager;

import java.util.ArrayList;

/**
 * This class handles all the messaging which comprises the state machine for capture.
 */
public final class CaptureActivityHandler extends Handler {

    private final DecodeThread mDecodeThread;
//    private final DecodeThread mOpenCvDecodeThread;

    private DecodeHandlerDelegate mDecodeHandlerDelegate;
    private State mState;
//    private LruCache<byte[], String> mResultCache = new LruCache<>(10);

    public CaptureActivityHandler(DecodeHandlerDelegate decodeHandlerDelegate) {
        mDecodeHandlerDelegate = decodeHandlerDelegate;
        mDecodeHandlerDelegate.setCaptureActivityHandler(this);
        mDecodeThread = new DecodeThread(mDecodeHandlerDelegate);
//        mOpenCvDecodeThread = new DecodeThread(mDecodeHandlerDelegate, DecodeThread.DECODE_THREAD_TYPE_OPENCV);
        mDecodeThread.start();
//        mOpenCvDecodeThread.start();

        mState = State.SUCCESS;
        // Start ourselves capturing previews and decoding.
        restartPreviewAndDecode();
    }

    @Override
    public void handleMessage(Message message) {
        if (message.what == R.id.auto_focus) {// When one auto focus pass finishes, start another. This is the closest thing to
            // continuous AF. It does seem to hunt a bit, but I'm not sure what else to do.
            Log.d("CaptureHandleMessage", "auto_focus");
            if (mState == State.PREVIEW) {
                CameraManager.get().requestAutoFocus(this, R.id.auto_focus);
            }
        } else if (message.what == R.id.decode_succeeded) {
            mState = State.SUCCESS;
            Result result = (Result) message.obj;
            Log.d("CaptureHandleMessage", "decode_succeeded");
//            if (result.getRawBytes() != null && mResultCache.get(result.getRawBytes()) == null) {
//                Log.d("CaptureHandleMessage", "result.getRawBytes() value: " + mResultCache.get(result.getRawBytes()));
//                mResultCache.put(result.getRawBytes(), result.getText());
                mDecodeHandlerDelegate.handleDecode(result);
//            } else if(result.getRawBytes() != null && mResultCache.get(result.getRawBytes()) != null){
//                Log.d("CaptureHandleMessage", "Cache on handle!!!!!!");
//                restartPreviewAndDecode();
//            } else {
//                restartPreviewAndDecode();
//            }
        } else if (message.what == R.id.decode_failed) {// We're decoding as fast as possible, so when one decode fails, start another.
            Log.d("CaptureHandleMessage", "decode_failed");
            mState = State.PREVIEW;
            CameraManager.get().requestPreviewFrame(getHandlers(), R.id.decode);
        } else if (message.what == R.id.decode_restart) {
            restartPreviewAndDecode();
        }
    }

    public void quitSynchronously() {
        mState = State.DONE;
        CameraManager.get().stopPreview();
        for (Handler decodeHandler : getHandlers()) {
            Message quit = Message.obtain(decodeHandler, R.id.quit);
            quit.sendToTarget();
        }
        try {
            mDecodeThread.join();
//            mOpenCvDecodeThread.join();
        } catch (InterruptedException e) {
            // continue
        }
        // Be absolutely sure we don't send any queued up messages
        removeMessages(R.id.decode_succeeded);
        removeMessages(R.id.decode_failed);
    }

    public void pauseDecode() {
        pauseDecode(0);
    }

    public void pauseDecode(long delay) {
        for (Handler decodeHandler : getHandlers()) {
            ((DecodeHandler) decodeHandler).setSpotEnable(false);
        }
        sendEmptyMessageDelayed(R.id.decode_restart, delay);
    }

    public void restartPreviewAndDecode() {
        for (Handler decodeHandler : getHandlers()) {
            ((DecodeHandler) decodeHandler).setSpotEnable(true);
        }
        if (mState != State.PREVIEW) {
            CameraManager.get().startPreview();
            mState = State.PREVIEW;
            CameraManager.get().requestPreviewFrame(getHandlers(), R.id.decode);
            CameraManager.get().requestAutoFocus(this, R.id.auto_focus);
        }
    }

    private ArrayList<Handler> getHandlers() {
        ArrayList<Handler> handlers = new ArrayList<>();
        handlers.add(mDecodeThread.getHandler());
//        handlers.add(mOpenCvDecodeThread.getHandler());
        return handlers;
    }

    private enum State {
        PREVIEW, SUCCESS, DONE
    }

}
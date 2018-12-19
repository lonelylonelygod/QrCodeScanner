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

import com.google.zxing.Result;
import com.zmsoft.component.codescanner.R;
import com.zmsoft.component.codescanner.camera.CameraManager;

/**
 * This class handles all the messaging which comprises the state machine for capture.
 */
public final class CaptureActivityHandler extends Handler {

    private final static int WHAT_RESTART_DECODE = 0;
    private final DecodeThread mDecodeThread;
    private DecodeHandlerDelegate mDecodeHandlerDelegate;
    private State mState;

    private enum State {
        PREVIEW, SUCCESS, DONE
    }

    public CaptureActivityHandler(DecodeHandlerDelegate decodeHandlerDelegate) {
        mDecodeHandlerDelegate = decodeHandlerDelegate;
        mDecodeHandlerDelegate.setCaptureActivityHandler(this);
        mDecodeThread = new DecodeThread(mDecodeHandlerDelegate);
        mDecodeThread.start();
        mState = State.SUCCESS;
        // Start ourselves capturing previews and decoding.
        restartPreviewAndDecode();
    }

    @Override
    public void handleMessage(Message message) {
        if (message.what == R.id.auto_focus) {// When one auto focus pass finishes, start another. This is the closest thing to
            // continuous AF. It does seem to hunt a bit, but I'm not sure what else to do.
            if (mState == State.PREVIEW) {
                CameraManager.get().requestAutoFocus(this, R.id.auto_focus);
            }

        } else if (message.what == R.id.decode_succeeded) {
            mState = State.SUCCESS;
            mDecodeHandlerDelegate.handleDecode((Result) message.obj);

        } else if (message.what == R.id.decode_failed) {// We're decoding as fast as possible, so when one decode fails, start another.
            mState = State.PREVIEW;
            CameraManager.get().requestPreviewFrame(mDecodeThread.getHandler(), R.id.decode);

        } else if (message.what == R.id.decode_restart){
            restartPreviewAndDecode();
        }
    }

    public void quitSynchronously() {
        mState = State.DONE;
        CameraManager.get().stopPreview();
        Message quit = Message.obtain(mDecodeThread.getHandler(), R.id.quit);
        quit.sendToTarget();
        try {
            mDecodeThread.join();
        } catch (InterruptedException e) {
            // continue
        }

        // Be absolutely sure we don't send any queued up messages
        removeMessages(R.id.decode_succeeded);
        removeMessages(R.id.decode_failed);
    }

    public void pauseDecode(){
        ((DecodeHandler)mDecodeThread.getHandler()).setSpotEnable(false);
    }

    public void pauseDecode(long delay){
        ((DecodeHandler)mDecodeThread.getHandler()).setSpotEnable(false);
        sendEmptyMessageDelayed(R.id.decode_restart, delay);
    }

    public void restartPreviewAndDecode() {
        ((DecodeHandler)mDecodeThread.getHandler()).setSpotEnable(true);
        startInternal();
    }

    private void startInternal() {
        if (mState != State.PREVIEW) {
            CameraManager.get().startPreview();
            mState = State.PREVIEW;
            CameraManager.get().requestPreviewFrame(mDecodeThread.getHandler(), R.id.decode);
            CameraManager.get().requestAutoFocus(this, R.id.auto_focus);
        }
    }
}
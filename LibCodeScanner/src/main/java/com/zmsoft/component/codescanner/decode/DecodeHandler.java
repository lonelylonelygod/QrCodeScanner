/*
 * Copyright (C) 2010 ZXing authors
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
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.oned.Code128Reader;
import com.google.zxing.oned.Code39Reader;
import com.google.zxing.oned.Code93Reader;
import com.google.zxing.oned.MultiFormatUPCEANReader;
import com.google.zxing.qrcode.QRCodeReader;
import com.zmsoft.component.codescanner.R;

import java.util.*;

final class DecodeHandler extends Handler {

    private final DecodeHandlerDelegate mDecodeHandlerDelegate;
    private final Map<DecodeHintType, Object> mHints;
    private final ArrayList<Reader> mReaders = new ArrayList<>(5);
    private byte[] mRotatedData;
    private boolean mSpotEnable = true;

    DecodeHandler(DecodeHandlerDelegate decodeHandlerDelegate) {
        mDecodeHandlerDelegate = decodeHandlerDelegate;

        ArrayList<BarcodeFormat> barcodeFormats = new ArrayList<>(4);
        barcodeFormats.add(BarcodeFormat.EAN_8);
        barcodeFormats.add(BarcodeFormat.EAN_13);
        barcodeFormats.add(BarcodeFormat.UPC_A);
        barcodeFormats.add(BarcodeFormat.UPC_E);

        mHints = new HashMap<>();
        mHints.put(DecodeHintType.CHARACTER_SET, "utf-8");
        mHints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        mHints.put(DecodeHintType.POSSIBLE_FORMATS, barcodeFormats);

        MultiFormatUPCEANReader multiFormatUPCEANReader = new MultiFormatUPCEANReader(mHints);
        QRCodeReader qrCodeReader = new QRCodeReader();


        mReaders.add(multiFormatUPCEANReader);
        mReaders.add(qrCodeReader);
        mReaders.add(new Code39Reader());
        mReaders.add(new Code93Reader());
        mReaders.add(new Code128Reader());
    }

    @Override
    public void handleMessage(Message message) {
        if (message.what == R.id.decode) {
            Log.d("DecodeHandler ", "mSpotEnable: " + mSpotEnable);
            if (mSpotEnable) {
                decode((byte[]) message.obj, message.arg1, message.arg2);
            } else {
                sendFailedMessage();
            }

        } else if (message.what == R.id.quit) {
            Looper looper = Looper.myLooper();
            if (null != looper) {
                looper.quit();
            }

        }
    }

    /**
     * Decode the data within the viewfinder rectangle, and time how long it took. For efficiency, reuse the same reader
     * objects from one decode to the next.
     *
     * @param data   The YUV preview frame.
     * @param width  The width of the preview frame.
     * @param height The height of the preview frame.
     */
    private void decode(byte[] data, int width, int height) {
        if (null == mRotatedData) {
            mRotatedData = new byte[width * height];
        } else {
            if (mRotatedData.length < width * height) {
                mRotatedData = new byte[width * height];
            }
        }
        Arrays.fill(mRotatedData, (byte) 0);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (x + y * width >= data.length) {
                    break;
                }
                mRotatedData[x * height + height - y - 1] = data[x + y * width];
            }
        }
        int tmp = width; // Here we are swapping, that's the difference to #11
        width = height;
        height = tmp;
        Result rawResult = null;
        try {
            PlanarYUVLuminanceSource source =
                    new PlanarYUVLuminanceSource(mRotatedData, width, height, 0, 0, width, height, false);
            BinaryBitmap image = new BinaryBitmap(new HybridBinarizer(source));
            rawResult = decodeInternal(image);
        } catch (ReaderException e) {
        } finally {
            reset();
        }

        if (rawResult != null) {
            sendSucceedMessage(rawResult);
        } else {
            sendFailedMessage();
        }
    }

    private void sendSucceedMessage(Result rawResult) {
        Message message = Message.obtain(mDecodeHandlerDelegate.getCaptureActivityHandler(), R.id.decode_succeeded, rawResult);
        message.sendToTarget();
    }

    private void sendFailedMessage(){
        Message message = Message.obtain(mDecodeHandlerDelegate.getCaptureActivityHandler(), R.id.decode_failed);
        message.sendToTarget();
    }

    private Result decodeInternal(BinaryBitmap image) throws NotFoundException {
        for (Reader reader : mReaders) {
            try {
                return reader.decode(image, mHints);
            } catch (ReaderException re) {
                // continue
            }
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private void reset() {
        for (Reader reader : mReaders) {
            reader.reset();
        }
    }

    void setSpotEnable(boolean spotEnable){
        mSpotEnable = spotEnable;
    }

    public boolean getSpotEnable(){
        return mSpotEnable;
    }
}

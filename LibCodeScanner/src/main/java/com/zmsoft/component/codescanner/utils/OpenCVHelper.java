package com.zmsoft.component.codescanner.utils;

/**
 * Project name : NDKdevolp
 * Created by libai
 * on 2017/3/17.
 */

public class OpenCVHelper {

    static {
        System.loadLibrary("opencv-helper");
    }

    public static native int[] process(byte[] data, int w, int h, int left, int top, int scanSize, boolean isClip);
}

package com.zmsoft.component.codescanner.decode;

/**
 * Created by xingli on 1/4/16.
 * 
 * 图片解析二维码回调方法
 */
public interface DecodeImageCallback {

    void decodeSucceed(String result);

    void decodeFail(int type, String reason);
}

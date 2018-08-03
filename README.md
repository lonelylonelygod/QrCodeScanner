# CodeScanner

## 简介
参考自[BGAQRCode-Android](https://github.com/bingoogolapple/BGAQRCode-Android)，去掉了zbar支持，在zxing部分优化了识别效率。

- [x] ZXing 生成可自定义颜色、带 logo 的二维码
- [x] ZXing 扫描二维码
- [x] ZXing 识别图库中的二维码图片
- [x] 可以设置用前置摄像头扫描
- [x] 可以控制闪光灯，方便夜间使用
- [x] 可以定制各式各样的扫描框

## Gradle 依赖
```groovy
maven {
    url "http://nexus.2dfire-dev.com/content/groups/public/"
}

dependencies {
    compile 'com.zmsoft.component.codescanner:codescanner:0.5.6'
}
```
## 布局文件

```xml
<com.zmsoft.component.codescanner.CodeScanView
    android:id="@+id/zxingview"
    style="@style/MatchMatch"
    app:qrcv_animTime="1000"
    app:qrcv_borderColor="@android:color/white"
    app:qrcv_borderSize="1dp"
    app:qrcv_cornerColor="@color/colorPrimaryDark"
    app:qrcv_cornerLength="20dp"
    app:qrcv_cornerSize="3dp"
    app:qrcv_maskColor="#33FFFFFF"
    app:qrcv_rectWidth="200dp"
    app:qrcv_scanLineColor="@color/colorPrimaryDark"
    app:qrcv_scanLineSize="1dp"
    app:qrcv_topOffset="90dp" />
```

## 自定义属性说明

属性名 | 说明 | 默认值
:----------- | :----------- | :-----------
qrcv_topOffset         | 扫描框距离 toolbar 底部的距离        | 90dp
qrcv_cornerSize         | 扫描框边角线的宽度        | 3dp
qrcv_cornerLength         | 扫描框边角线的长度        | 20dp
qrcv_cornerColor         | 扫描框边角线的颜色        | @android:color/white
qrcv_rectWidth         | 扫描框的宽度        | 200dp
qrcv_barcodeRectHeight         | 条码扫样式描框的高度        | 140dp
qrcv_maskColor         | 除去扫描框，其余部分阴影颜色        | #33FFFFFF
qrcv_scanLineSize         | 扫描线的宽度        | 1dp
qrcv_scanLineColor         | 扫描线的颜色「扫描线和默认的扫描线图片的颜色」        | @android:color/white
qrcv_scanLineMargin         | 扫描线距离上下或者左右边框的间距        | 0dp
qrcv_isShowDefaultScanLineDrawable         | 是否显示默认的图片扫描线「设置该属性后 qrcv_scanLineSize 将失效，可以通过 qrcv_scanLineColor 设置扫描线的颜色，避免让你公司的UI单独给你出特定颜色的扫描线图片」        | false
qrcv_customScanLineDrawable         | 扫描线的图片资源「默认的扫描线图片样式不能满足你的需求时使用，设置该属性后 qrcv_isShowDefaultScanLineDrawable、qrcv_scanLineSize、qrcv_scanLineColor 将失效」        | null
qrcv_borderSize         | 扫描边框的宽度        | 1dp
qrcv_borderColor         | 扫描边框的颜色        | @android:color/white
qrcv_animTime         | 扫描线从顶部移动到底部的动画时间「单位为毫秒」        | 1000
qrcv_isCenterVertical         | 扫描框是否垂直居中，该属性为true时会忽略 qrcv_topOffset 属性        | false
qrcv_toolbarHeight         | Toolbar 的高度，通过该属性来修正由 Toolbar 导致扫描框在垂直方向上的偏差        | 0dp
qrcv_isBarcode         | 是否是扫条形码        | false
qrcv_tipText         | 提示文案        | null
qrcv_tipTextSize         | 提示文案字体大小        | 14sp
qrcv_tipTextColor         | 提示文案颜色        | @android:color/white
qrcv_isTipTextBelowRect         | 提示文案是否在扫描框的底部        | false
qrcv_tipTextMargin         | 提示文案与扫描框之间的间距        | 20dp
qrcv_isShowTipTextAsSingleLine         | 是否把提示文案作为单行显示        | false
qrcv_isShowTipBackground         | 是否显示提示文案的背景        | false
qrcv_tipBackgroundColor         | 提示文案的背景色        | #22000000
qrcv_isScanLineReverse         | 扫描线是否来回移动        | true
qrcv_isShowDefaultGridScanLineDrawable         | 是否显示默认的网格图片扫描线        | false
qrcv_customGridScanLineDrawable         | 扫描线的网格图片资源        | null
qrcv_isOnlyDecodeScanBoxArea         | 是否只识别扫描框区域的二维码        | false

```java
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_spot:
                //重新开始识别
                mCodeScanView.restartScan();
                break;
            case R.id.stop_spot:
                //暂停识别
                mCodeScanView.pauseScan();
                break;
            case R.id.show_rect:
                //显示扫描框
                mCodeScanView.showScanRect();
                break;
            case R.id.hidden_rect:
                //隐藏扫描框
                mCodeScanView.hiddenScanRect();
                break;
            case R.id.open_flashlight:
                //打开闪光灯
                mCodeScanView.openFlashlight();
                break;
            case R.id.close_flashlight:
                //关闭闪光灯
                mCodeScanView.closeFlashlight();
                break;
            case R.id.scan_barcode:
                //变形成条形码样式
                mCodeScanView.changeToScanBarcodeStyle();
                break;
            case R.id.scan_qrcode:
                //变形成二维码样式
                mCodeScanView.changeToScanQRCodeStyle();
                break;
            case R.id.choose_qrcde_from_gallery:
                //打开相册
                onPickImage(v);
                break;
        }
    }
```

#### 详细用法请查看 CodeScannerSample


#### 引用参考
QrCodeScanner <https://github.com/lonelylonelygod/QrCodeScanner>

BGAQRCode-Android <https://github.com/bingoogolapple/BGAQRCode-Android>



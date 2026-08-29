package jp.yamamoto5989.qr;

import com.journeyapps.barcodescanner.CaptureActivity;

/**
 * ZXingの標準CaptureActivityは横向き固定で定義されているため、
 * 縦横どちらでも自動回転できるよう、画面の向きだけ上書きしたサブクラス。
 * (向きの指定はAndroidManifest.xml側の screenOrientation="fullSensor" で行う)
 */
public class FullSensorCaptureActivity extends CaptureActivity {
}

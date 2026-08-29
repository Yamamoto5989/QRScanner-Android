package jp.yamamoto5989.qr;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

/**
 * 起動直後にQRコードスキャン画面を開き、
 * 読み取った内容をシンプルに表示するだけのアプリ。
 */
public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 1001;

    private TextView resultTextView;
    private String lastScannedText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        resultTextView = findViewById(R.id.resultTextView);
        // URLが読み取られた際にタップでアクセスできるようにする
        resultTextView.setMovementMethod(LinkMovementMethod.getInstance());

        Button rescanButton = findViewById(R.id.rescanButton);
        rescanButton.setOnClickListener(v -> checkCameraPermissionAndScan());

        Button copyButton = findViewById(R.id.copyButton);
        copyButton.setOnClickListener(v -> copyResultToClipboard());

        // 起動時に自動でスキャンを開始する
        checkCameraPermissionAndScan();
    }

    /** カメラ権限を確認し、なければリクエスト、あればスキャン開始 */
    private void checkCameraPermissionAndScan() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startScan();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION
            );
        }
    }

    /** ZXingのスキャン画面を起動する */
    private void startScan() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("QRコードを枠内に合わせてください");
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(false);
        integrator.setOrientationLocked(false);
        integrator.setCaptureActivity(FullSensorCaptureActivity.class);
        integrator.initiateScan();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                            @NonNull String[] permissions,
                                            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScan();
            } else {
                Toast.makeText(this, "カメラの使用を許可してください", Toast.LENGTH_LONG).show();
                resultTextView.setText("カメラ権限が許可されていないため、スキャンできません。\n"
                        + "「再スキャン」ボタンからもう一度お試しください。");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                // スキャンがキャンセルされた場合
                resultTextView.setText("スキャンがキャンセルされました。\n「再スキャン」を押してください。");
            } else {
                showResult(result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /** スキャン結果を表示する。URLが含まれる場合はタップして開けるリンクにする */
    private void showResult(String content) {
        lastScannedText = content;
        resultTextView.setText(content);
        Linkify.addLinks(resultTextView, Linkify.WEB_URLS);
    }

    /** 読み取った文字列をクリップボードにコピーする */
    private void copyResultToClipboard() {
        if (lastScannedText.isEmpty()) {
            Toast.makeText(this, "コピーできる読み取り結果がありません", Toast.LENGTH_SHORT).show();
            return;
        }
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("QRスキャン結果", lastScannedText);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, R.string.copied_toast, Toast.LENGTH_SHORT).show();
        }
    }
}

package pe.edu.usat.ecorutapac.ui.atender;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.List;

import pe.edu.usat.ecorutapac.R;

public class ScannerQRActivity extends AppCompatActivity {

    private DecoratedBarcodeView barcodeScannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner_qractivity);

        barcodeScannerView = findViewById(R.id.barcodeScannerView);
        findViewById(R.id.btnCancelarScanner).setOnClickListener(v -> finish());

        barcodeScannerView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result.getText() != null) {
                    barcodeScannerView.pause();
                    Intent data = new Intent();
                    data.putExtra("codigo_qr", result.getText());
                    setResult(RESULT_OK, data);
                    finish();
                }
            }

            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        barcodeScannerView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeScannerView.pause();
    }
}

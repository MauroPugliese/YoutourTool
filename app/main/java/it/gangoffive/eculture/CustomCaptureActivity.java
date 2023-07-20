package it.gangoffive.eculture;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

public class CustomCaptureActivity extends AppCompatActivity {

    private boolean isFlashOn = true;
    private CaptureManager capture;
    private DecoratedBarcodeView barcodeScannerView;
    private  ImageButton btnFlash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        barcodeScannerView = initializeContent();

        capture = new CaptureManager(this, barcodeScannerView);
        barcodeScannerView.setTorchOn();
        capture.initializeFromIntent(getIntent(), savedInstanceState);
        capture.decode();
    }

    /**
     *  Override per usare un layout differente.
     *
     *  @return ritorna la DecoretedBarcodeView
     */
    protected DecoratedBarcodeView initializeContent() {
        setContentView(R.layout.activity_custom_capture);

        btnFlash = findViewById(R.id.btn_flash);
        if (!hasFlash()) {
            btnFlash.setVisibility(View.GONE);
        }
        btnFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFlashlight();
            }
        });


        return (DecoratedBarcodeView)findViewById(R.id.zxing_barcode_scanner);
    }

    /**
     * Controlla se il device ha una torcia.
     *
     * @return true se il dispositivo è dotato di flash, false in caso non sia.
     */
    private boolean hasFlash() {
        return getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }
    /**
     * Metodo responsabile dello switch del flash nella view di scanning.
     *
     *
     */
    public void switchFlashlight() {
        if (isFlashOn) {
            isFlashOn = false;
            btnFlash.setImageDrawable(getDrawable(R.drawable.ic_baseline_flash_off_24));
            barcodeScannerView.setTorchOff();
        } else {
            isFlashOn = true;
            btnFlash.setImageDrawable(getDrawable(R.drawable.ic_baseline_flash_on_24));
            barcodeScannerView.setTorchOn();
        }
    }




    @Override
    protected void onResume() {
        super.onResume();
        capture.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        capture.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        capture.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        capture.onSaveInstanceState(outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        capture.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeScannerView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }



}
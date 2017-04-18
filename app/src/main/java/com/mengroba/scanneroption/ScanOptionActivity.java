package com.mengroba.scanneroption;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.mengroba.scanneroption.utils.UtilsKeys;
import com.mengroba.scanneroption.utils.UtilsTools;
import com.mengroba.scanneroption.bluebird.BluebirdMode;
import com.mengroba.scanneroption.webview.ScanWebView;
import com.mengroba.scanneroption.javascript.WebAppInterface;

import me.sudar.zxingorient.ZxingOrient;
import me.sudar.zxingorient.ZxingOrientResult;

import static com.mengroba.scanneroption.bluebird.BluebirdMode.BARCODE_MODE;
import static com.mengroba.scanneroption.bluebird.BluebirdMode.RFID_MODE;

/**
 * Created by mengroba on 24/01/2017.
 */

public class ScanOptionActivity extends AppCompatActivity implements ScanOptionInterface {

    //DECLARACIONES
    private static final String TAG = "ScanOptionActivity";
    private static final String TAG2 = "LaserLog";
    private static final String WEB_LOCAL = "http://10.236.3.80:8080/wms-pme-hht/login.htm";
    private static final int BARCODE_RESULTCODE = 100;
    private static final String BLUEBIRD = "Bluebird";
    private static final String ZEBRA = "Zebra";
    //UI
    private UtilsTools utils;
    private InputMethodManager imm;
    private Button main_btn;
    private Button keysoft_btn;
    private String device;
    //Scanner
    private BluebirdMode bluebirdMode;
    private ScanWebView scanWebView;
    private String scanContentResult;
    //Elementos HTML
    public WebView webView;
    private WebAppInterface webInterface;
    private ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan_option_layout);

        webView = (WebView) findViewById(R.id.wbv_webView);
        webInterface = new WebAppInterface(this);
        utils = new UtilsTools(this);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        progressBar = (ProgressBar) findViewById(R.id.pgb_progressbar);
        main_btn = (Button) findViewById(R.id.btn_main);
        main_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG2, "main_btn.arg1: " + bluebirdMode.getArg1());
                Log.d(TAG2, "main_btn.arg2: " + bluebirdMode.getArg2());
                readerMode();
            }
        });
        keysoft_btn = (Button) findViewById(R.id.btn_keysoft);
        keysoft_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                utils.showKeyboard(ScanOptionActivity.this);
            }
        });

        //definimos el visor HTML
        scanWebView = new ScanWebView(this, webView);
        scanWebView.createWebView();
        // creamos el visor HTML
        scanWebView.startWebView();
    }

    /**
     * Metodo ejecutado con el resultado de la camara {@link Activity#startActivityForResult}
     *
     * @param requestCode
     * @param resultCode
     * @param intent
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent intent) {

        Log.d(TAG, "ResultScan.requesCode" + requestCode);

        if (requestCode == BARCODE_RESULTCODE) {
            //Cargamos la libreria Zxing a traves de scanResult y parseamos el resultado
            ZxingOrientResult scanResult = ZxingOrient.parseActivityResult(requestCode, resultCode, intent);
            scanContentResult = scanResult.getContents();
            Log.i(TAG, "onActivityResult" + scanContentResult);
            if (scanResult.getContents() != null) {
                Log.d(TAG, "onActivityResult(): no es nulo");
                // Cuando se escanea como el foco lo tiene el elemento se simulan keypresets
                UtilsKeys.clearKeys(webView);
                UtilsKeys.loadKeys(webView, scanContentResult);
            } else {
                Log.d(TAG, "onActivityResult()Scanner cancelado");
                //Opcion de borrado de valor al cancelar el escaneo (por defecto se mantiene el valor)
                // UtilsKeys.clearKeys(webView);
                //imm.showSoftInput(webView, 0);
                webInterface.makeToastAndroid("No se ha obtenido ningún dato");
                //Opcion de habilitar el sonido
                //beepTone(BEEP_ERROR);
                //webInterface.textSpeech("No se ha obtenido ningun dato");
            }
        } else {
            webInterface.showDialog(getString(R.string.no_value));
        }
    }

    @Override
    public void connect() {
        //BLUEBIRD
        bluebirdMode = new BluebirdMode(this);
        if(bluebirdMode.getReader() == null){
            bluebirdMode.init();
        }
        if (bluebirdMode.getReader().SD_GetConnectState() != 32) {
            device = BLUEBIRD;
        }
        //ZEBRA
        /*zebraMode = new ZebraMode(this);
        if(zebraMode.getReader() == null){
            zebraMode.connect();
        }
        else if(zebraMode.getReader().codigo_zebraConnected){
            device = ZEBRA;
        }*/
        else {
            Log.d(TAG, "Error connect()");
        }
    }

    @Override
    public void disconnect() {
        //BLUEBIRD
        if (bluebirdMode.getReader().SD_GetConnectState() == 32) {
            bluebirdMode.disconnect();
        }
        //ZEBRA
        /*else if(zebraMode.getReader().codigo_disconnect){
            zebraMode.disconnect();
        }*/
        else {
            Log.d(TAG, "Error Disconnect()");
        }
    }

    @Override
    public void readerMode() {
        //BLUEBIRD
        if (bluebirdMode.getReader() != null) {
            bluebirdMode.readerMode();
        }
        //ZEBRA
        /*else if(zebraMode.getReader() != null){
            zebraMode.readerMode();
        }*/
        else {
            Toast.makeText(this, R.string.error_init_scan, Toast.LENGTH_SHORT).show();
            main_btn.setText("RFR OFF");
            main_btn.setTextColor(Color.RED);
        }
    }

    @Override
    public void setBarcodeMode() {
        //BLUEBIRD
        if (device == BLUEBIRD) {
            bluebirdMode.getReader().SD_SetTriggerMode(BARCODE_MODE);
            setMainButton(getString(R.string.barcode), Color.GREEN);
        }
        //ZEBRA
        /*else if(device == ZEBRA){
            zebraMode.getReader().setTriggerMode(BARCODE);
            setMainButton("RFID", Color.GREEN);
        }*/
        else {
            Log.d(TAG, "Error setBarcodeMode()");
        }
    }

    @Override
    public void setRfidMode() {
        //BLUEBIRD
        if (device == BLUEBIRD) {
            bluebirdMode.getReader().SD_SetTriggerMode(RFID_MODE);
            setMainButton(getString(R.string.rfid), Color.GREEN);
        }
        //ZEBRA
        /*else if(device == ZEBRA){
            zebraMode.getReader().setTriggerMode(RFID);
            setMainButton("RFID", Color.GREEN);
        }*/
        else {
            Log.d(TAG, "Error setRfidMode()");
        }
    }

    @Override
    public void setModeScan(String elementScanClass) {
        //BLUEBIRD
        if (device == BLUEBIRD) {
            bluebirdMode.setModeScan(elementScanClass);
        }
        //ZEBRA
        /*else if(device == ZEBRA){
            zebraMode.setScanMode(elementScanClass);
        }*/
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        Log.d(TAG, " onStart");
        super.onStart();
        if(device == null){
            connect();
        }
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        Log.d(TAG, " onPause");
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //BLUEBIRD
        bluebirdMode.disconnect();
        //ZEBRA
        //zebraMode.disconnect();
        Log.d(TAG, "onPause.SD_Disconnect");

        super.onPause();
    }

    @Override
    protected void onStop() {
        utils.hideKeyboard(this);
        super.onStop();
        Log.d(TAG, " onStop");
        //BLUEBIRD
        bluebirdMode.disconnect();
        //ZEBRA
        //zebraMode.disconnect();
        Log.d(TAG2, "onStop.SD_Disconnect");
    }

    @Override
    public void onDestroy() {
        utils.hideKeyboard(this);
        // TODO Auto-generated method stub
        Log.d(TAG, " onDestroy");
        super.onDestroy();
    }

    @Override
    public void onResume() {
        utils.hideKeyboard(this);
        // TODO Auto-generated method stub
        Log.d(TAG, "onResume");
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if(device == null){
            connect();
        }
    }

    @Override
    public void onBackPressed() {

        //por defecto muestra la pagina de seleccion
        if (webView.canGoBack()) {
            //webView.goBack();
            webView.loadUrl(WEB_LOCAL);
        } else {
            // Si no lo hay, damos el control al Back de la Activity
            super.onBackPressed();
        }
    }


    public void setMainButton(String text, int color) {
        this.main_btn.setText(text);
        this.main_btn.setTextColor(color);
    }

    public String getTextMainButton() {
        return String.valueOf(this.main_btn.getText());
    }

    public WebView getWebView() {
        return webView;
    }

    public WebAppInterface getWebAppInterface() {
        return webInterface;
    }

    public ProgressBar getProgressBar(){
        return progressBar;
    }
}
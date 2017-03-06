package com.mengroba.scanneroption;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.ConsoleMessage;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.mengroba.scanneroption.utils.UtilsKeys;
import com.mengroba.scanneroption.utils.UtilsTools;

import co.kr.bluebird.ser.protocol.Reader;
import co.kr.bluebird.ser.protocol.SDConsts;
import me.sudar.zxingorient.ZxingOrient;
import me.sudar.zxingorient.ZxingOrientResult;

import static com.mengroba.scanneroption.WebAppInterface.JS_LOAD_PAGE;
import static com.mengroba.scanneroption.WebAppInterface.JS_JAVASCRIPT;
import static com.mengroba.scanneroption.WebAppInterface.JS_FUNCTION;
import static com.mengroba.scanneroption.WebAppInterface.JS_START_SCAN_IF_EMPTY;

/**
 * Created by mengroba on 24/01/2017.
 */

public class MainActivity extends AppCompatActivity {

    //DECLARACIONES
    private static final String TAG = "MainActivity";
    private static final String WEB_LOCAL =
            "file:///android_asset/main_menu.html";
    private static final int BARCODE_RESULTCODE = 100;

    private UtilsTools utils;
    private InputMethodManager imm;
    private Button bluebird_btn;
    //Scanner
    private String scanContentResult;
    private int errorScan;
    private Reader laserReader;
    private int arg1;
    private int arg2;
    private int res;
    //Elementos HTML
    public WebView webView;
    private WebAppInterface webInterface;
    private ProgressBar progressBar;
    private long eventDuration;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webInterface = new WebAppInterface(this);
        utils = new UtilsTools(this);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        //definimos el visor HTML
        createWebView(this);
        // creamos el visor HTML
        startWebView();

        bluebird_btn = (Button) findViewById(R.id.btn_bluebird);
        bluebird_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "bluebird_btn.arg1: " + arg1);
                Log.d(TAG, "bluebird_btn.arg2: " + arg2);
                if (laserReader != null && laserHandler != null) {
                    if (bluebird_btn.getText().equals("SLED ON")) {
                        bluebird_btn.setText("SLED OFF");
                        //bluebird_btn.setTextColor(Color.RED);
                        res = laserReader.SD_Disconnect();
                        Log.d(TAG, "bluebird_btn.SD_Disconnect: " + res);
                    } else {
                        bluebird_btn.setText("SLED ON");
                        //bluebird_btn.setTextColor(Color.GREEN);
                        res = laserReader.SD_Wakeup();
                        Log.d(TAG, "bluebird_btn.SD_Wakeup: " + res);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Error en inicialización de laser", Toast.LENGTH_SHORT).show();
                    /*mainLaserHandler = new LaserHandler(getApplicationContext(), webView);
                    mainLaserHandler.startHandler();*/
                }
            }
        });
    }

    /**
     * Creamos el visor WebView para cargar el HTML
     */
    private void createWebView(final Context context) {

        //Enlazamos los elementos graficos
        webView = (WebView) findViewById(R.id.webView1);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        WebSettings settings = webView.getSettings();
        //Enlazamos WebAppInterface entre el codigo JavaScript y el codigo Android
        webView.addJavascriptInterface(new WebAppInterface(this, webView), "Android");
        //Accedemos a la configuracion del WebView y habilitamos el javascript
        settings.setJavaScriptEnabled(true);
        webView.setWebContentsDebuggingEnabled(true);
        // Ajustamos el HTML al WebView
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        //añadimos scroll
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(false);
        //habilitamos el uso de mediaplayer sin gestos
        settings.setMediaPlaybackRequiresUserGesture(false);
        //habilitamos el tratamiento de ficheros
        settings.setAllowFileAccess(true);
        //habilitamos las opciones de zoom
        settings.setBuiltInZoomControls(false);
        settings.setSupportZoom(false);
        settings.setTextZoom(125);
        /*settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);*/

        webView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                //TODO si el elemento tiene el class de html scanner entonces abrimos el scanner.
                eventDuration = event.getEventTime() - event.getDownTime();
                if (eventDuration > 500) {
                    utils.showKeyboard(MainActivity.this);
                }

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    WebView.HitTestResult hr = ((WebView) view).getHitTestResult();

                    Log.d(TAG, "onTouch():findFocus" + view.findFocus());
                    Log.d(TAG, "HitTestResult:" + webView.getHitTestResult());
                    Log.d(TAG, "HitTestResult: getExtra = " + hr.getExtra() + "\t\t Type=" + hr.getType());
                    Log.d(TAG, "onTouch()eventDuration = " + eventDuration);

                    if (eventDuration > 500) {
                        utils.showKeyboard(MainActivity.this);
                    } else if (hr.getType() == 9 && eventDuration < 500) {
                        utils.hideKeyboard(MainActivity.this);
                        if (!bluebird_btn.getText().toString().contains("ON"))
                            webView.loadUrl(JS_JAVASCRIPT + JS_FUNCTION + JS_START_SCAN_IF_EMPTY);
                    } else if (hr.getType() == 0) {
                        utils.toggleKey(MainActivity.this);
                    } else {

                    }
                }
                return false;
            }
        });

        webView.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Log.d(TAG, "onFocusChange(): " + v.getOnFocusChangeListener());

                //el foco siempre se queda en el webview
                if (hasFocus) {
                    Toast.makeText(getApplicationContext(), "Has Focus", Toast.LENGTH_SHORT).show();
                }
            }
        });

        webView.loadUrl(WEB_LOCAL);

    } //Fin de createWebView()


    /**
     * Iniciamos un cliente de WebView que es llamado al abrir el HTML
     */
    private void startWebView() {


        // Hacemos que todas las paginas se carguen en el mismo WebView
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView webView, String url) {
                super.onPageFinished(webView, url);
                webView.loadUrl(JS_JAVASCRIPT + JS_FUNCTION + JS_LOAD_PAGE);
                // Estado del teclado segun la pagina en la que estemos
                imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                Log.d(TAG, "onPageFinished(): " + webView.getTitle());

                if (webView.getTitle().equals("Opciones")) {
                    imm.showSoftInput(webView, 0);
                } else if (webView.getTitle().equals("Información de bloque") && errorScan != 1) {
                    utils.hideKeyboard(MainActivity.this);
                } else {
                    //utils.hideKeyboard(MainActivity.this);
                }
            }
        });

        /**
         * Definimos una clase interna WebChrome Client y un metodo openFileChooser para
         * seleccionar un archivo desde la aplicacion de camara o del almacenamiento.
         */
        webView.setWebChromeClient(new WebChromeClient() {


            @Override
            public void onPermissionRequest(PermissionRequest request) {
                Log.i(TAG, "onPermissionRequest");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    request.grant(request.getResources());
                }
            }

            /**
             * Mostrar la carga de la pagina web
             * @param view
             * @param progress
             */
            @Override
            public void onProgressChanged(WebView view, int progress) {
                progressBar.setProgress(0);
                progressBar.setVisibility(View.VISIBLE);
                MainActivity.this.setProgress(progress * 1000);

                progressBar.incrementProgressBy(progress);

                if (progress == 100) {
                    progressBar.setVisibility(View.GONE);
                }
            }


            /**
             * Enviamos un mensaje por Javascript en caso de error en el WebChromeClient
             * @param cm
             * @return
             */
            @Override
            public boolean onConsoleMessage(ConsoleMessage cm) {
                onConsoleMessage(cm.message(), cm.lineNumber(), cm.sourceId());
                return true;
            }

            public void onConsoleMessage(String message, int lineNumber, String sourceID) {
                Log.d(TAG, "Mostrando mensaje de consola: " + message);
            }

            @Override
            public void onCloseWindow(WebView window) {
                super.onCloseWindow(window);
                finish();
                Log.d(TAG, "Cerrando ventana" + window);
            }
        });   // Fin de setWebChromeClient()

    } //Fin de startWebView()

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
            //scanFormatResult = scanResult.getFormatName();
            scanContentResult = scanResult.getContents();
            //Log.i(TAG, "onActivityResult" + scanFormatResult);
            Log.i(TAG, "onActivityResult" + scanContentResult);
            if (scanResult.getContents() != null) {
                errorScan = 0;
                Log.d(TAG, "onActivityResult(): no es nulo");
                // Cuando se escanea como el foco lo tiene el elemento se simulan keypresets
                UtilsKeys.clearKeys(webView);
                UtilsKeys.loadKeys(webView, scanContentResult);
            } else {
                errorScan = 1;
                Log.d(TAG, "onActivityResult()Scanner cancelado");
                //Opcion de borrado de valor al cancelar el escaneo (por defecto se mantiene el valor)
                // UtilsKeys.clearKeys(webView);
                imm.showSoftInput(webView, 0);
                webInterface.makeToastAndroid("No se ha obtenido ningún dato");
                //Opcion de habilitar el sonido
                //beepTone(BEEP_ERROR);
                //webInterface.textSpeech("No se ha obtenido ningun dato");
            }
        } else {
            webInterface.showDialog("No se detectó ningún valor");
        }
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        Log.d(TAG, " onStart");
        super.onStart();
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        Log.d(TAG, " onPause");
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        laserReader.SD_Disconnect();
        Log.d(TAG, "onPause.SD_Disconnect: " + laserReader.SD_Disconnect());

        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, " onStop");
        laserReader.SD_Disconnect();
        Log.d(TAG, "onStop.SD_Disconnect: " + laserReader.SD_Disconnect());

        super.onStop();
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        Log.d(TAG, " onDestroy");
        super.onDestroy();
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        Log.d(TAG, "onResume");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        laserReader = Reader.getReader(this, laserHandler);
        boolean openResult = false;
        openResult = laserReader.RF_Open();
        if (openResult == SDConsts.RF_OPEN_SUCCESS) {
            Log.i(TAG, "Reader opened");
            int ret = laserReader.SD_Wakeup();
            Log.d(TAG, "WakeUp: " + ret);
        } else if (openResult == SDConsts.RF_OPEN_FAIL)
            Log.e(TAG, "Reader open failed");

        super.onResume();
    }

    @Override
    public void onBackPressed() {

        //Comprobamos si hay historial de navegacion
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            // Si no lo hay, damos el control al Back de la Activity
            super.onBackPressed();
        }
    }

    /**
     * Handler para disparar los eventos del broadcast del laser
     */
    public Handler laserHandler = new Handler() {
        public void handleMessage(Message m) {
            Log.d(TAG, "laserHandler");
            Log.d(TAG, "arg1 = " + m.arg1 + ", arg2 = " + m.arg2 + ", what = " + m.what);
            Log.d(TAG, "SDConnect(): " + laserReader.SD_Connect());
            arg1 = m.arg1;
            arg2 = m.arg2;

            switch (m.arg1) {
                //SLED Mensajes
                case SDConsts.SDCmdMsg.SLED_WAKEUP:
                    if (m.arg2 == SDConsts.SDResult.SUCCESS) {
                        Log.d(TAG, "SLED conectado");
                        res = laserReader.SD_Connect();
                        bluebird_btn.setText("SLED ON");
                        if (laserReader.SD_Connect() == SDConsts.SDResult.ACCESS_TIMEOUT) {
                            bluebird_btn.setText("RFR OFF");
                            laserReader.SD_Disconnect();
                        }
                        Log.d(TAG, "SD_Connect: " + res);
                    } else {
                        Log.d(TAG, "Fallo en SLED");
                        bluebird_btn.setText("ALREADY ON");
                    }
                    break;
                case SDConsts.BCCmdMsg.BARCODE_TRIGGER_PRESSED:
                    if (laserReader.SD_Connect() == -32) {
                        bluebird_btn.setText("RFR OFF");
                        laserReader.SD_Disconnect();
                    }else if (laserReader.SD_Connect() == -10 && m.arg2 == 0) {
                        bluebird_btn.setText("SLED OFF");
                        laserReader.SD_Disconnect();
                    }
                    //bluebird_btn.setText("LASER ON");
                    Log.d(TAG, "lecturaLaser(): Laser on");
                    break;
                case SDConsts.BCCmdMsg.BARCODE_TRIGGER_RELEASED:
                    if (laserReader.SD_Connect() != -32) {
                        bluebird_btn.setText("SLED ON");
                    } else {
                        bluebird_btn.setText("SLED OFF");
                    }
                    break;
                case SDConsts.BCCmdMsg.BARCODE_READ:
                    if (m.arg2 == SDConsts.BCResult.SUCCESS) {
                        bluebird_btn.setText("SUCCES");
                        Log.d(TAG, "lecturaLaser(): Laser leyendo");
                        //Toast.makeText(context, "LASER LEYENDO CODIGO", Toast.LENGTH_SHORT).show();
                    } else if (m.arg2 == SDConsts.BCResult.ACCESS_TIMEOUT) {
                        bluebird_btn.setText("TIMEOUT");
                        Log.d(TAG, "lecturaLaser(): Laser expirado");
                        //Toast.makeText(context, "TIEMPO DE ESPERA EXPIRADO", Toast.LENGTH_SHORT).show();
                    }
                    if (m.obj != null) {
                        StringBuilder readData = new StringBuilder();
                        bluebird_btn.setText("CODE OK");
                        Log.d(TAG, "lecturaLaser(): valor no nulo");
                        String resultFull = readData.append((String) m.obj).toString();
                        String code = resultFull.substring(0, resultFull.indexOf(";"));
                        UtilsKeys.clearKeys(webView);
                        UtilsKeys.loadKeys(webView, code);
                        Log.d(TAG, "lecturaLaser(): Resultado: " + code);
                        //Pasamos la informacion al usuario, para ello usamos un dialogo emergente
                        //webInterface.showDialog("Codigo capturado: " + code);
                    } else {
                        Log.d(TAG, "lecturaLaser() no hay codigo laser");
                        webInterface.showDialog("No se ha detectado ningún valor");
                    }
                    break;
                case SDConsts.SDCmdMsg.SLED_MODE_CHANGED:
                    if (m.arg2 == 0)
                        bluebird_btn.setText("RFID ON");
                    else if (m.arg2 == 1)
                        bluebird_btn.setText("BARCODE ON");
                    break;
                case SDConsts.SDCmdMsg.TRIGGER_PRESSED:
                    Log.d(TAG, "RFID capturando");
                    bluebird_btn.setText("RFID TRIGGER ON");
                    break;
                case SDConsts.SDCmdMsg.TRIGGER_RELEASED:
                    Log.d(TAG, "RFID capturando");
                    bluebird_btn.setText("SLED ON");
                    break;
                case SDConsts.SDCmdMsg.SLED_UNKNOWN_DISCONNECTED:
                    Log.d(TAG, "SLED desconectado");
                    bluebird_btn.setText("SLED OFF");
                    break;
                case SDConsts.SDCmdMsg.SLED_BATTERY_STATE_CHANGED:
                    bluebird_btn.setText("LASER ON");
                    break;
                case SDConsts.SDBatteryState.LOW_BATTERY:
                    bluebird_btn.setText("LOW BATTERY");
                    break;
                default:
                    bluebird_btn.setText("BLUEBIRD ON");
                    break;
            }
        }
    };


}
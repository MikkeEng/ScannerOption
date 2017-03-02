package com.mengroba.scanneroption;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
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

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String WEB_LOCAL =
            "file:///android_asset/main_menu.html";

    private UtilsTools utils;

    public WebView webView;
    private ProgressBar progressBar;
    private InputMethodManager imm;
    //Scanner
    private ZxingOrient scanner;
    private String scanContentResult;
    //Elementos HTML
    private WebAppInterface webInterface;

    private static final int BARCODE_RESULTCODE = 100;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    private long eventDuration;
    private int errorScan;
    private Reader laserReader;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //fijamos el layout a utilizar
        setContentView(R.layout.activity_main);
        utils = new UtilsTools(this);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        //definimos el visor HTML
        createWebView(this);
        // creamos el visor HTML
        startWebView();
        webInterface = new WebAppInterface(this);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
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
                        webView.loadUrl(JS_JAVASCRIPT + JS_FUNCTION + JS_START_SCAN_IF_EMPTY);                    } else if (hr.getType() == 0) {
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
     * Metodo ejecutado con el resultado del {@link Activity#startActivityForResult}, segun su utilizacion
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
                //Toast.makeText(this, "No se ha obtenido ningun dato", Toast.LENGTH_SHORT).show();
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

        laserReader = Reader.getReader(this, laserHandler);
        if (laserReader.SD_GetChargeState() == SDConsts.SDConnectState.CONNECTED) {
            laserReader.SD_Disconnect();
        }
        laserReader.RF_Close();
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, " onStop");

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
            laserReader = Reader.getReader(this, laserHandler);
            laserReader.SD_Wakeup();
        } else if (openResult == SDConsts.RF_OPEN_FAIL)
            Log.e(TAG, "Reader open failed");

        super.onResume();
    }

    public Handler laserHandler = new Handler() {
        public void handleMessage(Message m) {
            Log.d(TAG, "laserHandler");
            Log.d(TAG, "command = " + m.arg1 + " result = " + m.arg2 + " obj = data");

            switch (m.what) {
                case SDConsts.Msg.SDMsg:
                    if (m.arg1 == SDConsts.SDCmdMsg.SLED_WAKEUP) {
                        if (m.arg2 == SDConsts.SDResult.SUCCESS) {
                            Log.d(TAG, "SLED conectado");
                            laserReader.SD_Connect();
                        } else
                            Log.d(TAG, "Fallo en SLED");
                        laserReader.SD_Disconnect();
                    } else if (m.arg1 == SDConsts.SDCmdMsg.SLED_UNKNOWN_DISCONNECTED) {
                        Log.d(TAG, "SLED desconectado");
                        laserReader.SD_Disconnect();
                    }
                    break;
                case SDConsts.Msg.BCMsg:
                    StringBuilder readData = new StringBuilder();
                    if (m.arg1 == SDConsts.BCCmdMsg.BARCODE_TRIGGER_PRESSED)
                        Log.d(TAG, "startLaserScan(): Laser activado");
                        //Toast.makeText(context, "LASER ACTIVADO", Toast.LENGTH_SHORT).show();
                    else if (m.arg1 == SDConsts.BCCmdMsg.BARCODE_TRIGGER_RELEASED)
                        Log.d(TAG, "startLaserScan(): Laser desactivado");
                        //Toast.makeText(context, "LASER DESACTIVADO", Toast.LENGTH_SHORT).show();
                    else if (m.arg1 == SDConsts.BCCmdMsg.BARCODE_READ) {
                        if (m.arg2 == SDConsts.BCResult.SUCCESS)
                            Log.d(TAG, "startLaserScan(): Laser leyendo");
                            //Toast.makeText(context, "LASER LEYENDO CODIGO", Toast.LENGTH_SHORT).show();
                        else if (m.arg2 == SDConsts.BCResult.ACCESS_TIMEOUT)
                            Log.d(TAG, "startLaserScan(): Laser expirado");
                        //Toast.makeText(context, "TIEMPO DE ESPERA EXPIRADO", Toast.LENGTH_SHORT).show();
                        if (m.obj != null) {
                            String resultFull = readData.append((String) m.obj).toString();
                            String code = resultFull.substring(0, resultFull.indexOf(";"));
                            Log.d(TAG, "startLaserScan(): Resultado: " + code);
                            Log.i(TAG, "onActivityResult: " + code);
                            if (code != null) {
                                Log.d(TAG, "startLaserScan(): valor no nulo");
                                UtilsKeys.clearKeys(webView);
                                UtilsKeys.loadKeys(webView, code);
                            } else {
                                Log.d(TAG, "laserCodeKey() no hay codigo laser");
                                webInterface.showDialog("No se ha detectado ningún valor");
                            }
                            //Pasamos la informacion al usuario, para ello usamos un dialogo emergente
                            //webInterface.showDialog("Codigo capturado: " + code);
                        }
                    }
                    break;
            }
        }
    };

    /**
     * Sobreescribimos el metodo de boton vuelta atras para que vaya a la anterior pagina visitada
     */
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
}
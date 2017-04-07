package com.mengroba.scanneroption;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.mengroba.scanneroption.utils.UtilsKeys;
import com.mengroba.scanneroption.utils.UtilsTools;
import com.mengroba.scanneroption.utils.epc.Epc;
import com.mengroba.scanneroption.utils.epc.GarmentEpc;

import co.kr.bluebird.ser.protocol.Reader;
import co.kr.bluebird.ser.protocol.SDConsts;
import me.sudar.zxingorient.ZxingOrient;
import me.sudar.zxingorient.ZxingOrientResult;

import static com.mengroba.scanneroption.utils.JSConstants.JS_JAVASCRIPT;
import static com.mengroba.scanneroption.utils.JSConstants.JS_FUNCTION;
import static com.mengroba.scanneroption.utils.JSConstants.JS_JAVASCRIPT_LISTENER;
import static com.mengroba.scanneroption.utils.JSConstants.JS_LOAD_PAGE;
import static com.mengroba.scanneroption.utils.JSConstants.JS_SCAN_MODE;
import static com.mengroba.scanneroption.utils.JSConstants.JS_START_CAMSCAN_IF_EMPTY;
import static com.mengroba.scanneroption.utils.JSConstants.JS_TEXT_SPEECH;

/**
 * Created by mengroba on 24/01/2017.
 */

public class MainActivity extends AppCompatActivity {

    //DECLARACIONES
    private static final String TAG = "MainActivity";
    private static final String TAG2 = "LaserLog";
    private static final String WEB_TEST = "file:///android_asset/main_menu.html";
    private static final String WEB_LOCAL = "http://localhost:8080/wms-pme-hht/login.htm";
    private static final String WEB_SELECTION = "http://localhost:8080/wms-pme-hht/userActions.htm";
    private static final int BARCODE_RESULTCODE = 100;
    private static final int BEEP_OK = 1;
    private static final int BEEP_ERROR = 2;
    private static final String SCAN_MODE_EPC = "scanEpc";
    private static final String SCAN_MODE_GARMENT = "scanGarmentRfid";
    private static final String SCAN_MODE_BARCODE = "scanBarcode";
    private static final String SCAN_MODE_MANUAL = "manual";
    public static final int RFR_OFF = 0;
    public static final int RFR_ON = 1;
    public static final int RFID_MODE = 0;
    public static final int BARCODE_MODE = 1;
    //UI
    private UtilsTools utils;
    private InputMethodManager imm;
    private Boolean keyIsActive = false;
    private Button bluebird_btn;
    private Button keysoft_btn;
    //Scanner
    private int rfrActive;
    private String elementScanClass;
    private String scanContentResult;
    private int errorScan;
    private Reader laserReader;
    private int arg1;
    private int arg2;
    private int res;
    private Epc epc;
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
                Log.d(TAG2, "bluebird_btn.arg1: " + arg1);
                Log.d(TAG2, "bluebird_btn.arg2: " + arg2);
                if (laserReader != null) {
                    if (bluebird_btn.getText().equals("SLED ON") &&
                            laserReader.SD_GetConnectState() == -32) {
                        bluebird_btn.setText("RFR OFF");
                        Log.d(TAG2, "Button -32: " + laserReader.SD_GetConnectState());
                        bluebird_btn.setTextColor(Color.RED);
                        laserReader.SD_Disconnect();
                        Log.d(TAG2, "bluebird_btn.SD_Disconnect");
                    } else if (bluebird_btn.getText().equals("SLED ON") ||
                            bluebird_btn.getText().equals("RFR ON") &&
                                    laserReader.SD_GetConnectState() == 1) {
                        bluebird_btn.setText("SLED OFF");
                        bluebird_btn.setTextColor(Color.RED);
                        laserReader.SD_Disconnect();
                        Log.d(TAG2, "bluebird_btn.SD_Disconnect");
                    } else {
                        bluebird_btn.setText("SLED ON");
                        bluebird_btn.setTextColor(Color.GREEN);
                        res = laserReader.SD_Wakeup();
                        Log.d(TAG2, "bluebird_btn.SD_Wakeup: " + res);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Error en inicialización de laser", Toast.LENGTH_SHORT).show();
                    bluebird_btn.setText("RFR OFF");
                    bluebird_btn.setTextColor(Color.RED);
                }
            }
        });
        keysoft_btn = (Button) findViewById(R.id.btn_keysoft);
        keysoft_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                    utils.showKeyboard(MainActivity.this);
            }
        });
    }


    /**
     * Creamos el visor WebView para cargar el HTML
     */
    private void createWebView(final Context context) {

        //Configuramos el webview
        webView = (WebView) findViewById(R.id.webView1);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        WebSettings settings = webView.getSettings();
        //Enlazamos WebAppInterface entre el codigo JavaScript y el codigo Android
        webView.addJavascriptInterface(new WebAppInterface(this, webView), "Android");
        //Accedemos a la configuracion del WebView y habilitamos el javascript
        settings.setJavaScriptEnabled(true);
        webView.setWebContentsDebuggingEnabled(true);
        // Ajustamos el l WebView
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(false);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setAllowFileAccess(true);
        settings.setBuiltInZoomControls(false);
        settings.setSupportZoom(false);
        settings.setTextZoom(125);

        webView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                //Nos valemos del metodo HitTestResult de la clase WebView para reconocer el foco
                // y si el elemento tiene la clase scanner, activamos el scanner por camara.
                eventDuration = event.getEventTime() - event.getDownTime();

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    WebView.HitTestResult hr = ((WebView) view).getHitTestResult();

                    Log.d(TAG, "onTouch():findFocus: " + view.findFocus());
                    Log.d(TAG, "onTouch().HitTestResult: " + webView.getHitTestResult());
                    Log.d(TAG, "onTouch().HitTestResult.getClass: " + hr.getClass());
                    Log.d(TAG, "onTouch().HitTestResult.hashCode: " + hr.hashCode());
                    Log.d(TAG, "onTouch().HitTestResult:getExtra: " + hr.getExtra() + "\t\t Type=" + hr.getType());
                    Log.d(TAG, "onTouch()eventDuration: " + eventDuration);

                    if (hr.getType() == 9 && eventDuration < 500) {
                        webView.loadUrl(JS_JAVASCRIPT + JS_FUNCTION + JS_SCAN_MODE);
                        webView.loadUrl(JS_JAVASCRIPT + JS_JAVASCRIPT_LISTENER);
                        if (bluebird_btn.getText().toString().contains("OFF")) {
                            webView.loadUrl(JS_JAVASCRIPT + JS_FUNCTION + JS_START_CAMSCAN_IF_EMPTY);
                        }
                    } else if (eventDuration > 500) {
                        //Reecargamos la pagina
                        webView.loadUrl(WEB_TEST);
                    }
                }
                return false;
            }
        });
        webView.loadUrl(WEB_TEST);

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
                //comprobamos las clases de los elementos HTML
                webView.loadUrl(JS_JAVASCRIPT + JS_FUNCTION + JS_LOAD_PAGE);
                webView.loadUrl(JS_JAVASCRIPT + JS_FUNCTION + JS_TEXT_SPEECH);
                webView.loadUrl(JS_JAVASCRIPT + JS_FUNCTION + JS_SCAN_MODE);
                Log.d(TAG, "onPageFinished(): " + webView.getTitle());
            }

            @Override
            public void onPageStarted(WebView webView, String url, Bitmap favicon) {
                super.onPageStarted(webView, url, favicon);
                //webView.loadUrl(JS_JAVASCRIPT + JS_FUNCTION + JS_SCAN_MODE);
                Log.d(TAG, "onPageStarted(): " + webView.getTitle());
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                Toast.makeText(MainActivity.this, "Hay un problema con la red", Toast.LENGTH_LONG).show();
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
                Log.d("MensajeJS: ", "Tipo: " + cm.messageLevel() + " -- En linea "
                        + cm.lineNumber() + " de "
                        + cm.message());
                return true;
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
                //imm.showSoftInput(webView, 0);
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
        Log.d(TAG2, "onStop.SD_Disconnect: " + laserReader.SD_Disconnect());

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
            Log.i(TAG2, "Reader opened");
            int ret = laserReader.SD_Wakeup();
            Log.d(TAG2, "WakeUp: " + ret);
        } else if (openResult == SDConsts.RF_OPEN_FAIL)
            Log.e(TAG2, "Reader open failed");

        super.onResume();
    }

    @Override
    public void onBackPressed() {

        //por defecto muestra la pagina de seleccion
        if (webView.canGoBack()) {
            //webView.goBack();
            webView.loadUrl(WEB_SELECTION);
        } else {
            // Si no lo hay, damos el control al Back de la Activity
            super.onBackPressed();
        }
    }

    public void setModeScan(final String elementScanClass) {
        if (elementScanClass != null) {
            this.elementScanClass = elementScanClass;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!bluebird_btn.getText().toString().contains("OFF")) {

                        if(elementScanClass.contains(SCAN_MODE_EPC)){
                            utils.delayKeyboard(MainActivity.this);
                            //laserReader.SD_SetTriggerMode(RFID_MODE);
                            bluebird_btn.setText("RFID_EPC");
                            bluebird_btn.setTextColor(Color.GREEN);
                            /*UtilsKeys.clearKeys(webView);
                            UtilsKeys.loadKeys(webView, "CEXP1064");*/
                        } else if(elementScanClass.contains(SCAN_MODE_GARMENT)){
                            utils.delayKeyboard(MainActivity.this);
                            //laserReader.SD_SetTriggerMode(RFID_MODE);
                            bluebird_btn.setText("RFID_GAR");
                            bluebird_btn.setTextColor(Color.GREEN);
                            /*UtilsKeys.clearKeys(webView);
                            UtilsKeys.loadKeys(webView, "30100010000641012064");*/
                        } else if(elementScanClass.contains(SCAN_MODE_MANUAL)){
                            utils.showKeyboard(MainActivity.this);
                            bluebird_btn.setText("MANUAL");
                            bluebird_btn.setTextColor(Color.GREEN);
                        } else if(elementScanClass.contains(SCAN_MODE_BARCODE)){
                            utils.delayKeyboard(MainActivity.this);
                            //laserReader.SD_SetTriggerMode(BARCODE_MODE);
                            bluebird_btn.setText("BARCODE");
                            bluebird_btn.setTextColor(Color.GREEN);
                        } else {
                            utils.delayKeyboard(MainActivity.this);
                            //laserReader.SD_SetTriggerMode(BARCODE_MODE);
                            bluebird_btn.setText("--");
                            bluebird_btn.setTextColor(Color.GREEN);
                        }
                    }
                }
            });
        }
    }

    /**
     * Handler para disparar los eventos del broadcast de la pistola
     */
    public Handler laserHandler = new Handler() {
        public void handleMessage(Message m) {
            Log.d(TAG2, "laserHandler");
            Log.d(TAG2, "arg1 = " + m.arg1 + ", arg2 = " + m.arg2 + ", what = " + m.what);
            Log.d(TAG2, "SDConnectState(): " + laserReader.SD_GetConnectState());
            arg1 = m.arg1;
            arg2 = m.arg2;
            //Se comprueba si esta conectado el RFR (pistola)
            if (laserReader.SD_GetConnectState() != -32) {
                bluebird_btn.setText("RFR ON");
                bluebird_btn.setTextColor(Color.GREEN);
            }

            switch (m.arg1) {
                //SLED Mensajes
                case SDConsts.SDCmdMsg.SLED_WAKEUP:
                    if (laserReader.SD_GetConnectState() != -32) {
                        laserReader.SD_Connect();
                        Log.d(TAG2, "SLED conectado");
                        bluebird_btn.setText("SLED ON");
                        bluebird_btn.setTextColor(Color.GREEN);
                        break;
                    } else {
                        Log.d(TAG2, "SLED desconectado");
                        bluebird_btn.setText("RFR OFF");
                        Log.d(TAG2, "WakeUp -32: " + laserReader.SD_GetConnectState());
                        bluebird_btn.setTextColor(Color.RED);
                        laserReader.SD_Disconnect();
                        break;
                    }
                case SDConsts.SDCmdMsg.SLED_MODE_CHANGED:
                    if (m.arg2 == 0) {
                        bluebird_btn.setText("RFID ON");
                        bluebird_btn.setTextColor(Color.GREEN);
                    } else if (m.arg2 == 1) {
                        bluebird_btn.setText("BARCODE ON");
                        bluebird_btn.setTextColor(Color.GREEN);
                    }
                    break;
                case SDConsts.BCCmdMsg.BARCODE_TRIGGER_PRESSED:
                    if (laserReader.SD_GetConnectState() == -32) {
                        bluebird_btn.setText("RFR OFF");
                        Log.d(TAG2, "triggerPress -32: " + laserReader.SD_GetConnectState());
                        bluebird_btn.setTextColor(Color.RED);
                    } else if (laserReader.SD_GetConnectState() == 0) {
                        bluebird_btn.setText("SLED OFF");
                        bluebird_btn.setTextColor(Color.RED);
                    } else {
                        bluebird_btn.setText("LASER ACTIVE");
                        bluebird_btn.setTextColor(Color.GREEN);
                        Log.d(TAG2, "lecturaLaser(): Laser active");
                    }
                    break;
                case SDConsts.BCCmdMsg.BARCODE_TRIGGER_RELEASED:
                    switch (laserReader.SD_GetConnectState()) {
                        case 0:
                            bluebird_btn.setText("RFR ON");
                            bluebird_btn.setTextColor(Color.GREEN);
                            laserReader.SD_Connect();
                            break;
                        case 1:
                            bluebird_btn.setText("SLED ON");
                            bluebird_btn.setTextColor(Color.GREEN);
                            break;
                        case -32:
                            Log.d(TAG2, "triggerRelease -32: " + laserReader.SD_GetConnectState());
                            bluebird_btn.setText("SLED OFF");
                            bluebird_btn.setTextColor(Color.GREEN);
                            break;
                        default:
                            break;
                    }
                    break;
                case SDConsts.BCCmdMsg.BARCODE_READ:
                    if (m.arg2 == SDConsts.BCResult.SUCCESS) {
                        bluebird_btn.setText("SUCCES");
                        bluebird_btn.setTextColor(Color.GREEN);
                        Log.d(TAG2, "lecturaLaser(): Laser leyendo");
                        if (m.obj != null) {
                            StringBuilder readData = new StringBuilder();
                            bluebird_btn.setText("CODE OK");
                            bluebird_btn.setTextColor(Color.GREEN);
                            Log.d(TAG2, "lecturaLaser(): valor no nulo");
                            String resultFull = readData.append((String) m.obj).toString();
                            String code = resultFull.substring(0, resultFull.indexOf(";"));
                            UtilsKeys.clearKeys(webView);
                            UtilsKeys.loadKeys(webView, code);
                            Log.d(TAG2, "lecturaLaser(): Resultado: " + code);
                        } else {
                            Log.d(TAG2, "lecturaLaser() no hay codigo laser");
                            webInterface.showDialog("No se ha detectado ningún valor");
                        }
                    } else if (m.arg2 == SDConsts.BCResult.ACCESS_TIMEOUT) {
                        bluebird_btn.setText("TIMEOUT");
                        bluebird_btn.setTextColor(Color.RED);
                        Log.d(TAG2, "lecturaLaser(): Laser expirado");
                    } else {
                        utils.beepTone(BEEP_ERROR);
                    }
                    break;
                case SDConsts.SDCmdMsg.TRIGGER_PRESSED:
                    laserReader.RF_SetRadioPowerState(10);
                    Log.d(TAG2, "RFID capturando");
                    bluebird_btn.setText("RFID TRIGGER ON");
                    bluebird_btn.setTextColor(Color.GREEN);
                    if (laserReader.SD_GetTriggerMode() == 0) {
                        //checkState();
                        laserReader.RF_READ(SDConsts.RFMemType.EPC, 2, 8, "00000000", false);
                    }
                    break;
                case SDConsts.SDCmdMsg.TRIGGER_RELEASED:
                    Log.d(TAG2, "RFID capturando");
                    bluebird_btn.setText("SLED ON");
                    bluebird_btn.setTextColor(Color.GREEN);
                    break;
                case SDConsts.RFCmdMsg.READ:
                    if (m.arg2 == SDConsts.RFResult.SUCCESS) {
                        bluebird_btn.setText("READ CODE");
                        bluebird_btn.setTextColor(Color.GREEN);
                        Log.d(TAG2, "lecturaRFID(): RFID leyendo");
                        String data = (String) m.obj;
                        if (data != null) {
                            utils.beepTone(BEEP_OK);
                            String[] epcData = data.split(";");
                            String epcHex = epcData[0];
                            epc = Epc.of(epcHex);

                            switch (elementScanClass) {
                                case SCAN_MODE_EPC:
                                    data = String.valueOf(epc);
                                    Toast.makeText(MainActivity.this, "EPC HEX: " + data, Toast.LENGTH_SHORT).show();
                                    break;
                                case SCAN_MODE_GARMENT:
                                    if (epc instanceof GarmentEpc) {
                                        data = ((GarmentEpc) epc).garmentCode().toString();
                                        Toast.makeText(MainActivity.this, "EPC GARMENT: " + data, Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(MainActivity.this, "EPC BEACON", Toast.LENGTH_SHORT).show();
                                    }
                                    break;
                                default:
                                    break;
                            }
                            UtilsKeys.clearKeys(webView);
                            UtilsKeys.loadKeys(webView, data);
                            Log.d(TAG2, "lecturaRFID(): Resultado: " + data);
                        } else {
                            utils.beepTone(BEEP_ERROR);
                        }
                    } else {
                        utils.beepTone(BEEP_ERROR);
                    }
                    break;
                case SDConsts.SDCmdMsg.SLED_UNKNOWN_DISCONNECTED:
                    Log.d(TAG2, "SLED desconectado");
                    bluebird_btn.setText("SLED OFF");
                    bluebird_btn.setTextColor(Color.RED);
                    break;
                case SDConsts.SDCmdMsg.SLED_BATTERY_STATE_CHANGED:
                    if (laserReader.SD_GetConnectState() != -32) {
                        bluebird_btn.setText("RFR ON");
                        bluebird_btn.setTextColor(Color.GREEN);
                    } else {
                        bluebird_btn.setText("RFR OFF");
                        Log.d(TAG2, "batState -32: " + laserReader.SD_GetConnectState());
                        bluebird_btn.setTextColor(Color.RED);
                    }
                    break;
                case SDConsts.SDBatteryState.LOW_BATTERY:
                    Log.d(TAG2, "SLED Bateria baja");
                    bluebird_btn.setText("LOW BATTERY");
                    bluebird_btn.setTextColor(Color.RED);
                    break;
                default:
                    bluebird_btn.setText("BLUEBIRD ON");
                    bluebird_btn.setTextColor(Color.GREEN);
                    break;
            }
        }
    };
}
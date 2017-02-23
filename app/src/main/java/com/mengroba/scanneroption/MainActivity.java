package com.mengroba.scanneroption;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
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

import me.sudar.zxingorient.Barcode;
import me.sudar.zxingorient.ZxingOrient;
import me.sudar.zxingorient.ZxingOrientResult;

public class MainActivity extends AppCompatActivity {

    //TAG para el Log Info
    private static final String TAG = "MainActivity";
    private static final String WEB_LOCAL =
            "file:///android_asset/main_menu.html";
    private static final int BEEP_OK = 1;
    private static final int BEEP_ERROR = 2;

    public WebView webView;
    private ProgressBar progressBar;

    private ZxingOrient scanner;
    private String scanContentResult;
    private String scanFormatResult;
    private WebAppInterface webInterface;
    //Elementos HTML
    private final String javascritpt = "javascript:(";
    private final String jsFunction = "function() {";

    private static final int BARCODE_RESULTCODE = 49374;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    private long eventDuration;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //fijamos el layout a utilizar
        setContentView(R.layout.activity_main);
        webInterface = new WebAppInterface(this);
        //definimos el visor HTML
        createWebView(this);
        // creamos el visor HTML
        startWebView();

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
        webView.addJavascriptInterface(new WebAppInterface(this), "Android");
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
        settings.setTextZoom(150);
        /*settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);*/

        webView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                eventDuration = event.getEventTime() - event.getDownTime();

                if (event.getAction() == MotionEvent.ACTION_UP) {

                    WebView.HitTestResult hr = ((WebView) v).getHitTestResult();
                    Log.d(TAG, "onTouch():findFocus" + v.findFocus());
                    Log.d(TAG, "HitTestResult:" + webView.getHitTestResult());
                    Log.d(TAG, "HitTestResult: getExtra = " + hr.getExtra() + "\t\t Type=" + hr.getType());
                    Log.d(TAG, "onTouch()eventDuration = " + eventDuration);

                    if (hr.getType() == 9 && eventDuration > 500) {

                        webView.loadUrl(javascritpt +
                                jsFunction +
                                "var listElementScanner = document.querySelectorAll('.scanner');" +
                                "console.log('num de class: ' + listElementScanner.length);" +
                                "for(var i = 0; i < listElementScanner.length; i++) {" +
                                "var elementScanner = listElementScanner[i];" +
                                "console.log('name de elemento: ' + elementScanner.name);" +
                                "if(elementScanner == document.activeElement){" +
                                "Android.startScan();" +
                                "}" +
                                    "}" +
                                "})()"
                        );
                    } else {
                        webView.loadUrl(javascritpt +
                                jsFunction +
                                "var listElementScanner = document.querySelectorAll('.scanner');" +
                                "console.log('num de class: ' + listElementScanner.length);" +
                                "for(var i = 0; i < listElementScanner.length; i++) {" +
                                "var elementScanner = listElementScanner[i];" +
                                "elementScanner.autocomplete = 'off';" +
                                "elementScanner.placeholder = 'Manten para escanear';" +
                                    "}" +
                                "})()"
                        );

                    }
                }
                return false;
            }
        });

        webView.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Log.d(TAG, "onFocusChange(): " + v.getOnFocusChangeListener());

                //TODO si el elemento tiene el class de html scanner entonces abrimos el scanner.
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
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // Mostramos el teclado al cargar la pagina de index
                Log.d(TAG, "onPageFinished(): "+ view.getTitle());
                if (view.getTitle().equals("Opciones")) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(view, 0);
                    //webView.setInitialScale(200);
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

    private void startScanMain() {
        //Creamos el scanner
        scanner = new ZxingOrient(MainActivity.this);
        scanner.setToolbarColor("#1c1c1c");
        scanner.setIcon(R.drawable.ic_barcode_scan);
        scanner.setInfo("Pulsa ATRÁS para cancelar");
        scanner.setInfoBoxColor("#1c1c1c");
        scanner.setBeep(true).initiateScan(Barcode.ONE_D_CODE_TYPES, -1);
    }

    private void loadKeys(String msg) {
        for (char character : msg.toCharArray()) {
            webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, UtilsKeys.getKeyEvent(character)));
        }
        webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
    }

    private void clearKeys() {
        webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MOVE_HOME));
        for (int i = 0; i < 25; i++) {
            webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_FORWARD_DEL));
        }
    }

    private void enterKeys() {
        webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
    }

    /**
     * Metodo ejecutado con el resultado del {@link Activity#startActivityForResult}, segun su utilizacion
     *
     * @param requestCode
     * @param resultCode
     * @param intent
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent intent) {

        if (requestCode == BARCODE_RESULTCODE) {
            //Cargamos la libreria Zxing a traves de scanResult y parseamos el resultado
            ZxingOrientResult scanResult = ZxingOrient.parseActivityResult(requestCode, resultCode, intent);
            scanFormatResult = scanResult.getFormatName();
            scanContentResult = scanResult.getContents();
            Log.i(TAG, "onActivityResult" + scanFormatResult);
            Log.i(TAG, "onActivityResult" + scanContentResult);
            String prueba = "1646265651114";
            if (scanResult.getContents() != null) {
                /*if (scanContentResult.length() != 13) {
                    Log.d(TAG, "Tonegenerator error beep");
                    beepTone(BEEP_ERROR);
                    //webInterface.textSpeech("El codigo no es correcto");
                }*/
                Log.d(TAG, "onActivityResult(): no es nulo");
                // Cuando se escanea como el foco lo tiene el elemento se simulan keypresseds
                clearKeys();
                loadKeys(scanContentResult);
                /*if (scanContentResult.equals(prueba)) {
                    Log.d(TAG, "onActivityResult(): valores coincidentes");
                    //beepTone(BEEP_OK);
                    //Toast.makeText(this, "El codigo es correcto.", Toast.LENGTH_LONG).show();

                } else {
                    Log.d(TAG, "onActivityResult(): valores no coincidentes");
                    Toast.makeText(this, "Error en el codigo. Vuelve a intentarlo.", Toast.LENGTH_LONG).show();
                }*/

            } else {
                Log.d(TAG, "onActivityResult()Scanner cancelado");
                clearKeys();
                enterKeys();
                Toast.makeText(this, "No se ha obtenido ningun dato", Toast.LENGTH_SHORT).show();
                //beepTone(BEEP_ERROR);
                //webInterface.textSpeech("No se ha obtenido ningun dato");
            }
        }
    }

    /*private int getScale(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int widthDevice = displayMetrics.widthPixels;

        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int width = display.getWidth();
        Double val = new Double(width)/new Double(widthDevice);
        val = val * 100d;
        return val.intValue();
    }*/

    public void beepTone(int status) {
        ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);

        try {
            switch (status) {
                case BEEP_OK:
                    tg.startTone(ToneGenerator.TONE_PROP_ACK, 500);
                    Thread.sleep(500);
                    tg.release();
                    break;
                case BEEP_ERROR:
                    tg.startTone(ToneGenerator.TONE_PROP_NACK, 500);
                    Thread.sleep(500);
                    tg.release();
                    break;
                default:
                    tg.startTone(ToneGenerator.TONE_PROP_NACK, 500);
                    Thread.sleep(500);
                    tg.release();
                    break;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

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
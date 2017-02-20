package com.mengroba.scanneroption;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.ConsoleMessage;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.mengroba.scanneroption.utils.UtilsKeys;

import me.sudar.zxingorient.Barcode;
import me.sudar.zxingorient.ZxingOrient;
import me.sudar.zxingorient.ZxingOrientResult;

public class MainActivity extends AppCompatActivity {

    //TAG para el Log Info
    private static final String TAG = "MainActivity";
    private static final int STATE_HOMEPAGE = 0;
    private static final int STATE_SCAN = 3;
    private static final String WEB_HOME = "file:///android_asset/main_menu.html";

    public WebView webView;
    private ProgressBar progressBar;
    private int state;

    private String url;
    private ZxingOrient scanner;
    private String scanContentResult;
    private String scanFormatResult;
    //Elementos HTML
    private final String javascritpt = "javascript:(";
    private final String jsFunction = "function() {";

    private static final int BARCODE_RESULTCODE = 49374;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //fijamos el layout a utilizar
        setContentView(R.layout.activity_main);
        //creamos el visor HTML
        createWebView(this);

        //Cargamos el WebView segun las elecciones en el HTML
        //Por defecto se cargaria siempre la pagina principal
        /*state = getIntent().getIntExtra("STATE", STATE_HOMEPAGE);
        switch (state) {
            case STATE_HOMEPAGE:
                webView.loadUrl(WEB_HOME);
                break;
            *//*Si se ha pulsado el boton de escanear codigo de barras, se hace uso de la
            libreria zxing para lanzar el escaner*//*
            case STATE_SCAN:
                //se edita el layout del scanner
                scanner = new ZxingOrient(MainActivity.this);
                scanner.setToolbarColor("#1c1c1c");
                scanner.setIcon(R.drawable.ic_barcode_scan);
                scanner.setInfo("Pulsa ATRÁS para cancelar");
                scanner.setInfoBoxColor("#1c1c1c");
                scanner.setBeep(true).initiateScan(Barcode.ONE_D_CODE_TYPES, -1);
                break;
            default:
                webView.loadUrl(WEB_HOME);
                break;
        */ //Fin del switch

        // definimos el visor HTML
        startWebView();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    /**
     * Creamos el visor WebView para cargar el HTML
     */
    private void createWebView(Context context) {

        //Enlazamos los elementos graficos
        webView = (WebView) findViewById(R.id.webView1);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        //Enlazamos WebAppInterface entre el codigo JavaScript y el codigo Android
        webView.addJavascriptInterface(new WebAppInterface(this), "Android");
        //Accedemos a la configuracion del WebView y habilitamos el javascript
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebContentsDebuggingEnabled(true);
        // Ajustamos el HTML al WebView
        webView.getSettings().setLoadWithOverviewMode(true);
        //añadimos scroll
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(false);
        //habilitamos el uso de mediaplayer sin gestos
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        //habilitamos las opciones de zoom
        /*webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);*/
        //Si queremos habilitar plugins al WebView (no se recomienda por seguridad)
        //webView.getSettings().setPluginState(WebSettings.PluginState.OFF);
        //habilitamos el tratamiento de ficheros
        webView.getSettings().setAllowFileAccess(true);

        webView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                /*if (v.hasFocus()) {
                    v.requestFocus();
                }*/

                if (event.getAction() == MotionEvent.ACTION_UP) {

                    WebView.HitTestResult hr = ((WebView) v).getHitTestResult();
                    Log.d(TAG, "HitTestResult:" + webView.getHitTestResult());
                    Log.d(TAG, "getExtra = " + hr.getExtra() + "\t\t Type=" + hr.getType());

                    if (hr.getType() == 9) {
                        /*webView.loadUrl(javascritpt +
                                jsFunction +
                                    "var elementClass = document.getElementsByTagName('class');" +
                                    "for(var i = 0; i < elementClass.length; i++) {" +
                                        "console.log('num de class: ' + elementClass.length);" +
                                        "if(scanClass.className.toLowerCase() == 'scanner') {" +
                                            "var elementClass = document.getElementsByTagName('class');" +
                                            "console.log('valor scanClass: ' + scanClass);" +
                                            "Android.startScan();" +
                                        "}" +
                                    "}" +
                                "})()"
                        );*/
                        /*webView.loadUrl(javascritpt +
                                jsFunction +
                                    "var elementClass = document.getElementsByTagName('class');" +
                                    "for(var i = 0; i < elementClass.length; i++) {" +
                                        "console.log('num de class: ' + elementClass.length);" +
                                        "if(scanClass.className.toLowerCase() == 'scanner') {" +
                                            "var elementClass = document.getElementsByTagName('class');" +
                                            "console.log('valor scanClass: ' + scanClass);" +
                                            "Android.startScan();" +
                                        "}" +
                                    "}" +
                                "})()"
                        );*/

                        Log.d(TAG, "onTouch():findFocus" + v.findFocus());
                        startScanMain();
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
                //Pero como sabemos que elemento es al que hay incluir la clase? Como lo detectamos?
                if (hasFocus) {
                    Toast.makeText(getApplicationContext(), "Has Focus", Toast.LENGTH_SHORT).show();
                }
                /*webView.loadUrl(javascritpt +
                        jsFunction +
                            "var inputs = document.getElementsByTagName('input');" +
                            "for(var i = 0; i < inputs.length; i++) {" +
                                "console.log('num de inputs: ' + inputs.length);" +
                                "if(inputs[i].type.toLowerCase() == 'text') {" +
                                    "console.log('valor de input: ' + inputs[i].value);" +
                                    "alert(inputs[i].value);" +
                                "}" +
                            "}" +
                        "})()"
                );*/

            }
        });

        webView.loadUrl(WEB_HOME);

    } //Fin de createWebView()

    /**
     * Iniciamos un cliente de WebView que es llamado al abrir el HTML
     */
    private void startWebView() {

        // Hacemos que todas las paginas se carguen en el mismo WebView
        webView.setWebViewClient(new WebViewClient() {
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

    private void loadData(String msg) {
        for (char character : msg.toCharArray()) {
            webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, UtilsKeys.getKeyEvent(character)));
        }
        //webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
    }

    private void clearData() {
        webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MOVE_HOME));
        for (int i = 0; i < 25; i++) {
            webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_FORWARD_DEL));
        }
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
                Log.d(TAG, "onActivityResult(): no es nulo");
                // Cuando se escanea como el foco lo tiene el elemento se simulan keypresseds
                clearData();
                loadData(scanContentResult);
                if (scanContentResult.equals(prueba)) {
                    Log.d(TAG, "onActivityResult(): valores coincidentes");
                    Toast.makeText(this, "El codigo es correcto.", Toast.LENGTH_LONG).show();

                } else {
                    Log.d(TAG, "onActivityResult(): valores no coincidentes");
                    Toast.makeText(this, "Error en el codigo. Vuelve a intentarlo.", Toast.LENGTH_LONG).show();
                }
                //webView.loadUrl("https://www.google.es");
                //webView.loadUrl("file:///android_asset/scannerTest.html?value=" + scanContentResult);
                //Podemos pasar la informacion al usuario, para ello usamos un dialogo emergente
                /*AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder
                        .setMessage("El formato es: " + scanFormatResult + "\n" +
                                "y el codigo es: " + scanContentResult)
                        .setPositiveButton("Guardar codigo", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(MainActivity.this, "Guardando codigo", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                webView.loadUrl("file:///android_asset/scannerOK.html?valor=" + scanContentResult);
                            }
                        })
                        .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(MainActivity.this, "Cancelando operación...", Toast.LENGTH_LONG).show();
                                dialog.dismiss();
                                finish();
                            }
                        });
                //Creamos el dialogo
                builder.create().show();*/

            } else {
                Log.d(TAG, "onActivityResult()Scanner cancelado");
                clearData();
                Toast.makeText(this, "No se ha obtenido ningun dato", Toast.LENGTH_SHORT).show();
                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
            }
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
package com.mengroba.scanneroption;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import me.sudar.zxingorient.Barcode;
import me.sudar.zxingorient.ZxingOrient;
import me.sudar.zxingorient.ZxingOrientResult;

public class MainActivity extends AppCompatActivity {

    //TAG para el Log Info
    private static final String TAG = "MainActivity";
    private static final int STATE_HOMEPAGE = 0;
    private static final int STATE_SCAN = 3;
    private static final int STATE_VOICE = 5;
    private static final String WEB_HOME = "file:///android_asset/main_menu.html";

    public WebView webView;
    private ProgressBar progressBar;
    private int state;

    private String url;
    private ZxingOrient scanner;
    private String scanContentResut;
    private String scanFormatResut;

    private static final int BARCODE_RESULTCODE = 49374;
    private String textVoice;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //fijamos el layout a utilizar
        setContentView(R.layout.activity_main);
        //TTS: textToSpeech = new TextToSpeech( this, this );
        //creamos el visor HTML
        createWebView(this);
        //Cargamos el WebView segun las elecciones en el HTML
        //Por defecto se cargaria siempre la pagina principal
        state = getIntent().getIntExtra("STATE", STATE_HOMEPAGE);
        switch (state) {
            case STATE_HOMEPAGE:
                webView.loadUrl(WEB_HOME);
                break;
            /*Si se ha pulsado el boton de escanear codigo de barras, se hace uso de la
            libreria zxing para lanzar el escaner*/
            case STATE_SCAN:
                //se edita el layout del scanner
                scanner = new ZxingOrient(MainActivity.this);
                scanner.setToolbarColor("#1c1c1c");
                scanner.setIcon(R.drawable.ic_barcode_scan);
                scanner.setInfo("Pulsa ATRÁS para cancelar");
                scanner.setInfoBoxColor("#1c1c1c");
                scanner.setBeep(true).initiateScan(Barcode.ONE_D_CODE_TYPES, -1);
                break;
            case STATE_VOICE:
                //metodo para TTS
                textVoice = getIntent().getStringExtra("MSG");
                Toast.makeText(this, textVoice, Toast.LENGTH_SHORT).show();
               /* textToSpeech.setLanguage( new Locale( "spa", "ESP" ) );
                speak(textVoice);*/
            default:
                webView.loadUrl(WEB_HOME);
                break;
        } //Fin del switch

        // definimos el visor HTML
        startWebView();

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
        // Ajustamos el HTML al WebView
        webView.getSettings().setLoadWithOverviewMode(true);
        //añadimos scroll
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(false);
        //habilitamos el uso de mediaplayer sin gestos
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        //habilitamos las opciones de zoom
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        //Si queremos habilitar plugins al WebView (no se recomienda por seguridad)
        //webView.getSettings().setPluginState(WebSettings.PluginState.OFF);
        //habilitamos el tratamiento de ficheros
        webView.getSettings().setAllowFileAccess(true);
    }

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
        });   // Fin de setWebChromeClient

    } //Fin de startWebView

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
            scanFormatResut = scanResult.getFormatName();
            scanContentResut = scanResult.getContents();
            Log.i(TAG, "onActivityResult" + scanFormatResut);
            Log.i(TAG, "onActivityResult" + scanContentResut);
            if (scanResult.getContents() != null) {
                webView.loadUrl("file:///android_asset/scannerOK.html?valor=" + scanContentResut);
                //Podemos pasar la informacion al usuario, para ello usamos un dialogo emergente
                /*AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder
                        .setMessage("El formato es: " + scanFormatResut + "\n" +
                                "y el codigo es: " + scanContentResut)
                        .setPositiveButton("Guardar codigo", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(MainActivity.this, "Guardando codigo", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                webView.loadUrl("file:///android_asset/scannerOK.html?valor=" + scanContentResut);
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
                Toast.makeText(this, "No se ha obtenido ningun dato", Toast.LENGTH_SHORT).show();
                finish();
            }
        }


    }

    /**
     * Metodo sobreescrito para iniciar el servicio TTS (Text to Speech)
     */
    /*@Override
    public void onInit(int status) {
        if ( status == TextToSpeech.LANG_MISSING_DATA | status == TextToSpeech.LANG_NOT_SUPPORTED )
        {
            Toast.makeText( this, "Error, en datos de lenguaje | Lenguaje no soportado", Toast.LENGTH_SHORT ).show();
        }

    }

    private void speak(String str){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            newSpeak(str);
        }else{
            oldSpeak(str);
        }
    }
    @SuppressWarnings("deprecation")
    public void oldSpeak(String text){
        HashMap<String, String> map = new HashMap<>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, map );
        textToSpeech.setSpeechRate( 1.0f );
        textToSpeech.setPitch( 1.0f );
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void newSpeak(String text){
        String utteranceId=this.hashCode() + "";
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH,null,utteranceId);
        textToSpeech.setSpeechRate( 1.0f );
        textToSpeech.setPitch( 1.0f );
    }

    @Override
    protected void onDestroy()
    {
        if ( textToSpeech != null )
        {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }*/


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
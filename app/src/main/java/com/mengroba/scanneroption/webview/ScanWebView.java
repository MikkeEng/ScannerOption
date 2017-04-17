package com.mengroba.scanneroption.webview;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
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

import com.mengroba.scanneroption.ScanOptionActivity;
import com.mengroba.scanneroption.bluebird.BluebirdMode;
import com.mengroba.scanneroption.javascript.WebAppInterface;

import static com.mengroba.scanneroption.javascript.JSConstants.JS_FUNCTION;
import static com.mengroba.scanneroption.javascript.JSConstants.JS_JAVASCRIPT;
import static com.mengroba.scanneroption.javascript.JSConstants.JS_JAVASCRIPT_LISTENER;
import static com.mengroba.scanneroption.javascript.JSConstants.JS_LOAD_PAGE;
import static com.mengroba.scanneroption.javascript.JSConstants.JS_SCAN_MODE;
import static com.mengroba.scanneroption.javascript.JSConstants.JS_START_CAMSCAN_IF_EMPTY;
import static com.mengroba.scanneroption.javascript.JSConstants.JS_TEXT_SPEECH;

/**
 * Created by miguelef on 12/04/2017.
 */

public class ScanWebView {

    private static final String TAG = "ScanWebView";
    private static final String WEB_TEST = "file:///android_asset/main_menu.html";
    private static final String WEB_LOCAL = "http://10.236.3.80:8080/wms-pme-hht/login.htm";
    private static final String WEB_SERVER = "http://-------------/wms-pme-hht/login.htm";
    private static final String WEB_SELECTION = "http://localhost:8080/wms-pme-hht/userActions.htm";

    private Context context;
    private BluebirdMode bluebirdMode;
    private ScanOptionActivity activity;
    private WebView webView;
    private Button bluebird_btn;
    private WebAppInterface webInterface;
    private ProgressBar progressBar;
    private long eventDuration;

    public ScanWebView(ScanOptionActivity activity, WebView webView) {
        this.activity = activity;
        this.webView = webView;
    }

    /**
     * Creamos el visor WebView para cargar el HTML
     */
    public void createWebView() {
        this.bluebird_btn = activity.getMain_btn();

        //Configuramos el webview
        WebSettings settings = webView.getSettings();
        //Enlazamos WebAppInterface entre el codigo JavaScript y el codigo Android
        webView.addJavascriptInterface(new WebAppInterface(context, webView), "Android");
        //Accedemos a la configuracion del WebView y habilitamos el javascript
        settings.setJavaScriptEnabled(true);
        webView.setWebContentsDebuggingEnabled(true);
        // Ajustamos el WebView
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
                    Log.d(TAG, "onTouch().HitTestResult:getExtra: " + hr.getExtra() + "\t\t Type=" + hr.getType());
                    Log.d(TAG, "onTouch()eventDuration: " + eventDuration);

                    if (hr.getType() == 9 && eventDuration < 500) {
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
    public void startWebView() {

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
                Toast.makeText(activity, "Hay un problema con la red", Toast.LENGTH_LONG).show();
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
                activity.setProgress(progress * 1000);

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
                activity.finish();
                Log.d(TAG, "Cerrando ventana" + window);
            }
        });   // Fin de setWebChromeClient()

    } //Fin de startWebView()


}

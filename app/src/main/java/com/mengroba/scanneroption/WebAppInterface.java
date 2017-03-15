package com.mengroba.scanneroption;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;
import me.sudar.zxingorient.Barcode;
import me.sudar.zxingorient.ZxingOrient;


/**
 * Created by mengroba on 25/01/2017.
 */

/**
 * Clase que hace de Interface entre Android y JavaScript, agrupando los metodos/funciones entre
 * ambas.
 */
public class WebAppInterface implements TextToSpeech.OnInitListener {

    //Codigo JS//
    public static final String JS_JAVASCRIPT = "javascript:(";
    public static final String JS_FUNCTION = "function() {";
    public static final String JS_LOAD_PAGE =
                    "var listElementScanner = document.querySelectorAll('.scanner');" +
                    "var listElementLaser = document.querySelectorAll('.laser');" +
                    "var actElement = document.activeElement;" +
                        "for(var i = 0; i < listElementScanner.length; i++) {" +
                            "var elementScanner = listElementScanner[i];" +
                            "elementScanner.autocomplete = 'off';" +
                            "elementScanner.placeholder = 'Pulsa y escanea';" +
                            //"Android.textSpeech('Pulsa y escanea');" +
                        "}" +
                    "})()";
    public static final String JS_ELEMENT_SCANNER =
            "var listElementScanner = document.querySelectorAll('.scanner');" +
                    "var actElement = document.activeElement;" +
                    "for(var i = 0; i < listElementScanner.length; i++) {" +
                    "var elementScanner = listElementScanner[i];";
    public static final String JS_TEXT_SPEECH =
                     "var listElementVoice = document.querySelectorAll('.errorMessage');" +
                        "for(var i = 0; i < listElementVoice.length; i++) {" +
                            "var elementVoice = listElementVoice[i];" +
                            "console.log('element voice: ' + elementVoice);" +
                            "console.log('error: ' + elementVoice.innerHTML);" +
                            "Android.textSpeech(elementVoice.innerHTML);" +
                        "}" +
                    "})()";
    public static final String JS_START_SCAN_IF_EMPTY =
            JS_ELEMENT_SCANNER +
                        "var elementValue = elementScanner.value;" +
                            "if(elementScanner == actElement && !elementValue){" +
                                "Android.startScan();" +
                            "}" +
                        "}" +
                    "})()";

    private static final String TAG = "WebAppInterface";
    private static final int STATE_SCAN = 3;
    private static final int STATE_CAMERA = 4;

    private Context context;
    private WebView webView;
    private ZxingOrient scanner;
    private Intent intent;
    private TextToSpeech tts;
    private String msg;
    private Boolean ttsOk = true;

    public WebAppInterface(Context context) {
        this.context = context;
    }

    public WebAppInterface(Context context, WebView webView) {
        this.context = context;
        this.webView = webView;
    }

    @JavascriptInterface
    public void openCameraFile() {
        //Pasamos a MainActivity el estado correspondiente a captura de camara
        intent = new Intent(context, MainActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("STATE", STATE_CAMERA);
        context.startActivity(intent);
    }

    @JavascriptInterface
    public void startScan() {
        //Creamos el scanner
        scanner = new ZxingOrient((Activity) context);
        scanner.setToolbarColor("#1c1c1c");
        scanner.setIcon(R.drawable.ic_barcode_scan);
        scanner.setInfo("Pulsa ATRÁS para cancelar");
        scanner.setInfoBoxColor("#1c1c1c");
        scanner.setBeep(true).initiateScan(Barcode.ONE_D_CODE_TYPES, -1);
    }

    /**
     * Muestra un cuadro de dialogo del mensaje pasado por parametro en el HTML
     *
     * @param msg
     */
    @JavascriptInterface
    public void showDialog(String msg) {
        //Usamos una clase Builder para la construccion del dialogo
        AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
        builder.setMessage(msg).setNeutralButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        //Creamos el dialogo
        builder.create().show();
    }

    /**
     * Creamos un Toast con el mensaje pasado por parametro en el HTML     *
     *
     * @param msg
     */
    @JavascriptInterface
    public void makeToastAndroid(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * Accedemos al modulo de captura de codigo de barras
     */
    @JavascriptInterface
    public void scanBarcode() {
        //Pasamos a MainActivity el estado correspondiente al escaner de barras
        intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("STATE", STATE_SCAN);
        context.startActivity(intent);
    }

    @JavascriptInterface
    public void finishWindow() {
        ((Activity) context).finish();
    }

    /**
     * Metodo para pasar a voz un mensaje de texto
     *
     * @param msg
     */
    @JavascriptInterface
    public void textSpeech(String msg) {
        this.msg = msg;
        tts = new TextToSpeech(context, this);
    }

    @Override
    // Metodo para recibir el estado del motor de TTS
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            ttsOk = true;
            speak(msg, true);
        } else {
            tts.stop();
            tts.shutdown();
            ttsOk = false;
        }
    }

    // Metodo de habla del TTS
    @SuppressWarnings("deprecation")
    public void speak(String text, Boolean override) {
        if (ttsOk) {
            if (override) {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            } else {
                tts.speak(text, TextToSpeech.QUEUE_ADD, null);
            }
        }
    }
}

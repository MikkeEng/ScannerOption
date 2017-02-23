package com.mengroba.scanneroption;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.view.KeyEvent;
import android.webkit.JavascriptInterface;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    //JS//
    public static final String JS_JAVASCRIPT = "javascript:(";
    public static final String JS_FUNCTION = "function() {";
    public static final String JS_ELEMENT_SCANNER =
            "var listElementScanner = document.querySelectorAll('.scanner');" +
                    "var actElement = document.activeElement;" +
                    "for(var i = 0; i < listElementScanner.length; i++) {" +
                    "var elementScanner = listElementScanner[i];";
    public static final String JS_START_SCAN = JS_ELEMENT_SCANNER +
            "if(elementScanner === actElement){" +
            "Android.startScan();" +
            "}" +
            "}" +
            "})()";
    public static final String JS_START_SCAN_IF_EMPTY = JS_ELEMENT_SCANNER +
            "var elementValue = elementScanner.value;" +
            "elementScanner.autocomplete = 'off';" +
            "elementScanner.placeholder = 'Pulsa para escanear';" +
            "console.log('name de elemento: ' + elementScanner.name);" +
            "console.log('elemento activo: ' + actElement);" +
            "console.log('valor de elemento: ' + elementValue);" +
            "if(elementScanner === actElement && !elementValue){" +
            "console.log('Activacion de escaner');" +
            "Android.startScan();" +
            "}" +
            "}" +
            "})()";

    private Context context;
    private static final int STATE_SEARCH = 1;
    private static final int STATE_SCAN = 3;
    private static final int STATE_CAMERA = 4;
    private ZxingOrient scanner;
    private Intent intent;
    private TextToSpeech tts;
    private String msg;
    private Boolean ttsOk = true;

    /**
     * Constructor de la clase WebAppInterface
     *
     * @param context
     */
    public WebAppInterface(Context context) {
        this.context = context;
    }

    /**
     * Accedemos al modulo de apertura de camara
     */
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

    public static Map<String, List<String>> getParamsByUrl(String url) {
        try {
            Map<String, List<String>> params = new HashMap<String, List<String>>();
            String[] urlParts = url.split("\\?");
            if (urlParts.length > 1) {
                String query = urlParts[1];
                for (String param : query.split("&")) {
                    String[] pair = param.split("=");
                    String key = URLDecoder.decode(pair[0], "UTF-8");
                    String value = "";
                    if (pair.length > 1) {
                        value = URLDecoder.decode(pair[1], "UTF-8");
                    }

                    List<String> values = params.get(key);
                    if (values == null) {
                        values = new ArrayList<String>();
                        params.put(key, values);
                    }
                    values.add(value);
                }
            }

            return params;
        } catch (UnsupportedEncodingException ex) {
            throw new AssertionError(ex);
        }
    }

    /**
     * Muestra un cuadro de dialogo del mensaje pasado por parametro en el HTML
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
     * Metodo para pasar a voz un mensaje de texto
     * @param msg
     */
    @JavascriptInterface
    public void textSpeech(String msg) {
        this.msg = msg;
        tts = new TextToSpeech(context, this);
    }

    /**
     * Creamos un Toast con el mensaje pasado por parametro en el HTML     *
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
    public void finishWindow(){
        ((Activity)context).finish();
    }

    @Override
    // OnInitListener method to receive the TTS engine status
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            ttsOk = true;
            speak(msg, true);
        }
        else {
            tts.stop();
            tts.shutdown();
            ttsOk = false;
        }
    }

    // A method to speak something
    @SuppressWarnings("deprecation") // Support older API levels too.
    public void speak(String text, Boolean override) {
        if (ttsOk) {
            if (override) {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            }
            else {
                tts.speak(text, TextToSpeech.QUEUE_ADD, null);
            }
        }
    }
}

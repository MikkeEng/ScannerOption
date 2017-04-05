package com.mengroba.scanneroption;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.speech.tts.TextToSpeech;
import android.util.Log;
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

    private static final String TAG = "WebAppInterface";
    private static final int RFID_MODE_EPC = 1;
    private static final int RFID_MODE_GARMENT = 2;

    private Context context;
    private MainActivity activity;
    private WebView webView;
    private ZxingOrient scanner;
    private TextToSpeech tts;
    private String msg;
    private Boolean ttsOk = true;
    private String elementScanClass;

    public WebAppInterface(Context context) {
        this.context = context;
    }

    public WebAppInterface(Context context, WebView webView) {
        this.context = context;
        this.webView = webView;
    }

    /**
     * Accedemos al modulo de captura de codigo de barras
     */
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
     * Configuramos el modo de lectura
     */
    @JavascriptInterface
    public void setScanMode(String elementScanClass) {
        this.elementScanClass = elementScanClass;
        this.activity = (MainActivity) context;
        activity.setModeScan(elementScanClass);
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
     * Metodo para pasar a voz un mensaje de texto
     *
     * @param msg
     */
    @JavascriptInterface
    public void findMsg(String elementScanClass, String msg) {
        if(elementScanClass.contains("error")){
            msg = "Error, " + msg;
        }
        Log.d(TAG, "tts.msg:" + msg);
        String result = msg.replaceAll("(?=[0-9]+).", "$0 ").trim();
        Log.d(TAG, "tts.msg:" + result);
        this.msg = result;
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

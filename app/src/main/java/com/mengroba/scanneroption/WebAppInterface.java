package com.mengroba.scanneroption;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;
//SCANNER
import co.kr.bluebird.ser.protocol.Reader;
import co.kr.bluebird.ser.protocol.SDConsts;
import me.sudar.zxingorient.Barcode;
import me.sudar.zxingorient.ZxingOrient;
//LASER


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
    public static final String JS_LOAD_PAGE =
            "var listElementScanner = document.querySelectorAll('.scanner');" +
                    "var actElement = document.activeElement;" +
                    "for(var i = 0; i < listElementScanner.length; i++) {" +
                    "var elementScanner = listElementScanner[i];" +
                    "elementScanner.autocomplete = 'off';" +
                    "elementScanner.placeholder = 'Pulsa y escanea';" +
                    "}" +
                    "})()";
    public static final String JS_ELEMENT_SCANNER =
            "var listElementScanner = document.querySelectorAll('.scanner');" +
                    "var actElement = document.activeElement;" +
                    "for(var i = 0; i < listElementScanner.length; i++) {" +
                    "var elementScanner = listElementScanner[i];";
    public static final String JS_ELEMENT_LASER =
            "var listElementLaser = document.querySelectorAll('.laser');" +
                    "var actElement = document.activeElement;" +
                    "for(var i = 0; i < listElementLaser.length; i++) {" +
                    "var elementLaser = listElementLaser[i];";
    public static final String JS_START_SCAN = JS_ELEMENT_SCANNER +
            "if(elementScanner === actElement){" +
            "Android.startScan();" +
            "}" +
            "}" +
            "})()";
    public static final String JS_START_SCAN_IF_EMPTY =
            JS_ELEMENT_SCANNER +
                    "var elementValue = elementScanner.value;" +
                    "if(elementScanner === actElement && !elementValue){" +
                    "Android.startScan();" +
                    "}" +
                    "}" +
                    "})()";
    public static final String JS_START_LASER_IF_EMPTY =
            JS_ELEMENT_SCANNER +
                    "var lasertValue = elementLaser.value;" +
                    "if(elementLaser === actElement && !lasertValue){" +
                    "Android.startLaser();" +
                    "}" +
                    "}" +
                    "})()";

    private static final String TAG = "WebAppInterface";

    private Context context;
    private static final int STATE_SEARCH = 1;
    private static final int STATE_SCAN = 3;
    private static final int STATE_CAMERA = 4;
    private ZxingOrient scanner;
    private Intent intent;
    private TextToSpeech tts;
    private String msg;
    private Boolean ttsOk = true;
    private Reader laserReader;

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

    @JavascriptInterface
    public void startLaser() {
        laserReader = Reader.getReader(context, laserHandler);
        laserReader.BC_SetTriggerState(true);

    }


    public Handler laserHandler = new Handler() {
        public void handleMessage(Message m) {
            Log.d(TAG, "laserHandler");
            Log.d(TAG, "command = " + m.arg1 + " result = " + m.arg2 + " obj = data");

            if (m.what == SDConsts.Msg.BCMsg) {
                StringBuilder readData = new StringBuilder();
                if (m.arg1 == SDConsts.BCCmdMsg.BARCODE_TRIGGER_PRESSED)
                    Toast.makeText(context, "LASER ACTIVADO", Toast.LENGTH_SHORT).show();
                else if (m.arg1 == SDConsts.BCCmdMsg.BARCODE_TRIGGER_RELEASED)
                    Toast.makeText(context, "LASER DESACTIVADO", Toast.LENGTH_SHORT).show();
                else if (m.arg1 == SDConsts.BCCmdMsg.BARCODE_READ) {
                    if (m.arg2 == SDConsts.BCResult.SUCCESS)
                        readData.append(" " + "LASER LEYENDO CODIGO");
                    else if (m.arg2 == SDConsts.BCResult.ACCESS_TIMEOUT)
                        readData.append(" " + "TIEMPO DE ESPERA EXPIRADO");
                    if (m.obj != null)
                        readData.append("\n" + (String) m.obj);
                    Toast.makeText(context, "\" \" + readData.toString()", Toast.LENGTH_SHORT).show();
                }
                Log.d(TAG, "RESULTADO = " + readData.toString());
            }
        }
    };

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

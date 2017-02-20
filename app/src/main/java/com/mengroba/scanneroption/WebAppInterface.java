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

    private Context context;
    private static final String URLGOOGLE =
            "https://www.google.es/search?sourceid=chrome-psyapi2&rlz=1C1CAFA_enES728ES728&ion=1&espv=2&ie=UTF-8&q=";
    private static final String URLMAP1 = "https://www.google.es/maps/place//@";
    private static final String URLMAP2 = ",15z/data=!4m5!3m4!1s0x0:0x0!8m2!3d";
    private static final int STATE_SEARCH = 1;
    private static final int STATE_MAP = 2;
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
     * Muestra un cuadro de dialogo del mensaje pasado por parametro en el HTML
     * @param msg
     */
    @JavascriptInterface
    public void showScanCode(String msg) {
        //Usamos una clase Builder para la construccion del dialogo
        AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
        builder
                .setMessage("El codigo es: " + msg)
                .setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(context, "Guardando codigo", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(context, "Cancelando operación...", Toast.LENGTH_LONG).show();
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

        /**
     * Metodo que lanza el buscador cuando se presiona el boton correspondiente en el HTML     *
     * @param texto
     */
    @JavascriptInterface
    public void showWebPage(String texto) {
        //Pasamos a MainActivity el texto a buscar
        intent = new Intent(context, MainActivity.class);
        intent.putExtra("STATE", STATE_SEARCH);
        intent.putExtra("SEARCH", URLGOOGLE + texto);
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

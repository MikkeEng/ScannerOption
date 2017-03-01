package com.mengroba.scanneroption.laser;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.mengroba.scanneroption.WebAppInterface;

import co.kr.bluebird.ser.protocol.Reader;
import co.kr.bluebird.ser.protocol.SDConsts;

/**
 * Created by miguelef on 27/02/2017.
 */

public class LaserScan {

    private static final String TAG = "laserScan";
    private static final String MY_PACKAGE = "com.mengroba.scanneroption.laser";
    private Context context;
    public static final int REQUEST_CODE = 50;

    private WebAppInterface webInterface;
    private Reader barcodeReader;
    private Handler barcodeHandler;


    public LaserScan(Context context) {
        this.context = context;
    }

    public void startLaserScan() {

        webInterface = new WebAppInterface(context);
        barcodeHandler = new Handler() {
            public void handleMessage(Message m) {
                Log.d(TAG, "laserHandler");
                Log.d(TAG, "command = " + m.arg1 + " result = " + m.arg2 + " obj = data");

                if (m.what == SDConsts.Msg.BCMsg) {
                    StringBuilder readData = new StringBuilder();
                    if (m.arg1 == SDConsts.BCCmdMsg.BARCODE_TRIGGER_PRESSED)
                        Log.d(TAG, "startLaserScan(): Laser activado");
                        //Toast.makeText(context, "LASER ACTIVADO", Toast.LENGTH_SHORT).show();
                    else if (m.arg1 == SDConsts.BCCmdMsg.BARCODE_TRIGGER_RELEASED)
                        Log.d(TAG, "startLaserScan(): Laser desactivado");
                        //Toast.makeText(context, "LASER DESACTIVADO", Toast.LENGTH_SHORT).show();
                    else if (m.arg1 == SDConsts.BCCmdMsg.BARCODE_READ) {
                        if (m.arg2 == SDConsts.BCResult.SUCCESS)
                            Log.d(TAG, "startLaserScan(): Laser leyendo");
                            //Toast.makeText(context, "LASER LEYENDO CODIGO", Toast.LENGTH_SHORT).show();
                        else if (m.arg2 == SDConsts.BCResult.ACCESS_TIMEOUT)
                            Log.d(TAG, "startLaserScan(): Laser expirado");
                        //Toast.makeText(context, "TIEMPO DE ESPERA EXPIRADO", Toast.LENGTH_SHORT).show();
                        if (m.obj != null) {
                            String resultFull = readData.append((String) m.obj).toString();
                            String code = resultFull.substring(0, resultFull.indexOf(";"));
                            Log.d(TAG, "startLaserScan(): Resultado: " + code);
                            //initLaser(code);
                            //Pasamos la informacion al usuario, para ello usamos un dialogo emergente
                            webInterface.showDialog("Codigo capturado: " + code);
                        }
                    }
                    Log.d(TAG, "RESULTADO = " + readData.toString());
                }
            }
        };

        barcodeReader = Reader.getReader(context, barcodeHandler);

        //barcodeReader.BC_SetTriggerState(true);
    }

    public void initLaser(String value) {
        Intent intentLaser = new Intent(MY_PACKAGE + ".LASER_SCAN");
        intentLaser.addCategory(Intent.CATEGORY_DEFAULT);
        intentLaser.putExtra("Codigo", value);
        intentLaser.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intentLaser.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        startActivityForResult(intentLaser, REQUEST_CODE); //Are you missing a call to unregisterReceiver()?
    }

    protected void startActivityForResult(Intent intent, int code) {
        Activity activity = (Activity) context;
        activity.startActivityForResult(intent, code);
    }

    public static LaserResult parseActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                String contents = intent.getStringExtra("Codigo");
                return new LaserResult(contents);
            }
            return new LaserResult();
        }
        return null;
    }

}

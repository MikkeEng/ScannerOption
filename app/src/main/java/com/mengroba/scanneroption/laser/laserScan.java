package com.mengroba.scanneroption.laser;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.mengroba.scanneroption.MainActivity;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

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

    private final Activity activity;
    private final android.app.Fragment fragment;
    private final android.support.v4.app.Fragment supportFragment;
    private Reader laserReader;
    private final Map<String, Object> moreExtras = new HashMap<String, Object>(1);


    public LaserScan(Activity activity) {
        this.activity = activity;
        this.fragment = null;
        this.supportFragment = null;
    }

    public void startLaserScan() {

        Handler laserHandler = new Handler() {
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
                            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(activity);
                            builder
                                    .setMessage("El codigo es: " + code)
                                    .setNeutralButton("Aceptar", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                            //Creamos el dialogo
                            builder.create().show();
                        }

                    }
                    Log.d(TAG, "RESULTADO = " + readData.toString());
                }
            }
        };
        laserReader = Reader.getReader(activity, laserHandler);
        boolean openResult = laserReader.RF_Open();
        if (openResult == SDConsts.RF_OPEN_SUCCESS) {
            Log.d(TAG, "Lector abierto");
        } else if (openResult == SDConsts.RF_OPEN_FAIL)
            Log.e(TAG, "Apertura de lector fallida");

        laserReader.BC_SetTriggerState(true);
    }

    public void initLaser(String value) {
        Intent intentLaser = new Intent(MY_PACKAGE + ".LASER_SCAN");
        intentLaser.addCategory(Intent.CATEGORY_DEFAULT);
        intentLaser.putExtra("Codigo", value);
        intentLaser.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intentLaser.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        //attachMoreExtras(intentLaser);
        if (value != null) {
            activity.setResult(Activity.RESULT_OK, intentLaser);
        }
        startActivityForResult(intentLaser, REQUEST_CODE); //Are you missing a call to unregisterReceiver()?
    }

    protected void startActivityForResult(Intent intent, int code) {
        if (fragment == null && supportFragment == null) {
            activity.startActivityForResult(intent, code);
            //TODO: unregisterReceiver()?
        } else if (supportFragment == null) {
            fragment.startActivityForResult(intent, code);
        } else if (fragment == null) {
            supportFragment.startActivityForResult(intent, code);
        }
    }

    public void attachMoreExtras(Intent intent) {
        for (Map.Entry<String, Object> entry : moreExtras.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            // Kind of hacky
            if (value instanceof Integer) {
                intent.putExtra(key, (Integer) value);
            } else if (value instanceof Long) {
                intent.putExtra(key, (Long) value);
            } else if (value instanceof Boolean) {
                intent.putExtra(key, (Boolean) value);
            } else if (value instanceof Double) {
                intent.putExtra(key, (Double) value);
            } else if (value instanceof Float) {
                intent.putExtra(key, (Float) value);
            } else if (value instanceof Bundle) {
                intent.putExtra(key, (Bundle) value);
            } else {
                intent.putExtra(key, value.toString());
            }
        }
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

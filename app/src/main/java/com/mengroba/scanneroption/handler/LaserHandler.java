package com.mengroba.scanneroption.handler;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

import com.mengroba.scanneroption.utils.UtilsKeys;

import co.kr.bluebird.ser.protocol.Reader;
import co.kr.bluebird.ser.protocol.SDConsts;

/**
 * Created by miguelef on 02/03/2017.
 */

public class LaserHandler {

    private static final String TAG = "LaserHandler";
    private final Context context;
    private final WebView webView;
    private Reader laserReader;

    public LaserHandler(Context context, WebView webView) {
        this.context = context;
        this.webView = webView;
    }

    public void startHandler() {

        Handler laserHandler = new Handler() {
            public void handleMessage(Message m) {
                Log.d(TAG, "laserHandler");
                Log.d(TAG, "command = " + m.arg1 + " result = " + m.arg2 + " obj = data");

                switch (m.what) {
                    case SDConsts.Msg.SDMsg:
                        if (m.arg1 == SDConsts.SDCmdMsg.SLED_WAKEUP) {
                            if (m.arg2 == SDConsts.SDResult.SUCCESS) {
                                Log.d(TAG, "SLED conectado");
                                laserReader.SD_Connect();
                            } else
                                Log.d(TAG, "Fallo en SLED");
                            laserReader.SD_Disconnect();
                        } else if (m.arg1 == SDConsts.SDCmdMsg.SLED_UNKNOWN_DISCONNECTED) {
                            Log.d(TAG, "SLED desconectado");
                            laserReader.SD_Disconnect();
                        }
                        break;
                    case SDConsts.Msg.BCMsg:
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
                                Log.i(TAG, "onActivityResult: " + code);
                                if (code != null) {
                                    Log.d(TAG, "startLaserScan(): valor no nulo");
                                    UtilsKeys.clearKeys(webView);
                                    UtilsKeys.loadKeys(webView, code);
                                } else {
                                    Log.d(TAG, "laserCodeKey() no hay codigo laser");
                                    Toast.makeText(context, "No se ha detectado ningún valor", Toast.LENGTH_SHORT).show();
                                }
                                //Pasamos la informacion al usuario, para ello usamos un dialogo emergente
                                //webInterface.showDialog("Codigo capturado: " + code);
                            }
                        }
                        break;
                }
            }
        };

        laserReader = Reader.getReader(context, laserHandler);
        boolean openResult = false;
        openResult = laserReader.RF_Open();
        if (openResult == SDConsts.RF_OPEN_SUCCESS) {
            Log.i(TAG, "Reader opened");
            laserReader = Reader.getReader(context, laserHandler);
            laserReader.SD_Wakeup();
        } else if (openResult == SDConsts.RF_OPEN_FAIL)
            Log.e(TAG, "Reader open failed");
    }

    public void stopHandler(){

        Handler laserHandler = new Handler() {
            public void handleMessage(Message m) {
                Log.d(TAG, "laserHandler");
                Log.d(TAG, "command = " + m.arg1 + " result = " + m.arg2 + " obj = data");

                switch (m.what) {
                    case SDConsts.Msg.SDMsg:
                        if (m.arg1 == SDConsts.SDCmdMsg.SLED_WAKEUP) {
                            if (m.arg2 == SDConsts.SDResult.SUCCESS) {
                                Log.d(TAG, "SLED conectado");
                                laserReader.SD_Connect();
                            } else
                                Log.d(TAG, "Fallo en SLED");
                            laserReader.SD_Disconnect();
                        } else if (m.arg1 == SDConsts.SDCmdMsg.SLED_UNKNOWN_DISCONNECTED) {
                            Log.d(TAG, "SLED desconectado");
                            laserReader.SD_Disconnect();
                        }
                        break;
                    case SDConsts.Msg.BCMsg:
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
                                Log.i(TAG, "onActivityResult: " + code);
                                if (code != null) {
                                    Log.d(TAG, "startLaserScan(): valor no nulo");
                                    UtilsKeys.clearKeys(webView);
                                    UtilsKeys.loadKeys(webView, code);
                                } else {
                                    Log.d(TAG, "laserCodeKey() no hay codigo laser");
                                    Toast.makeText(context, "No se ha detectado ningún valor", Toast.LENGTH_SHORT).show();
                                }
                                //Pasamos la informacion al usuario, para ello usamos un dialogo emergente
                                //webInterface.showDialog("Codigo capturado: " + code);
                            }
                        }
                        break;
                }
            }
        };
        laserReader = Reader.getReader(context, laserHandler);
        if (laserReader.SD_GetChargeState() == SDConsts.SDConnectState.CONNECTED) {
            laserReader.SD_Disconnect();
        }
        laserReader.RF_Close();

    }

}

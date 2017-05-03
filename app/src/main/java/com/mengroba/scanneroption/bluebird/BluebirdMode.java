package com.mengroba.scanneroption.bluebird;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

import com.mengroba.scanneroption.R;
import com.mengroba.scanneroption.ScanOptionActivity;
import com.mengroba.scanneroption.javascript.WebAppInterface;
import com.mengroba.scanneroption.utils.UtilsKeys;
import com.mengroba.scanneroption.utils.UtilsTools;
import com.mengroba.scanneroption.epc.Epc;
import com.mengroba.scanneroption.epc.GarmentEpc;

import co.kr.bluebird.ser.protocol.Reader;
import co.kr.bluebird.ser.protocol.SDConsts;

import static com.mengroba.scanneroption.javascript.JSConstants.JS_FUNCTION;
import static com.mengroba.scanneroption.javascript.JSConstants.JS_JAVASCRIPT;
import static com.mengroba.scanneroption.javascript.JSConstants.JS_START_CAMSCAN_IF_EMPTY;

/**
 * Created by miguelef on 12/04/2017.
 */

public class BluebirdMode {


    private static final String TAG = "BluebirdMode";
    private static final int BEEP_OK = 1;
    private static final int BEEP_ERROR = 2;
    private static final String SCAN_MODE_EPC = "scanEpc";
    private static final String SCAN_MODE_GARMENT = "scanGarmentRfid";
    private static final String SCAN_MODE_BARCODE = "scanBarcode";
    private static final String SCAN_MODE_MANUAL = "manual";
    public static final int RFR_OFF = 0;
    public static final int RFR_ON = 1;
    public static final int RFID_MODE = 0;
    public static final int BARCODE_MODE = 1;

    private Context context;
    private ScanOptionActivity activity;
    private WebView webView;
    private WebAppInterface webInterface;
    private String elementScanClass;
    private UtilsTools utils;
    private Reader reader;
    private int arg1;
    private int arg2;
    private int res;
    private Epc epc;
    private static boolean RFRState;

    public BluebirdMode(ScanOptionActivity activity) {
        this.activity = activity;
        init();
    }

    public Reader getReader() {
        return reader;
    }

    public void init() {
        this.webView = activity.getWebView();
        this.webInterface = activity.getWebAppInterface();
        try {
            this.reader = Reader.getReader(activity, connectivityReaderHandler);

            boolean openResult = this.reader.RF_Open();
            if (openResult == SDConsts.RF_OPEN_SUCCESS) {
                Log.i(TAG, "Reader opened");
            }

            this.reader.SD_SetTriggerMode(SDConsts.SDTriggerMode.RFID);

            int ret = this.reader.SD_Wakeup();

            if (ret == SDConsts.SDResult.SUCCESS) {
                Log.i(TAG, "Reader ok");
            }
        } catch (Exception e) {
            Log.e(TAG, "init().Error: " + e.getMessage());
            e.printStackTrace();
        } catch (Throwable t) {
            Log.e(TAG, "init().Throw: " + t.getMessage());
            t.printStackTrace();
        }
    }

    public void disconnect() {
        reader.SD_Disconnect();
    }


    public void scanBarcode() {

    }


    public void startRfid() {

    }

    public void readerMode() {
        if (reader != null) {
            if (activity.getTextMainButton().equals(activity.getString(R.string.sled_on)) &&
                    reader.SD_GetConnectState() == -32) {
                activity.setMainButton(activity.getString(R.string.rfr_off), Color.RED);
                reader.SD_Disconnect();
                RFRState = false;
                Log.d(TAG, "Button -32: " + reader.SD_GetConnectState());
            } else if (activity.getTextMainButton().equals(activity.getString(R.string.sled_on)) ||
                    activity.getTextMainButton().equals(activity.getString(R.string.rfr_on)) &&
                            reader.SD_GetConnectState() == 1) {
                activity.setMainButton(activity.getString(R.string.sled_off), Color.RED);
                reader.SD_Disconnect();
                RFRState = false;
                Log.d(TAG, "readerMode.SD_Disconnect");
            } else {
                activity.setMainButton(activity.getString(R.string.sled_on), Color.GREEN);
                res = reader.SD_Wakeup();
                RFRState = true;
                Log.d(TAG, "readerMode.SD_Wakeup: " + res);
            }
        } else {
            Toast.makeText(activity, activity.getString(R.string.error_init_scan), Toast.LENGTH_SHORT).show();
            activity.setMainButton(activity.getString(R.string.rfr_off), Color.RED);
        }
    }


    public void setModeScan(final String elementScanClass) {
        utils = new UtilsTools(activity);
        if (elementScanClass != null) {
            this.elementScanClass = elementScanClass;
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (getReader().SD_GetConnectState() == 1) {
                        if (elementScanClass.contains(SCAN_MODE_EPC)) {
                            utils.delayKeyboard(activity);
                            getReader().SD_SetTriggerMode(RFID_MODE);
                            activity.setMainButton(activity.getString(R.string.rfid_epc), Color.GREEN);
                            /*UtilsKeys.clearKeys(webView);
                            UtilsKeys.loadKeys(webView, "CEXP1064");*/
                        } else if (elementScanClass.contains(SCAN_MODE_GARMENT)) {
                            utils.delayKeyboard(activity);
                            getReader().SD_SetTriggerMode(RFID_MODE);
                            activity.setMainButton(activity.getString(R.string.rfid_garment), Color.GREEN);
                            /*UtilsKeys.clearKeys(webView);
                            UtilsKeys.loadKeys(webView, "30100010000641012064");*/
                        } else if (elementScanClass.contains(SCAN_MODE_BARCODE)) {
                            utils.delayKeyboard(activity);
                            getReader().SD_SetTriggerMode(BARCODE_MODE);
                            activity.setMainButton(activity.getString(R.string.barcode), Color.GREEN);
                        } else if (elementScanClass.contains(SCAN_MODE_MANUAL)) {
                            utils.delayKeyboard(activity);
                            utils.showKeyboard(activity);
                            getReader().SD_SetTriggerMode(BARCODE_MODE);
                            activity.setMainButton(activity.getString(R.string.manual), Color.GREEN);
                        } else {
                            utils.delayKeyboard(activity);
                            getReader().SD_SetTriggerMode(BARCODE_MODE);
                        }
                    } else {
                        if (elementScanClass.contains(SCAN_MODE_BARCODE)) {
                            utils.delayKeyboard(activity);
                            getReader().SD_SetTriggerMode(BARCODE_MODE);
                            webView.loadUrl(JS_JAVASCRIPT + JS_FUNCTION + JS_START_CAMSCAN_IF_EMPTY);
                            activity.setMainButton(activity.getString(R.string.barcode), Color.GREEN);
                        }else if (elementScanClass.contains(SCAN_MODE_EPC)) {
                            utils.delayKeyboard(activity);
                            getReader().SD_SetTriggerMode(BARCODE_MODE);
                            activity.setMainButton(activity.getString(R.string.rfid_epc), Color.GREEN);
                            /*UtilsKeys.clearKeys(webView);
                            UtilsKeys.loadKeys(webView, "CEXP1064");*/
                        } else if (elementScanClass.contains(SCAN_MODE_GARMENT)) {
                            utils.delayKeyboard(activity);
                            getReader().SD_SetTriggerMode(BARCODE_MODE);
                            activity.setMainButton(activity.getString(R.string.rfid_garment), Color.GREEN);
                        } else if (elementScanClass.contains(SCAN_MODE_MANUAL)) {
                            utils.showKeyboard(activity);
                            activity.setMainButton(activity.getString(R.string.manual), Color.GREEN);
                        } else {
                            utils.delayKeyboard(activity);
                            getReader().SD_SetTriggerMode(BARCODE_MODE);
                        }
                    }
                }
            });
        }
    }

    public Handler connectivityReaderHandler = new Handler() {
        public void handleMessage(Message m) {
            Log.d(TAG, "laserHandler");
            Log.d(TAG, "arg1 = " + m.arg1 + ", arg2 = " + m.arg2 + ", what = " + m.what);
            Log.d(TAG, "SDConnectState(): " + reader.SD_GetConnectState());
            arg1 = m.arg1;
            arg2 = m.arg2;
            //Se comprueba si esta conectado el RFR (pistola)
            if (reader.SD_GetConnectState() != -32) {
                activity.setMainButton(activity.getString(R.string.rfr_on), Color.GREEN);
            }

            switch (m.arg1) {
                //SLED Mensajes
                case SDConsts.SDCmdMsg.SLED_WAKEUP:
                    if (reader.SD_GetConnectState() != -32) {
                        reader.SD_Connect();
                        Log.d(TAG, "SLED conectado");
                        activity.setMainButton(activity.getString(R.string.sled_on), Color.GREEN);
                        break;
                    } else {
                        Log.d(TAG, "SLED desconectado");
                        activity.setMainButton(activity.getString(R.string.rfr_off), Color.RED);
                        Log.d(TAG, "WakeUp -32: " + reader.SD_GetConnectState());
                        reader.SD_Disconnect();
                        break;
                    }
                case SDConsts.SDCmdMsg.SLED_MODE_CHANGED:
                    if (m.arg2 == 0) {
                        activity.setMainButton(activity.getString(R.string.rfid_on), Color.GREEN);
                    } else if (m.arg2 == 1) {
                        activity.setMainButton(activity.getString(R.string.barcode_on), Color.GREEN);
                    }
                    break;
                case SDConsts.BCCmdMsg.BARCODE_TRIGGER_PRESSED:
                    if (reader.SD_GetConnectState() == -32) {
                        activity.setMainButton(activity.getString(R.string.rfr_off), Color.RED);
                        Log.d(TAG, "triggerPress -32: " + reader.SD_GetConnectState());
                    } else if (reader.SD_GetConnectState() == 0) {
                        activity.setMainButton(activity.getString(R.string.sled_off), Color.RED);
                    } else {
                        activity.setMainButton(activity.getString(R.string.laser_active), Color.GREEN);
                        Log.d(TAG, "lecturaLaser(): Laser active");
                    }
                    break;
                case SDConsts.BCCmdMsg.BARCODE_TRIGGER_RELEASED:
                    switch (reader.SD_GetConnectState()) {
                        case 0:
                            activity.setMainButton(activity.getString(R.string.rfr_on), Color.GREEN);
                            reader.SD_Connect();
                            break;
                        case 1:
                            activity.setMainButton(activity.getString(R.string.sled_on), Color.GREEN);
                            break;
                        case -32:
                            activity.setMainButton(activity.getString(R.string.sled_off), Color.RED);
                            Log.d(TAG, "triggerRelease -32: " + reader.SD_GetConnectState());
                            break;
                        default:
                            break;
                    }
                    break;
                case SDConsts.BCCmdMsg.BARCODE_READ:
                    if (m.arg2 == SDConsts.BCResult.SUCCESS) {
                        activity.setMainButton(activity.getString(R.string.success), Color.GREEN);
                        Log.d(TAG, "lecturaLaser(): Laser leyendo");
                        if (m.obj != null) {
                            StringBuilder readData = new StringBuilder();
                            activity.setMainButton(activity.getString(R.string.code_ok), Color.GREEN);
                            Log.d(TAG, "lecturaLaser(): valor no nulo");
                            String resultFull = readData.append((String) m.obj).toString();
                            String code = resultFull.substring(0, resultFull.indexOf(";"));
                            if(!isRFRState()){
                                UtilsKeys.clearKeys(webView);
                            }else {
                                UtilsKeys.clearKeys(webView);
                                UtilsKeys.loadKeys(webView, code);
                            }
                            Log.d(TAG, "lecturaLaser(): Resultado: " + code);
                        } else {
                            Log.d(TAG, "lecturaLaser() no hay codigo laser");
                            webInterface.showDialog(activity.getString(R.string.no_value));
                        }
                    } else if (m.arg2 == SDConsts.BCResult.ACCESS_TIMEOUT) {
                        activity.setMainButton(activity.getString(R.string.timeout), Color.RED);
                        Log.d(TAG, "lecturaLaser(): tiempo expirado");
                    } else {
                        UtilsTools.beepTone(BEEP_ERROR);
                    }
                    break;
                case SDConsts.SDCmdMsg.TRIGGER_PRESSED:
                    reader.RF_SetRadioPowerState(20);
                    Log.d(TAG, "RFID capturando");
                    activity.setMainButton(activity.getString(R.string.rfid_trigger_on), Color.GREEN);
                    if (reader.SD_GetTriggerMode() == 0) {
                        //checkState();
                        reader.RF_READ(SDConsts.RFMemType.EPC, 2, 8, "00000000", false);
                    }
                    break;
                case SDConsts.SDCmdMsg.TRIGGER_RELEASED:
                    Log.d(TAG, "RFID capturando");
                    activity.setMainButton(activity.getString(R.string.sled_on), Color.GREEN);
                    break;
                case SDConsts.RFCmdMsg.READ:
                    if (m.arg2 == SDConsts.RFResult.SUCCESS) {
                        activity.setMainButton(activity.getString(R.string.reading_code), Color.GREEN);
                        Log.d(TAG, "lecturaRFID(): RFID leyendo");
                        String data = (String) m.obj;
                        if (data != null) {
                            UtilsTools.beepTone(BEEP_OK);
                            String[] epcData = data.split(";");
                            String epcHex = epcData[0];
                            epc = Epc.of(epcHex);

                            if (elementScanClass.contains(SCAN_MODE_EPC)) {
                                data = String.valueOf(epc);
                                Toast.makeText(activity, R.string.epc_hex, Toast.LENGTH_SHORT).show();
                            } else if (elementScanClass.contains(SCAN_MODE_GARMENT)) {
                                if (epc instanceof GarmentEpc) {
                                    data = ((GarmentEpc) epc).garmentCode().toString();
                                    Toast.makeText(activity, R.string.epc_garment, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(activity, R.string.epc_beacon, Toast.LENGTH_SHORT).show();
                                }
                            }
                            UtilsKeys.clearKeys(webView);
                            UtilsKeys.loadKeys(webView, data);
                            Log.d(TAG, "lecturaRFID(): Resultado: " + data);
                        } else {
                            UtilsTools.beepTone(BEEP_ERROR);
                        }
                    } else {
                        UtilsTools.beepTone(BEEP_ERROR);
                    }
                    break;
                case SDConsts.SDCmdMsg.SLED_UNKNOWN_DISCONNECTED:
                    Log.d(TAG, "SLED desconectado");
                    activity.setMainButton(activity.getString(R.string.sled_off), Color.RED);
                    break;
                case SDConsts.SDCmdMsg.SLED_BATTERY_STATE_CHANGED:
                    if (reader.SD_GetConnectState() != -32) {
                        activity.setMainButton(activity.getString(R.string.rfr_on), Color.GREEN);
                    } else {
                        activity.setMainButton(activity.getString(R.string.rfr_off), Color.RED);
                        Log.d(TAG, "batState -32: " + reader.SD_GetConnectState());
                    }
                    break;
                case SDConsts.SDBatteryState.LOW_BATTERY:
                    Log.d(TAG, "SLED Bateria baja");
                    activity.setMainButton(activity.getString(R.string.low_battery), Color.RED);
                    break;
                default:
                    activity.setMainButton(activity.getString(R.string.bluebird_on), Color.GREEN);
                    break;
            }
        }
    };

    public void checkState() {
        System.out.println("##@@--checkState-INI-> " + this.reader.RF_GetDutyCycle());
        while (this.reader.RF_GetDutyCycle() != 100) {
            try {
                Thread.sleep(70);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("##@@--checkState-FIN-> " + this.reader.RF_GetDutyCycle());
    }

    private void readingTest() {
        //reader.RF_SetToggle(SDConsts.RFToggle.OFF);
        //reader.RF_SetInventorySessionTarget(SDConsts.RFInvSessionTarget.TARGET_A);
        //reader.RF_SetInventorySessionTarget(SDConsts.RFInvSessionTarget.TARGET_B);
        //reader.RF_SetRFMode(SDConsts.RFMode.DSB_ASK_1);
        /*reader.RF_SetSession(SDConsts.RFSession.SESSION_S1);
        reader.RF_SetSingulationControl(SDConsts.RFSingulation.MAX_SINGULATION,
                SDConsts.RFSingulation.MIN_SINGULATION, SDConsts.RFSingulation.MAX_SINGULATION);
        reader.RF_PerformInventory(false, false, false);*/

        //readingOnceSecond();
        reader.RF_PerformInventory(true, false, false);
    }

    private void readingOnceSecond() {
        reader.RF_SetToggle(SDConsts.RFToggle.OFF);
        reader.RF_SetSession(SDConsts.RFSession.SESSION_S1);
    }

    private void readingOnceThreeSecond() {
        reader.RF_SetToggle(SDConsts.RFToggle.OFF);
        reader.RF_SetSingulationControl(SDConsts.RFSingulation.MAX_SINGULATION,
                SDConsts.RFSingulation.MIN_SINGULATION, SDConsts.RFSingulation.MAX_SINGULATION);
    }

    private void readingOnceMinute() {
        reader.RF_SetToggle(SDConsts.RFToggle.OFF);
        //paraece que con cualquier modo PR_ASK_1
        reader.RF_SetRFMode(SDConsts.RFMode.PR_ASK_1);
        reader.RF_SetSingulationControl(SDConsts.RFSingulation.MAX_SINGULATION,
                SDConsts.RFSingulation.MAX_SINGULATION, SDConsts.RFSingulation.MAX_SINGULATION);
    }

    public String getElementScanClass() {
        return elementScanClass;
    }

    public int getArg1() {
        return arg1;
    }

    public int getArg2() {
        return arg2;
    }

    public int getRes() {
        return res;
    }

    public Epc getEpc() {
        return epc;
    }

    public static boolean isRFRState() {
        return RFRState;
    }
}
package com.mengroba.scanneroption.utils;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Handler;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by miguelef on 23/02/2017.
 */

public class UtilsTools {

    private Context context;
    private static final int BEEP_OK = 1;
    private static final int BEEP_ERROR = 2;
    private InputMethodManager imm;

    public UtilsTools(Context context) {
        this.context = context;
    }

    public static void beepTone(int status) {
        ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);

        try {
            switch (status) {
                case BEEP_OK:
                    tg.startTone(ToneGenerator.TONE_PROP_ACK, 500);
                    Thread.sleep(500);
                    tg.release();
                    break;
                case BEEP_ERROR:
                    tg.startTone(ToneGenerator.TONE_PROP_NACK, 500);
                    Thread.sleep(500);
                    tg.release();
                    break;
                default:
                    tg.startTone(ToneGenerator.TONE_PROP_NACK, 500);
                    Thread.sleep(500);
                    tg.release();
                    break;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void toggleKey(final Activity activity) {
        imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0); // ocultar
        } else {
            imm.toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY); // mostrar
        }
    }//end method

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void showKeyboard(final Activity activity) {
        imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.RESULT_SHOWN, InputMethodManager.HIDE_IMPLICIT_ONLY); // mostrar
    }

    public static void delayKeyboard(final Activity activity) {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                UtilsTools.hideKeyboard(activity);
            }
        }, 30);
    }
}


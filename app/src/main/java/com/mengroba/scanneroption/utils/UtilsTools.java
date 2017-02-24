package com.mengroba.scanneroption.utils;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;

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

    public void beepTone(int status) {
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

    public void toggleKey(final Activity activity){
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (imm.isActive()){
            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0); // ocultar
        } else {
            imm.toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY); // mostrar
        }
    }//end method

    public void hideKeyboard(final Activity activity){
        final InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0); // ocultar
    }

    public void showKeyboard(final Activity activity){
        final InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY); // mostrar
    }

    public static float dpToPx(Context context, float valueInDp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics);
    }
}

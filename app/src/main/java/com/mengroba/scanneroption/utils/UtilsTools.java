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


    public void hideKeyboard(final Context context, final WebView webView){
        final Activity activity = (Activity) webView.getContext();
        imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        webView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = webView.getRootView().getHeight() - webView.getHeight();
                if (heightDiff > dpToPx(context, 200)) {
                    imm.hideSoftInputFromInputMethod(activity.getCurrentFocus().getWindowToken(), 0);
                }
            }
        });

    }

    public static float dpToPx(Context context, float valueInDp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics);
    }
}

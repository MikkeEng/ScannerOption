package com.mengroba.scanneroption.utils;

import android.view.KeyEvent;
import android.webkit.WebView;

/**
 * Created by miguelef on 16/02/2017.
 */

public enum UtilsKeys {
    A(KeyEvent.KEYCODE_A, 'A'),
    B(KeyEvent.KEYCODE_B, 'B'),
    C(KeyEvent.KEYCODE_C, 'C'),
    D(KeyEvent.KEYCODE_D, 'D'),
    E(KeyEvent.KEYCODE_E, 'E'),
    F(KeyEvent.KEYCODE_F, 'F'),
    G(KeyEvent.KEYCODE_G, 'G'),
    H(KeyEvent.KEYCODE_H, 'H'),
    I(KeyEvent.KEYCODE_I, 'I'),
    J(KeyEvent.KEYCODE_J, 'J'),
    K(KeyEvent.KEYCODE_K, 'K'),
    L(KeyEvent.KEYCODE_L, 'L'),
    M(KeyEvent.KEYCODE_M, 'M'),
    N(KeyEvent.KEYCODE_N, 'N'),
    O(KeyEvent.KEYCODE_O, 'O'),
    P(KeyEvent.KEYCODE_P, 'P'),
    Q(KeyEvent.KEYCODE_Q, 'Q'),
    R(KeyEvent.KEYCODE_R, 'R'),
    S(KeyEvent.KEYCODE_S, 'S'),
    T(KeyEvent.KEYCODE_T, 'T'),
    U(KeyEvent.KEYCODE_U, 'U'),
    V(KeyEvent.KEYCODE_V, 'V'),
    W(KeyEvent.KEYCODE_W, 'W'),
    X(KeyEvent.KEYCODE_X, 'X'),
    Y(KeyEvent.KEYCODE_Y, 'Y'),
    Z(KeyEvent.KEYCODE_Z, 'Z'),
    ZERO(KeyEvent.KEYCODE_0, '0'),
    ONE(KeyEvent.KEYCODE_1, '1'),
    TWO(KeyEvent.KEYCODE_2, '2'),
    THREE(KeyEvent.KEYCODE_3, '3'),
    FOUR(KeyEvent.KEYCODE_4, '4'),
    FIVE(KeyEvent.KEYCODE_5, '5'),
    SIX(KeyEvent.KEYCODE_6, '6'),
    SEVEN(KeyEvent.KEYCODE_7, '7'),
    EIGHT(KeyEvent.KEYCODE_8, '8'),
    NINE(KeyEvent.KEYCODE_9, '9'),
    MINUS(KeyEvent.KEYCODE_MINUS, '-'),
    BACKSLASH(KeyEvent.KEYCODE_BACKSLASH, '\\'),
    SLASH(KeyEvent.KEYCODE_SLASH, '/');

    private int event;
    private char character;
    private WebView webView;

    UtilsKeys(int event, char character) {
        this.event = event;
        this.character = character;
    }

    UtilsKeys(WebView webView) {
        this.webView = webView;
    }

    public static int getKeyEvent(char character) {
        for (UtilsKeys item : values()) {
            if (character == item.character) {
                return item.event;
            }
        }

        return -1;
    }

    public static void loadKeys(WebView webView, String msg) {
        for (char character : msg.toCharArray()) {
            webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, UtilsKeys.getKeyEvent(character)));
        }
        webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
    }

    public static void clearKeys(WebView webView) {
        webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MOVE_HOME));
        for (int i = 0; i < 50; i++) {
            webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_FORWARD_DEL));
        }
    }

    public static void introKey(WebView webView) {
        webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
    }
}


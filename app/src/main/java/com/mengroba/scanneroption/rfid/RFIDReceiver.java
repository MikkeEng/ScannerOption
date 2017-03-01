/*
 * Copyright (C) 2015 - 2016 Bluebird Inc, All rights reserved.
 * 
 * http://www.bluebird.co.kr/
 * 
 * Author : Bogon Jun
 *
 * Date : 2016.03.03
 */

package com.mengroba.scanneroption.rfid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class RFIDReceiver extends BroadcastReceiver {
    private static final String TAG = RFIDReceiver.class.getSimpleName();
    
    private static final String SLED_ATTACHED = "kr.co.bluebird.android.sled.action.SLED_ATTACHED";
    
    private static final String SLED_DETACHED = "kr.co.bluebird.android.sled.action.SLED_DETACHED";
    
    @Override
    public void onReceive(Context arg0, Intent arg1) {
        // TODO Auto-generated method stub
        String action = arg1.getAction();
        if (SLED_ATTACHED.equals(action)) {
            Toast.makeText(arg0, "SLED_ATTACHED", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "SLED_ATTACHED");
            //Intent intent = new Intent(arg0, MainActivity.class);
            //intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            //arg0.startActivity(intent);
        }
        else if (SLED_DETACHED.equals(action)) {
            Toast.makeText(arg0, "SLED_DETACHED", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "SLED_DETACHED");
        }
    }
}

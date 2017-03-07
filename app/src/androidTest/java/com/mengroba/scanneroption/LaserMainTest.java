package com.mengroba.scanneroption;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.test.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;

import co.kr.bluebird.ser.protocol.Reader;
import co.kr.bluebird.ser.protocol.SDConsts;

import static org.junit.Assert.*;

/**
 * Created by miguelef on 06/03/2017.
 */
public class LaserMainTest {


    private int arg1;
    private int arg2;
    private Reader laserReader;
    private Handler laserHandler;
    private Context appContext;
    private String stringRes;
    private int intRes;
    private Object obj;


    @Before
    public void initHandler(){
        appContext = InstrumentationRegistry.getTargetContext();

    }

    @Test
    public void useAppContext() throws Exception {
        assertEquals("com.mengroba.scanneroption", appContext.getPackageName());
    }

    @Test
    public void sledWakeUp(){
        assertEquals(47, SDConsts.SDCmdMsg.SLED_WAKEUP);
        assertEquals(arg2, 0);
    }

    @Test
    public void BarcodeModeON(){
        assertEquals(45, SDConsts.SDCmdMsg.SLED_MODE_CHANGED);
        assertEquals(arg2, 0);
    }

    @Test
    public void RFIDModeON(){
        assertEquals(45, SDConsts.SDCmdMsg.SLED_MODE_CHANGED);
        assertNotEquals(arg2, 1);
    }

    @Test
    public void BatteryLOW(){
        assertEquals(-12, SDConsts.SDBatteryState.LOW_BATTERY);
    }
}

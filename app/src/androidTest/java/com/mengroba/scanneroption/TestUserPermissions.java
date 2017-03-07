package com.mengroba.scanneroption;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;

/**
 * Created by miguelef on 06/03/2017.
 */

@RunWith(AndroidJUnit4.class)
public class TestUserPermissions {

    private Context appContext;
    private PackageManager pm;
    private int res;

    @Before
    public void initialize() {
        appContext=InstrumentationRegistry.getTargetContext();
        pm=appContext.getPackageManager();
    }


    @Test
    public void testPermission_INTERNET_true(){
        res = pm.checkPermission("android.permission.INTERNET", "com.mengroba.scanneroption");
        assertEquals(res, PackageManager.PERMISSION_GRANTED);
    }

    @Test
    public void testPermission_ACCESS_NETWORK_STATE_true(){
        res = pm.checkPermission("android.permission.ACCESS_NETWORK_STATE", "com.mengroba.scanneroption");
        assertEquals(res, PackageManager.PERMISSION_GRANTED);
    }

    @Test
    public void testPermission_CAMERA_true(){
        res = pm.checkPermission("android.permission.CAMERA", "com.mengroba.scanneroption");
        assertEquals(res, PackageManager.PERMISSION_GRANTED);
    }
}

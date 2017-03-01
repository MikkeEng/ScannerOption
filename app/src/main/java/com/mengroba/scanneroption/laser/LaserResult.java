package com.mengroba.scanneroption.laser;

/**
 * Created by miguelef on 27/02/2017.
 */

public class LaserResult {

    private String laserContents;

    public LaserResult() {
        this(null);
    }

    public LaserResult(String contents) {
        this.laserContents = contents;
    }

    public String getLaserContents() {
        return laserContents;
    }

    @Override
    public String toString() {
        String dialogText = "Contenidos: " + laserContents + '\n';
        return dialogText;
    }
}

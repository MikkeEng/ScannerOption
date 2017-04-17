package com.mengroba.scanneroption;

/**
 * Created by miguelef on 12/04/2017.
 */

public interface ScanOptionInterface {

    void connect();

    void disconnect();

    void setBarcodeMode();

    void setRfidMode();

    void setModeScan(String elementScanClass);

    void readerMode();

}

package com.mengroba.scanneroption.javascript;

/**
 * Created by miguelef on 31/03/2017.
 */

public class JSConstants {

    //Codigo JS//
    public static final String JS_JAVASCRIPT = "javascript:(";
    public static final String JS_FUNCTION = "function() {";
    public static final String JS_LOAD_PAGE =
            "console.log('JS_LOAD_PAGE');" +
                    "var listElementScan = document.querySelectorAll(\"input[class*='scan']\");" +
                    "for(var i = 0; i < listElementScan.length; i++) {" +
                    "var elementScan = listElementScan[i];" +
                    "elementScan.autocomplete = 'off';" +
                    "}" +
                    "})()";
    public static final String JS_JAVASCRIPT_LISTENER =
            "document.addEventListener('focus'," +
                    "function(event){" +
                    "var classElement = event.target.className;" +
                    "console.log('JS_JAVASCRIPT_LISTENER.clase  de elemento: ' + classElement);" +
                    "Android.setScanMode(classElement);" +
                    "}" +
                    ",true))()";
    public static final String JS_TEXT_SPEECH =
            "console.log('JS_TEXT_SPEECH');" +
                    "var listElementMsg = document.querySelectorAll(\"p[class*='message']\");" +
                    "for(var i = 0; i < listElementMsg.length; i++) {" +
                    "var elementVoice = listElementMsg[i];" +
                    "if(elementVoice != null){" +
                    "console.log('JS_TEXT_SPEECH.element class: ' + elementVoice.className);" +
                    "console.log('JS_TEXT_SPEECH.msg: ' + elementVoice.innerHTML);" +
                    "Android.findMsg(elementVoice.className, elementVoice.innerHTML);" +
                    "}" +
                    "}" +
                    "})()";
    public static final String JS_START_CAMSCAN_IF_EMPTY =
            "var listElementScanner = document.querySelectorAll('.scanBarcode');" +
                    "var actElement = document.activeElement;" +
                    "console.log('JS_START_CAMSCAN_IF_EMPTY.actElement: ' + actElement);" +
                    "for(var i = 0; i < listElementScanner.length; i++) {" +
                    "var elementScanner = listElementScanner[i];" +
                    "console.log('JS_START_CAMSCAN_IF_EMPTY.elementScanner: ' + elementScanner);" +
                    "var elementValue = elementScanner.value;" +
                    "console.log('JS_START_CAMSCAN_IF_EMPTY.elementValue: ' + elementValue);" +
                    "if(elementScanner == actElement && !elementValue){" +
                    "Android.startScan();" +
                    "}" +
                    "}" +
                    "})()";
    public static final String JS_SCAN_CLASS =
            "console.log('JS_SCAN_CLASS');" +
                    "console.log('elementScanner: ' + elementScanner);" +
                    "if(elementScanner == actElement){" +
                    "elementScanClass = actElement.className;" +
                    "console.log('JS_SCAN_CLASS.clase de elemento: ' + elementScanClass);" +
                    "Android.setScanMode(elementScanClass);" +
                    "}";
    public static final String JS_SCAN_MODE =
            "console.log('JS_SCAN_MODE');" +
                    "var elementScanClass = '';" +
                    "var elementScanner = '';" +
                    "var actElement = document.activeElement;" +
                    "console.log('elementScanner actElement: ' + actElement);" +
                    "var listElementScanBarcode = document.querySelectorAll('.scanBarcode');" +
                    "console.log('setBarcodeMode: ' + listElementScanBarcode.length);" +
                    "for(var i = 0; i < listElementScanBarcode.length; i++) {" +
                    "elementScanner = listElementScanBarcode[i];" +
                    JS_SCAN_CLASS +
                    "}" +
                    "var listElementScanEpc = document.querySelectorAll('.scanEpc');" +
                    "console.log('scanEpc: ' + listElementScanEpc.length);" +
                    "for(var i = 0; i < listElementScanEpc.length; i++) {" +
                    "elementScanner = listElementScanEpc[i];" +
                    JS_SCAN_CLASS +
                    "}" +
                    "var listElementScanGarment = document.querySelectorAll('.scanGarmentRfid');" +
                    "console.log('scanGarmentRfid: ' + listElementScanGarment.length);" +
                    "for(var i = 0; i < listElementScanGarment.length; i++) {" +
                    "elementScanner = listElementScanGarment[i];" +
                    JS_SCAN_CLASS +
                    "}" +
                    "var listElementTextManual = document.querySelectorAll('.manual');" +
                    "console.log('manual: ' + listElementTextManual.length);" +
                    "for(var i = 0; i < listElementTextManual.length; i++) {" +
                    "elementManual = listElementTextManual[i];" +
                    "console.log('JS_SCAN_CLASS');" +
                    "console.log('elementScanner: ' + elementManual);" +
                    "elementManualClass = elementManual.className;" +
                    "console.log('JS_SCAN_CLASS.claseManual: ' + elementManualClass);" +
                    "}" +
                    "Android.setScanMode(elementManualClass);" +
                    "})()";
}

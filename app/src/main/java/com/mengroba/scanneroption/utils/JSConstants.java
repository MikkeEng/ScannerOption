package com.mengroba.scanneroption.utils;

/**
 * Created by miguelef on 31/03/2017.
 */

public class JSConstants {

    //Codigo JS//
    public static final String JS_JAVASCRIPT = "javascript:(";
    public static final String JS_FUNCTION = "function() {";
    public static final String JS_JAVASCRIPT_LISTENER = "document.addEventListener('focus'," +
            "function(event){" +
            "var classElement = event.target.className;" +
            "console.log('JS_JAVASCRIPT_LISTENER.clase  de element: ' + classElement);" +
            "document.getElementById('id_text').innerHTML = classElement;" +
            "Android.setScanMode(classElement);" +
            "}" +
            ",true))()";
    public static final String JS_TEXT_SPEECH =
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
            "var listElementScanner = document.querySelectorAll('.scanCam');" +
                    "var actElement = document.activeElement;" +
                    "for(var i = 0; i < listElementScanner.length; i++) {" +
                    "var elementScanner = listElementScanner[i];" +
                    "var elementValue = elementScanner.value;" +
                    "if(elementScanner == actElement && !elementValue){" +
                    "Android.startScan();" +
                    "}" +
                    "}" +
                    "})()";
    public static final String JS_SCANMODE =
            "console.log('elementScanner scanBarcode: ' + elementScanner);" +
                    "if(elementScanner == actElement){" +
                    "elementScanClass = actElement.className;" +
                    "console.log('clase de scanBarcode: ' + elementScanClass);" +
                    "Android.setScanMode(elementScanClass);" +
                    "}";
    public static final String JS_SCAN_CLASS =
            "var elementScanClass = '';" +
                    "var elementScanner = '';" +
                    "var actElement = document.activeElement;" +
                    "console.log('elementScanner actElement: ' + actElement);" +
                    "var listElementScanBarcode = document.querySelectorAll('.scanBarcode');" +
                    "console.log('scanBarcode' + listElementScanBarcode);" +
                    "var listElementScanEpc = document.querySelectorAll('.scanEpc');" +
                    "console.log('scanEpc' + listElementScanEpc);" +
                    "var listElementScanGarment = document.querySelectorAll('.scanGarmentRfid');" +
                    "console.log('scanGarmentRfid' + listElementScanGarment);" +
                    "var listElementTextManual = document.querySelectorAll('.manual');" +
                    "console.log('manual' + listElementTextManual);" +

                    "for(var i = 0; i < listElementScanBarcode.length; i++) {" +
                    "elementScanner = listElementScanBarcode[i];" +
                    JS_SCANMODE +
                    "}" +
                    "for(var i = 0; i < listElementScanEpc.length; i++) {" +
                    "elementScanner = listElementScanEpc[i];" +
                    JS_SCANMODE +
                    "}" +
                    "for(var i = 0; i < listElementScanGarment.length; i++) {" +
                    "elementScanner = listElementScanGarment[i];" +
                    JS_SCANMODE +
                    "}" +
                    "for(var i = 0; i < listElementTextManual.length; i++) {" +
                    "elementScanner = listElementTextManual[i];" +
                    JS_SCANMODE +
                    "}" +
                    "})()";
}

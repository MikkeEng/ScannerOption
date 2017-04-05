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
                "var listElementMsg = document.querySelectorAll('.message');" +
                        "for(var i = 0; i < listElementMsg.length; i++) {" +
                            "var elementVoice = listElementMsg[i];" +
                            "if(elementVoice != null){" +
                                "console.log('JS_TEXT_SPEECH.element class: ' + elementVoice.className);" +
                                "console.log('JS_TEXT_SPEECH.msg: ' + elementVoice.innerHTML);" +
                                "Android.findMsg(elementVoice.className, elementVoice.innerHTML);" +
                            "}" +
                        "}" +
                    "})()";
    public static final String JS_ADD_MANUAL_CLASS = "document.addEventListener('focus'," +
            "function(event){" +
            "var focusElement = event.target;" +
            "focusElement.className += 'manual';" +
            "}" +
            ",true))()";
    public static final String JS_REMOVE_MANUAL_CLASS = "document.addEventListener('focus'," +
            "function(event){" +
            "var focusElement = event.target;" +
            "focusElement.className -= 'manual';" +
            "}" +
            ",true))()";
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
}

package com.mengroba.scanneroption.utils;

import android.content.Context;
import android.util.Log;

import com.mengroba.scanneroption.R;
import com.mengroba.scanneroption.utils.epc.BeaconEpc;
import com.mengroba.scanneroption.utils.epc.Epc;
import com.mengroba.scanneroption.utils.epc.GarmentEpc;
import com.mengroba.scanneroption.utils.epc.TagInfo;

/**
 * Created by miguelef on 14/03/2017.
 */

public class ReaderSkuTag {

    public static final String TAG = "ReaderSkuTag";

    private Context context;
    private Epc epcIdCurrentTmp;
    private boolean isPressed = false;

    public ReaderSkuTag(Context context) {
        this.context = context;
    }

    public String updateInfo(final TagInfo tagInfo, final boolean isPressed) {
        //this.tagInfo = tagInfo;
        Epc epc = tagInfo.getEpc();

        final String description = epc instanceof GarmentEpc ? ((GarmentEpc) epc).garmentCode().toString()
                : getBeaconExtendedInfo(context, (BeaconEpc) epc);

        return description;
    }

    public final static String getBeaconExtendedInfo(Context context, BeaconEpc epc){
        String result = context.getString(R.string.beaconBlock)+epc.block() + "-" +
                context.getString(R.string.beaconSide) +(epc.right() ? "2" : "1") + "-" +
                context.getString(R.string.beaconNumber)+epc.number();

        if (epc.extreme()){
            result = result + "-" + context.getString(R.string.beaconExtreme);
        }

        if (epc.limit()){
            result = result + "-" + context.getString(R.string.beaconLimit);
        }
        return result;
    }

    public String tagReaded(final TagInfo tagInfoParams) {
        Log.d(TAG, "##@@@@---tagInfo -> " + tagInfoParams);

        TagInfo tagInfo = tagInfoParams != null ? tagInfoParams : null;

        if((this.epcIdCurrentTmp != null && tagInfo != null)
                && !this.epcIdCurrentTmp.equals(tagInfo.getEpc())) {
            Log.d(TAG, "####---tagReaded--serial-> " + tagInfoParams.getEpc().serial() +
                    " -EPC-> " + tagInfoParams.getEpc());
        }

        return this.validateTags(tagInfo);
    }

    private String validateTags(final TagInfo tagInfo) {
        epcIdCurrentTmp = tagInfo.getEpc();

       Log.d(TAG, "####---validateTags--serial-> " + tagInfo.getEpc().serial() + " -EPC-> " +
                tagInfo.getEpc());

        return updateInfo(tagInfo, this.isPressed);
    }
}

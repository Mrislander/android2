package com.elitise.firmwareupdate;

import android.util.Log;

/**
 * Created by Andy.Xiang on 2/9/2017.
 */
public class GloableVar {

    private String mSN;
    private String ELT_BATT_MOBBAT_SERVICE_UUID_WITH_SN = "AC55D02D-0XXX-4F64-ZZZZ-ABCB5C6D409D";
    private String ELT_BATT_MOBBAT_CHARACTERISTIC_UUID_WITH_SN = "A88C5B46-0XXX-473A-ZZZZ-49B98173948D";

    private static GloableVar ourInstance = new GloableVar();

    public static GloableVar getInstance() {
        return ourInstance;
    }

    private GloableVar() {
    }

    public String getmSN() {
        return mSN;
    }

    public void setmSN(String mSN) {
        this.mSN = mSN;
    }

    public String getELT_BATT_MOBBAT_SERVICE_UUID_WITH_SN() {
        return ELT_BATT_MOBBAT_SERVICE_UUID_WITH_SN;
    }

    public void setELT_BATT_MOBBAT_SERVICE_UUID_WITH_SN(String ELT_BATT_MOBBAT_SERVICE_UUID_WITH_SN) {
        this.ELT_BATT_MOBBAT_SERVICE_UUID_WITH_SN = ELT_BATT_MOBBAT_SERVICE_UUID_WITH_SN;
    }

    public String getELT_BATT_MOBBAT_CHARACTERISTIC_UUID_WITH_SN() {
        return ELT_BATT_MOBBAT_CHARACTERISTIC_UUID_WITH_SN;
    }

    public void setELT_BATT_MOBBAT_CHARACTERISTIC_UUID_WITH_SN(String ELT_BATT_MOBBAT_CHARACTERISTIC_UUID_WITH_SN) {
        this.ELT_BATT_MOBBAT_CHARACTERISTIC_UUID_WITH_SN = ELT_BATT_MOBBAT_CHARACTERISTIC_UUID_WITH_SN;
    }

    public  void CustomUUID(){
        String first = "000";
        String last = "0000";
        if(mSN.length() == 7) {
            first = mSN.substring(0, 3);
            last = mSN.substring(3, 7);
        }else if(mSN.length() == 8){
            first = mSN.substring(1, 4);
            last = mSN.substring(4, 8);
        }
        ELT_BATT_MOBBAT_CHARACTERISTIC_UUID_WITH_SN = ELT_BATT_MOBBAT_CHARACTERISTIC_UUID_WITH_SN.substring(0,10)+
                first+ELT_BATT_MOBBAT_CHARACTERISTIC_UUID_WITH_SN.substring(13,19)+
                last+ELT_BATT_MOBBAT_CHARACTERISTIC_UUID_WITH_SN.substring(23);
        ELT_BATT_MOBBAT_SERVICE_UUID_WITH_SN = ELT_BATT_MOBBAT_SERVICE_UUID_WITH_SN.substring(0,10)+
                first+ELT_BATT_MOBBAT_SERVICE_UUID_WITH_SN.substring(13,19)+
                last+ELT_BATT_MOBBAT_SERVICE_UUID_WITH_SN.substring(23);
        Log.v("SYS","UUID: "+ELT_BATT_MOBBAT_CHARACTERISTIC_UUID_WITH_SN );
    }
}

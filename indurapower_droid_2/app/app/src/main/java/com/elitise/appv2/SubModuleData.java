package com.elitise.appv2;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.ParcelUuid;

/**
 * Created by Andy.Xiang on 9/15/2016.
 */
public class SubModuleData extends BaseObservable {
    private int moduleNum;
    private float bankV;
    private float C_MON_AMP;
    private float cellTemp;
    private float AmpHour;
    private String tempLable;

    public SubModuleData(boolean isCelsius){
        moduleNum = 0;
        bankV = 0.0f;
        C_MON_AMP = 0.0f;
        cellTemp = 0.0f;
        AmpHour = 0.0f;
        if(isCelsius){
            tempLable = "Temperature (°C)";
        }
        else {
            tempLable = "Temperature (°F)";
        }
    }

    public void resetData(boolean isCelsius){
        bankV = 0.0f;
        C_MON_AMP = 0.0f;
        cellTemp = 0.0f;
        AmpHour = 0.0f;
        if(isCelsius){
            tempLable = "Temperature (°C)";
        }
        else {
            tempLable = "Temperature (°F)";
        }
        notifyPropertyChanged(com.elitise.appv2.BR._all);
    }
    @Bindable
    public String  getTempLable() {
        return tempLable;
    }

    public void setTempLable(boolean isCelsius) {
        if(isCelsius){
            tempLable = "Temperature (°C)";
        }
        else {
            tempLable = "Temperature (°F)";
        }
        notifyPropertyChanged(com.elitise.appv2.BR.tempLable);
    }

    @Bindable
    public int getModuleNum() {
        return moduleNum;
    }

    public void setModuleNum(int moduleNum) {
        this.moduleNum = moduleNum;
        notifyPropertyChanged(com.elitise.appv2.BR.moduleNum);
    }

    @Bindable
    public float getBankV() {
        return bankV;
    }

    public void setBankV(float bankV) {
        this.bankV = bankV;
        notifyPropertyChanged(com.elitise.appv2.BR.bankV);
    }
    @Bindable
    public float getC_MON_AMP() {
        return C_MON_AMP;
    }

    public void setC_MON_AMP(float c_MON_AMP) {
        C_MON_AMP = c_MON_AMP;
        notifyPropertyChanged(com.elitise.appv2.BR.c_MON_AMP);
    }
    @Bindable
    public float getCellTemp() {
        return cellTemp;
    }

    public void setCellTemp(float cellTemp) {
        this.cellTemp = cellTemp;
        notifyPropertyChanged(com.elitise.appv2.BR.cellTemp);
    }
    @Bindable
    public float getAmpHour() {
        return AmpHour;
    }

    public void setAmpHour(float ampHour) {
        this.AmpHour = ampHour;
        notifyPropertyChanged(com.elitise.appv2.BR.ampHour);
    }
}

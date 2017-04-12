package com.elitise.appv2;

import com.google.common.base.Strings;
import com.google.common.collect.Table;
import com.google.common.math.IntMath;

import java.security.PublicKey;
import java.util.Arrays;

/**
 * Created by andy on 3/30/16.
 */
public class BatteryData {

    private static BatteryData ourInstance = new BatteryData();

    public static BatteryData getInstance() {
        if(ourInstance == null) {
            ourInstance = new BatteryData();
        }
        return ourInstance;
    }

    private float m_I_CHGR;
    private float m_C_MON;
    private float m_BUS_MON;
    private float m_batteryVoltage;
    private float m_bodyTemperature;
    private float m_cellTemperature;
    private float m_bodyTemperatureC;
    private float m_cellTemperatureC;
    private float m_C_MON_amps;


    private float[] m_FUEL_CELL_V= new float[4];
    private float m_SOH;
    private float m_SOC;


    public class receivedBytes {
        public byte[] bytesArray;
        public byte expectPkts;
        public boolean Done;
        receivedBytes(int length){
            bytesArray = new byte[length];
            Done = false;
            for(int i = 0;i< (length>>2);i++) {
                expectPkts |= (byte) (IntMath.pow(2, i));
            }
        }
        public String toString(){
            String res = new String(bytesArray);
            return res;
        }
        public String toDate(){
            int year = ((bytesArray[1]&0xff)<<8)|(bytesArray[0]&0xff);
            int month = bytesArray[2];
            int day = bytesArray[3];
            int hour = bytesArray[4]&0xff;
            int minute = bytesArray[5]&0xff;
            int sec = bytesArray[6]&0xff;


            String res = year+"-"+month+"-"+day+"\n"+ hour+":" + minute + ":" +sec;

            return res;

        }

    }

    public receivedBytes SN = new receivedBytes(16);
    public receivedBytes PN = new receivedBytes(16);
    private int cellNum;
    private int C1SN;
    private int C2SN;
    private int C3SN;
    private int C4SN;
    private int C5SN;
    private int C6SN;
    private int C7SN;
    private int C8SN;
    public receivedBytes birthDate = new receivedBytes(8);
    public receivedBytes sunraiseDate = new receivedBytes(8);
    public receivedBytes sunsetData = new receivedBytes(8);
    private float cMonCal;
    private float initSOC;
    private  int blePowerLevel;
    public receivedBytes contactorNum = new receivedBytes(16);
    public receivedBytes pcbSN = new receivedBytes(16);


    public String getSN() {
        return SN.toString();
    }

    public void setSN( byte[] data,int idx) {
        for(int i = idx*4;i<idx*4+4;i++){
               SN.bytesArray[i] = data[(i%4)];
        }
        SN.expectPkts ^= (byte)IntMath.pow(2,idx);
        if(SN.expectPkts == 0){
               SN.Done = true;
        }
    }

    public String getPN() {
        return PN.toString();
    }

    public void setPN( byte[] data,int idx) {
        for(int i = idx*4;i<idx*4+4;i++){
            PN.bytesArray[i] = data[(i%4)];
        }
        PN.expectPkts ^= (byte)IntMath.pow(2,idx);
        if(PN.expectPkts == 0){
            PN.Done = true;
        }
    }

    public int getCellNum() {
        return cellNum;
    }

    public void setCellNum(int cellNum) {
        this.cellNum = cellNum;
    }

    public int getC1SN() {
        return C1SN;
    }

    public void setC1SN(int c1SN) {
        C1SN = c1SN;
    }

    public int getC2SN() {
        return C2SN;
    }

    public void setC2SN(int c2SN) {
        C2SN = c2SN;
    }

    public int getC3SN() {
        return C3SN;
    }

    public void setC3SN(int c3SN) {
        C3SN = c3SN;
    }

    public int getC4SN() {
        return C4SN;
    }

    public void setC4SN(int c4SN) {
        C4SN = c4SN;
    }

    public int getC5SN() {
        return C5SN;
    }

    public void setC5SN(int c5SN) {
        C5SN = c5SN;
    }

    public int getC6SN() {
        return C6SN;
    }

    public void setC6SN(int c6SN) {
        C6SN = c6SN;
    }

    public int getC7SN() {
        return C7SN;
    }

    public void setC7SN(int c7SN) {
        C7SN = c7SN;
    }

    public int getC8SN() {
        return C8SN;
    }

    public void setC8SN(int c8SN) {
        C8SN = c8SN;
    }

    public int getBlePowerLevel() {
        return blePowerLevel;
    }

    public void setBlePowerLevel(int blePowerLevel) {
        this.blePowerLevel = blePowerLevel;
    }

    public String getBirthDate() {
        return birthDate.toDate();
    }

    public void setBirthDate(byte[] data,int idx) {
        for(int i = idx*4;i<idx*4+4;i++){
            birthDate.bytesArray[i] = data[(i%4)];
        }
        birthDate.expectPkts ^= (byte)IntMath.pow(2,idx);
        if(birthDate.expectPkts == 0){
            birthDate.Done = true;
        }
    }

    public String getSunraiseDate() {
        return sunraiseDate.toDate();
    }

    public void setSunraiseDate(byte[] data,int idx) {
        for(int i = idx*4;i<idx*4+4;i++){
            sunraiseDate.bytesArray[i] = data[(i%4)];
        }
        sunraiseDate.expectPkts ^= (byte)IntMath.pow(2,idx);
        if(sunraiseDate.expectPkts == 0){
            sunraiseDate.Done = true;
        }
    }

    public String getSunsetData() {
        return sunsetData.toDate();
    }

    public void setSunsetData(byte[] data,int idx) {
        for(int i = idx*4;i<idx*4+4;i++){
            sunsetData.bytesArray[i] = data[(i%4)];
        }
        sunsetData.expectPkts ^= (byte)IntMath.pow(2,idx);
        if(sunsetData.expectPkts == 0){
            sunsetData.Done = true;
        }
    }

    public float getcMonCal() {
        return cMonCal;
    }

    public void setcMonCal(float cMonCal) {
        this.cMonCal = cMonCal;
    }

    public float getInitSOC() {
        return initSOC;
    }

    public void setInitSOC(float initSOC) {
        this.initSOC = initSOC;
    }

    public String getContactorNum() {
        return contactorNum.toString();
    }

    public void setContactorNum(byte[] data,int idx) {
        for(int i = idx*4;i<idx*4+4;i++){
            contactorNum.bytesArray[i] = data[(i%4)];
        }
        contactorNum.expectPkts ^= (byte)IntMath.pow(2,idx);
        if(contactorNum.expectPkts == 0){
            contactorNum.Done = true;
        }
    }

    public String getPcbSN() {
        return pcbSN.toString();
    }

    public void setPcbSN(byte[] data,int idx) {
        for(int i = idx*4;i<idx*4+4;i++){
            pcbSN.bytesArray[i] = data[(i%4)];
        }
        pcbSN.expectPkts ^= (byte)IntMath.pow(2,idx);
        if(pcbSN.expectPkts == 0){
            pcbSN.Done = true;
        }
    }

    public void setM_chargerState(String m_chargerState) {
        this.m_chargerState = m_chargerState;
    }

    public void setM_ignitionState(String m_ignitionState) {
        this.m_ignitionState = m_ignitionState;
    }

    public void setM_heaterState(String m_heaterState) {
        this.m_heaterState = m_heaterState;
    }

    public void setM_contactorState(String m_contactorState) {
        this.m_contactorState = m_contactorState;
    }

    public void setM_sleepState(String m_sleepState) {
        this.m_sleepState = m_sleepState;
    }

    public void setM_balancerState(String m_balancerState) {
        this.m_balancerState = m_balancerState;
    }

    public String getM_safeModeState() {
        return m_safeModeState;
    }

    public void setM_safeModeState(String m_safeModeState) {
        this.m_safeModeState = m_safeModeState;
    }

    private float m_SOF;

    private String m_chargerState;
    private String m_ignitionState;
    private String m_heaterState;
    private String m_contactorState;
    private String m_sleepState;
    private String m_balancerState;



    private String m_safeModeState;

    public String getAppVer() {
        return AppVer;
    }

    public void setAppVer(String appVer) {
        AppVer = appVer;
    }

    private String AppVer;

    public float getM_I_CHGR() {
        return m_I_CHGR;
    }

    public void setM_I_CHGR(float m_I_CHGR) {
        this.m_I_CHGR = m_I_CHGR;
//        if(m_I_CHGR>0&&getCurrentListener()!=null){
//            getCurrentListener().onCurrentChange(m_I_CHGR);
//        }
    }

    public void setM_C_MON(float m_C_MON) {
        this.m_C_MON = m_C_MON;

    }

    public void setM_BUS_MON(float m_BUS_MON) {
        this.m_BUS_MON = m_BUS_MON;
    }

    public void setM_batteryVoltage(float m_batteryVoltage) {
        this.m_batteryVoltage = m_batteryVoltage;
    }

    public void setM_bodyTemperature(float m_bodyTemperature) {
        this.m_bodyTemperature = m_bodyTemperature;
    }

    public void setM_cellTemperature(float m_cellTemperature) {
        this.m_cellTemperature = m_cellTemperature;
    }

    public void setM_bodyTemperatureC(float m_bodyTemperatureC) {
        this.m_bodyTemperatureC = m_bodyTemperatureC;
    }

    public void setM_cellTemperatureC(float m_cellTemperatureC) {
        this.m_cellTemperatureC = m_cellTemperatureC;
    }

    public void setM_C_MON_amps(float m_C_MON_amps) {
        this.m_C_MON_amps = m_C_MON_amps;
//        if(m_I_CHGR==0&&getCurrentListener()!=null){
//            getCurrentListener().onCurrentChange(m_C_MON_amps);
//        }
    }

    public void setM_FUEL_CELL_V(float m_FUEL_CELL_V,int idx) {
        this.m_FUEL_CELL_V[idx] = m_FUEL_CELL_V;
    }

    public void setM_SOH(float m_SOH) {
        this.m_SOH = m_SOH;
    }

    public void setM_SOC(float m_SOC) {
        if(m_SOC >= 0)
            this.m_SOC = m_SOC ;
        else
            return;
    }

    public void setM_SOF(float m_SOF) {
        this.m_SOF = m_SOF;
    }



    public float getM_C_MON() {
        return m_C_MON;
    }

    public float getM_BUS_MON() {
        return m_BUS_MON;
    }

    public float getM_batteryVoltage() {
        return m_batteryVoltage;
    }

    public float getM_bodyTemperature() {
        return m_bodyTemperature;
    }

    public float getM_cellTemperature() {
        return m_cellTemperature;
    }

    public float getM_bodyTemperatureC() {
        return m_bodyTemperatureC;
    }

    public float getM_cellTemperatureC() {
        return m_cellTemperatureC;
    }

    public float getM_C_MON_amps() {
        return m_C_MON_amps;
    }

    public float getM_FUEL_CELL_V(int idx) {
        return m_FUEL_CELL_V[idx];
    }

    public float getM_SOH() {
        return m_SOH;
    }

    public float getM_SOC() {
        return m_SOC;
    }

    public float getM_SOF() {
        return m_SOF;
    }

    public String getM_chargerState() {
        return m_chargerState;
    }

    public String getM_ignitionState() {
        return m_ignitionState;
    }

    public String getM_heaterState() {
        return m_heaterState;
    }

    public String getM_contactorState() {
        return m_contactorState;
    }

    public String getM_sleepState() {
        return m_sleepState;
    }

    public String getM_balancerState() {
        return m_balancerState;
    }





    public float getTemperatureFromVoltage(float volts)
    {
        float result;

        result = ((float)129.1 * volts) - (float)232.38;

        return result;
    }

    private BatteryData()
    {
        m_I_CHGR=m_C_MON
                =m_BUS_MON
                =m_batteryVoltage
                =m_bodyTemperature
                =m_cellTemperature
                =m_bodyTemperatureC
                =m_cellTemperatureC
                =m_C_MON_amps
                =m_SOH=m_SOC=m_SOF=0;

        m_chargerState=m_ignitionState
                =m_heaterState
                =m_sleepState
                =m_balancerState="N/A";

        m_contactorState = m_safeModeState="OFF";
    }

    public void reset(){
        m_I_CHGR=m_C_MON
                =m_BUS_MON
                =m_batteryVoltage
                =m_bodyTemperature
                =m_cellTemperature
                =m_bodyTemperatureC
                =m_cellTemperatureC
                =m_C_MON_amps
                =m_SOH=m_SOC=m_SOF=0;

        m_chargerState=m_ignitionState
                =m_heaterState
                =m_sleepState
                =m_balancerState="N/A";

        m_contactorState = m_safeModeState="OFF";
    }

}



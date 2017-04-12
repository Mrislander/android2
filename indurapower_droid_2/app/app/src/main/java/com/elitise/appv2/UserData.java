package com.elitise.appv2;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by andy on 5/9/16.
 */
public class UserData{

    private static UserData ourInstance = new UserData();

    public static UserData getInstance() {
        if(ourInstance == null) {
            ourInstance = new UserData();
        }
        return ourInstance;
    }

    private String userName;

    private boolean isLogin = false;

    private ArrayList<OwnedBattery> myBatteries;

    private ArrayList<BatteriesInventory> batteries;

    private int mCurrentIdx;



    private UserData() {
        userName = "N/A";
        mCurrentIdx = -1;
        isLogin = false;
        myBatteries = new ArrayList<>();
        batteries = new ArrayList<>();
        this.addNewBatteryInventory("Series 108", "\n" +
                "1.    Ideal pack series for motorcycles and power sports use \n" +
                "2.    350 cold cranking amps\n" +
                "3.    8 A-Hr capacity", 1);

        this.addNewBatteryInventory("Series 110", "\n" +
                "1.    Part of the 210 series batteries\n" +
                "2.    280 cold cranking amps\n" +
                "3.    10 A-Hr capacity\n", 2);

        this.addNewBatteryInventory("Series 120", "\n" +
                "1.    Compact dimensions with the full power of our 210 series\n" +
                "2.    560 cold cranking amps\n" +
                "3.    20 A-Hr capacity\n", 3);

        this.addNewBatteryInventory("Series 210", "\n" +
                "1.    Backup capabilities if no one module stops operating properly\n" +
                "2.    560 cold cranking amps\n" +
                "3.    20 A-Hr capacity\n", 4);

        this.addNewBatteryInventory("Series 220", "\n" +
                "1.    Backup capabilities if no one module stops operating properly\n" +
                "2.    1100 cold cranking amps\n" +
                "3.    40 A-Hr capacity\n", 5);

    }

    public boolean isLogin() {
        return isLogin;
    }

    public void setLogin(boolean login) {
        isLogin = login;
    }

    public ArrayList<BatteriesInventory> getBatteries() {
        return this.batteries;
    }


    public ArrayList<OwnedBattery> getBatteriesList(){
        return this.myBatteries;
    }

    public void setMyBatteries(OwnedBattery[] Batteries) {
        this.myBatteries.clear();
        for(OwnedBattery B: Batteries){
            this.myBatteries.add(B);
        }
    }

    public void setBatteries(ArrayList<BatteriesInventory> batteries) {
        this.batteries = batteries;
    }

    public String getUserName() {

        return userName;
    }

    public void setUserName(String userName) {

        this.userName = userName;
    }

    public void addNewBattery(String name, int id, String type, @Nullable byte[] image){
        OwnedBattery B;
        if(image == null) {
            B = new OwnedBattery(name, id, type);
        }else{
            B = new OwnedBattery(name, id, type,image);
        }
        myBatteries.add(B);
    }

    public void addNewBatteryInventory(String type, String description,int cnt){
        BatteriesInventory B = new BatteriesInventory(type,description,cnt);
        batteries.add(B);
    }

    public void removeNewBattery(int idx){
        myBatteries.remove(idx);
    }

    public void removeNewBatteryInventory(int idx){
        batteries.remove(idx);
    }

    public void setCurrentIdx(int idx){
        mCurrentIdx = idx;
    }
    public int getCurrentIdx(){
        return mCurrentIdx;
    }

    public class OwnedBattery implements Serializable{

        public String mName;

        public int mImageId;

        public String mType;

        public String MAC ;

        public  boolean isBonded;

        public String mSN;

        public String mCharactriscUUID;

        public String mServiceUUID;

        public boolean isDemo;

        //private Bitmap image;

        private byte[] imageByte;

        public  int engineState;

        public  boolean heaterOn;


        public byte[] getImage(){
            return imageByte;
        }

        public OwnedBattery(String name, int id, String type){
            mName = name;
            mImageId = id;
            mType = type;
            MAC = null;
            isBonded = false;
            mSN = null;
            imageByte = null;
            isDemo = false;
            engineState = 0;
            heaterOn = false;
        }
        public OwnedBattery(String name, int id, String type, byte[] image){
            mName = name;
            mImageId = id;
            mType = type;
            MAC = null;
            isBonded = false;
            mSN = null;
            imageByte = image;
            isDemo = false;
            engineState = 0;
            heaterOn = false;

        }
        public void setBonded(boolean c){
            isBonded = c;
        }
        public boolean getConnection(){
            return isBonded;
        }
        public void setMac(String addr){
            MAC = new String (addr);
        }
        public String getMac(){
            return MAC;
        }

        public void setmSN(String pwd){
            this.mSN = pwd;
        }
        public String getmSN(){
            return this.mSN;
        }
//        public String toJson(){
//            Gson gson = new Gson();
//            return gson.toJson(this);
//        }
//        public  OwnedBattery fromJson(String Json){
//            Gson gson = new Gson();
//            return  gson.fromJson(Json,OwnedBattery.class);
//        }

    }



    public class BatteriesInventory  {
        public String mType;
        public String mDescription;
        public int mCnt;

        public BatteriesInventory(String type, String description,int cnt ){
            mType = type;
            mDescription = description;
            mCnt = cnt;
        }

    }

}

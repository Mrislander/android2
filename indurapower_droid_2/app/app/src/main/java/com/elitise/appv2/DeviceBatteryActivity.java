package com.elitise.appv2;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.BatteryManager;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.text.Text;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jaredrummler.android.device.DeviceName;

import java.util.Timer;

import static com.elitise.appv2.R.id.imageView;

public class DeviceBatteryActivity extends AppCompatActivity {

    private static DeviceBatteryActivity ins;

    private ChargerGaugeView chargerGaugeView;
    private TempGaugeView tempGaugeView;
    private TextView volt;
    private ConnectLedView connectionView;
    private boolean isChargeGauge = true;
    private ImageView blueInfo;
    private BroadcastReceiver batteryReceiver;
    private Timer timer;
    private UserData mUser = UserData.getInstance();

    private SharedPreferences sharedPrefs;
    private boolean unitType;

    private TextView deviceName;
    private TextView batteryCap;
    private TextView batteryLevel;
    private TextView chargerState;
    private TextView avgCurrentView;
    private TextView mBname;
    private ImageView chargeAnimationBtn;
    private Logo_pathsView logoView;

    private int status;

    private Dialog alertDialog ;

    private int scale = -1;
    private int level = -1;
    private int voltage = -1;
    private int temp = -1;
    private int currentNow = 0;
    private int avgCurrent = 0;
    private int mCapacity;
    private DeviceBatteryAnimationView deviceBatteryAnimationView;

    private boolean isTablet = false;

    public static DeviceBatteryActivity  getInstace(){
        return ins;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ins = this;
        setContentView(R.layout.activity_device_battery_activey);
        chargerGaugeView = (ChargerGaugeView) findViewById(R.id.chargegauge);
        tempGaugeView = (TempGaugeView) findViewById(R.id.tempgauge);
        blueInfo = (ImageView) findViewById(R.id.chargerAnimation);

        deviceName = (TextView) findViewById(R.id.deviceModel);
        batteryCap = (TextView) findViewById(R.id.batteryCap);
        batteryLevel = (TextView) findViewById(R.id.batteryLevel);
        chargerState = (TextView) findViewById(R.id.chargeState);
        avgCurrentView = (TextView) findViewById(R.id.avgCurrent);
        volt = (TextView) findViewById(R.id.volt);
        mBname = (TextView)findViewById(R.id.BatteryName);
        chargeAnimationBtn = (ImageView) findViewById(R.id.chargerAnimation);


        mBname.setText("Device Battery");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(false);


        getSupportActionBar().setDisplayShowTitleEnabled(false);

        logoView = (Logo_pathsView) findViewById(R.id.logoBar);
        String myVersionName = "1.0.0"; // initialize String
        PackageManager packageManager =  getApplicationContext().getPackageManager();
        String packageName = getApplicationContext().getPackageName();
        try {
            myVersionName = packageManager.getPackageInfo(packageName, 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        logoView.setVersion(myVersionName);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        isTablet = isTabletDevice();

        if (sharedPrefs.getString(getString(R.string.pref_units_key), getString(R.string.pref_units_imperial)).equals(getString(R.string.pref_units_imperial))) {
            unitType = false;
        } else {
            unitType = true;
        }

        connectionView = (ConnectLedView) this.findViewById(R.id.connectionView);
        connectionView.setLedColor(Color.GREEN);
        final String devicename = DeviceName.getDeviceName();
        deviceName.setText(devicename);
        getBatteryCapacity();
        tempGaugeView.initGauge(unitType);
        tempGaugeView.setmCurrentVaule(42.5f);
        chargerGaugeView.setmSOC(50f);
        chargerGaugeView.setDevice(true);

        chargerGaugeView.setChargeListener(new ChargerGaugeView.ChargeGaugeListener() {
            @Override
            public void onGaugeSwipe(boolean type) {
                isChargeGauge = type;
            }
        });
        tempGaugeView.setTempListener(new TempGaugeView.TempGaugeListener() {
            @Override
            public void onGaugeSwipe(boolean type) {
                unitType = type;
                if (unitType) {
                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.putString(getString(R.string.pref_units_key), getString(R.string.pref_units_metric));
                    editor.apply();

                } else {
                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.putString(getString(R.string.pref_units_key), getString(R.string.pref_units_imperial));
                    editor.apply();
                }
            }
        });

        chargeAnimationBtn.bringToFront();
        chargeAnimationBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog = new Dialog(DeviceBatteryActivity.getInstace());
                    alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    alertDialog.setContentView(R.layout.sample_device_battery_animation_view);
                    alertDialog.setCancelable(true);
                    alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    alertDialog.getWindow().setDimAmount(1);
                    alertDialog.show();
                    deviceBatteryAnimationView = (DeviceBatteryAnimationView) alertDialog.findViewById(R.id.deviceAnimationView);

                    deviceBatteryAnimationView.setExitListener(new DeviceBatteryAnimationView.exitListener() {
                        @Override
                        public void onGaugeSwipe() {
                            if(alertDialog.isShowing()){
                                alertDialog.dismiss();
                            }
                        }
                    });
                    deviceBatteryAnimationView.init(isTablet);
                    deviceBatteryAnimationView.setMamps((float)avgCurrent);
                    deviceBatteryAnimationView.startAnimation();
                }
            });

            batteryReceiver = new BroadcastReceiver() {
            BatteryManager batteryManager = (BatteryManager) getBaseContext().getSystemService(Context.BATTERY_SERVICE);

            @Override
            public void onReceive(Context context, Intent intent) {

                level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
                voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
                mCapacity = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
                currentNow = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
                avgCurrent = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE);
                status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0);
                Log.e("BatteryManager", "level is " + level + "/" + scale + ", temp is " + temp + ", voltage is " + voltage + "current is" + currentNow + "AvgCurrent is " + avgCurrent);
                DeviceBatteryActivity.getInstace().updateUI();
            }
        };
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryReceiver, filter);

        Window wd = this.getWindow();
        wd.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        wd.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        wd.setStatusBarColor(Color.parseColor("#000000"));

    }

    public void getBatteryCapacity() {
        Object mPowerProfile_ = null;

        final String POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile";

        try {
            mPowerProfile_ = Class.forName(POWER_PROFILE_CLASS)
                    .getConstructor(Context.class).newInstance(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            double batteryCapacity = (Double) Class
                    .forName(POWER_PROFILE_CLASS)
                    .getMethod("getAveragePower", java.lang.String.class)
                    .invoke(mPowerProfile_, "battery.capacity");
            batteryCap.setText(String.format("%.1f",batteryCapacity));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(batteryReceiver != null) {
            try {
                unregisterReceiver(batteryReceiver);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(alertDialog!=null) {
            if (alertDialog.isShowing()) {
                alertDialog.dismiss();
            }
        }
        GsonBuilder gsonb = new GsonBuilder();
        Gson gson = gsonb.create();
        String ownbatteries = gson.toJson(mUser.getBatteriesList());
        SharedPreferences prefs = this.getSharedPreferences("userdata", Context.MODE_PRIVATE);
        SharedPreferences.Editor e = prefs.edit();
        e.putString("batteris", ownbatteries);
        e.apply();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private String getStatusString(int status) {
        String statusString = "Unknown";

        switch (status) {
            case BatteryManager.BATTERY_STATUS_CHARGING:
                statusString = "Charging";
                break;
            case BatteryManager.BATTERY_STATUS_DISCHARGING:
                statusString = "Discharging";
                break;
            case BatteryManager.BATTERY_STATUS_FULL:
                statusString = "Full";
                break;
            case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                statusString = "Not Charging";
                break;
        }
        return statusString;
    }

    public void updateUI(){
        DeviceBatteryActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                float percent = (float) level*100f/ (float) scale;
                batteryLevel.setText(String.format("%.1f",percent)+"%");
                chargerState.setText(getStatusString(status));
                avgCurrentView.setText(Float.toString((float) avgCurrent/1000f));
                tempGaugeView.setmCurrentVaule((float) temp/10f);
                volt.setText(String.format("%.1f",voltage/1000f)+" V");
                if(deviceBatteryAnimationView!=null){
                    deviceBatteryAnimationView.setMamps((float)avgCurrent);
                }
                //batteryCap.setText(Integer.toString(mCapacity));
                if (!isChargeGauge) {
                    chargerGaugeView.setmSOC((float)avgCurrent/1000f);
                }else{
                    chargerGaugeView.setmSOC(percent);
                }
            }
        });

    }

    public boolean isTabletDevice() {
        TelephonyManager telephony = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        int type = telephony.getPhoneType();
        if (type == TelephonyManager.PHONE_TYPE_NONE) {
            return true;
        } else {
            return false;
        }
    }
}
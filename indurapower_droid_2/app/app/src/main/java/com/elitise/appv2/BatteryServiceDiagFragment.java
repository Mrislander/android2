package com.elitise.appv2;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.SwitchCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.labo.kaji.fragmentanimations.CubeAnimation;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import static com.elitise.appv2.SystemDefinition.chargeGain.m_highGainVal;
import static com.elitise.appv2.SystemDefinition.chargeGain.m_lowGainVal;
import static com.elitise.appv2.SystemDefinition.chargeGain.m_rdsOnVal;


/**
 * Created by andy on 6/8/16.
 */
public class BatteryServiceDiagFragment extends ServiceFragment {
//
//    private static final UUID ELT_BATT_MOBBAT_SERVICE_UUID = UUID
//            .fromString("AC55D02D-A7FE-4F64-A659-ABCB5C6D4099");
//
//    private static final UUID ELT_BATT_MOBBAT_CHARACTERISTIC_UUID = UUID
//            .fromString("788C5B46-B899-473A-96C9-49B981739482");

    private static final UUID ELT_BATT_MOBBAT_SERVICE_UUID = UUID
            .fromString("AC55D02D-A7FE-4F64-A659-ABCB5C6D409D");


    private static final UUID ELT_BATT_MOBBAT_CHARACTERISTIC_UUID = UUID
            .fromString("A88C5B46-B899-473A-96C9-49B98173948D");

//    private static final byte[] CloseContactor = {-64, 104, 0, 4, 32, 77, 1, 0, 0, 0, -64};
//    private static final byte[] OpenContactor =  {-64, 104, 0, 4, 32, 77, 0, 0, 0, 0, -64};
//    private static final byte[] HeaterOn =       {-64, 102, 0, 4, 32, 64, 1, 0, 0, 0, -64};
//    private static final byte[] HeaterOff =      {-64, 102, 0, 4, 32, 64, 2, 0, 0, 0, -64};
//    private static final byte[] safeModeOn =     {-64, 110, 0, 4, 32, 75, 1, 0, 0, 0, -64};
//    private static final byte[] safeModeOff =     {-64, 110, 0, 4, 32, 74, 0, 0, 0, 0, -64};
//    private static final byte[] OneLastStartOn =    {-64, 111, 0, 4, 32, 74, 1, 0, 0, 0, -64};
   // private static  byte firstTime = 0;

    private ServiceFragment.ServiceFragmentDelegate mDelegate;

    private MaterialDialog blockDialog;


    private  BatteryData BD;
    private  UserData mUser = UserData.getInstance();
    private  int batteryIdx;
    private  int mStep = 0;

    private boolean connection = false;
    private Timer connectionTimer = new Timer();
    private TimerTask timerTask ;

    private String rotationModeArray = "00000000";
    private String uniDirectionModeArray = "11111111";

    private String currentRotationModeArray = null;
    private int accidentDetectionON = 0;
    //private boolean uniDirectionMode = false;


    private SharedPreferences sharedPrefs;
    private boolean unitType;

    private boolean isTablet;

    private TextView statusBar;
////Diagnostic Mode

    private TextView AppVer;
    private TextView ChargeState;
    private TextView IgnitionState;
    private TextView BalancerState;
    private TextView SleepState;
    private TextView FC1V;
    private TextView FC2V;
    private TextView FC3V;
    private TextView FC4V;
    private TextView BankV;
    private TextView BusV;
    private TextView C_MONV;
    private TextView C_MONA;
    private TextView I_CHGRA;
    private TextView BTV;
    private TextView CTV;
    private TextView BTDC;
    private TextView CTDC;
    private TextView SOC;
    private TextView SOH;
    private TextView SOF;
    private TextView VOLT;
    private int normalModeCount = 0;

    private SwitchCompat ContactorBtn;
    private SwitchCompat safeModeBtn;
    private SwitchCompat heaterBtn;
    private SwitchCompat oltmBtn;
    private SwitchCompat blkLedBtn;
    private SwitchCompat rotationBtn;
    private SwitchCompat currentMode;
    private SwitchCompat deepCycleBtn;

    private Button showGaugeBtn;


    private CompoundButton.OnCheckedChangeListener mContactorListener;
    private CompoundButton.OnCheckedChangeListener mSafeModeListener;
    private CompoundButton.OnCheckedChangeListener mDeepCycleListener;
    private CompoundButton.OnCheckedChangeListener mHeaterListener;

    private ChargerGaugeView chargeGauge;
    private TempGaugeView tempGauge;

    //personalization  data
    private TextView SN;
    private TextView PN;
    private TextView cellNum;
    private TextView C1SN;
    private TextView C2SN;
    private TextView C3SN;
    private TextView C4SN;
    private TextView C5SN;
    private TextView C6SN;
    private TextView C7SN;
    private TextView C8SN;
    private TextView birthData;
    private TextView sunrisehData;
    private TextView sunsetData;
    private TextView cMonClaibration;
    private TextView initSOC;
    private TextView contactorSN;
    private TextView pcbSN;
    private TextView blePowerLevel;
    private TextView mBname;
    //private String[] rotations = {"Bottom Up","Upside Down","PositiveTermDown","NegativeTermDown","LabelUp","LabelDown"};

    private int selected = -1;

    //seekbar
    //private DiscreteSeekBar TXPowerSeekbar;
    //private DiscreteSeekBar WakeCycleSeekbar;

    private Dialog alertDialog ;
    private ImageView chargeAnimationBtn;
    private CurrentChargeStylekitView currentChargeStylekitView;

    private ConnectLedView connectionView;



    private static  boolean ContactorOpen = false;
    //private static  boolean isCelsius = false;

    private  boolean isChargeGauge = true;
    // GATT
    private BluetoothGattService mBatteryService;
    private BluetoothGattCharacteristic mBatteryCharacteristic;
    public BatteryServiceDiagFragment() {
            mBatteryCharacteristic =
                    new BluetoothGattCharacteristic(ELT_BATT_MOBBAT_CHARACTERISTIC_UUID,
                            BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
                            BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PERMISSION_WRITE);
            mBatteryCharacteristic.addDescriptor(
                    Peripheral.getClientCharacteristicConfigurationDescriptor());
            mBatteryService = new BluetoothGattService(ELT_BATT_MOBBAT_SERVICE_UUID,
                    BluetoothGattService.SERVICE_TYPE_PRIMARY);
            mBatteryCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            mBatteryCharacteristic.setValue("test");
            mBatteryService.addCharacteristic(mBatteryCharacteristic);
        this.setRetainInstance(true);
        }


    //Lifecycle callbacks
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_diag_battery, container, false);

        isTablet = isTabletDevice();

        BD = BatteryData.getInstance();
        batteryIdx = getArguments().getInt("bIndex");
        statusBar = (TextView) getActivity().findViewById(R.id.statusBar);

        AppVer = (TextView) view.findViewById(R.id.AppVar);
        ChargeState = (TextView) view.findViewById(R.id.ChargerState);
        IgnitionState = (TextView) view.findViewById(R.id.IgnitionState);
        BalancerState = (TextView) view.findViewById(R.id.BalancerState);
        SleepState = (TextView) view.findViewById(R.id.SleepState);
        FC1V = (TextView) view.findViewById(R.id.FC1V);
        FC2V = (TextView) view.findViewById(R.id.FC2V);
        FC3V = (TextView) view.findViewById(R.id.FC3V);
        FC4V = (TextView) view.findViewById(R.id.FC4V);
        BankV = (TextView) view.findViewById(R.id.BankV);
        BusV = (TextView) view.findViewById(R.id.BusV);
        C_MONV = (TextView) view.findViewById(R.id.C_MONV);
        C_MONA = (TextView) view.findViewById(R.id.C_MONA);
        I_CHGRA = (TextView) view.findViewById(R.id.I_CHRA);
        BTV = (TextView) view.findViewById(R.id.BTV);
        BTDC = (TextView) view.findViewById(R.id.BTDC);
        CTV = (TextView) view.findViewById(R.id.CTV);
        CTDC = (TextView) view.findViewById(R.id.CTDC);
        SOC = (TextView) view.findViewById(R.id.SOC);
        SOF = (TextView) view.findViewById(R.id.SOF);
        SOH = (TextView) view.findViewById(R.id.SOH);
        VOLT = (TextView) view.findViewById(R.id.volt);
        // personalization  data
        SN = (TextView) view.findViewById(R.id.SN);
        PN = (TextView) view.findViewById(R.id.partNumber);
        cellNum = (TextView) view.findViewById(R.id.cellNumber);
        C1SN = (TextView) view.findViewById(R.id.C1SN);
        C2SN = (TextView) view.findViewById(R.id.C2SN);
        C3SN = (TextView) view.findViewById(R.id.C3SN);
        C4SN = (TextView) view.findViewById(R.id.C4SN);
        C5SN = (TextView) view.findViewById(R.id.C5SN);
        C6SN = (TextView) view.findViewById(R.id.C6SN);
        C7SN = (TextView) view.findViewById(R.id.C7SN);
        C8SN = (TextView) view.findViewById(R.id.C8SN);
        birthData = (TextView) view.findViewById(R.id.birhtDate);
        sunrisehData = (TextView) view.findViewById(R.id.sunraiseDate);
        sunsetData = (TextView) view.findViewById(R.id.sunsetDate);
        cMonClaibration = (TextView) view.findViewById(R.id.c_MonCalibrationValue);
        initSOC = (TextView)view.findViewById(R.id.initialSOC);
        contactorSN = (TextView) view.findViewById(R.id.contactorSN);
        pcbSN = (TextView) view.findViewById(R.id.pcbSN);
        blePowerLevel = (TextView)view.findViewById(R.id.blePowerLevel);
//        TXPowerSeekbar = (DiscreteSeekBar)view.findViewById(R.id.txSeekBar);
//        WakeCycleSeekbar = (DiscreteSeekBar)view.findViewById(R.id.wCSeekBar);


        //load unitType

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if(sharedPrefs.getString(getString(R.string.pref_units_key),getString(R.string.pref_units_imperial)).equals(getString(R.string.pref_units_imperial))){
            unitType = false;
        }
        else {
            unitType = true;
        }




        blockDialog =  new MaterialDialog.Builder(getActivity())
                .autoDismiss(false)
                .content("Entering Power OFF Mode, please wait for confirmation")
                .build();


        mBname = (TextView) view.findViewById(R.id.BatteryName);
        showGaugeBtn = (Button) view.findViewById(R.id.showGaugeBtn);

        if(getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            LinearLayout phone = (LinearLayout) view.findViewById(R.id.forPhone);
            LinearLayout tablet = (LinearLayout) view.findViewById(R.id.forTablet);
            if(!isTablet){
                phone.setVisibility(View.VISIBLE);
                tablet.setVisibility(View.GONE);
            }else{
                phone.setVisibility(View.GONE);
                tablet.setVisibility(View.VISIBLE);
            }
        }



        if(!isTablet){

            LinearLayout topGaugeLayout = (LinearLayout) view.findViewById(R.id.topGaugeLayout);
            topGaugeLayout.setVisibility(View.GONE);
            LinearLayout topPhoneWithoutGauge = (LinearLayout) view.findViewById(R.id.topPhoneWithoutGauge);
            topPhoneWithoutGauge.setVisibility(View.VISIBLE);
            mBname = (TextView) view.findViewById(R.id.BatteryName2);
            connectionView = (ConnectLedView) view.findViewById(R.id.connectionView2);
            showGaugeBtn.setVisibility(View.VISIBLE);
            showGaugeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((Peripheral)getActivity()).enableNormalMode();
                }
            });
            if(getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                chargeGauge = (ChargerGaugeView) view.findViewById(R.id.chargegauge2);
                tempGauge = (TempGaugeView) view.findViewById(R.id.tempgauge2);
            }else{
                chargeGauge = (ChargerGaugeView) view.findViewById(R.id.chargegauge);
                tempGauge = (TempGaugeView) view.findViewById(R.id.tempgauge);
            }
        }else{
            if(getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                LinearLayout topGaugeLayout = (LinearLayout) view.findViewById(R.id.topGaugeLayout);
                topGaugeLayout.setVisibility(View.VISIBLE);
                LinearLayout topPhoneWithoutGauge = (LinearLayout) view.findViewById(R.id.topPhoneWithoutGauge);
                topPhoneWithoutGauge.setVisibility(View.GONE);
            }else {
                LinearLayout topGaugeLayout = (LinearLayout) view.findViewById(R.id.topGaugeLayout);
                topGaugeLayout.setVisibility(View.VISIBLE);
            }
            mBname = (TextView) view.findViewById(R.id.BatteryName);
            connectionView = (ConnectLedView) view.findViewById(R.id.connectionView);
            showGaugeBtn.setVisibility(View.GONE);
            chargeGauge = (ChargerGaugeView) view.findViewById(R.id.chargegauge);
            tempGauge = (TempGaugeView) view.findViewById(R.id.tempgauge);
        }

        mBname.setText(getArguments().getString("mBname"));

        chargeGauge = (ChargerGaugeView) view.findViewById(R.id.chargegauge);

        chargeGauge.setmSOC(50f);

        chargeGauge.setChargeListener(new ChargerGaugeView.ChargeGaugeListener() {
            @Override
            public void onGaugeSwipe(boolean type) {
                isChargeGauge = type;
            }
        });



        tempGauge = (TempGaugeView) view.findViewById(R.id.tempgauge);

        tempGauge.initGauge(unitType);

        tempGauge.setmCurrentVaule(42.5f);


//        alertDialog = new Dialog(getActivity());
//        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        alertDialog.setContentView(R.layout.sample_current_charge_stylekit_view);
//        alertDialog.setCancelable(true);
//        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//            @Override
//            public void onDismiss(DialogInterface dialog) {
//                currentChargeStylekitView.stopAnimation();
//            }
//        });



        //currentChargeStylekitView.startAnimation();
        chargeAnimationBtn = (ImageView) view.findViewById(R.id.chargerAnimation);
        if(getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ) {
            chargeAnimationBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog = new Dialog(getActivity());
                    alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    alertDialog.setContentView(R.layout.sample_current_charge_stylekit_view);
                    alertDialog.setCancelable(true);
                    alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    alertDialog.getWindow().setDimAmount(1);
                    alertDialog.show();
                    currentChargeStylekitView = (CurrentChargeStylekitView) alertDialog.findViewById(R.id.currentChargeStylekitView);
                    currentChargeStylekitView.setExitListener(new CurrentChargeStylekitView.exitListener() {
                        @Override
                        public void onGaugeSwipe() {
                            if(alertDialog.isShowing()){
                                alertDialog.dismiss();
                            }
                        }
                    });
                    if (BD.getM_C_MON_amps() < 0) {
                        if (mUser.getBatteriesList().get(batteryIdx).mImageId == 6) {
                            currentChargeStylekitView.setImage(2);
                        } else {
                            currentChargeStylekitView.setImage(1);
                        }
                    } else {
                        currentChargeStylekitView.setImage(0);
                    }
                    currentChargeStylekitView.setmValue(BD.getM_C_MON_amps());
                    currentChargeStylekitView.startAnimation();
                }
            });
        }else{
            chargeAnimationBtn.setVisibility(View.GONE);
        }

        ContactorBtn = (SwitchCompat) view.findViewById(R.id.Engage);
        mContactorListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    if(!safeModeBtn.isChecked()) {
                        new MaterialDialog.Builder(getActivity())
                                .theme(Theme.LIGHT)
                                .title("Low Power Mode")
                                .content("Low Power Mode will enable low power out only.\n" +
                                        "You will not be able to start your engine, but All radio presets will be maintained.")
                                .positiveText("OK")
                                .positiveColor(Color.parseColor("#ff6529"))
                                .negativeText("Cancel")
                                .negativeColor(Color.parseColor("#ff6529"))
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        mBatteryCharacteristic.setValue(SystemDefinition.OpenContactor);
                                        mDelegate.sendNotificationToDevices(mBatteryCharacteristic);
                                    }
                                })
                                .onNegative(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        ContactorBtn.setChecked(false);
                                    }
                                })
                                .show();
                        statusBar.setText("Status: Low Power Mode");
                    }
                    else {
                        new MaterialDialog.Builder(getActivity())
                                .content("Please Turn off Power Off Mode First")
                                .positiveColor(Color.parseColor("#ff6529"))
                                .positiveText("Ok")
                                .cancelable(false)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        buttonView.setOnCheckedChangeListener(null);
                                        buttonView.setChecked(false);
                                        buttonView.setOnCheckedChangeListener(mContactorListener);
                                    }
                                })
                                .show();
                    }
                } else {
                    mBatteryCharacteristic.setValue(SystemDefinition.CloseContactor);
                    mDelegate.sendNotificationToDevices(mBatteryCharacteristic);
                    statusBar.setText("Status: Normal Mode");
                }

            }
        };

        ContactorBtn.setOnCheckedChangeListener(mContactorListener);

        heaterBtn = (SwitchCompat) view.findViewById(R.id.heaterBtn);
        mHeaterListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    new MaterialDialog.Builder(getActivity())
                            .title("Heater On")
                            .theme(Theme.LIGHT)
                            .content("The heater will turn on for at most 10 minutes while the battery temperature is below 30°C(86°F)")
                            .negativeColor(Color.parseColor("#ff6529"))
                            .positiveColor(Color.parseColor("#ff6529"))
                            .positiveText("Ok")
                            .negativeText("Cancel")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    mBatteryCharacteristic.setValue(SystemDefinition.HeaterOn);
                                    mDelegate.sendNotificationToDevices(mBatteryCharacteristic);
                                }
                            })
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    heaterBtn.setChecked(false);
                                }
                            })
                            .show();
                    statusBar.setText("Status: Heater On");
                }else {
                    mBatteryCharacteristic.setValue(SystemDefinition.HeaterOff);
                    mDelegate.sendNotificationToDevices(mBatteryCharacteristic);
                    statusBar.setText("Status: Normal Mode");
                }

            }
        };
        heaterBtn.setOnCheckedChangeListener(mHeaterListener);


        oltmBtn = (SwitchCompat) view.findViewById(R.id.oltmBtn);
        oltmBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    new MaterialDialog.Builder(getActivity())
                            .theme(Theme.LIGHT)
                            .title("One Last Start Mode")
                            .content("One Last Start Mode will turn on the battery for 30 seconds.\n" +
                                    "During this time you may attempt to start your engine")
                            .negativeColor(Color.parseColor("#ff6529"))
                            .positiveColor(Color.parseColor("#ff6529"))
                            .positiveText("Ok")
                            .negativeText("Cancel")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    mBatteryCharacteristic.setValue(SystemDefinition.OneLastStartOn);
                                    mDelegate.sendNotificationToDevices(mBatteryCharacteristic);
                                    oltmBtn.setChecked(true);
                                    oltmBtn.setEnabled(false);
                                    Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        public void run() {
                                            oltmBtn.setChecked(false);
                                            oltmBtn.setEnabled(true);
                                            statusBar.setText("Status: Normal Mode");
                                        }
                                    }, 30000);
                                }
                            })
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    oltmBtn.setChecked(false);
                                }
                            })
                            .show();
                    statusBar.setText("Status: One Last Start Mode");
                }


            }
        });

        rotationBtn = (SwitchCompat) view.findViewById(R.id.rotationModeSettingBtn);
        rotationBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    selectionDialog();
                    buttonView.setChecked(false);
                }
            }
        });

        safeModeBtn = (SwitchCompat) view.findViewById(R.id.batteryState);
        mSafeModeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    if(!ContactorBtn.isChecked()) {
                        new MaterialDialog.Builder(getActivity())
                                .theme(Theme.LIGHT)
                                .title("Power Off Mode")
                                .content("Power Off Mode will completely shutdown the battery. However, bluetooth connectivity will remain active.\n" +
                                        "You will not be able to start your engine and All radio presents will be lost")
                                .negativeColor(Color.parseColor("#ff6529"))
                                .positiveColor(Color.parseColor("#ff6529"))
                                .positiveText("Ok")
                                .negativeText("Cancel")
                                .cancelable(false)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        mBatteryCharacteristic.setValue(SystemDefinition.safeModeOn);
                                        mDelegate.sendNotificationToDevices(mBatteryCharacteristic);
                                        blockDialog.show();
                                    }
                                })
                                .onNegative(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        buttonView.setChecked(false);
                                        dialog.dismiss();
                                    }
                                })
                                .show();
                        statusBar.setText("Status: Power Off Mode");
                    }
                    else {
                        new MaterialDialog.Builder(getActivity())
                                .content("Please Turn off Low Power Mode First")
                                .positiveColor(Color.parseColor("#ff6529"))
                                .positiveText("Ok")
                                .cancelable(false)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        buttonView.setOnCheckedChangeListener(null);
                                        buttonView.setChecked(false);
                                        buttonView.setOnCheckedChangeListener(mSafeModeListener);
                                    }
                                })
                                .show();

                    }

                } else {
                    statusBar.setText("Status: Normal Mode");
                    mBatteryCharacteristic.setValue(SystemDefinition.safeModeOff);
                    mDelegate.sendNotificationToDevices(mBatteryCharacteristic);
                }
            }
        };
        safeModeBtn.setOnCheckedChangeListener(mSafeModeListener);


        tempGauge.setTempListener(new TempGaugeView.TempGaugeListener() {
            @Override
            public void onGaugeSwipe(boolean type) {
                unitType = type;
                if(unitType) {
                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.putString(getString(R.string.pref_units_key),getString(R.string.pref_units_metric));
                    editor.apply();
                }
                else {
                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.putString(getString(R.string.pref_units_key),getString(R.string.pref_units_imperial));
                    editor.apply();
                }
            }
        });

        chargeGauge.setChargeListener(new ChargerGaugeView.ChargeGaugeListener() {
            @Override
            public void onGaugeSwipe(boolean type) {
                isChargeGauge = type;
            }
        });




        blkLedBtn = (SwitchCompat) view.findViewById(R.id.BLKLED);
        blkLedBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                byte[] cmd = SystemDefinition.encodeCmd(SystemDefinition.CMOMANDSTYPE.SERVICE_COMMANDS,
                        SystemDefinition.SERVICE_CMD.BLINK_LED, new byte[4],SystemDefinition.dataType.INTEGERX,0,9);
                mBatteryCharacteristic.setValue(cmd);
                mDelegate.sendNotificationToDevices(mBatteryCharacteristic);
                //statusBar.setText("Status: Blink Led");
                buttonView.setChecked(false);
            }
        });

//        ContactorBtn.setEnabled(false);
//        safeModeBtn.setEnabled(false);
//        heaterBtn.setEnabled(false);
//        oltmBtn.setEnabled(false);

        currentMode = (SwitchCompat) view.findViewById(R.id.currentMode);
        currentMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    rotationModeDialog(currentRotationModeArray);
                }
            }
        });



        deepCycleBtn = (SwitchCompat) view.findViewById(R.id.deepCycleBtn);
        mDeepCycleListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    oltmBtn.setEnabled(false);
//                    byte[] cmd = SystemDefinition.encodeCmd(SystemDefinition.CMOMANDSTYPE.DEBUG_COMMANDS, SystemDefinition.DEBUG_CMD.SET_DEEP_CYCLE_MODE,
//                            ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(0).array(), 0, 0, 9);
//                    mBatteryCharacteristic.setValue(cmd);
//                    mDelegate.sendNotificationToDevices(mBatteryCharacteristic);
                    new MaterialDialog.Builder(getActivity())
                            .content("Enable Deep Cycle will disable One Last Start Feature")
                            .positiveText("Confirm")
                            .negativeText("Cancel")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    byte[] cmd = SystemDefinition.encodeCmd(SystemDefinition.CMOMANDSTYPE.DEBUG_COMMANDS, SystemDefinition.DEBUG_CMD.SET_DEEP_CYCLE_MODE,
                                            ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(1).array(), 0, 0, 9);
                                    mBatteryCharacteristic.setValue(cmd);
                                    mDelegate.sendNotificationToDevices(mBatteryCharacteristic);
                                }
                            })
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    deepCycleBtn.setChecked(false);
                                }
                            })
                            .show();

                }
                else{
                    oltmBtn.setChecked(false);
                    oltmBtn.setEnabled(true);
                    byte[] cmd = SystemDefinition.encodeCmd(SystemDefinition.CMOMANDSTYPE.DEBUG_COMMANDS, SystemDefinition.DEBUG_CMD.SET_DEEP_CYCLE_MODE,
                            ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(0).array(), 0, 0, 9);
                    mBatteryCharacteristic.setValue(cmd);
                    mDelegate.sendNotificationToDevices(mBatteryCharacteristic);

                }
            }
        };
        deepCycleBtn.setOnCheckedChangeListener(mDeepCycleListener);


        if(!mUser.getBatteriesList().get(batteryIdx).isBonded && mUser.getBatteriesList().get(batteryIdx).getmSN()!= null ) {
                    scanBarcode(1);
        }


        VOLT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(++normalModeCount>2){
                    normalModeCount = 0;
                    byte[] cmd = SystemDefinition.encodeCmd(SystemDefinition.CMOMANDSTYPE.HOST_COMMANDS,SystemDefinition.HOST_CMD.SET_DIAG_MODE,
                            ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN ).putInt(0).array(),0,0,9);
                    mBatteryCharacteristic.setValue(cmd);
                    mDelegate.sendNotificationToDevices(mBatteryCharacteristic);
                    ((Peripheral)getActivity()).enableNormalMode();
                }
            }
        });

        timerTask = new TimerTask() {
            @Override
            public void run() {
                //connection = false;
                updateConnection(false);
            }
        };
        connectionTimer.schedule(timerTask,5000);

        return view;
    }


    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try{
            mDelegate = (ServiceFragment.ServiceFragmentDelegate) activity;
        }catch (ClassCastException e){
            throw new ClassCastException(activity.toString());
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(alertDialog!=null) {
            if (alertDialog.isShowing()) {
                alertDialog.dismiss();
            }
        }

    }
    @Override
    public  void onDetach(){
        super.onDetach();
        mDelegate = null;
    }
    public BluetoothGattService getBluetoothGattService() {
        return mBatteryService;
    }
    public BluetoothGattService getBluetoothGattServiceCustom(){

        batteryIdx = getArguments().getInt("bIndex");
        SystemDefinition.customUUID(mUser.getBatteriesList().get(batteryIdx).getmSN());

        mBatteryCharacteristic =
                new BluetoothGattCharacteristic(UUID.fromString(SystemDefinition.ELT_BATT_MOBBAT_CHARACTERISTIC_UUID_WITH_SN),
                        BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
                        BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PERMISSION_WRITE);
        mBatteryCharacteristic.addDescriptor(
                Peripheral.getClientCharacteristicConfigurationDescriptor());
//        mBatteryCharacteristic.addDescriptor(
//                Peripheral.getClientCharacteristicExtendedDescriptor());
        mBatteryService = new BluetoothGattService(UUID.fromString(SystemDefinition.ELT_BATT_MOBBAT_SERVICE_UUID_WITH_SN),
                BluetoothGattService.SERVICE_TYPE_PRIMARY);

        mBatteryCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        mBatteryCharacteristic.setValue("test");
        mBatteryService.addCharacteristic(mBatteryCharacteristic);
        return mBatteryService;
    }

    @Override
    public ParcelUuid getServiceUUID(boolean bonded) {

        if(!bonded)
            return new ParcelUuid(ELT_BATT_MOBBAT_SERVICE_UUID);
        else {
            SystemDefinition.customUUID(mUser.getBatteriesList().get(batteryIdx).getmSN());
            return new ParcelUuid(UUID.fromString(SystemDefinition.ELT_BATT_MOBBAT_SERVICE_UUID_WITH_SN));
        }
    }



//    public static final byte SLIP_END = (byte) 0xC0;
//    public static final byte SLIP_ESC = (byte) 0xDB;
//    public static final byte SLIP_ESC_END = (byte)0xDC;
//    public static final byte SLIP_ESC_ESC = (byte)0xDD;
    public static final int BUFF_LENGTH = 9;

    public static  boolean m_receivingSlip = false;
    public static  boolean m_haveCompletepackA = false;
    public static  byte[] ByteArray = null;
    public static  byte[] decodeByteArray = null;


    private static final String TAG = Peripheral.class.getCanonicalName();


    @Override
    public int writeCharacteristic(BluetoothGattCharacteristic characteristic, int offset, byte[] value) {

        //connection = true;

        updateConnection(true);
        if(timerTask != null)
            timerTask.cancel();

        int receivedLen = value.length;
        if(blockDialog!=null && blockDialog.isShowing()) {
            blockDialog.dismiss();
        }
        ByteArray = new byte[receivedLen];
        for(int idx = 0;idx<receivedLen;idx++){
            if(m_receivingSlip)
            {

                ByteArray[idx] = value[idx];
                if(value[idx]==SystemDefinition.SLIP_END)
                {
                    m_receivingSlip=false;
                    m_haveCompletepackA=true;
                }
                value[idx]=0;

            }
            else if(value[idx]==SystemDefinition.SLIP_END)
            {
                ByteArray[idx] = value[idx];
                m_receivingSlip = true;
                value[idx]=0;
                if((idx+1)<receivedLen)
                    if(value[idx+1]==SystemDefinition.SLIP_END)
                        idx++;
            }
            if(m_haveCompletepackA)
            {

                decodeByteArray = decode(ByteArray);
                //Log.v(TAG, "AfterDecode: "+ Arrays.toString(decodeByteArray));
                parseSentence(decodeByteArray);
                m_haveCompletepackA = false;
                ByteArray = null;
            }
        }

        timerTask = new TimerTask() {
            @Override
            public void run() {
                //connection = false;
                updateConnection(false);
            }
        };
        connectionTimer.schedule(timerTask,5000);

        return  BluetoothGatt.GATT_SUCCESS;
    }

    public byte[] decode(byte[] ByteArray)
    {
        int idx;
        byte c;
        boolean escaped = false;
        byte[] decodeArray  = new byte[BUFF_LENGTH];
        boolean beginingFound = false;
        int dest_pos = 0;
        for(idx = 0;idx < ByteArray.length;idx++){
            c = ByteArray[idx];
            if(c==SystemDefinition.SLIP_END){
                if(dest_pos == 0){
                    beginingFound = true;

                    continue;
                }
                else{

                    return decodeArray ;
                }


            }
            else if(escaped){
                if(c == SystemDefinition.SLIP_ESC_END)
                    c=SystemDefinition.SLIP_END;
                else if(c == SystemDefinition.SLIP_ESC_ESC)
                    c=SystemDefinition.SLIP_ESC;
                else{


                }
                escaped = false;
            }
            else if(c== SystemDefinition.SLIP_ESC)
            {
                escaped = true;
                continue;
            }

            if(beginingFound&&(dest_pos<BUFF_LENGTH)){
                decodeArray[dest_pos++] = c;
            }
        }

        return decodeArray;
    }



    public void updateConnection(final boolean c) {
        if(getActivity() == null )
            return;
        if(c == true){
            //Log.v(TAG, "Obs: Connect");
            if(!connection) {
                byte[] cmd = SystemDefinition.encodeCmd(SystemDefinition.CMOMANDSTYPE.HOST_COMMANDS, SystemDefinition.HOST_CMD.SET_DIAG_MODE,
                        ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(1).array(), 0, 0, 9);
                mBatteryCharacteristic.setValue(cmd);
                mDelegate.sendNotificationToDevices(mBatteryCharacteristic);
                connection = true;
             }
             getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        connectionView.setLedColor(Color.GREEN);
                    }
                });

        }
        else {
            //Log.v(TAG,"Obs: Disonnect");
            connection = false;
            BD.reset();
             // firstTime = 0;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    connectionView.setLedColor(Color.RED);
                    FC1V.setText("N/A");
                    FC2V.setText("N/A");
                    FC3V.setText("N/A");
                    FC4V.setText("N/A");
                    BankV.setText("N/A");
                    BusV.setText("N/A");
                    C_MONV.setText("N/A");
                    if (!ContactorOpen) {
                        C_MONA.setText("N/A");
                    } else {
                        C_MONA.setText("0.00");
                    }
                    I_CHGRA.setText("N/A");
                    BTV.setText("N/A");
                    BTDC.setText("N/A");
                    CTV.setText("N/A");
                    CTDC.setText("N/A");
                    SOC.setText("N/A");
                    SOH.setText("N/A");
                    SOF.setText("N/A");
                    AppVer.setText("N/A");

                    ChargeState.setText("N/A");
                    IgnitionState.setText("N/A");
                    BalancerState.setText("N/A");
                    SleepState.setText("N/A");


                    VOLT.setText("0.0V");

                    tempGauge.setmCurrentVaule(42.5f);
//
                    chargeGauge.setmSOC(50f);

                    SN.setText("N/A");
                    PN.setText("N/A");
                    cellNum.setText("N/A");
                    C1SN.setText("N/A");
                    C2SN.setText("N/A");
                    C3SN.setText("N/A");
                    C4SN.setText("N/A");
                    C5SN.setText("N/A");
                    C6SN.setText("N/A");
                    C7SN.setText("N/A");
                    C8SN.setText("N/A");
                    birthData.setText("N/A");
                    sunsetData.setText("N/A");
                    sunrisehData.setText("N/A");
                    cMonClaibration.setText("N/A");
                    initSOC.setText("N/A");
                    blePowerLevel.setText("N/A");
                    contactorSN.setText("N/A");
                    pcbSN.setText("N/A");

//                    ContactorBtn.setEnabled(false);
//                    safeModeBtn.setEnabled(false);
//                    heaterBtn.setEnabled(false);
//                    oltmBtn.setChecked(false);

                }
            });

        }
    }

    public void parseSentence(byte[] data){

        int commandClass;
        int commandID;
        byte[] parameter = null;
        int checksum;
        int dataType;
        int index;

        commandClass = data[0] & 0xE0;
        commandID = data[0] & 0x1F;
        parameter = combineFourBytes(data[5],data[6],data[7],data[8]);
        checksum = data[4];
        dataType = (data[3] & 0xE0) >> 5;
        index = (data[3] & 0x1F);
        //Log.v(TAG, "parsing: "+ commandClass+" CID:"+commandID+ "  Parm: "+ Arrays.toString(parameter) + " index : "+index + " dataType :" + dataType);
        if(commandClass == SystemDefinition.CMOMANDSTYPE.HOST_COMMANDS ){
            if(mUser.getBatteriesList().get(batteryIdx).isBonded) {
                ParseHostCommand(commandID, parameter, dataType, index);
            }
            //Log.v(TAG,"HOST_COMMANDS");
        }
        else if (commandClass ==SystemDefinition.CMOMANDSTYPE.DEBUG_COMMANDS){
            if(mUser.getBatteriesList().get(batteryIdx).isBonded) {
                ParseDebugCommand(commandID, parameter, dataType, index);
            }
            //Log.v(TAG,"DEBUG_COMMANDS");
        }
        else if (commandClass ==SystemDefinition.CMOMANDSTYPE.SERVICE_COMMANDS){
            if(mUser.getBatteriesList().get(batteryIdx).isBonded) {
                ParseServiceCommand(commandID, parameter, dataType, index);
            }
            else {
                if(commandID == SystemDefinition.SERVICE_CMD.SERIAL_NUMBER){
                    ParseServiceCommand(commandID, parameter, dataType, index);
                }
            }
            //Log.v(TAG,"SERVICE_COMMANDS");
        }
        else {
            Log.v(TAG,"Wrong Type Command");
        }


    }
    public static byte[] combineFourBytes(byte b1,byte b2,byte b3,byte b4){
        byte[] temp = new byte[4];
        temp[0]=b1;
        temp[1]=b2;
        temp[2]=b3;
        temp[3]=b4;
        return temp;
    }



    public static int attempts = 0;
    public SystemDefinition.ValContainer<String> storedPwd = new SystemDefinition.ValContainer();
    private float[] SOCs = {0.0f,0.0f,0.0f,0.0f,0.0f,0.0f};
    private static int nodeNum = 1;

    public void ParseHostCommand(int commandID, byte[] parm, int dataType, final int index)
    {
        float parmFloat = ByteBuffer.wrap(parm).order(ByteOrder.LITTLE_ENDIAN).getFloat();
        int parmInt = ByteBuffer.wrap(parm).order(ByteOrder.LITTLE_ENDIAN).getInt();
            switch (commandID) {
                case SystemDefinition.HOST_CMD.GET_I_CHARGE:
                    break;
                case SystemDefinition.HOST_CMD.SET_I_CHARGE:
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            I_CHGRA.setText(String.format("%.3f", BD.getM_I_CHGR()));
                        }
                    });
                    BD.setM_I_CHGR(parmFloat * (float) 3.333);
                    break;
                case SystemDefinition.HOST_CMD.GET_C_MON:
                    break;
                case SystemDefinition.HOST_CMD.SET_C_MON:


                    if(index == 0 ) {
                        float chargingGain;
                        boolean isChargeing;
                        isChargeing = parmFloat > (float) 1.2206 ? true:false;

                        if(isChargeing){
                            chargingGain = m_highGainVal;
                            if (parmFloat>100.0)
                            {
                                parmFloat -= 100.0;
                                chargingGain = m_lowGainVal;
                            }

                        }else{
                            chargingGain = 29.369f;
                        }
                        //chargingGain = (parmFloat > (float) 1.2218 ? (float) 104 : (float) 29.369);
                        BD.setM_C_MON(parmFloat);
                        BD.setM_C_MON_amps(((float) 1.2218 - BD.getM_C_MON()) / ((float) m_rdsOnVal * chargingGain));
                        if(currentChargeStylekitView != null) {
                            if (BD.getM_C_MON_amps() < 0) {
                                if (mUser.getBatteriesList().get(batteryIdx).mImageId == 6) {
                                    currentChargeStylekitView.setImage(2);
                                } else {
                                    currentChargeStylekitView.setImage(1);
                                }
                            } else {
                                currentChargeStylekitView.setImage(0);
                            }
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                C_MONV.setText(String.format("%.3f", BD.getM_C_MON()));
                                C_MONA.setText(String.format("%.3f", BD.getM_C_MON_amps()));
                                if (!isChargeGauge) {
                                    //double f = (BD.getM_C_MON_amps() + 100) / 2;
                                    if(currentChargeStylekitView!=null) {
                                        currentChargeStylekitView.setmValue(BD.getM_C_MON_amps());
                                    }
                                    chargeGauge.setmSOC(BD.getM_C_MON_amps());
                                }
                            }
                        });
                    }
                    break;
                case SystemDefinition.HOST_CMD.GET_BUS_MON:
                    break;
                case SystemDefinition.HOST_CMD.SET_BUS_MON:
                    BD.setM_BUS_MON(parmFloat);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            BusV.setText(String.format("%.3f", BD.getM_BUS_MON()));
                            VOLT.setText(String.format("%.1f", BD.getM_BUS_MON()) + "V");
                        }
                    });
                    break;
                case SystemDefinition.HOST_CMD.GET_BANK_V:
                    break;
                case SystemDefinition.HOST_CMD.SET_BANK_V:
                    if(index == 0 ) {
                        BD.setM_batteryVoltage(parmFloat);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                BankV.setText(String.format("%.3f", BD.getM_batteryVoltage()));
                            }
                        });
                    }
                    break;
                case SystemDefinition.HOST_CMD.GET_FUEL_CELL_V:
                    break;
                case SystemDefinition.HOST_CMD.SET_FUEL_CELL_V:
                    BD.setM_FUEL_CELL_V(parmFloat, index);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (index == 0)
                                FC1V.setText(String.format("%.3f", BD.getM_FUEL_CELL_V(index)));
                            else if (index == 1)
                                FC2V.setText(String.format("%.3f", BD.getM_FUEL_CELL_V(index)));
                            else if (index == 2)
                                FC3V.setText(String.format("%.3f", BD.getM_FUEL_CELL_V(index)));
                            else if (index == 3)
                                FC4V.setText(String.format("%.3f", BD.getM_FUEL_CELL_V(index)));
                            else
                                Log.v("decode", "wrong inx for fuel cell");
                        }
                    });
                    break;
                case SystemDefinition.HOST_CMD.GET_BODY_TEMPERATURE:
                    break;
                case SystemDefinition.HOST_CMD.SET_BODY_TEMPERATURE:
                    BD.setM_bodyTemperature(parmFloat);
                    BD.setM_bodyTemperatureC(BD.getTemperatureFromVoltage(BD.getM_bodyTemperature()));
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            BTV.setText(String.format("%.3f", BD.getM_bodyTemperature()));
                            BTDC.setText(String.format("%.3f", BD.getM_bodyTemperatureC()));
                        }
                    });
                    break;
                case SystemDefinition.HOST_CMD.GET_CELL_TEMPERATURE:
                    break;

                case SystemDefinition.HOST_CMD.SET_CELL_TEMPERATURE:
                    if(index == 0 ) {
                        BD.setM_cellTemperature(parmFloat);
                        BD.setM_cellTemperatureC(BD.getTemperatureFromVoltage(BD.getM_cellTemperature()));
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                CTV.setText(String.format("%.3f", BD.getM_cellTemperature()));
                                CTDC.setText(String.format("%.3f", BD.getM_cellTemperatureC()));
                                tempGauge.setmCurrentVaule(BD.getM_cellTemperatureC());
//                                if (unitType) {
//                                    //tempGauge.setUnitsText(String.format("%.2f", BD.getM_cellTemperatureC()) + "°C");
//                                    float temp = (BD.getM_cellTemperatureC() + 40) / 165 * 100;
//                                    //tempGauge.setSpeed(temp, true);
//                                } else {
//                                    double f = BD.getM_cellTemperatureC() * 1.8 + 32;
//                                    //tempGauge.setUnitsText(String.format("%.2f", f) + "°F");
//                                    float temp = (BD.getM_cellTemperatureC() + 40) / 165 * 100;
//                                    //tempGauge.setSpeed(temp, true);
//                                }

                            }
                        });
                    }
                    break;
                case SystemDefinition.HOST_CMD.GET_SOC_VAL:
                    break;
                case SystemDefinition.HOST_CMD.SET_SOC_VAL:
                    SOCs[index]=parmFloat;
                    float totalSOC = 0f;
                    for(float f:SOCs){
                        if(!Double.isNaN(f))
                            totalSOC +=f;
                    }
                    BD.setM_SOC(totalSOC);
                    if(index == 0 ) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                SOC.setText(String.format("%.3f", BD.getM_SOC()));
                                if (isChargeGauge) {
                                    if(BD.getInitSOC() != 0.0) {
                                        float f = BD.getM_SOC()*100 / BD.getInitSOC();
                                        chargeGauge.setmSOC(f);
                                    }
                                }
                            }
                        });
                    }
                    break;
                case SystemDefinition.HOST_CMD.GET_SOH_VAL:
                    break;
                case SystemDefinition.HOST_CMD.SET_SOH_VAL:
                    BD.setM_SOH(parmFloat);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            SOH.setText(String.format("%.3f", BD.getM_SOH()));
                        }
                    });
                    break;
                case SystemDefinition.HOST_CMD.GET_SOF_VAL:

                    break;
                case SystemDefinition.HOST_CMD.SET_SOF_VAL:
                    BD.setM_SOF(parmFloat);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            SOF.setText(String.format("%.3f", BD.getM_SOF()));
                            if(isChargeGauge){
                                float f = BD.getM_SOF()*100 / BD.getInitSOC();
                                chargeGauge.setmSOF(f);
                            }
                        }
                    });
                    break;
                case SystemDefinition.HOST_CMD.SET_APP_VER:

                    int major = parm[0] & 0xff;
                    int minor = parm[1] & 0xff;
                    int build = ((parm[3] & 0xff) << 8) | (parm[2] & 0xff);
                    String app_ver = Integer.toString(major) + "." + Integer.toString(minor) + "." + Integer.toString(build);
                    BD.setAppVer(app_ver);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AppVer.setText(BD.getAppVer());
                            mBname.setText(getArguments().getString("mBname")+"\n FW: "+BD.getAppVer());
                        }
                    });
                    break;

                default:

                    break;

            }
    }


    public static final int ChargeStateCnt = 5;
    public static final int IgnitionStateCnt = 7;
    public static final int HeaterStateCnt = 5;
    public static final int ContactorStateCnt = 2;
    public static final int BalancerStateCnt = 3;
    public static final int SleepStateCnt = 4;
    public static final int SafeModeCnt = 2;
    private static boolean safeModeSync = false;
    public void ParseDebugCommand(int commandID,byte[] parm,int dataType,int index)
    {
        int parmInt = ByteBuffer.wrap(parm).order(ByteOrder.LITTLE_ENDIAN).getInt();
        switch (commandID){

            case SystemDefinition.DEBUG_CMD.SET_REPROGRAM_MODE:
//                if(parmInt != -1){
//                    rotationModeArray = "00000000";
//                    uniDirectionModeArray = "11111111";
//                }else {
//                    new MaterialDialog.Builder(getActivity())
//                            .title("Mode Setting Failed")
//                            .content("Fail to write settings to the Battery flash, set mode to default")
//                            .positiveText("OK")
//                            .show();
//                }
                break;

            case SystemDefinition.DEBUG_CMD.GET_REPROGRAM_MODE:
                String hexValue = Integer.toHexString(parmInt);
                if(hexValue.length()==4){
                    hexValue="0000"+hexValue;
                }
                else if(hexValue.length()==2){
                    hexValue="000000"+hexValue;
                }
                currentRotationModeArray = hexValue;
                break;
            case SystemDefinition.DEBUG_CMD.ENABLE_ACCIDENT_DETECTION:
                accidentDetectionON = parmInt;
                break;
            case SystemDefinition.DEBUG_CMD.GET_SAFEMODE_STATE:

                break;
            case SystemDefinition.DEBUG_CMD.SET_SAFEMODE_STATE:
                if(parmInt<SafeModeCnt && parmInt>=0){
                    safeModeSync = true;
                    switch (parmInt){
                        case 0:
                            BD.setM_safeModeState("OFF");
                            safeModeBtn.setOnCheckedChangeListener(null);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    safeModeBtn.setChecked(false);
                                    safeModeBtn.setOnCheckedChangeListener(mSafeModeListener);
                                }
                            });

                            break;
                        case 1:
                            BD.setM_safeModeState("ON");
                            safeModeBtn.setOnCheckedChangeListener(null);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    safeModeBtn.setChecked(true);
                                    safeModeBtn.setOnCheckedChangeListener(mSafeModeListener);
                                }
                            });


                            break;
                    }
                }

                break;
            case SystemDefinition.DEBUG_CMD.GET_CHARGE_STATE:
                break;

            case SystemDefinition.DEBUG_CMD.SET_CHARGE_STATE:

                if(parmInt<ChargeStateCnt && parmInt>=0){

                    switch (parmInt){
                        case 0:
                            BD.setM_chargerState(getString(R.string.CHARGER_INIT));
                            break;
                        case 1:
                            BD.setM_chargerState(getString(R.string.SYNC_CHARGER_ON));
                            break;
                        case 2:
                            BD.setM_chargerState(getString(R.string.CHARGER_ON));
                            break;
                        case 3:
                            BD.setM_chargerState(getString(R.string.SYNC_CHARGER_OFF));
                            break;
                        case 4:
                            BD.setM_chargerState(getString(R.string.CHARGER_OFF));
                            break;
                        default:
                            break;
                    }

                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ChargeState.setText(BD.getM_chargerState());
                    }
                });
                break;

            case SystemDefinition.DEBUG_CMD.GET_IGNITION_STATE:
                break;
            case SystemDefinition.DEBUG_CMD.SET_IGNITION_STATE:

                if(parmInt<IgnitionStateCnt && parmInt>=0){
                    switch (parmInt){
                        case 0:
                            BD.setM_ignitionState(getString(R.string.IGNITION_IDLE));
                            break;
                        case 1:
                            BD.setM_ignitionState(getString(R.string.IGNITION_START));
                            break;
                        case 2:
                            BD.setM_ignitionState(getString(R.string.ENGINE_ON_TRIAL_PERIOD));
                            break;
                        case 3:
                            BD.setM_ignitionState(getString(R.string.ENGINE_ON));
//                            getActivity().runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                        ContactorBtn.setEnabled(false);
//                                        safeModeBtn.setEnabled(false);
//                                        heaterBtn.setEnabled(false);
//                                        oltmBtn.setEnabled(false);
//                                }
//                            });

                            break;
                        case 4:
                            BD.setM_ignitionState(getString(R.string.ENGINE_OFF));
//                            getActivity().runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    if(!ContactorBtn.isEnabled()&&!safeModeBtn.isEnabled()&&!heaterBtn.isEnabled()&&!oltmBtn.isEnabled()) {
//                                        ContactorBtn.setEnabled(true);
//                                        safeModeBtn.setEnabled(true);
//                                        heaterBtn.setEnabled(true);
//                                        oltmBtn.setEnabled(true);
//                                    }
//                                }
//                            });
                            break;
                        case 5:
                            BD.setM_ignitionState(getString(R.string.IGNITION_DISENGAGED));
                            break;
                        case 6:
                            BD.setM_ignitionState(getString(R.string.IGNITION_DISABLED));
                            break;
                        default:
                            break;
                    }
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        IgnitionState.setText(BD.getM_ignitionState());
                    }
                });

                break;
            case SystemDefinition.DEBUG_CMD.GET_HEATER_STATE:
                break;
            case SystemDefinition.DEBUG_CMD.SET_HEATER_STATE:
                if(parmInt<HeaterStateCnt && parmInt>=0){
                    switch (parmInt){
                        case 0:
                            BD.setM_heaterState(getString(R.string.HEATER_INIT));
                        break;
                        case 1:
                            BD.setM_heaterState(getString(R.string.HEATER_ON));
                            break;
                        case 2:
                            BD.setM_heaterState(getString(R.string.HEATER_OFF));
                            heaterBtn.setOnCheckedChangeListener(null);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    heaterBtn.setChecked(false);
                                }
                            });
                            heaterBtn.setOnCheckedChangeListener(mHeaterListener);
                            break;
                        case 3:
                            BD.setM_heaterState(getString(R.string.HEATER_ENGAGED));
                            heaterBtn.setOnCheckedChangeListener(null);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    heaterBtn.setChecked(true);
                                }
                            });
                            heaterBtn.setOnCheckedChangeListener(mHeaterListener);
                            break;
                        case 4:
                            BD.setM_heaterState(getString(R.string.HEATER_DISENGAGED));
                            break;
                        case 5:

                            break;
                        case 6:
                            new MaterialDialog.Builder(getActivity())
                                    .title("Failed")
                                    .content("The temperature is too hot, unable turn on the heater")
                                    .positiveText("ok")
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            heaterBtn.setChecked(false);
                                        }
                                    })
                                    .show();
                            break;
                        default:
                            break;
                    }
                }
                break;
            case SystemDefinition.DEBUG_CMD.GET_CONTACTOR_STATE:
                break;
            case SystemDefinition.DEBUG_CMD.SET_CONTACTOR_STATE:
                if(parmInt<ContactorStateCnt && parmInt>=0&&safeModeSync) {
                    switch (parmInt){
                        case 0:
                            BD.setM_contactorState(getString(R.string.CONTACTOR_OPEN));
                            ContactorOpen = true;
                            if(!safeModeBtn.isChecked()&&!heaterBtn.isChecked()) {
                                ContactorBtn.setOnCheckedChangeListener(null);
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ContactorBtn.setChecked(true);
                                        ContactorBtn.setOnCheckedChangeListener(mContactorListener);
                                    }
                                });

                            }
                            break;
                        case 1:
                            BD.setM_contactorState(getString(R.string.CONTACTOR_CLOSED));
                            ContactorOpen = false;
                            if(!safeModeBtn.isChecked()&&!heaterBtn.isChecked()) {
                                ContactorBtn.setOnCheckedChangeListener(null);
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ContactorBtn.setChecked(false);
                                        ContactorBtn.setOnCheckedChangeListener(mContactorListener);
                                    }
                                });

                            }
                            break;
                        default:
                            break;
                    }


                }
                break;
            case SystemDefinition.DEBUG_CMD.GET_SLEEP_STATE:
                break;
            case SystemDefinition.DEBUG_CMD.SET_SLEEP_STATE:
                if(parmInt<SleepStateCnt && parmInt>=0) {

                    switch (parmInt){
                        case 0:
                            BD.setM_sleepState(getString(R.string.SLEEP_INIT));
                            break;
                        case 1:
                            BD.setM_sleepState(getString(R.string.SLEEP_SYNC));
                            break;
                        case 2:
                            BD.setM_sleepState(getString(R.string.SLEEP_ON));
                            break;
                        case 3:
                            BD.setM_sleepState(getString(R.string.SLEEP_OFF));
                            break;
                        default:
                            break;
                    }
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SleepState.setText(BD.getM_sleepState());
                    }
                });
                break;
            case SystemDefinition.DEBUG_CMD.GET_BALANCER_STATE:
                break;
            case SystemDefinition.DEBUG_CMD.SET_BALANCER_STATE:
                if(parmInt<BalancerStateCnt && parmInt>=0) {

                    switch (parmInt){
                        case 0:
                            BD.setM_balancerState(getString(R.string.BALANCER_INIT));
                            break;
                        case 1:
                            BD.setM_balancerState(getString(R.string.BALANCER_NOT_BALANCED));
                            break;
                        case 2:
                            BD.setM_balancerState(getString(R.string.BALANCER_BALANCED));
                            break;
                        default:
                            break;
                    }
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        BalancerState.setText(BD.getM_balancerState());
                    }
                });
                break;
            default:
                Log.v(TAG,"Wrong  CommandID");
                break;
        }

    }


    private static float C = 0.0f;
    private boolean verifing = false;
    public void ParseServiceCommand(int commandID,byte[] parm,int dataType,int index){
        float parmFloat = ByteBuffer.wrap(parm).order(ByteOrder.LITTLE_ENDIAN).getFloat();
        int parmInt = ByteBuffer.wrap(parm).order(ByteOrder.LITTLE_ENDIAN).getInt();
        switch (commandID){
            case SystemDefinition.SERVICE_CMD.SERIAL_NUMBER:
                BD.setSN(parm,index);
                if(BD.SN.Done){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            SN.setText(BD.getSN());
                        }
                    });
                    String temp = BD.getSN().substring(0,7);
                    mUser.getBatteriesList().get(batteryIdx).setmSN(temp);
                    if(!mUser.getBatteriesList().get(batteryIdx).isBonded && ! verifing ) {
                        verifing = true;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                scanBarcode(1);
                            }
                        });
                    }
                }
                break;
            case SystemDefinition.SERVICE_CMD.PART_NUMBER:
                BD.setPN(parm,index);
                if(BD.PN.Done){
                    C = SystemDefinition.getCapacity(BD.getPN());
                    BD.setInitSOC(C * nodeNum);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            PN.setText(BD.getPN());
                        }
                    });
                }
                break;
            case SystemDefinition.SERVICE_CMD.NUMBER_OF_CELLS:
                BD.setCellNum(parmInt);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        cellNum.setText(String.valueOf(BD.getCellNum()));
                    }
                });
                break;
            case SystemDefinition.SERVICE_CMD.SN_CELL:
                switch (index){
                    case 0:
                        BD.setC1SN(parmInt);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                C1SN.setText(String.valueOf(BD.getC1SN()));
                            }
                        });
                        break;
                    case 1:
                        BD.setC2SN(parmInt);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                C2SN.setText(String.valueOf(BD.getC2SN()));
                            }
                        });
                        break;
                    case 2:
                        BD.setC3SN(parmInt);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                C3SN.setText(String.valueOf(BD.getC3SN()));
                            }
                        });
                        break;
                    case 3:
                        BD.setC4SN(parmInt);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                C4SN.setText(String.valueOf(BD.getC4SN()));
                            }
                        });
                        break;
                    case 4:
                        BD.setC5SN(parmInt);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                C5SN.setText(String.valueOf(BD.getC5SN()));
                            }
                        });
                        break;
                    case 5:
                        BD.setC6SN(parmInt);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                C6SN.setText(String.valueOf(BD.getC6SN()));
                            }
                        });
                        break;
                    case 6:
                        BD.setC7SN(parmInt);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                C7SN.setText(String.valueOf(BD.getC7SN()));
                            }
                        });
                        break;
                    case 7:
                        BD.setC8SN(parmInt);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                C8SN.setText(String.valueOf(BD.getC8SN()));
                            }
                        });
                        break;
                    default:
                        break;
                }
                break;
            case SystemDefinition.SERVICE_CMD.BIRTH_DATE:
                BD.setBirthDate(parm,index);
                if(BD.birthDate.Done){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            birthData.setText(BD.getBirthDate());
                        }
                    });
                }
                break;
            case SystemDefinition.SERVICE_CMD.SUNRISE_DATE:
                BD.setSunraiseDate(parm,index);
                if(BD.sunraiseDate.Done){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            sunrisehData.setText(BD.getSunraiseDate());
                        }
                    });
                }
                break;
            case SystemDefinition.SERVICE_CMD.SUNSET_DATE:
                BD.setSunsetData(parm,index);
                if(BD.sunsetData.Done){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            sunsetData.setText(BD.getSunsetData());
                        }
                    });
                }
                break;
            case SystemDefinition.SERVICE_CMD.C_MON_CAL:
                BD.setcMonCal(parmFloat);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        cMonClaibration.setText(String.format("%.3f",BD.getcMonCal()));
                    }
                });
                break;
            case SystemDefinition.SERVICE_CMD.INITIAL_SOC:
//                if(Double.isNaN(parmFloat)){
//                    BD.setInitSOC(0.0f);
//                }else {
//                    BD.setInitSOC(parmFloat);
//                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initSOC.setText(String.format("%.3f",BD.getInitSOC()));
                    }
                });
                break;
            case SystemDefinition.SERVICE_CMD.BLE_USE_HIGH_POWER:
                BD.setBlePowerLevel(parmInt);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        blePowerLevel.setText(String.valueOf(BD.getBlePowerLevel()));
                    }
                });
                break;
            case SystemDefinition.SERVICE_CMD.CONTACTOR_SERIAL_NUMBER:
                BD.setContactorNum(parm,index);
                if(BD.contactorNum.Done){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            contactorSN.setText(BD.getContactorNum());
                        }
                    });
                }
                break;
            case SystemDefinition.SERVICE_CMD.PCB_SERIAL_NUMBER:
                BD.setPcbSN(parm,index);
                if(BD.pcbSN.Done){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pcbSN.setText(BD.getPcbSN());
                        }
                    });
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        isFirstTime = true;
        BD.reset();
    }
    private static  boolean isFirstTime = true;
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if(!isFirstTime) {
            return CubeAnimation.create(CubeAnimation.LEFT, enter, 500);
        }else{
            isFirstTime=false;
            return null;
        }
    }

    private static final int RC_BARCODE_CAPTURE = 9001;
    private static String mBarCode = "";

    private static int count = 1;
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    mBarCode = barcode.displayValue.toString();
                    Log.d(MainActivity.class.getName(), "Barcode read: " + barcode.displayValue);
                    if (((Peripheral) getActivity()).verifySN(mBarCode)) {
                               verifing = false;
                                count = 1;
                              ((Peripheral) getActivity()).startCustomAdv();

                        }
                    else{
                        count ++;
                        if(count<11) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    scanBarcode(count);
                                }
                            });
                        }
                        else {
                            verifing = false;
                            count = 1;
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    new MaterialDialog.Builder(getActivity())
                                            .title("Auth Failed")
                                            .titleColor(Color.parseColor("#ff6529"))
                                            .titleGravity(GravityEnum.CENTER)
                                            .content("Exceed 10 Attempts, Battery Deleting Current Bonding")
                                            .positiveText("OK")
                                            .positiveColor(Color.parseColor("#ff6529"))
                                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                @Override
                                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                    byte [] data = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(SystemDefinition.connectionStatus.BLE_CONNECTION_FAILED_AUTHENTICATION).array();
                                                    byte [] cmd = SystemDefinition.encodeCmd(SystemDefinition.CMOMANDSTYPE.DEBUG_COMMANDS,SystemDefinition.DEBUG_CMD.SET_BLE_CONNECTION_STATE,
                                                            data,0,0,9);
                                                    mBatteryCharacteristic.setValue(cmd);
                                                    mDelegate.sendNotificationToDevices(mBatteryCharacteristic);
                                                    getActivity().finish();
                                                }
                                            })
                                            .show();
                                }
                            });
                        }

                    }
                } else {
                    Log.d(MainActivity.class.getName(), "No barcode captured, intent data is null");
                }
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private  void scanBarcode(int attempts){
        new MaterialDialog.Builder(getActivity())
                .title("Scan Barcode")
                .titleColor(Color.parseColor("#ff6529"))
                .titleGravity(GravityEnum.CENTER)
                .content("Please use the camera to scan the barcode or manually enter the Serial Number. Attempts:"+attempts)
                .inputRangeRes(0, -1, R.color.material_drawer_dark_background)
                .input("Enter Serial Number Or Tap Scan", null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(final MaterialDialog dialog, CharSequence input) {
                        if (((Peripheral) getActivity()).verifySN(input.toString())) {
                            verifing = false;
                            count = 1;
                            ((Peripheral) getActivity()).startCustomAdv();
                        }
                        else{
                            count ++;
                            if(count<10) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        dialog.dismiss();
                                        scanBarcode(count);
                                    }
                                });
                            }
                            else {
                                verifing = false;
                                count = 1;
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        new MaterialDialog.Builder(getActivity())
                                                .title("Auth Failed")
                                                .titleColor(Color.parseColor("#ff6529"))
                                                .titleGravity(GravityEnum.CENTER)
                                                .content("Exceed 10 attempts")
                                                .positiveText("OK")
                                                .positiveColor(Color.parseColor("#ff6529"))
                                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                    @Override
                                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                        byte [] data = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(SystemDefinition.connectionStatus.BLE_CONNECTION_FAILED_AUTHENTICATION).array();
                                                        byte [] cmd = SystemDefinition.encodeCmd(SystemDefinition.CMOMANDSTYPE.DEBUG_COMMANDS,SystemDefinition.DEBUG_CMD.SET_BLE_CONNECTION_STATE,
                                                                data,0,0,9);
                                                        mBatteryCharacteristic.setValue(cmd);
                                                        mDelegate.sendNotificationToDevices(mBatteryCharacteristic);
                                                        getActivity().finish();
                                                    }
                                                })
                                                .show();
                                    }
                                });
                            }

                        }
                    }
                })
                .neutralText("SCAN")
                .neutralColor(Color.parseColor("#ff6529"))
                .negativeText("CANCEL")
                .negativeColor(Color.parseColor("#ff6529"))
                .positiveText("ENTER")
                .positiveColor(Color.parseColor("#ff6529"))
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Intent intent = new Intent(getActivity(), BarcodeCaptureActivity.class);
                        intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);
                        intent.putExtra(BarcodeCaptureActivity.UseFlash,false);
                        startActivityForResult(intent,RC_BARCODE_CAPTURE);
                    }
                })
                .cancelable(false)
                .show();
    }

    public void selectionDialog(){

        new MaterialDialog.Builder(getActivity())
                .title("Choose Mode")
                .content("The UniDirectionMode is a special mode where only one side is Active")
                .items("1. Normal Mode","2. UniDirectionMode")
                .itemsColor(Color.parseColor("#ff6529"))
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                        sequenceDialog(position);
                    }
                })
                .cancelable(false)
                .show();
    }

    public void sequenceDialog(final int type){
        if(type == SystemDefinition.BatteryMode.NormalMode) {
            switch (mStep) {
                case 0:
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                                    .title("Please Set Storage Mode")
                                    .customView(R.layout.rotation_mode_dialog, false)
                                    .positiveText("CONFIRM")
                                    .negativeText("CANCEL")
                                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            mStep = 0;
                                            selected = -1;
                                        }
                                    })
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            mStep++;
                                            rotationModeArray = rotationModeArray.substring(0, 7 - selected)
                                                    + Integer.toString(SystemDefinition.rotationMode.STORAGE_MODE) + rotationModeArray.substring(8 - selected, 8);
                                            sequenceDialog(SystemDefinition.BatteryMode.NormalMode);
                                        }
                                    })
                                    .cancelable(false)
                                    .build();

                            RadioGroup radioGroup = (RadioGroup) dialog.getCustomView().findViewById(R.id.rotationGroup);

                            final ImageView rotationImage = (ImageView) dialog.getCustomView().findViewById(R.id.batteryRotation);
                            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(RadioGroup group, int checkedId) {
                                    switch (checkedId) {
                                        case R.id.rightsideUpBtn:
                                            rotationImage.setImageResource(R.drawable.rightsideup);
                                            selected = 0;
                                            break;
                                        case R.id.upsideDownBtn:
                                            rotationImage.setImageResource(R.drawable.upsidedown);
                                            selected = 1;
                                            break;
                                        case R.id.positiveDownBtn:
                                            rotationImage.setImageResource(R.drawable.positivetermdown);
                                            selected = 2;
                                            break;
                                        case R.id.negitiveDownBtn:
                                            rotationImage.setImageResource(R.drawable.negitivetermdown);
                                            selected = 3;
                                            break;
                                        case R.id.markUpBth:
                                            rotationImage.setImageResource(R.drawable.labelup);
                                            selected = 4;
                                            break;
                                        case R.id.markDownBth:
                                            rotationImage.setImageResource(R.drawable.labledown);
                                            selected = 5;
                                            break;
                                    }
                                }
                            });

                            dialog.show();
                            radioGroup.check(R.id.positiveDownBtn);
                        }
                    });

                    break;
                case 1:
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                                    .title("Please Set BLE Toggle Mode")
                                    .customView(R.layout.rotation_mode_dialog, false)
                                    .positiveText("CONFIRM")
                                    .negativeText("CANCEL")
                                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            mStep = 0;
                                            selected = -1;
                                        }
                                    })
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                            mStep++;
                                            rotationModeArray = rotationModeArray.substring(0, 7 - selected)
                                                    + Integer.toString(SystemDefinition.rotationMode.BLE_TOGGLE_MODE) + rotationModeArray.substring(8 - selected, 8);
                                            int data = Integer.parseInt(rotationModeArray, 16);
                                            byte[] settings = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(data).array();
                                            byte[] cmd = SystemDefinition.encodeCmd(SystemDefinition.CMOMANDSTYPE.DEBUG_COMMANDS, SystemDefinition.DEBUG_CMD.SET_REPROGRAM_MODE,
                                                    settings, 0, 0, 9);
                                            mBatteryCharacteristic.setValue(cmd);
                                            mDelegate.sendNotificationToDevices(mBatteryCharacteristic);
                                            sequenceDialog(SystemDefinition.BatteryMode.NormalMode);
                                        }
                                    })
                                    .cancelable(false)
                                    .build();

                            RadioGroup radioGroup = (RadioGroup) dialog.getCustomView().findViewById(R.id.rotationGroup);
                            final ImageView rotationImage = (ImageView) dialog.getCustomView().findViewById(R.id.batteryRotation);
                            radioGroup.getChildAt(selected).setEnabled(false);

                            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(RadioGroup group, int checkedId) {
                                    switch (checkedId) {
                                        case R.id.rightsideUpBtn:
                                            rotationImage.setImageResource(R.drawable.rightsideup);
                                            selected = 0;
                                            break;
                                        case R.id.upsideDownBtn:
                                            rotationImage.setImageResource(R.drawable.upsidedown);
                                            selected = 1;
                                            break;
                                        case R.id.positiveDownBtn:
                                            rotationImage.setImageResource(R.drawable.positivetermdown);
                                            selected = 2;
                                            break;
                                        case R.id.negitiveDownBtn:
                                            rotationImage.setImageResource(R.drawable.negitivetermdown);
                                            selected = 3;
                                            break;
                                        case R.id.markUpBth:
                                            rotationImage.setImageResource(R.drawable.labelup);
                                            selected = 4;
                                            break;
                                        case R.id.markDownBth:
                                            rotationImage.setImageResource(R.drawable.labledown);
                                            selected = 5;
                                            break;
                                    }
                                }
                            });

                            dialog.show();
                            radioGroup.check(R.id.negitiveDownBtn);
                        }
                    });

                    break;
                case 2:
                    mStep = 0;
                    selected = -1;
                    rotationModeArray = "00000000";
                    uniDirectionModeArray = "11111111";
                    //uniDirectionMode = false;
                    //accidentDetectionON = false;
                    break;
                default:
                    break;
            }
        }
        else{
            switch (mStep) {
                case 0:
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                          new MaterialDialog.Builder(getActivity())
                                  .title("Do you want enable accident detection mode?")
                                  .positiveText("YES")
                                  .negativeText("NO")
                                  .cancelable(false)
                                  .onPositive(new MaterialDialog.SingleButtonCallback() {
                                      @Override
                                      public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                          mStep++;
                                          byte[] cmd = SystemDefinition.encodeCmd(SystemDefinition.CMOMANDSTYPE.DEBUG_COMMANDS,SystemDefinition.DEBUG_CMD.ENABLE_ACCIDENT_DETECTION,
                                                  ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN ).putInt(1).array(),0,0,9);
                                          mBatteryCharacteristic.setValue(cmd);
                                          mDelegate.sendNotificationToDevices(mBatteryCharacteristic);
                                          sequenceDialog(SystemDefinition.BatteryMode.UniDirectionMode);
                                          //accidentDetectionON = true;
                                      }
                                  })
                                  .onNegative(new MaterialDialog.SingleButtonCallback() {
                                      @Override
                                      public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                          mStep++;
                                          byte[] cmd = SystemDefinition.encodeCmd(SystemDefinition.CMOMANDSTYPE.DEBUG_COMMANDS,SystemDefinition.DEBUG_CMD.ENABLE_ACCIDENT_DETECTION,
                                                  ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN ).putInt(0).array(),0,0,9);
                                          mBatteryCharacteristic.setValue(cmd);
                                          mDelegate.sendNotificationToDevices(mBatteryCharacteristic);
                                          sequenceDialog(SystemDefinition.BatteryMode.UniDirectionMode);
                                          //accidentDetectionON = false;
                                      }
                                  })
                                  .cancelable(false)
                                  .show();
                        }
                    });

                    break;
                case 1:
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                                    .title("Please Select One Direction As Active Mode")
                                    .customView(R.layout.rotation_mode_dialog, false)
                                    .positiveText("CONFIRM")
                                    .negativeText("CANCEL")
                                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            mStep = 0;
                                            selected = -1;
                                        }
                                    })
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            mStep++;
                                            uniDirectionModeArray = uniDirectionModeArray.substring(0, 7 - selected)
                                                    + Integer.toString(SystemDefinition.rotationMode.ACTIVE_MODE) + uniDirectionModeArray.substring(8 - selected, 8);
                                            int data = Integer.parseInt(uniDirectionModeArray, 16);
                                            byte[] settings = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(data).array();
                                            byte[] cmd = SystemDefinition.encodeCmd(SystemDefinition.CMOMANDSTYPE.DEBUG_COMMANDS, SystemDefinition.DEBUG_CMD.SET_REPROGRAM_MODE,
                                                    settings, 0, 0, 9);
                                            mBatteryCharacteristic.setValue(cmd);
                                            mDelegate.sendNotificationToDevices(mBatteryCharacteristic);
                                            sequenceDialog(SystemDefinition.BatteryMode.UniDirectionMode);
                                        }
                                    })
                                    .cancelable(false)
                                    .build();

                            RadioGroup radioGroup = (RadioGroup) dialog.getCustomView().findViewById(R.id.rotationGroup);
                            final ImageView rotationImage = (ImageView) dialog.getCustomView().findViewById(R.id.batteryRotation);
                            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(RadioGroup group, int checkedId) {
                                    switch (checkedId) {
                                        case R.id.rightsideUpBtn:
                                            rotationImage.setImageResource(R.drawable.rightsideup);
                                            selected = 0;
                                            break;
                                        case R.id.upsideDownBtn:
                                            rotationImage.setImageResource(R.drawable.upsidedown);
                                            selected = 1;
                                            break;
                                        case R.id.positiveDownBtn:
                                            rotationImage.setImageResource(R.drawable.positivetermdown);
                                            selected = 2;
                                            break;
                                        case R.id.negitiveDownBtn:
                                            rotationImage.setImageResource(R.drawable.negitivetermdown);
                                            selected = 3;
                                            break;
                                        case R.id.markUpBth:
                                            rotationImage.setImageResource(R.drawable.labelup);
                                            selected = 4;
                                            break;
                                        case R.id.markDownBth:
                                            rotationImage.setImageResource(R.drawable.labledown);
                                            selected = 5;
                                            break;
                                    }
                                }
                            });

                            dialog.show();
                            radioGroup.check(R.id.rightsideUpBtn);

                        }
                    });

                    break;
                case 2:

                    mStep = 0;
                    selected = -1;
                    rotationModeArray = "00000000";
                    uniDirectionModeArray = "11111111";
                    //uniDirectionMode = true;

                    break;
                default:
                    break;
            }
        }


    }

    public void rotationModeDialog(String rotationMode){

        String title = null;
        
        if(rotationMode == null&&rotationMode.length()!=8)
            return;

        if (rotationMode.charAt(1) == '1') {
            title = "uniDirectionMode";
        } else {
            title = "Normal Mode";
        }

        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
            .title(title)
            .customView(R.layout.rotation_mode_display_dialog, false)
            .cancelable(false)
            .positiveText("CONFIRM")
            .dismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    currentMode.setChecked(false);
                }
            })
            .build();
        TextView rightSideUpMode = (TextView) dialog.getCustomView().findViewById(R.id.rightSideUpMode);
        TextView upsideDownMode = (TextView) dialog.getCustomView().findViewById(R.id.upsideDownMode);
        TextView positvieDownMode = (TextView) dialog.getCustomView().findViewById(R.id.positiveDownMode);
        TextView negitiveDownMode = (TextView) dialog.getCustomView().findViewById(R.id.negitiveDownMode);
        TextView labelUpMode = (TextView) dialog.getCustomView().findViewById(R.id.labelUpMode);
        TextView labelDownMode = (TextView) dialog.getCustomView().findViewById(R.id.labelDownMode);
        TextView accidentDetection = (TextView) dialog.getCustomView().findViewById(R.id.accidentDetection);

        if(rotationMode.charAt(1)=='1' && accidentDetectionON == 1){
            accidentDetection.setText("Accident Detection Mode On");
        }else {
            accidentDetection.setText("Accident Detection Mode Off");
        }
        if(rotationMode != null ) {

            if(rotationMode.charAt(7) == '0'){
                rightSideUpMode.setText("ACTIVE");
            }else if(rotationMode.charAt(7) == '1'){
                rightSideUpMode.setText("STORAGE");
            }
            else if(rotationMode.charAt(7) == '2'){
                rightSideUpMode.setText("BLE_TOGGLE");
            }

            if(rotationMode.charAt(6) == '0'){
                upsideDownMode.setText("ACTIVE");
            }else if(rotationMode.charAt(6) ==  '1'){
                upsideDownMode.setText("STORAGE");
            }
            else if(rotationMode.charAt(6) ==  '2'){
                upsideDownMode.setText("BLE_TOGGLE");
            }

            if(rotationMode.charAt(5) ==  '0'){
                positvieDownMode.setText("ACTIVE");
            }else if(rotationMode.charAt(5) ==  '1'){
                positvieDownMode.setText("STORAGE");
            }
            else if(rotationMode.charAt(5) == '2'){
                positvieDownMode.setText("BLE_TOGGLE");
            }

            if(rotationMode.charAt(4) ==  '0'){
                negitiveDownMode.setText("ACTIVE");
            }else if(rotationMode.charAt(4) ==  '1'){
                negitiveDownMode.setText("STORAGE");
            }
            else if(rotationMode.charAt(4) ==  '2'){
                negitiveDownMode.setText("BLE_TOGGLE");
            }

            if(rotationMode.charAt(3) ==  '0'){
                labelUpMode.setText("ACTIVE");
            }else if(rotationMode.charAt(3) ==  '1'){
                labelUpMode.setText("STORAGE");
            }
            else if(rotationMode.charAt(3) ==  '2'){
                labelUpMode.setText("BLE_TOGGLE");
            }

            if(rotationMode.charAt(2) ==  '0'){
                labelDownMode.setText("ACTIVE");
            }else if(rotationMode.charAt(2) ==  '1'){
                labelDownMode.setText("STORAGE");
            }
            else if(rotationMode.charAt(2) ==  '2'){
                labelDownMode.setText("BLE_TOGGLE");
            }
        }
        dialog.show();
    }


    public boolean isTabletDevice() {
        TelephonyManager telephony = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
        int type = telephony.getPhoneType();
        if (type == TelephonyManager.PHONE_TYPE_NONE) {
            return true;
        } else {
            return false;
        }
    }
}

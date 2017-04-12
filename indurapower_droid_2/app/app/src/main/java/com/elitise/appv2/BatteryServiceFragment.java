/*
 * Copyright 2015 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import android.support.v7.widget.SwitchCompat;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.labo.kaji.fragmentanimations.CubeAnimation;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import static com.elitise.appv2.SystemDefinition.chargeGain.m_highGainVal;
import static com.elitise.appv2.SystemDefinition.chargeGain.m_lowGainVal;
import static com.elitise.appv2.SystemDefinition.chargeGain.m_rdsOnVal;


public class BatteryServiceFragment extends ServiceFragment{

//    private static final UUID ELT_BATT_MOBBAT_SERVICE_UUID = UUID
//            .fromString("AC55D02D-A7FE-4F64-A659-ABCB5C6D4099");
//
//
//    private static final UUID ELT_BATT_MOBBAT_CHARACTERISTIC_UUID = UUID
//            .fromString("788C5B46-B899-473A-96C9-49B981739482");

    private static final UUID ELT_BATT_MOBBAT_SERVICE_UUID = UUID
            .fromString("AC55D02D-A7FE-4F64-A659-ABCB5C6D409D");


    private static final UUID ELT_BATT_MOBBAT_CHARACTERISTIC_UUID = UUID
            .fromString("A88C5B46-B899-473A-96C9-49B98173948D");



    private ServiceFragmentDelegate mDelegate;

    private boolean isTablet = false;

    private MaterialDialog blockDialog;
    private boolean connection = false;
    private Timer connectionTimer = new Timer();
    private TimerTask timerTask ;

    private  BatteryData BD;
    private  UserData mUser = UserData.getInstance();
    private  int batteryIdx;
    private  TextView statusBar;
    //   private  UserData mUser;
 //   private  int bIndex;

    private SharedPreferences sharedPrefs;
    private boolean unitType;




    private SwitchCompat safeModeBtn;
    private SwitchCompat oneLastStart;
    private SwitchCompat contactorBtn;
    private SwitchCompat heaterBtn;
    private SwitchCompat rotationBtn;
    private SwitchCompat deepCycleBtn;


    private CompoundButton.OnCheckedChangeListener mContactorListener;
    private CompoundButton.OnCheckedChangeListener mSafeModeListener;
    private CompoundButton.OnCheckedChangeListener mHeaterListener;
    private CompoundButton.OnCheckedChangeListener mDeepCycleListener;

    private View.OnClickListener mDiagModeListener;

    private TextView BankV;

    private TextView BusV;

    private TextView mBname;

    private TextView SOC2;

    private float totalSOC = 0.0f;
    private float totalCMON = 0.0f;

    private TextView VOLT;
    private int diagModeCount = 0;
    private String password = "Elitise1";


    private RecyclerView subModuleContainer;

    private  int mStep = 0;
    private int selected = -1;
    private String rotationModeArray = "00000000";
    private String currentRotationModeArray = null;


    private ChargerGaugeView chargeGauge;
    private TempGaugeView tempGauge;

    private  ConnectLedView connectionView;
    //private  ImageView mTempImage;
    private TextView useAbleBatteryCap;


    private  int nodeNum = 1 ;
    private Dialog alertDialog ;
    private ImageView chargeAnimationBtn;
    private CurrentChargeStylekitView currentChargeStylekitView;


    private  boolean isChargeGauge = true;
    private  boolean demoPowerOff = false;
    private  boolean demoLowPower = false;

    private Button showGaugeBtn;

    private  ArrayList<SubModuleData> mSubData;
    private SubModuleAdapter mAdapter;
    private boolean isDemo;
    // GATT
    private BluetoothGattService mBatteryService;
    private BluetoothGattCharacteristic mBatteryCharacteristic;
    public BatteryServiceFragment() {
        mBatteryCharacteristic =
                new BluetoothGattCharacteristic(ELT_BATT_MOBBAT_CHARACTERISTIC_UUID,
                        BluetoothGattCharacteristic.PROPERTY_READ|BluetoothGattCharacteristic.PROPERTY_NOTIFY|BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
                        BluetoothGattCharacteristic.PERMISSION_WRITE|BluetoothGattCharacteristic.PERMISSION_READ);
        mBatteryCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        mBatteryCharacteristic.addDescriptor(
                Peripheral.getClientCharacteristicConfigurationDescriptor());

        mBatteryService = new BluetoothGattService(ELT_BATT_MOBBAT_SERVICE_UUID,
                BluetoothGattService.SERVICE_TYPE_PRIMARY);
        mBatteryCharacteristic.setValue("test");
        mBatteryService.addCharacteristic(mBatteryCharacteristic);

        this.setRetainInstance(true);
    }


    //Lifecycle callbacks
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        isTablet = isTabletDevice();
        isDemo = mUser.getBatteriesList().get(batteryIdx).isDemo;
        final View  view = inflater.inflate(R.layout.fragment_battery, container, false);


        BD = BatteryData.getInstance();
        batteryIdx = getArguments().getInt("bIndex");

        if(mUser.getBatteriesList().get(batteryIdx).isDemo){
            TextView view0 = (TextView)view.findViewById(R.id.textView0);
            view0.setVisibility(View.VISIBLE);
            View view1 = view.findViewById(R.id.Viewff);
            view1.setVisibility(View.VISIBLE);
            SwitchCompat startEngineBtn = (SwitchCompat)view.findViewById(R.id.startEngineBtn);
            startEngineBtn.setVisibility(View.VISIBLE);
            startEngineBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                   if(isChecked) {
                       mUser.getBatteriesList().get(batteryIdx).engineState = 21;

                               contactorBtn.setEnabled(false);
                               safeModeBtn.setEnabled(false);
                               heaterBtn.setEnabled(false);
                               oneLastStart.setEnabled(false);
                               deepCycleBtn.setEnabled(false);
                               rotationBtn.setEnabled(false);

                   }
                   else {
                       mUser.getBatteriesList().get(batteryIdx).engineState = 20;
                       contactorBtn.setEnabled(true);
                       safeModeBtn.setEnabled(true);
                       heaterBtn.setEnabled(true);
                       oneLastStart.setEnabled(true);
                       deepCycleBtn.setEnabled(true);
                       rotationBtn.setEnabled(true);
                   }
                }
            });

        }else {
            TextView view0 = (TextView)view.findViewById(R.id.textView0);
            view0.setVisibility(View.GONE);
            View view1 = view.findViewById(R.id.Viewff);
            view1.setVisibility(View.GONE);
            SwitchCompat startEngineBtn = (SwitchCompat)view.findViewById(R.id.startEngineBtn);
            startEngineBtn.setVisibility(View.GONE);
        }

        //load unitType
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if(sharedPrefs.getString(getString(R.string.pref_units_key),getString(R.string.pref_units_imperial)).equals(getString(R.string.pref_units_imperial))){
            unitType = false;
        }
        else {
            unitType = true;
        }


        blockDialog =  new MaterialDialog.Builder(getActivity())
                .theme(Theme.LIGHT)
                .autoDismiss(false)
                .content("Entering Power OFF Mode, please wait for confirmation")
                .build();

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

        if (!isTablet) {
            final LinearLayout topGaugeLayout = (LinearLayout) view.findViewById(R.id.topGaugeLayout);
            topGaugeLayout.setVisibility(View.GONE);
            LinearLayout topPhoneWithoutGauge = (LinearLayout) view.findViewById(R.id.topPhoneWithoutGauge);
            topPhoneWithoutGauge.setVisibility(View.VISIBLE);
            mBname = (TextView) view.findViewById(R.id.BatteryName2);
            connectionView = (ConnectLedView) view.findViewById(R.id.connectionView2);
            showGaugeBtn.setVisibility(View.VISIBLE);
            showGaugeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if( mUser.getBatteriesList().get(batteryIdx).engineState == 21) {
                        mUser.getBatteriesList().get(batteryIdx).engineState = 1;
                        contactorBtn.setEnabled(false);
                        safeModeBtn.setEnabled(false);
                        heaterBtn.setEnabled(false);
                        oneLastStart.setEnabled(false);
                        deepCycleBtn.setEnabled(false);
                        rotationBtn.setEnabled(false);

                    }
                    else if (mUser.getBatteriesList().get(batteryIdx).engineState == 20) {
                        mUser.getBatteriesList().get(batteryIdx).engineState = 0;
                        contactorBtn.setEnabled(true);
                        safeModeBtn.setEnabled(true);
                        heaterBtn.setEnabled(true);
                        oneLastStart.setEnabled(true);
                        deepCycleBtn.setEnabled(true);
                        rotationBtn.setEnabled(true);

                    }
                    ((Peripheral) getActivity()).enableShowGauge();
                }
            });
            if(getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                chargeGauge = (ChargerGaugeView) view.findViewById(R.id.chargegauge2);
                tempGauge = (TempGaugeView) view.findViewById(R.id.tempgauge2);
            }else{
                chargeGauge = (ChargerGaugeView) view.findViewById(R.id.chargegauge);
                tempGauge = (TempGaugeView) view.findViewById(R.id.tempgauge);
            }
        } else {

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

        BankV = (TextView) view.findViewById(R.id.batteryVolt);
        BusV = (TextView) view.findViewById(R.id.aVolt);//

        useAbleBatteryCap = (TextView) view.findViewById(R.id.bStatus2);


//        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//            @Override
//            public void onDismiss(DialogInterface dialog) {
//                currentChargeStylekitView.stopAnimation();
//            }
//        });



        //currentChargeStylekitView.startAnimation();
        chargeAnimationBtn = (ImageView) view.findViewById(R.id.chargerAnimation);
        chargeAnimationBtn.bringToFront();
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


        SOC2 = (TextView) view.findViewById(R.id.bStatus);

        VOLT = (TextView) view.findViewById(R.id.volt);


        //set logo green
        //mTempImage = (ImageView) view.findViewById(R.id.mTempImage);

        //chargeGauge = (ChargerGaugeView) view.findViewById(R.id.chargegauge);

        chargeGauge.setmSOC(50f);




        //tempGauge = (TempGaugeView) view.findViewById(R.id.tempgauge);


        tempGauge.initGauge(unitType);



        tempGauge.setmCurrentVaule(42.5f);


        statusBar = (TextView) getActivity().findViewById(R.id.statusBar);

        //btn

        contactorBtn = (SwitchCompat) view.findViewById(R.id.Engage);

        mContactorListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    if (!safeModeBtn.isChecked()) {
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
                                       if(!isDemo) {
                                           mBatteryCharacteristic.setValue(SystemDefinition.OpenContactor);
                                           mDelegate.sendNotificationToDevices(mBatteryCharacteristic);
                                       }else{
                                           demoLowPower = true;
                                       }

                                    }
                                })
                                .onNegative(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        contactorBtn.setChecked(false);
                                    }
                                })
                                .show();
                        statusBar.setText("Status: Low Power Mode");
                    } else {
                        new MaterialDialog.Builder(getActivity())
                                .theme(Theme.LIGHT)
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
                    if(!isDemo) {
                        mBatteryCharacteristic.setValue(SystemDefinition.CloseContactor);
                        mDelegate.sendNotificationToDevices(mBatteryCharacteristic);
                    }else{
                        demoLowPower = false;
                    }
                    statusBar.setText("Status: Normal Mode");
                }
            }
        };
        contactorBtn.setOnCheckedChangeListener(mContactorListener);


        safeModeBtn = (SwitchCompat) view.findViewById(R.id.batteryState);
        mSafeModeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    if (!contactorBtn.isChecked()) {
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
                                        if(!isDemo) {
                                            mBatteryCharacteristic.setValue(SystemDefinition.safeModeOn);
                                            mDelegate.sendNotificationToDevices(mBatteryCharacteristic);
                                            blockDialog.show();
                                        }else{
                                           demoPowerOff = true;
                                        }
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
                    } else {
                        new MaterialDialog.Builder(getActivity())
                                .theme(Theme.LIGHT)
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
                    if(!isDemo) {
                        mBatteryCharacteristic.setValue(SystemDefinition.safeModeOff);
                        mDelegate.sendNotificationToDevices(mBatteryCharacteristic);
                    }else{
                         demoPowerOff = false;
                    }
                    statusBar.setText("Status: Normal Mode");
                }
            }
        };
        safeModeBtn.setOnCheckedChangeListener(mSafeModeListener);

        oneLastStart = (SwitchCompat) view.findViewById(R.id.oneLastMode);

        oneLastStart.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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
                                    if(!isDemo) {
                                        mBatteryCharacteristic.setValue(SystemDefinition.OneLastStartOn);
                                        mDelegate.sendNotificationToDevices(mBatteryCharacteristic);
                                        oneLastStart.setChecked(true);
                                        oneLastStart.setEnabled(false);
                                        Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            public void run() {
                                                oneLastStart.setChecked(false);
                                                oneLastStart.setEnabled(true);
                                                statusBar.setText("Status: Normal Mode");
                                            }
                                        }, 30000);
                                    }
                                }
                            })
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    oneLastStart.setChecked(false);
                                }
                            })
                            .show();
                    statusBar.setText("Status: One Last Start Mode");
                }
            }
        });

        heaterBtn = (SwitchCompat) view.findViewById(R.id.heaterBtn);
        mHeaterListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
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
                                    if(!isDemo) {
                                        mBatteryCharacteristic.setValue(SystemDefinition.HeaterOn);
                                        mDelegate.sendNotificationToDevices(mBatteryCharacteristic);
                                    }else{
                                        mUser.getBatteriesList().get(batteryIdx).heaterOn = true;
                                    }
                                }
                            })
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    heaterBtn.setChecked(false);
                                }
                            })
                            .show();
                    statusBar.setText("Status: heater On");

                } else {
                    if(!isDemo) {
                        mBatteryCharacteristic.setValue(SystemDefinition.HeaterOff);
                        mDelegate.sendNotificationToDevices(mBatteryCharacteristic);
                    }else{
                        mUser.getBatteriesList().get(batteryIdx).heaterOn = false;
                    }
                    statusBar.setText("Status: Normal Mode");
                }

            }
        };
        heaterBtn.setOnCheckedChangeListener(mHeaterListener);

        chargeGauge.setChargeListener(new ChargerGaugeView.ChargeGaugeListener() {
            @Override
            public void onGaugeSwipe(boolean type) {
                isChargeGauge = type;
            }
        });
        tempGauge.setTempListener(new TempGaugeView.TempGaugeListener() {
            @Override
            public void onGaugeSwipe(boolean type) {
                unitType = type;
                if (unitType) {
                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.putString(getString(R.string.pref_units_key), getString(R.string.pref_units_metric));
                    editor.apply();
                    mAdapter.changeTempLebal(unitType);

                } else {
                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.putString(getString(R.string.pref_units_key), getString(R.string.pref_units_imperial));
                    editor.apply();
                    mAdapter.changeTempLebal(unitType);
                }
            }
        });




        //load the master node
        mSubData = new ArrayList<>();
        SubModuleData moduleData = new SubModuleData(unitType);
        moduleData.setModuleNum(1);
        mSubData.add(moduleData);
        mAdapter = new SubModuleAdapter(mSubData);

        subModuleContainer = (RecyclerView) view.findViewById(R.id.submoduleContainer);
        subModuleContainer.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        subModuleContainer.setAdapter(mAdapter);

        if(!mUser.getBatteriesList().get(batteryIdx).isBonded && mUser.getBatteriesList().get(batteryIdx).getmSN()!= null ) {
            scanBarcode(1);
        }

        mDiagModeListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(++diagModeCount>2){
                    diagModeCount = 0;
                    new MaterialDialog.Builder(getActivity())
                            .title("Enter Password")
                            .titleColor(Color.parseColor("#ff6529"))
                            .cancelable(false)
                            .titleGravity(GravityEnum.CENTER)
                            .content("Please Enter Password For Diagnostics Mode")
                            .inputRangeRes(0, -1, R.color.md_red_500)
                            .input("", "", new MaterialDialog.InputCallback() {
                                @Override
                                public void onInput(MaterialDialog dialog, CharSequence input) {
                                    if(input.toString().equals(password)){
                                        byte[] cmd = SystemDefinition.encodeCmd(SystemDefinition.CMOMANDSTYPE.HOST_COMMANDS,SystemDefinition.HOST_CMD.SET_DIAG_MODE,
                                                ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN ).putInt(1).array(),0,0,9);
                                        mBatteryCharacteristic.setValue(cmd);
                                        mDelegate.sendNotificationToDevices(mBatteryCharacteristic);
                                        ((Peripheral)getActivity()).enableDiagMode();
                                    }else{
                                        new MaterialDialog.Builder(getActivity())
                                                .title("Failed")
                                                .content("Wrong Password")
                                                .positiveText("OK")
                                                .show();
                                    }
                                }
                            })
                            .positiveText("Confirm")
                            .show();
                }
            }
        };


        rotationBtn = (SwitchCompat) view.findViewById(R.id.rotationModeBtn);
        rotationBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    sequenceDialog(0);
                    buttonView.setChecked(false);
                }
            }
        });

        deepCycleBtn = (SwitchCompat) view.findViewById(R.id.deepCycleBtn);
        mDeepCycleListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    oneLastStart.setEnabled(false);
//                    byte[] cmd = SystemDefinition.encodeCmd(SystemDefinition.CMOMANDSTYPE.DEBUG_COMMANDS, SystemDefinition.DEBUG_CMD.SET_DEEP_CYCLE_MODE,
//                    ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(0).array(), 0, 0, 9);
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
                    oneLastStart.setChecked(false);
                    oneLastStart.setEnabled(true);
                    byte[] cmd = SystemDefinition.encodeCmd(SystemDefinition.CMOMANDSTYPE.DEBUG_COMMANDS, SystemDefinition.DEBUG_CMD.SET_DEEP_CYCLE_MODE,
                            ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(0).array(), 0, 0, 9);
                    mBatteryCharacteristic.setValue(cmd);
                    mDelegate.sendNotificationToDevices(mBatteryCharacteristic);
                }
            }
        };
        deepCycleBtn.setOnCheckedChangeListener(mDeepCycleListener);

        if(!mUser.getBatteriesList().get(batteryIdx).isDemo){
            contactorBtn.setEnabled(false);
            safeModeBtn.setEnabled(false);
            heaterBtn.setEnabled(false);
            oneLastStart.setEnabled(false);
            deepCycleBtn.setEnabled(false);
            rotationBtn.setEnabled(false);
        }else{
            contactorBtn.setEnabled(true);
            safeModeBtn.setEnabled(true);
            heaterBtn.setEnabled(true);
            oneLastStart.setEnabled(true);
            deepCycleBtn.setEnabled(true);
            rotationBtn.setEnabled(true);
        }

        timerTask = new TimerTask() {
            @Override
            public void run() {
                //connection = false;
                updateConnection(false);
            }
        };



        CustomLinearLayoutManager customLayoutManager = new CustomLinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false);
        subModuleContainer.setLayoutManager(customLayoutManager);

        if(!mUser.getBatteriesList().get(batteryIdx).isDemo) {
            connectionTimer.schedule(timerTask, 5000);
        }else{
            if(!isTablet) {
                startDemo();
            }else{
                startDemo2();
            }
            connectionView.setLedColor(Color.GREEN);
        }

        return view;
    }


    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try{
            mDelegate = (ServiceFragmentDelegate) activity;
        }catch (ClassCastException e){
            throw new ClassCastException(activity.toString());
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

        mBatteryService = new BluetoothGattService(UUID.fromString(SystemDefinition.ELT_BATT_MOBBAT_SERVICE_UUID_WITH_SN),
                BluetoothGattService.SERVICE_TYPE_PRIMARY);


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

    static  boolean isFirst = true;
    public void updateConnection(final boolean c) {
        if(getActivity() == null )
            return;



        if(c == true){
            //Log.v(TAG, "Obs: Connect");
            if(!connection) {
                connection = true;
                //((Peripheral) getActivity()).createBond();
            }

            if(mUser.getBatteriesList().get(batteryIdx).mImageId == 12 && mUser.getBatteriesList().get(batteryIdx).isBonded  && isFirst){// enable deep cycle mode
                isFirst = false;
                byte[] cmd = SystemDefinition.encodeCmd(SystemDefinition.CMOMANDSTYPE.DEBUG_COMMANDS, SystemDefinition.DEBUG_CMD.SET_DEEP_CYCLE_MODE,
                        ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(1).array(), 0, 0, 9);
                mBatteryCharacteristic.setValue(cmd);
                mDelegate.sendNotificationToDevices(mBatteryCharacteristic);

            }else if(isFirst && mUser.getBatteriesList().get(batteryIdx).mImageId != 12 && mUser.getBatteriesList().get(batteryIdx).isBonded ){

                isFirst = false;
                byte[] cmd = SystemDefinition.encodeCmd(SystemDefinition.CMOMANDSTYPE.DEBUG_COMMANDS, SystemDefinition.DEBUG_CMD.SET_DEEP_CYCLE_MODE,
                        ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(0).array(), 0, 0, 9);
                mBatteryCharacteristic.setValue(cmd);
                mDelegate.sendNotificationToDevices(mBatteryCharacteristic);

            }

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    connectionView.setLedColor(Color.GREEN);
                    if(!VOLT.hasOnClickListeners()) {
                        VOLT.setOnClickListener(mDiagModeListener);
                    }
                }
            });

        }
        else {
            //Log.v(TAG,"Obs: Disonnect");
            connection = false;
            BD.reset();
            mAdapter.resetAlldata(unitType);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    connectionView.setLedColor(Color.RED);
                    BankV.setText("0.0");
                    BusV.setText("0.0");

                    SOC2.setText("0.0");

                    useAbleBatteryCap.setText("0.0");
                    tempGauge.setmCurrentVaule(42.5f);
                    tempGauge.reset();
//
                    chargeGauge.setmSOC(50f);
                    chargeGauge.reset();
                    VOLT.setText("0.0V");
                    contactorBtn.setEnabled(false);
                    safeModeBtn.setEnabled(false);
                    heaterBtn.setEnabled(false);
                    oneLastStart.setEnabled(false);
                    deepCycleBtn.setEnabled(false);
                    rotationBtn.setEnabled(false);
                    mAdapter.notifyDataSetChanged();

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
        //Log.v(TAG, "parsing: "+ commandClass+" CID:"+commandID+ "  Parm: "+ parameter + " index : "+index + " dataType :" + dataType);
        if(commandClass == SystemDefinition.CMOMANDSTYPE.HOST_COMMANDS){
            if(mUser.getBatteriesList().get(batteryIdx).isBonded) {
                ParseHostCommand(commandID, parameter, dataType, index);
            }
        }
        else if (commandClass == SystemDefinition.CMOMANDSTYPE.DEBUG_COMMANDS){
            if(mUser.getBatteriesList().get(batteryIdx).isBonded) {
                ParseDebugCommand(commandID, parameter, dataType, index);
            }
        }
        else if (commandClass == SystemDefinition.CMOMANDSTYPE.SERVICE_COMMANDS){
            if(mUser.getBatteriesList().get(batteryIdx).isBonded) {
                ParseServiceCommand(commandID, parameter, dataType, index);
            }
            else {
                if(commandID == SystemDefinition.SERVICE_CMD.SERIAL_NUMBER){
                    ParseServiceCommand(commandID, parameter, dataType, index);
                }
            }
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
    private static boolean safeModeSnyc = false;

    public void ParseHostCommand(int commandID, byte[] parm, int dataType, final int index)
    {
        float parmFloat = ByteBuffer.wrap(parm).order(ByteOrder.LITTLE_ENDIAN).getFloat();
        int parmInt = ByteBuffer.wrap(parm).order(ByteOrder.LITTLE_ENDIAN).getInt();
        switch (commandID) {
            case SystemDefinition.HOST_CMD.GET_I_CHARGE:
                break;
            case SystemDefinition.HOST_CMD.SET_I_CHARGE:

                BD.setM_I_CHGR(parmFloat * (float) 3.333);
                break;
            case SystemDefinition.HOST_CMD.GET_C_MON:
                break;
            case SystemDefinition.HOST_CMD.SET_C_MON:
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
                //chargingGain = (parmFloat > (float) 1.2206 ? (float) 104 : (float) 29.369);

                totalCMON = 0.0f;
                for(int i = 0;i<nodeNum;i++){
                    totalCMON+=mAdapter.getDatabyIdx(i).getC_MON_AMP();
                    BD.setM_C_MON_amps(totalCMON);
                }
                if (index < mAdapter.getItemCount()) {
                    mAdapter.getDatabyIdx(index).setC_MON_AMP(((float) 1.2206 - parmFloat) / ((float) m_rdsOnVal * chargingGain));
                }
                if(currentChargeStylekitView!=null) {
                    if (BD.getM_C_MON_amps() < 0 ) {
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
                        if (index == 0) {
                            if (!isChargeGauge) {
                                //double f = (BD.getM_C_MON_amps() + 100) / 2;
                                chargeGauge.setmSOC(BD.getM_C_MON_amps());
                                if(currentChargeStylekitView!=null) {
                                    currentChargeStylekitView.setmValue(BD.getM_C_MON_amps());
                                }
//                                chargeGauge.setSpeed(f, true);
//                                chargeGauge.setUnitsText(String.format("%.1f", BD.getM_C_MON_amps()) + " A");
                            }
                        }
                    }
                });
                break;
            case SystemDefinition.HOST_CMD.GET_BUS_MON:
                break;
            case SystemDefinition.HOST_CMD.SET_BUS_MON:
                BD.setM_BUS_MON(parmFloat);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        BusV.setText(String.format("%.1f", BD.getM_BUS_MON()));
                        //VOLT.setText(String.format("%.1f", BD.getM_batteryVoltage()) + "V");
                    }
                });
                break;
            case SystemDefinition.HOST_CMD.GET_BANK_V:
                break;
            case SystemDefinition.HOST_CMD.SET_BANK_V:
                final float val = parmFloat;
                if (index == 0) {
                    BD.setM_batteryVoltage(parmFloat);
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (index == 0) {
                            BankV.setText(String.format("%.1f", BD.getM_batteryVoltage()));
                            VOLT.setText(String.format("%.1f", BD.getM_batteryVoltage()) + "V");
                        }
                        if (index < mAdapter.getItemCount()) {
                            mAdapter.getDatabyIdx(index).setBankV(val);
                            //mAdapter.notifyDataSetChanged();
                        }
                    }
                });
                break;
            case SystemDefinition.HOST_CMD.GET_FUEL_CELL_V:
                break;
            case SystemDefinition.HOST_CMD.SET_FUEL_CELL_V:
                BD.setM_FUEL_CELL_V(parmFloat, index);
                break;
            case SystemDefinition.HOST_CMD.GET_BODY_TEMPERATURE:
                break;
            case SystemDefinition.HOST_CMD.SET_BODY_TEMPERATURE:
                BD.setM_bodyTemperature(parmFloat);
                BD.setM_bodyTemperatureC(BD.getTemperatureFromVoltage(BD.getM_bodyTemperature()));
                break;
            case SystemDefinition.HOST_CMD.GET_CELL_TEMPERATURE:
                break;
            case SystemDefinition.HOST_CMD.SET_CELL_TEMPERATURE:
                if(!isTablet) {
                    if (sharedPrefs.getString(getString(R.string.pref_units_key), getString(R.string.pref_units_imperial)).equals(getString(R.string.pref_units_imperial))) {
                        unitType = false;
                    } else {
                        unitType = true;
                    }
                    mAdapter.changeTempLebal(unitType);
                }

                final float val2 = ((float)129.1 * parmFloat) - (float)232.38;
                if (index == 0) {
                    BD.setM_cellTemperature(parmFloat);
                    BD.setM_cellTemperatureC(BD.getTemperatureFromVoltage(BD.getM_cellTemperature()));
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                            if (index < mAdapter.getItemCount()) {
                                if(unitType) {
                                    mAdapter.getDatabyIdx(index).setCellTemp(val2);
                                }else {
                                    float f = BD.getM_cellTemperatureC() * 1.8f + 32f;
                                    mAdapter.getDatabyIdx(index).setCellTemp(f);
                                }
                                //mAdapter.notifyDataSetChanged();
                            }
                            if (index == 0) {

                                tempGauge.setmCurrentVaule(BD.getM_cellTemperatureC());

                            }
                    }
                });
                break;
            case SystemDefinition.HOST_CMD.GET_SOC_VAL:
                break;
            case SystemDefinition.HOST_CMD.SET_SOC_VAL:
                totalSOC=0.0f;
                for(int i = 0;i<nodeNum;i++) {
                    totalSOC+=mAdapter.getDatabyIdx(i).getAmpHour();
                    BD.setM_SOC(totalSOC);
                }
                if (index < mAdapter.getItemCount()) {
                    mAdapter.getDatabyIdx(index).setAmpHour(parmFloat);
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    SOC2.setText(String.format("%.1f", BD.getM_SOC()));
                    if (isChargeGauge) {
                        if(BD.getInitSOC() != 0.0) {
                            float f = BD.getM_SOC()*100 / BD.getInitSOC();
//                                    chargeGauge.setUnitsText(String.format("%.2f", f) + "%");
//                                    chargeGauge.setSpeed(f,true);
                            chargeGauge.setmSOC(f);
                        }
                    }
                        }
                    });

                break;
            case SystemDefinition.HOST_CMD.GET_SOH_VAL:
                break;
            case SystemDefinition.HOST_CMD.SET_SOH_VAL:
                BD.setM_SOH(parmFloat);
                break;
            case SystemDefinition.HOST_CMD.GET_SOF_VAL:

                break;
            case SystemDefinition.HOST_CMD.SET_SOF_VAL:
                BD.setM_SOF(parmFloat);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(isChargeGauge){
                            useAbleBatteryCap.setText(String.format("%.1f", BD.getM_SOF()));
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
                        mBname.setText(getArguments().getString("mBname")+"\n FW: "+BD.getAppVer());
                    }
                });
                break;

            case SystemDefinition.HOST_CMD.SET_NUM_BATT_NODES:
                    nodeNum = parmInt;
                    BD.setInitSOC(C * nodeNum);
                    if(mAdapter.getItemCount() < nodeNum){
                        for(int i = 1;i < nodeNum;i++){
                            mAdapter.addItem(new SubModuleData(unitType));
                            mAdapter.getDatabyIdx(i).setModuleNum(i+1);
                        }
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.notifyDataSetChanged();
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



    public void ParseDebugCommand(int commandID,byte[] parm,int dataType,int index)
    {
        final int parmInt = ByteBuffer.wrap(parm).order(ByteOrder.LITTLE_ENDIAN).getInt();
        switch (commandID){

            case SystemDefinition.DEBUG_CMD.SET_DEEP_CYCLE_MODE:
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        deepCycleBtn.setOnCheckedChangeListener(null);
                        if(parmInt == 1){
                            deepCycleBtn.setChecked(true);
                        }else if(parmInt == 0){
                            deepCycleBtn.setChecked(false);
                        }
                        deepCycleBtn.setOnCheckedChangeListener(mDeepCycleListener);
                    }
                });

                break;
            case SystemDefinition.DEBUG_CMD.GET_SAFEMODE_STATE:

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
            case SystemDefinition.DEBUG_CMD.SET_SAFEMODE_STATE:
                if(parmInt<SafeModeCnt && parmInt>=0){
                    safeModeSnyc = true;
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
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    contactorBtn.setEnabled(false);
                                    safeModeBtn.setEnabled(false);
                                    oneLastStart.setEnabled(false);
                                    heaterBtn.setEnabled(false);
                                    deepCycleBtn.setEnabled(false);
                                    rotationBtn.setChecked(false);
                                }
                            });
                            break;
                        case 4:
                            BD.setM_ignitionState(getString(R.string.ENGINE_OFF));
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(!contactorBtn.isEnabled()&&!safeModeBtn.isEnabled()&&!heaterBtn.isEnabled()&&!oneLastStart.isEnabled()) {
                                        contactorBtn.setEnabled(true);
                                        safeModeBtn.setEnabled(true);
                                        heaterBtn.setEnabled(true);
                                        //oneLastStart.setEnabled(true);
                                        rotationBtn.setEnabled(true);
                                        deepCycleBtn.setEnabled(true);
                                    }
                                }
                            });
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
                if(safeModeSnyc&&parmInt<ContactorStateCnt && parmInt>=0) {
                    switch (parmInt){
                        case 0:
                            BD.setM_contactorState(getString(R.string.CONTACTOR_OPEN));
                            if(!safeModeBtn.isChecked()&&!heaterBtn.isChecked()){
                                contactorBtn.setOnCheckedChangeListener(null);
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        contactorBtn.setChecked(true);
                                        contactorBtn.setOnCheckedChangeListener(mContactorListener);
                                    }
                                });

                            }
                            break;
                        case 1:
                            BD.setM_contactorState(getString(R.string.CONTACTOR_CLOSED));
                            if(!safeModeBtn.isChecked()&&!heaterBtn.isChecked()) {
                                contactorBtn.setOnCheckedChangeListener(null);
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        contactorBtn.setChecked(false);
                                        contactorBtn.setOnCheckedChangeListener(mContactorListener);
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
                break;

            default:

                Log.v(TAG,"Debug Wrong  CommandID");
                break;
        }
    }


    private float C = 0.0f;
    private boolean verifing = false;
    public void ParseServiceCommand(int commandID, byte[] parm, int dataType, final int index){
        float parmFloat = ByteBuffer.wrap(parm).order(ByteOrder.LITTLE_ENDIAN).getFloat();
        int parmInt = ByteBuffer.wrap(parm).order(ByteOrder.LITTLE_ENDIAN).getInt();
        switch (commandID){
            case SystemDefinition.SERVICE_CMD.SERIAL_NUMBER:
                BD.setSN(parm,index);
                if(BD.SN.Done){
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
                if(BD.PN.Done ){
                        C = SystemDefinition.getCapacity(BD.getPN());
                        BD.setInitSOC(C * nodeNum);
                }
                break;
            default:
                Log.v(TAG,"Service Wrong  CommandID");
                break;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(tickTimer!=null){
            tickTimer.cancel();
            tickTimer.purge();
        }
        if(alertDialog!=null) {
            if (alertDialog.isShowing()) {
                alertDialog.dismiss();
            }
        }
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

    public void rotationModeDialog(String rotationMode){

        String title = null;

        if(rotationMode == null)
            return;


        title = "Normal Mode";
        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .title(title)
                .customView(R.layout.rotation_mode_display_dialog, false)
                .cancelable(false)
                .positiveText("CONFIRM")
                .dismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        deepCycleBtn.setChecked(false);
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

        accidentDetection.setVisibility(View.INVISIBLE);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(tickTimer!=null){
            tickTimer.cancel();
            tickTimer.purge();
        }
        if(alertDialog!=null) {
            if (alertDialog.isShowing()) {
                alertDialog.dismiss();
            }
        }
    }

    public void sequenceDialog(final int type){
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
                    break;
                default:
                    break;
            }
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

    public class CustomLinearLayoutManager extends LinearLayoutManager {
        public CustomLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);

        }

        // it will always pass false to RecyclerView when calling "canScrollVertically()" method.
        @Override
        public boolean canScrollVertically() {
            return false;
        }
    }

    private Timer tickTimer;
    public void startDemo() {
        tickTimer = new Timer();
        tickTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(demoPowerOff){
                            VOLT.setText("0 V");
                            BankV.setText("0");
                            BusV.setText("0");
                            mAdapter.getDatabyIdx(0).setBankV(0);
                        }else if(demoLowPower){
                            VOLT.setText(String.format("%.1f", BD.getM_batteryVoltage()-3) + "V");
                            BankV.setText(String.format("%.1f", BD.getM_batteryVoltage()-3));
                            BusV.setText(String.format("%.1f", BD.getM_batteryVoltage()-3));
                            mAdapter.getDatabyIdx(0).setBankV(BD.getM_batteryVoltage()-3);
                        }else {
                            VOLT.setText(String.format("%.1f", BD.getM_batteryVoltage()) + "V");
                            BankV.setText(String.format("%.1f", BD.getM_batteryVoltage()));
                            BusV.setText(String.format("%.1f", BD.getM_batteryVoltage()));
                            mAdapter.getDatabyIdx(0).setBankV(BD.getM_batteryVoltage());
                        }

                        mAdapter.getDatabyIdx(0).setC_MON_AMP((BD.getM_C_MON_amps()));
                        mAdapter.getDatabyIdx(0).setCellTemp(BD.getM_bodyTemperatureC());
                        mAdapter.getDatabyIdx(0).setAmpHour(BD.getM_SOC()/10f);
                        SOC2.setText(String.format("%.1f", BD.getM_SOC()/10f));
                        useAbleBatteryCap.setText(String.format("%.1f", BD.getM_SOF()/10));
                        if(currentChargeStylekitView!=null){
                            currentChargeStylekitView.setmValue(BD.getM_C_MON_amps());
                        }

                    }
                });

            }
        }, 0,1000);

    }

    private int ticks = 0;
    private float demoSOC = 100f,demoCurrent = 0f,demoVolt = 13.6f,demoTemp = -5;
    public void startDemo2() {
        tickTimer = new Timer();
        tickTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(mUser.getBatteriesList().get(batteryIdx).engineState == 21)
                {
                    mUser.getBatteriesList().get(batteryIdx).engineState = 3;
                    ticks = 0;
                    demoSOC = 100f;
                    demoCurrent = 0f;
                    demoVolt = 13.6f;
                    demoTemp = -5;

                }

                if(mUser.getBatteriesList().get(batteryIdx).heaterOn){
                    demoTemp = demoTemp >= 30?30:demoTemp+1 ;
                }

                if(ticks == 0){

                    BD.setM_SOC(demoSOC);
                    BD.setM_SOF(socToSof(demoSOC,demoTemp));
                    BD.setM_C_MON_amps(demoCurrent);
                    BD.setM_batteryVoltage(demoVolt);
                    BD.setM_bodyTemperatureC(demoTemp);

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            BD.setM_C_MON_amps(demoCurrent);
                            if(isChargeGauge){

                                chargeGauge.setmSOC(demoSOC);
                                chargeGauge.setmSOF(socToSof(demoSOC,demoTemp));
                            }else{
                                chargeGauge.setmSOC(demoCurrent);
                            }
                            tempGauge.setmCurrentVaule(demoTemp);
                            if(mUser.getBatteriesList().get(batteryIdx).engineState == 3) {
                                Toast.makeText(getContext(), "Ignition On ", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else if (ticks < 5){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(mUser.getBatteriesList().get(batteryIdx).engineState == 3) {
                                demoSOC -= 10;
                                demoCurrent = -75;
                                demoVolt -= 0.5;
                            }
                            else if( mUser.getBatteriesList().get(batteryIdx).engineState == 20 ){
                                demoCurrent = 0;
                                demoTemp = (demoTemp <=-5)?-5:demoTemp-0.5f;
                            }
                            if(isChargeGauge){
                                chargeGauge.setmSOC(demoSOC);
                                chargeGauge.setmSOF(socToSof(demoSOC,demoTemp));
                            }else{
                                chargeGauge.setmSOC(demoCurrent);
                            }
                            BD.setM_SOC(demoSOC);
                            BD.setM_C_MON_amps(demoCurrent);
                            BD.setM_batteryVoltage(demoVolt);
                            BD.setM_bodyTemperatureC(demoTemp);
                            BD.setM_SOF(socToSof(demoSOC,demoTemp));
                        }
                    });

                }else if(ticks  < 15){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(ticks == 5) {
                                Toast.makeText(getContext(), "Engine On ", Toast.LENGTH_SHORT).show();
                            }
                            if(mUser.getBatteriesList().get(batteryIdx).engineState == 3) {
                                demoSOC = demoSOC > 100 ? 100 : demoSOC + 5;
                                demoCurrent = 45;
                                demoVolt += 0.1;
                                demoTemp += 0.5;
                            }
                            else if( mUser.getBatteriesList().get(batteryIdx).engineState == 20 ){
                                demoCurrent = 0;
                                demoTemp = (demoTemp <=-5)?-5:demoTemp-0.5f;
                            }
                            if(isChargeGauge){
                                chargeGauge.setmSOC(demoSOC);
                                chargeGauge.setmSOF(socToSof(demoSOC,demoTemp));
                            }else{
                                chargeGauge.setmSOC(demoCurrent);
                            }
                            tempGauge.setmCurrentVaule(demoTemp);
                            BD.setM_SOC(demoSOC);
                            BD.setM_C_MON_amps(demoCurrent);
                            BD.setM_batteryVoltage(demoVolt);
                            BD.setM_bodyTemperatureC(demoTemp);
                            BD.setM_SOF(socToSof(demoSOC,demoTemp));
                        }
                    });
                }else if (ticks  < 45) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(mUser.getBatteriesList().get(batteryIdx).engineState == 3) {
                                demoSOC = demoSOC > 100 ? 100 : demoSOC + 1;
                                demoCurrent = 15;
                                demoVolt += 0.05;
                                demoTemp += 0.5;
                            }
                            else if( mUser.getBatteriesList().get(batteryIdx).engineState == 20 ){
                                demoCurrent = 0;
                                demoTemp = (demoTemp <=-5)?-5:demoTemp-0.5f;
                            }
                            if(isChargeGauge){
                                chargeGauge.setmSOC(demoSOC);
                                chargeGauge.setmSOF(socToSof(demoSOC,demoTemp));
                            }else{
                                chargeGauge.setmSOC(demoCurrent);
                            }
                            tempGauge.setmCurrentVaule(demoTemp);
                            BD.setM_SOC(demoSOC);
                            BD.setM_C_MON_amps(demoCurrent);
                            BD.setM_batteryVoltage(demoVolt);
                            BD.setM_bodyTemperatureC(demoTemp);
                            BD.setM_SOF(socToSof(demoSOC,demoTemp));
                        }
                    });

                }else if(ticks <75){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(mUser.getBatteriesList().get(batteryIdx).engineState == 3) {
                                if (ticks % 2 == 0) {
                                    demoSOC = demoSOC > 100 ? 100 : demoSOC + 0.5f;
                                    demoCurrent = 1.5f;
                                    demoVolt += 0.1;
                                    demoTemp += 0.5;
                                } else {
                                    demoSOC = demoSOC > 100 ? 100 : demoSOC - 0.5f;
                                    demoCurrent = -1.5f;
                                    demoVolt -= 0.1;
                                    demoTemp += 0.5;
                                }
                            }   else if( mUser.getBatteriesList().get(batteryIdx).engineState == 20 ){
                                demoCurrent = 0;
                                demoTemp = (demoTemp <=-5)?-5:demoTemp-0.5f;
                            }
                            if (isChargeGauge) {
                                chargeGauge.setmSOC(demoSOC);
                                chargeGauge.setmSOF(socToSof(demoSOC,demoTemp));
                            } else {
                                chargeGauge.setmSOC(demoCurrent);
                            }
                            tempGauge.setmCurrentVaule(demoTemp);
                            BD.setM_SOC(demoSOC);
                            BD.setM_C_MON_amps(demoCurrent);
                            BD.setM_batteryVoltage(demoVolt);
                            BD.setM_bodyTemperatureC(demoTemp);
                            BD.setM_SOF(socToSof(demoSOC,demoTemp));
                        }
                    });
                }else{
                    demoSOC = 100f;
                    demoCurrent = 0f;
                    demoVolt = 13.6f;
                    demoTemp = -5;
                    ticks = 0;
                }


                if(mUser.getBatteriesList().get(batteryIdx).engineState == 3) {
                    ticks++;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if(demoPowerOff){
                            VOLT.setText("0 V");
                            //BankV.setText("0");
                            BusV.setText("0");
                            //mAdapter.getDatabyIdx(0).setBankV(0);
                        }else if(demoLowPower){
                            VOLT.setText(String.format("%.1f", BD.getM_batteryVoltage()-3) + "V");
                            //BankV.setText(String.format("%.1f", BD.getM_batteryVoltage()-3));
                            BusV.setText(String.format("%.1f", BD.getM_batteryVoltage()-3));
                            //mAdapter.getDatabyIdx(0).setBankV(BD.getM_batteryVoltage()-3);
                        }else {
                            VOLT.setText(String.format("%.1f", BD.getM_batteryVoltage()) + "V");
                            //BankV.setText(String.format("%.1f", BD.getM_batteryVoltage()));
                            BusV.setText(String.format("%.1f", BD.getM_batteryVoltage()));
                            //mAdapter.getDatabyIdx(0).setBankV(BD.getM_batteryVoltage());
                        }
                        mAdapter.getDatabyIdx(0).setBankV(BD.getM_batteryVoltage());
                        BankV.setText(String.format("%.1f", BD.getM_batteryVoltage()));
                        mAdapter.getDatabyIdx(0).setC_MON_AMP((BD.getM_C_MON_amps()));
                        mAdapter.getDatabyIdx(0).setCellTemp(BD.getM_bodyTemperatureC());
                        mAdapter.getDatabyIdx(0).setAmpHour(BD.getM_SOC()/10f);
                        SOC2.setText(String.format("%.1f", BD.getM_SOC()/10f));
                        useAbleBatteryCap.setText(String.format("%.1f", BD.getM_SOF()/10));
                        if(currentChargeStylekitView!=null){
                            currentChargeStylekitView.setmValue(BD.getM_C_MON_amps());
                        }
                            }
                });
            }
        }, 0,1000);

    }

    public float socToSof(float soc,float temperatureC){

        int i = 0;

        // Assign slope and intercept based on discharge data
        float m = 0;
        float b = 0;
        float dy = 0;
        float dx = 0;

        float sof = 0;

        // Independent variable
        float[] dataTemperature = { -20, -10, 0, 10, 20, 30, 40, 50, 60 };

        // Dependent variable. Default is for 10 Ah battery
        float[] dataCapacity = { 5.9688f,  6.7783f, 7.5544f,
                8.4303f,  9.2683f, 10.4889f,
                10.7063f, 9.9840f, 10.0019f };

//        float[] dataCap8Ah = { 3.3034f, 4.7475f, 5.9838f,
//                7.0288f, 7.7852f, 7.9048f,
//                8.0389f, 7.9266f, 7.9301f };
//
//        // If this battery is an 8Ah battery, copy that data to the array
//        if (defaultCellAmpHours == DEFAULT_AMP_HOURS_108)
//            memcpy(dataCapacity, dataCap8Ah, sizeof(dataCapacity));

        // Scale the temperature variable since cellTempC is in volts

        // Find temperature range to calculate regression
        for (i = 1; i < 9; i++)
        {
            if (temperatureC < dataTemperature[i])
            {
                dy = dataCapacity[i] - dataCapacity[i-1];
                dx = dataTemperature[i] - dataTemperature[i-1];
                m = dy/dx;
                b = dataCapacity[i] - (m * dataTemperature[i]);

                break;
            }
        }

        // SOF calculation
        sof = m * temperatureC + b;
        sof /= 10; // Scale down so that 1.0 = 100%

        if (sof < 0.0)
            sof = 0.0f;
        else if (sof > 1.0)
            sof = 1.0f;

        sof *= soc;

        return sof;

    }
}

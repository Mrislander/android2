package com.elitise.appv2;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.labo.kaji.fragmentanimations.CubeAnimation;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.regex.Pattern;


public class PhoneLayoutFragement extends ServiceFragment {
    private static final UUID ELT_BATT_MOBBAT_SERVICE_UUID = UUID
            .fromString("AC55D02D-A7FE-4F64-A659-ABCB5C6D409D");

    private static final UUID ELT_BATT_MOBBAT_CHARACTERISTIC_UUID = UUID
            .fromString("A88C5B46-B899-473A-96C9-49B98173948D");


    private ServiceFragmentDelegate mDelegate;


    private MaterialDialog blockDialog;
    private boolean connection = false;
    private Timer connectionTimer = new Timer();
    private TimerTask timerTask;

    private BatteryData BD;
    private UserData mUser = UserData.getInstance();
    private int batteryIdx;


    private SharedPreferences sharedPrefs;
    private boolean unitType;


    private TextView VOLT;
    private View.OnClickListener mDiagModeListener;


    private float totalSOC = 0.0f;
    private float totalCMON = 0.0f;


    private int diagModeCount = 0;
    private String password = "Elitise1";


    private ArrayList<SubModuleData> mSubData;
    private SubModuleAdapter mAdapter;


    private Dialog alertDialog;

    private ChargerGaugeView chargeGauge;
    private TempGaugeView tempGauge;

    private CurrentChargeStylekitView currentChargeStylekitView;

    private ConnectLedView connectionView;
    //private  ImageView mTempImage;

    private int nodeNum = 1;

    private boolean isChargeGauge = true;

    private Button showDataBtn;

    private ImageView chargeAnimationBtn;

    private Timer animationTimer = new Timer();
    private TextView mBname;
    // GATT
    private BluetoothGattService mBatteryService;
    private BluetoothGattCharacteristic mBatteryCharacteristic;

    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    public PhoneLayoutFragement() {
        mBatteryCharacteristic =
                new BluetoothGattCharacteristic(ELT_BATT_MOBBAT_CHARACTERISTIC_UUID,
                        BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
                        BluetoothGattCharacteristic.PERMISSION_WRITE | BluetoothGattCharacteristic.PERMISSION_READ);
        mBatteryCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        mBatteryCharacteristic.addDescriptor(
                Peripheral.getClientCharacteristicConfigurationDescriptor());
        mBatteryCharacteristic.addDescriptor(
                Peripheral.getClientCharacteristicExtendedDescriptor());
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


        final View view = inflater.inflate(R.layout.phone_layout, container, false);


        BD = BatteryData.getInstance();
        batteryIdx = getArguments().getInt("bIndex");
        //load unitType
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (sharedPrefs.getString(getString(R.string.pref_units_key), getString(R.string.pref_units_imperial)).equals(getString(R.string.pref_units_imperial))) {
            unitType = false;
        } else {
            unitType = true;
        }

        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            showDataBtn = (Button) view.findViewById(R.id.showDataBtn);
            showDataBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mUser.getBatteriesList().get(batteryIdx).isBonded) {
                        ((Peripheral) getActivity()).enableShowData();
                    } else {

                        new MaterialDialog.Builder(getActivity())
                                .title("Failed")
                                .content("Please connect to a battery first")
                                .positiveText("OK")
                                .positiveColor(Color.parseColor("#ff6529"))
                                .show();
                    }
                }
            });

        }



        blockDialog = new MaterialDialog.Builder(getActivity())
                .theme(Theme.LIGHT)
                .autoDismiss(false)
                .content("Entering Power OFF Mode, please wait for confirmation")
                .build();

        mBname = (TextView) view.findViewById(R.id.BatteryName2);
        mBname.setText(getArguments().getString("mBname"));

        connectionView = (ConnectLedView) view.findViewById(R.id.connectionView2);
        //set logo green
        //mTempImage = (ImageView) view.findViewById(R.id.mTempImage);

        chargeGauge = (ChargerGaugeView) view.findViewById(R.id.chargegauge2);

        chargeGauge.setmSOC(50f);


        tempGauge = (TempGaugeView) view.findViewById(R.id.tempgauge2);


        tempGauge.initGauge(unitType);


        //tempGauge.celsiusAndFahrenheit(true);
        tempGauge.setmCurrentVaule(42.5f);


        VOLT = (TextView) view.findViewById(R.id.volt2);


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

        if (!mUser.getBatteriesList().get(batteryIdx).isBonded && mUser.getBatteriesList().get(batteryIdx).getmSN() != null) {
            scanBarcode(1);
        }

        mDiagModeListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (++diagModeCount > 2) {
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
                                    if (input.toString().equals(password)) {
                                        byte[] cmd = SystemDefinition.encodeCmd(SystemDefinition.CMOMANDSTYPE.HOST_COMMANDS, SystemDefinition.HOST_CMD.SET_DIAG_MODE,
                                                ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(1).array(), 0, 0, 9);
                                        mBatteryCharacteristic.setValue(cmd);
                                        mDelegate.sendNotificationToDevices(mBatteryCharacteristic);
                                        ((Peripheral) getActivity()).enableDiagMode();
                                    } else {
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
        chargeAnimationBtn.bringToFront();
        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
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
        } else {
            chargeAnimationBtn.setVisibility(View.GONE);
        }

//        animationTimer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                getActivity().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        currentChargeStylekitView.startAnimation();
//                    }
//                });
//            }
//        },0,250);

        if(!mUser.getBatteriesList().get(batteryIdx).isDemo) {
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    //connection = false;
                    updateConnection(false);
                }
            };

            connectionTimer.schedule(timerTask, 5000);
        }else{
            connectionView.setLedColor(Color.GREEN);
            TextView statusBar = (TextView)getActivity().findViewById(R.id.statusBar);
            statusBar.setText("Status: Connected to device --- Demo Battery");
            startDemo();
        }
        return view;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mDelegate = (ServiceFragmentDelegate) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mDelegate = null;
    }

    public BluetoothGattService getBluetoothGattService() {
        return mBatteryService;
    }

    public BluetoothGattService getBluetoothGattServiceCustom() {

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

        if (!bonded)
            return new ParcelUuid(ELT_BATT_MOBBAT_SERVICE_UUID);
        else {
            SystemDefinition.customUUID(mUser.getBatteriesList().get(batteryIdx).getmSN());
            return new ParcelUuid(UUID.fromString(SystemDefinition.ELT_BATT_MOBBAT_SERVICE_UUID_WITH_SN));
        }
    }


    public static final int BUFF_LENGTH = 9;

    public static boolean m_receivingSlip = false;
    public static boolean m_haveCompletepackA = false;
    public static byte[] ByteArray = null;
    public static byte[] decodeByteArray = null;


    private static final String TAG = Peripheral.class.getCanonicalName();

    @Override
    public int writeCharacteristic(BluetoothGattCharacteristic characteristic, int offset, byte[] value) {

        //connection = true;

        updateConnection(true);

        if (timerTask != null)
            timerTask.cancel();

        int receivedLen = value.length;
        if (blockDialog != null && blockDialog.isShowing()) {
            blockDialog.dismiss();
        }
        ByteArray = new byte[receivedLen];
        for (int idx = 0; idx < receivedLen; idx++) {
            if (m_receivingSlip) {

                ByteArray[idx] = value[idx];
                if (value[idx] == SystemDefinition.SLIP_END) {
                    m_receivingSlip = false;
                    m_haveCompletepackA = true;
                }
                value[idx] = 0;

            } else if (value[idx] == SystemDefinition.SLIP_END) {
                ByteArray[idx] = value[idx];
                m_receivingSlip = true;
                value[idx] = 0;
                if ((idx + 1) < receivedLen)
                    if (value[idx + 1] == SystemDefinition.SLIP_END)
                        idx++;
            }
            if (m_haveCompletepackA) {

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
        connectionTimer.schedule(timerTask, 5000);
        return BluetoothGatt.GATT_SUCCESS;
    }

    public byte[] decode(byte[] ByteArray) {
        int idx;
        byte c;
        boolean escaped = false;
        byte[] decodeArray = new byte[BUFF_LENGTH];
        boolean beginingFound = false;
        int dest_pos = 0;
        for (idx = 0; idx < ByteArray.length; idx++) {
            c = ByteArray[idx];
            if (c == SystemDefinition.SLIP_END) {
                if (dest_pos == 0) {
                    beginingFound = true;

                    continue;
                } else {

                    return decodeArray;
                }


            } else if (escaped) {
                if (c == SystemDefinition.SLIP_ESC_END)
                    c = SystemDefinition.SLIP_END;
                else if (c == SystemDefinition.SLIP_ESC_ESC)
                    c = SystemDefinition.SLIP_ESC;
                else {


                }
                escaped = false;
            } else if (c == SystemDefinition.SLIP_ESC) {
                escaped = true;
                continue;
            }

            if (beginingFound && (dest_pos < BUFF_LENGTH)) {
                decodeArray[dest_pos++] = c;
            }
        }

        return decodeArray;
    }

    static boolean isFirst = true;

    public void updateConnection(final boolean c) {
        if (getActivity() == null)
            return;


        if (c == true) {
            //Log.v(TAG, "Obs: Connect");
            if (!connection) {
                connection = true;
                //((Peripheral) getActivity()).createBond();
            }

            if (mUser.getBatteriesList().get(batteryIdx).mImageId == 12 && mUser.getBatteriesList().get(batteryIdx).isBonded && isFirst) {// enable deep cycle mode
                isFirst = false;
                byte[] cmd = SystemDefinition.encodeCmd(SystemDefinition.CMOMANDSTYPE.DEBUG_COMMANDS, SystemDefinition.DEBUG_CMD.SET_DEEP_CYCLE_MODE,
                        ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(1).array(), 0, 0, 9);
                mBatteryCharacteristic.setValue(cmd);
                mDelegate.sendNotificationToDevices(mBatteryCharacteristic);

            } else if (isFirst && mUser.getBatteriesList().get(batteryIdx).mImageId != 12 && mUser.getBatteriesList().get(batteryIdx).isBonded) {

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
                    if (!VOLT.hasOnClickListeners()) {
                        VOLT.setOnClickListener(mDiagModeListener);
                    }
                }
            });

        } else {
            //Log.v(TAG,"Obs: Disonnect");
            connection = false;
            BD.reset();
            mAdapter.resetAlldata(unitType);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    connectionView.setLedColor(Color.RED);

                    tempGauge.setmCurrentVaule(42.5f);

                    VOLT.setText("0.0V");

                    chargeGauge.setmSOC(50f);

                    mAdapter.notifyDataSetChanged();

                }
            });

        }
    }


    public void parseSentence(byte[] data) {

        int commandClass;
        int commandID;
        byte[] parameter = null;
        int checksum;
        int dataType;
        int index;

        commandClass = data[0] & 0xE0;
        commandID = data[0] & 0x1F;
        parameter = combineFourBytes(data[5], data[6], data[7], data[8]);
        checksum = data[4];
        dataType = (data[3] & 0xE0) >> 5;
        index = (data[3] & 0x1F);
        //Log.v(TAG, "parsing: "+ commandClass+" CID:"+commandID+ "  Parm: "+ parameter + " index : "+index + " dataType :" + dataType);
        if (commandClass == SystemDefinition.CMOMANDSTYPE.HOST_COMMANDS) {
            if (mUser.getBatteriesList().get(batteryIdx).isBonded) {
                ParseHostCommand(commandID, parameter, dataType, index);
            }
        } else if (commandClass == SystemDefinition.CMOMANDSTYPE.DEBUG_COMMANDS) {
            if (mUser.getBatteriesList().get(batteryIdx).isBonded) {
                ParseDebugCommand(commandID, parameter, dataType, index);
            }
        } else if (commandClass == SystemDefinition.CMOMANDSTYPE.SERVICE_COMMANDS) {
            if (mUser.getBatteriesList().get(batteryIdx).isBonded) {
                ParseServiceCommand(commandID, parameter, dataType, index);
            } else {
                if (commandID == SystemDefinition.SERVICE_CMD.SERIAL_NUMBER) {
                    ParseServiceCommand(commandID, parameter, dataType, index);
                }
            }
        } else {
            Log.v(TAG, "Wrong Type Command");
        }


    }

    public static byte[] combineFourBytes(byte b1, byte b2, byte b3, byte b4) {
        byte[] temp = new byte[4];
        temp[0] = b1;
        temp[1] = b2;
        temp[2] = b3;
        temp[3] = b4;
        return temp;
    }


    public static int attempts = 0;
    public SystemDefinition.ValContainer<String> storedPwd = new SystemDefinition.ValContainer();

    public void ParseHostCommand(int commandID, byte[] parm, int dataType, final int index) {
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
                final float chargingGain;
                chargingGain = (parmFloat > (float) 1.2218 ? (float) 104 : (float) 29.369);

                totalCMON = 0.0f;
                for (int i = 0; i < nodeNum; i++) {
                    totalCMON += mAdapter.getDatabyIdx(i).getC_MON_AMP();
                    BD.setM_C_MON_amps(totalCMON);
                }
                if (index < mAdapter.getItemCount()) {
                    mAdapter.getDatabyIdx(index).setC_MON_AMP(((float) 1.2218 - parmFloat) / ((float) 0.0004 * chargingGain));

                }
                if (currentChargeStylekitView != null) {
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
                        if (index == 0) {
                            if (!isChargeGauge) {
                                chargeGauge.setmSOC(BD.getM_C_MON_amps());
                                if (currentChargeStylekitView != null) {
                                    currentChargeStylekitView.setmValue(BD.getM_C_MON_amps());
                                }

                            }
                        }
                    }
                });
                break;
            case SystemDefinition.HOST_CMD.GET_BUS_MON:
                break;
            case SystemDefinition.HOST_CMD.SET_BUS_MON:
                BD.setM_BUS_MON(parmFloat);
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
                            VOLT.setText(String.format("%.1f", BD.getM_batteryVoltage()) + "V");
                        }
                        if (index < mAdapter.getItemCount()) {
                            mAdapter.getDatabyIdx(index).setBankV(val);
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
                final float val2 = ((float) 129.1 * parmFloat) - (float) 232.38;
                if (index == 0) {
                    BD.setM_cellTemperature(parmFloat);
                    BD.setM_cellTemperatureC(BD.getTemperatureFromVoltage(BD.getM_cellTemperature()));
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (unitType) {
                            if (index < mAdapter.getItemCount()) {
                                mAdapter.getDatabyIdx(index).setCellTemp(val2);
                                //mAdapter.notifyDataSetChanged();
                            }
                            if (index == 0) {
//                                tempGauge.setUnitsText(String.format("%.2f", BD.getM_cellTemperatureC()) + "°C");
                                //float temp = (BD.getM_cellTemperatureC() + 40) / 165 * 100;
//                                tempGauge.setSpeed(temp, true);
                                tempGauge.setmCurrentVaule(BD.getM_cellTemperatureC());
                            }
                        } else {
                            double f = val2 * 1.8 + 32;
                            if (index < mAdapter.getItemCount()) {
                                mAdapter.getDatabyIdx(index).setCellTemp((float) f);
                                //mAdapter.notifyDataSetChanged();
                            }
                            if (index == 0) {
//                                tempGauge.setUnitsText(String.format("%.2f", f) + "°F");
                                // float temp = (BD.getM_cellTemperatureC() + 40) / 165 * 100;
                                tempGauge.setmCurrentVaule(BD.getM_cellTemperatureC());
                            }
                        }
                    }
                });
                break;
            case SystemDefinition.HOST_CMD.GET_SOC_VAL:
                break;
            case SystemDefinition.HOST_CMD.SET_SOC_VAL:
                totalSOC = 0.0f;

                if (index < mAdapter.getItemCount()) {
                    mAdapter.getDatabyIdx(index).setAmpHour(parmFloat);
                }
                for (int i = 0; i < nodeNum; i++) {
                    totalSOC += mAdapter.getDatabyIdx(i).getAmpHour();
                    BD.setM_SOC(totalSOC);
                }


                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isChargeGauge) {
                            if (BD.getInitSOC() != 0.0) {
                                float f = BD.getM_SOC() * 100 / BD.getInitSOC();
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
                        if (isChargeGauge) {
                            float f = BD.getM_SOF() * 100 / BD.getInitSOC();
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
                        mBname.setText(getArguments().getString("mBname") + "\n FW: " + BD.getAppVer());
                    }
                });
                break;

            case SystemDefinition.HOST_CMD.SET_NUM_BATT_NODES:
                nodeNum = parmInt;
                BD.setInitSOC(C * nodeNum);
                if (mAdapter.getItemCount() < nodeNum) {
                    for (int i = 1; i < nodeNum; i++) {
                        mAdapter.addItem(new SubModuleData(unitType));
                        mAdapter.getDatabyIdx(i).setModuleNum(i + 1);
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


    public void ParseDebugCommand(int commandID, byte[] parm, int dataType, int index) {
        int parmInt = ByteBuffer.wrap(parm).order(ByteOrder.LITTLE_ENDIAN).getInt();
        switch (commandID) {

            case SystemDefinition.DEBUG_CMD.GET_SAFEMODE_STATE:

                break;

            case SystemDefinition.DEBUG_CMD.GET_REPROGRAM_MODE:
                break;
            case SystemDefinition.DEBUG_CMD.SET_SAFEMODE_STATE:
                if (parmInt < SafeModeCnt && parmInt >= 0) {
                    switch (parmInt) {
                        case 0:
                            BD.setM_safeModeState("OFF");
                            break;
                        case 1:
                            BD.setM_safeModeState("ON");
                            break;
                    }
                }

                break;
            case SystemDefinition.DEBUG_CMD.GET_CHARGE_STATE:
                break;

            case SystemDefinition.DEBUG_CMD.SET_CHARGE_STATE:

                if (parmInt < ChargeStateCnt && parmInt >= 0) {

                    switch (parmInt) {
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

                if (parmInt < IgnitionStateCnt && parmInt >= 0) {

                    switch (parmInt) {
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
                            break;
                        case 4:
                            BD.setM_ignitionState(getString(R.string.ENGINE_OFF));
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
                if (parmInt < HeaterStateCnt && parmInt >= 0) {

                    switch (parmInt) {
                        case 0:
                            BD.setM_heaterState(getString(R.string.HEATER_INIT));
                            break;
                        case 1:
                            BD.setM_heaterState(getString(R.string.HEATER_ON));
                            break;
                        case 2:
                            BD.setM_heaterState(getString(R.string.HEATER_OFF));
                            break;
                        case 3:
                            BD.setM_heaterState(getString(R.string.HEATER_ENGAGED));
                            break;
                        case 4:
                            BD.setM_heaterState(getString(R.string.HEATER_DISENGAGED));
                            break;
                        case 5:
                            break;
                        case 6:
                            break;
                        default:
                            break;
                    }
                }
                break;
            case SystemDefinition.DEBUG_CMD.GET_CONTACTOR_STATE:
                break;
            case SystemDefinition.DEBUG_CMD.SET_CONTACTOR_STATE:
                if (parmInt < ContactorStateCnt && parmInt >= 0) {
                    switch (parmInt) {
                        case 0:
                            BD.setM_contactorState(getString(R.string.CONTACTOR_OPEN));
                            break;
                        case 1:
                            BD.setM_contactorState(getString(R.string.CONTACTOR_CLOSED));
                            break;
                        default:
                            break;
                    }
                }
                break;
            case SystemDefinition.DEBUG_CMD.GET_SLEEP_STATE:
                break;
            case SystemDefinition.DEBUG_CMD.SET_SLEEP_STATE:
                if (parmInt < SleepStateCnt && parmInt >= 0) {

                    switch (parmInt) {
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
                if (parmInt < BalancerStateCnt && parmInt >= 0) {

                    switch (parmInt) {
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
                Log.v(TAG, "Wrong  CommandID");
                break;
        }
    }


    private float C = 0.0f;
    private boolean verifing = false;

    public void ParseServiceCommand(int commandID, byte[] parm, int dataType, final int index) {
        float parmFloat = ByteBuffer.wrap(parm).order(ByteOrder.LITTLE_ENDIAN).getFloat();
        int parmInt = ByteBuffer.wrap(parm).order(ByteOrder.LITTLE_ENDIAN).getInt();
        switch (commandID) {
            case SystemDefinition.SERVICE_CMD.SERIAL_NUMBER:
                BD.setSN(parm, index);
                if (BD.SN.Done) {
                    String temp = BD.getSN().substring(0, 7);
//                    if(!Pattern.matches(".*",temp)){
//                        temp =  BD.getSN().substring(0,7);
//                    }
                    //String temp = BD.getSN();
                    mUser.getBatteriesList().get(batteryIdx).setmSN(temp);
                    if (!mUser.getBatteriesList().get(batteryIdx).isBonded && !verifing) {
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
                BD.setPN(parm, index);
                if (BD.PN.Done) {
                    C = SystemDefinition.getCapacity(BD.getPN());
                    BD.setInitSOC(C * nodeNum);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (alertDialog != null) {
            if (alertDialog.isShowing()) {
                alertDialog.dismiss();
            }
        }
        if(tickTimer!=null) {
            tickTimer.cancel();
            tickTimer.purge();
        }
        isFirstTime = true;
        BD.reset();
    }

    private static boolean isFirstTime = true;

    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {

        if (!isFirstTime) {
            return CubeAnimation.create(CubeAnimation.LEFT, enter, 500);

        } else {
            isFirstTime = false;
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
                    } else {
                        count++;
                        if (count < 11) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    scanBarcode(count);
                                }
                            });
                        } else {
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
                                                    byte[] data = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(SystemDefinition.connectionStatus.BLE_CONNECTION_FAILED_AUTHENTICATION).array();
                                                    byte[] cmd = SystemDefinition.encodeCmd(SystemDefinition.CMOMANDSTYPE.DEBUG_COMMANDS, SystemDefinition.DEBUG_CMD.SET_BLE_CONNECTION_STATE,
                                                            data, 0, 0, 9);
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
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void scanBarcode(int attempts) {
        new MaterialDialog.Builder(getActivity())
                .title("Scan Barcode")
                .titleColor(Color.parseColor("#ff6529"))
                .titleGravity(GravityEnum.CENTER)
                .content("Please use the camera to scan the barcode or manually enter the Serial Number. Attempts:" + attempts)
                .inputRangeRes(0, -1, R.color.material_drawer_dark_background)
                .input("Enter Serial Number Or Tap Scan", null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(final MaterialDialog dialog, CharSequence input) {
                        if (((Peripheral) getActivity()).verifySN(input.toString())) {
                            verifing = false;
                            count = 1;
                            ((Peripheral) getActivity()).startCustomAdv();
                        } else {
                            count++;
                            if (count < 10) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        dialog.dismiss();
                                        scanBarcode(count);
                                    }
                                });
                            } else {
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
                                                        byte[] data = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(SystemDefinition.connectionStatus.BLE_CONNECTION_FAILED_AUTHENTICATION).array();
                                                        byte[] cmd = SystemDefinition.encodeCmd(SystemDefinition.CMOMANDSTYPE.DEBUG_COMMANDS, SystemDefinition.DEBUG_CMD.SET_BLE_CONNECTION_STATE,
                                                                data, 0, 0, 9);
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
                        intent.putExtra(BarcodeCaptureActivity.UseFlash, false);
                        startActivityForResult(intent, RC_BARCODE_CAPTURE);
                    }
                })
                .cancelable(false)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(tickTimer!=null) {
            tickTimer.cancel();
            tickTimer.purge();
        }
    }



    private int ticks = 0;
    private float demoSOC = 100f,demoCurrent = 0f,demoVolt = 13.6f,demoTemp = -5;
    private Timer tickTimer;
    public void startDemo() {
        tickTimer = new Timer();
        tickTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(mUser.getBatteriesList().get(batteryIdx).engineState == 1)
                {
                    mUser.getBatteriesList().get(batteryIdx).engineState = 3;
                    ticks = 0;
                    demoSOC = 100f;
                    demoCurrent = 0f;
                    demoVolt = 13.6f;
                    demoTemp = -5;
                }
                if(mUser.getBatteriesList().get(batteryIdx).heaterOn){
                    demoTemp = demoTemp >= 30?30:demoTemp+1f ;
                }

                if(ticks == 0){
//                    ticks = 0;
//                    demoSOC = 100f;
//                    demoCurrent = 0f;
//                    demoVolt = 13.6f;
//                    demoTemp = -5;

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
                            VOLT.setText(String.format("%.1f",demoVolt));
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
                            else if( mUser.getBatteriesList().get(batteryIdx).engineState == 0 ){
                                demoCurrent = 0;
                                demoTemp = (demoTemp <=-5)?-5:demoTemp-0.5f;
                            }
                            if(currentChargeStylekitView!=null){
                                currentChargeStylekitView.setmValue(demoCurrent);
                            }
                            if(isChargeGauge){
                                chargeGauge.setmSOC(demoSOC);
                                chargeGauge.setmSOF(socToSof(demoSOC,demoTemp));
                            }else{
                                chargeGauge.setmSOC(demoCurrent);
                            }
                            VOLT.setText(String.format("%.1f",demoVolt));
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
                            else if( mUser.getBatteriesList().get(batteryIdx).engineState == 0 ){
                                demoCurrent = 0;
                                demoTemp = (demoTemp <=-5)?-5:demoTemp-0.5f;
                            }
                            if(currentChargeStylekitView!=null){
                                currentChargeStylekitView.setmValue(demoCurrent);
                            }
                            if(isChargeGauge){
                                chargeGauge.setmSOC(demoSOC);
                                chargeGauge.setmSOF(socToSof(demoSOC,demoTemp));
                            }else{
                                chargeGauge.setmSOC(demoCurrent);
                            }
                            tempGauge.setmCurrentVaule(demoTemp);
                            VOLT.setText(String.format("%.1f",demoVolt));
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
                            else if( mUser.getBatteriesList().get(batteryIdx).engineState == 0 ){
                                demoCurrent = 0;
                                demoTemp = (demoTemp <=-5)?-5:demoTemp-0.5f;
                            }
                            if(currentChargeStylekitView!=null){
                                currentChargeStylekitView.setmValue(demoCurrent);
                            }
                            if(isChargeGauge){
                                chargeGauge.setmSOC(demoSOC);
                                chargeGauge.setmSOF(socToSof(demoSOC,demoTemp));
                            }else{
                                chargeGauge.setmSOC(demoCurrent);
                            }
                            tempGauge.setmCurrentVaule(demoTemp);
                            VOLT.setText(String.format("%.1f",demoVolt));
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
                            }   else if( mUser.getBatteriesList().get(batteryIdx).engineState == 0 ){
                                demoCurrent = 0;
                                demoTemp = (demoTemp <=-5)?-5:demoTemp-0.5f;
                            }
                            if (currentChargeStylekitView != null) {
                                currentChargeStylekitView.setmValue(demoCurrent);
                            }
                            if (isChargeGauge) {
                                chargeGauge.setmSOC(demoSOC);
                                chargeGauge.setmSOF(socToSof(demoSOC,demoTemp));
                            } else {
                                chargeGauge.setmSOC(demoCurrent);
                            }
                            tempGauge.setmCurrentVaule(demoTemp);
                            VOLT.setText(String.format("%.1f", demoVolt));
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

package com.elitise.firmwareupdate;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;


import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;



/**
 * Created by andy on 7/26/16.
 */
public class FWupdate extends ServiceFragment{

//    private static final UUID ELT_BATT_MOBBAT_SERVICE_UUID = UUID
//            .fromString("AC55D02D-A7FE-4F64-A659-ABCB5C6D409D");
//
//    private static final UUID ELT_BATT_MOBBAT_CHARACTERISTIC_UUID = UUID
//            .fromString("A88C5B46-B899-473A-96C9-49B98173948D");
//    public static  String ELT_BATT_MOBBAT_SERVICE_UUID_WITH_SN = "AC55D02D-0XXX-4F64-ZZZZ-ABCB5C6D409D";
//
//    public static  String ELT_BATT_MOBBAT_CHARACTERISTIC_UUID_WITH_SN = "A88C5B46-0XXX-473A-ZZZZ-49B98173948D";
//
    private static final UUID ELT_BATT_MOBBAT_SERVICE_UUID = UUID
            .fromString("AC55D02D-0000-4F64-0000-ABCB5C6D409D");
//
//    private static final UUID ELT_BATT_MOBBAT_CHARACTERISTIC_UUID = UUID
//            .fromString("A88C5B46-0000-473A-0000-49B98173948D");

    private ServiceFragmentDelegate mDelegate;
    private GloableVar gloableVar = GloableVar.getInstance();

    private boolean updating = false;

    private final int cmdTypeMem = 32;



    private  final int FIXBUFFSIZE = 4096;
    private  int BUFFSIZE = FIXBUFFSIZE;

    private int mBuffCheckSum = 0;
    private int mImagePostion = 0;

    private TextView tv;
    private TextView appTV;


    private String  newText = "";
    private String mHexString;
    private String mSN;
    private byte[] mHexStringByteArray;
    private int mHexStringByteArrayLength;
    private byte [] lengthByte;
    private byte[] mImageWithLength;
    private int blockNum = 0;
    private static int attempts = 0;
    // GUI
    private Button updateBtn;

    private Button uploadBtn;

    private Button cancelBtn;

    private Button resetBtn;

    private Button BlinkLedBth;

    private ScrollView scrollview;

    private ConnectLEDView ledTX;

    private ConnectLEDView ledRX;

    private ProgressBarView progressBar;

    private  TextView progressNum;

    private TextView nodeNum;

    private TextView imageVersion;

    private TextView buildDate;

    private TextView imageSize;

    private String fileName;

    final Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    updateBtn.performClick();
                    break;
                case 1:
                    cancelBtn.performClick();
                    break;
                default:
                    break;
            }
            return false;
        }
    });
    private Timer timer = new Timer();
    private MaterialDialog finishUploadDialog;

    private  int nodes;
    List<Byte>  pkt = new ArrayList<Byte>();
    //timer

    long startTime = 0;
    long startTime1 = 0;
    long startTime2 = 0;
    private boolean isFirstTimeConnected = true;
    // GATT
    private BluetoothGattService mBatteryService;
    private BluetoothGattCharacteristic mBatteryCharacteristic;
    public FWupdate() {


    }



    private String newVesion;
    final long ONE_MEGABYTE = 1024 * 1024;
    //Lifecycle callbacks
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_fwupdate, container, false);
        tv = (TextView) view.findViewById(R.id.textView);
        appTV = (TextView) view.findViewById(R.id.currentVersion);
//        mHexString = getString(R.string.hexFile);
//        mHexString= mHexString.replace("\n","").replace("\r","").replaceAll(" ","");
//        mHexStringByteArray = mHexString.getBytes(StandardCharsets.UTF_8);
//        mHexStringByteArrayLength = mHexStringByteArray.length; //decrease size by 3 for length pkg

        resetBtn = (Button)view.findViewById(R.id.resetBtn);


        BlinkLedBth = (Button) view.findViewById(R.id.blinkBtn);

        ledRX = (ConnectLEDView) view.findViewById(R.id.RXLED);


        ledTX = (ConnectLEDView) view.findViewById(R.id.TXLED);

        progressNum = (TextView) view.findViewById(R.id.progressNum);

        progressNum.bringToFront();

        progressBar = (ProgressBarView)view.findViewById(R.id.progressBar);

        scrollview = (ScrollView) view.findViewById(R.id.scrollView);

        uploadBtn = (Button) view.findViewById(R.id.uploadBtn);

        nodeNum = (TextView) view.findViewById(R.id.nodes);

        imageVersion = (TextView) view.findViewById(R.id.ImageVersion);
        buildDate = (TextView) view.findViewById(R.id.lastUpdateTime);
        imageSize = (TextView) view.findViewById(R.id.ImageSize);

        finishUploadDialog = new MaterialDialog.Builder(getContext())
                .content("Press Ok to start update or cancel")
                .positiveText("Ok")
                .theme(Theme.DARK)
                .negativeText("Cancel")
                .negativeColor(Color.parseColor("#ff6529"))
                .positiveColor(Color.parseColor("#ff6529"))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        handler.sendEmptyMessage(0);
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        handler.sendEmptyMessage(1);
                    }
                })
                .build();
        infoPrint("Attempting to connect to battery...");

        BlinkLedBth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] cmd = SystemDefine.encodeCmd(SystemDefine.CMOMANDSTYPE.SERVICE_COMMANDS,
                        SystemDefine.SERVICE_CMD.BLINK_LED, new byte[4],SystemDefine.dataType.INTEGERX,0,9);
                if(mBatteryCharacteristic!=null) {
                    mBatteryCharacteristic.setValue(cmd);
                    mDelegate.sendNotificationToDevices(mBatteryCharacteristic);
                }
            }
        });
        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] cmd = SystemDefine.encodeCmd(SystemDefine.CMOMANDSTYPE.SERVICE_COMMANDS,
                        SystemDefine.SERVICE_CMD.FORCED_RESET_ALL, new byte[4],SystemDefine.dataType.INTEGERX,0,9);
                if(mBatteryCharacteristic!=null) {
                    mBatteryCharacteristic.setValue(cmd);
                    mDelegate.sendNotificationToDevices(mBatteryCharacteristic);
                }
            }
        });

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                 attempts = 0;
                 mImagePostion = 0;
                 BUFFSIZE = FIXBUFFSIZE;
                 blockNum = 0;

                 lengthByte = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(mHexStringByteArray.length).array();
                 mImageWithLength = new byte[mHexStringByteArrayLength+lengthByte.length-1];
                 System.arraycopy(lengthByte,0,mImageWithLength,0,lengthByte.length-1);
                 System.arraycopy(mHexStringByteArray,0,mImageWithLength,lengthByte.length-1,mHexStringByteArray.length);
                if(mBatteryCharacteristic!=null) {
                    mBatteryCharacteristic.setValue(sendCommand(CmdType.START_MEM_XFER));
                    mDelegate.sendNotificationToDevices(mBatteryCharacteristic);
                }
                 infoPrint("Start Upload");
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.VISIBLE);
                        progressNum.setVisibility(View.VISIBLE);
                        progressNum.setText("0.0%");
                        progressBar.setPercent(0f);
                    }
                });

            }
        });
        uploadBtn.setVisibility(View.GONE);
        updateBtn = (Button) view.findViewById(R.id.updateBtn);
        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                infoPrint("NOTICE - Do Not start engine during this process");
                infoPrint("NOTE - Bluetooth connection will be lost during upgrade");
                infoPrint("NOTE - The LED should light solid yellow followed by solid blue to indicate the firmware update is being applied." +
                        "if you do not see the LED change, please select the Update button again tp attempt the upgrade.");
                mDelegate.advertise();
                isFirstTimeConnected = true;
                if(mBatteryCharacteristic!=null) {
                    mBatteryCharacteristic.setValue(sendCommand(CmdType.APPLY_MEM_UPDATE));
                    mDelegate.sendNotificationToDevices(mBatteryCharacteristic);
                }
                if(ledRX.isBlinking()){
                    ledRX.stopBlink();
                }
                if(ledTX.isBlinking()){
                    ledTX.stopBlink();
                }
                ledTX.blinkLED(Color.parseColor("#00ff08"),true);
                ledRX.blinkLED(Color.parseColor("#ffa500"),true);

            }
        });

        cancelBtn = (Button) view.findViewById(R.id.cancelBtn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                infoPrint("Cancelled.");
                if(mBatteryCharacteristic!=null) {
                    mBatteryCharacteristic.setValue(sendCommand(CmdType.END_MEM_XFER));
                    mDelegate.sendNotificationToDevices(mBatteryCharacteristic);
                }
                progressBar.setVisibility(View.INVISIBLE);
                progressNum.setVisibility(View.INVISIBLE);
                progressNum.setText("0.0%");
                progressBar.setPercent(0f);
                updateBtn.setVisibility(View.GONE);
                uploadBtn.setVisibility(View.VISIBLE);
                BlinkLedBth.setVisibility(View.VISIBLE);
                resetBtn.setVisibility(View.VISIBLE);
                cancelBtn.setVisibility(View.GONE);
                if(ledRX.isBlinking()){
                   ledRX.stopBlink();
                }
                if(ledTX.isBlinking()){
                    ledTX.stopBlink();
                }


            }
        });

        final FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        //FirebaseOptions opts = FirebaseApp.getInstance().getOptions();
        //Log.e(TAG, "Bucket = " + opts.getStorageBucket()+"\n "+opts.toString());
       // StorageReference newFileVersion = storageRef.child("version.text");
        StorageReference newFileVersion = storage.getReferenceFromUrl("gs://firmwareupdate-tool.appspot.com/version.txt");

        newFileVersion.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                fileName = new String(bytes);
                String[] info = fileName.split(",");
                if(info.length==3) {
                    if (info[0] != null) {
                        String version = info[0].substring(1, 7);
                        imageVersion.setText(version);
                        fileName = "gs://firmwareupdate-tool.appspot.com/"+info[0];
                    }
                    if (info[1] != null) {
                        imageSize.setText(info[1]);
                    }
                    if (info[2] != null) {
                        buildDate.setText(info[2]);
                    }
                }
                int i =0;

                StorageReference firmware = storage.getReferenceFromUrl(fileName);
                firmware.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        mHexString = new String(bytes);
                        mHexString= mHexString.replace("\n","").replace("\r","").replaceAll(" ","");
                        mHexStringByteArray = mHexString.getBytes(StandardCharsets.UTF_8);
                        mHexStringByteArrayLength = mHexStringByteArray.length;
                       // Log.e("Data2", mHexString);
                        uploadBtn.setVisibility(View.VISIBLE);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.e("Version1","Fail");
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e("Version2","Fail");
            }
        });


       // Log.e("path",fileName);


        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        mBatteryCharacteristic =
                new BluetoothGattCharacteristic(UUID.fromString(gloableVar.getELT_BATT_MOBBAT_CHARACTERISTIC_UUID_WITH_SN()),
                        BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY|BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
                        BluetoothGattCharacteristic.PERMISSION_READ|BluetoothGattCharacteristic.PERMISSION_WRITE);
        mBatteryCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        mBatteryCharacteristic.addDescriptor(
                Peripheral.getClientCharacteristicConfigurationDescriptor());
        mBatteryService = new BluetoothGattService(UUID.fromString(gloableVar.getELT_BATT_MOBBAT_SERVICE_UUID_WITH_SN()),
                BluetoothGattService.SERVICE_TYPE_PRIMARY);
        mBatteryService.addCharacteristic(mBatteryCharacteristic);
        return mBatteryService;
    }

    @Override
    public ParcelUuid getServiceUUID() {
        return new ParcelUuid(ELT_BATT_MOBBAT_SERVICE_UUID);
    }




    public static final byte SLIP_END = (byte) 0xC0;
    public static final byte SLIP_ESC = (byte) 0xDB;
    public static final byte SLIP_ESC_END = (byte)0xDC;
    public static final byte SLIP_ESC_ESC = (byte)0xDD;


//    public static  boolean m_receivingSlip = false;
//    public static  boolean m_haveCompletepackA = false;
//    public static  byte[] ByteArray = null;
//    public static  byte[] decodeByteArray = null;


    private static final String TAG = Peripheral.class.getCanonicalName();

    @Override
    public int writeCharacteristic(BluetoothGattCharacteristic characteristic, int offset, byte[] value) {

        if(updating){
            updating = false;
            infoPrint("NOTICE - Firmware update completed successfully");
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.INVISIBLE);
                    progressNum.setVisibility(View.INVISIBLE);
                    progressNum.setText("0.0%");
                    progressBar.setPercent(0f);
                    updateBtn.setVisibility(View.GONE);
                    uploadBtn.setVisibility(View.VISIBLE);
                    BlinkLedBth.setVisibility(View.VISIBLE);
                    resetBtn.setVisibility(View.VISIBLE);
                    cancelBtn.setVisibility(View.GONE);
                    if(ledRX.isBlinking()){
                        ledRX.stopBlink();
                    }
                    if(ledTX.isBlinking()){
                        ledTX.stopBlink();
                    }
                }
            });
        }
        byte[] result = decode(value);
        int cmd = result[0];
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ledRX.blinkLED(Color.parseColor("#ffa500"),false);
            }
        });


        int commandClass;
        int commandID;
        byte[] parameter = null;
        int checksum;
        int dataType;
        int index;

        commandClass = value[1] & 0xE0;
        commandID = value[1] & 0x1F;
        parameter = combineBytes(value[6],value[7],value[8],value[9]);
        checksum = value[5];
        dataType = (value[4] & 0xE0) >> 5;
        index = (value[4] & 0x1F);

        if(commandClass == SystemDefine.CMOMANDSTYPE.HOST_COMMANDS){
            ParseHostCommand(commandID, parameter, dataType, index);
        }else {
            switch (cmd) {

                case CmdType.START_MEM_XFER:

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.VISIBLE);
                            progressNum.setVisibility(View.VISIBLE);
                            //cancelBtn.setEnabled(true);
                        }
                    });

                    if (result[5] == 1) {
                        Log.d(TAG, "ACK");
                        infoPrint("Starting firmware transfer to Battery Module 1...");
                        startBufferIMG(BUFFSIZE);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ledTX.blinkLED(Color.parseColor("#00ff08"),true);
                                updateBtn.setVisibility(View.GONE);
                                uploadBtn.setVisibility(View.GONE);
                                BlinkLedBth.setVisibility(View.GONE);
                                resetBtn.setVisibility(View.GONE);
                                cancelBtn.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                    break;
                case CmdType.VERIFY_IMAGE:
                    int checkSum = 0;
                    int buffLength = 0;
                    buffLength = (result[8] & 0xff) | ((result[9] & 0xff) << 8) | ((result[10] & 0xff) << 16) | ((result[11] & 0xff) << 24);
                    checkSum = (result[12] & 0xff) | ((result[13] & 0xff) << 8) | ((result[14] & 0xff) << 16) | ((result[15] & 0xff) << 24);

                    if (checkSum == mBuffCheckSum && buffLength == BUFFSIZE) {
                        Log.d(TAG, "buffer successful");

                        mBatteryCharacteristic.setValue(sendCommand(CmdType.WRITE_MEM_BLOCK));
                        mDelegate.sendNotificationToDevices(mBatteryCharacteristic);
                    } else {
                        //long estimatedTime2 = System.nanoTime() - startTime2;
                        //int estimatedTimeSec2 = (int) (estimatedTime2 / 1000000);
                        //infoPrint("Time used for verify: " + estimatedTimeSec2 + " ms");

                        isFirstTimeConnected = false;
                        String failed = "buffer transfer failed:\n length received by Battery--" + buffLength + "\n length send--" + BUFFSIZE +
                                "\n Checksum from Battery:" + checkSum + "\n Checksum from Image:" + mBuffCheckSum +
                                "\n Left Image Size--" + (mHexStringByteArrayLength - mImagePostion);
                        Log.d(TAG, failed);

                        //infoPrint(failed);

                    }

                    break;
                case CmdType.WRITE_MEM_BLOCK:
                    if (result[5] == 0) {
                        //long estimatedTime2 = System.nanoTime() - startTime2;
                        //int estimatedTimeSec2 = (int) (estimatedTime2 / 1000000);
                        //infoPrint("Time used for verify: " + estimatedTimeSec2 + " ms");

                        Log.d(TAG, "buffer write to flash successful:" + blockNum);
                        final float process = ((float) mImagePostion/(float)mHexStringByteArrayLength);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setPercent(process);
                                progressNum.setText(String.format("%.1f", process*100) + "%");
                            }
                        });

                        if (mImagePostion < (mHexStringByteArrayLength + 3)) {                      //add 3 bytes offset for the length header

                            if ((mHexStringByteArrayLength + 3 - mImagePostion) > FIXBUFFSIZE) {         //add 3 bytes offset for the length header
                                startBufferIMG(BUFFSIZE);
                            } else {
                                BUFFSIZE = mHexStringByteArrayLength + 3 - mImagePostion; //add 3 bytes offset for the length header
                                startBufferIMG(BUFFSIZE);
                            }
                        } else {
                            infoPrint("Firmware upload completed successfully");
                            if(nodes == 1){
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        updateBtn.setVisibility(View.VISIBLE);
                                        uploadBtn.setVisibility(View.GONE);
                                        BlinkLedBth.setVisibility(View.GONE);
                                        resetBtn.setVisibility(View.GONE);
                                        cancelBtn.setVisibility(View.GONE);
                                        Log.d(TAG, "Image transfer finished, start update");
                                        long estimatedTime = System.nanoTime() - startTime;
                                        int estimatedTimeSec = (int) (estimatedTime / 1000000000);
                                        infoPrint("Time used for uploading: " + estimatedTimeSec + " secs");
                                        infoPrint("Please select the Update button to perform the firmware update");
                                        progressBar.setVisibility(View.INVISIBLE);
                                        progressNum.setVisibility(View.INVISIBLE);
                                        finishUploadDialog.show();
                                        timer.schedule(new TimerTask() {
                                            @Override
                                            public void run() {
                                                 handler.sendEmptyMessage(0);
                                                finishUploadDialog.dismiss();
                                                timer.purge();
                                            }
                                        },30000);
                                        updating = true;


                                    }
                                });
                            }else {
                                infoPrint("Uploading battery firmware to all battery nodes...");
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressNum.setText("0%");
                                        progressBar.setPercent(0);
                                    }
                                });
                            }
                        }

                    } else {

                        Log.d(TAG, "buffer write to flash failed");
                        infoPrint("buffer write to flash failed, attempts: " + attempts);
                        mBatteryCharacteristic.setValue(sendCommand(CmdType.WRITE_MEM_BLOCK));
                        mDelegate.sendNotificationToDevices(mBatteryCharacteristic);
                        attempts++;
                    }
                    break;
                case CmdType.APPLY_MEM_UPDATE:
                    if (result[5] == 1) {
                        Log.d(TAG, "update successful");
                    }
                    break;

                case CmdType.NODE_UPLOAD_PROGRESS:
                    byte[] data = combineBytes((byte)0,value[7],value[8],value[9]);
                    int blockNum = ByteBuffer.wrap(data).getInt();
                    final float process = (blockNum*4096f >= mHexStringByteArrayLength) ?1:((float) blockNum *4096f/(float) mHexStringByteArrayLength);
                    final int idx = value[6];
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setPercent(process);
                            progressNum.setText(String.format("%.1f", process*100) + "%");

                            if(idx==nodes && process == 1f){
                                updateBtn.setVisibility(View.VISIBLE);
                                uploadBtn.setVisibility(View.GONE);
                                BlinkLedBth.setVisibility(View.GONE);
                                resetBtn.setVisibility(View.GONE);
                                cancelBtn.setVisibility(View.GONE);
                                Log.d(TAG, "Image transfer finished, start update");
                                long estimatedTime = System.nanoTime() - startTime;
                                int estimatedTimeSec = (int) (estimatedTime / 1000000000);
                                infoPrint("Time used for uploading: " + estimatedTimeSec + " secs");
                                infoPrint("Please select the Update button to perform the firmware update");
                                progressBar.setVisibility(View.INVISIBLE);
                                progressNum.setVisibility(View.INVISIBLE);
                                finishUploadDialog.show();
                                timer.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        handler.sendEmptyMessage(0);
                                        finishUploadDialog.dismiss();
                                        timer.purge();
                                    }
                                },30000);
                                updating = true;
                            }
                        }
                    });

                    break;
                default:

                    if (isFirstTimeConnected) {
                        infoPrint("Connection to the battery established");
                        infoPrint("NOTE: Please ensure that the LED on all battery nodes are blinking at the same rate before starting the firmware upload.This will ensure that all " +
                                "batteries are awake and ready for the firmware update.");
                        isFirstTimeConnected = false;
                    }

                    break;
            }
        }
        return 0;
    }

    public void ParseHostCommand(int commandID, byte[] parm, int dataType, final int index){
        float parmFloat = ByteBuffer.wrap(parm).order(ByteOrder.LITTLE_ENDIAN).getFloat();
        int parmInt = ByteBuffer.wrap(parm).order(ByteOrder.LITTLE_ENDIAN).getInt();
        switch (commandID) {
            case SystemDefine.HOST_CMD.SET_APP_VER:

                int major = parm[0] & 0xff;
                int minor = parm[1] & 0xff;
                int build = ((parm[3] & 0xff) << 8) | (parm[2] & 0xff);
                final String app_ver = Integer.toString(major) + "." + Integer.toString(minor) + "." + Integer.toString(build);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        appTV.setText(app_ver);
                    }
                });
                break;
            case SystemDefine.HOST_CMD.SET_NUM_BATT_NODES:
                nodes = parmInt;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        nodeNum.setText(Integer.toString(nodes));
                    }
                });

                break;
            default:
                break;
        }
    }

    public byte[] decode(byte[] ByteArray)
    {
        int idx;
        byte c;
        boolean escaped = false;
        byte[] decodeArray  = new byte[16];
        boolean beginingFound = false;
        int dest_pos = 0;
        for(idx = 0;idx < ByteArray.length;idx++){
            c = ByteArray[idx];
            if(c==SLIP_END){
                if(dest_pos == 0){
                    beginingFound = true;

                    continue;
                }
                else{

                    return decodeArray ;
                }


            }
            else if(escaped){
                if(c == SLIP_ESC_END)
                    c=SLIP_END;
                else if(c == SLIP_ESC_ESC)
                    c=SLIP_ESC;
                else{


                }
                escaped = false;
            }
            else if(c== SLIP_ESC)
            {
                escaped = true;
                continue;
            }

            if(beginingFound&&(dest_pos<16)){
                decodeArray[dest_pos++] = c;
            }
        }

        return decodeArray;
    }





    private byte[] sendCommand(int cmd){
        byte[] result = new byte[11];
        byte[] bufferResult = new byte[18];

        switch (cmd){
           case CmdType.START_MEM_XFER:
               startTime = System.nanoTime();
               lengthByte = intToByte(mHexStringByteArray.length);
               Log.d(TAG,"Length: "+mHexStringByteArray.length);
               result[0] = (byte) 0xC0;
               result[1]=(byte)(cmdTypeMem|CmdType.START_MEM_XFER);
               //packet length
               result[2]=9;
               result[3]=0;
               //REQUEST_MSG
               result[4]=1<<5;
               //checksum
               result[5]=0;
               //status
               result[6]=lengthByte[0];
               result[7]=lengthByte[1];
               result[8]=lengthByte[2];
               result[9]=0;
               result[10] = (byte) 0xC0;
               break;
            case CmdType.END_MEM_XFER:
                result[0] = (byte) 0xC0;
                result[1]=(byte)(cmdTypeMem|CmdType.END_MEM_XFER);
                //packet length
                result[2]=9;
                result[3]=0;
                //REQUEST_MSG
                result[4]=1<<5;
                //checksum
                result[5]=0;
                //status
                result[6]=0;
                result[7]=0;
                result[8]=0;
                result[9]=0;
                result[10] = (byte) 0xC0;
               break;
           case CmdType.VERIFY_IMAGE:
               result[0] = (byte) 0xC0;
               result[1]=(byte)(cmdTypeMem|CmdType.VERIFY_IMAGE);
               //packet length
               result[2]=9;
               result[3]=0;
               //REQUEST_MSG
               result[4]=1<<5;
               //checksum
               result[5]=0;
               //status
               result[6]=result[7]=result[8]=result[9]=0;

               result[10] = (byte) 0xC0;
               break;
           case CmdType.APPLY_MEM_UPDATE:
               result[0] = (byte) 0xC0;
               result[1]=(byte)(cmdTypeMem|CmdType.APPLY_MEM_UPDATE);
               result[2]=9;
               result[3]=0;
               //REQUEST_MSG
               result[4]=1<<5;
               //checksum
               result[5]=0;
               //status
               result[6]=result[7]=result[8]=result[9]=0;

               result[10] = (byte) 0xC0;
               break;
           case CmdType.GET_XFER_BLOCK_SIZE:
               result[0]=(byte)(cmdTypeMem|CmdType.GET_XFER_BLOCK_SIZE);
               break;
           case CmdType.BUFFER_IMG_DATA:
               bufferResult[0] = (byte) 0xC0;
               bufferResult[1]=(byte)(cmdTypeMem|CmdType.BUFFER_IMG_DATA);
               //packet length
               bufferResult[2]=(byte)(pkt.size()+8);
               bufferResult[3]=0;
               //REQUEST_MSG
               bufferResult[4]=1<<5;
               //checksum
               bufferResult[5]=0;
               //status
               bufferResult[6]=bufferResult[7]=bufferResult[8]=0;
               for(int i = 0; i <pkt.size();i++){
                   bufferResult[9+i] = pkt.get(i);
               }
               bufferResult[17] = (byte) 0xC0;
               break;
           case CmdType.WRITE_MEM_BLOCK:
               byte [] blockNumByte;
               blockNumByte = intToByte(blockNum);
               Log.d(TAG,"Length: "+BUFFSIZE);
               result[0] = (byte) 0xC0;
               result[1]=(byte)(cmdTypeMem|CmdType.WRITE_MEM_BLOCK);
               result[2]=9;
               result[3]=0;
               //REQUEST_MSG
               result[4]=1<<5;
               //checksum
               result[5]=0;
               //
               result[6] = blockNumByte[0];
               result[7] = blockNumByte[1];
               result[8] = blockNumByte[2];
               result[9] = 0;

               result[10] = (byte) 0xC0;
               blockNum++;
               break;
           case CmdType.READ_MEM_BLOCK:
               result[0] = (byte) 0xC0;
               result[1]=(byte)(cmdTypeMem|CmdType.READ_MEM_BLOCK);
               break;
           case CmdType.UPDATE_RESET:
               result[0]=(byte)(cmdTypeMem|CmdType.UPDATE_RESET);
               break;
           case CmdType.SET_BAUDRATE:
               result[0]=(byte)(cmdTypeMem|CmdType.SET_BAUDRATE);
               break;
           case CmdType.GET_BAUDRATE:
               result[0]=(byte)(cmdTypeMem|CmdType.GET_BAUDRATE);
               break;
       }

        if(cmd != CmdType.BUFFER_IMG_DATA)
            return result;
        else
            return bufferResult;
    }



    public void startBufferIMG(int len){


        byte [] cmd2 ;
        //byte [] checkSumByte = new byte[4];
        mBuffCheckSum= CheckSum(mImageWithLength,mImagePostion,len);
//        checkSumByte = intToByte(mCheckSum);
//        Log.v(TAG,"CRC: "+Arrays.toString(checkSumByte));
        int j = 0;
        startTime1 = System.nanoTime();
        for (int i = 0; i < len; i++) {
                pkt.add(mImageWithLength[mImagePostion++]);
                if ((i % 8) == 7||(i==len-1)) {
                    j++;
                    cmd2 = sendCommand(CmdType.BUFFER_IMG_DATA);
                    mBatteryCharacteristic.setValue(cmd2);
                    mDelegate.sendNotificationToDevices(mBatteryCharacteristic);
                    pkt.clear();
//                    if((j%6)==0) {
//                        try {
//                            Thread.sleep(10);
//                        } catch (InterruptedException e) {
//                            Thread.currentThread().interrupt();
//                        }
//                    }
//                    else {
//                        try {
//                            Thread.sleep(2);
//                        } catch (InterruptedException e) {
//                            Thread.currentThread().interrupt();
//                        }
//                    }
                    try {
                        Thread.sleep(15);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                }
             }

            mBatteryCharacteristic.setValue(sendCommand(CmdType.VERIFY_IMAGE));
            mDelegate.sendNotificationToDevices(mBatteryCharacteristic);
                //long estimatedTime1 = System.nanoTime() - startTime1;
                //int estimatedTimeSec1 = (int) (estimatedTime1 / 1000000);
                //infoPrint("Time used for uploading buffer: " + estimatedTimeSec1 + " ms");
                startTime2 = System.nanoTime();
    }





    public class CmdType {
        public static final int START_MEM_XFER = 32;
        public static final int END_MEM_XFER = 33;
        public static final int VERIFY_IMAGE = 34;
        public static final int APPLY_MEM_UPDATE = 35;
        public static final int GET_XFER_BLOCK_SIZE = 36;
        public static final int BUFFER_IMG_DATA = 37;
        public static final int WRITE_MEM_BLOCK = 38;
        public static final int READ_MEM_BLOCK = 39;
        public static final int UPDATE_RESET = 40;
        public static final int SET_BAUDRATE = 41;
        public static final int GET_BAUDRATE = 42;
        public static final int NODE_UPLOAD_PROGRESS = 43;
    }

    public static byte[] intToByte(int val){
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(val).array();
    }

    public int CheckSum(byte[] array,int off, int len){
        int checksum = 0;
        int val = 0;
        List<Byte> tempArray = new ArrayList<Byte>();
        for(int i = 0;i<len;i+=4){
            tempArray.clear();
            val = 0;
            if(len-i<2){
                tempArray.add(array[i+off]);
                val = tempArray.get(0)&0xff;
            }
            else if(len-i<3){
                tempArray.add(array[i+off]);
                tempArray.add(array[i+off+1]);
                val = (tempArray.get(0)&0xff)|((tempArray.get(1)&0xff)<<8);
            }
            else if(len-i<4){
                tempArray.add(array[i+off]);
                tempArray.add(array[i+off+1]);
                tempArray.add(array[i+off+2]);
                val = (tempArray.get(0)&0xff)|((tempArray.get(1)&0xff)<<8)|((tempArray.get(2)&0xff)<<16);
            }
            else{
                tempArray.add(array[i+off]);
                tempArray.add(array[i+off+1]);
                tempArray.add(array[i+off+2]);
                tempArray.add(array[i+off+3]);
                val = (tempArray.get(0)&0xff)|((tempArray.get(1)&0xff)<<8)|((tempArray.get(2)&0xff)<<16)|((tempArray.get(3)&0xff)<<24);

            }

            checksum^=val;
        }

        return checksum;

    }


     void infoPrint(String s){
         final String temp = s;
         getActivity().runOnUiThread(new Runnable() {
             @Override
             public void run() {
                 newText +="\n"+temp;
                 tv.setText(newText);
                 scrollview.fullScroll(View.FOCUS_DOWN);
             }
         });
     }


//    public static byte[] combineFourBytes(byte b1,byte b2,byte b3,byte b4){
//        byte[] temp = new byte[4];
//        temp[0]=b1;
//        temp[1]=b2;
//        temp[2]=b3;
//        temp[3]=b4;
//        return temp;
//    }

    public static byte[] combineBytes(byte... bs){
        byte[]temp = new byte[bs.length];
        for(int i = 0;i <bs.length;i++){
            temp[i]=bs[i];
                }
        return temp;
    }


}


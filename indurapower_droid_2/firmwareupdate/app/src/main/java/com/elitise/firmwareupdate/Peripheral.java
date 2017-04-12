package com.elitise.firmwareupdate;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import io.fabric.sdk.android.Fabric;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

/**
 * Created by andy on 7/26/16.
 */
public class Peripheral extends AppCompatActivity implements ServiceFragment.ServiceFragmentDelegate {

    private static final int REQUEST_ENABLE_BT = 1;



    private GloableVar gloableVar;




    private static final String TAG = Peripheral.class.getCanonicalName();
    private static final String CURRENT_FRAGMENT_TAG = "CURRENT_FRAGMENT";



    private static final UUID CLIENT_CHARACTERISTIC_CONFIGURATION_UUID = UUID
            .fromString("00002902-0000-1000-8000-00805f9b34fb");


    private ServiceFragment mCurrentServiceFragment;

    private BluetoothGattService mBluetoothGattService;
    private HashSet<BluetoothDevice> mBluetoothDevices;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private AdvertiseData mAdvData;
    private AdvertiseSettings mAdvSettings;
    private BluetoothLeAdvertiser mAdvertiser;







    private final AdvertiseCallback mAdvCallback = new AdvertiseCallback() {
        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            Log.e(TAG, "Not broadcasting: " + errorCode);
        }

        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            Log.v(TAG, "Broadcasting");
            // mAdvStatus.setText(R.string.status_advertising);
        }
    };


    private BluetoothGattServer mGattServer;
    private final BluetoothGattServerCallback mGattServerCallback = new BluetoothGattServerCallback() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, final int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
            if (status == BluetoothGatt.GATT_SUCCESS) {
               //if (newState == BluetoothGatt.STATE_CONNECTED&&device.getAddress().toString().equals("00:07:80:B5:B9:80")) {
                if (newState == BluetoothGatt.STATE_CONNECTED){
                    mBluetoothDevices.add(device);

                    //updateConnectedDevicesStatus();
                   //mAdvertiser.stopAdvertising(mAdvCallback);
                    Log.v(TAG, "Connected to device: " + device.getAddress());
                   // mAdvertiser.stopAdvertising(mAdvCallback);
                } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                    mBluetoothDevices.remove(device);
                    //mAdvertiser.startAdvertising(mAdvSettings,mAdvData,mAdvCallback);
                    //updateConnectedDevicesStatus();
                    Log.v(TAG, "Disconnected from device");
                }
            } else {
                mBluetoothDevices.remove(device);
                //updateConnectedDevicesStatus();
                // There are too many gatt errors (some of them not even in the documentation) so we just
                // show the error to the user.
//                final String errorMessage = getString(R.string.status_errorWhenConnecting) + ": " + status;
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(Peripheral.this, errorMessage, Toast.LENGTH_LONG).show();
//                    }
//                });
                Log.e(TAG, "Error when connecting: " + status);
            }
        }


        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset,
                                                BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            Log.d(TAG, "Device tried to read characteristic: " + characteristic.getUuid());
            Log.d(TAG, "Value: " + Arrays.toString(characteristic.getValue()));
            if (offset != 0) {
                mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_INVALID_OFFSET, offset,
            /* value (optional) */ null);
                return;
            }
            mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS,
                    offset, characteristic.getValue());
        }

        @Override
        public void onNotificationSent(BluetoothDevice device, int status) {
            super.onNotificationSent(device, status);
            //Log.v(TAG, "Notification sent. Status: " + status);
        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId,
                                                 BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded,
                                                 int offset, byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite,
                    responseNeeded, offset, value);
            Log.v(TAG, "Characteristic Write request: " + Arrays.toString(value));
            int status = mCurrentServiceFragment.writeCharacteristic(characteristic, offset, value);
            if (responseNeeded) {
                mGattServer.sendResponse(device, requestId, status,
            /* No need to respond with an offset */ 0,
            /* No need to respond with a value */ null);
            }
        }

        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId,
                                             BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded,
                                             int offset,
                                             byte[] value) {
            Log.v(TAG, "Descriptor Write Request " + descriptor.getUuid() + " " + Arrays.toString(value));
            super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded,
                    offset, value);
            if(responseNeeded) {
                mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS,
            /* No need to respond with offset */ 0,
            /* No need to respond with a value */ null);
            }
        }
    };




    /////////////////////////////////
    ////// Lifecycle Callbacks //////
    /////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        gloableVar = GloableVar.getInstance();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        mBluetoothDevices = new HashSet<>();
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
//        byte[] data = {0x0,0x0,0x0,0x1};
//        //int blockNum = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getInt();
//        int blockNum = ByteBuffer.wrap(data).getInt();
//        Log.v(TAG, "Descriptor Write Request " + blockNum);
        scanBarcode();


        mCurrentServiceFragment = new FWupdate();

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, mCurrentServiceFragment, CURRENT_FRAGMENT_TAG)
                .commit();




        mAdvSettings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW)
                .setConnectable(true)
                .build();

        Window wd = this.getWindow();
        wd.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        wd.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        wd.setStatusBarColor(Color.parseColor("#000000"));
        //wd.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
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



    @Override
    protected void onStart() {
        super.onStart();

        //resetStatusViews();
        // If the user disabled Bluetooth when the app was in the background,
        // openGattServer() will return null.
        mGattServer = mBluetoothManager.openGattServer(this, mGattServerCallback);
        if (mGattServer == null) {
            ensureBleFeaturesAvailable();
            return;
        }
        // Add a service for a total of three services (Generic Attribute and Generic Access
        // are present by default).
//        mGattServer.addService(mBluetoothGattService);
//        if (mBluetoothAdapter.isMultipleAdvertisementSupported()) {
//            mAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
//            mAdvertiser.startAdvertising(mAdvSettings, mAdvData, mAdvCallback);
//        } else {
//            //mAdvStatus.setText(R.string.status_noLeAdv);
//        }
    }


    @Override
    protected void onStop() {

        super.onStop();
        disconnectFromDevices();
        if (mGattServer != null) {
            mGattServer.close();
        }
        if (mBluetoothAdapter.isEnabled() && mAdvertiser != null) {
            // If stopAdvertising() gets called before close() a null
            // pointer exception is raised.
            mAdvertiser.stopAdvertising(mAdvCallback);
        }
        //resetStatusViews();
    }

    @Override
    public void sendNotificationToDevices(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothDevices.isEmpty()) {
            Toast.makeText(this, R.string.bluetoothDeviceNotConnected, Toast.LENGTH_SHORT).show();
        } else {
            boolean indicate = (characteristic.getProperties()
                    & BluetoothGattCharacteristic.PROPERTY_NOTIFY)
                    == BluetoothGattCharacteristic.PROPERTY_NOTIFY;
            for (BluetoothDevice device : mBluetoothDevices) {
                // true for indication (acknowledge) and false for notification (unacknowledge).
               mGattServer.notifyCharacteristicChanged(device, characteristic, false);
            }
        }
    }

    @Override
    public void advertise() {
        mAdvertiser.startAdvertising(mAdvSettings,mAdvData,mAdvCallback);
    }

    ///////////////////////
    ////// Bluetooth //////
    ///////////////////////
    public static BluetoothGattDescriptor getClientCharacteristicConfigurationDescriptor() {
        return new BluetoothGattDescriptor(CLIENT_CHARACTERISTIC_CONFIGURATION_UUID,
                (BluetoothGattDescriptor.PERMISSION_READ | BluetoothGattDescriptor.PERMISSION_WRITE));
    }

    private void ensureBleFeaturesAvailable() {
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.bluetoothNotSupported, Toast.LENGTH_LONG).show();
            Log.e(TAG, "Bluetooth not supported");
            finish();
        } else if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }
    private void disconnectFromDevices() {
        Log.d(TAG, "Disconnecting devices...");
        for (BluetoothDevice device : mBluetoothManager.getConnectedDevices(
                BluetoothGattServer.GATT)) {
            Log.d(TAG, "Devices: " + device.getAddress() + " " + device.getName());
            mGattServer.cancelConnection(device);
        }
    }



    private static final int RC_BARCODE_CAPTURE = 9001;
    private  void scanBarcode(){
        new MaterialDialog.Builder(this)
                .title("Scan Barcode")
                .theme(Theme.DARK)
                .titleColor(Color.parseColor("#ff6529"))
                .titleGravity(GravityEnum.CENTER)
                .content("Please use the camera to scan the barcode or manually enter the Serial Number.")
                .input("Enter Serial Number Or Tap Scan", null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(final MaterialDialog dialog, CharSequence input) {
                        gloableVar.setmSN(input.toString());
                        gloableVar.CustomUUID();
                        mAdvData = new AdvertiseData.Builder()
                                .setIncludeDeviceName(false)
                                .setIncludeTxPowerLevel(false)
                                .addServiceUuid(new ParcelUuid(UUID.fromString(gloableVar.getELT_BATT_MOBBAT_SERVICE_UUID_WITH_SN())))
                                .build();
                        mBluetoothGattService = mCurrentServiceFragment.getBluetoothGattService();
                        mGattServer.addService(mBluetoothGattService);
                        if (mBluetoothAdapter.isMultipleAdvertisementSupported()) {
                            mAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
                            mAdvertiser.startAdvertising(mAdvSettings, mAdvData, mAdvCallback);
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
                        Intent intent =new Intent(getBaseContext(),BarcodeCaptureActivity.class);
                        intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);
                        intent.putExtra(BarcodeCaptureActivity.UseFlash,false);
                        startActivityForResult(intent,RC_BARCODE_CAPTURE);
                    }
                })
                .cancelable(false)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                if (!mBluetoothAdapter.isMultipleAdvertisementSupported()) {
                    Toast.makeText(this, R.string.bluetoothAdvertisingNotSupported, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Advertising not supported");
                }
                onStart();
            } else {
                Toast.makeText(this, R.string.bluetoothNotEnabled, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Bluetooth not enabled");
                finish();
            }
        }
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    gloableVar.setmSN(barcode.displayValue.toString());
                    gloableVar.CustomUUID();
                    mBluetoothGattService = mCurrentServiceFragment.getBluetoothGattService();
                    Log.d(FWupdate.class.getName(), "Barcode read: " + barcode.displayValue);
                    mAdvData = new AdvertiseData.Builder()
                            .setIncludeDeviceName(false)
                            .setIncludeTxPowerLevel(false)
                            .addServiceUuid(new ParcelUuid(UUID.fromString(gloableVar.getELT_BATT_MOBBAT_SERVICE_UUID_WITH_SN())))
                            .build();
                    mGattServer.addService(mBluetoothGattService);
                    if (mBluetoothAdapter.isMultipleAdvertisementSupported()) {
                        mAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
                        mAdvertiser.startAdvertising(mAdvSettings, mAdvData, mAdvCallback);
                    }
                }
            } else {
                Log.d(FWupdate.class.getName(), "No barcode captured, intent data is null");
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


}

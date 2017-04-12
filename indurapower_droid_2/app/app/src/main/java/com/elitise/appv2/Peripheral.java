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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.elitise.appv2.ServiceFragment.ServiceFragmentDelegate;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class Peripheral extends AppCompatActivity implements ServiceFragmentDelegate{

  private static final int REQUEST_ENABLE_BT = 1;
  //private boolean connect = false;
  private ArrayList<Observer> obs = new ArrayList<>();
  private SharedPreferences sharedPrefs;
  //private GestureDetectorCompat mDetetor;
  private boolean enableAnimationFragment = false;
  private String connectedMac = null;
  private Logo_pathsView logoView;
  private  BluetoothDevice mDevice;

  private TextView statusBar;

  private boolean isTablet = true;

  private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
  private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
//  @Override
//  public void registerObserver(Observer o) {
//    obs.add(o);
//  }
//
//  @Override
//  public void removeObserver(Observer o) {
//    obs.remove(o);
//  }

//  @Override
//  public void notifyObservers() {
//    for (Observer o : obs) {
//      o.update(this.connect);
//
//    }
//  }


  private static final String TAG = Peripheral.class.getCanonicalName();
  private static final String CURRENT_FRAGMENT_TAG = "CURRENT_FRAGMENT";

  private static final UUID CLIENT_CHARACTERISTIC_CONFIGURATION_UUID = UUID
          .fromString("00002902-0000-1000-8000-00805f9b34fb");

  private static final UUID CLIENT_CHARACTERISTIC_EXTENDED_UUID = UUID
          .fromString("00002900-0000-1000-8000-00805f9b34fb");

  private ServiceFragment mCurrentServiceFragment;
  private ServiceFragment diagFragment;
  private ServiceFragment phoneFragment;
  private ServiceFragment normalFragment;
  private ChargeAnimationFragment mChargeFragment;
  private BluetoothGattService mBluetoothGattService;
  private HashSet<BluetoothDevice> mBluetoothDevices;
  private BluetoothManager mBluetoothManager;
  private BluetoothAdapter mBluetoothAdapter;
  private AdvertiseData mAdvData;
  private AdvertiseData mScanResponse;
  private AdvertiseSettings mAdvSettings;
  private BluetoothLeAdvertiser mAdvertiser;

  private String name;
  private int batteryIdx;

  private UserData mUser = UserData.getInstance();


  private final AdvertiseCallback mAdvCallback = new AdvertiseCallback() {
    @Override
    public void onStartFailure(int errorCode) {
      super.onStartFailure(errorCode);
      Log.e(TAG, "Not broadcasting: " + errorCode);
//      int statusText;
//      switch (errorCode) {
//        case ADVERTISE_FAILED_ALREADY_STARTED:
//          statusText = R.string.status_advertising;
//          Log.w(TAG, "App was already advertising");
//
//          break;
//        case ADVERTISE_FAILED_DATA_TOO_LARGE:
//          statusText = R.string.status_advDataTooLarge;
//          break;
//        case ADVERTISE_FAILED_FEATURE_UNSUPPORTED:
//          statusText = R.string.status_advFeatureUnsupported;
//          break;
//        case ADVERTISE_FAILED_INTERNAL_ERROR:
//          statusText = R.string.status_advInternalError;
//          break;
//        case ADVERTISE_FAILED_TOO_MANY_ADVERTISERS:
//          statusText = R.string.status_advTooManyAdvertisers;
//          break;
//        default:
//          statusText = R.string.status_notAdvertising;
//          Log.wtf(TAG, "Unhandled error: " + errorCode);
//      }
//      //mAdvStatus.setText(statusText);
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
    public void onConnectionStateChange(final BluetoothDevice device, final int status, int newState) {

      boolean enableBleKeepAdv = sharedPrefs.getBoolean(getString(R.string.pref_ble_adv_mode_key), true);

      super.onConnectionStateChange(device, status, newState);

      if (connectedMac == null) {
        connectedMac = device.getAddress();
      }

      if (status == BluetoothGatt.GATT_SUCCESS && connectedMac.equals(device.getAddress())) {

        if (newState == BluetoothGatt.STATE_CONNECTED) {
          mBluetoothDevices.add(device);

          if (!enableBleKeepAdv) {
            mAdvertiser.stopAdvertising(mAdvCallback);
          }
          // createBond2();
          //Log.v(TAG, "Connected to device: " + device.getAddress());
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              statusBar.setText("Status: Connected to device --- "+device.getAddress());
            }
          });

          if (batteryIdx != -1) { //bond to MAC addre0ss
            boolean isExist = false;
            String bName = "N/A";
            for (int i = 0; i < mUser.getBatteriesList().size(); i++) {   //check existence
              if (mUser.getBatteriesList().get(i).getMac() != null) {
                if (mUser.getBatteriesList().get(i).getMac().equals(device.getAddress())) {
                  isExist = true;
                  bName = mUser.getBatteriesList().get(i).mName;
                }
              }
            }
            if (mUser.getBatteriesList().get(batteryIdx).getMac() == null) {
              mDevice = device;
              mUser.getBatteriesList().get(batteryIdx).setMac(device.getAddress()); //first time assign MAC address to the battery
              //connect = true;
              //notifyObservers();
              if (!isExist) {
                mUser.getBatteriesList().get(batteryIdx).setMac(device.getAddress()); //first time assign MAC address to the battery
                ///connect = true;
                //notifyObservers();
              } else {
                final String name = bName;
                final String addr = device.getAddress();
                runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                    new MaterialDialog.Builder(Peripheral.this)
                            .content("This battery has already been connected to " + name+"\n do you still want to create this one ?")
                            .negativeColor(Color.parseColor("#ff6529"))
                            .positiveColor(Color.parseColor("#ff6529"))
                            .positiveText("Ok")
                            .negativeText("Cancel")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                              @Override
                              public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                mUser.getBatteriesList().get(batteryIdx).setMac(addr);
                                //connect = true;
                                //notifyObservers();
                              }
                            })
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                              @Override
                              public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                disconnectFromDevices();
                                mUser.getBatteriesList().remove(batteryIdx);
                                finish();

                              }
                            })
                            .show();
                  }
                });
              }
            } else {
              if (mUser.getBatteriesList().get(batteryIdx).getMac().equals(device.getAddress())) { //compare with stored MAC address
                mDevice = device;
                //connect = true;
                //notifyObservers();
              } else {
                if (isExist) {
                  disconnectFromDevices();
                  final String name = bName;
                  runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                      new MaterialDialog.Builder(Peripheral.this)
                              .theme(Theme.LIGHT)
                              .content("This battery has already been connected to " + name + " Please switch to " + name + " .")
                              .positiveColor(Color.parseColor("#ff6529"))
                              .positiveText("Ok")
                              .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                  finish();
                                }
                              })
                              .autoDismiss(true)
                              .show();
                    }
                  });
                } else {
                  Log.v(TAG, "Not Exist\n");
                  disconnectFromDevices();
                }
              }
            }
          } else {
            finish();
          }
        } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
          mBluetoothDevices.remove(device);
          connectedMac = null;

          if (!enableBleKeepAdv) {
            mAdvertiser.startAdvertising(mAdvSettings, mAdvData, mScanResponse,mAdvCallback);
          }
          Log.v(TAG, "Disconnected from device");
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              statusBar.setText("Status: Disconnected from device");
            }
          });

          //connect = false;
          //notifyObservers();
        }
      } else if (status == BluetoothGatt.GATT_SUCCESS && !connectedMac.equals(device.getAddress())) {
        mGattServer.cancelConnection(device);
        mBluetoothDevices.remove(device);
        Log.v(TAG, "Already connect to a battery \n");
      } else {
        mBluetoothDevices.remove(device);

        // There are too many gatt errors (some of them not even in the documentation) so we just
        // show the error to the user.
        final String errorMessage = getString(R.string.status_errorWhenConnecting) + ": " + status;
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            Toast.makeText(Peripheral.this, errorMessage, Toast.LENGTH_LONG).show();
          }
        });
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
      Log.v(TAG, "Notification sent. Status: " + status);
    }

    @Override
    public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId,
                                             BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded,
                                             int offset, byte[] value) {
      super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite,
              responseNeeded, offset, value);
      //Log.v(TAG, "Characteristic Write request: " + Arrays.toString(value));
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
      if (responseNeeded) {
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


    Intent I = getIntent();
    name = I.getStringExtra("mBname");
    batteryIdx = I.getIntExtra("idx", -1);
    if(!sNExist()) {
      String mSN = I.getStringExtra("mSN");
      saveSN(mSN);
    }
    isTablet = isTabletDevice();


//    FirebaseStorage storage = FirebaseStorage.getInstance();
//    StorageReference storageRef = storage.getReference();
//    FirebaseOptions opts = FirebaseApp.getInstance().getOptions();
//    Log.e(TAG, "Bucket = " + opts.getStorageBucket()+"\n URL"+opts.getApplicationId());
    //mDetetor = new GestureDetectorCompat(this, new MyGestureListener());

    setContentView(R.layout.activity_peripherals);
    TextView demoText = (TextView) findViewById(R.id.demoText);
    if(mUser.getBatteriesList().get(batteryIdx).isDemo){
      demoText.setVisibility(View.VISIBLE);
      demoText.bringToFront();
    }else {
      demoText.setVisibility(View.INVISIBLE);
    }
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    mBluetoothDevices = new HashSet<>();
    mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
    mBluetoothAdapter = mBluetoothManager.getAdapter();
    sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    statusBar = (TextView) findViewById(R.id.statusBar);
//    boolean enableDiag = sharedPrefs.getBoolean(getString(R.string.pref_mode_key), false);
//

//    if (!enableDiag)
//      mCurrentServiceFragment = new BatteryServiceFragment();
//    else
//      mCurrentServiceFragment = new BatteryServiceDiagFragment();
    if(savedInstanceState != null){
      phoneFragment  = (PhoneLayoutFragement) getSupportFragmentManager().findFragmentByTag("PHONE");
      normalFragment = (BatteryServiceFragment)getSupportFragmentManager().findFragmentByTag("NORMAL");
      diagFragment = (BatteryServiceDiagFragment)getSupportFragmentManager().findFragmentByTag("DIAG");
      if (isTablet) {
        mCurrentServiceFragment = normalFragment;
      } else {
        mCurrentServiceFragment = phoneFragment;
      }

    }else {
      diagFragment = new BatteryServiceDiagFragment();
      normalFragment = new BatteryServiceFragment();
      phoneFragment = new PhoneLayoutFragement();
      //mCurrentServiceFragment = normalFragment;

      Bundle bd = new Bundle();
      bd.putString("mBname", name);
      bd.putInt("bIndex", batteryIdx);
      diagFragment.setArguments(bd);
      normalFragment.setArguments(bd);
      phoneFragment.setArguments(bd);
      if (isTablet) {
        mCurrentServiceFragment = normalFragment;
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.normal_fragment_container, normalFragment, "NORMAL")
                .add(R.id.diag_fragment_container, diagFragment, "DIAG")
                .hide(diagFragment)
                .show(normalFragment)
                .commit();
      } else {
        mCurrentServiceFragment = phoneFragment;
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.phone_fragment_container, phoneFragment, "PHONE")
                .add(R.id.normal_fragment_container, normalFragment, "NORMAL")
                .add(R.id.diag_fragment_container, diagFragment, "DIAG")
                .hide(diagFragment)
                .hide(normalFragment)
                .show(phoneFragment)
                .commit();
      }
      mCurrentServiceFragment.setArguments(bd);
    }


    if(!mUser.getBatteriesList().get(batteryIdx).isBonded) {
      mBluetoothGattService = mCurrentServiceFragment.getBluetoothGattService();
    }
    else {
      mBluetoothGattService = mCurrentServiceFragment.getBluetoothGattServiceCustom();
    }

    mAdvSettings = new AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(true)
            .setTimeout(0)
            .build();

    mAdvData = new AdvertiseData.Builder()
              .setIncludeDeviceName(false)
              .setIncludeTxPowerLevel(false)
              .addServiceUuid(mCurrentServiceFragment.getServiceUUID(mUser.getBatteriesList().get(batteryIdx).isBonded))
              .build();

    mScanResponse = new AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .setIncludeTxPowerLevel(true)
            .build();


//
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
    setSupportActionBar(toolbar);

    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setHomeButtonEnabled(false);


    getSupportActionBar().setDisplayShowTitleEnabled(false);

    logoView = (Logo_pathsView) findViewById(R.id.logoBar);

    Context context = getApplicationContext(); // or activity.getApplicationContext()
    PackageManager packageManager = context.getPackageManager();
    String packageName = context.getPackageName();

    String myVersionName = "1.0.0"; // initialize String

    try {
      myVersionName = packageManager.getPackageInfo(packageName, 0).versionName;
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
    }
    logoView.setVersion(myVersionName);



    if (!mBluetoothAdapter.isMultipleAdvertisementSupported()) {
      new MaterialDialog.Builder(this)
              .title("BLE ERROR")
              .content("Sorry, this device not support BLE peripheral mode")
              .positiveText("CONFIRM")
              .onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                  finish();
                }
              });
    }




  }



  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    Window wd = this.getWindow();
    wd.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    wd.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
    wd.setStatusBarColor(Color.parseColor("#000000"));
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
        //TODO(g-ortuno): UX for asking the user to activate bt
        Toast.makeText(this, R.string.bluetoothNotEnabled, Toast.LENGTH_LONG).show();
        Log.e(TAG, "Bluetooth not enabled");
        finish();
      }
    }
  }


  @Override
  protected void onStart() {
    super.onStart();
    ensureBleFeaturesAvailable();
    //resetStatusViews();
    // If the user disabled Bluetooth when the app was in the background,
    // openGattServer() will return null.
    mGattServer = mBluetoothManager.openGattServer(this, mGattServerCallback);
    if (mGattServer == null) {
      return;
    }
    // Add a service for a total of three services (Generic Attribute and Generic Access
    // are present by default).
    mGattServer.addService(mBluetoothGattService);

    if (mBluetoothAdapter.isMultipleAdvertisementSupported()) {
      mAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
      if(!mUser.getBatteriesList().get(batteryIdx).isDemo) {
        mAdvertiser.startAdvertising(mAdvSettings, mAdvData, mScanResponse, mAdvCallback);
      }
    } else {
      //mAdvStatus.setText(R.string.status_noLeAdv);
    }
  }


  @Override
  protected void onStop() {

    super.onStop();
    disconnectFromDevices();
    GsonBuilder gsonb = new GsonBuilder();
    Gson gson = gsonb.create();
    String ownbatteries = gson.toJson(mUser.getBatteriesList());
    SharedPreferences prefs = this.getSharedPreferences("userdata", Context.MODE_PRIVATE);
    SharedPreferences.Editor e = prefs.edit();
    e.putString("batteris", ownbatteries);
    e.apply();
    if (mGattServer != null) {
      mGattServer.close();
    }
    if (mBluetoothAdapter.isEnabled() && mAdvertiser != null) {
      // If stopAdvertising() gets called before close() a null
      // pointer exception is raised.

      mAdvertiser.stopAdvertising(mAdvCallback);
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);

  }

  @Override
  public void sendNotificationToDevices(BluetoothGattCharacteristic characteristic) {
    if (mBluetoothDevices.isEmpty()) {
      Toast.makeText(this, R.string.bluetoothDeviceNotConnected, Toast.LENGTH_SHORT).show();
    } else {
//      boolean indicate = (characteristic.getProperties()
//              & BluetoothGattCharacteristic.PROPERTY_INDICATE)
//              == BluetoothGattCharacteristic.PROPERTY_INDICATE;
      for (BluetoothDevice device : mBluetoothDevices) {
        // true for indication (acknowledge) and false for notification (unacknowledge).
        mGattServer.notifyCharacteristicChanged(device, characteristic, false);
      }
    }
  }


  ///////////////////////
  ////// Bluetooth //////
  ///////////////////////
  public static BluetoothGattDescriptor getClientCharacteristicConfigurationDescriptor() {
    return new BluetoothGattDescriptor(CLIENT_CHARACTERISTIC_CONFIGURATION_UUID,
            (BluetoothGattDescriptor.PERMISSION_WRITE | BluetoothGattDescriptor.PERMISSION_READ));
  }

  public static BluetoothGattDescriptor getClientCharacteristicExtendedDescriptor() {
    return new BluetoothGattDescriptor(CLIENT_CHARACTERISTIC_EXTENDED_UUID,
            (BluetoothGattDescriptor.PERMISSION_WRITE | BluetoothGattDescriptor.PERMISSION_READ));
  }

  private void ensureBleFeaturesAvailable() {
    if (mBluetoothAdapter == null) {
      Toast.makeText(this, R.string.bluetoothNotSupported, Toast.LENGTH_LONG).show();
      Log.e(TAG, "Bluetooth not supported");
      finish();
    } else if (!mBluetoothAdapter.isEnabled()) {
      // Make sure bluetooth is enabled.
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
      //mGattServer.close();
    }

  }


  @Override
  public boolean onTouchEvent(MotionEvent event) {
    //this.mDetetor.onTouchEvent(event);
    return super.onTouchEvent(event);
  }



//  class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
//
//    @Override
//    public boolean onDown(MotionEvent e) {
//      return true;
//    }
//
//    @Override
//    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//
//      boolean result = false;
//
//      try {
//        float diffY = e2.getY() - e1.getY();
//        float diffX = e2.getX() - e1.getX();
//        if (Math.abs(diffX) > Math.abs(diffY)) {
//          if (Math.abs(diffX) > 10 && Math.abs(velocityX) > 10) {
//            if (diffX < 0) {  //detect swipe right
//
//              enableAnimationFragment = !enableAnimationFragment;
//
//              if (mChargeFragment == null) {   //swipe right to toggle  between  charge animation and dashboard
//
//                mChargeFragment = new ChargeAnimationFragment();
//
//                getSupportFragmentManager()
//                        .beginTransaction()
//                        .add(R.id.fragment_animation_container, mChargeFragment, CURRENT_FRAGMENT_TAG)
//                        .hide(mChargeFragment)
//                        .commit();
//              }
//
//              if (enableAnimationFragment) {
//                getSupportFragmentManager()
//                        .beginTransaction()
//                        .show(mChargeFragment)
//                        .hide(mCurrentServiceFragment)
//                        .commit();
//              } else {
//                getSupportFragmentManager()
//                        .beginTransaction()
//                        .hide(mChargeFragment)
//                        .show(mCurrentServiceFragment)
//                        .commit();
//              }
//            }
//
//          }
//        }
//        result = true;
//      } catch (Exception e) {
//        e.printStackTrace();
//      }
//      return result;
//    }
//  }


  @Override
  public void advertise() {
    mAdvertiser.startAdvertising(mAdvSettings, mAdvData, mAdvCallback);
  }

  @Override
  public void stopadvertise() {
    mAdvertiser.stopAdvertising(mAdvCallback);
  }


  public void saveSN(String pwd) {
    mUser.getBatteriesList().get(batteryIdx).setmSN(pwd);
  }

  public String getSaveSN() {
    return mUser.getBatteriesList().get(batteryIdx).getmSN();
  }



  public boolean sNExist() {
    if (mUser.getBatteriesList().size()> 0 && mUser.getBatteriesList().get(batteryIdx).getmSN() != null) {
      return true;
    } else {
      return false;
    }
  }

  public boolean verifySN(String pwd){
    String SN = mUser.getBatteriesList().get(batteryIdx).getmSN();
    if(pwd .length() >= 8){
      pwd = pwd.substring(pwd.length()-7,pwd.length());
    } else if(pwd .length() == 7) {

    }else {
      return false;
    }
    if (SN.equals(pwd)) {
      if(user!=null){
       // String uniqueID = UUID.randomUUID().toString();
        String email = user.getEmail();
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = df.format(c.getTime());
        UploadData data = new UploadData(formattedDate,email);
        mDatabase.child("serialNumber").child(SN).setValue(data);
      }
      return true;
    } else {
      return false;
    }

  }

  public void startCustomAdv(){
    mAdvertiser.stopAdvertising(mAdvCallback);
    //disconnectFromDevices();
    mUser.getBatteriesList().get(batteryIdx).setBonded(true);
    mGattServer.removeService(mBluetoothGattService);
    mBluetoothGattService = mCurrentServiceFragment.getBluetoothGattServiceCustom();
    mGattServer.addService(mBluetoothGattService);
    mAdvData = new AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .setIncludeTxPowerLevel(false)
            .addServiceUuid(mCurrentServiceFragment.getServiceUUID(mUser.getBatteriesList().get(batteryIdx).isBonded))
            .build();

    if(!mUser.getBatteriesList().get(batteryIdx).isDemo) {
//      try {
//        Thread.sleep(500);
//      } catch (InterruptedException e) {
//        Thread.currentThread().interrupt();
//      }
      mAdvertiser.startAdvertising(mAdvSettings, mAdvData, mScanResponse, mAdvCallback);
    }
  }

  public void enableDiagMode(){
    mCurrentServiceFragment = diagFragment;
    if(isTablet) {
      getSupportFragmentManager()
              .beginTransaction()
              .hide(normalFragment)
              .show(diagFragment)
              .commit();
    }
    else{
      getSupportFragmentManager()
              .beginTransaction()
              .hide(phoneFragment)
              .show(diagFragment)
              .commit();
    }
    startCustomAdv();
  }

  public void enableNormalMode(){

    if(isTablet) {
      mCurrentServiceFragment = normalFragment;
      getSupportFragmentManager()
              .beginTransaction()
              .show(normalFragment)
              .hide(diagFragment)
              .commit();
    }
    else{
      mCurrentServiceFragment = phoneFragment;
      getSupportFragmentManager()
              .beginTransaction()
              .show(phoneFragment)
              .hide(diagFragment)
              .commit();
    }
    startCustomAdv();
  }

  public void enableShowData(){
    mCurrentServiceFragment = normalFragment;
    getSupportFragmentManager()
            .beginTransaction()
            .hide(phoneFragment)
            .show(normalFragment)
            .commit();
    startCustomAdv();
  }

  public void enableShowGauge(){
    mCurrentServiceFragment = phoneFragment;
    getSupportFragmentManager()
            .beginTransaction()
            .show(phoneFragment)
            .hide(normalFragment)
            .commit();
    startCustomAdv();
  }


//  private final BroadcastReceiver mReceiver = new BroadcastReceiver()
//  {
//    @Override
//    public void onReceive(Context context, Intent intent)
//    {
//      final String action = intent.getAction();
//      Log.v(TAG,"Broadcast Receiver:" + action);
//
//      if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED))
//      {
//        final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
//
//        if(state == BluetoothDevice.BOND_BONDING)
//        {
//          Log.v(TAG,"Bonding...");
//          if (mDevice != null) {
//            //mDevice.setPin("1111".getBytes());
//            //Log.v(TAG,"Setting bonding code = 1111" );
//          }
//        }
//        else if(state == BluetoothDevice.BOND_BONDED)
//        {
//          Log.v(TAG,"Bonded!!!");
//        }
//        else if(state == BluetoothDevice.BOND_NONE)
//        {
//          Log.v(TAG,"Not Bonded");
//        }
//      }
//    }
//  };

  @Override
  protected void onDestroy() {
    super.onDestroy();
    Log.v(TAG,"onDestroy++++++++++++++++++++++++++++++++++");
//    try {
//      this.unregisterReceiver(mReceiver);
//    }catch (Exception  e){
//    }
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

  private boolean refreshDeviceCache(BluetoothGatt gatt){
    try {
      BluetoothGatt localBluetoothGatt = gatt;
      Method localMethod = localBluetoothGatt.getClass().getMethod("refresh", new Class[0]);
      if (localMethod != null) {
        boolean bool = ((Boolean) localMethod.invoke(localBluetoothGatt, new Object[0])).booleanValue();
        return bool;
      }
    }
    catch (Exception localException) {
      Log.e(TAG, "An exception occured while refreshing device");
    }
    return false;
  }



}

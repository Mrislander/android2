package com.elitise.appv2;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.share.internal.LikeButton;
import com.facebook.share.model.ShareContent;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.ShareMediaContent;
import com.facebook.share.widget.LikeView;
import com.facebook.share.widget.ShareButton;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.Nameable;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.*;


import io.fabric.sdk.android.*;
import tourguide.tourguide.Overlay;
import tourguide.tourguide.Pointer;
import tourguide.tourguide.ToolTip;
import tourguide.tourguide.TourGuide;


public class MainActivity extends AppCompatActivity implements NewBatteryFragment.selectResultListener {

    private Drawer result = null;
    private ListViewFragment mList;
    private NewBatteryFragment mBattery ;
    private ProfileFragment mProfileFragment;
    private PurchaseFragment mPurchase ;
    private UserData mUser = UserData.getInstance();

    private Logo_pathsView logoView;

    private String mNewBatteryName = null;
    private String mNewBatteryType = null;
    private int mImageId;
    private int mStep = 0;
    private FirebaseAuth mAuth;

    private boolean isTablet;
    private Bitmap genericPhone;
    private Bitmap genericTablet;
    private TourGuide mTourGuideHandler = null;
    private SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        View anchor = findViewById(R.id.anchor);




        Fabric.with(this, new Crashlytics());

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean enableShowCase = sharedPrefs.getBoolean("enableShowCase",true);

        //Create the drawer
        if(savedInstanceState != null){
            mList  = (ListViewFragment) getFragmentManager().findFragmentByTag("ListFragment");
        }else {
            mList = new ListViewFragment();
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.frame_main_container, mList, "ListFragment")
                    .commit();
        }

        genericPhone = BitmapFactory.decodeResource(getResources(),R.drawable.generic_phone);
        genericTablet = BitmapFactory.decodeResource(getResources(),R.drawable.generic_tablet);
        isTablet = isTabletDevice();
        result = new DrawerBuilder(this)
                .withRootView(R.id.drawer_container)
                .withDisplayBelowStatusBar(false)
                .withToolbar(toolbar)
                .withOnDrawerListener(new Drawer.OnDrawerListener() {
                    @Override
                    public void onDrawerOpened(View drawerView) {
                        if(mTourGuideHandler!=null) {
                            mTourGuideHandler.cleanUp();
                            mTourGuideHandler = null;
                        }
                    }

                    @Override
                    public void onDrawerClosed(View drawerView) {

                    }

                    @Override
                    public void onDrawerSlide(View drawerView, float slideOffset) {

                    }
                })
                .withActionBarDrawerToggleAnimated(true)
                .withDrawerLayout(R.layout.material_drawer_fits_not)
                .inflateMenu(R.menu.mainactivity_menu)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (drawerItem instanceof Nameable) {

                            switch (position) {
                                case 1:
                                    getFragmentManager()
                                            .beginTransaction()
                                            .replace(R.id.frame_main_container, mList, "ListFragment")
                                            .commit();

                                    break;
                                case 2:
                                    mProfileFragment = new ProfileFragment();
                                    getFragmentManager()
                                            .beginTransaction()
                                            .replace(R.id.frame_main_container, mProfileFragment, "profile")
                                            .commit();
                                    break;
                                case 3:
                                    sequenceAlertDialog();
                                    break;
                                case 4:
                                    mPurchase = new PurchaseFragment();
                                    getFragmentManager()
                                            .beginTransaction()
                                            .replace(R.id.frame_main_container, mPurchase, "PurchaseFragment")
                                            .commit();
                                    break;
                                case 5:
                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.indurapower.com/contact.html"));
                                    startActivity(browserIntent);
                                    break;
                                case 6:
                                    getFragmentManager()
                                            .beginTransaction()
                                            .replace(R.id.frame_main_container, new SettingFragment(),"SettingFragment")
                                            .commit();
                                    break;
                                case 7:
                                    mAuth = FirebaseAuth.getInstance();
                                    mAuth.signOut();
                                    mUser.setLogin(false);
                                    LoginManager.getInstance().logOut();
                                    SharedPreferences sp=getSharedPreferences("Login", 0);
                                    SharedPreferences.Editor Ed=sp.edit();
                                    Ed.putString("loginState","false" );
                                    Ed.commit();
                                    finish();
                                    break;
                                default:
                                    break;
                            }

                        }
                        return false;
                    }
                })
                .withSavedInstance(savedInstanceState)
                .build();

        result.setSelection(0);



        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
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


        if(mUser.getBatteriesList().size() == 0){
            new MaterialDialog.Builder(this)
                    .title("No Batteries Found")
                    .content("Would you like to connect your first battery")
                    .positiveText("YES")
                    .positiveColor(Color.parseColor("#ff6529"))
                    .negativeText("NO")
                    .negativeColor(Color.parseColor("#ff6529"))
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            sequenceAlertDialog();
                        }
                    })
                    .show();

        }
        if(enableShowCase) {
            mTourGuideHandler = TourGuide.init(this).with(TourGuide.Technique.Click)
                    .setPointer(new Pointer())
                    .setToolTip(new ToolTip().setTitle("Welcome!").setDescription("Click here or swipe right to begin ..."))
                    .playOn(anchor);
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putBoolean("enableShowCase",false);
            editor.commit();
        }
//        new MaterialShowcaseView.Builder(this)
//                .setTarget(anchor)
//                .setDismissText("GOT IT")
//                .setTitleText("Welcome!")
//                .setContentText("Click here or swipe right to begin ...")
//                .setDelay(500) // optional but starting animations immediately in onCreate can make them choppy
//                .singleUse("one") // provide a unique ID used to ensure it is only shown once
//                .show();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(mTourGuideHandler!=null)
                        mTourGuideHandler.cleanUp();
                    }
                });
            }
        },8000);



    }


    public boolean onCreateOptionsMenu(Menu menu) {
        Window wd = this.getWindow();
        wd.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        wd.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        wd.setStatusBarColor(Color.parseColor("#000000"));
        return true;
    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //add the values which need to be saved from the drawer to the bundle
        outState = result.saveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //handle the click on the back arrow click
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onBackPressed() {
        //handle the back press :D close the drawer first and if the drawer is closed close the activity
        if (result != null && result.isDrawerOpen()) {
            result.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    public void sequenceAlertDialog() {
        switch (mStep) {
            case 0:
                Random rand = new Random();
                final String name = "ID#"+String.format("%04d", rand.nextInt(10000));
                new MaterialDialog.Builder(this)
                        .title("Battery Name")
                        .titleColor(Color.parseColor("#ff6529"))
                        .inputRangeRes(0, -1, R.color.material_drawer_dark_background)
                        .content("What would you like your battery to be named?")
                        .negativeText("Cancel")
                        .negativeColor(Color.parseColor("#ff6529"))
                        .positiveText("OK")
                        .positiveColor(Color.parseColor("#ff6529"))
                        .titleGravity(GravityEnum.CENTER)
                        .input(name, null, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                if(input.length()==0){
                                    mNewBatteryName = name;
                                }else {
                                    mNewBatteryName = input.toString();
                                }
                            }
                        })
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog dialog, DialogAction which) {
                                mStep++;
                                sequenceAlertDialog();
                            }
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog dialog, DialogAction which) {
                                mStep = 0;
                                dialog.dismiss();
                            }
                        })
                        .show();
                break;
            case 1:
                if(!isTablet) {
                    new MaterialDialog.Builder(this)
                            .title("Choose your battery Type")
                            .titleColor(Color.parseColor("#ff6529"))
                            .items(SystemDefinition.batteryList1)
                            .itemsCallback(new MaterialDialog.ListCallback() {
                                @Override
                                public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                                    mNewBatteryType = SystemDefinition.batteryList1[position];
                                    if (mNewBatteryType.equals("Internal Phone Battery") || mNewBatteryType.equals("Virtual  Battery")) {
                                        mStep += 2;
                                        sequenceAlertDialog();
                                    } else {
                                        mStep++;
                                        sequenceAlertDialog();
                                    }
                                }
                            })
                            .cancelable(false)
                            .show();
                }else{
                    new MaterialDialog.Builder(this)
                            .title("Choose your battery Type")
                            .titleColor(Color.parseColor("#ff6529"))
                            .items(SystemDefinition.batteryList2)
                            .itemsCallback(new MaterialDialog.ListCallback() {
                                @Override
                                public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                                    mNewBatteryType = SystemDefinition.batteryList2[position];
                                    if (mNewBatteryType.equals("Internal Tablet Battery")|| mNewBatteryType.equals("Virtual  Battery")) {
                                        mStep += 2;
                                        sequenceAlertDialog();
                                    } else {
                                        mStep++;
                                        sequenceAlertDialog();
                                    }
                                }
                            })
                            .cancelable(false)
                            .show();
                }

                break;


            case 2:
                mBattery= new NewBatteryFragment();
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frame_main_container, mBattery, "BatteryFragment")
                        .commit();
                break;
            case 3:
                if(!mNewBatteryType.equals("Internal Phone Battery") && !mNewBatteryType.equals("Internal Tablet Battery")  ) {
                   if(!mNewBatteryType.equals("Virtual  Battery")) {
                       mStep = 0;
                       Intent I = new Intent(getBaseContext(), Peripheral.class);
                       I.putExtra("mBname", mNewBatteryName);
                       I.putExtra("idx", mUser.getBatteriesList().size() - 1);
                       mUser.setCurrentIdx(mImageId);
                       startActivity(I);
                   }else{
                       mStep = 0;
                       mUser.setCurrentIdx(11);
                       Bitmap demo = BitmapFactory.decodeResource(getResources(),R.drawable.demo_battery2_icon);
                       ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                       demo.compress(Bitmap.CompressFormat.PNG,0,byteArrayOutputStream);
                       mUser.addNewBattery(mNewBatteryName, 11, mNewBatteryType,  byteArrayOutputStream.toByteArray());
                       mUser.getBatteriesList().get(mUser.getBatteriesList().size() - 1).isDemo = true;
                       mUser.getBatteriesList().get(mUser.getBatteriesList().size() - 1).isBonded = true;
                       mUser.getBatteriesList().get(mUser.getBatteriesList().size() - 1).setmSN("a23456d");
                       Intent I = new Intent(getBaseContext(), Peripheral.class);
                       I.putExtra("mBname", mNewBatteryName);
                       I.putExtra("idx", mUser.getBatteriesList().size() - 1);
                       startActivity(I);
                   }
                }else{
                    mStep = 0;
                    mUser.setCurrentIdx(13);
                    if(isTablet) {
//                        int length = genericTablet.getByteCount();
//                        ByteBuffer byteBuffer = ByteBuffer.allocate(length);
//                        genericTablet.copyPixelsFromBuffer(byteBuffer);
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        genericTablet.compress(Bitmap.CompressFormat.PNG,0,byteArrayOutputStream);
                        mUser.addNewBattery(mNewBatteryName, 13, mNewBatteryType,  byteArrayOutputStream.toByteArray());
                    }else {
//                        int length = genericTablet.getByteCount();
//                        ByteBuffer byteBuffer = ByteBuffer.allocate(length);
//                        genericTablet.copyPixelsFromBuffer(byteBuffer);
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        genericPhone.compress(Bitmap.CompressFormat.PNG,0,byteArrayOutputStream);
                        mUser.addNewBattery(mNewBatteryName, 13, mNewBatteryType, byteArrayOutputStream.toByteArray());
                    }
                    Intent I = new Intent(getBaseContext(), DeviceBatteryActivity.class);
                    I.putExtra("mBname", mNewBatteryName);
                    I.putExtra("idx", mUser.getBatteriesList().size() - 1);
                    startActivity(I);
                }
//                String instruction = "1.\tTurn Battery ON by orientating battery with terminals upward; LED will Flash GREEN several times.\n\n" +
//                        "2.\tOrient Battery on its side with (-) terminal down and (+) terminal up\n\n" +
//                        "3.\tWhen Battery LED starts flashes BLUE, press OK below to start pairing process.\n\n" +
//                        "4.\tIf pairing fails, set battery to ON mode and then Storage mode and repeat above steps to repeat pairing process\n\n";
//                new MaterialDialog.Builder(this)
//                        .title("Instructions")
//                        .titleColor(Color.parseColor("#ff6529"))
//                        .titleGravity(GravityEnum.CENTER)
//                        .content(instruction)
//                        .positiveText("OK")
//                        .positiveColor(Color.parseColor("#ff6529"))
//                        .onPositive(new MaterialDialog.SingleButtonCallback() {
//                            @Override
//                            public void onClick(MaterialDialog dialog, DialogAction which) {
//                                mStep = 0;
//                                Intent I = new Intent(getBaseContext(), Peripheral.class);
//                                I.putExtra("mBname", mNewBatteryName);
//                                I.putExtra("idx",mUser.getBatteriesList().size()-1);
//                                mUser.setCurrentIdx(mImageId);
//                                startActivity(I);
//                            }
//                        })
//                        .dismissListener(new DialogInterface.OnDismissListener() {
//                            @Override
//                            public void onDismiss(DialogInterface dialog) {
//                                if (mStep != 0) {
//                                    mStep = 0;
//                                }
//                            }
//                        })
//                        .show();
                break;

            default:
                break;

        }
    }
    static final int REQUEST_IMAGE_CAPTURE = 1;
    @Override
    public void getResult(int idx) {
        mStep++;
        mImageId = idx;
        if(idx == 7) {
            dispatchTakePictureIntent();
            return;
        }else{
            mUser.addNewBattery(mNewBatteryName,mImageId,mNewBatteryType,null);
        }
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_main_container, mList, "ListFragment")
                .commit();
        sequenceAlertDialog();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            Bundle extras = data.getExtras();
            Bitmap image = (Bitmap) extras.get("data");
//            int length = image.getByteCount();
//            ByteBuffer byteBuffer = ByteBuffer.allocate(length);
//            image.copyPixelsFromBuffer(byteBuffer);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.PNG,0,byteArrayOutputStream);
            mUser.addNewBattery(mNewBatteryName,mImageId,mNewBatteryType,byteArrayOutputStream.toByteArray());
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_main_container, mList, "ListFragment")
                    .commit();
            sequenceAlertDialog();
        }
    }

    @Override
    protected void onStop() {
        GsonBuilder gsonb = new GsonBuilder();
        Gson gson = gsonb.create();
        String ownbatteries = gson.toJson(mUser.getBatteriesList());
        SharedPreferences prefs = this.getSharedPreferences("userdata", Context.MODE_PRIVATE);
        SharedPreferences.Editor e = prefs.edit();
        e.putString("batteris", ownbatteries);
        e.commit();
        super.onStop();
    }


    @Override
    protected void onRestart() {  //refresh list on every start
        super.onRestart();
        mList.getAdapter().notifyDataSetChanged();
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
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



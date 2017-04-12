package com.elitise.appv2;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.*;
import android.support.design.BuildConfig;
import android.support.design.widget.Snackbar;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.crashlytics.android.Crashlytics;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.share.widget.LikeView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


import org.json.JSONObject;

import io.fabric.sdk.android.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
//public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {
public class LoginActivity extends AppCompatActivity{
    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    private UserData mUser;

    private SharedPreferences prefs;
    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
   // private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private MaterialDialog mProgressDialog;
    private MaterialDialog mRegisterDailog;

    private String mEmailReg;
    private String mPasswordReg;

    private View mLoginFormView;
    private Button mBtn;
    private Button registerBtn;
    private Logo_pathsView logoView;
    private Intent intent;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private LoginButton loginButton;
    CallbackManager callbackManager;
    private Profile profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        try {
//            PackageInfo info = getPackageManager().getPackageInfo(
//                    "com.elitise.appv2",
//                    PackageManager.GET_SIGNATURES);
//            for (Signature signature : info.signatures) {
//                MessageDigest md = MessageDigest.getInstance("SHA");
//                md.update(signature.toByteArray());
//                Log.e("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
//            }
//        } catch (PackageManager.NameNotFoundException e) {
//
//        } catch (NoSuchAlgorithmException e) {
//
//        }

        FacebookSdk.sdkInitialize(getApplicationContext());
        Fabric.with(this, new Crashlytics());

        setContentView(R.layout.activity_login);
        //facebook login
        callbackManager = CallbackManager.Factory.create();
        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList(
                "public_profile", "email"));
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                handleFacebookAccessToken(loginResult.getAccessToken());
                SharedPreferences sp = getSharedPreferences("Login", 0);
                SharedPreferences.Editor Ed = sp.edit();
                Ed.putString("loginState", "true");
                Ed.commit();
                LoadUserData();
                startActivity(intent);
//                profile=Profile.getCurrentProfile();
//                GraphRequest request = GraphRequest.newMeRequest(
//                        loginResult.getAccessToken(),
//                        new GraphRequest.GraphJSONObjectCallback() {
//                            @Override
//                            public void onCompleted(JSONObject object, GraphResponse response) {
//                                Log.v("LoginActivity", response.toString());
//                                try {
//                                    String email = object.getString("email");
//                                    String name = object.getString("name");
//                                }catch (Exception e){
//
//                                }
//                            }
//                        });
//                Bundle parameters = new Bundle();
//                parameters.putString("fields", "id,name,email");
//                request.setParameters(parameters);
//                request.executeAsync();
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });


        mProgressDialog = new MaterialDialog.Builder(this)
                .progress(true,0)
                .title("Wait")
                .build();


        mUser = UserData.getInstance();


        intent = new Intent(getBaseContext(), MainActivity.class);
        // Set up the login form.
        SharedPreferences sp1=this.getSharedPreferences("Login",0);
        String isLogin =sp1.getString("loginState", null);
        if(isLogin!=null) {
            if (isLogin.equals("true")) {
                LoadUserData();
                startActivity(intent);
            }
        }

        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        //populateAutoComplete();
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(LoginActivity.class.getName(), "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out m
                    Log.d(LoginActivity.class.getName(), "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };
        mRegisterDailog = new MaterialDialog.Builder(this)
                .title("Create New Account")
                .customView(R.layout.registerdialog,false)
                .autoDismiss(false)
                .positiveText("Register")
                .negativeText("CANCEL")
                .negativeColor(Color.parseColor("#ff6529"))
                .positiveColor(Color.parseColor("#ff6529"))
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        EditText emailRegister = (EditText) dialog.findViewById(R.id.emailregister);
                        mEmailReg = emailRegister.getText().toString();
                        EditText passwordRegister = (EditText) dialog.findViewById(R.id.passwordRegister);
                        mPasswordReg = passwordRegister.getText().toString();
                        View focusView = null;
                        boolean cancel = false;
                        if (TextUtils.isEmpty(mPasswordReg)) {
                            passwordRegister.setError(getString(R.string.error_invalid_password));
                            focusView = passwordRegister;
                            cancel = true;
                        }else if( !isPasswordValid(mPasswordReg)){
                            passwordRegister.setError(getString(R.string.error_invalid_password));
                            focusView = passwordRegister;
                            cancel = true;
                        }
                        // Check for a valid email address.
                        if (TextUtils.isEmpty(mEmailReg)) {
                            emailRegister.setError(getString(R.string.error_field_required));
                            focusView = emailRegister;
                            cancel = true;
                        } else if (!isEmailValid(mEmailReg)) {
                            emailRegister.setError(getString(R.string.error_invalid_email));
                            focusView = emailRegister;
                            cancel = true;
                        }
                        if (cancel) {
                            // There was an error; don't attempt login and focus the first
                            // form field with an error.
                            focusView.requestFocus();
                        } else{
                            mProgressDialog.show();
                            mAuth.createUserWithEmailAndPassword(mEmailReg, mPasswordReg)
                                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        Log.d("register", "createUserWithEmail:onComplete:" + task.isSuccessful());
                                        if (mProgressDialog.isShowing()) {
                                            mProgressDialog.dismiss();
                                        }
                                        // If sign in fails, display a message to the user. If sign in succeeds
                                        // the auth state listener will be notified and logic to handle the
                                        // signed in user can be handled in the listener.
                                        if (!task.isSuccessful()) {
                                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                                    Toast.LENGTH_SHORT).show();
                                        } else {
                                            mUser.setLogin(true);
                                            SharedPreferences sp = getSharedPreferences("Login", 0);
                                            SharedPreferences.Editor Ed = sp.edit();
                                            Ed.putString("loginState", "true");
                                            Ed.commit();
                                            LoadUserData();
                                            startActivity(intent);
                                        }

                                    }
                                });
                            dialog.dismiss();
                        }
                    }
                })
                .build();
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
        registerBtn = (Button) findViewById(R.id.register);
        registerBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mRegisterDailog.show();
            }
        });
        mBtn = (Button) findViewById(R.id.bypassLogin);
        mBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                LoadUserData();
                startActivity(intent);
            }
        });
        mLoginFormView = findViewById(R.id.login_form);
        //mProgressView = findViewById(R.id.login_progress);

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

        Window wd = this.getWindow();
        wd.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        wd.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        wd.setStatusBarColor(Color.parseColor("#444444"));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {


        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }else if(!isEmailValid(password)){
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            mProgressDialog.show();
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d("Login", "signInWithEmail:onComplete:" + task.isSuccessful());
                            if(mProgressDialog.isShowing()){
                                mProgressDialog.dismiss();
                            }
                            // If sign in fails, display a message to the user. If sign in succeeds
                            // the auth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.
                            if (!task.isSuccessful()) {
                                Log.w("Login", "signInWithEmail", task.getException());
//                                Toast.makeText(LoginActivity.this, "Authentication failed.",
//                                        Toast.LENGTH_SHORT).show();
                                mPasswordView.setError(getString(R.string.error_incorrect_password));
                                mPasswordView.requestFocus();
                            }else {
                                mUser.setLogin(true);
                                SharedPreferences sp=getSharedPreferences("Login", 0);
                                SharedPreferences.Editor Ed=sp.edit();
                                Ed.putString("loginState","true" );
                                Ed.commit();
                                LoadUserData();
                                startActivity(intent);
                            }
                            // ...
                        }
                    });

        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 1;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
//    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
//    private void showProgress(final boolean show) {
//        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
//        // for very easy animations. If available, use these APIs to fade-in
//        // the progress spinner.
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
//            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
//
//            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
//            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
//                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
//                }
//            });
//
//            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
//            mProgressView.animate().setDuration(shortAnimTime).alpha(
//                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
//                }
//            });
//        } else {
//            // The ViewPropertyAnimator APIs are not available, so simply show
//            // and hide the relevant UI components.
//            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
//            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
//        }
//    }



    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
//    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
//
//        private final String mEmail;
//        private final String mPassword;
//        private boolean mSuccess = false;
//
//        UserLoginTask(String email, String password) {
//            mEmail = email;
//            mPassword = password;
//        }

//        @Override
//        protected Boolean doInBackground(Void... params) {
//            // TODO: attempt authentication against a network service.
//
//
//                // Simulate network access.
//
//            mAuth.signInWithEmailAndPassword(mEmail, mPassword)
//                    .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
//                        @Override
//                        public void onComplete(@NonNull Task<AuthResult> task) {
//                            Log.d("Login", "signInWithEmail:onComplete:" + task.isSuccessful());
//
//                            // If sign in fails, display a message to the user. If sign in succeeds
//                            // the auth state listener will be notified and logic to handle the
//                            // signed in user can be handled in the listener.
//                            if (!task.isSuccessful()) {
//                                Log.w("Login", "signInWithEmail", task.getException());
////                                Toast.makeText(LoginActivity.this, "Authentication failed.",
////                                        Toast.LENGTH_SHORT).show();
//                                mSuccess = false;
//                            }
//                            LoadUserData();
//                            mSuccess = true;
//                            // ...
//                        }
//                    });
//
//
//            // TODO: register the new account here.
//            return mSuccess;
//        }
//
//        @Override
//        protected void onPostExecute(final Boolean success) {
//            mAuthTask = null;
//            //showProgress(false);
//
//            if (success) {
//                startActivity(intent);
//            } else {
//                mPasswordView.setError(getString(R.string.error_incorrect_password));
//                mPasswordView.requestFocus();
//            }
//        }
//
//        @Override
//        protected void onCancelled() {
//            mAuthTask = null;
//            //showProgress(false);
//        }
//    }

    private void LoadUserData() {
        prefs = getBaseContext().getSharedPreferences("userdata", Context.MODE_PRIVATE);
        String mBatteries = prefs.getString("batteris",null);
        if(mBatteries != null) {
            GsonBuilder gsonb = new GsonBuilder();
            Gson gson = gsonb.create();
            //mUser.setBatteries(gson.fromJson(mBatteries,UserData.OwnedBattery.class));
            UserData.OwnedBattery[] L = gson.fromJson(mBatteries, UserData.OwnedBattery[].class);
            mUser.setMyBatteries(L);

        }
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d("Login", "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("Login", "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w("Login", "signInWithCredential", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }

}


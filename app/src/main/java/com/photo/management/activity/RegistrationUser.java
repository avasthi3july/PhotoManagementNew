package com.photo.management.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.photo.management.R;
import com.photo.management.dao.BaseResponse;
import com.photo.management.dao.Registration;
import com.photo.management.delegates.Api;
import com.photo.management.delegates.ServiceCallBack;
import com.photo.management.notification.Config;
import com.photo.management.util.Util;
import com.photo.management.webservice.BaseRequest;
import com.photo.management.webservice.JsonDataParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import retrofit.RetrofitError;

/**
 * Created by kavasthi on 12/26/2016.
 */

public class RegistrationUser extends BaseActivity implements ServiceCallBack, View.OnClickListener {
    private ImageView cameraIcon, importImage;
    private EditText mobNum, email;
    private Button saveBtn;
    private TextView headerName, terms;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private String regid = "";
    private SharedPreferences pref;
    private CheckBox checkTerms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {
                    // gcm successfully registered
                    // now subscribe to `global` topic to receive app wide notifications
                    FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);

                    displayFirebaseRegId();

                } else if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    // new push notification is received

                    String message = intent.getStringExtra("message");

                    Toast.makeText(getApplicationContext(), "Push notification: " + message, Toast.LENGTH_LONG).show();

                    //  t/xtMessage.setText(message);
                }
            }
        };
        displayFirebaseRegId();
    }

    private void initViews() {
        pref = Util.getSharedPreferences(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.header_view);
        headerName = (TextView) findViewById(R.id.headerName);
        terms = (TextView) findViewById(R.id.terms);
        headerName.setText("Registration");
        cameraIcon = (ImageView) findViewById(R.id.camera);
        checkTerms = (CheckBox) findViewById(R.id.check);
        importImage = (ImageView) findViewById(R.id.import_image);
        importImage.setVisibility(View.INVISIBLE);
        email = (EditText) findViewById(R.id.email);
        mobNum = (EditText) findViewById(R.id.mobNum);
        saveBtn = (Button) findViewById(R.id.saveBtn);
        cameraIcon.setVisibility(View.GONE);
        saveBtn.setOnClickListener(this);
        terms.setOnClickListener(this);
        // getUserExist();
    }

    @Override
    protected int myView() {
        return R.layout.registration;
    }

    @Override
    public void onClick(View v) {
        if (v == saveBtn) {

            if (email.getText() != null && email.getText().toString().length() > 0) {
                if (mobNum.getText() != null && mobNum.getText().toString().length() > 0) {
                    if (Util.isValidEmail(email.getText().toString()))
                        if (!checkTerms.isChecked())
                            Util.showToast(this, "Please check Term & Condition");
                        else /*getUserExist();*/
                            userRegistration("register", email.getText().toString(), mobNum.getText().toString(), displayFirebaseRegId());
                    else Util.showToast(this, "PLease enter valid email.");
                } else Util.showToast(this, "PLease enter mobile number.");
            } else Util.showToast(this, "PLease enter email.");


        } else if (v == terms) {
            Intent i = new Intent(this, TermsCondition.class);
            startActivity(i);

        }
    }

    public void getUserExist() {
        BaseRequest baseRequest = new BaseRequest(this);
        baseRequest.setProgressShow(true);
        baseRequest.setRequestTag(Api.GET_USER_EXIST);
        baseRequest.setServiceCallBack(this);
        baseRequest.setMessage("Please wait...");
        Api api = (Api) baseRequest.execute(Api.class);
        api.getUserExist("check", email.getText().toString(), pref.getString("regId", null), baseRequest.requestCallback());

    }

    public void userRegistration(String type, String email, String mob, String deviceId) {
        BaseRequest baseRequest = new BaseRequest(this);
        baseRequest.setProgressShow(true);
        baseRequest.setRequestTag(Api.ADD_USER_REGISTRATION);
        baseRequest.setMessage("Please wait...");
        baseRequest.setServiceCallBack(this);
        Api api = (Api) baseRequest.execute(Api.class);
        api.userRegistreation(type, email, mob, deviceId, baseRequest.requestCallback());

    }

    @Override
    public void onSuccess(int tag, String baseResponse) {
        if (tag == Api.GET_USER_EXIST) {
            BaseResponse baseData = JsonDataParser.getInternalParser(baseResponse, new TypeToken<BaseResponse>() {
            }.getType());
            if (baseData.getSuccess().equalsIgnoreCase("1")) {
                //userRegistration("register", email.getText().toString(), mobNum.getText().toString(), displayFirebaseRegId());
            } else if (baseData.getSuccess().equalsIgnoreCase("2")) {
                //userRegistration("register", email.getText().toString(), mobNum.getText().toString(), displayFirebaseRegId());
            } else if (baseData.getSuccess().equalsIgnoreCase("3")) {
                //  userRegistration("register", email.getText().toString(), mobNum.getText().toString(), displayFirebaseRegId());
            }
            userRegistration("register", email.getText().toString(), mobNum.getText().toString(), displayFirebaseRegId());
        } else if (tag == Api.ADD_USER_REGISTRATION) {
            try {
                BaseResponse baseData = JsonDataParser.getInternalParser(baseResponse, new TypeToken<BaseResponse>() {
                }.getType());
                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean("isRegister", true);
                editor.putString("email", email.getText().toString());
                editor.commit();
                if (baseData.getSuccess().equalsIgnoreCase("1")) {
                    Util.showDialog1(this, baseData.getMessage());
                  /*
                    Intent i = new Intent(this, MainActivity.class);
                    startActivity(i);
                    finish();*/
                } else {
                    Util.showToast(this, baseData.getMessage());

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

    @Override
    public void onFail(RetrofitError error) {

    }

    @Override
    public void onNoNetwork() {

    }

    private String displayFirebaseRegId() {

        return pref.getString("regId", null);
        //Log.e("DashBoard", "Firebase reg id: " + regid);
    }
}

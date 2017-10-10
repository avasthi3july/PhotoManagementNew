package com.tagmypicture.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.reflect.TypeToken;
import com.mukesh.countrypicker.CountryPicker;
import com.mukesh.countrypicker.CountryPickerListener;
import com.tagmypicture.dao.BaseResponse;
import com.tagmypicture.database.DatabaseHandler;
import com.tagmypicture.delegates.Api;
import com.tagmypicture.delegates.ServiceCallBack;
import com.tagmypicture.notification.Config;
import com.tagmypicture.util.Util;
import com.tagmypicture.webservice.BaseRequest;
import com.tagmypicture.webservice.JsonDataParser;
import com.tagmypicture.R;

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
    private TextView countryCode;
    private DatabaseHandler db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
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

    @Override
    public void updateUi() {

    }

    private void initViews() {
        TelephonyManager tm = (TelephonyManager) getSystemService(getApplicationContext().TELEPHONY_SERVICE);
        String countryCode1 = tm.getNetworkCountryIso();
        db = new DatabaseHandler(this);
        pref = Util.getSharedPreferences(this);
        picker = CountryPicker.newInstance("Select Country");
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
        countryCode = (TextView) findViewById(R.id.country_code);
        cameraIcon.setVisibility(View.GONE);
        saveBtn.setOnClickListener(this);
        terms.setOnClickListener(this);
        countryCode.setOnClickListener(this);
        //SpannableString content = new SpannableString("I agree Terms & Conditions");
        // content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        // terms.setText(content);
    }

    @Override
    protected int myView() {
        return R.layout.registration;
    }

    CountryPicker picker;

    @Override
    public void onClick(View v) {
        if (v == saveBtn) {

            if (email.getText() != null && email.getText().toString().length() > 0) {
                if (mobNum.getText() != null && mobNum.getText().toString().length() > 0) {
                    if (Util.isValidEmail(email.getText().toString()))
                        if (!checkTerms.isChecked())
                            Util.showToast(this, "Please check Term & Condition");
                        else /*getUserExist();*/ {
                            String phoneNum = countryCode.getText().toString().concat(mobNum.getText().toString());
                            userRegistration("register", email.getText().toString(), phoneNum, displayFirebaseRegId());
                        }
                    else Util.showToast(this, "Please enter valid email.");
                } else Util.showToast(this, "Please enter mobile number.");
            } else Util.showToast(this, "Please enter email.");


        } else if (v == terms) {
            Intent i = new Intent(this, TermsCondition.class);
            startActivity(i);
        } else if (v == countryCode) {
            // dialog title
            picker.setListener(new CountryPickerListener() {
                @Override
                public void onSelectCountry(String name, String code, String dialCode, int flagDrawableResID) {
                    picker.dismiss();
                    countryCode.setText(dialCode);
                    // countryCode.setCompoundDrawablesWithIntrinsicBounds(flagDrawableResID, 0, 0, 0);
                }
            });
            picker.show(getSupportFragmentManager(), "COUNTRY_PICKER");
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

    private void clearDb() {

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
                db.deleteData();
                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean("isRegister", true);
                editor.putBoolean("appLaunch", false);
                editor.putString("email", email.getText().toString());
                editor.commit();
                if (baseData.getSuccess().equalsIgnoreCase("1")) {
                    Util.showDialog1(this, baseData.getMessage());
                    /*Intent i = new Intent(this, MainActivity.class);
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
    }
}

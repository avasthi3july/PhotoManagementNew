package com.tagmypicture.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;
import com.tagmypicture.R;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.reflect.TypeToken;
import com.tagmypicture.dao.BaseResponse;
import com.tagmypicture.dao.ImageDetail;
import com.tagmypicture.dao.Photo;
import com.tagmypicture.dao.TestBase;
import com.tagmypicture.database.DatabaseHandler;
import com.tagmypicture.delegates.Api;
import com.tagmypicture.delegates.ServiceCallBack;
import com.tagmypicture.notification.Config;
import com.tagmypicture.notification.NotificationUtils;
import com.tagmypicture.util.Util;
import com.tagmypicture.webservice.BaseRequest;
import com.tagmypicture.webservice.JsonDataParser;

import java.util.ArrayList;

import retrofit.RetrofitError;

/**
 * Created by kavasthi on 12/13/2016.
 */

public class SplashScreen extends Activity implements ServiceCallBack {
    private static int SPLASH_TIME_OUT = 3000;
    private DatabaseHandler db;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private String regid = "";
    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        pref = Util.getSharedPreferences(SplashScreen.this);
        db = new DatabaseHandler(this);

        getUserExist();
 /*       new Handler().postDelayed(new Runnable() {

            *//*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             *//*

            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity


                // close this activity
                finish();
            }
        }, SPLASH_TIME_OUT);*/
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

    private void displayFirebaseRegId() {
        regid = pref.getString("regId", null);
        Log.e("DashBoard", "Firebase reg id: " + regid);
    }

    public void getUserExist() {
        BaseRequest baseRequest = new BaseRequest(this);
        baseRequest.setProgressShow(true);
        baseRequest.setRequestTag(Api.GET_USER_EXIST);
        baseRequest.setServiceCallBack(this);
        baseRequest.setMessage("Please wait...");
        Api api = (Api) baseRequest.execute(Api.class);
        api.getUserExist("check", pref.getString("email", ""), pref.getString("regId", null), baseRequest.requestCallback());

    }

    @Override
    protected void onResume() {
        super.onResume();

        // register GCM registration complete receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.REGISTRATION_COMPLETE));

        // register new push message receiver
        // by doing this, the activity will be notified each time a new message arrives
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION));

        // clear the notification area when the app is opened
        NotificationUtils.clearNotifications(getApplicationContext());
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    public void getImage() {
        BaseRequest baseRequest = new BaseRequest(this);
        baseRequest.setProgressShow(true);
        baseRequest.setRequestTag(Api.GET_IMAGE);
        baseRequest.setServiceCallBack(this);
        baseRequest.setMessage("Please wait...");
        Api api = (Api) baseRequest.execute(Api.class);
        api.getImage("get_image", pref.getString("email", ""), baseRequest.requestCallback());

    }


    @Override
    public void onSuccess(int tag, String baseResponse) {
        if (tag == Api.GET_IMAGE) {
            BaseResponse<ArrayList<ImageDetail>> imageDetail = JsonDataParser.getInternalParser(baseResponse, new TypeToken<BaseResponse<ArrayList<ImageDetail>>>() {
            }.getType());
            if (imageDetail.getSuccess().equalsIgnoreCase("1")) {
                for (int i = 0; i < imageDetail.getData().size(); i++) {
                    ImageDetail image = imageDetail.getData().get(i);
                    Photo mPhoto = new Photo();
                    mPhoto.setRefId(image.getImgurl().hashCode());
                    mPhoto.setPicPath(image.getImgurl());
                    mPhoto.setTagName(image.getTag());
                    mPhoto.setDownload(true);
                    mPhoto.setTag(true);
                    mPhoto.setDate(image.getCreatedon());
                    db.tagImageDb(mPhoto);
                }
            }
            finish();
            Intent i = new Intent(SplashScreen.this, MainActivity.class);
            startActivity(i);
        } else if (tag == Api.GET_USER_EXIST) {
            BaseResponse data = JsonDataParser.getInternalParser(baseResponse, new TypeToken<BaseResponse>() {
            }.getType());
            // Util.showDialog(this, data.getSuccess());
            Log.v("DATTTA", data.getSuccess());
            if (data.getSuccess().equalsIgnoreCase("1")) {
                //  if (pref.getBoolean("isRegister", false)) {
                getImage();

                /*} else {
                    Intent i = new Intent(SplashScreen.this, RegistrationUser.class);
                    startActivity(i);
                }*/

            } else if (data.getSuccess().equalsIgnoreCase("2")) {
                Util.showDialog1(this, data.getMessage());
                //finish();
            } else {
                if (data.getSuccess().equalsIgnoreCase("0")) {
                    Intent i = new Intent(SplashScreen.this, RegistrationUser.class);
                    startActivity(i);
                    finish();
                }
            }
        } else {
            BaseResponse<TestBase> imageDetail = JsonDataParser.getInternalParser(baseResponse, new TypeToken<BaseResponse<TestBase>>() {
            }.getType());
            ArrayList<TestBase> base = imageDetail.getData().getSubFolder().get(0).getSubFolder().get(0).getSubFolder().get(0).getSubFolder();
        }

    }

    @Override
    public void onFail(RetrofitError error) {

    }

    @Override
    public void onNoNetwork() {

    }
}

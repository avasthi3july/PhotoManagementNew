package com.tagmypicture.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.google.gson.reflect.TypeToken;
import com.tagmypicture.activity.MainActivity;
import com.tagmypicture.dao.BaseResponse;
import com.tagmypicture.dao.ImageDetail;
import com.tagmypicture.dao.Photo;
import com.tagmypicture.database.DatabaseHandler;
import com.tagmypicture.delegates.Api;
import com.tagmypicture.delegates.ServiceCallBack;
import com.tagmypicture.util.Util;
import com.tagmypicture.webservice.BaseRequest;
import com.tagmypicture.webservice.JsonDataParser;

import java.util.ArrayList;

import retrofit.RetrofitError;

/**
 * Created by kavasthi on 4/12/2017.
 */

public class DownloadService extends IntentService implements ServiceCallBack {
    private SharedPreferences pref;
    private DatabaseHandler db;
   // ResultReceiver receiver;


    public DownloadService() {
        super(DownloadService.class.getName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        //receiver = intent.getParcelableExtra("receiver");
        getImage();
    }

    public void getImage() {
        pref = Util.getSharedPreferences(this);
        db = new DatabaseHandler(this);
        BaseRequest baseRequest = new BaseRequest(this);
        baseRequest.setProgressShow(false);
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
                ImageDetail image = imageDetail.getData().get(0);
                Photo mPhoto = new Photo();
                mPhoto.setRefId(image.getImgurl().hashCode());
                mPhoto.setPicPath(image.getImgurl());
                mPhoto.setTagName(image.getTag());
                mPhoto.setDownload(true);
                mPhoto.setTag(true);
                mPhoto.setDate(image.getCreatedon());
                db.tagImageDb(mPhoto);
                Bundle bundle = new Bundle();
                //receiver.send(1, bundle);
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(MainActivity.MyWebRequestReceiver.PROCESS_RESPONSE);
               /* broadcastIntent.setAction(IntentServiceActivity.MyWebRequestReceiver.PROCESS_RESPONSE);
                broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
                broadcastIntent.putExtra(RESPONSE_STRING, responseString);
                broadcastIntent.putExtra(RESPONSE_MESSAGE, responseMessage);*/
                sendBroadcast(broadcastIntent);
                //this.stopSelf();
            }

        }
    }

    @Override
    public void onFail(RetrofitError error) {

    }

    @Override
    public void onNoNetwork() {

    }
}

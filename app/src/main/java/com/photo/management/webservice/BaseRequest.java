package com.photo.management.webservice;

import android.content.Context;

import com.photo.management.delegates.ServerApi;
import com.photo.management.delegates.ServiceCallBack;

import retrofit.Callback;

/**
 * Created by   on 10/1/2015.
 */
public class BaseRequest {
    private int requestTag;
    private boolean progressShow;
    private String message;
    private String callURL;
    private ServiceCallBack serviceCallBack;
    private Callback<Object> callback;
    private RestClient restClient;
    private Context context;

    public BaseRequest(Context context) {
        this.context = context;
        setCallURL(ServerApi.BASE_URL);
        setProgressShow(true);
    }


    public Object execute(Class classes) {
        restClient = new RestClient(context, this);
        return restClient.execute(classes);
    }

    public Callback<String> requestCallback() {

        return restClient.callback;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Callback<Object> getCallback() {
        return callback;
    }

    public void setCallback(Callback<Object> callback) {
        this.callback = callback;
    }


    public RestClient getRestClient() {
        return restClient;
    }

    public void setRestClient(RestClient restClient) {
        this.restClient = restClient;
    }


    public ServiceCallBack getServiceCallBack() {
        return serviceCallBack;
    }

    public void setServiceCallBack(ServiceCallBack serviceCallBack) {
        this.serviceCallBack = serviceCallBack;
    }

    public int getRequestTag() {
        return requestTag;
    }

    public void setRequestTag(int requestType) {
        this.requestTag = requestType;
    }


    public boolean isProgressShow() {
        return progressShow;
    }

    public void setProgressShow(boolean progressShow) {
        this.progressShow = progressShow;
    }

    public String getCallURL() {
        return callURL;
    }

    public void setCallURL(String callURL) {
        this.callURL = callURL;
    }

}

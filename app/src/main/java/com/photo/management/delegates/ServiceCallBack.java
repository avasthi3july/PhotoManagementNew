package com.photo.management.delegates;

import retrofit.RetrofitError;

/**
 * Created by  on 10/1/2015.
 */
public interface ServiceCallBack
{
    public void onSuccess(int tag, String baseResponse);
    public void onFail(RetrofitError error);
    public void onNoNetwork();
}

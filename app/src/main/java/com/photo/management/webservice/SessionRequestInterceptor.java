package com.photo.management.webservice;


import retrofit.RequestInterceptor;

/**
 * Created by K on 9/24/2015.
 */
public class SessionRequestInterceptor implements RequestInterceptor {
    @Override
    public void intercept(RequestFacade request) {
        /*request.addHeader("Authenticate-Key", "21D34AE3-AE07-429D-BFAD-52C27A11DD2A");
        request.addHeader("device_type", "android_ver");
        request.addHeader("Version", "v1");
        request.addHeader("Content-Type", "application/x-www-form-urlencoded");
        request.addHeader("Accept", "application/json");
        request.addHeader("token", "441b58df46b930a7cc6fb3e32c52e0a4");*/
       // request.addHeader("ip", Util.getLocalIpAddress());

        /*"Content-Type":"application/x-www-form-urlencoded",
                "Accept":"application/json"*/
        // request.addHeader("Private-Key","");

    }
}

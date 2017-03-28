package com.photo.management.webservice;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.widget.Toast;

import com.squareup.okhttp.OkHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.android.AndroidLog;
import retrofit.client.OkClient;
import retrofit.client.Response;
import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;

/**
 * Created by Admin on 9/21/2015.
 */
public class RestClient {
    private Object REST_CLIENT = null;

    private BaseRequest baseRequest;
    private ProgressDialog pDialog = null;
    private Context context;


    public RestClient(Context context, BaseRequest _baseRequest) {
        this.baseRequest = _baseRequest;
        this.context = context;
    }

    public Object execute(Class classes) {
        if (baseRequest.isProgressShow())
            showProgressDialog(context);
        return setUpRestClient(classes);
    }

    private Object setUpRestClient(Class apiClasses) {
        final OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.setReadTimeout(60, TimeUnit.SECONDS);
        okHttpClient.setConnectTimeout(60, TimeUnit.SECONDS);
        RestAdapter.Builder builder = new RestAdapter.Builder().setRequestInterceptor(new SessionRequestInterceptor()).setEndpoint(baseRequest.getCallURL()).setClient(new OkClient(okHttpClient));
        builder.setConverter(new StringConverter());
        builder.setLogLevel(RestAdapter.LogLevel.FULL).setLog(new AndroidLog("Avasthi"));
        RestAdapter restAdapter = builder.build();
        return restAdapter.create(apiClasses);

    }

    public Callback<String> callback = new Callback<String>() {


        @Override
        public void success(String o, Response response) {
            Log.e("success", o);
            if (o.trim().contains("<!DOCTYPE") || o.trim().contains("<div")) {
                Log.i("response after error", "");
                Toast.makeText(context, "Error: Html response instead of json.", Toast.LENGTH_LONG).show();
            } else {
                baseRequest.getServiceCallBack().onSuccess(baseRequest.getRequestTag(), o);
            }

            dismissProgressDialog();

        }

        @Override
        public void failure(RetrofitError error) {
            Log.e("failure", "true");
            ConnectionDetector cd = new ConnectionDetector();
            if (!cd.isInternetOn(context)) {
                Toast.makeText(context, "No internet connection.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "Please try again  -" + error, Toast.LENGTH_LONG).show();
            }
            baseRequest.getServiceCallBack().onFail(error);
            dismissProgressDialog();


        }
    };


    public void showProgressDialog(Context context) {
        try {
            if (((Activity) context).isFinishing()) {
                return;
            }
            pDialog = new ProgressDialog(context);
            pDialog.setMessage(baseRequest.getMessage());
            pDialog.setCancelable(false);
            pDialog.show();

            // private final ProgressDialog progressDialog;
           /* pDialog = MyCustomProgressDialog.ctor(context);
            pDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
             pDialog.setCancelable(false);
             pDialog.setIndeterminate(false);
            pDialog.show();*/

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dismissProgressDialog() {
        try {
            if ((pDialog != null) && pDialog.isShowing())
                pDialog.dismiss();
            pDialog = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class StringConverter implements Converter {

        @Override
        public Object fromBody(TypedInput typedInput, Type type) throws ConversionException {
            String text = null;
            try {

                text = fromStream(typedInput.in());
            } catch (IOException ignored) {
                ignored.printStackTrace();
            }
            return text;
        }

        @Override
        public TypedOutput toBody(Object o) {

            return null;
        }

        private String fromStream(InputStream in) throws IOException {

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder out = new StringBuilder();
            String newLine = System.getProperty("line.separator");
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line);
                out.append(newLine);
            }
            return out.toString();
        }
    }

}

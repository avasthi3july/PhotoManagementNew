package com.tagmypicture.delegates;

import android.app.Application;

import com.tagmypicture.R;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by kavasthi on 4/9/2016.
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/HelveticaNeue Light.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
        //....
    }
   /* protected void attachBaseContext(Context base,int id) {
        super.attachBaseContext(base);
        CalligraphyConfig.initDefault(
                new CalligraphyConfig.Builder()
                        .setDefaultFontPath("fonts/HelveticaNeue Light.ttf")
                        .setFontAttrId(R.layout.gallery_view)
                        .build()
        );
    }*/
}
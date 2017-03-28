package com.photo.management.delegates;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by kavasthi on 1/18/2017.
 */

public class UninstallIntentReceiver extends BroadcastReceiver {

    Context context;

    @Override
    public void onReceive(Context context, Intent intent) {

        this.context = context;

        // when package removed
        if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {
            Log.e(" BroadcastReceiver ", "onReceive called "
                    + " PACKAGE_REMOVED ");
            Toast.makeText(context, " onReceive !!!! PACKAGE_REMOVED",
                    Toast.LENGTH_LONG).show();

        }
        // when package installed
        else if (intent.getAction().equals(
                "android.intent.action.PACKAGE_ADDED")) {

            Log.e(" BroadcastReceiver ", "onReceive called " + "PACKAGE_ADDED");
            Toast.makeText(context, " onReceive !!!!." + "PACKAGE_ADDED",
                    Toast.LENGTH_LONG).show();

        }
        else {
            Log.e(" BroadcastReceiver11 ", "onReceive called 1111111" + "PACKAGE_ADDED");
            Toast.makeText(context, " onReceive11111 !!!!." + "PACKAGE_ADDED111",
                    Toast.LENGTH_LONG).show();
        }
    }
}

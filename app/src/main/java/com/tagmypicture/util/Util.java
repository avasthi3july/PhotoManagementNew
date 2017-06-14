package com.tagmypicture.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.tagmypicture.R;
import com.tagmypicture.delegates.MyApplication;
import com.tagmypicture.notification.Config;

/**
 * Created by kavasthi on 12/12/2016.
 */

public class Util {
    public static MyApplication appClass(Context mContext)
    {
        MyApplication myApplication=(MyApplication)mContext.getApplicationContext();
        return myApplication;
    }


    public static void showToast(Context mContext, String meg) {
        Toast.makeText(mContext, meg, Toast.LENGTH_LONG).show();
    }

    public static void showDialog(Context context, String message) {
        final AlertDialog.Builder alertDialog;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            alertDialog = new AlertDialog.Builder(context, R.style.CustomDialog);

        } else {
            alertDialog = new AlertDialog.Builder(context);
        }
        //final AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setMessage(message);
        alertDialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.create();
        alertDialog.show();
    }
    private static void showInterstitial(InterstitialAd mInterstitialAd) {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }
    public static void showAd(final Activity activity)
    {
       final InterstitialAd mInterstitialAd = new InterstitialAd(activity);
         mInterstitialAd.setAdUnitId(activity.getString(R.string.interstitial_full_screen));
        AdRequest adRequest1 = new AdRequest.Builder()
                // Check the LogCat to get your test device ID
               /* .addTestDevice("A39375AA95300B129D02F61E26E305AB")*/
                .build();

        mInterstitialAd.loadAd(adRequest1);
        mInterstitialAd.setAdListener(new AdListener() {
            public void onAdLoaded() {
                showInterstitial(mInterstitialAd);
            }

            @Override
            public void onAdClosed() {
                //Toast.makeText(activity, "Ad is closed!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                //Toast.makeText(activity, "Ad failed to load! error code: " + errorCode, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdLeftApplication() {
                //Toast.makeText(activity, "Ad left application!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdOpened() {
                //Toast.makeText(activity, "Ad is opened!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void showDialog1(final Activity activity, String message) {
        final AlertDialog.Builder alertDialog;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            alertDialog = new AlertDialog.Builder(activity, R.style.CustomDialog);

        } else {
            alertDialog = new AlertDialog.Builder(activity);
        }
        //final AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setMessage(message);
        alertDialog.setCancelable(false);
        alertDialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                activity.finish();
            }
        });
        alertDialog.create();
        alertDialog.show();
    }

    public static void showDialogPermission(final Activity activity) {
        final AlertDialog.Builder alertDialog;
        alertDialog = new AlertDialog.Builder(activity);
        //final AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle("Permission nedded");
        alertDialog.setCancelable(true);
        alertDialog.setMessage("In order to work properly, Tag My Pictures needs permission to Aceess");
        alertDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        dialog.dismiss();
                        final Intent i = new Intent();
                        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        i.addCategory(Intent.CATEGORY_DEFAULT);
                        i.setData(Uri.parse("package:" + activity.getPackageName()));
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                        activity.startActivity(i);
                    }
                });
        alertDialog.show();
       /* alertDialog.setNeutralButton("SETTINGS", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alertDialog.create();
        alertDialog.show();*/
    }

   /* public static Bitmap getBitMap(File imgFile) {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            options.inSampleSize = 2;
            options.inJustDecodeBounds = false;
            options.inTempStorage = new byte[16 * 1024];
            Bitmap bmp = BitmapFactory.decodeFile(imgFile.getAbsolutePath(), options);
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bmp, bitmap.getWidth(), bitmap.getHeight(), false);

            ExifInterface exif = null;
            try {
                exif = new ExifInterface(imgFile.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);

            return rotateBitmap(resizedBitmap, orientation);


        } catch (Exception e) {
            return null;
        }

    }*/

    /*public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {

        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }*/

    public static boolean isNetworkConnected(Context mContext) {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    public final static boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    public static SharedPreferences getSharedPreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Config.SHARED_PREF, 0);
        return sharedPreferences;

    }

    public static void showInAppResponseCodeMessage(String responseCode, Context mContext) {

        if (responseCode.equalsIgnoreCase(PurchaseConstants.BILLING_RESPONSE_RESULT_OK)) {
            showToast(mContext, mContext.getString(R.string.STATUS_OK));

        } else if (responseCode.equalsIgnoreCase(PurchaseConstants.BILLING_RESPONSE_RESULT_USER_CANCELED)) {
            showToast(mContext, mContext.getString(R.string.STATUS_CANCEL));

        } else if (responseCode.equalsIgnoreCase(PurchaseConstants.BILLING_RESPONSE_RESULT_SERVICE_UNAVAILABLE)) {
            showToast(mContext, mContext.getString(R.string.STATUS_NETWORK));

        } else if (responseCode.equalsIgnoreCase(PurchaseConstants.BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE)) {
            showToast(mContext, mContext.getString(R.string.STATUS_VERSION));

        } else if (responseCode.equalsIgnoreCase(PurchaseConstants.BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE)) {
            showToast(mContext, mContext.getString(R.string.STATUS_UNAVAILABLE));

        } else if (responseCode.equalsIgnoreCase(PurchaseConstants.BILLING_RESPONSE_RESULT_DEVELOPER_ERROR)) {
            showToast(mContext, mContext.getString(R.string.STATUS_DEVP_ERROR));

        } else if (responseCode.equalsIgnoreCase(PurchaseConstants.BILLING_RESPONSE_RESULT_ERROR)) {
            showToast(mContext, mContext.getString(R.string.STATUS_FATAL));

        } else if (responseCode.equalsIgnoreCase(PurchaseConstants.BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED)) {
            showToast(mContext, mContext.getString(R.string.STATUS_ALREADY_PUR));

        } else if (responseCode.equalsIgnoreCase(PurchaseConstants.BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED)) {
            showToast(mContext, mContext.getString(R.string.STATUS_NOT_OWNED));
        }
    }
   /* public class Iso2Phone {
        public  String getPhone(String code) {
            return country2phone.get(code.toUpperCase());
        }
        public  Map<String, String> getAll() {
            return country2phone;
        }*/


}

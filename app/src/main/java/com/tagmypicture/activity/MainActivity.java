package com.tagmypicture.activity;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tagmypicture.database.DatabaseHandler;
import com.tagmypicture.delegates.FragmentCommunicator;
import com.tagmypicture.delegates.HeadeName;
import com.tagmypicture.fragement.GallaryView;
import com.tagmypicture.fragement.MyPictureView;
import com.tagmypicture.fragement.NoTagView;
import com.tagmypicture.fragement.TagPictureView;
import com.tagmypicture.util.Util;
import com.tagmypicture.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by kavasthi on 12/6/2016.
 */

public class MainActivity extends BaseActivity implements View.OnClickListener, HeadeName {
    private ImageView importImages, cameraClick;
    private File tempFile;
    private Uri fileUri;
    private static final int PICK_FROM_CAMERA = 1;
    private int STORAGE_PERMISSION_CODE = 3;
    private int PERMISSION_REQUEST_CONTACT = 6;
    private int CAMERA_PERMISSION_CODE = 5;
    int picCount;
    protected static final int RESULT_SPEECH = 2;
    private DatabaseHandler db;
    public FragmentCommunicator fragmentCommunicator;
    private TextView headerName;
    private boolean isCamera = false, isGallery = false;
    private String email = "";
    private Fragment f;
    MyWebRequestReceiver receiver;
    private SharedPreferences pref;
    protected static final int REQUEST_CODE = 99, REQUEST_CODE_PHONE = 90;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();


    }

    private void initViews() {
        IntentFilter filter = new IntentFilter(MyWebRequestReceiver.PROCESS_RESPONSE);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new MyWebRequestReceiver();
        registerReceiver(receiver, filter);
        db = new DatabaseHandler(this);
        pref = Util.getSharedPreferences(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.header_view);
        f = getSupportFragmentManager().findFragmentById(R.id.frame_layout);
        picCount = db.getContactsCount();
        importImages = (ImageView) findViewById(R.id.import_image);
        cameraClick = (ImageView) findViewById(R.id.camera);
        headerName = (TextView) findViewById(R.id.headerName);
        importImages.setOnClickListener(this);
        cameraClick.setOnClickListener(this);
        if (picCount > 0) {
            f = new MyPictureView();
        } else {
            f = new NoTagView();
        }
        addFragementView(f);
    }//

    @Override
    protected int myView() {
        return R.layout.gallery_view;
    }

    @Override
    public void onClick(View v) {
        if (v == importImages) {
            isCamera = false;
            isGallery = true;
            if (isReadStorageAllowed()) {
                importGalleryImage();
                return;
            }
            requestStoragePermission();

        } else if (v == cameraClick) {
            isCamera = true;
            isGallery = false;
            if (isReadStorageAllowed()) {
                if (isCheckPermissionForCamera()) {
                    //If permission is already having then showing the toast
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    tempFile = createImageFile();
                    tempFile.getParentFile().mkdirs();
                    fileUri = Uri.fromFile(tempFile);

                    if (tempFile != null) {
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                        startActivityForResult(cameraIntent, 1);
                    }
                    return;
                }
                //If the app has not the permission then asking for the permission
                requestPermissionForCamera();
                return;
            }

            requestStoragePermission();
            //clearBackStackInclusive(new MyPictureView().getClass().getName());

        }
    }

    private File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new
                Date());
        String IMAGE_PATH = Environment
                .getExternalStorageDirectory().getPath() + "/tagMyPic";
        File file = new File(IMAGE_PATH, "IMG_" + timeStamp + ".jpg");
        return file;
    }

    public void addFragementView(Fragment fragment) {
        replaceFragment(R.id.frame_layout, fragment, fragment.getClass().getName(), fragment.getClass().getName());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Fragment f = getSupportFragmentManager().findFragmentById(R.id.frame_layout);
            if (f instanceof MyPictureView) {
                MainActivity.this.finish();
            } else {
                if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
                    finish();
                } else {

                    super.onBackPressed();
                }
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    public void startSpeechToText() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");

        try {
            startActivityForResult(intent, RESULT_SPEECH);
        } catch (ActivityNotFoundException a) {
            Toast t = Toast.makeText(getApplicationContext(),
                    "Opps! Your device doesn't support Speech to Text",
                    Toast.LENGTH_SHORT);
            t.show();
        }

    }

    String text;

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        f = getSupportFragmentManager().findFragmentById(R.id.frame_layout);
        if (requestCode == PICK_FROM_CAMERA) {
            tempFile = new File(fileUri.getPath());

            if (resultCode != 0) {
                TagPictureView mTagPictureView = new TagPictureView();
                Bundle mBundle = new Bundle();
                mBundle.putString("picPath", fileUri.getPath());
                mTagPictureView.setArguments(mBundle);
                addFragementView(mTagPictureView);
            }
        } else if (requestCode == RESULT_SPEECH) {
            if (data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                text = result.get(0);
                fragmentCommunicator.passDataToFragment(text);
            }
        } else if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    email = "";
                    Uri contactData = data.getData();
                    getEmailFromContact(contactData);

                }
            }
        } else if (requestCode == REQUEST_CODE_PHONE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    email = "";
                    Uri contactData = data.getData();
                    getPhoneFromContact(contactData);

                }
            }
        }
        Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
        Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
        if (mHelper == null) return;
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == 1001) {
                int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
                String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
                String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");

                if (resultCode == RESULT_OK) {
                    try {
                        JSONObject jo = new JSONObject(purchaseData);
                        String sku = jo.getString("productId");
                        Log.v("You have bought the>>>>", sku);
                    } catch (JSONException e) {
                        alert("Failed to parse purchase data.");
                        e.printStackTrace();
                    }
                }
            }
        } else {
            Log.d(TAG, "onActivityResult handled by IABUtil.");
        }
    }


    private void getEmailFromContact(Uri contactData) {
        //  ContentResolver cr = getContentResolver();
        Cursor c = getContentResolver().query(contactData, null, null, null, null);
        String id = "";
        if (c.moveToFirst()) {
            id = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
        }

        c.close();
        Cursor emailCur = getContentResolver().query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                new String[]{id}, null);
        while (emailCur.moveToNext()) {
            email = emailCur.getString(
                    emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
            String emailType = emailCur.getString(
                    emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
            System.out.println("Email " + email + " Email Type : " + emailType);
        }

        if (f instanceof MyPictureView) {
            ((MyPictureView) f).setEmailId(email);
        }

        emailCur.close();
    }

    private void getPhoneFromContact(Uri contactData) {
        Cursor c = getContentResolver().query(contactData, null, null, null, null);
        String num = "";
        if (c.moveToFirst()) {
            String contactId = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
            String hasNumber = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

            if (Integer.valueOf(hasNumber) == 1) {
                Cursor numbers = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
                while (numbers.moveToNext()) {
                    num = numbers.getString(numbers.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                }
            }
        }
        c.close();
        if (f instanceof MyPictureView)
            ((MyPictureView) f).setPhone("" + num);
    }

    public void importGalleryImage() {
        GallaryView mGallaryView = new GallaryView();
        clearBackStackInclusive(mGallaryView.getClass().getName());

        addFragementView(mGallaryView);
    }

    public void clearBackStackInclusive(String tag) {
        try {
            getSupportFragmentManager().popBackStack(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    public void setHeaderName(String text) {
        headerName.setText(text);
    }


    //This method will be called when the user will tap on allow or deny
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //Checking the request code of our request
        if (requestCode == CAMERA_PERMISSION_CODE) {
            String permission = permissions[0];
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // user rejected the permission
                boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, permission);
                if (!showRationale) {
                    //  openSetting();
                    Util.showDialogPermission(this);
                }
            }
            //If permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && isCamera) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                tempFile = createImageFile();
                tempFile.getParentFile().mkdirs();
                fileUri = Uri.fromFile(tempFile);
                if (tempFile != null) {
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                    startActivityForResult(cameraIntent, 1);
                }

            } else if (isReadStorageAllowed() && isGallery)
                importGalleryImage();
           /* else {
                importGalleryImage();
            }*/
        }
        if (requestCode == STORAGE_PERMISSION_CODE) {

            //If permission is granted
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // user rejected the permission
                String permission = permissions[0];
                boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, permission);
                if (!showRationale) {
                    Util.showDialogPermission(this);
                }
            }
            if (!isCheckPermissionForCamera())
                requestPermissionForCamera();
        }
        if (requestCode == PERMISSION_REQUEST_CONTACT) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // user rejected the permission
                String permission = permissions[0];
                boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, permission);
                if (!showRationale) {
                    Util.showDialogPermission(this);
                }
            } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Fragment  f=f;
                if (f instanceof MyPictureView)
                    ((MyPictureView) f).grandPermissionToFragment();
            }
        }
    }

    public void requestPermissionForReadContact() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}
                , PERMISSION_REQUEST_CONTACT);
    }

    public boolean isCheckPermissionForContact() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isReadStorageAllowed() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        //If permission is granted returning true
        if (result == PackageManager.PERMISSION_GRANTED)
            return true;

        //If permission is not granted returning false
        return false;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }

    public boolean isCheckPermissionForCamera() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public void requestPermissionForCamera() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
    }

    public class MyWebRequestReceiver extends BroadcastReceiver {

        public static final String PROCESS_RESPONSE = "photo.manage.PROCESS_RESPONSE";

        @Override
        public void onReceive(Context context, Intent intent) {
            f = getSupportFragmentManager().findFragmentById(R.id.frame_layout);
            if (f instanceof MyPictureView)
                ((MyPictureView) f).reFreshAdapter();
        }

    }

    @Override
    public void updateUi() {
        f = getSupportFragmentManager().findFragmentById(R.id.frame_layout);
        if (f instanceof MyPictureView)
            ((MyPictureView) f).updateMessageButton();
        else if (f instanceof TagPictureView)
            ((TagPictureView) f).updateAdsButton();
    }
   /* @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        //System.out.println("SIZEEEE>>" + MyPictureView.picList.size());
       // MyPictureView.myPictureAdapter.notifyDataSetChanged();
    }*/
}

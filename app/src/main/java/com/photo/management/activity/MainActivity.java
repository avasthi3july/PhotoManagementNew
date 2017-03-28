package com.photo.management.activity;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
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

import com.photo.management.R;
import com.photo.management.database.DatabaseHandler;
import com.photo.management.delegates.FragmentCommunicator;
import com.photo.management.delegates.HeadeName;
import com.photo.management.delegates.UninstallIntentReceiver;
import com.photo.management.fragement.GallaryView;
import com.photo.management.fragement.MyPictureView;
import com.photo.management.fragement.NoTagView;
import com.photo.management.fragement.TagPictureView;
import com.photo.management.util.Util;

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
    private int CAMERA_PERMISSION_CODE = 5;
    int picCount;
    private final int SPEECH_RECOGNITION_CODE = 2;
    protected static final int RESULT_SPEECH = 2;
    private DatabaseHandler db;
    public FragmentCommunicator fragmentCommunicator;
    private TextView headerName;
    private boolean isCamera = false, isGallery = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        

    }

    private void initViews() {
        db = new DatabaseHandler(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.header_view);
        picCount = db.getContactsCount();
        importImages = (ImageView) findViewById(R.id.import_image);
        cameraClick = (ImageView) findViewById(R.id.camera);
        headerName = (TextView) findViewById(R.id.headerName);
        importImages.setOnClickListener(this);
        cameraClick.setOnClickListener(this);
        Fragment f;
        if (picCount > 0) {
            f = new MyPictureView();
        } else {
            f = new NoTagView();
        }

        // No noView = new MyPictureView();
        addFragementView(f);
    }

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
                .getExternalStorageDirectory().getPath() + "/.photoManage";
        File file = new File(IMAGE_PATH, "IMG_" + timeStamp + ".jpg");
        return file;
    }

    public void addFragementView(Fragment fragment) {
        replaceFragment(R.id.frame_layout, fragment, fragment.getClass().getName(), fragment.getClass().getName());
    }

  /* @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            finish();
        } else {
            super.onBackPressed();
        }
    }*/

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
        if (requestCode == PICK_FROM_CAMERA) {
            tempFile = new File(fileUri.getPath());

            if (resultCode != 0) {
                //clearBackStackInclusive("");

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
        }
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
          /*  if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                //Displaying a toast
                //Toast.makeText(this, "Permission granted now you can read the storage", Toast.LENGTH_LONG).show();
            } else {
                //Displaying another toast if permission is not granted
              //  Toast.makeText(this, "Oops you just denied the permission", Toast.LENGTH_LONG).show();
            }*/
        }
    }

    private boolean isReadStorageAllowed() {
        //Getting the permission status
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
}

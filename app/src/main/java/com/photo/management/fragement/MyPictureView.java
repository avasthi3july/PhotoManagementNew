package com.photo.management.fragement;

import android.app.Activity;
import android.app.Dialog;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.reflect.TypeToken;
import com.photo.management.R;
import com.photo.management.activity.MainActivity;
import com.photo.management.adapter.MyPictureAdapter;
import com.photo.management.adapter.UserAdapter;
import com.photo.management.dao.BaseResponse;
import com.photo.management.dao.Photo;
import com.photo.management.dao.User;
import com.photo.management.database.DatabaseHandler;
import com.photo.management.delegates.Api;
import com.photo.management.delegates.RecyclerItemClickListener;
import com.photo.management.delegates.ServerApi;
import com.photo.management.delegates.ServiceCallBack;
import com.photo.management.util.DividerItemDecoration;
import com.photo.management.util.Util;
import com.photo.management.webservice.BaseRequest;
import com.photo.management.webservice.JsonDataParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit.RetrofitError;
import retrofit.mime.TypedFile;

/**
 * Created by kavasthi on 12/6/2016.
 */

public class MyPictureView extends Fragment implements View.OnClickListener, ServiceCallBack {
    private RecyclerView photoView;
    private ArrayList<Photo> picList;
    private DatabaseHandler db;
    private TextView imageCount;
    private int seletedPos, selectedUser;
    private MyPictureAdapter myPictureAdapter;
    private EditText searchText, userEmail, mobNum;
    private List<Photo> filteredList;
    private String searchValue = "", selectedPath = "", emailId = "";
    private boolean isDownLoad;
    private ArrayList<User> userList;
    private int uniqueId;
    private Button sendEmail, sendSms, contactList;
    private RelativeLayout closeDialog;
    private final int REQUEST_CODE=99;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.my_pic_view, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initLayout(view);

    }

    private void initLayout(View view) {
        SharedPreferences pref = Util.getSharedPreferences(getActivity());
        emailId = pref.getString("email", "");
        ((MainActivity) getActivity()).setHeaderName("Tag My Picture");
        db = new DatabaseHandler(getContext());
        picList = db.getAllPics();
        photoView = (RecyclerView) view.findViewById(R.id.photoView);
        imageCount = (TextView) view.findViewById(R.id.imageCount);

        searchText = (EditText) view.findViewById(R.id.searchBox);
        LinearLayoutManager ll = new LinearLayoutManager(getContext());
        photoView.setItemAnimator(new DefaultItemAnimator());
        ll.setOrientation(LinearLayoutManager.VERTICAL);
        photoView.setLayoutManager(ll);
        if (picList != null && picList.size() > 0) {
            Collections.reverse(picList);
            myPictureAdapter = new MyPictureAdapter(getActivity(), picList);
            photoView.setAdapter(myPictureAdapter);
            imageCount.setText("" + picList.size());

        }
        photoView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), photoView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                seletedPos = position;

                openDialog();
            }

            @Override
            public void onLongItemClick(View view, int position) {
            }
        }));
        searchText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
                searchValue = cs.toString();
                filteredList = filter(picList, cs.toString());
                myPictureAdapter.setFilter(filteredList);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable arg0) {

                // TODO Auto-generated method stub
            }
        });
    }

    private List<Photo> filter(List<Photo> models, String query) {
        query = query.toLowerCase();

        filteredList = new ArrayList<>();
        for (Photo model : models) {
            final String text = model.getTagName().toLowerCase();
            if (text.contains(query)) {
                filteredList.add(model);
            }
        }
        return filteredList;
    }

    private LinearLayout edit, email, message, delete, openImage;
    private ImageView crossClick, imgView;
    private Dialog dialog;

    private void openDialog() {
        try {
            dialog = new Dialog(getActivity(), R.style.MyDialogTheme);
            dialog.setContentView(R.layout.edit_layout);
            // set the custom dialog components - text, image and button
            openImage = (LinearLayout) dialog.findViewById(R.id.openLayout);
            edit = (LinearLayout) dialog.findViewById(R.id.editLayout);
            email = (LinearLayout) dialog.findViewById(R.id.emailLayout);
            message = (LinearLayout) dialog.findViewById(R.id.megLayout);
            delete = (LinearLayout) dialog.findViewById(R.id.delLayout);
            closeDialog = (RelativeLayout) dialog.findViewById(R.id.close_dialog);
            crossClick = (ImageView) dialog.findViewById(R.id.cross);
            imgView = (ImageView) dialog.findViewById(R.id.imgName);
            edit.setOnClickListener(this);
            email.setOnClickListener(this);
            message.setOnClickListener(this);
            delete.setOnClickListener(this);
            crossClick.setOnClickListener(this);
            openImage.setOnClickListener(this);
            closeDialog.setOnClickListener(this);
            if (searchValue.length() > 0)

                Glide.with(getActivity()).load(filteredList.get(seletedPos).getPicPath()).into(imgView);
            else Glide.with(getActivity()).load(picList.get(seletedPos).getPicPath()).into(imgView);
            dialog.show();

        } catch (Exception e) {

        }
    }

    private void userListDialog() {
        dialog = new Dialog(getActivity(), R.style.MyDialogTheme);
        dialog.setContentView(R.layout.user_list);
        // set the custom dialog components - text, image and button
        UserAdapter mUserAdapter = new UserAdapter(getActivity(), userList);
        RecyclerView recyclerView = (RecyclerView) dialog.findViewById(R.id.listUser);
        TextView headerName = (TextView) dialog.findViewById(R.id.select_id);
        sendEmail = (Button) dialog.findViewById(R.id.send_email);
        contactList = (Button) dialog.findViewById(R.id.contact_list);
        userEmail = (EditText) dialog.findViewById(R.id.email);
        sendEmail.setOnClickListener(this);
        contactList.setOnClickListener(this);
        headerName.setTypeface(null, Typeface.BOLD);
        LinearLayoutManager ll = new LinearLayoutManager(getContext());
        ll.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(ll);
        recyclerView.setAdapter(mUserAdapter);
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), photoView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                selectedUser = position;
                dialog.dismiss();
                sendImage(userList.get(selectedUser).getEmail());
            }

            @Override
            public void onLongItemClick(View view, int position) {
            }
        }));
        dialog.show();
    }

    private void smstDialog() {
        dialog = new Dialog(getActivity(), R.style.MyDialogTheme);
        dialog.setContentView(R.layout.mob_num);
        // set the custom dialog components - text, image and button
        sendSms = (Button) dialog.findViewById(R.id.send_sms);
        mobNum = (EditText) dialog.findViewById(R.id.mobNum);
        sendSms.setOnClickListener(this);
        dialog.show();
    }

    private String path, tagName;

    @Override
    public void onClick(View v) {
        if (searchValue.length() > 0) {
            uniqueId = filteredList.get(seletedPos).getRefId();
            path = filteredList.get(seletedPos).getPicPath();
            tagName = filteredList.get(seletedPos).getTagName();
            isDownLoad = filteredList.get(seletedPos).isDownload();
        } else {
            uniqueId = picList.get(seletedPos).getRefId();
            path = picList.get(seletedPos).getPicPath();
            tagName = picList.get(seletedPos).getTagName();
            isDownLoad = picList.get(seletedPos).isDownload();
        }
        // dialog.dismiss();
        if (v == edit) {
            dialog.dismiss();
            Photo mPhoto = db.getPicDetail(uniqueId);
            TagPictureView mTagPictureView = new TagPictureView();
            Bundle bundle = new Bundle();
            bundle.putSerializable("photoDetail", mPhoto);
            mTagPictureView.setArguments(bundle);
            ((MainActivity) getActivity()).addFragementView(mTagPictureView);
        } else if (v == email) {
            dialog.dismiss();
            getUserList();
            //openGmail();

        } else if (v == message) {
            dialog.dismiss();
            //openMessageApp();
            smstDialog();

        } else if (v == delete) {
            dialog.dismiss();
            db.deletePicture(uniqueId);
            picList.remove(seletedPos);
            // if (filteredList != null)
            // filteredList.remove(seletedPos);
            myPictureAdapter.notifyDataSetChanged();
            imageCount.setText("" + picList.size());
        } else if (v == sendEmail) {
            //selectedUser = position;
            if (userEmail.getText() != null && userEmail.getText().toString().length() > 0) {
                if (Util.isValidEmail(userEmail.getText().toString())) {
                    dialog.dismiss();
                    sendImage(userEmail.getText().toString());
                } else {
                    Util.showToast(getActivity(), "Please enter valid email.");
                }
            } else {
                Util.showToast(getActivity(), "Please enter email.");
            }

        } else if (v == closeDialog | v == crossClick) {
            dialog.dismiss();
        } else if (v == openImage) {
            dialog.dismiss();

            Photo mPhoto = db.getPicDetail(uniqueId);
            FullImageView mFullImageView = new FullImageView();
            Bundle bundle = new Bundle();
            bundle.putString("picPath", mPhoto.getPicPath());
            mFullImageView.setArguments(bundle);
            ((MainActivity) getActivity()).addFragementView(mFullImageView);
        } else if (v == sendSms) {
            dialog.dismiss();
            if (mobNum != null && mobNum.length() > 0)
                sendImageSms();
            else Util.showDialog(getActivity(), "Please enter mobile number");

        } else if (v == contactList) {
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            startActivityForResult(intent, REQUEST_CODE);
        }

    }

    private void openGmail() {
        //String path = picList.get(seletedPos).getPicPath();
        String[] parts = path.split("/");
        String preFile = path.substring(0, path.lastIndexOf("/") + 1);
        String imgName = parts[parts.length - 1];

        File prePath = new File(preFile, imgName);
        File newPath = new File(preFile, tagName + ".jpg");
        if (prePath.exists())
            prePath.renameTo(newPath);
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("text/plain");
        //emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{userList.get(selectedUser).getEmail()});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "");
        emailIntent.putExtra(Intent.EXTRA_TEXT, tagName);
     /* File file = new File(path);
        if (!file.exists() || !file.canRead()) {
            return;
        }*/
        db.updateImagePath(uniqueId, newPath.getAbsolutePath());
        Uri uri = Uri.fromFile(newPath);


        emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
        final PackageManager pm = getActivity().getPackageManager();
        final List<ResolveInfo> matches = pm.queryIntentActivities(emailIntent, 0);
        ResolveInfo best = null;
        for (final ResolveInfo info : matches)
            if (info.activityInfo.packageName.endsWith(".gm") || info.activityInfo.name.toLowerCase().contains("gmail"))
                best = info;
        if (best != null)
            emailIntent.setClassName(best.activityInfo.packageName, best.activityInfo.name);
        startActivity(Intent.createChooser(emailIntent, "Pick an Email provider"));
    }

    private void getImageFromCache() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        String urlOfImageToDownload = selectedPath;
        String attachmentFileName = tagName + ".png";

// Start to build up the email intent
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{userList.get(selectedUser).getEmail()});
        i.putExtra(Intent.EXTRA_SUBJECT, "");
        i.putExtra(Intent.EXTRA_TEXT, tagName);

// Do we need to download and attach an icon and is the SD Card available?
        if (urlOfImageToDownload != null && Environment.MEDIA_MOUNTED.equals(
                Environment.getExternalStorageState())) {
            // Download the icon...
            try {
                URL iconUrl = new URL(urlOfImageToDownload);
                HttpURLConnection connection
                        = (HttpURLConnection) iconUrl.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap immutableBpm = BitmapFactory.decodeStream(input);

                // Save the downloaded icon to the pictures folder on the SD Card
                File directory = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES);
                directory.mkdirs(); // Make sure the Pictures directory exists.
                File destinationFile = new File(directory, attachmentFileName);
                FileOutputStream out = new FileOutputStream(destinationFile);
                immutableBpm.compress(Bitmap.CompressFormat.PNG, 90, out);
                out.flush();
                out.close();
                Uri mediaStoreImageUri = Uri.fromFile(destinationFile);
                i.putExtra(Intent.EXTRA_STREAM, mediaStoreImageUri);
            } catch (IOException e) {

            }
        }
        startActivity(i);


    }

    private void openMessageApp() {
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setPackage("com.android.mms");
        sendIntent.putExtra("sms_body", tagName);
        sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(path)));
        sendIntent.setType("image/png");
        startActivity(sendIntent);

    }

    private void sendImageSms() {
        TypedFile typedFile;
        if (!isDownLoad) {

            File file = new File(path);
            typedFile = new TypedFile("multipart/form-data", file);
            selectedPath = "";
        } else {
            typedFile = null;
            selectedPath = path;
        }
        BaseRequest baseRequest = new BaseRequest(getActivity());
        baseRequest.setProgressShow(true);
        baseRequest.setRequestTag(Api.SEND_SMS);
        baseRequest.setServiceCallBack(this);
        baseRequest.setMessage("Sending Picture. Please Wait...");
        Api api = (Api) baseRequest.execute(Api.class);
        api.sendSms("send_sms", mobNum.getText().toString(), typedFile, selectedPath, tagName, emailId, baseRequest.requestCallback());
    }

    private void getUserList() {
        //SharedPreferences pref = Util.getSharedPreferences(getActivity());
        // emailId = pref.getString("email", "");
        BaseRequest baseRequest = new BaseRequest(getActivity());
        baseRequest.setProgressShow(true);
        baseRequest.setRequestTag(Api.GET_LIST_DATA);
        baseRequest.setMessage("Please wait...");
        baseRequest.setServiceCallBack(this);
        Api api = (Api) baseRequest.execute(Api.class);
        api.getList("listing", emailId, baseRequest.requestCallback());
    }

    public void sendImage(String email) {
        TypedFile typedFile;
        if (!isDownLoad) {

            File file = new File(path);
            typedFile = new TypedFile("multipart/form-data", file);
            selectedPath = "";
        } else {
            typedFile = null;
            selectedPath = path;
        }
        BaseRequest baseRequest = new BaseRequest(getActivity());
        baseRequest.setProgressShow(true);
        baseRequest.setRequestTag(Api.ADD_IMAGE);
        baseRequest.setServiceCallBack(this);
        baseRequest.setMessage("Sending Picture. Please Wait...");
        Api api = (Api) baseRequest.execute(Api.class);
        api.sendImage("send_image", email, typedFile, selectedPath, tagName, emailId, baseRequest.requestCallback());

    }

    @Override
    public void onSuccess(int tag, String baseResponse) {
        if (tag == Api.GET_LIST_DATA) {
            BaseResponse<ArrayList<User>> baseData = JsonDataParser.getInternalParser(baseResponse, new TypeToken<BaseResponse<ArrayList<User>>>() {
            }.getType());
            userList = new ArrayList<>();
            userList.addAll(baseData.getData());
            userListDialog();
        } else if (tag == Api.ADD_IMAGE) {
            BaseResponse baseData = JsonDataParser.getInternalParser(baseResponse, new TypeToken<BaseResponse>() {
            }.getType());
            if (baseData.getSuccess().equalsIgnoreCase("1")) {
                Util.showToast(getActivity(), "Image Successfully Sent.");
            } else Util.showToast(getActivity(), "Please try again.");
           /* if (isDownLoad)
                getImageFromCache();
            else openGmail();*/
        } else if (tag == Api.SEND_SMS) {
            BaseResponse baseData = JsonDataParser.getInternalParser(baseResponse, new TypeToken<BaseResponse>() {
            }.getType());
            if (baseData.getSuccess().equalsIgnoreCase("1")) {
                Util.showToast(getActivity(), "Image Successfully Sent.");
            } else Util.showToast(getActivity(), "Please try again.");
        }
    }

    @Override
    public void onFail(RetrofitError error) {

    }

    @Override
    public void onNoNetwork() {
        

    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        switch (reqCode) {
            case (REQUEST_CODE):
                if (resultCode == Activity.RESULT_OK) {
                    Uri contactData = data.getData();
                    Cursor c = getActivity().getContentResolver().query(contactData, null, null, null, null);
                    if (c.moveToFirst()) {
                        String contactId = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
                        String hasNumber = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                        String hasEmail= c.getString(c.getColumnIndex(ContactsContract.Contacts.Em));
                        String num = "";
                        if (Integer.valueOf(hasNumber) == 1) {
                            Cursor numbers = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
                            while (numbers.moveToNext()) {
                                num = numbers.getString(numbers.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                Toast.makeText(getActivity(), "Number=" + num, Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                    break;
                }
        }
    }

}

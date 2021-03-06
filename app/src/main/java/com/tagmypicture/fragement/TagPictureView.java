package com.tagmypicture.fragement;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.gson.reflect.TypeToken;
import com.tagmypicture.R;
import com.tagmypicture.activity.MainActivity;
import com.tagmypicture.dao.BaseResponse;
import com.tagmypicture.dao.Photo;
import com.tagmypicture.database.DatabaseHandler;
import com.tagmypicture.delegates.Api;
import com.tagmypicture.delegates.FragmentCommunicator;
import com.tagmypicture.delegates.ServerApi;
import com.tagmypicture.delegates.ServiceCallBack;
import com.tagmypicture.util.Util;
import com.tagmypicture.webservice.BaseRequest;
import com.tagmypicture.webservice.JsonDataParser;

import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;

import retrofit.RetrofitError;
import retrofit.mime.TypedFile;

/**
 * Created by kavasthi on 12/9/2016.
 */

public class TagPictureView extends Fragment implements View.OnClickListener, FragmentCommunicator, ServiceCallBack {
    private ImageView selectedView, voiceView;
    private final int SPEECH_RECOGNITION_CODE = 1;
    protected static final int RESULT_SPEECH = 1;
    private EditText tagName;
    private Button saveBtn, removeAds;
    private DatabaseHandler db;
    private int refId, imageId;
    private Fragment fragment;
    private String picPath = "", selectedPath = "", path = "", emailId = "";
    private SharedPreferences pref;
    private boolean isEdit = false, isDownload = false;
    String months[] = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep",
            "Oct", "Nov", "Dec"};

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initLayout(view);
    }

    private void initLayout(View view) {

        fragment = new TagPictureView();
        pref = Util.getSharedPreferences(getActivity());
        emailId = pref.getString("email", "");
        /*else Util.showAd(getActivity());*/
        db = new DatabaseHandler(getActivity());

        selectedView = (ImageView) view.findViewById(R.id.selectedImage);
        voiceView = (ImageView) view.findViewById(R.id.voice_view);
        tagName = (EditText) view.findViewById(R.id.tagName);
        saveBtn = (Button) view.findViewById(R.id.saveBtn);
        removeAds = (Button) view.findViewById(R.id.remove_ads);
        //saveBtn.setTypeface(Typeface.DEFAULT_BOLD);
        Bundle bundle = this.getArguments();
        if (pref.getBoolean("isPremium1", false)) {
            removeAds.setVisibility(View.GONE);
        }
        if (bundle != null) {
            picPath = bundle.getString("picPath");
            if (bundle.getSerializable("photoDetail") != null) {
                ((MainActivity) getActivity()).setHeaderName("Edit Picture");
                isEdit = true;
                Photo mPhoto = (Photo) bundle.getSerializable("photoDetail");
                picPath = mPhoto.getPicPath();
                imageId = mPhoto.getRefId();
                tagName.setText("" + mPhoto.getTagName());
            } else ((MainActivity) getActivity()).setHeaderName("Tag Picture");
        }
        saveBtn.setOnClickListener(this);
        removeAds.setOnClickListener(this);
        voiceView.setOnClickListener(this);
        File imgFile = new File(picPath);
        if (imgFile.exists()) {
            //tagName.setClickable(true);
            //voiceView.setClickable(true);

            isDownload = false;
            refId = imgFile.getPath().hashCode();
            try {
                Glide.with(getActivity()).load(imgFile)
                        .into(selectedView);
                // selectedView.setImageBitmap(com.tagmypicture.util.Util.getBitMap(imgFile));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            isDownload = true;
            tagName.setFocusable(false);
            tagName.setClickable(false);
            voiceView.setClickable(false);
            refId = picPath.hashCode();
            try {
                Glide.with(getActivity()).load(picPath)
                        .into(selectedView);
                // selectedView.setImageBitmap(com.tagmypicture.util.Util.getBitMap(imgFile));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tag_view, container, false);
    }

    @Override
    public void onClick(View v) {
        if (v == saveBtn) {
            if (tagName.getText() != null && tagName.getText().toString().length() > 0) {
                /*GregorianCalendar gcalendar = new GregorianCalendar();
                String dateTime = months[gcalendar.get(Calendar.MONTH)] + " " + gcalendar.get(Calendar.DATE) + " " + gcalendar.get(Calendar.YEAR);
                Photo mPhoto = new Photo();
                mPhoto.setRefId(refId);
                mPhoto.setPicPath(picPath);
                mPhoto.setTagName(tagName.getText().toString());
                mPhoto.setDownload(isDownload);
                mPhoto.setTag(true);
                mPhoto.setDate(dateTime);
                if (isEdit) {
                    db.updateContact(mPhoto);
                } else {
                    db.tagImageDb(mPhoto);
                }*/
                if (isEdit) {
                    editImage("edit_image", imageId, tagName.getText().toString());
                } else {
                    addImageServer(picPath, tagName.getText().toString());
                }

                /*com.tagmypicture.util.Util.showToast(getActivity(), "Picture Successfully Tagged");
                ((MainActivity) getActivity()).clearBackStackInclusive(fragment.getClass().getName());
                MyPictureView myPictureView = new MyPictureView();
                ((MainActivity) getActivity()).addFragementView(myPictureView);
                if (!pref.getBoolean("isPremium1", false))
                    Util.showAd(getActivity());*/
            } else com.tagmypicture.util.Util.showToast(getActivity(), "Please Enter TagName");
        } else if (v == voiceView) {
            if (Util.isNetworkConnected(getActivity())) {
                if (!isDownload)
                    ((MainActivity) getActivity()).startSpeechToText();
                else Util.showToast(getActivity(), "This is non editable");
            } else Util.showToast(getActivity(), "Please check internet connection");
        } else if (v == removeAds) {
            try {
                if (!pref.getBoolean("isPremium1", false))
                    ((MainActivity) getActivity()).onInfiniteGasButtonClicked(v, "2");
                else removeAds.setVisibility(View.GONE);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void passDataToFragment(String someValue) {
        tagName.setText(someValue);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((MainActivity) getActivity()).fragmentCommunicator = this;
    }

    public void updateAdsButton() {
        if (removeAds != null)
            removeAds.setVisibility(View.GONE);

    }

    public void addImageServer(String path, String tagName) {
        TypedFile typedFile;
        if (!isDownload) {
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
        System.out.println("EMAILL>>>" + emailId + "selectedPath>>>>>>" + selectedPath + "tagName>>" + tagName);
        api.addImage("add_image", emailId, typedFile, selectedPath, tagName, baseRequest.requestCallback());

    }

    public void editImage(String type, int imageId, String tag) {
        BaseRequest baseRequest = new BaseRequest(getActivity());
        baseRequest.setProgressShow(true);
        baseRequest.setRequestTag(Api.EDIT_IMAGE);
        baseRequest.setMessage("Please wait...");
        baseRequest.setServiceCallBack(this);
        Api api = (Api) baseRequest.execute(Api.class);
        api.editImage(type, String.valueOf(imageId), tag, baseRequest.requestCallback());

    }

    @Override
    public void onSuccess(int tag, String baseResponse) {
        if (tag == Api.ADD_IMAGE) {
            BaseResponse data = JsonDataParser.getInternalParser(baseResponse, new TypeToken<BaseResponse>() {
            }.getType());
            // Util.showDialog(this, data.getSuccess());
            Log.v("DATTTA", data.getSuccess());
            if (data.getSuccess().equalsIgnoreCase("2")) {
                Util.showToast(getActivity(), "Picture Successfully Tagged");
                GregorianCalendar gcalendar = new GregorianCalendar();




                String dateTime = months[gcalendar.get(Calendar.MONTH)] + " " + gcalendar.get(Calendar.DATE) + " " + gcalendar.get(Calendar.YEAR);
                Photo mPhoto = new Photo();
                mPhoto.setRefId(data.getImageId());
                mPhoto.setPicPath(picPath);
                mPhoto.setTagName(tagName.getText().toString());
                mPhoto.setDownload(isDownload);
                mPhoto.setTag(true);
                mPhoto.setDate(dateTime);
                if (isEdit) {
                    db.updateContact(mPhoto);
                } else {
                    db.tagImageDb(mPhoto);
                }
                ((MainActivity) getActivity()).clearBackStackInclusive(fragment.getClass().getName());
                MyPictureView myPictureView = new MyPictureView();
                ((MainActivity) getActivity()).addFragementView(myPictureView);
                if (!pref.getBoolean("isPremium1", false))
                    Util.showAd(getActivity());
            } else Util.showToast(getActivity(), data.getMessage());
        } else if (tag == Api.EDIT_IMAGE) {
            BaseResponse data = JsonDataParser.getInternalParser(baseResponse, new TypeToken<BaseResponse>() {
            }.getType());
            if (data.getSuccess().equalsIgnoreCase("2")) {
                GregorianCalendar gcalendar = new GregorianCalendar();
                String dateTime = months[gcalendar.get(Calendar.MONTH)] + " " + gcalendar.get(Calendar.DATE) + " " + gcalendar.get(Calendar.YEAR);
                Photo mPhoto = new Photo();
                mPhoto.setRefId(imageId);
                mPhoto.setPicPath(picPath);
                mPhoto.setTagName(tagName.getText().toString());
                mPhoto.setDownload(isDownload);
                mPhoto.setTag(true);
                mPhoto.setDate(dateTime);
                db.updateContact(mPhoto);
                Util.showToast(getActivity(), data.getMessage());
                ((MainActivity) getActivity()).clearBackStackInclusive(fragment.getClass().getName());
                MyPictureView myPictureView = new MyPictureView();
                ((MainActivity) getActivity()).addFragementView(myPictureView);
                if (!pref.getBoolean("isPremium1", false))
                    Util.showAd(getActivity());
            } else Util.showToast(getActivity(), data.getMessage());


        }

    }

    @Override
    public void onFail(RetrofitError error) {

    }

    @Override
    public void onNoNetwork() {

    }

    private void editDeleteImage() {

    }
}

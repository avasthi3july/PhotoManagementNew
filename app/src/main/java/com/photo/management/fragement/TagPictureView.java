package com.photo.management.fragement;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.photo.management.R;
import com.photo.management.activity.MainActivity;
import com.photo.management.dao.Photo;
import com.photo.management.database.DatabaseHandler;
import com.photo.management.delegates.FragmentCommunicator;
import com.photo.management.util.Util;

import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by kavasthi on 12/9/2016.
 */

public class TagPictureView extends Fragment implements View.OnClickListener, FragmentCommunicator {
    private ImageView selectedView, voiceView;
    private final int SPEECH_RECOGNITION_CODE = 1;
    protected static final int RESULT_SPEECH = 1;
    private EditText tagName;
    private Button saveBtn;
    private DatabaseHandler db;
    private int refId;
    private Fragment fragment;
    private String picPath = "";
    private boolean isEdit = false,isDownload=false;
    String months[] = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep",
            "Oct", "Nov", "Dec"};

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initLayout(view);
    }

    private void initLayout(View view) {
        fragment = new TagPictureView();
        
        db = new DatabaseHandler(getActivity());

        selectedView = (ImageView) view.findViewById(R.id.selectedImage);
        voiceView = (ImageView) view.findViewById(R.id.voice_view);
        tagName = (EditText) view.findViewById(R.id.tagName);
        saveBtn = (Button) view.findViewById(R.id.saveBtn);
        //saveBtn.setTypeface(Typeface.DEFAULT_BOLD);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            picPath = bundle.getString("picPath");
            if (bundle.getSerializable("photoDetail") != null) {
                ((MainActivity) getActivity()).setHeaderName("Edit Picture");
                isEdit = true;
                Photo mPhoto = (Photo) bundle.getSerializable("photoDetail");
                picPath = mPhoto.getPicPath();
                tagName.setText("" + mPhoto.getTagName());
            } else ((MainActivity) getActivity()).setHeaderName("Tag Picture");
        }
        saveBtn.setOnClickListener(this);
        voiceView.setOnClickListener(this);
        File imgFile = new File(picPath);
        if (imgFile.exists()) {
            //tagName.setClickable(true);
            //voiceView.setClickable(true);

            isDownload=false;
            refId = imgFile.getPath().hashCode();
            try {
                Glide.with(getActivity()).load(imgFile)
                        .into(selectedView);
                // selectedView.setImageBitmap(com.photo.management.util.Util.getBitMap(imgFile));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            isDownload=true;
            tagName.setFocusable(false);
            tagName.setClickable(false);
            voiceView.setClickable(false);
            refId = picPath.hashCode();
            try {
                Glide.with(getActivity()).load(picPath)
                        .into(selectedView);
                // selectedView.setImageBitmap(com.photo.management.util.Util.getBitMap(imgFile));
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
                GregorianCalendar gcalendar = new GregorianCalendar();
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
                }
                com.photo.management.util.Util.showToast(getActivity(), "Picture Successfully Tagged");
                ((MainActivity) getActivity()).clearBackStackInclusive(fragment.getClass().getName());
                MyPictureView myPictureView = new MyPictureView();
                ((MainActivity) getActivity()).addFragementView(myPictureView);
            } else com.photo.management.util.Util.showToast(getActivity(), "Please Enter TagName");
        } else if (v == voiceView) {
            if (Util.isNetworkConnected(getActivity())) {
                if(!isDownload)
                ((MainActivity) getActivity()).startSpeechToText();
                else Util.showToast(getActivity(), "This is non editable");
            }
            else Util.showToast(getActivity(), "Please check internet connection");
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

}

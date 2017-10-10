package com.tagmypicture.fragement;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
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
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.gson.reflect.TypeToken;
import com.mukesh.countrypicker.CountryPicker;
import com.mukesh.countrypicker.CountryPickerListener;
import com.tagmypicture.R;
import com.tagmypicture.activity.MainActivity;
import com.tagmypicture.adapter.MyPictureAdapter;
import com.tagmypicture.adapter.UserAdapter;
import com.tagmypicture.dao.BaseResponse;
import com.tagmypicture.dao.Photo;
import com.tagmypicture.dao.User;
import com.tagmypicture.database.DatabaseHandler;
import com.tagmypicture.delegates.AdapterCall;
import com.tagmypicture.delegates.Api;
import com.tagmypicture.delegates.ContactPermission;
import com.tagmypicture.delegates.RecyclerItemClickListener;
import com.tagmypicture.delegates.ServiceCallBack;
import com.tagmypicture.delegates.EmailName;
import com.tagmypicture.inapppurchase.IabHelper;
import com.tagmypicture.inapppurchase.IabResult;
import com.tagmypicture.inapppurchase.Inventory;
import com.tagmypicture.inapppurchase.Purchase;
import com.tagmypicture.util.Util;
import com.tagmypicture.webservice.BaseRequest;
import com.tagmypicture.webservice.JsonDataParser;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

import retrofit.RetrofitError;
import retrofit.mime.TypedFile;

/**
 * Created by kavasthi on 12/6/2016.
 */

public class MyPictureView extends Fragment implements View.OnClickListener, ServiceCallBack, EmailName, AdapterCall, ContactPermission {
    private RecyclerView photoView;
    private ArrayList<Photo> picList;
    private DatabaseHandler db;
    private TextView imageCount, countryCode;
    private int seletedPos, selectedUser;
    private MyPictureAdapter myPictureAdapter;
    private static EditText searchText, userEmail, mobNum;
    private List<Photo> filteredList;
    private String searchValue = "", selectedPath = "", emailId = "";
    private boolean isDownLoad;
    private ArrayList<User> userList;
    private int uniqueId;
    private Button sendEmail, sendSms, contactList, phoneNum;
    private RelativeLayout closeDialog;
    // private final int REQUEST_CODE=99;
    protected static final int REQUEST_CODE = 99, REQUEST_CODE_PHONE = 90;
    private SharedPreferences pref;
    private View view;
    InterstitialAd mInterstitialAd;

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
        pref = Util.getSharedPreferences(getActivity());
        /*if (!pref.getBoolean("isPremium1", false))
            Util.showAd(getActivity());*/


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

    private void showInterstitial() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }

    private LinearLayout edit, email, message, delete, openImage;
    private ImageView crossClick, imgView, emailIcon, megIcon;
    private Dialog dialog;
    private TextView emailText, megText;

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
            emailIcon = (ImageView) dialog.findViewById(R.id.email_icn);
            megIcon = (ImageView) dialog.findViewById(R.id.meg_icn);
            emailText = (TextView) dialog.findViewById(R.id.email_txt);
            megText = (TextView) dialog.findViewById(R.id.message);
            isPremium();
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

    private void isPremium() {
        if (pref.getBoolean("isPremium", false)) {
            emailText.setTextColor(Color.parseColor("#1CABD4"));
            megText.setTextColor(Color.parseColor("#1CABD4"));
            emailIcon.setColorFilter(Color.parseColor("#1CABD4"));
            megIcon.setColorFilter(Color.parseColor("#1CABD4"));
        }
    }

    private void userListDialog() {
        dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.send_email);
        dialog.getWindow()
                .setLayout((int) (getScreenWidth(getActivity()) * .9), ViewGroup.LayoutParams.WRAP_CONTENT);
        // set the custom dialog components - text, image and button
        UserAdapter mUserAdapter = new UserAdapter(getActivity(), userList);
        // RecyclerView recyclerView = (RecyclerView) dialog.findViewById(R.id.listUser);
        //TextView headerName = (TextView) dialog.findViewById(R.id.select_id);
        sendEmail = (Button) dialog.findViewById(R.id.send_email);
        contactList = (Button) dialog.findViewById(R.id.contact_list);
        userEmail = (EditText) dialog.findViewById(R.id.email);
        sendEmail.setOnClickListener(this);
        contactList.setOnClickListener(this);
        // headerName.setTypeface(null, Typeface.BOLD);
        LinearLayoutManager ll = new LinearLayoutManager(getContext());
        ll.setOrientation(LinearLayoutManager.VERTICAL);
       /* recyclerView.setLayoutManager(ll);
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
        }));*/

        dialog.show();
    }

    public static int getScreenWidth(Activity activity) {
        Point size = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(size);
        return size.x;
    }

    private void smsDialog() {
        dialog = new Dialog(getActivity(), R.style.MyDialogTheme);
        dialog.setContentView(R.layout.send_phone);
        dialog.getWindow()
                .setLayout((int) (getScreenWidth(getActivity()) * .9), ViewGroup.LayoutParams.WRAP_CONTENT);
        // set the custom dialog components - text, image and button
        sendSms = (Button) dialog.findViewById(R.id.send_sms);
        mobNum = (EditText) dialog.findViewById(R.id.mobNum);
        phoneNum = (Button) dialog.findViewById(R.id.contact_list);
        countryCode = (TextView) dialog.findViewById(R.id.country_code);
        sendSms.setOnClickListener(this);
        phoneNum.setOnClickListener(this);
        countryCode.setOnClickListener(this);
        dialog.show();
    }

    private String path, tagName;
    CountryPicker picker;

    public void deleteImage(int imageId) {
        BaseRequest baseRequest = new BaseRequest(getActivity());
        baseRequest.setProgressShow(true);
        baseRequest.setRequestTag(Api.DELETE_IMAGE);
        baseRequest.setMessage("Please wait...");
        baseRequest.setServiceCallBack(this);
        Api api = (Api) baseRequest.execute(Api.class);
        api.deleteImage("delete_image", String.valueOf(imageId), baseRequest.requestCallback());

    }

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
            view = v;
            if (!pref.getBoolean("isPremium", false))
                ((MainActivity) getActivity()).onInfiniteGasButtonClicked(v, "1");
            else getUserList();
            // getUserList();

        } else if (v == message) {
            dialog.dismiss();
            //openMessageApp();
            view = v;
            if (!pref.getBoolean("isPremium", false))
                ((MainActivity) getActivity()).onInfiniteGasButtonClicked(v, "1");
            else smsDialog();

        } else if (v == delete) {
            dialog.dismiss();

            deleteImage(uniqueId);
            // if (filteredList != null)
            // filteredList.remove(seletedPos);

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

            if (mobNum != null && mobNum.length() > 0) {
                dialog.dismiss();
                sendImageSms();
            } else Util.showDialog(getActivity(), "Please enter mobile number");

        } else if (v == contactList) {
            if (((MainActivity) getActivity()).isCheckPermissionForContact()) {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                getActivity().startActivityForResult(intent, REQUEST_CODE);
                return;
            }
            ((MainActivity) getActivity()).requestPermissionForReadContact();
        } else if (v == phoneNum) {
            if (((MainActivity) getActivity()).isCheckPermissionForContact()) {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                getActivity().startActivityForResult(intent, REQUEST_CODE_PHONE);
                return;
            }
            ((MainActivity) getActivity()).requestPermissionForReadContact();
        } else if (v == countryCode) {
            picker = CountryPicker.newInstance("Select Country");
            picker.setListener(new CountryPickerListener() {
                @Override
                public void onSelectCountry(String name, String code, String dialCode, int flagDrawableResID) {
                    picker.dismiss();
                    countryCode.setText(dialCode);
                    // countryCode.setCompoundDrawablesWithIntrinsicBounds(flagDrawableResID, 0, 0, 0);
                }
            });
            picker.show(getActivity().getSupportFragmentManager(), "COUNTRY_PICKER");
        }

    }

    private void sendImageSms() {
        String phoneNum = countryCode.getText().toString().concat(mobNum.getText().toString());
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
        System.out.println("NUMM>>" + phoneNum);
        api.sendSms("send_sms", phoneNum, typedFile, selectedPath, tagName, emailId, baseRequest.requestCallback());
    }

    private void getUserList() {
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
        System.out.println("EMAILL" + email);
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
                if (dialog != null)
                    dialog.dismiss();
                Util.showToast(getActivity(), "Image Successfully Sent.");
            } else Util.showToast(getActivity(), "Please try again.");
        } else if (tag == Api.SEND_SMS) {
            BaseResponse baseData = JsonDataParser.getInternalParser(baseResponse, new TypeToken<BaseResponse>() {
            }.getType());
            if (baseData.getSuccess().equalsIgnoreCase("1")) {
                Util.showToast(getActivity(), "Image Successfully Sent.");
            } else Util.showToast(getActivity(), "Please try again.");
        }
        else if(tag==Api.DELETE_IMAGE)
        {
            BaseResponse data = JsonDataParser.getInternalParser(baseResponse, new TypeToken<BaseResponse>() {
            }.getType());
            if (data.getSuccess().equalsIgnoreCase("2") || data.getSuccess().equalsIgnoreCase("1")) {
                db.deletePicture(uniqueId);
                picList.remove(seletedPos);
                imageCount.setText("" + picList.size());
                myPictureAdapter.notifyDataSetChanged();

            }
            else Util.showToast(getActivity(), data.getMessage());




        }
    }

    @Override
    public void onFail(RetrofitError error) {

    }

    @Override
    public void onNoNetwork() {


    }


    @Override
    public void setEmailId(String emailId) {
        if (emailId != null && emailId.length() > 0)
            userEmail.setText(emailId);
        else {
            userEmail.setText("");
            Util.showToast(getActivity(), "No Email Found- Please Update Contact");
        }
    }

    @Override
    public void setPhone(String phone) {
        if (phone != null && phone.length() > 0)
            mobNum.setText(phone);
        else {
            mobNum.setText("");
            Util.showToast(getActivity(), "No Phone Number Found - Please Update Contact");
        }
    }


    @Override
    public void grandPermissionToFragment() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        getActivity().startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    public void reFreshAdapter() {
        picList.clear();
        picList.addAll(db.getAllPics());
        Collections.reverse(picList);
        imageCount.setText("" + picList.size());
        myPictureAdapter.notifyDataSetChanged();

    }

    public void updateMessageButton() {
        if (view == email)
            getUserList();
        else if (view == message)
            smsDialog();

    }
}

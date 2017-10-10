package com.tagmypicture.delegates;


import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.mime.TypedFile;

/**
 * Created by Admin on 9/21/2015.
 */
public interface Api {

    int ADD_IMAGE = 10001;
    int GET_LIST_DATA = 10002;
    int ADD_USER_REGISTRATION = 10004;
    int EDIT_IMAGE = 10005;
    int GET_IMAGE = 10006;
    int GET_USER_EXIST = 10007;
    int SEND_SMS = 10008;
    int GET_ALL_IMAGE = 10008;
    int DELETE_IMAGE = 10009;


    @FormUrlEncoded
    @POST("/api.php")
    void userRegistreation(@Field("type") String type,
                           @Field("email") String email,
                           @Field("phone") String phone,
                           @Field("device_id") String deviceId,
                           Callback<String> callback);
    @FormUrlEncoded
    @POST("/api.php")
    void editImage(@Field("type") String type,
                           @Field("image_id") String imageId,
                           @Field("tag") String tag,
                           Callback<String> callback);

    @FormUrlEncoded
    @POST("/api.php")
    void deleteImage(@Field("type") String type,
                   @Field("image_id") String imageId,
                   Callback<String> callback);

    @FormUrlEncoded
    @POST("/api.php")
    void getList(@Field("type") String type,
                 @Field("email") String email,
                 Callback<String> callback);

    @Multipart
    @POST("/api.php")
    void addImage(@Part("type") String type,
                   @Part("email") String emailId,
                   @Part("imgurl") TypedFile file,
                   @Part("r_imgurl") String url,
                   @Part("tag") String tag,
                   Callback<String> callback);

    @Multipart
    @POST("/api.php")
    void sendImage(@Part("type") String type,
                   @Part("email") String emailId,
                   @Part("imgurl") TypedFile file,
                   @Part("r_imgurl") String url,
                   @Part("tag") String tag,
                   @Part("fromemail") String fromemail,
                   Callback<String> callback);


    @Multipart
    @POST("/api.php")
    void sendSms(@Part("type") String type,
                   @Part("phone") String emailId,
                   @Part("imgurl") TypedFile file,
                   @Part("r_imgurl") String url,
                   @Part("tag") String tag,
                   @Part("fromemail") String fromemail,
                   Callback<String> callback);

    @FormUrlEncoded
    @POST("/api.php")
    void getImage(@Field("type") String type,
                  @Field("email") String email,
                  Callback<String> callback);

    @FormUrlEncoded
    @POST("/api.php")
    void getUserExist(@Field("type") String type,
                      @Field("email") String email,
                      @Field("device_id") String deviceId,
                      Callback<String> callback);
}


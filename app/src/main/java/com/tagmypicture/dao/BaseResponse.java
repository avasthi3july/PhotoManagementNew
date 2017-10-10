package com.tagmypicture.dao;

import com.google.gson.annotations.SerializedName;

/**
 * Created by kavasthi on 4/11/2016.
 */
public class BaseResponse<T> {
    private String message;
    private String success;
    @SerializedName("image_id")
    private int imageId;
    private T data;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }
}

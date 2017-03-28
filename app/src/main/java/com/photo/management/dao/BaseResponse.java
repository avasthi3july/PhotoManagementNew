package com.photo.management.dao;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by kavasthi on 4/11/2016.
 */
public class BaseResponse<T> {
    private String message;
    private String success;
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
}

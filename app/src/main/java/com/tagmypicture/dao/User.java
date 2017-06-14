package com.tagmypicture.dao;

import com.google.gson.annotations.SerializedName;

/**
 * Created by kavasthi on 12/29/2016.
 */

public class User {
    private String id;
    private String email;
    @SerializedName("device_id")
    private String deviceId;
    private String phone;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}

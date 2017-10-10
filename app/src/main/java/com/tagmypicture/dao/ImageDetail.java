package com.tagmypicture.dao;

import com.google.gson.annotations.SerializedName;

/**
 * Created by kavasthi on 12/30/2016.
 */

public class ImageDetail {
    private String email;
    private String imgurl;
    private String tag;
    @SerializedName("created_on")
    private String createdon;
    private int id;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImgurl() {
        return imgurl;
    }

    public void setImgurl(String imgurl) {
        this.imgurl = imgurl;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getCreatedon() {
        return createdon;
    }

    public void setCreatedon(String createdon) {
        this.createdon = createdon;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}

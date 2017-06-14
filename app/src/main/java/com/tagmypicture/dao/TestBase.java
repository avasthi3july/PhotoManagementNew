package com.tagmypicture.dao;

import java.util.ArrayList;

/**
 * Created by kavasthi on 3/7/2017.
 */

public class TestBase {
    private String id;
    private String name;
    private String pId;
    private ArrayList<TestBase> subFolder;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<TestBase> getSubFolder() {
        return subFolder;
    }

    public void setSubFolder(ArrayList<TestBase> subFolder) {
        this.subFolder = subFolder;
    }

    public String getpId() {
        return pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }
}

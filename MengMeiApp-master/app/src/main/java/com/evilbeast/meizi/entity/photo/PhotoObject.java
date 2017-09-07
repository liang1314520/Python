package com.evilbeast.meizi.entity.photo;

import com.evilbeast.meizi.utils.RealmAutoIncrement;

import java.io.Serializable;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Author: sumary
 */
public class PhotoObject extends RealmObject implements Serializable {


    @PrimaryKey
    private int id = RealmAutoIncrement.getInstance(this.getClass()).getNextIdFromModel();

    private String imageUrl;
    private int groupId;
    private String module;
    private int position;

    public int getId() {
        return id;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }


    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public boolean isEmpty() {
        if (this.groupId <= 0) {
            return true;
        }
        return false;
    }
}

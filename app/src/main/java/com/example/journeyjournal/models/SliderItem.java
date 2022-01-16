package com.example.journeyjournal.models;

import android.net.Uri;

import java.io.Serializable;

public class SliderItem implements Serializable {
    private transient Uri image;
    private transient String imageName;
    private transient String FromDatabase;


    public SliderItem(Uri image, String imageName) {
        this.image = image;
        this.imageName = imageName;
    }

    public SliderItem(String fromDatabase,String imageName) {
        FromDatabase = fromDatabase;
        this.imageName=imageName;
    }

    public String getFromDatabase() {
        return FromDatabase;
    }

    public void setFromDatabase(String fromDatabase) {
        FromDatabase = fromDatabase;
    }

    public Uri getImage() {
        return image;
    }

    public void setImage(Uri image) {
        this.image = image;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
}

package com.example.journeyjournal.models;

import android.net.Uri;

public class SliderItem {
    private Uri image;
    private String imageName;

    public SliderItem(Uri image, String imageName) {
        this.image = image;
        this.imageName = imageName;
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

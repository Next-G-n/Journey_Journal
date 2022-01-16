package com.example.journeyjournal.models;

public class PastJourney {
    String Title;
    String Date;
    String Description;
    String Location;
    String UserId;
    String EntryId;
    String Image0;

    public PastJourney() {
    }

    public PastJourney(String title, String date, String description, String location, String userId, String entryId, String image0) {
        Title = title;
        Date = date;
        Description = description;
        Location = location;
        UserId = userId;
        EntryId = entryId;
        Image0 = image0;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public String getEntryId() {
        return EntryId;
    }

    public void setEntryId(String postId) {
        EntryId = postId;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        Location = location;
    }

    public String getImage0() {
        return Image0;
    }

    public void setImage0(String image0) {
        Image0 = image0;
    }
}

package com.referendum.uoctubre.model;

public class Hashtag {
    private String hashtag;
    private boolean userAdded;

    public Hashtag(String hashtag, boolean userAdded) {
        this.hashtag = hashtag;
        this.userAdded = userAdded;
    }

    public String getHashtag() {
        return hashtag;
    }

    public void setHashtag(String hashtag) {
        this.hashtag = hashtag;
    }

    public boolean isUserAdded() {
        return userAdded;
    }

    public void setUserAdded(boolean userAdded) {
        this.userAdded = userAdded;
    }
}

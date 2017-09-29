package com.referendum.uoctubre.model;

public class Results {
    private float yes;
    private float no;
    private float blank;
    private float invalid;
    private float participation;
    private float counted;
    private String message;

    public float getYes() {
        return yes;
    }

    public void setYes(float yes) {
        this.yes = yes;
    }

    public float getNo() {
        return no;
    }

    public void setNo(float no) {
        this.no = no;
    }

    public float getBlank() {
        return blank;
    }

    public void setBlank(float blank) {
        this.blank = blank;
    }

    public float getInvalid() {
        return invalid;
    }

    public void setInvalid(float invalid) {
        this.invalid = invalid;
    }

    public float getParticipation() {
        return participation;
    }

    public void setParticipation(float participation) {
        this.participation = participation;
    }

    public float getCounted() {
        return counted;
    }

    public void setCounted(float counted) {
        this.counted = counted;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

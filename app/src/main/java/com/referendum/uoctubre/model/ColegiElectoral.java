package com.referendum.uoctubre.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ColegiElectoral implements Serializable {

    @SerializedName("municipi")
    private String municipi;
    @SerializedName("local")
    private String local;
    @SerializedName("adresa")
    private String adresa;
    @SerializedName("cp")
    private int cp;
    @SerializedName("lat")
    private double lat;
    @SerializedName("lon")
    private double lon;
    @SerializedName("meses")
    private int meses;

    public ColegiElectoral(String municipi, String local, String adresa, int cp, double lat, double lon, int meses) {
        this.municipi = municipi;
        this.local = local;
        this.adresa = adresa;
        this.cp = cp;
        this.lat = lat;
        this.lon = lon;
        this.meses = meses;
    }

    public ColegiElectoral() {

    }

    public String getMunicipi() {
        return municipi;
    }

    public void setMunicipi(String municipi) {
        this.municipi = municipi;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public String getAdresa() {
        return adresa;
    }

    public void setAdresa(String adresa) {
        this.adresa = adresa;
    }

    public int getCp() {
        return cp;
    }

    public void setCp(int cp) {
        this.cp = cp;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public int getMeses() {
        return meses;
    }

    public void setMeses(int meses) {
        this.meses = meses;
    }
}

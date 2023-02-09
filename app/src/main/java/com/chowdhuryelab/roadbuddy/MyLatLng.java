package com.chowdhuryelab.roadbuddy;

public class MyLatLng {
    private double latitude;
    private double longitude;
    private String type;

    public MyLatLng(){
    }

    public MyLatLng(double latitude, double longitude, String type) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.type = type;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

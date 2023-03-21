package com.chowdhuryelab.roadbuddy.Interface;

import com.chowdhuryelab.roadbuddy.Models.MyLatLng;

import java.util.List;

public interface IOnLoadLocationListener {
    void onLoadLocationSuccess(List<MyLatLng>latLngs);
    void onLoadLocationFailed(String message);

}

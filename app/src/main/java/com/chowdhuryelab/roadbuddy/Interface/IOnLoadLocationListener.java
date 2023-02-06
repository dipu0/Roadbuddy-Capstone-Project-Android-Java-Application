package com.chowdhuryelab.roadbuddy.Interface;

import com.chowdhuryelab.roadbuddy.MyLatLng;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public interface IOnLoadLocationListener {
    void onLoadLocationSuccess(List<MyLatLng>latLngs);
    void onLoadLocationFailed(String message);

}

package com.example.harald.runwithme2;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

/**
 * Created by harald on 02.06.17.
 */

public class GPSPosition implements Serializable {
    private Double latitude;
    private Double longitude;

    public GPSPosition(LatLng position) {
        this.longitude = position.longitude;
        this.latitude = position.latitude;
    }

    public LatLng getValue() { return new LatLng(latitude, longitude);}
    public Double getLongitude() { return this.longitude; }
    public Double getLatitude() { return this.latitude; }
}

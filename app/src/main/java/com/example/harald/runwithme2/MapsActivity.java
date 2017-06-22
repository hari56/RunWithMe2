package com.example.harald.runwithme2;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener,
        GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener, GoogleMap.OnMapClickListener {

    private Model model = null;
    private GoogleMap googleMap;
    private LocationManager locationManager;
    private LatLng fromPosition = null;
    private LatLng toPosition = null;

    private static final LatLng LINZ = new LatLng(48.306072,14.286293);
    private static final LatLng WELS = new LatLng(48.156647,14.024618);


    //private MarkerOptions currentMarker = null;

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.i("GoogleMapActivity", "onMarkerClick");
        return false;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        this.fromPosition = marker.getPosition();
        Log.d(getClass().getSimpleName(), "Drag start at: " + this.fromPosition);
    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        this.toPosition = marker.getPosition();
        Toast.makeText(
                getApplicationContext(),
                "Marker " + marker.getTitle() + " dragged from " + fromPosition
                        + " to " + toPosition, Toast.LENGTH_LONG).show();
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try
        {
            super.onCreate(savedInstanceState);

            Bundle b = this.getIntent().getExtras();
            if (b != null) {
                this.model = (Model) b.getSerializable("model");

            }

            setContentView(R.layout.activity_maps);
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

            //gets the location of the device
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            }//end of if
        }
        catch(Exception ex)
        {
            this.showMessage(ex.toString());
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        try
        {
            this.googleMap = googleMap;
            this.googleMap.setOnMarkerClickListener(this);
            this.googleMap.setOnMarkerDragListener(this);
            this.googleMap.setOnMapClickListener(this);
            this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LINZ, 13));

            /*
            this.model.addItem(new GPSPosition(LINZ));
            this.model.addItem(new GPSPosition(WELS));

            Double distance = this.model.distance(new GPSPosition(LINZ), new GPSPosition(WELS));
            long time = System.currentTimeMillis();
            Double speed = this.model.speed(distance, time, time + 1800000);
            this.showMessage(String.valueOf(speed));
            this.updateMap();
            */
        }
        catch(Exception ex)
        {
            this.showMessage(ex.toString());
        }
    }





    @Override
    public void onLocationChanged(Location location) {

       try {
           LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
           if(this.model.getStartPos() == null)
           {
               this.model.setStartPos(new GPSPosition(currentLatLng));
               Toast.makeText(getApplicationContext(), "Startpunkt festgelegt. EndPunkt?", Toast.LENGTH_LONG).show();
               this.updateMap();

               this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 13));
               this.model.setCurrentAction(Model.ACTION.SELECT_ENDPOINT);
           }
        /*
        else if(currentMarker == null)
        {
            currentMarker = new MarkerOptions().position(currentLatLng)
                    .title("Hier").snippet("Standort")
                    .draggable(true);
            this.googleMap.addMarker(currentMarker);
        }
        */
           else
           {
               //currentMarker.position(currentLatLng);
               if(this.model.getCurrentAction() == Model.ACTION.ACTIVE_RUN)
                   this.model.addItem(new GPSPosition(currentLatLng));
           }
       }
       catch(Exception ex)
       {
           this.showMessage(ex.toString());
       }
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Latitude","disable");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Latitude","enable");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Latitude","status");
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if(this.model.getCurrentAction() == Model.ACTION.SELECT_ENDPOINT)
        {
            this.model.setEndPos(new GPSPosition(latLng));
            this.updateMap();

            Toast.makeText(getApplicationContext(), "Zielpunkt festgelegt", Toast.LENGTH_LONG).show();
            this.model.setCurrentAction(Model.ACTION.ACTIVE_RUN);

        }
    }
    public void updateMap() {
        this.googleMap.clear();

        GPSPosition startPos = this.model.getStartPos();
        if(startPos != null)
        {
            this.googleMap.addMarker(new MarkerOptions()
                    .position(startPos.getValue())
                    .title("Startpunkt")
                    .draggable(false)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        }

        GPSPosition endPos = this.model.getEndPos();
        if(endPos != null)
        {
            this.googleMap.addMarker(new MarkerOptions()
                    .position(endPos.getValue())
                    .title("Zielpunkt")
                    .draggable(false)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        }

        ArrayList<LatLng> values = new ArrayList<>();
        for(PathItem item : this.model.getItems())
        {
            values.add(item.getPosition().getValue());
        }
        this.googleMap.addPolyline((new PolylineOptions())
                .addAll(values).width(5).color(Color.BLUE)
                .geodesic(true));
    }

    private void showMessage(String message)
    {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }
}

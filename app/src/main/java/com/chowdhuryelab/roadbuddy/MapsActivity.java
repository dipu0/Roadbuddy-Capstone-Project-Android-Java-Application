package com.chowdhuryelab.roadbuddy;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.chowdhuryelab.roadbuddy.Interface.IOnLoadLocationListener;
import com.chowdhuryelab.roadbuddy.Models.MyLatLng;
import com.chowdhuryelab.roadbuddy.databinding.ActivityMapsBinding;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MapsActivity extends DrawerBaseActivity implements OnMapReadyCallback, GeoQueryEventListener, IOnLoadLocationListener {

    ActivityMapsBinding activityMapsBinding;
    private GoogleMap mMap;
    static MapsActivity instance;
    LocationRequest locationRequest;
    FusedLocationProviderClient fusedLocationProviderClient;
    Marker currentUser, oldUser;
    DatabaseReference myLocationRef;
    GeoFire geoFire;
    List<LatLng> dangerousArea = new ArrayList<>();
    List<MyLatLng> MapDangerousArea = new ArrayList<>();
    IOnLoadLocationListener listener;

    String lat = "", lon = "";
    private MediaPlayer alertSound;
    LocationManager locationManager = null;
    Location location;

    SharedPreferences sharedpreferences;

    FirebaseAuth auth ;
    FirebaseDatabase database ;
    FirebaseStorage storage;
    FirebaseUser account;
    DatabaseReference reff;
    String uid;

    public static MapsActivity getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMapsBinding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(activityMapsBinding.getRoot());
        allocateActivityTitle("RoadBuddy");
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        instance = this;

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();
        uid = auth.getUid();


        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {

                        //Now, after dangerous Area is have data, We will call map display
                        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                                .findFragmentById(R.id.map);
                        mapFragment.getMapAsync(MapsActivity.this);

                        updateLocation();
                        initArea();
                        settingGeoFire();

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(getApplicationContext(), "You must Grant Permission to make it work!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();


    }

    private void initArea() {
        listener = this;
        //load from firebase
        FirebaseDatabase.getInstance()
                .getReference("DangerousArea")
                .child("MyCity")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<MyLatLng> latLngList = new ArrayList<>();
                        for(DataSnapshot locationSnapShot: dataSnapshot.getChildren())
                        {
                            MyLatLng latLng = locationSnapShot.getValue(MyLatLng.class);
                            latLngList.add(latLng);
                        }
                        listener.onLoadLocationSuccess(latLngList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        listener.onLoadLocationFailed(databaseError.getMessage());
                    }
                });
       }


    private void settingGeoFire() {
        myLocationRef = FirebaseDatabase.getInstance().getReference("MyLocation");
        geoFire = new GeoFire(myLocationRef);
    }

    private void updateLocation() {
        buildLocationRequest();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, getPendingIntent());
    }


    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(this, MyLocationService.class);
        intent.setAction(MyLocationService.ACTION_PROCESS_UPDATE);
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }


//    private void storelocation(){
//
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
//            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        }
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//        String latlongString = location.getLatitude() + "," + location.getLatitude();
//        sharedpreferences = getSharedPreferences("MYSETTINGS", Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedpreferences.edit();
//        editor.putString("LocationKey", latlongString);
//        editor.apply();
//    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(500);
        locationRequest.setFastestInterval(200);
        locationRequest.setSmallestDisplacement(1f);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        SharedPreferences sharedpreferences  = getSharedPreferences("MYSETTINGS", Context.MODE_PRIVATE);
        String radius = sharedpreferences.getString("radius", "0.05");
        double rad = Double.parseDouble(radius+"f");

        for (MyLatLng mylatLng : MapDangerousArea){

            LatLng convert = new LatLng(mylatLng.getLatitude(), mylatLng.getLongitude());
            String type = new String(mylatLng.getType());

            if(type.equals("Pothole")){
                try {
                    mMap.addMarker(new MarkerOptions()
                            .position(convert)
                            .title("Pothole")
                            .snippet(getAddress(convert))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.pothole)));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if(type.equals("SpeedBreaker")){
                try {
                    mMap.addMarker(new MarkerOptions()
                            .position(convert)
                            .title("SpeedBreaker")
                            .snippet(getAddress(convert))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.speedbreaker)));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            mMap.addCircle(new CircleOptions().center(convert)
                    .radius(10)
                    .strokeColor(Color.RED)
                    .fillColor(0x220000FF)
                    .strokeWidth(2.0f)
            );

            GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(convert.latitude, convert.longitude), rad); //50m
            geoQuery.addGeoQueryEventListener(MapsActivity.this);

        }

    }

    private String getAddress(LatLng location) throws IOException {

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        String address="";

        List<Address> addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1);
        Address obj = addresses.get(0);
        String  add = obj.getAddressLine(0);
//optional
          /*  add = add + "\n" + obj.getCountryName();
            add = add + "\n" + obj.getCountryCode();
            add = add + "\n" + obj.getAdminArea();
            add = add + "\n" + obj.getPostalCode();
            add = add + "\n" + obj.getSubAdminArea();
            add = add + "\n" + obj.getLocality();
            add = add + "\n" + obj.getSubThoroughfare();*/

        Log.e("Location", "Address" + add);
        address=add;
        return address;
    }

    public void updateLocation(String latitude, String longitude) {

        MapsActivity.this.runOnUiThread(new Runnable() {

            @Override
            public void run() {

                String s = latitude + " , " + longitude;
                //Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();
                if(mMap != null){

                    geoFire.setLocation(uid, new GeoLocation(Double.parseDouble(latitude), Double.parseDouble(longitude)),
                            new GeoFire.CompletionListener() {
                                @Override
                                public void onComplete(String key, DatabaseError error) {

                                    if(currentUser != null) currentUser.remove();
                                    currentUser = mMap.addMarker(new MarkerOptions()
                                            .position(new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude)))
                                            .title("You"));

                                    mMap.animateCamera(CameraUpdateFactory
                                            .newLatLngZoom(currentUser.getPosition(), 16f));

                                    mMap.addCircle(new CircleOptions().center(currentUser.getPosition())
                                            .radius(2)
                                            .strokeColor(Color.BLUE)
                                            .fillColor(0x95135748)
                                            .strokeWidth(2.0f));
                                }
                            }
                    );
                }
                else{
                    Toast.makeText(getApplicationContext(), "mMap is NULL", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }


    @Override
    public void onKeyEntered(String key, GeoLocation location) {
        Toast.makeText(getApplicationContext(),"Entering.." , Toast.LENGTH_SHORT).show();
                // Send Notifications
        SendNotification("Roadbuddy", String.format("Entered the dangerous area "));

        // Load the state of the switch from SharedPreferences and update the UI
        SharedPreferences sharedPreferences  = getSharedPreferences("MYSETTINGS", Context.MODE_PRIVATE);
        boolean pop_up_dia = sharedPreferences.getBoolean("pop_up_dia", false);
        boolean alert = sharedPreferences.getBoolean("alert_sound", false);

        // Initialize the alert sound
        alertSound = MediaPlayer.create(this, R.raw.alert_sound);

        if(pop_up_dia){
            // Show the red alert
            showRedDialog(this, "RoadBuddy", "Entered the dangerous area.");

        }
        if(alert){
            // Play the alert sound
            alertSound.start();
            // Stop the alert sound after 3 seconds
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    alertSound.stop();
                    alertSound.reset();
                }
            }, 3000);

        }

    }

    @Override
    public void onKeyExited(String key) {
//        Toast.makeText(getApplicationContext(), key + "Exited" , Toast.LENGTH_SHORT).show();
//        SendNotification("Roadbuddy", String.format("%s leave the dangerous area",key));
    }

    @Override
    public void onKeyMoved(String key, GeoLocation location) {
//        Toast.makeText(getApplicationContext(), key + "Moving inside" , Toast.LENGTH_SHORT).show();
//        SendNotification("Roadbuddy", String.format("%s move within the dangerous area",key));
    }

    @Override
    public void onGeoQueryReady() {

    }

    @Override
    public void onGeoQueryError(DatabaseError error) {
        Toast.makeText(getApplicationContext(), error.getMessage() , Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLoadLocationSuccess(List<MyLatLng> latLngs) {

        MapDangerousArea=(latLngs);

        //Now, after dangerous Area is have data, We will call map display
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivity.this);
    }

    @Override
    public void onLoadLocationFailed(String message) {
        Toast.makeText(MapsActivity.this,""+message,Toast.LENGTH_SHORT).show();
    }
    //Notification
    private void SendNotification(String title, String content) {

        String NOTIFICATION_CHANNEL_ID ="roadbuddy_multiple_location";
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,"My Notification", NotificationManager.IMPORTANCE_DEFAULT);
            //config
            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0,100,500,1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);

        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.logo1)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.logo1));
        Notification notification = builder.build();
        notificationManager.notify(new Random().nextInt(), notification);
    }

    public void showRedDialog(Context context, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(true);
        builder.setIcon(android.R.drawable.ic_dialog_alert);

        // Set the positive button text and action
//        builder.setPositiveButton("", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int id) {
//
//                alertSound.stop();
//            }
//        });

        // Create the AlertDialog object and set its properties
        AlertDialog dialog = builder.create();
        dialog.show();

        // Set the dialog background color to red
        int alertTitleId = context.getResources().getIdentifier("alertTitle", "id", "android");
        TextView alertTitle = dialog.findViewById(alertTitleId);
        alertTitle.setTextColor(Color.RED);

        int titleDividerId = context.getResources().getIdentifier("titleDivider", "id", "android");
        View titleDivider = dialog.findViewById(titleDividerId);
        if (titleDivider != null) {
            titleDivider.setBackgroundColor(Color.RED);
        }

    }


}
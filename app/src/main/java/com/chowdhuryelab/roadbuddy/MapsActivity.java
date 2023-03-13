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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.chowdhuryelab.roadbuddy.Interface.IOnLoadLocationListener;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MapsActivity extends DrawerBaseActivity implements OnMapReadyCallback, GeoQueryEventListener, IOnLoadLocationListener {

    ActivityMapsBinding activityMapsBinding;
    private GoogleMap mMap;

    static MapsActivity instance;
    LocationRequest locationRequest;
    FusedLocationProviderClient fusedLocationProviderClient;
    Marker currentUser;
    DatabaseReference myLocationRef;
    GeoFire geoFire;
    List<LatLng> dangerousArea = new ArrayList<>();
    List<MyLatLng> updatedangerousArea = new ArrayList<>();
    List<MyLatLng> MapDangerousArea = new ArrayList<>();
    IOnLoadLocationListener listener;

    String lat = "", lon = "";

    private MediaPlayer alertSound;

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
//        updatedangerousArea.add(new MyLatLng(23.70039,90.43729,"SpeedBreaker"));
//        updatedangerousArea.add(new MyLatLng(23.70044,90.43722,"Pothole"));
//
//       // we are submitting above location to our firebase data
//        FirebaseDatabase.getInstance()
//                .getReference("DangerousArea")
//                .child("MyCity")
//                .setValue(updatedangerousArea)
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        Toast.makeText(MapsActivity.this,"Updated!",Toast.LENGTH_SHORT).show();
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(MapsActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
//                    }
//                });
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

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(100000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setSmallestDisplacement(2f);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);

        for (MyLatLng mylatLng : MapDangerousArea){
            LatLng convert = new LatLng(mylatLng.getLatitude(), mylatLng.getLongitude());
            String type = new String(mylatLng.getType());

            if(type.equals("Pothole")){
                mMap.addMarker(new MarkerOptions()
                        .position(convert)
                        .title("Pothole")
                        .snippet("Snippet")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.pothole)));
            }
            if(type.equals("SpeedBreaker")){
                mMap.addMarker(new MarkerOptions()
                        .position(convert)
                        .title("SpeedBreaker")
                        .snippet("Snippet")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.speedbreaker)));
            }

            mMap.addCircle(new CircleOptions().center(convert)
                    .radius(1)
                    .strokeColor(Color.RED)
                    .fillColor(0x220000FF)
                    .strokeWidth(2.0f)
            );
//            Toast.makeText(getApplicationContext(),"" + latlng.latitude , Toast.LENGTH_SHORT).show();
            GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(convert.latitude, convert.longitude), 1f);
            geoQuery.addGeoQueryEventListener(MapsActivity.this);
        }


        // Add a marker in Dholaipar and move the camera
//        LatLng dholaipar = new LatLng(23.70043, 90.43728);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCameraf(CameraUpdateFactory.newLatLng(sydney));
    }

    public void updateLocation(String latitude, String longitude) {
        MapsActivity.this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
//                try {
//                    Toast.makeText(getApplicationContext(),"Size: " + dangerousArea.size() , Toast.LENGTH_SHORT).show();
//                    for(LatLng latlng : dangerousArea){
////                        Toast.makeText(getApplicationContext(),"L:" + latlng.latitude , Toast.LENGTH_SHORT).show();
//                    }
//                }
//                catch(Exception ex){
//                    Toast.makeText(getApplicationContext(),"Databse Empty" + String.valueOf(ex) , Toast.LENGTH_SHORT).show();
//                }
                String s = latitude + " , " + longitude;
                //Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();
                if(mMap != null){

                    geoFire.setLocation("Keyy", new GeoLocation(Double.parseDouble(latitude), Double.parseDouble(longitude)),
                            new GeoFire.CompletionListener() {
                                @Override
                                public void onComplete(String key, DatabaseError error) {
                                    if(currentUser != null) currentUser.remove();
                                    currentUser = mMap.addMarker(new MarkerOptions()
                                            .position(new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude)))
                                            .title("You"));

                                    mMap.animateCamera(CameraUpdateFactory
                                            .newLatLngZoom(currentUser.getPosition(), 100.0f));
                                }
                            }
                    );
                }
                else{
                    Toast.makeText(getApplicationContext(), "mMap is NULL", Toast.LENGTH_SHORT).show();
                }
//                textView.setText(s);
//                Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
//                LocationHelper object = new LocationHelper(latitude, longitude);
//                mDatabase.push().setValue(object);
            }
        });
    }

    @Override
    public void onKeyEntered(String key, GeoLocation location) {
        Toast.makeText(getApplicationContext(),"Entering.." , Toast.LENGTH_SHORT).show();
                // Send Notifications
        SendNotification("Roadbuddy", String.format("%s entered the dangerous area ",key));

        // Load the state of the switch from SharedPreferences and update the UI
        SharedPreferences sharedPreferences  = getSharedPreferences("MYSETTINGS", Context.MODE_PRIVATE);
        boolean pop_up_dia = sharedPreferences.getBoolean("pop_up_dia", false);
        boolean alert = sharedPreferences.getBoolean("alert_sound", false);

        // Initialize the alert sound
        alertSound = MediaPlayer.create(this, R.raw.alert_sound);

        if(pop_up_dia){
            // Show the red alert
            showRedDialog(this, "RoadBuddyr", "entered the dangerous area.");

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
        MapDangerousArea = new ArrayList<>();
        MapDangerousArea=(latLngs);
//        for(MyLatLng myLatLng: latLngs)
//        {
//            LatLng convert = new LatLng(myLatLng.getLatitude(), myLatLng.getLongitude());
//            dangerousArea.add(convert);
//        }
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
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher));
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
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                alertSound.stop();
            }
        });

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
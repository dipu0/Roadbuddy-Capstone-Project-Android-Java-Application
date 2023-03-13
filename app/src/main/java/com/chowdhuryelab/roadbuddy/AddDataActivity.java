package com.chowdhuryelab.roadbuddy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.chowdhuryelab.roadbuddy.databinding.ActivityAddDataBinding;
import com.chowdhuryelab.roadbuddy.databinding.ActivityMainBinding;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddDataActivity extends DrawerBaseActivity {
    ActivityAddDataBinding activityAddDataBinding;
    ImageButton pothole,speedbreaker;
    LocationManager locationManager = null;
    Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityAddDataBinding = ActivityAddDataBinding.inflate(getLayoutInflater());
        setContentView(activityAddDataBinding.getRoot());
        allocateActivityTitle("Add Missing Data");

        pothole = findViewById(R.id.pothole);
        speedbreaker = findViewById(R.id.speedbreaker);

        pothole.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.pothole) {
                    updatelocation();
                    MyLatLng data = new MyLatLng(Double.parseDouble(String.valueOf(location.getLatitude())) , Double.parseDouble(String.valueOf(location.getLongitude())),"Pothole");
                    // Do add pothole data for pothole click
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference("DangerousArea").child("MyCity");
                    myRef.push().setValue(data);


                }
            }
        });

        speedbreaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.speedbreaker) {
                    updatelocation();
                    MyLatLng data = new MyLatLng(Double.parseDouble(String.valueOf(location.getLatitude())) , Double.parseDouble(String.valueOf(location.getLongitude())),"SpeedBreaker");
                    // Do add pothole data for pothole click
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference("DangerousArea").child("MyCity");
                    myRef.push().setValue(data);


                }
            }
        });


    }

    private void updatelocation(){

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }
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
        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }
}
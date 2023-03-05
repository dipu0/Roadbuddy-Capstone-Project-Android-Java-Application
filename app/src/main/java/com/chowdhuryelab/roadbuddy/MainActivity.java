package com.chowdhuryelab.roadbuddy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.chowdhuryelab.roadbuddy.databinding.ActivityMainBinding;

public class MainActivity extends DrawerBaseActivity {

ActivityMainBinding activityMainBinding;
    Button map;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        allocateActivityTitle("MainActivity");
        setContentView(activityMainBinding.getRoot());
        map = findViewById(R.id.button);

        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MapsActivity.class));
            }
        });
    }
}
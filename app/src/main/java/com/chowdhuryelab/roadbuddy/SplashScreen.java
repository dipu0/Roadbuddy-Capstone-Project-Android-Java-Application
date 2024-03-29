package com.chowdhuryelab.roadbuddy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.chowdhuryelab.roadbuddy.LoginActivities.SigninActivity;
import com.chowdhuryelab.roadbuddy.databinding.ActivitySplashScreenBinding;
import com.google.firebase.auth.FirebaseAuth;

public class SplashScreen extends AppCompatActivity {

    ActivitySplashScreenBinding binding ;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if(auth.getCurrentUser() != null) {
                    Intent intent = new Intent(SplashScreen.this, MapsActivity.class);
                    startActivity(intent);
                    finish();
                }
                else {
                    Intent intent = new Intent(SplashScreen.this, SigninActivity.class);
                    startActivity(intent);
                    finish();
                }

            }
        },4000);

    }
}
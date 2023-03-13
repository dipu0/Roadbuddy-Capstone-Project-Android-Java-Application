package com.chowdhuryelab.roadbuddy;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.chowdhuryelab.roadbuddy.databinding.ActivityAddDataBinding;
import com.chowdhuryelab.roadbuddy.databinding.ActivityMainBinding;

public class AddDataActivity extends DrawerBaseActivity {
    ActivityAddDataBinding activityAddDataBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityAddDataBinding = ActivityAddDataBinding.inflate(getLayoutInflater());
        setContentView(activityAddDataBinding.getRoot());
        allocateActivityTitle("Add Missing Data");
    }
}
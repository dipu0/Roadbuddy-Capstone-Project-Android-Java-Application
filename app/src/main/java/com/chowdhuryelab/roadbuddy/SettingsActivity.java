package com.chowdhuryelab.roadbuddy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.CompoundButton;

import com.chowdhuryelab.roadbuddy.databinding.ActivityMapsBinding;
import com.chowdhuryelab.roadbuddy.databinding.ActivitySettingsBinding;

public class SettingsActivity extends DrawerBaseActivity {

    ActivitySettingsBinding activitySettingsBinding;
    private androidx.appcompat.widget.SwitchCompat pop_up, alert_sound;
    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activitySettingsBinding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(activitySettingsBinding.getRoot());
        allocateActivityTitle("Settings");

        // Find the SwitchCompat view and set its listener
        pop_up = findViewById(R.id.switch_pop_up);
        alert_sound = findViewById(R.id.switch_alert_sound);

        pop_up.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Save the state of the switch to SharedPreferences
                sharedpreferences = getSharedPreferences("MYSETTINGS", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean("pop_up_dia", isChecked);
                editor.apply();
            }
        });

        alert_sound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Save the state of the switch to SharedPreferences
                sharedpreferences = getSharedPreferences("MYSETTINGS", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean("alert_sound", isChecked);
                editor.apply();
            }
        });


        // Load the state of the switch from SharedPreferences and update the UI



        SharedPreferences sharedPreferences  = getSharedPreferences("MYSETTINGS", Context.MODE_PRIVATE);
        boolean pop_up_dia = sharedPreferences.getBoolean("pop_up_dia", false);
        pop_up.setChecked(pop_up_dia);

        boolean alertSound = sharedPreferences.getBoolean("alert_sound", false);
        alert_sound.setChecked(alertSound);
    }
}
package com.chowdhuryelab.roadbuddy;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.chowdhuryelab.roadbuddy.databinding.ActivityMapsBinding;
import com.chowdhuryelab.roadbuddy.databinding.ActivityTrafficSignBinding;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;

public class TrafficSignActivity extends DrawerBaseActivity {

    ActivityTrafficSignBinding activityTrafficSignBinding;
    private PDFView pdfView;
    private String pdfFileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityTrafficSignBinding = ActivityTrafficSignBinding.inflate(getLayoutInflater());
        setContentView(activityTrafficSignBinding.getRoot());
        allocateActivityTitle("Bangladesh Traffic Sign");

        pdfView = findViewById(R.id.pdfView);
        pdfFileName = "traffic_signs.pdf";

        displayFromAsset(pdfFileName);
    }

    private void displayFromAsset(String assetFileName) {
        pdfView.fromAsset(assetFileName)
                .onLoad(new OnLoadCompleteListener() {
                    @Override
                    public void loadComplete(int nbPages) {
                        setTitle(pdfFileName);
                    }
                })
                .load();
    }

}
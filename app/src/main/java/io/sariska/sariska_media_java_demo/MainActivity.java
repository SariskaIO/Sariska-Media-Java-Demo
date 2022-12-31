package io.sariska.sariska_media_java_demo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import io.sariska.sdk.SariskaMediaTransport;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SariskaMediaTransport.initializeSdk(getApplication());
    }
}
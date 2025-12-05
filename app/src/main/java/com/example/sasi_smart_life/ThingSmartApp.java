package com.example.sasi_smart_life;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.thingclips.smart.home.sdk.ThingHomeSdk;

public class ThingSmartApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ThingHomeSdk.init(this,"wqjdh8pwama4yg5qnh5j","3mpm3sqjseh9hkk8aatf9nagv4swc7ge");
    }
}

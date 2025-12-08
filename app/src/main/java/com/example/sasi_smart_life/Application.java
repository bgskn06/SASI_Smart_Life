package com.example.sasi_smart_life;

import android.util.Log;

import com.thingclips.smart.home.sdk.ThingHomeSdk;

public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ThingHomeSdk.init(this,"wqjdh8pwama4yg5qnh5j","3mpm3sqjseh9hkk8aatf9nagv4swc7ge");
        Log.d("APP_SASI", "Tuya SDK Initialized");
    }
}

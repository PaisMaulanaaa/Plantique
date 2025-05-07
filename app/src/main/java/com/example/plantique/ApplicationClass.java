package com.example.plantique;

import android.app.Application;
import android.util.Log;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

public class ApplicationClass extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Log.e("APP_CRASH", "Uncaught exception", throwable);
        });
    }
}
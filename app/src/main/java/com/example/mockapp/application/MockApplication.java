package com.example.mockapp.application;

import android.app.Application;
import android.content.Context;
import android.support.v7.app.AppCompatDelegate;

import com.example.mockapp.slang.VoiceInterface;

import in.slanglabs.platform.application.SlangLocaleException;

public class MockApplication extends Application {
    //private static MockApplication instance;
    private static Context appContext;

    /*public static MockApplication getInstance() {
        return instance;
    }*/

    public static Context getAppContext() {
        return appContext;
    }

    public void setAppContext(Context mAppContext) {
        appContext = mAppContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //instance = this;
        try {
            VoiceInterface.init(this,"1a3959828ef54c3b8987df74a67bbb7a",
                    "e120ae40f0334ed795ae1887bb74146a");
        } catch (SlangLocaleException e) {
            e.printStackTrace();
        }

        this.setAppContext(getApplicationContext());
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

}

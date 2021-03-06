package in.slanglabs.shrinemock.application;

import android.app.Application;
import android.content.Context;
import android.support.v7.app.AppCompatDelegate;

import in.slanglabs.shrinemock.slang.VoiceInterface;

public class MockApplication extends Application {
    private static Context appContext;

    public static Context getAppContext() {
        return appContext;
    }

    public void setAppContext(Context mAppContext) {
        appContext = mAppContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
            VoiceInterface.init(this,"1a3959828ef54c3b8987df74a67bbb7a",
                    "e120ae40f0334ed795ae1887bb74146a");

        this.setAppContext(getApplicationContext());
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

}

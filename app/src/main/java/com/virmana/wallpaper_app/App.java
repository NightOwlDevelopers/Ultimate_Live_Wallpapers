package com.virmana.wallpaper_app;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.multidex.MultiDex;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.orhanobut.hawk.Hawk;

import timber.log.Timber;

/**
 * Created by hsn on 27/11/2017.
 */


public class App extends Application {
    private static App instance;
    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        MultiDex.install(this);
    }
    @Override
    public void onCreate()
    {
        MultiDex.install(this);
        super.onCreate();
        instance = this;
        Hawk.init(this).build();
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        if (BuildConfig.DEBUG)
        {
            Timber.plant(new Timber.DebugTree());
        }
       Timber.i("Creating our Application");
    }

    public static App getInstance ()
    {
        return instance;
    }

    public static boolean hasNetwork ()
    {
        return instance.checkIfHasNetwork();
    }

    public boolean checkIfHasNetwork()
    {
        ConnectivityManager cm = (ConnectivityManager) getSystemService( Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}

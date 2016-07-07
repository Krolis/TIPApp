package com.krolis.tipapp;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

/**
 * Created by Krolis on 2016-06-09.
 */
public class TIPApplication extends Application implements Application.ActivityLifecycleCallbacks {
    public static final String PREFS_NAME = "TIPprefs";
    public static final String CLOSE_ALL = "CLOSE_ALL";
    public static final String PREFS_LOGIN_KEY = "login";
    public static final String PREFS_PASSWORD_KEY = "password";

    private static boolean isVisible = false;


    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(this);
    }

    public static boolean isVisible() {
        return isVisible;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        isVisible = true;
    }

    @Override
    public void onActivityPaused(Activity activity) {
        isVisible = false;
    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}

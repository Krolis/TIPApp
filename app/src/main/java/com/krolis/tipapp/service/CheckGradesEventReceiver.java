package com.krolis.tipapp.service;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.krolis.tipapp.TIPApplication;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Krolis on 2016-06-29.
 */
public class CheckGradesEventReceiver extends WakefulBroadcastReceiver{

    public static final String ACTION_START_MONITORING_GRADES = "ACTION_START_MONITORING_GRADES";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(ACTION_START_MONITORING_GRADES.equals(intent.getAction())){
            Intent service = new Intent(context, CheckGradesService.class);
            service.setAction(CheckGradesService.CHECK_GRADES);
            startWakefulService(context, service);
        }else if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())){
            startMonitoring(context);
        }

    }

    public static void startMonitoring(Context applicationContext) {
        SharedPreferences sharedPreferences = applicationContext.getSharedPreferences(TIPApplication.PREFS_NAME, Context.MODE_PRIVATE);
        boolean x = sharedPreferences.getBoolean(applicationContext.getString(R.string.pref_sync_key),true);
        if(!x){
            stopMonitoring(applicationContext);
            return;
        }

        int period = Integer.parseInt(sharedPreferences.getString(applicationContext.getString(R.string.pref_sync_time_key),applicationContext.getResources().getString(R.string.pref_sync_time_default)));
        if(period!=0)
            period = period*60 * 60 * 1000;
        else
            period = 30 * 1000;
        AlarmManager alarmManager = (AlarmManager) applicationContext.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, getTriggered() + period, period, getStartingIntent(applicationContext));

    }

    public static void stopMonitoring(Context applicationContext){
        AlarmManager alarmManager = (AlarmManager) applicationContext.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(getStartingIntent(applicationContext));
        final NotificationManager manager = (NotificationManager) applicationContext.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancelAll();
    }

    private static long getTriggered(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        //calendar.add();
        return calendar.getTimeInMillis();
    }

    private static PendingIntent getStartingIntent(Context applicationContext) {
        Intent intent = new Intent(applicationContext, CheckGradesEventReceiver.class);
        intent.setAction(ACTION_START_MONITORING_GRADES);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(applicationContext,0,intent, PendingIntent.FLAG_CANCEL_CURRENT);
        return pendingIntent;
    }
}

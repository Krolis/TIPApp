package com.krolis.tipapp.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.krolis.tipapp.TIPApplication;
import com.krolis.tipapp.activity.LoginActivity;
import com.krolis.tipapp.adapter.SemesterAdapter;
import com.krolis.tipapp.dao.DBConnection;
import com.krolis.tipapp.dao.GradeMockProvider;
import com.krolis.tipapp.dao.GradeOnlineProvider;
import com.krolis.tipapp.dao.GradeProvider;
import com.krolis.tipapp.model.Grade;
import com.krolis.tipapp.util.NoActiveSessionOnServer;
import com.krolis.tipapp.util.NoInternetConnection;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Krolis on 2016-06-29.
 */
public class CheckGradesService extends IntentService {

    private static final int NOTIF_ID = 1;
    public static final String CHECK_GRADES = "CHECK_GRADES";

    public CheckGradesService() {
        super(CheckGradesService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        Log.d("XD", "OnHandleIntent " + intent.getAction());
        if(CHECK_GRADES.equals(action)){
            startNotification();
        }
        WakefulBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void startNotification() {
        List<String> semesters = null;
        List<Grade> newGrades = new LinkedList<>();
        boolean isUpdated = false;
        try{
            SharedPreferences preferences = getSharedPreferences(TIPApplication.PREFS_NAME,
                    MODE_PRIVATE);
            String login = preferences.getString(TIPApplication.PREFS_LOGIN_KEY, null);
            String password = preferences.getString(TIPApplication.PREFS_PASSWORD_KEY, null);

            GradeProvider gradeProvider = GradeProvider.getActiveProvider();
            if(gradeProvider==null){
                if(GradeMockProvider.MOCK_LOGIN.equals(login))
                    gradeProvider=GradeMockProvider.getInstance();
                else
                    gradeProvider= GradeOnlineProvider.getInstance();

                if(GradeProvider.LOGIN_OK != gradeProvider.login(login,password)){
                    CheckGradesEventReceiver.stopMonitoring(getApplicationContext());
                    return;
                }
            }

            semesters = gradeProvider.getSemesters();
            DBConnection db = new DBConnection(this,login);
            isUpdated = db.setupSemesterTables(semesters);

            for(String semester: semesters){
                List<Grade> dbGrades = db.getGrades(semester);
                List<Grade> downloadedGrades = gradeProvider.getGrades(semester);
                for(Grade grade: downloadedGrades){
                    if(!dbGrades.contains(grade)){
                        newGrades.add(grade);
                        db.insertGrade(semester, grade);
                    }
                }
            }
        }catch (NoInternetConnection e){
            //// TODO: 2016-07-02
            e.printStackTrace();
            return;
        }catch (NoActiveSessionOnServer e){
            CheckGradesEventReceiver.stopMonitoring(getApplicationContext());
            GradeProvider.logoutTask(getApplicationContext());
            return;
        }
        catch (IOException e){
            e.printStackTrace();
            return;
        }

        int size = newGrades.size();
        String message;

        if(isUpdated){
            message = "Baza danych zaaktualizowana!\n" +
                    "Poinformujemy Cię o nowych ocenach.";
        }else{
            if(size == 1)
                message = "Nowa ocena! \n" + newGrades.get(0).getSubject();
            else if(size>1)
                message = size + " nowych ocen!";
            else
                message = "Brak nowych ocen." + semesters.size();
            for(Grade g: newGrades){
                if (SemesterAdapter.getFinalGrade(g) >1 && SemesterAdapter.getFinalGrade(g)<3){
                    message = "TROLOLOLO uwaliłeś\n" + g.getSubject();
                }
            }
        }

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentTitle("TIP Notification")
                .setAutoCancel(true)
                .setTicker("TIP Notification")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentText(message)
                .setSmallIcon(R.drawable.ikonka);

        Intent resultIntent = new Intent(this, LoginActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(LoginActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent pendingIntent= stackBuilder.getPendingIntent(NOTIF_ID, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(pendingIntent);

        final NotificationManager manager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(NOTIF_ID, builder.build());

    }
}

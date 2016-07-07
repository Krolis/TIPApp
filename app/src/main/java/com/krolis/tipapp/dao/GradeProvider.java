package com.krolis.tipapp.dao;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.krolis.tipapp.model.Grade;
import com.krolis.tipapp.util.NoActiveSessionOnServer;
import com.krolis.tipapp.util.NoInternetConnection;
import com.krolis.tipapp.TIPApplication;
import com.krolis.tipapp.activity.LoginActivity;

import java.io.IOException;
import java.util.List;

/**
 * Created by Krolis on 2016-06-22.
 */
public abstract class GradeProvider {
    public static final int LOGIN_OK = 0;
    public static final int LOGIN_WRONG = 1;

    public static final int LOGOUT_OK = 0;
    public static final int LOGOUT_WRONG = 1;
    public static final int OFFLINE = 404;

    public abstract int login(String login, String pass) throws IOException, NoInternetConnection;

    public abstract List<Grade> getGrades(String semester) throws IOException, NoInternetConnection,NoActiveSessionOnServer;

    public abstract List<String> getSemesters() throws IOException, NoInternetConnection,NoActiveSessionOnServer;

    public abstract int logout() throws NoInternetConnection ;

    public static void logoutTask(Context context){
        if(GradeOnlineProvider.getInstance().isActiveOnlineSession()){
            GradeOnlineProvider.getLogoutThread(context).start();
            SharedPreferences.Editor preferencesEditor =
                    context.getSharedPreferences(TIPApplication.PREFS_NAME, Context.MODE_PRIVATE).edit();
            preferencesEditor.remove(TIPApplication.PREFS_LOGIN_KEY);
            preferencesEditor.remove(TIPApplication.PREFS_PASSWORD_KEY);
            //todo wywalic powiadomienia
        }else{
            GradeMockProvider.getInstance().logout();
            Intent intent = new Intent(context, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        }


    }

    public static boolean isActiveSession(){
        return GradeOnlineProvider.getInstance().isActiveOnlineSession() ||
                GradeMockProvider.getInstance().isActiveMockSession();
    }

    public static GradeProvider getActiveProvider(){
        if(GradeOnlineProvider.getInstance().isActiveOnlineSession())
            return GradeOnlineProvider.getInstance();
        else if(GradeMockProvider.getInstance().isActiveMockSession())
            return GradeMockProvider.getInstance();
        else
            return null;
    }

    public static boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException e){
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        return false;
    }
}

package com.krolis.tipapp.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.krolis.tipapp.service.CheckGradesEventReceiver;
import com.krolis.tipapp.dao.GradeMockProvider;
import com.krolis.tipapp.dao.GradeOnlineProvider;
import com.krolis.tipapp.dao.GradeProvider;
import com.krolis.tipapp.util.NoInternetConnection;
import com.krolis.tipapp.TIPApplication;

/**
 * Created by Krolis on 2016-06-06.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText loginET;
    EditText passET;
    Button button;
    ProgressDialog loadingDialog;
    GradeProvider provider;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        loginET  = (EditText) findViewById(R.id.loginET);
        passET = (EditText) findViewById(R.id.passET);
        button = (Button) findViewById(R.id.sign_in_btn);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(!loginET.getText().toString().equals(GradeMockProvider.MOCK_LOGIN)){
                    provider = GradeOnlineProvider.getInstance();
                }else{
                    provider = GradeMockProvider.getInstance();
                }
                    boolean isOk = true;
                    loginET.setError(null);
                    passET.setError(null);
                    if(loginET.getText().toString().trim().isEmpty()){
                        loginET.setError("nie posiadasz tajnego loginu?");
                        isOk= false;
                    }
                    if(passET.getText().toString().trim().isEmpty()){
                        passET.setError("nie posiadasz hasła?");
                        isOk = false;
                    }
                    if(!isOk) return;

                    final Thread loginThread = new LoginThread(loginET.getText().toString().trim(), passET.getText().toString().trim());
                    loadingDialog = new ProgressDialog(LoginActivity.this, android.R.style.Theme_DeviceDefault_Light_DarkActionBar);
                    loadingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    loadingDialog = ProgressDialog.show(LoginActivity.this,
                            "Logowanie", "proszę czekać", true, true,
                            new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    loginThread.interrupt();
                                }
                            });

                    loginThread.start();


            }
        });

        passET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                button.performClick();
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(GradeOnlineProvider.getInstance().isActiveSession()){
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
        }
    }



    private class LoginThread extends Thread{
        private String log, pswd;
        public LoginThread(String log, String pswd){
            this.log=log;
            this.pswd = pswd;
        }
        public void run() {
            try{

  //final int result = GradeProvider.LOGIN_OK;
                if(isInterrupted()) return;
                final int result = provider.login(log, pswd);
                if(isInterrupted()) {
                    provider.logout();
                    return;
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    public void run() {
                        switch (result) {
                            case GradeProvider.LOGIN_OK:
                                SharedPreferences sharedPreferences = getSharedPreferences(TIPApplication.PREFS_NAME,MODE_PRIVATE);
                                SharedPreferences.Editor preferencesEditor =
                                        sharedPreferences.edit();
                                preferencesEditor.putString(TIPApplication.PREFS_LOGIN_KEY, log);
                                preferencesEditor.putString(TIPApplication.PREFS_PASSWORD_KEY, pswd);
                                preferencesEditor.commit();

                                if(sharedPreferences.getBoolean(getResources().getString(R.string.pref_sync_key),true))
                                CheckGradesEventReceiver.startMonitoring(getApplicationContext());

                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                break;
                            case GradeProvider.LOGIN_WRONG:
                                loginET.setError("Błędne dane dostępu!");
                                passET.setError("Błędne dane dostępu!");
                                break;
                        }
                        loadingDialog.dismiss();
                    }
                });
            }catch (final Exception e){
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    public void run() {
                        if(e.getClass().equals(NoInternetConnection.class)){
                            Toast.makeText(LoginActivity.this,R.string.toast_no_internet_conn, Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(LoginActivity.this, R.string.toast_sth_went_wrong, Toast.LENGTH_SHORT).show();
                        }
                        loadingDialog.dismiss();
                    }
                });
            }
        }

        @Override
        public void interrupt() {
            super.interrupt();
        }
    }



}